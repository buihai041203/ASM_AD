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


}

