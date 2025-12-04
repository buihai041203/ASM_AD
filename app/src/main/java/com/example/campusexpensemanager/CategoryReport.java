package com.example.campusexpensemanager;

public class CategoryReport {
    private String categoryName;
    private double totalAmount;

    public CategoryReport(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}