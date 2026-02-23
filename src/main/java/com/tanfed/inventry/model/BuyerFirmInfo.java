package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuyerFirmInfo {
	private Long id;

	private List<String> empId;

	private String officeName;

	private String district;

	private String taluk;

	private String block;

	private String village;

	private String firmRegister;

	private String supplyTo;

	private String firmType;

	private String buyerGstNo;

	private List<String> businessWithTanfed;

	private String LicenceNo;

	private LocalDate ValidityDateFrom;

	private LocalDate ValidityDateTo;

	private String ifmsIdNo;

	private String nameOfInstitution;

	private String address;

	private String emailId;

	private Long contact1;

	private Long contact2;

	private String bankOperation;

	private String financingBank;

	private String branchName;
}
