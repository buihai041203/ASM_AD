package com.example.campusexpensemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.campusexpensemanager.Transaction;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 2; // Lưu ý version

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Bảng Giao dịch
        db.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT, amount REAL, date TEXT, category TEXT, description TEXT)");
        // Bảng Ngân sách
        db.execSQL("CREATE TABLE budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT UNIQUE, limit_amount REAL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS budgets");
        onCreate(db);
    }

    // --- TRANSACTION LOGIC ---
    public void addTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("note", t.getNote());
        values.put("amount", t.getAmount());
        values.put("date", t.getDate());
        values.put("category", t.getCategory());
        values.put("description", t.getDescription());
        db.insert("transactions", null, values);
        db.close();
    }

    public void updateTransaction(Transaction t) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("note", t.getNote());
        values.put("amount", t.getAmount());
        values.put("date", t.getDate());
        values.put("category", t.getCategory());
        values.put("description", t.getDescription());
        db.update("transactions", values, "id = ?", new String[]{String.valueOf(t.getId())});
        db.close();
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions ORDER BY date DESC", null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Transaction(
                        cursor.getInt(0), cursor.getString(1), cursor.getDouble(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5)
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public double getTotalExpense() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM transactions", null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    // --- BUDGET LOGIC ---
    public void setBudget(double amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", "General"); // Mặc định 1 ngân sách chung
        values.put("limit_amount", amount);

        int rows = db.update("budgets", values, "category = ?", new String[]{"General"});
        if (rows == 0) db.insert("budgets", null, values);
        db.close();
    }

    public double getTotalBudget() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(limit_amount) FROM budgets", null);
        double total = 0;
        if (cursor.moveToFirst()) total = cursor.getDouble(0);
        cursor.close();
        return total;
    }

    public void deleteTransaction(int id) {

    }
}