package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JournalVoucher {

	private Long id;
	
	private List<String> empId;
	
	private String officeName;
	
	private String voucherStatus = "Pending";
	
	private List<String> designation;
	
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate jvDate;
	
	private String jvMonth;
	
	private String financialYear;

	private String jvNo;
	
	private String jvType;
	
	private String jvFor;
	
	private String narration;
	
	private Double debit;
	
	private Double totalDr;
	
	private Double totalCr;
	
	private Double derivedQty;
	
	private Double derivedTotal;
	
	private List<JV_Array_Data> rows;
}
