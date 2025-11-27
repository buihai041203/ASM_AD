package com.example.campusexpensemanager;

public class Transaction {
    private String title;   // Ví dụ: "Tiền lương"
    private String time;    // Ví dụ: "28/11/2025..."
    private double amount;  // Ví dụ: 5000000

    // Constructor (Hàm khởi tạo để AppData dùng được lệnh new Transaction(...))
    public Transaction(String title, String time, double amount) {
        this.title = title;
        this.time = time;
        this.amount = amount;
    }

    // Các hàm Getter để lấy dữ liệu ra hiển thị
    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public double getAmount() {
        return amount;
    }
}