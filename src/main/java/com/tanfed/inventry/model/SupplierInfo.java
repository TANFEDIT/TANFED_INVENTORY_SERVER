package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierInfo {

	private Long id;
	
	private List<String> empId;
	
	private String contact1;
	
	private String contact2;
	
	private String door;
	
	private String street;
	
	private Integer pincode;
	
	private String district;
	
	private String email;
	
	private String supplierName;
	
	private String supplierPanNo;
	
	private String supplierTanNo;
	
	private String supplierTin;
	
	private String supplierGst;
	
	private String relationship;
	
	private List<String> supplierOf;
	
	private String website;
}
