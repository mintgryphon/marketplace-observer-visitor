package com.marketplace.visitor;

import com.marketplace.common.Money;
import com.marketplace.visitor.elements.*;
import com.marketplace.visitor.visitors.XmlExportVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}