
package com.hotelise.simple;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class SimpleCheckoutSolutionTest {

    @Test
    void add_null_quantity_means_one_and_subtotals() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), null); // null -> 1
        cart.add("B", Money.of("30.00"), 2);
        assertThat(cart.subtotal().toString()).isEqualTo("110.00");
    }

    @Test
    void reject_non_positive_quantity() {
        Cart cart = new Cart();
        assertThatThrownBy(() -> cart.add("A", Money.of("10.00"), 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cart.add("A", Money.of("10.00"), -5)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void merges_lines_by_sku() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), 1);
        cart.add("A", Money.of("50.00"), 2); // merges -> qty 3
        assertThat(cart.subtotal().toString()).isEqualTo("150.00");
    }

    @Test
    void unit_price_mismatch_is_rejected() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), 1);
        assertThatThrownBy(() -> cart.add("A", Money.of("49.99"), 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("unit price mismatch");
    }

    @Test
    void multiBuy_3for2_on_A() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), 3);

        Checkout.DiscountRule threeForTwo = Checkout.multiBuy("A", 3, 2);
        Checkout checkout = new Checkout(List.of(threeForTwo));

        assertThat(threeForTwo.discount(cart).toString()).isEqualTo("50.00");
        assertThat(checkout.total(cart).toString()).isEqualTo("100.00");
    }

    @Test
    void threshold_10_percent_when_subtotal_at_least_100() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), 2); // 100
        cart.add("B", Money.of("30.00"), 1); // 130

        Checkout.DiscountRule tenPercentOver100 = Checkout.thresholdPercent(Money.of("100.00"), 10);
        Checkout checkout = new Checkout(List.of(tenPercentOver100));

        assertThat(tenPercentOver100.discount(cart).toString()).isEqualTo("13.00");
        assertThat(checkout.total(cart).toString()).isEqualTo("117.00");
    }

    @Test
    void rules_compose() {
        Cart cart = new Cart();
        cart.add("A", Money.of("50.00"), 3); // 150

        Checkout.DiscountRule threeForTwo = Checkout.multiBuy("A", 3, 2); // 50 off
        Checkout.DiscountRule tenPercentOver100 = Checkout.thresholdPercent(Money.of("100.00"), 10); // 15 off

        Checkout checkout = new Checkout(List.of(threeForTwo, tenPercentOver100));
        assertThat(checkout.total(cart).toString()).isEqualTo("85.00");
    }

    @Test
    void threshold_does_not_apply_below_cutoff() {
        Cart cart = new Cart();
        cart.add("A", Money.of("49.99"), 2); // 99.98 < 100.00
        Checkout.DiscountRule tenPercentOver100 = Checkout.thresholdPercent(Money.of("100.00"), 10);
        Checkout checkout = new Checkout(List.of(tenPercentOver100));
        assertThat(checkout.total(cart).toString()).isEqualTo("99.98");
    }

    @Test
    void total_is_clamped_non_negative_even_if_rule_over_discounts() {
        Cart cart = new Cart();
        cart.add("A", Money.of("10.00"), 1); // subtotal 10

        // Malicious / buggy rule returning too much discount
        Checkout.DiscountRule bogus = c -> Money.of("999.00");
        Checkout checkout = new Checkout(List.of(bogus));

        assertThat(checkout.total(cart).toString()).isEqualTo("0.00");
    }
}
