package com.akshay.stockwatchapp;

public class Stock {
    private String symbol;
    private String name;
    private double price, priceChange, changePercentage;

    public Stock() {
        symbol = "";
        name = "";
        price =0 ;
        priceChange = 0;
        changePercentage=0;
    }

    public Stock(String symbol, String name, double price, double priceChange, double changePercentage) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.priceChange = priceChange;
        this.changePercentage = changePercentage;
    }

    public Stock(String name, String symbol) {
        this.name= name;
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(double priceChange) {
        this.priceChange = priceChange;
    }

    public double getChangePercentage() {
        return changePercentage;
    }

    public void setChangePercentage(double changePercentage) {
        this.changePercentage = changePercentage;
    }
}
