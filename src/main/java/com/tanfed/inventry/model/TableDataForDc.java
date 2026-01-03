package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableDataForDc {

	private String productCategory;
	private String productGroup;
	private String supplierName;
	private String productName;
	private String packing;
	private String standardUnits;
	private Double avlQty;
	private String outwardBatchNo;
	private String termsNo;
	private String collectionMode;
	private Double mrp;
	private LocalDate grnDate;
	private String voucherId;
}
