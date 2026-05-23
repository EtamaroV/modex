package com.modex.modex.model;

public class Player { // ข้อมูลผู้เล่น
    private int money; // เงิน

    public Player(int initialMoney) {
        this.money = initialMoney;
    } // เงินเริ่มต้น

    public int getMoney() {
        return money;
    } // ดึงจำนวนเงิน

    public void setMoney(int amount) {
        this.money = amount;
    } // เปลี่ยนจำนวนเงิน

    public void addMoney(int amount) {
        this.money += amount;
    } // เพิ่มจำนวนเงิน

    public boolean deductMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    } // ลดจำนวนเงิน
}