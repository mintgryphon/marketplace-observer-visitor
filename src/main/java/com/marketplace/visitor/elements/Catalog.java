package com.marketplace.visitor.elements;

import com.marketplace.visitor.api.CatalogElement;
import com.marketplace.visitor.api.CatalogVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Catalog implements CatalogElement {
    private final String id;
    private final String name;
    private final List<Category> categories = new CopyOnWriteArrayList<>();

    public Catalog(String id, String name) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    public void addCategory(Category category) {
        categories.add(Objects.requireNonNull(category));
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Category> getCategories() {
        return Collections.unmodifiableList(categories);
    }

    @Override
    public <R> R accept(CatalogVisitor<R> visitor) {
        return visitor.visit(this);
    }
}