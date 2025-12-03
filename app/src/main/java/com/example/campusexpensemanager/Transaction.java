package com.example.campusexpensemanager;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String note;        // Tên khoản chi (VD: Mua cơm)
    private double amount;      // Số tiền
    private String date;        // Ngày
    private String category;    // Danh mục
    private String description; // Mô tả thêm

    // Constructor đầy đủ dùng khi đọc từ DB
    public Transaction(int id, String note, double amount, String date, String category, String description) {
        this.id = id;
        this.note = note;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public Transaction(String salary, String note, int amount) {

    }

    // Getters
    public int getId() { return id; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
}