package com.tanfed.inventry.entity;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.StockRecoveryTable;
import com.tanfed.inventry.model.TcCheckMemoChargesTable;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table
@NoArgsConstructor
@AllArgsConstructor
public class TcCheckMemo {

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
	private String contractFirm;
	private String claimBillNo;
	private LocalDate claimBillDate;
	private Double totalBillValue;
	private String gstReturnType;
	private String gstNo;
	private LocalDate date;

	private Double totalChargesValue;
	private Double totalCGST;
	private Double totalSGST;
	private Double totalPaymentValue;
	private Double totalRecoveryValue;
	private Double recoveryIfAny;
	private Double netPaymentAfterAdjustment;
	private String tcsOrTds;
	private Double rate;
	private Double percentageValue;
	private Double netPaymentAfterTdsTcs;
	private String remarks;
	private String pvNo;
	private List<String> jvNo;
	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<TcCheckMemoChargesTable> chargesData;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<StockRecoveryTable> recoveryData;
}
