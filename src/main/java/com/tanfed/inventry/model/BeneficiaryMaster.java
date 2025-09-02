package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryMaster {

	private Long id;
	
	private String officeName;
	
	private String beneficiaryName;

	private String gstNo;
	
	private String panNo;
	
	private String bankName;
	
	private Long accountNo;
	
	private String accountType;
	
	private String ifscCode;
	
	private List<String> beneficiaryApplicableToHoAccount;
	
	private List<String> empId;
	
	private LocalDate date = LocalDate.now();
}
