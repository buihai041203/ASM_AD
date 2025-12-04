package com.example.campusexpensemanager;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String note;        // Tên chi phí (VD: Mua rau)
    private double amount;      // Số tiền
    private String date;        // Ngày
    private String category;    // Danh mục
    private String description; // Mô tả chi tiết

    public Transaction(int id, String note, double amount, String date, String category, String description) {
        this.id = id;
        this.note = note;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public Transaction(String reason, String currentTime, double amount) {

    }

    // Getters
    public int getId() { return id; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }

    public Object getTitle() {
                    return null;
    }

    public int getTime() {
        return 0;
    }
}