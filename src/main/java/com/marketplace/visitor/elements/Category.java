package com.marketplace.visitor.elements;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Category implements CatalogElement {
    private final String id;
    private final String name;
    private final List<CatalogElement> children = new CopyOnWriteArrayList<>();

    public Category(String id, String name) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public void addChild(CatalogElement element) {
        children.add(Objects.requireNonNull(element));
    }

    public void removeChild(CatalogElement element) {
        children.remove(element);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<CatalogElement> getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}