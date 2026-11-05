package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.PriceTagFormattingVisitor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceTagFormattingVisitorTest {

    @Test
    void shouldFormatOneTagPerProductInTreeOrder() {
        Category category = new Category("CAT-1", "Mixed");
        category.addChild(new PhysicalProduct("P1", "Mouse", Money.of(20.0), 0.2, 3));
        category.addChild(new GiftCardProduct("G1", "Gift Card", Money.of(50.0)));

        List<String> tags = category.accept(new PriceTagFormattingVisitor());

        assertEquals(List.of(
                "[PHYSICAL] Mouse - $20.00 (Weight: 0.2 kg)",
                "[GIFT CARD] Gift Card - Denomination: $50.00"
        ), tags);
    }
}
