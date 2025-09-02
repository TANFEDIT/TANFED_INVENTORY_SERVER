package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDayBookValue {

	private String checkMemoNo;
	private List<RegionQtyTypes> qtys;
	private Double netPurchaseValue;
	private Double margin;
	private Double tradeIncome;
	private Double inputTax;
	private Double paymentValue;
	private Double tdsTcsValue;
	private Double advAdjValue;
	private Double cnValue;
	private Double allowedMargin;
	private Double outputGst;
	private Double discription;
}
