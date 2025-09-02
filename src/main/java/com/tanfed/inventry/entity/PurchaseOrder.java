package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.PoTableData;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate createdAt = LocalDate.now();
	
	private String voucherStatus = "Pending";

	private String sobVoucherStatus = "Pending";
	
	private List<String> empId;
	
	private List<String> designation;
	
	private List<String> sobDesignation;
	
	private String poNo;
	
	private LocalDate approvedDate;
	
	private LocalDate sobApprovedDate;
	
	
	
	private LocalDate date;
	
	private String activity;
	
	private String purchaseOrderType;

	
	private String poBased;
	
	private String productCategory;
	
	private String productGroup;
	
	private String productName;
	
	private String supplierName;
	
	private String supplierGst;
	
	private String standardUnits;

	private String packing;
		
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<PoTableData> tableData;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<GRN> grnData;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "terms_id", nullable = false)
	private TermsPrice termsPrice;
}
