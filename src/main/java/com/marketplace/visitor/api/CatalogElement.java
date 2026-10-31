package com.marketplace.visitor.api;

/**
 * Базовый элемент дерева каталога (Composite).
 */
public interface CatalogElement {
    <R> R accept(CatalogVisitor<R> visitor);
}