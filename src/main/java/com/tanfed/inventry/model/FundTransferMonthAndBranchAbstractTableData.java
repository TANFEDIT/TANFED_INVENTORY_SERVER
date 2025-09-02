package com.tanfed.inventry.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FundTransferMonthAndBranchAbstractTableData {

	private LocalDate date;
	
	private Double openingBalance;
	 
	private Double collection;

	private Double ibrAmount;
	
	private Double total;
	
	private Double currentTransfer;
	
	private Double ibtAmount;
	
	private Double bankCharges;
	
	private Double others;
	
	private Double closingBalance;
}
