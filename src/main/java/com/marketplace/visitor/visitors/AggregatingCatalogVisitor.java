package com.marketplace.visitor.visitors;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;
import com.marketplace.visitor.elements.Catalog;
import com.marketplace.visitor.elements.Category;

import java.util.List;

/**
 * Base class for visitors whose container result is "the combination of the children's results"
 * (sum of money, merged lists, merged reports).
 * <p>
 * Traversal stays in the visitor (see README), but is written once here instead of being
 * copied into every visitor. Subclasses keep no mutable state, so one instance can be
 * reused many times and shared between threads.
 */
public abstract class AggregatingCatalogVisitor<R> implements CatalogVisitor<R> {

    /** Result for an empty container (neutral element of {@link #combine}). */
    protected abstract R empty();

    /** Merges two partial results. */
    protected abstract R combine(R left, R right);

    @Override
    public final R visit(Catalog catalog) {
        return combineChildren(catalog.getCategories());
    }

    @Override
    public final R visit(Category category) {
        return combineChildren(category.getChildren());
    }

    private R combineChildren(List<? extends CatalogElement> children) {
        R result = empty();
        for (CatalogElement child : children) {
            result = combine(result, child.accept(this));
        }
        return result;
    }
}
