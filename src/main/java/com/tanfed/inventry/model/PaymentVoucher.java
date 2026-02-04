package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentVoucher {

	private Long id;

	private List<String> empId;

	private LocalDate createdAt = LocalDate.now();

	private String voucherNo;

	private String officeName;
	private LocalDate approvedDate;

	// create
	private String pvType;
	private String activity;
	private LocalDate date;

	private Long accountNo;

	private String accountType;

	private String branchName;

	private String mainHead;

	private String subHead;

	private String paidTo;

	private Long beneficiaryAccountNo;

	private Double amount;

	private String narration;
//	private String contraNarration;
	private String ifmsIdNo;

	// cash update
	private LocalDate paidOn;

	// online update
	private Long utrNumber;

	private LocalDate onlineDate;

	// cheque update
	private LocalDate chequeDate;

	private String chequeNumber;

	private String issueBankName;

	private LocalDate settledDate;

	private String contraEntry;
	private String contraId;

	private String status;

	private String voucherStatus;

	private List<String> designation;

	private String voucherFor;

	private String idNo;

}
