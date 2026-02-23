package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceTable {

	private String grnNo;
	private String productCategory;
	private String supplierName;
	private String productName;
	private String packing;
	private Double bags;
	private Double qty;
	private String hsnCode;
	private Double gstRate;
	private Double basicPrice;
	private Double cgstAmount;
	private Double sgstAmount;
	private Double total;
	private Double mrp;
	private Double margin;
	private Double gstOnMargin;
	private String batchcertNo;
}
