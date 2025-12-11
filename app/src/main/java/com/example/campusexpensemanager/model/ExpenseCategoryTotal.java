package com.example.campusexpensemanager.model;

public class ExpenseCategoryTotal {
    private String categoryName;
    private double totalAmount;

    public ExpenseCategoryTotal(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    // Getter & Setter
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
}