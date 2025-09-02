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
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class TcBillEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private LocalDate createdAt = LocalDate.now();
	private LocalDate approvedDate;
	private List<String> empId;
	private String officeName;
	private List<String> designation;
	private String voucherStatus;
	
	private String checkMemoNo;
	private String financialYear;
	private String financialMonth;
	private String godownName;
	private String contractFirm;
	private String claimBillNo;
	private LocalDate claimBillDate;	
	private Double totalQty;
	private Double totalBillValue;
	private LocalDate date; 
	
	private Double totalTransportCharges;
	private Double totalLoadingCharges;
	private Double totalUnloadingCharges;
	private Double totalWagonClearanceCharges;
	
	private Boolean isTcCheckMemoDone;
	
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TcBillEntryChargesTable> chargesData;
}
