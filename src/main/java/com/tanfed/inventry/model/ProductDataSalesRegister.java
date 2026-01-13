package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDataSalesRegister {

	private String productName;
	private String bags;
	private String qty;
	private String basicPrice;
	private String cgst;
	private String sgst;
	private String total;
}
