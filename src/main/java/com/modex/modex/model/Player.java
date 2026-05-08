package com.modex.modex.model;

public class Player {
    private int money;

    public Player(int initialMoney) {
        this.money = initialMoney;
    }

    public int getMoney() {
        return money;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public void setMoney(int amount) {
        this.money = amount;
    }

    public boolean deductMoney(int amount) {
        if (this.money >= amount) {
            this.money -= amount;
            return true;
        }
        return false;
    }


}