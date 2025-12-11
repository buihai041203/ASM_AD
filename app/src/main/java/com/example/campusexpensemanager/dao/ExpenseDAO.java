package com.example.campusexpensemanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.model.Expense;
import com.example.campusexpensemanager.model.ExpenseCategory;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    private DatabaseHelper dbHelper;

    public ExpenseDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Thêm chi tiêu
    public boolean add(Expense e) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.CT_LOAI_ID, e.getLoaiId());
        values.put(DatabaseHelper.CT_SO_TIEN, e.getSoTien());
        values.put(DatabaseHelper.CT_NGAY, e.getNgay());
        values.put(DatabaseHelper.CT_GHI_CHU, e.getGhiChu());
        values.put(DatabaseHelper.CT_THANG_NAM, e.getThangNam());
        long result = db.insert(DatabaseHelper.TABLE_CHI_TIEU, null, values);
        return result != -1;
    }

    // Lấy danh sách chi tiêu theo tháng
    public List<Expense> getByMonth(String thangNam) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT ct.*, lc." + DatabaseHelper.LOAI_TEN +
                " FROM " + DatabaseHelper.TABLE_CHI_TIEU + " ct" +
                " JOIN " + DatabaseHelper.TABLE_LOAI_CHI_PHI + " lc ON ct." + DatabaseHelper.CT_LOAI_ID + " = lc." + DatabaseHelper.LOAI_ID +
                " WHERE ct." + DatabaseHelper.CT_THANG_NAM + " = ?" +
                " ORDER BY ct." + DatabaseHelper.CT_NGAY + " DESC";
        Cursor c = db.rawQuery(query, new String[]{thangNam});
        while (c.moveToNext()) {
            Expense e = new Expense();
            e.setId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CT_ID)));
            e.setLoaiId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.CT_LOAI_ID)));
            e.setTenLoai(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.LOAI_TEN)));
            e.setSoTien(c.getDouble(c.getColumnIndexOrThrow(DatabaseHelper.CT_SO_TIEN)));
            e.setNgay(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CT_NGAY)));
            e.setGhiChu(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CT_GHI_CHU)));
            e.setThangNam(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.CT_THANG_NAM)));
            list.add(e);
        }
        c.close();
        return list;
    }

    // Lấy tất cả loại chi phí (cho Spinner)
    public List<ExpenseCategory> getAllCategories() {
        List<ExpenseCategory> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_LOAI_CHI_PHI, null);
        while (c.moveToNext()) {
            list.add(new ExpenseCategory(
                    c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.LOAI_ID)),
                    c.getString(c.getColumnIndexOrThrow(DatabaseHelper.LOAI_TEN))
            ));
        }
        c.close();
        return list;
    }

    // Tính tổng chi tiêu trong tháng
    public double getTotalByMonth(String thangNam) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT SUM(" + DatabaseHelper.CT_SO_TIEN + ") FROM " +
                DatabaseHelper.TABLE_CHI_TIEU + " WHERE " + DatabaseHelper.CT_THANG_NAM + " = ?", new String[]{thangNam});
        double total = 0;
        if (c.moveToFirst()) {
            total = c.getDouble(0);
        }
        c.close();
        return total;
    }

    // Xóa, sửa… (các bạn thêm sau nếu cần)
}