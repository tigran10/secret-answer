
package com.hotelise.simple;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class Cart {
    // sku -> [unitPrice, qty]
    private final Map<String, Line> lines = new LinkedHashMap<>();

    public void add(String sku, Money unitPrice, Integer quantity) {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("sku required");
        if (unitPrice == null) throw new IllegalArgumentException("unitPrice required");

        int qty = (quantity == null) ? 1 : quantity.intValue();
        if (qty <= 0) throw new IllegalArgumentException("quantity must be positive");

        Line existing = lines.get(sku);
        if (existing == null) {
            lines.put(sku, new Line(unitPrice, qty));
        } else {
            if (!existing.unitPrice.equals(unitPrice)) {
                throw new IllegalArgumentException("unit price mismatch for SKU " + sku);
            }
            lines.put(sku, new Line(unitPrice, existing.quantity + qty));
        }
    }

    public Map<String, Line> items() {
        return Collections.unmodifiableMap(lines);
    }

    public Money subtotal() {
        Money total = Money.ZERO;
        for (Map.Entry<String, Line> e : lines.entrySet()) {
            Line line = e.getValue();
            total = total.plus(line.unitPrice.times(line.quantity));
        }
        return total;
    }

    public record Line(Money unitPrice, int quantity) {
        public Line {
            if (unitPrice == null) throw new IllegalArgumentException("unitPrice required");
            if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        }
    }
}
