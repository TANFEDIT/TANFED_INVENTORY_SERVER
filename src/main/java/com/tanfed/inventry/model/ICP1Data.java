package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICP1Data {

	private Long id;
	private String activity;
	private String materialCenter;
	private String invoiceNo;
	private LocalDate invoiceDate;
	private String ifmsId;
	private String name;
	private String district;
	private Double qty;
	private Double ackQty;
	private LocalDate ackEntryDate;
	private Double value;
	private LocalDate dueDate;
	private LocalDate dateAddedToPresent;
	private String ccbBranch;
	private Long accountNo;
	private LocalDate dateOfPresent;
	private LocalDate dateOfCollection;
	private Double collectionValue;
	private String voucherStatus;
	private AdjustmentReceiptVoucher adjVoucher;
	private List<String> designation;
}
