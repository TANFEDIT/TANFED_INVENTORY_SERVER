package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProductMaster {

	private Long id;
	private String activity;
	private String brandName;
	private String hsnCode;
	private String packing;
	private String productCategory;
	private String productGroup;
	private String productName;
	private String productSupply;
	private String standardUnits;
	private String supplierName;
	private String supplierGst;
	private String usedAs;
	private String batchNo;
	private String certification;
	private LocalDate date = LocalDate.now();
	private String gstCategory;
	private Double gstRate;
	private GstRateData gstData;
}
