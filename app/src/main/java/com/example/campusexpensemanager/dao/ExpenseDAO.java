package com.example.campusexpensemanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.model.Expense;
import com.example.campusexpensemanager.model.ExpenseCategory;
import com.example.campusexpensemanager.model.ExpenseCategoryTotal;
import com.example.campusexpensemanager.NotificationHelper; // Đã đổi package thành utils
import com.example.campusexpensemanager.dao.BudgetDAO;
import com.example.campusexpensemanager.model.Fixedcosts;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {
    private DatabaseHelper dbHelper;
    private Context context; // Cần Context cho Transaction và Notification
    private static final String TAG = "ExpenseDAO";

    public ExpenseDAO(Context context) {
        this.context = context; // Khởi tạo Context
        dbHelper = new DatabaseHelper(context);
    }

    // XÓA PHƯƠNG THỨC add(Expense e) CŨ.
    // Thay thế bằng saveNewExpenseTransaction() để đảm bảo tính toàn vẹn.

    // =================================================================
    // =========== PHƯƠNG THỨC GIAO DỊCH (TRANSACTION LOGIC) ===========
    // =================================================================

    // 1. Lưu Chi tiêu Mới (INSERT + UPDATE Ngân sách)
    /**
     * Lưu chi tiêu mới và tự động trừ số tiền khỏi ngân sách hiện tại bằng SQL Transaction.
     * @param expense: Đối tượng chi tiêu mới.
     * @return ID của chi tiêu mới, hoặc -1 nếu thất bại.
     */
    public long saveNewExpenseTransaction(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long expenseId = -1;

        // Bắt đầu Giao dịch (Transaction)
        db.beginTransaction();

        try {
            // 1. Thêm chi tiêu vào bảng CHI_TIEU
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.CT_LOAI_ID, expense.getLoaiId());
            values.put(DatabaseHelper.CT_SO_TIEN, expense.getSoTien());
            values.put(DatabaseHelper.CT_NGAY, expense.getNgay());
            values.put(DatabaseHelper.CT_GHI_CHU, expense.getGhiChu());
            values.put(DatabaseHelper.CT_THANG_NAM, expense.getThangNam());

            expenseId = db.insert(DatabaseHelper.TABLE_CHI_TIEU, null, values);

            if (expenseId > 0) {
                // 2. Cập nhật Ngân sách (Trừ số tiền chi tiêu)
                BudgetDAO budgetDAO = new BudgetDAO(context);
                // Sử dụng số tiền âm để trừ khỏi số tiền còn lại
                boolean budgetUpdated = budgetDAO.adjustRemainingBudget(-expense.getSoTien());

                if (budgetUpdated) {
                    checkBudgetAlert(); // <<< GỌI CHECK ALERT SAU KHI DB ĐƯỢC CẬP NHẬT
                    db.setTransactionSuccessful(); // Đánh dấu giao dịch thành công
                } else {
                    Log.e(TAG, "Lỗi: Không thể cập nhật Ngân sách.");
                    expenseId = -1;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi SQL Transaction khi thêm Chi tiêu: " + e.getMessage());
            expenseId = -1;
        } finally {
            db.endTransaction(); // Commit hoặc Rollback
            db.close();
        }
        return expenseId;
    }

    // 2. Sửa Chi tiêu (UPDATE + Điều chỉnh Ngân sách)
    /**
     * Sửa chi tiêu hiện có và điều chỉnh số tiền còn lại của ngân sách.
     * @param oldExpense: Chi tiêu cũ trước khi sửa.
     * @param newExpense: Chi tiêu mới sau khi sửa.
     * @return true nếu thành công.
     */
    public boolean adjustExpenseTransaction(Expense oldExpense, Expense newExpense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();

        try {
            // 1. Cập nhật chi tiêu trong bảng CHI_TIEU
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.CT_LOAI_ID, newExpense.getLoaiId());
            values.put(DatabaseHelper.CT_SO_TIEN, newExpense.getSoTien());
            values.put(DatabaseHelper.CT_NGAY, newExpense.getNgay());
            values.put(DatabaseHelper.CT_GHI_CHU, newExpense.getGhiChu());
            values.put(DatabaseHelper.CT_THANG_NAM, newExpense.getThangNam());

            int rowsAffected = db.update(DatabaseHelper.TABLE_CHI_TIEU, values,
                    DatabaseHelper.CT_ID + " = ?",
                    new String[]{String.valueOf(newExpense.getId())});

            if (rowsAffected > 0) {
                // 2. Điều chỉnh Ngân sách
                BudgetDAO budgetDAO = new BudgetDAO(context);

                // Adjustment: Cộng lại tiền cũ, trừ tiền mới
                double adjustmentAmount = oldExpense.getSoTien() - newExpense.getSoTien();

                boolean budgetUpdated = budgetDAO.adjustRemainingBudget(adjustmentAmount);

                if (budgetUpdated) {
                    checkBudgetAlert(); // <<< GỌI CHECK ALERT SAU KHI DB ĐƯỢC CẬP NHẬT
                    db.setTransactionSuccessful();
                    success = true;
                } else {
                    Log.e(TAG, "Lỗi: Không thể điều chỉnh Ngân sách khi sửa Chi tiêu.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi SQL Transaction khi sửa Chi tiêu: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    // 3. Xóa Chi tiêu (DELETE + Điều chỉnh Ngân sách)
    /**
     * Xóa chi tiêu và cộng lại số tiền vào ngân sách hiện tại.
     * @param expense: Đối tượng chi tiêu cần xóa (chỉ cần ID và SoTien).
     * @return true nếu thành công.
     */
    public boolean deleteExpenseTransaction(Expense expense) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean success = false;

        db.beginTransaction();

        try {
            // 1. Xóa chi tiêu khỏi bảng CHI_TIEU
            int rowsAffected = db.delete(DatabaseHelper.TABLE_CHI_TIEU,
                    DatabaseHelper.CT_ID + " = ?",
                    new String[]{String.valueOf(expense.getId())});

            if (rowsAffected > 0) {
                // 2. Điều chỉnh Ngân sách (Cộng lại số tiền đã xóa)
                BudgetDAO budgetDAO = new BudgetDAO(context);

                // Sử dụng số tiền dương để cộng lại vào số tiền còn lại
                boolean budgetUpdated = budgetDAO.adjustRemainingBudget(expense.getSoTien());

                if (budgetUpdated) {
                    checkBudgetAlert(); // <<< GỌI CHECK ALERT SAU KHI DB ĐƯỢC CẬP NHẬT
                    db.setTransactionSuccessful();
                    success = true;
                } else {
                    Log.e(TAG, "Lỗi: Không thể điều chỉnh Ngân sách khi xóa Chi tiêu.");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi SQL Transaction khi xóa Chi tiêu: " + e.getMessage());
        } finally {
            db.endTransaction();
            db.close();
        }
        return success;
    }

    // =================================================================
    // =========== LOGIC HỖ TRỢ & CẢNH BÁO (Giữ nguyên) ================
    // =================================================================

    // Logic kiểm tra Ngân sách và tạo thông báo
    private void checkBudgetAlert() {
        BudgetDAO budgetDAO = new BudgetDAO(context);
        Fixedcosts currentBudget = budgetDAO.getOrCreateCurrentBudget();

        if (currentBudget == null || currentBudget.getSoTienDuKien() <= 0) return;

        // Lấy chi tiêu biến đổi (dữ liệu trong CHI_TIEU)
        String currentMonth = budgetDAO.getOrCreateCurrentBudget().getThangNam();
        double variableExpense = getTotalByMonth(currentMonth);

        // Lấy chi phí cố định
        double totalFixedCosts = budgetDAO.getTotalFixedCosts();

        // Số tiền còn lại = Dự kiến - (Biến đổi + Cố định)
        double remaining = currentBudget.getSoTienDuKien() - (variableExpense + totalFixedCosts);

        // Tính ngưỡng (10% so với chi phí ban đầu)
        final double ALERT_THRESHOLD_PERCENT = 0.10;
        double alertThreshold = currentBudget.getSoTienDuKien() * ALERT_THRESHOLD_PERCENT;
        Log.d(TAG, "Remaining: " + remaining);
        Log.d(TAG, "Threshold (10%): " + alertThreshold);
        // Kiểm tra điều kiện thông báo
        if (remaining <= alertThreshold) {
            // Gọi NotificationHelper để hiển thị cảnh báo
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.showBudgetAlert(remaining);
        }
    }

    // Lấy danh sách chi tiêu theo tháng (Giữ nguyên)
    public List<Expense> getByMonth(String thangNam) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // ... (Giữ nguyên logic Cursor) ...
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

    // Lấy tất cả loại chi phí (cho Spinner) (Giữ nguyên)
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

    // Tính tổng chi tiêu biến đổi trong tháng (Giữ nguyên)
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

    // Lấy tổng chi tiêu theo từng danh mục trong tháng (Giữ nguyên)
    public List<ExpenseCategoryTotal> getCategoryTotalsByMonth(String thangNam) {
        List<ExpenseCategoryTotal> totals = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT lc." + DatabaseHelper.LOAI_TEN + ", SUM(ct." + DatabaseHelper.CT_SO_TIEN + ") AS TotalAmount " +
                "FROM " + DatabaseHelper.TABLE_CHI_TIEU + " ct " +
                "JOIN " + DatabaseHelper.TABLE_LOAI_CHI_PHI + " lc ON ct." + DatabaseHelper.CT_LOAI_ID + " = lc." + DatabaseHelper.LOAI_ID +
                " WHERE ct." + DatabaseHelper.CT_THANG_NAM + " = ? " +
                " GROUP BY lc." + DatabaseHelper.LOAI_TEN +
                " ORDER BY TotalAmount DESC";

        Cursor c = db.rawQuery(query, new String[]{thangNam});

        while (c.moveToNext()) {
            String categoryName = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.LOAI_TEN));
            double totalAmount = c.getDouble(c.getColumnIndexOrThrow("TotalAmount"));
            totals.add(new ExpenseCategoryTotal(categoryName, totalAmount));
        }
        c.close();
        return totals;
    }
}