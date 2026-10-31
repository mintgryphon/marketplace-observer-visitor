package com.marketplace.visitor.visitors;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.*;

public class XmlExportVisitor implements CatalogVisitor<String> {
    private final StringBuilder xml = new StringBuilder();
    private int indent = 0;

    private void appendIndent() {
        xml.append("  ".repeat(Math.max(0, indent)));
    }

    @Override
    public String visit(Catalog catalog) {
        appendIndent();
        xml.append("<catalog id=\"").append(catalog.getId())
                .append("\" name=\"").append(catalog.getName()).append("\">\n");
        indent++;
        for (Category category : catalog.getCategories()) {
            category.accept(this);
        }
        indent--;
        appendIndent();
        xml.append("</catalog>\n");
        return xml.toString();
    }

    @Override
    public String visit(Category category) {
        appendIndent();
        xml.append("<category id=\"").append(category.getId())
                .append("\" name=\"").append(category.getName()).append("\">\n");
        indent++;
        for (CatalogElement child : category.getChildren()) {
            child.accept(this);
        }
        indent--;
        appendIndent();
        xml.append("</category>\n");
        return xml.toString();
    }

    @Override
    public String visit(PhysicalProduct product) {
        appendIndent();
        xml.append("<physicalProduct id=\"").append(product.getId())
                .append("\" name=\"").append(product.getName())
                .append("\" price=\"").append(product.getPrice())
                .append("\" weightKg=\"").append(product.getWeightKg())
                .append("\" stock=\"").append(product.getStockQuantity())
                .append("\"/>\n");
        return xml.toString();
    }

    @Override
    public String visit(DigitalProduct product) {
        appendIndent();
        xml.append("<digitalProduct id=\"").append(product.getId())
                .append("\" name=\"").append(product.getName())
                .append("\" price=\"").append(product.getPrice())
                .append("\" sizeMb=\"").append(product.getDownloadSizeMb())
                .append("\"/>\n");
        return xml.toString();
    }

    @Override
    public String visit(ServiceProduct product) {
        appendIndent();
        xml.append("<serviceProduct id=\"").append(product.getId())
                .append("\" name=\"").append(product.getName())
                .append("\" hourlyRate=\"").append(product.getHourlyRate())
                .append("\" hours=\"").append(product.getEstimatedHours())
                .append("\"/>\n");
        return xml.toString();
    }

    @Override
    public String visit(SubscriptionProduct product) {
        appendIndent();
        xml.append("<subscriptionProduct id=\"").append(product.getId())
                .append("\" name=\"").append(product.getName())
                .append("\" monthlyFee=\"").append(product.getMonthlyFee())
                .append("\" months=\"").append(product.getPeriodInMonths())
                .append("\"/>\n");
        return xml.toString();
    }

    @Override
    public String visit(GiftCardProduct product) {
        appendIndent();
        xml.append("<giftCardProduct id=\"").append(product.getId())
                .append("\" name=\"").append(product.getName())
                .append("\" denomination=\"").append(product.getDenomination())
                .append("\"/>\n");
        return xml.toString();
    }
}