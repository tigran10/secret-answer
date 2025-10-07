
# Simple Checkout Kata — **Solution**

This is a complete, reference implementation for interviewers.

### Highlights
- `Cart.add(String, Money, Integer)` handles `null` quantity as 1; rejects ≤ 0.
- Lines merge by SKU and enforce unit-price consistency.
- `Checkout.total` = subtotal − sum(discounts), clamped to ≥ 0.
- `DiscountRule` factories:
  - `multiBuy(sku, n, m)` — N-for-M discount per full group.
  - `thresholdPercent(threshold, percent)` — applies when subtotal ≥ threshold.
- Money uses `BigDecimal` with scale 2 and HALF_UP.

Run tests:
```bash
mvn -q test
```
