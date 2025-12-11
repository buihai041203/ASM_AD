package com.example.campusexpensemanager.model;

public class ExpenseCategory {
    private int id;
    private String tenLoai;

    public ExpenseCategory() {}

    public ExpenseCategory(int id, String tenLoai) {
        this.id = id;
        this.tenLoai = tenLoai;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }

    @Override
    public String toString() {
        return tenLoai;
    }
}