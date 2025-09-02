package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class OpeningStock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private LocalDate createdAt = LocalDate.now();
	
	private String voucherStatus = "Pending";
	
	private List<String> designation;
	
	private String officeName;
	
	private List<String> empId;
	
	private LocalDate approvedDate;
	
	
	
	private String activity;
	
	private String supplierName;
	
	private String productName;
	
	private String productCategory;
	
	private String productGroup;
	
	private String supplierGst;
	
	private Double quantity;
	
	private Double bookValue;
	
	private LocalDate asOn;
	
	private String productStatus;
	
	private LocalDate expiryDate;
	private String packing;
	private String standardUnits;
	private String obId;
		
	private String batchOrCertificateNo;
	private String godownName;
	
	private Double qtyAvlForDc;
	
    @PrePersist
    protected void onCreate() {
        this.qtyAvlForDc = quantity;
    }
}
