package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankInfo {
	private Long id;

	private String officeName;

	private Long accountNumber;

	private String accountType;

	private String bankGST;

	private String bankId;

	private String bankName;

	private String branchName;

	private String door;

	private String street;

	private String district;

	private Long pincode;

	private Long contact1;

	private Long contact2;

	private String email;

	private String website;

	private String bankType;

	private String ifsc;

	private String micr;

	private String cif;

	private List<String> empId;
}
