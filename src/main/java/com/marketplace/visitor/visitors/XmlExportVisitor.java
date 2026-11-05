package com.marketplace.visitor.visitors;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

import java.util.List;

/**
 * Hand-written XML export with correct nesting and escaping.
 * <p>
 * Every visit returns the XML fragment of its own subtree; a container wraps its children's
 * fragments and indents them one level. No fields are mutated, so the same instance can be
 * reused and shared between threads.
 */
public class XmlExportVisitor implements CatalogVisitor<String> {
    private static final String INDENT = "  ";

    @Override
    public String visit(Catalog catalog) {
        return container("catalog", catalog.getCategories(),
                "id", catalog.getId(), "name", catalog.getName());
    }

    @Override
    public String visit(Category category) {
        return container("category", category.getChildren(),
                "id", category.getId(), "name", category.getName());
    }

    @Override
    public String visit(PhysicalProduct product) {
        return leaf("physicalProduct",
                "id", product.getId(),
                "name", product.getName(),
                "price", product.getPrice(),
                "weightKg", product.getWeightKg(),
                "stock", product.getStockQuantity());
    }

    @Override
    public String visit(DigitalProduct product) {
        return leaf("digitalProduct",
                "id", product.getId(),
                "name", product.getName(),
                "price", product.getPrice(),
                "sizeMb", product.getDownloadSizeMb());
    }

    @Override
    public String visit(ServiceProduct product) {
        return leaf("serviceProduct",
                "id", product.getId(),
                "name", product.getName(),
                "hourlyRate", product.getHourlyRate(),
                "hours", product.getEstimatedHours());
    }

    @Override
    public String visit(SubscriptionProduct product) {
        return leaf("subscriptionProduct",
                "id", product.getId(),
                "name", product.getName(),
                "monthlyFee", product.getMonthlyFee(),
                "months", product.getPeriodInMonths());
    }

    @Override
    public String visit(GiftCardProduct product) {
        return leaf("giftCardProduct",
                "id", product.getId(),
                "name", product.getName(),
                "denomination", product.getDenomination());
    }

    private String container(String tag, List<? extends CatalogElement> children, Object... attributes) {
        StringBuilder xml = new StringBuilder();
        xml.append('<').append(tag).append(attributes(attributes)).append(">\n");
        for (CatalogElement child : children) {
            child.accept(this).lines().forEach(line -> xml.append(INDENT).append(line).append('\n'));
        }
        xml.append("</").append(tag).append(">\n");
        return xml.toString();
    }

    private String leaf(String tag, Object... attributes) {
        return "<" + tag + attributes(attributes) + "/>\n";
    }

    /** Renders name/value pairs: ("id", "P1", "name", "Laptop") -> {@code  id="P1" name="Laptop"}. */
    private String attributes(Object... nameValuePairs) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < nameValuePairs.length; i += 2) {
            result.append(' ').append(nameValuePairs[i])
                    .append("=\"").append(escape(String.valueOf(nameValuePairs[i + 1]))).append('"');
        }
        return result.toString();
    }

    private static String escape(String text) {
        StringBuilder escaped = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&' -> escaped.append("&amp;");
                case '<' -> escaped.append("&lt;");
                case '>' -> escaped.append("&gt;");
                case '"' -> escaped.append("&quot;");
                case '\'' -> escaped.append("&apos;");
                default -> escaped.append(c);
            }
        }
        return escaped.toString();
    }
}
