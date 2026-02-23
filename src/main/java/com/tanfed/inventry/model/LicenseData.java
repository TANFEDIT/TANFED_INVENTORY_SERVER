package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicenseData {

	private Long id;
	private List<String> empId;
	private String officeName;
	private String filename;
	private String filetype;
	private byte[] filedata;
	private String licenseType;
	private String licenseFor;
	private String licenseNumber;
	private LocalDate validFrom;
	private LocalDate validTo;
}
