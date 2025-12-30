package com.tanfed.inventry.entity;


import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryChellan {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate createdAt = LocalDate.now();
	
	private LocalDate approvedDate;
	
	private List<String> empId;
	
	private String officeName;
	
	private List<String> designation;
	
	private String voucherStatus;
	
	private String loadType ;

	private String dcNo;

	private String clNo;
	
	private LocalDate date;
	
	private String ifmsId;
	
	private String nameOfInstitution;
	
	private String licenseNo;

	private String supplyTo;
	
	private String buyerGstNo;
	
	private String village;
	
	private String block;
	
	private String taluk;
	
	private String district;
	
	private String activity;
	
	private String supplyMode;
	
	private String transporterName;
	
	private String vehicleNo;
	
	private String godownName;
	
	private Long mobileNo;
	
	private String despatchAdviceNo;
	
	private Double totalBags;
	
	private Double totalQty;
	
	private Double km;
	
	private Double transportChargesValue;
	
	private Double loadingChargesValue;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<DcTableData> dcTableData;
	
	private Boolean billEntry;
	
	private String billNo;
}
