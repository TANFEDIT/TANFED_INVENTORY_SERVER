package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.GtnTableData;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table
public class GTN {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private LocalDate createdAt = LocalDate.now();

	private String gtnNo;

	private String officeName;

	private String voucherStatus;

	private List<String> designation;

	private List<String> empId;

	private LocalDate approvedDate;

	private String fromIfmsId;

	private String toIfmsId;

	private LocalDate date;

	private String activity;

	private String gtnFor;

	private String transactionFor;

	private String productCategory;

	private String productGroup;

	private String productName;

	private String supplierGst;

	private String supplierName;

	private String GodownName;

	private String toRegion;

	private String destination;

	private String daNo;
	private String dcNo;

	private String transporterName;

	private Double totalUnloadingCharges;

	private Double transportChargesValue;

	private Double transportChargesPerQty;

	private Double km;

	private Double loadingChargesValue;

	private Double loadingChargesPerQty;

	private String rrNo;

	private String movementDocDate;

	private String vehicleNo;

	private String issuedGtnNo;

	private String buyerName;
	private String buyerDistrict;
	private String buyerGstNo;
	private String transportCharges;

	private String loadingCharges;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "gtn")
	private List<GtnTableData> gtnTableData;

	private Boolean billEntry;

}
