package com.brazuca.entity;

public class OrderEntity {
	private String dishName;
	private int count;
	private double paySinglePrice;

	public double getPaySinglePrice() {
		return paySinglePrice;
	}

	public void setPaySinglePrice(double paySinglePrice) {
		this.paySinglePrice = paySinglePrice;
	}

	public String getDishName() {
		return dishName;
	}

	public void setDishName(String dishName) {
		this.dishName = dishName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

}
