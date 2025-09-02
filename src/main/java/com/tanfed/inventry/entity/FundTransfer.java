package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class FundTransfer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private List<String> designation;
	 
	private List<String> empId;
	
	private String voucherStatus;
	 
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate approvedDate;
	
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
	
	private List<String> pvList;

	private List<String> invoiceNoList;
	
	private List<Long> idList;
}
