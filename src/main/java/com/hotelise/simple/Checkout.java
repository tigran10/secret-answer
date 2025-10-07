
package com.hotelise.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Checkout {
    @FunctionalInterface
    public interface DiscountRule {
        Money discount(Cart cart);
    }

    private final List<DiscountRule> rules = new ArrayList<>();

    public Checkout(List<DiscountRule> rules) {
        this.rules.addAll(Objects.requireNonNull(rules));
    }

    public Money total(Cart cart) {
        Money subtotal = cart.subtotal();
        Money totalDiscount = Money.ZERO;
        for (DiscountRule r : rules) {
            totalDiscount = totalDiscount.plus(r.discount(cart));
        }
        Money result = subtotal.minus(totalDiscount);
        return result.clampNonNegative();
    }

    // ---- Static factories: no extra classes needed ----
    public static DiscountRule multiBuy(String sku, int n, int m) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku required");
        if (n <= 1 || m <= 0 || m >= n) throw new IllegalArgumentException("Require n>1 and 0<m<n");
        return cart -> {
            Cart.Line line = cart.items().get(sku);
            if (line == null) return Money.ZERO;
            int groups = line.quantity() / n;
            int freeUnits = groups * (n - m);
            if (freeUnits <= 0) return Money.ZERO;
            return line.unitPrice().times(freeUnits);
        };
    }

    public static DiscountRule thresholdPercent(Money threshold, int percent) {
        if (threshold == null) throw new IllegalArgumentException("threshold required");
        if (percent <= 0 || percent >= 100) throw new IllegalArgumentException("percent 1..99");
        return cart -> {
            Money subtotal = cart.subtotal();
            if (subtotal.compareTo(threshold) >= 0) {
                return subtotal.percentage(percent);
            }
            return Money.ZERO;
        };
    }
}
