package com.tanfed.inventry.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInvoiceInfoGrnAttach {

	private String termsNo;
	private String invoiceNo;
	private LocalDate invoiceDate;
	private Double invoiceQty;
	private Double invoiceAvlQty;
	private Double invoiceValue;
}
