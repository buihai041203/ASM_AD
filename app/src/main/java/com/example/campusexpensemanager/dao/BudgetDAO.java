package com.example.campusexpensemanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.model.Fixedcosts; // Sử dụng model Fixedcosts (thực chất là Budget)
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BudgetDAO {
    private DatabaseHelper dbHelper;

    public BudgetDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 1. Lấy Ngân sách của tháng hiện tại (tự động lấy yyyy-MM)
    public Fixedcosts getOrCreateCurrentBudget() {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        return getBudgetByMonth(currentMonth);
    }

    // Lấy Ngân sách theo tháng
    public Fixedcosts getBudgetByMonth(String thangNam) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NGAN_SACH + " WHERE " + DatabaseHelper.NS_THANG_NAM + " = ?", new String[]{thangNam});
        Fixedcosts budget = null;

        if (c.moveToFirst()) {
            budget = new Fixedcosts();
            budget.setId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.NS_ID)));
            budget.setThangNam(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.NS_THANG_NAM)));
            budget.setSoTienDuKien(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.NS_SO_TIEN_DU_KIEN)));
            budget.setSoTienConLai(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.NS_SO_TIEN_CON_LAI)));
        }
        c.close();

        // Nếu không có, tạo một bản ghi Ngân sách mới cho tháng
        if (budget == null) {
            long newId = createBudget(thangNam, 0); // Ngân sách dự kiến ban đầu là 0
            if (newId != -1) {
                // Lấy lại đối tượng vừa tạo để trả về
                budget = new Fixedcosts(thangNam, 0, 0);
                budget.setId((int) newId);
            }
        }
        return budget;
    }

    // Thêm bản ghi Ngân sách mới (Chỉ dùng nội bộ khi tháng mới bắt đầu)
    private long createBudget(String thangNam, double soTien) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NS_THANG_NAM, thangNam);
        values.put(DatabaseHelper.NS_SO_TIEN_DU_KIEN, soTien);
        values.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, soTien);
        return db.insert(DatabaseHelper.TABLE_NGAN_SACH, null, values);
    }

    // 2. Cập nhật Ngân sách Dự kiến cho tháng
    public boolean updateBudget(String thangNam, double soTienDuKienMoi) {
        // Cần tính toán số tiền đã chi để bảo toàn "số tiền còn lại"
        Fixedcosts oldBudget = getBudgetByMonth(thangNam);
        if (oldBudget == null) {
            return createBudget(thangNam, soTienDuKienMoi) != -1;
        }

        // Lấy số tiền đã chi của tháng đó
        double soTienDaChi = oldBudget.getSoTienDuKien() - oldBudget.getSoTienConLai();

        // Số tiền còn lại mới = Dự kiến mới - Đã chi
        double soTienConLaiMoi = soTienDuKienMoi - soTienDaChi;

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NS_SO_TIEN_DU_KIEN, soTienDuKienMoi);
        values.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, soTienConLaiMoi);

        int rows = db.update(DatabaseHelper.TABLE_NGAN_SACH, values,
                DatabaseHelper.NS_THANG_NAM + " = ?", new String[]{thangNam});
        return rows > 0;
    }
}