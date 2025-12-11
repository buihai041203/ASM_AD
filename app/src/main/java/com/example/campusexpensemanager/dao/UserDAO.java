package com.example.campusexpensemanager.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.campusexpensemanager.DatabaseHelper;
import com.example.campusexpensemanager.model.User;

public class UserDAO {
    private DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Đăng ký
    public boolean register(String hoTen, String email, String matKhau) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.USER_HO_TEN, hoTen);
        values.put(DatabaseHelper.USER_EMAIL, email);
        values.put(DatabaseHelper.USER_MAT_KHAU, matKhau);
        long result = db.insert(DatabaseHelper.TABLE_USER, null, values);
        return result != -1;
    }

    // Đăng nhập
    public User login(String email, String matKhau) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_USER +
                        " WHERE " + DatabaseHelper.USER_EMAIL + "=? AND " + DatabaseHelper.USER_MAT_KHAU + "=?",
                new String[]{email, matKhau});
        if (c.moveToFirst()) {
            User user = new User();
            user.setUserId(c.getInt(c.getColumnIndexOrThrow(DatabaseHelper.USER_ID)));
            user.setHoTen(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_HO_TEN)));
            user.setEmail(c.getString(c.getColumnIndexOrThrow(DatabaseHelper.USER_EMAIL)));
            c.close();
            return user;
        }
        c.close();
        return null;
    }

}