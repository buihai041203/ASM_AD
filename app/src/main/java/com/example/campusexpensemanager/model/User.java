package com.example.campusexpensemanager.model;

public class User {
    private int userId;
    private String hoTen;
    private String email;
    private String matKhau;

    public User() {}

    public User(String hoTen, String email, String matKhau) {
        this.hoTen = hoTen;
        this.email = email;
        this.matKhau = matKhau;
    }

    // Getter & Setter
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) { this.matKhau = matKhau; }
}