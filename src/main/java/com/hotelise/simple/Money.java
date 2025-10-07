
package com.hotelise.simple;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class Money implements Comparable<Money> {
    private static final int SCALE = 2;
    private static final RoundingMode MODE = RoundingMode.HALF_UP;

    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal amount;

    private Money(BigDecimal amount) {
        this.amount = amount.setScale(SCALE, MODE);
    }

    public static Money of(String decimal) {
        return new Money(new BigDecimal(decimal));
    }

    public static Money of(BigDecimal decimal) {
        return new Money(decimal);
    }

    public BigDecimal asBigDecimal() { return amount; }

    public Money plus(Money other) { return new Money(this.amount.add(other.amount)); }

    public Money minus(Money other) { return new Money(this.amount.subtract(other.amount)); }

    public Money times(int factor) { return new Money(this.amount.multiply(BigDecimal.valueOf(factor))); }

    public Money percentage(int percent) {
        BigDecimal p = amount.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), SCALE, MODE);
        return new Money(p);
    }

    public Money clampNonNegative() {
        return amount.signum() < 0 ? ZERO : this;
    }

    @Override public String toString() { return amount.toPlainString(); }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return Objects.equals(amount, money.amount);
    }

    @Override public int hashCode() { return Objects.hash(amount); }

    @Override public int compareTo(Money o) { return this.amount.compareTo(o.amount); }
}
