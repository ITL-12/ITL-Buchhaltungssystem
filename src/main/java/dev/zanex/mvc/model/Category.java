package dev.zanex.mvc.model;

public class Category {
    private int id;
    private String name;
    private TransactionType type;
    private String description;
    private boolean isDefault;

    public Category() {
        this.isDefault = false;
    }

    public Category(int id, String name, TransactionType type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.isDefault = false;
    }

    public Category(int id, String name, TransactionType type, String description, boolean isDefault) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.isDefault = isDefault;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Category other = (Category) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}