package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuanLyTaiChinh.db";
    private static final int DB_VERSION = 1;

    // ==================== KHAI BÁO TÊN BẢNG VÀ CỘT (GIỮ NGUYÊN) ====================
    // 1. USER
    public static final String TABLE_USER = "users";
    public static final String USER_ID = "user_id";
    public static final String USER_HO_TEN = "ho_ten";
    public static final String USER_EMAIL = "email";
    public static final String USER_MAT_KHAU = "mat_khau";

    // 2. LOẠI CHI PHÍ
    public static final String TABLE_LOAI_CHI_PHI = "loai_chi_phi";
    public static final String LOAI_ID = "id_phat_sinh";
    public static final String LOAI_TEN = "loai_chi_phi";

    // 3. CHI PHÍ CỐ ĐỊNH
    public static final String TABLE_CHI_PHI_CO_DINH = "chi_phi_co_dinh";
    public static final String CD_ID = "id_co_dinh";
    public static final String CD_TEN = "ten_chi_phi";
    public static final String CD_SO_TIEN = "so_tien";

    // 4. NGÂN SÁCH
    public static final String TABLE_NGAN_SACH = "ngan_sach";
    public static final String NS_ID = "ns_id";
    public static final String NS_THANG_NAM = "thang_nam"; // YYYY-MM
    public static final String NS_SO_TIEN_DU_KIEN = "so_tien_du_kien";
    public static final String NS_SO_TIEN_CON_LAI = "so_tien_con_lai";

    // 5. CHI TIÊU
    public static final String TABLE_CHI_TIEU = "chi_tieu";
    public static final String CT_ID = "ct_id";
    public static final String CT_LOAI_ID = "loai_id"; // FK
    public static final String CT_SO_TIEN = "so_tien";
    public static final String CT_NGAY = "ngay"; // YYYY-MM-DD
    public static final String CT_GHI_CHU = "ghi_chu"; // Note (Tên khoản chi)
    public static final String CT_THANG_NAM = "thang_nam"; // YYYY-MM
    // Lưu ý: Trong AddTransactionActivity, bạn dùng cột description cho mô tả thêm,
    // nhưng DB hiện tại chỉ có CT_GHI_CHU. Tôi sẽ gộp logic để không sửa DB.

    // Câu lệnh tạo bảng (GIỮ NGUYÊN)
    private static final String CREATE_USER = "CREATE TABLE " + TABLE_USER + " (" + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + USER_HO_TEN + " TEXT NOT NULL, " + USER_EMAIL + " TEXT UNIQUE NOT NULL, " + USER_MAT_KHAU + " TEXT NOT NULL);";
    private static final String CREATE_LOAI_CHI_PHI = "CREATE TABLE " + TABLE_LOAI_CHI_PHI + " (" + LOAI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + LOAI_TEN + " TEXT NOT NULL UNIQUE);";
    private static final String CREATE_CHI_PHI_CO_DINH = "CREATE TABLE " + TABLE_CHI_PHI_CO_DINH + " (" + CD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CD_TEN + " TEXT NOT NULL, " + CD_SO_TIEN + " REAL NOT NULL);";
    private static final String CREATE_NGAN_SACH = "CREATE TABLE " + TABLE_NGAN_SACH + " (" + NS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NS_THANG_NAM + " TEXT NOT NULL UNIQUE, " + NS_SO_TIEN_DU_KIEN + " REAL NOT NULL DEFAULT 0, " + NS_SO_TIEN_CON_LAI + " REAL NOT NULL DEFAULT 0);";
    private static final String CREATE_CHI_TIEU = "CREATE TABLE " + TABLE_CHI_TIEU + " (" + CT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CT_LOAI_ID + " INTEGER NOT NULL, " + CT_SO_TIEN + " REAL NOT NULL, " + CT_NGAY + " TEXT NOT NULL, " + CT_GHI_CHU + " TEXT, " + CT_THANG_NAM + " TEXT NOT NULL, FOREIGN KEY(" + CT_LOAI_ID + ") REFERENCES " + TABLE_LOAI_CHI_PHI + "(" + LOAI_ID + "));";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
        db.execSQL(CREATE_LOAI_CHI_PHI);
        db.execSQL(CREATE_CHI_PHI_CO_DINH);
        db.execSQL(CREATE_NGAN_SACH);
        db.execSQL(CREATE_CHI_TIEU);

        // Insert dữ liệu mẫu cho Loại Chi Phí
        String[] loaiMacDinh = {"Chi phí cố định hàng tháng", "Ăn uống", "Xăng xe / Đi lại", "Mua sắm / Shopping", "Giải trí", "Y tế / Thuốc men", "Tiền thuê nhà", "Giáo dục", "Khác"};
        for (String ten : loaiMacDinh) {
            ContentValues cv = new ContentValues();
            cv.put(LOAI_TEN, ten);
            db.insert(TABLE_LOAI_CHI_PHI, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHI_TIEU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NGAN_SACH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHI_PHI_CO_DINH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOAI_CHI_PHI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // ==================== CÁC HÀM XỬ LÝ (ĐÃ SỬA) ====================

    // Helper: Lấy ID loại chi phí từ Tên (Vì Spinner trả về tên, DB cần ID)
    private int getCategoryIdByName(String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int id = -1;
        // Tìm gần đúng hoặc chính xác
        Cursor cursor = db.rawQuery("SELECT " + LOAI_ID + " FROM " + TABLE_LOAI_CHI_PHI + " WHERE " + LOAI_TEN + " LIKE ?", new String[]{"%" + categoryName + "%"});
        if (cursor.moveToFirst()) {
            id = cursor.getInt(0);
        } else {
            // Nếu không tìm thấy, mặc định là "Khác" (thường là ID cuối hoặc ID 9 theo list mẫu)
            id = 9;
        }
        cursor.close();
        return id;
    }

    // Helper: Lấy chuỗi tháng hiện tại (YYYY-MM)
    private String getCurrentMonthKey() {
        return new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
    }

    // 1. Thêm giao dịch mới
    public void addTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Chuyển đổi Tên danh mục -> ID
        int catId = getCategoryIdByName(t.getCategory());

        values.put(CT_LOAI_ID, catId);
        values.put(CT_SO_TIEN, t.getAmount());
        values.put(CT_NGAY, t.getDate()); // YYYY-MM-DD
        values.put(CT_GHI_CHU, t.getNote()); // Lưu tên khoản chi vào Ghi chú

        // Cắt chuỗi ngày để lấy YYYY-MM
        String monthKey = t.getDate().substring(0, 7);
        values.put(CT_THANG_NAM, monthKey);

        db.insert(TABLE_CHI_TIEU, null, values);

        // Cập nhật lại số tiền còn lại trong ngân sách
        capNhatSoTienConLai(monthKey);
    }

    // 2. Lấy tất cả giao dịch (JOIN bảng để lấy tên loại)
    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query: Lấy thông tin chi tiêu + Tên loại chi phí
        String query = "SELECT t." + CT_ID + ", t." + CT_GHI_CHU + ", t." + CT_SO_TIEN + ", t." + CT_NGAY + ", l." + LOAI_TEN +
                " FROM " + TABLE_CHI_TIEU + " t " +
                " JOIN " + TABLE_LOAI_CHI_PHI + " l ON t." + CT_LOAI_ID + " = l." + LOAI_ID +
                " ORDER BY t." + CT_NGAY + " DESC, t." + CT_ID + " DESC";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String note = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                String category = cursor.getString(4);

                // Description tạm thời để trống hoặc dùng lại note vì DB không có cột riêng
                list.add(new Transaction(id, note, amount, date, category, note));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // 3. Lấy tổng ngân sách tháng hiện tại
    public double getTotalBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        double budget = 0;
        String currentMonth = getCurrentMonthKey();

        Cursor cursor = db.rawQuery("SELECT " + NS_SO_TIEN_DU_KIEN + " FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{currentMonth});
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(0);
        }
        cursor.close();
        return budget;
    }

    // 4. Lấy ngân sách theo tháng bất kỳ
    public double getBudgetByMonth(String monthKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        double budget = 0;
        Cursor cursor = db.rawQuery("SELECT " + NS_SO_TIEN_DU_KIEN + " FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{monthKey});
        if (cursor.moveToFirst()) {
            budget = cursor.getDouble(0);
        }
        cursor.close();
        return budget;
    }

    // 5. Cài đặt ngân sách cho tháng hiện tại
    public void setBudget(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        String currentMonth = getCurrentMonthKey();

        // Kiểm tra xem tháng này đã có ngân sách chưa
        Cursor cursor = db.rawQuery("SELECT " + NS_ID + " FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{currentMonth});

        if (cursor.moveToFirst()) {
            // Đã có -> Update
            ContentValues values = new ContentValues();
            values.put(NS_SO_TIEN_DU_KIEN, amount);
            db.update(TABLE_NGAN_SACH, values, NS_THANG_NAM + " = ?", new String[]{currentMonth});
        } else {
            // Chưa có -> Insert
            ContentValues values = new ContentValues();
            values.put(NS_THANG_NAM, currentMonth);
            values.put(NS_SO_TIEN_DU_KIEN, amount);
            values.put(NS_SO_TIEN_CON_LAI, amount); // Ban đầu còn lại = dự kiến
            db.insert(TABLE_NGAN_SACH, null, values);
        }
        cursor.close();

        // Tính lại số dư dựa trên chi tiêu đã có
        capNhatSoTienConLai(currentMonth);
    }

    // 6. Cập nhật giao dịch
    public void updateTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        int catId = getCategoryIdByName(t.getCategory());

        values.put(CT_GHI_CHU, t.getNote());
        values.put(CT_SO_TIEN, t.getAmount());
        values.put(CT_NGAY, t.getDate());
        values.put(CT_LOAI_ID, catId);

        String monthKey = t.getDate().substring(0, 7);
        values.put(CT_THANG_NAM, monthKey);

        db.update(TABLE_CHI_TIEU, values, CT_ID + " = ?", new String[]{String.valueOf(t.getId())});

        capNhatSoTienConLai(monthKey);
    }

    // 7. Xóa giao dịch
    public void deleteTransaction(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Lấy ngày giao dịch trước khi xóa để biết cập nhật tháng nào
        String monthKey = getCurrentMonthKey();
        Cursor c = db.rawQuery("SELECT " + CT_THANG_NAM + " FROM " + TABLE_CHI_TIEU + " WHERE " + CT_ID + " = ?", new String[]{String.valueOf(id)});
        if(c.moveToFirst()){
            monthKey = c.getString(0);
        }
        c.close();

        db.delete(TABLE_CHI_TIEU, CT_ID + " = ?", new String[]{String.valueOf(id)});

        capNhatSoTienConLai(monthKey);
    }

    // 8. Lấy tổng chi tiêu tháng hiện tại
    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        String currentMonth = getCurrentMonthKey();

        Cursor cursor = db.rawQuery("SELECT SUM(" + CT_SO_TIEN + ") FROM " + TABLE_CHI_TIEU + " WHERE " + CT_THANG_NAM + " = ?", new String[]{currentMonth});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Hàm cập nhật số tiền còn lại (Logic quan trọng)
    public void capNhatSoTienConLai(String thangNam) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Tính tổng chi
        Cursor c1 = db.rawQuery("SELECT SUM(" + CT_SO_TIEN + ") FROM " + TABLE_CHI_TIEU + " WHERE " + CT_THANG_NAM + " = ?", new String[]{thangNam});
        double tongChi = 0;
        if(c1.moveToFirst()) tongChi = c1.getDouble(0);
        c1.close();

        // 2. Lấy ngân sách
        Cursor c2 = db.rawQuery("SELECT " + NS_SO_TIEN_DU_KIEN + " FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{thangNam});
        double duKien = 0;
        if (c2.moveToFirst()) duKien = c2.getDouble(0);
        c2.close();

        // 3. Update số dư
        double conLai = duKien - tongChi;
        ContentValues values = new ContentValues();
        values.put(NS_SO_TIEN_CON_LAI, conLai);

        // Chỉ update nếu bản ghi ngân sách đã tồn tại
        int rows = db.update(TABLE_NGAN_SACH, values, NS_THANG_NAM + " = ?", new String[]{thangNam});
        if (rows == 0 && duKien > 0) {
            // Trường hợp hiếm: Có chi tiêu nhưng chưa tạo dòng ngân sách -> Tạo mới
            values.put(NS_THANG_NAM, thangNam);
            values.put(NS_SO_TIEN_DU_KIEN, 0); // Chưa set ngân sách
            db.insert(TABLE_NGAN_SACH, null, values);
        }
    }

    // Lấy tổng chi tiêu của MỘT THÁNG bất kỳ (đã có getTotalExpense() nhưng hàm đó chỉ cho tháng hiện tại)
    public double getTotalExpenseByMonth(String monthKey) {
        SQLiteDatabase db = this.getReadableDatabase();
        double total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + CT_SO_TIEN + ") FROM " + TABLE_CHI_TIEU + " WHERE " + CT_THANG_NAM + " = ?", new String[]{monthKey});
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    // Lấy chi tiêu đã được gom nhóm theo Category của MỘT THÁNG bất kỳ (Dùng cho Pie Chart)
    public java.util.HashMap<String, Double> getMonthlyExpensesByCategory(String monthKey) {
        java.util.HashMap<String, Double> categoryMap = new java.util.HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query: Gom nhóm theo LOAI_TEN và tính SUM
        String query = "SELECT l." + LOAI_TEN + ", SUM(t." + CT_SO_TIEN + ") " +
                " FROM " + TABLE_CHI_TIEU + " t " +
                " JOIN " + TABLE_LOAI_CHI_PHI + " l ON t." + CT_LOAI_ID + " = l." + LOAI_ID +
                " WHERE t." + CT_THANG_NAM + " = ? " +
                " GROUP BY l." + LOAI_TEN;

        Cursor cursor = db.rawQuery(query, new String[]{monthKey});
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(0);
                double sum = cursor.getDouble(1);
                categoryMap.put(category, sum);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categoryMap;
    }

    // Lấy chi tiết giao dịch theo NGÀY (Dùng cho RecyclerView)
    public List<Transaction> getTransactionsByDate(String targetDate) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query: Lấy chi tiết giao dịch của ngày chỉ định
        String query = "SELECT t." + CT_ID + ", t." + CT_GHI_CHU + ", t." + CT_SO_TIEN + ", t." + CT_NGAY + ", l." + LOAI_TEN +
                " FROM " + TABLE_CHI_TIEU + " t " +
                " JOIN " + TABLE_LOAI_CHI_PHI + " l ON t." + CT_LOAI_ID + " = l." + LOAI_ID +
                " WHERE t." + CT_NGAY + " = ?" +
                " ORDER BY t." + CT_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{targetDate});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String note = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                String category = cursor.getString(4);
                // Dùng note cho description theo quy ước của bạn
                list.add(new Transaction(id, note, amount, date, category, note));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    // Lấy chi tiết giao dịch theo THÁNG
    public List<Transaction> getTransactionsByMonth(String monthKey) {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Query: Lấy chi tiết giao dịch của tháng chỉ định
        String query = "SELECT t." + CT_ID + ", t." + CT_GHI_CHU + ", t." + CT_SO_TIEN + ", t." + CT_NGAY + ", l." + LOAI_TEN +
                " FROM " + TABLE_CHI_TIEU + " t " +
                " JOIN " + TABLE_LOAI_CHI_PHI + " l ON t." + CT_LOAI_ID + " = l." + LOAI_ID +
                " WHERE t." + CT_THANG_NAM + " = ?" +
                " ORDER BY t." + CT_NGAY + " DESC"; // Sắp xếp theo ngày

        Cursor cursor = db.rawQuery(query, new String[]{monthKey});
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String note = cursor.getString(1);
                double amount = cursor.getDouble(2);
                String date = cursor.getString(3);
                String category = cursor.getString(4);
                // Dùng note cho description theo quy ước của bạn
                list.add(new Transaction(id, note, amount, date, category, note));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }


}