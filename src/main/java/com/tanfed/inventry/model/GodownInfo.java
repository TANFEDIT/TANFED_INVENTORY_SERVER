package com.tanfed.inventry.model;

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

	private String totalCapacity;

	private Long numberOfGodowns;

	private List<String> capacities;

	private String keeperName;

	private String contactNo1;

	private String contactNo2;

	private String gkDesignation;

	private List<String> empId;

	private List<LicenseData> license;

	private List<GodownInsuranceData> insurance;
}
