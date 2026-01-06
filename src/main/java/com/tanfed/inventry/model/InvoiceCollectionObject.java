package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCollectionObject {

	private String invoiceNo;
	private String ccbBranch;
	private Double ackQty;
	private LocalDate ackEntryDate;
	private LocalDate addedToPresentDate;
	private String collectionProcess;
	private LocalDate dueDate;
	private LocalDate dateOfPresent;
	private LocalDate dateOfCollectionFromCcb;
	private Double collectionValue;
	private String officeName;
	private String collectionMethod;
	private Boolean isShort;
	private AdjustmentReceiptVoucher adjVoucher;
}
