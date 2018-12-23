package com.brazuca.util.sort;

import java.util.Comparator;

import com.brazuca.entity.HomeDishEntity;

public class DishPriceSortAsc implements Comparator<HomeDishEntity>{

	@Override
	public int compare(HomeDishEntity entity1, HomeDishEntity entity2) {
		double delta = entity1.getCurPrice() - entity2.getCurPrice();
		if(delta>0)
			return 1;
		else if(delta ==0)
			return 0 ;
		else
			return -1;
	}
}
