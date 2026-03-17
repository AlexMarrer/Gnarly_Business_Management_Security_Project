package com.business.basiclogics;

import java.math.BigDecimal;

public class Logic {
	public static BigDecimal countTotal(BigDecimal price, int quantity) {
		return price.multiply(BigDecimal.valueOf(quantity));
	}
}
