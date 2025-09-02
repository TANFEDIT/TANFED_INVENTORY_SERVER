package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ContractorInfo {

	private Long id;

	 
	private List<String> empId;
	
	
//	Contractor create
	 
	private String officeName;

	 
	private String officeCode;

	 
	private String contractThrough;

	 
	private String contractFirm;

	 
	private String proprietorName;	

	 
	private String door;

	 
	private String street;

	 
	private String district;

	 
	private Integer pincode;
	
	private List<ContractorTenderData> tenderData;
	
	
	
	
	
	
//	EMD Entry
	private List<ContractorGstData> gstData;
	
	 
	private Double emdAmount;
	
	 
	private Double solvencyValue;
	
	 
	private LocalDate emdReceivedOn;
	
	 
	private Double additionalEmd;
	
	 
	private String gstNo;
	
	 
	private String gstReturnType;
	
	 
	private List<String> godownName;
	
	private List<ContractorChargesData> chargesData;
	
	
	
//	EMD Refund
	 
	private String emdRefundFor;
	
	 
	private Double refundAmount;
	
	 
	private String chequeVoucherJvNo;
	
	 
	private LocalDate date;
	private String blocklist;
	private String remarks;
	private String status;
	private List<ReassignedGodown> additionalGodownData;
}
