package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeInfo {

	private Long id;
	private String officeType;
	private String officeName;
	private String officeCode;
	private String manager;
	private String organization;
	private String door;
	private String street;
	private String district;
	private Integer pincode;
	private String contactName;
	private Long contactNo1;
	private Long contactNo2;
	private String email;
	private String areaOperation;
	private String isUnitOffice;
	private String unitOfficeType;
	private List<String> productionUnit;
	private List<String> serviceUnit;
	private List<String> districtList;
	private List<String> empId;
}
