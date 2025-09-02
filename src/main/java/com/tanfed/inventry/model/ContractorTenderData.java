package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ContractorTenderData {

	private Long id;

	 
	private String contractApproval;
	
	 
	private String hoIrRcno;

	 
	private String approvalPeriod;

	 
	private LocalDate validityFrom;

	 
	private LocalDate validityTo;

	 
	private LocalDate hoLetterDate;
}
