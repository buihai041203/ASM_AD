package com.example.campusexpensemanager.model;

public class Expense {
    private int id;
    private int loaiId;
    private String tenLoai;
    private double soTien;
    private String ngay;        // yyyy-MM-dd
    private String ghiChu;
    private String thangNam;    // yyyy-MM

    public Expense() {}

    // Constructor đầy đủ
    public Expense(int id, int loaiId, String tenLoai, double soTien, String ngay, String ghiChu, String thangNam) {
        this.id = id;
        this.loaiId = loaiId;
        this.tenLoai = tenLoai;
        this.soTien = soTien;
        this.ngay = ngay;
        this.ghiChu = ghiChu;
        this.thangNam = thangNam;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getLoaiId() { return loaiId; }
    public void setLoaiId(int loaiId) { this.loaiId = loaiId; }
    public String getTenLoai() { return tenLoai; }
    public void setTenLoai(String tenLoai) { this.tenLoai = tenLoai; }
    public double getSoTien() { return soTien; }
    public void setSoTien(double soTien) { this.soTien = soTien; }
    public String getNgay() { return ngay; }
    public void setNgay(String ngay) { this.ngay = ngay; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
    public String getThangNam() { return thangNam; }
    public void setThangNam(String thangNam) { this.thangNam = thangNam; }
}