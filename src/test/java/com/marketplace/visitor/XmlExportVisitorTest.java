package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.XmlExportVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XmlExportVisitorTest {
    private Catalog catalog;

    @BeforeEach
    void setUp() {
        catalog = new Catalog("ROOT", "Catalog");
        Category category = new Category("CAT-1", "Devices and Services");

        category.addChild(new PhysicalProduct("P1", "Laptop", Money.of(100.0), 2.0, 5));
        category.addChild(new DigitalProduct("P2", "E-Book", Money.of(50.0), 500.0));
        category.addChild(new ServiceProduct("P3", "Installation", Money.of(30.0), 3));
        category.addChild(new SubscriptionProduct("P4", "Premium Plan", Money.of(10.0), 12));

        catalog.addCategory(category);
    }

    @Test
    void shouldGenerateWellFormedNestedXml() {
        XmlExportVisitor visitor = new XmlExportVisitor();
        String xml = catalog.accept(visitor);

        assertTrue(xml.startsWith("<catalog id=\"ROOT\" name=\"Catalog\">"));
        assertTrue(xml.contains("  <category id=\"CAT-1\" name=\"Devices and Services\">"));
        assertTrue(xml.contains("    <physicalProduct id=\"P1\""));
        assertTrue(xml.contains("    <digitalProduct id=\"P2\""));
        assertTrue(xml.contains("    <serviceProduct id=\"P3\""));
        assertTrue(xml.contains("    <subscriptionProduct id=\"P4\""));
        assertTrue(xml.endsWith("</catalog>\n"));
    }

    @Test
    void shouldProduceExactNestingForNestedCategories() {
        Catalog root = new Catalog("R", "Shop");
        Category outer = new Category("C1", "Outer");
        Category inner = new Category("C2", "Inner");
        inner.addChild(new GiftCardProduct("G1", "Card", Money.of(10.0)));
        outer.addChild(inner);
        root.addCategory(outer);

        String expected = """
                <catalog id="R" name="Shop">
                  <category id="C1" name="Outer">
                    <category id="C2" name="Inner">
                      <giftCardProduct id="G1" name="Card" denomination="$10.00"/>
                    </category>
                  </category>
                </catalog>
                """;
        assertEquals(expected, root.accept(new XmlExportVisitor()));
    }

    @Test
    void shouldEscapeSpecialCharactersInAttributes() {
        Category category = new Category("C&1", "Tom & Jerry <\"Kids\">");

        String xml = category.accept(new XmlExportVisitor());

        assertEquals("<category id=\"C&amp;1\" name=\"Tom &amp; Jerry &lt;&quot;Kids&quot;&gt;\">\n</category>\n", xml);
    }
}
