package com.example.campusexpensemanager.model;

public class Fixedcosts {
    private int id;
    private String thangNam;        // yyyy-MM
    private double soTienDuKien;
    private double soTienConLai;

    public Fixedcosts() {}

    public Fixedcosts(String thangNam, double soTienDuKien, double soTienConLai) {
        this.thangNam = thangNam;
        this.soTienDuKien = soTienDuKien;
        this.soTienConLai = soTienConLai;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getThangNam() { return thangNam; }
    public void setThangNam(String thangNam) { this.thangNam = thangNam; }
    public double getSoTienDuKien() { return soTienDuKien; }
    public void setSoTienDuKien(double soTienDuKien) { this.soTienDuKien = soTienDuKien; }
    public double getSoTienConLai() { return soTienConLai; }
    public void setSoTienConLai(double soTienConLai) { this.soTienConLai = soTienConLai; }
}