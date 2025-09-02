package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCollectionP1TableData {

	private String invoiceNo;
	private LocalDate date;
	private String ifmsId;
	private String name;
	private String district;
	private Double totalQty;
	private Double totalInvoiceValue;
	private LocalDate dueDate;
	private String ccbBranch;
	private String materialCenter;
	private Double ackQty;
	
//	private Double collectionValue;
//	private Double openingBalance;
//	private Double total;
//	private Double transfer;
//	private Double bankCharges;
//	private Double others;
//	private Double closingBalance;
}
