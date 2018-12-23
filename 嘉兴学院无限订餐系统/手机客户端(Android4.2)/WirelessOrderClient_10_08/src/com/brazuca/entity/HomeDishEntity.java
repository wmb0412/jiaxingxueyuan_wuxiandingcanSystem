package com.brazuca.entity;

public class HomeDishEntity {
	private int dishId;
    private String dishName;
    private double curPrice;
    private double oriPrice;
    private String picUrl;
    private String introduction;
    private String timeToMarket;
    private int orderTimes;  //被点次数
    
	public double getCurPrice() {
		return curPrice;
	}
	public void setCurPrice(double curPrice) {
		this.curPrice = curPrice;
	}
	public double getOriPrice() {
		return oriPrice;
	}
	public void setOriPrice(double oriPrice) {
		this.oriPrice = oriPrice;
	}
	public int getOrderTimes() {
		return orderTimes;
	}
	public void setOrderTimes(int orderTimes) {
		this.orderTimes = orderTimes;
	}
    

	public String getTimeToMarket() {
		return timeToMarket;
	}


	public void setTimeToMarket(String timeToMarket) {
		this.timeToMarket = timeToMarket;
	}


	public String getDishName() {
		return dishName;
	}
	
	
	public int getDishId() {
		return dishId;
	}


	public void setDishId(int dishId) {
		this.dishId = dishId;
	}


	public void setDishName(String dishName) {
		this.dishName = dishName;
	}

	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getIntroduction() {
		return introduction;
	}
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
}
