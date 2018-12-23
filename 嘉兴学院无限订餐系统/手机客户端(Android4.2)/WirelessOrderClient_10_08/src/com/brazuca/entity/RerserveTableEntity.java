package com.brazuca.entity;

public class RerserveTableEntity {
	private String useTime;
	private int hour;
	private int type;
	private int tableId;
	private String orderId;
	private boolean isEmptyFood;
	private boolean isSubmit;
	private String reserveTime;

	public String getReserveTime() {
		return reserveTime;
	}

	public void setReserveTime(String reserveTime) {
		this.reserveTime = reserveTime;
	}

	public String getUseTime() {
		return useTime;
	}

	public void setUseTime(String useTime) {
		this.useTime = useTime;
	}

	public boolean isEmptyFood() {
		return isEmptyFood;
	}

	public void setEmptyFood(boolean isEmptyFood) {
		this.isEmptyFood = isEmptyFood;
	}

	public boolean isSubmit() {
		return isSubmit;
	}

	public void setSubmit(boolean isSubmit) {
		this.isSubmit = isSubmit;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getTableId() {
		return tableId;
	}

	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
}
