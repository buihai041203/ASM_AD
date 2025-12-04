package com.example.campusexpensemanager;

import java.io.Serializable;

public class Transaction implements Serializable {
    private int id;
    private String note;        // Tên khoản chi (VD: Mua cơm)
    private double amount;      // Số tiền
    private String date;        // Ngày (YYYY-MM-DD)
    private String category;    // Tên Danh mục (VD: Ăn uống)
    private String description; // Mô tả chi tiết

    // Constructor chuẩn dùng khi đọc từ DB và Thêm mới
    public Transaction(int id, String note, double amount, String date, String category, String description) {
        this.id = id;
        this.note = note;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    // Getters
    public int getId() { return id; }
    public String getNote() { return note; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
}