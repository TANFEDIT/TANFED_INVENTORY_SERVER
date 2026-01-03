package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SundryDrOb {
	private Long id;

	private String activity;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private String invoiceNo;
	private LocalDate invoiceDate;
	private LocalDate dueDate;
	private String status;
	private Double qty;
	private Double amount;

	
	private String officeName;
	private List<String> empId;
	private String voucherStatus;
	private List<String> designation;
	private LocalDate createdAt = LocalDate.now();
	private LocalDate approvedDate;
	private String mainHead;
	private String subHead;

	private String collectionMode;
	private String collectionMethod;
	private Double ackQty;
	private LocalDate ackEntryDate;
	private String ccbBranch;
	private LocalDate addedToPresentDate;
	private LocalDate dateOfPresent;
	private String icmNo;
	private Double collectionValue;
	private LocalDate dateOfCollectionFromCcb;
	private String voucherStatusICP1;
	private List<String> designationICP1;
	private String voucherStatusICP2;
	private List<String> designationICP2;
	private String voucherStatusICP3;
	private List<String> designationICP3;
	private String voucherStatusICP4;
	private List<String> designationICP4;
	private Boolean transferDone;
	private AdjustmentReceiptVoucher adjReceipt;
}
