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

    public double getTotalFixedCosts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        double total = 0;
        // Lấy SUM của cột CD_SO_TIEN từ bảng CHI_PHI_CO_DINH
        Cursor c = db.rawQuery("SELECT SUM(" + DatabaseHelper.CD_SO_TIEN + ") FROM " + DatabaseHelper.TABLE_CHI_PHI_CO_DINH, null);
        if (c != null && c.moveToFirst()) {
            total = c.getDouble(0);
        }
        if (c != null) c.close();
        return total;
    }
    // Thêm bản ghi Ngân sách mới (Chỉ dùng nội bộ khi thá  ng mới bắt đầu)
    private long createBudget(String thangNam, double soTienDuKien) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        double totalFixedCosts = getTotalFixedCosts(); // Lấy tổng CPCD
        double soTienConLai = soTienDuKien - totalFixedCosts; // Trừ CPCD

        values.put(DatabaseHelper.NS_THANG_NAM, thangNam);
        values.put(DatabaseHelper.NS_SO_TIEN_DU_KIEN, soTienDuKien);
        values.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, soTienConLai);

        long result = db.insert(DatabaseHelper.TABLE_NGAN_SACH, null, values);
        db.close();
        return result;
    }

    // 2. Cập nhật Ngân sách Dự kiến cho tháng
    public boolean updateBudget(String thangNam, double soTienDuKienMoi) {
        // ... (Giữ nguyên phần lấy oldBudget) ...
        Fixedcosts oldBudget = getBudgetByMonth(thangNam);
        if (oldBudget == null) {
            // Nếu không tồn tại, tạo mới và trừ CPCD
            return createBudget(thangNam, soTienDuKienMoi) != -1;
        }

        // Tổng chi phí đã chi = (Dự kiến cũ - Còn lại cũ)
        // Lưu ý: Số tiền này bao gồm cả chi phí cố định (vì nó đã bị trừ đi ở lần set trước) và chi phí biến đổi
        double soTienDaChiTong = oldBudget.getSoTienDuKien() - oldBudget.getSoTienConLai();

        // Lấy Tổng Chi Phí Cố Định MỚI (phòng trường hợp người dùng thêm/xóa CPCD sau khi set ngân sách)
        double totalFixedCosts = getTotalFixedCosts();

        // Tính Chi phí Biến đổi đã chi:
        double soTienDaChiBienDoi = soTienDaChiTong - totalFixedCosts;

        // Số tiền còn lại mới = Dự kiến mới - (Chi phí Cố định + Chi phí Biến đổi Đã chi)
        double soTienConLaiMoi = soTienDuKienMoi - totalFixedCosts - soTienDaChiBienDoi;

        // ... (Giữ nguyên phần UPDATE DB và đóng DB) ...
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NS_SO_TIEN_DU_KIEN, soTienDuKienMoi);
        values.put(DatabaseHelper.NS_SO_TIEN_CON_LAI, soTienConLaiMoi);

        int rows = db.update(DatabaseHelper.TABLE_NGAN_SACH, values,
                DatabaseHelper.NS_THANG_NAM + " = ?", new String[]{thangNam});
        db.close();
        return rows > 0;
    }
    public boolean adjustRemainingBudget(double adjustmentAmount) {
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.execSQL(
                    "UPDATE " + DatabaseHelper.TABLE_NGAN_SACH +
                            " SET " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " = " + DatabaseHelper.NS_SO_TIEN_CON_LAI + " + ?" +
                            " WHERE " + DatabaseHelper.NS_THANG_NAM + " = ?",
                    new Object[]{adjustmentAmount, currentMonth}
            );
            return true;
        } catch (Exception e) {
            return false;
        }
        // Không đóng DB ở đây! Việc đóng sẽ do ExpenseDAO đảm nhiệm sau khi kết thúc Transaction.
    }
}