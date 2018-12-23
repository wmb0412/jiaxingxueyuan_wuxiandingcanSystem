package com.brazuca.util.sort;

import java.util.Comparator;

import com.brazuca.entity.HomeDishEntity;

//∞¥ ±º‰Ωµ–Ú≈≈–Ú
public class DishTimeSort implements Comparator<HomeDishEntity>{

	@Override
	public int compare(HomeDishEntity entity1, HomeDishEntity entity2) {

		System.out.println("entity2.getTimeToMarket()"+entity2.getTimeToMarket());
		System.out.println("entity1.getTimeToMarket()"+entity1.getTimeToMarket());
		return   entity2.getTimeToMarket().compareTo(entity1.getTimeToMarket()) ;
	}
}
