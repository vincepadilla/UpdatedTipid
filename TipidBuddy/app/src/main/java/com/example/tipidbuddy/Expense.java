package com.example.tipidbuddy;

public class Expense {
    private String category;
    private String amount;
    private String notes;

    public Expense(String category, String amount, String notes) {
        this.category = category;
        this.amount = amount;
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public String getAmount() {
        return amount;
    }

    public String getNotes() {
        return notes;
    }
}
