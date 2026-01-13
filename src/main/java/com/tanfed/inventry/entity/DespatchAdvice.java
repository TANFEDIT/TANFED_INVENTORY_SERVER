package com.tanfed.inventry.entity;


import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DespatchAdvice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate approvedDate;
	
	private String despatchAdviceNo;
	
	private List<String> designation;
	
	private List<String> empId;
	
	private String voucherStatus;
	
	private String officeName;
	
	private String activity;
	
	private LocalDate date;
	
	private String godownName;
	
	private String supplyMode;
	
	private String ifmsId;
	
	private String nameOfInstitution;
	
	private String buyerGstNo;
	
	private String supplyTo;
	
	private String otherRegion;
	
	private String village;
	
	private String taluk;
	
	private String block;
	
	private String district;

	private String licenseNo;
	
	private Boolean statusDisabled;
	
	private LocalDate statusDisabledDate;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<DespatchAdviceTableDataEntity> tableData;
}
