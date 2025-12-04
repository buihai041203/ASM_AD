package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.campusexpensemanager.Transaction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuanLyTaiChinh.db";
    private static final int DB_VERSION = 1;

    // ==================== 1. BẢNG USER ====================
    public static final String TABLE_USER = "users";
    public static final String USER_ID = "user_id";
    public static final String USER_HO_TEN = "ho_ten";
    public static final String USER_EMAIL = "email";
    public static final String USER_MAT_KHAU = "mat_khau";

    private static final String CREATE_USER =
            "CREATE TABLE " + TABLE_USER + " (" +
                    USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    USER_HO_TEN + " TEXT NOT NULL, " +
                    USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    USER_MAT_KHAU + " TEXT NOT NULL" +
                    ");";

    // ==================== 2. BẢNG LOẠI CHI PHÍ (chỉ 2 cột) ====================
    public static final String TABLE_LOAI_CHI_PHI = "loai_chi_phi";
    public static final String LOAI_ID = "id_phat_sinh";        // PK
    public static final String LOAI_TEN = "loai_chi_phi";

    private static final String CREATE_LOAI_CHI_PHI =
            "CREATE TABLE " + TABLE_LOAI_CHI_PHI + " (" +
                    LOAI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    LOAI_TEN + " TEXT NOT NULL UNIQUE" +
                    ");";

    // ==================== 3. BẢNG CHI PHÍ CỐ ĐỊNH (chỉ 3 cột) ====================
    public static final String TABLE_CHI_PHI_CO_DINH = "chi_phi_co_dinh";
    public static final String CD_ID = "id_co_dinh";
    public static final String CD_TEN = "ten_chi_phi";
    public static final String CD_SO_TIEN = "so_tien";

    private static final String CREATE_CHI_PHI_CO_DINH =
            "CREATE TABLE " + TABLE_CHI_PHI_CO_DINH + " (" +
                    CD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CD_TEN + " TEXT NOT NULL, " +
                    CD_SO_TIEN + " REAL NOT NULL" +
                    ");";

    // ==================== 4. BẢNG NGÂN SÁCH ====================
    public static final String TABLE_NGAN_SACH = "ngan_sach";
    public static final String NS_ID = "ns_id";
    public static final String NS_THANG_NAM = "thang_nam";            // YYYY-MM
    public static final String NS_SO_TIEN_DU_KIEN = "so_tien_du_kien";
    public static final String NS_SO_TIEN_CON_LAI = "so_tien_con_lai";

    private static final String CREATE_NGAN_SACH =
            "CREATE TABLE " + TABLE_NGAN_SACH + " (" +
                    NS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NS_THANG_NAM + " TEXT NOT NULL UNIQUE, " +
                    NS_SO_TIEN_DU_KIEN + " REAL NOT NULL DEFAULT 0, " +
                    NS_SO_TIEN_CON_LAI + " REAL NOT NULL DEFAULT 0" +
                    ");";

    // ==================== 5. BẢNG CHI TIÊU (6 cột) ====================
    public static final String TABLE_CHI_TIEU = "chi_tieu";
    public static final String CT_ID = "ct_id";
    public static final String CT_LOAI_ID = "loai_id";           // FK → loai_chi_phi.id_phat_sinh
    public static final String CT_SO_TIEN = "so_tien";
    public static final String CT_NGAY = "ngay";                 // YYYY-MM-DD
    public static final String CT_GHI_CHU = "ghi_chu";
    public static final String CT_THANG_NAM = "thang_nam";       // YYYY-MM

    private static final String CREATE_CHI_TIEU =
            "CREATE TABLE " + TABLE_CHI_TIEU + " (" +
                    CT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    CT_LOAI_ID + " INTEGER NOT NULL, " +
                    CT_SO_TIEN + " REAL NOT NULL, " +
                    CT_NGAY + " TEXT NOT NULL, " +
                    CT_GHI_CHU + " TEXT, " +
                    CT_THANG_NAM + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + CT_LOAI_ID + ") REFERENCES " + TABLE_LOAI_CHI_PHI + "(" + LOAI_ID + ")" +
                    ");";

    // ==================== LOẠI CHI PHÍ CỐ ĐỊNH ĐẶC BIỆT ====================
    private static final String LOAI_CO_DINH = "Chi phí cố định hàng tháng";

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

        String[] loaiMacDinh = {
                "Chi phí cố định hàng tháng",  // ID = 1 → hệ thống dùng
                "Ăn uống", "Xăng xe / Đi lại", "Mua sắm / Shopping", "Giải trí", "Y tế / Thuốc men",
                "Khác"
        };

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

    public double getTotalBudget() {
        String thangNam = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COALESCE(" + NS_SO_TIEN_DU_KIEN + ", 0) FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{thangNam});
        double budget = c.moveToFirst() ? c.getDouble(0) : 0;
        c.close();
        return budget;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==================== HÀM TỰ ĐỘNG TRỪ CHI PHÍ CỐ ĐỊNH ĐẦU THÁNG ====================
    public void kiemTraVaTruChiPhiCoDinhDauThang() {
        String thangNam = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        if (daTruChiPhiCoDinhThangNay(thangNam)) return;
        truChiPhiCoDinhVaoDauThang(thangNam);
    }

    private boolean daTruChiPhiCoDinhThangNay(String thangNam) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + TABLE_CHI_TIEU + " WHERE " + CT_THANG_NAM + " = ? AND " + CT_GHI_CHU + " = ? LIMIT 1",
                new String[]{thangNam, "CHI_PHI_CO_DINH_TU_DONG"});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    private void truChiPhiCoDinhVaoDauThang(String thangNam) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Tổng chi phí cố định
            Cursor c = db.rawQuery("SELECT COALESCE(SUM(" + CD_SO_TIEN + "), 0) FROM " + TABLE_CHI_PHI_CO_DINH, null);
            c.moveToFirst();
            double tong = c.getDouble(0);
            c.close();
            if (tong <= 0) return;

            // Thêm vào chi_tieu (loai_id = 1)
            ContentValues cv = new ContentValues();
            cv.put(CT_LOAI_ID, 1);                                      // ID = 1 → Chi phí cố định
            cv.put(CT_SO_TIEN, tong);
            cv.put(CT_NGAY, thangNam + "-01");
            cv.put(CT_GHI_CHU, "CHI_PHI_CO_DINH_TU_DONG");
            cv.put(CT_THANG_NAM, thangNam);
            db.insert(TABLE_CHI_TIEU, null, cv);

            // Cập nhật ngân sách
            capNhatSoTienConLai(thangNam);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }



    // Cập nhật số tiền còn lại mỗi khi có thay đổi chi tiêu
    public void capNhatSoTienConLai(String thangNam) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c1 = db.rawQuery("SELECT COALESCE(SUM(" + CT_SO_TIEN + "), 0) FROM " + TABLE_CHI_TIEU + " WHERE " + CT_THANG_NAM + " = ?", new String[]{thangNam});
        c1.moveToFirst();
        double tongChi = c1.getDouble(0);
        c1.close();

        Cursor c2 = db.rawQuery("SELECT " + NS_SO_TIEN_DU_KIEN + " FROM " + TABLE_NGAN_SACH + " WHERE " + NS_THANG_NAM + " = ?", new String[]{thangNam});
        double duKien = c2.moveToFirst() ? c2.getDouble(0) : 0;
        c2.close();

        double conLai = duKien - tongChi;
        db.execSQL("UPDATE " + TABLE_NGAN_SACH + " SET " + NS_SO_TIEN_CON_LAI + " = ? WHERE " + NS_THANG_NAM + " = ?", new Object[]{conLai, thangNam});
    }


}