package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDataTable {

	private String invoiceNo;
	private List<String> productCategory;
	private List<String> supplierName;
	private List<String> productName;
	private Double qty;
	private Double totalInvoiceValue;
}
