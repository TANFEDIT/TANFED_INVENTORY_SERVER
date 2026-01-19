package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentReceiptVoucher {

	private Long id;
	
	private String voucherNo;
	
	private String pvVoucherNo;

	private String officeName;
	
	private List<String> empId;
	
	private String voucherStatus;
	
	private List<String> designation;
	private LocalDate approvedDate;
	
	
	private LocalDate date;
	
	private String receivedFrom;
	private String icmInvNo;
	private String voucherFor;
	private Double receivedAmount;
	
	private String receiptMode;
	
	private Long utrChequeNoDdNo;
	
	private LocalDate docDate;
	
	private String issuingBank;
	
	private String mainHead;
	
	private String subHead;
	
	private String narration;
	
	private String contraEntry;
	
	private LocalDate createdAt = LocalDate.now();
	
	
	
	private String accountType;
	
	private Long accountNo;
	
	private String branchName;
	
	private LocalDate depositDate;
	
	
	
	
	private Double bankCharges;
	
	private LocalDate dateOfCollection;
	


}
