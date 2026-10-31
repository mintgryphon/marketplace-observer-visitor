package com.marketplace.visitor.api;

public interface CatalogElement {
    <R> R accept(CatalogVisitor<R> visitor);
}