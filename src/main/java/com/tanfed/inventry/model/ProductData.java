package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {

	private String supplierGst;
	private String standardUnits;
	private String packing;
	private String productCategory;
	private String productGroup;
	private String hsnCode;
}
