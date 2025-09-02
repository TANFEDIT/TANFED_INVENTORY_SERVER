package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class MpaCheckMemo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private List<String> designation;
	private List<String> empId;
	private String voucherStatus;
	private LocalDate createdAt = LocalDate.now();
	private LocalDate approvedDate;
	private LocalDate date;
	private String checkMemoNo;
	private String officeName;
	
	private String financialYear;
	private String financialMonth;
	private String contractFirm;
	private String claimBillNo;
	private LocalDate claimBillDate;
	private Double totalBillValue;
	
	private Double totalCalculatedValue;
	private Double totalSgstValue;
	private Double totalCgstValue;
	private Double totalPaymentValue;

	private Double recoveryIfAny;
	private Double netTotalDeduction;
	private String tcsOrTds;
	private Double rate;
	private Double calculatedTcsTdsValue;
	
	private String jvNo;
	private String pvNo;
	private Double netPaymentAfterAdjustment;
	private Double difference;
	private String remarks;
	
	
	
	
	
	
	
	
	
}
