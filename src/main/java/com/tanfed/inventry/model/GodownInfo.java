package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GodownInfo {

	private Long id;
	private String officeName;
	private String officeCode;
	private String district;
	private String taluk;
	private String block;
	private String village;
	private String ifmsId;
	private String godownType;
	private String godownName;
	private String door;
	private String street;
	private Integer pincode;
	private String licenseNo;
	private LocalDate insuranceFrom;
	private LocalDate insuranceTo;
	private String totalCapacity;
	private Long numberOfGodowns;
	private List<String> capacities;
	private String keeperName;
	private Long contactNo1;
	private Long contactNo2;
	private String gkDesignation;
	private String hoLetterRcNo;
	private LocalDate insuranceDate;
	private LocalDate validityFrom;
	private LocalDate validityTo;
	private List<String> empId;
}
