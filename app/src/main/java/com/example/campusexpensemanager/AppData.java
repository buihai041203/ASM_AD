package com.example.campusexpensemanager;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private static AppData instance;
    private List<Transaction> transactionList;

    private AppData() {
        transactionList = new ArrayList<>();
        // Dữ liệu mẫu
        transactionList.add(new Transaction("Salary", "28/11/2025 - 08:00", 5000000));
        transactionList.add(new Transaction("Breakfast", "28/11/2025 - 07:00", -35000));
    }

    public static AppData getInstance() {
        if (instance == null) instance = new AppData();
        return instance;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void addTransaction(Transaction transaction) {
        transactionList.add(0, transaction);
    }

    public double getTotalBalance() {
        double total = 0;
        for (Transaction t : transactionList) {
            total += t.getAmount();
        }
        return total;
    }

    // --- BẠN CẦN THÊM 2 HÀM NÀY ĐỂ EDIT HOẠT ĐỘNG ---

    // 1. Hàm cập nhật (Sửa)
    public void updateTransaction(int position, Transaction newTransaction) {
        if (position >= 0 && position < transactionList.size()) {
            transactionList.set(position, newTransaction);
        }
    }

    // 2. Hàm xóa
    public void removeTransaction(int position) {
        if (position >= 0 && position < transactionList.size()) {
            transactionList.remove(position);
        }
    }
}