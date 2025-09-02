package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;


import com.tanfed.inventry.model.PaymentVoucher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferDto {
	private Long id;
	
	private List<String> designation;
	 
	private List<String> empId;
	
	private String voucherStatus;
	private String activity;
	 
	private String officeName;
	
	private LocalDate date;
	
	private String transferType;

	private LocalDate dateOfTransfer;
	
	private String branchName;
	
	private Long accountNo;
	 
	private String toBranchName;
	
	private Long toAccountNo;
	
	private Double openingBalance;
	 
	private Double collection;

	private Double ibrAmount;

	private Double total;
	
	private Double ibtAmount;
	
	private Double currentTransfer;
	
	private Double bankCharges;
	
	private Double others;
	
	private Double closingBalance;
	
	private Boolean transferDone;
	
	private List<PaymentVoucher> pvData;

	private List<String> invoiceNoList;
	
	private List<Long> idList;
}
