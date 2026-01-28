package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.PaymentVoucher;
import com.tanfed.inventry.model.StockRecoveryTable;
import com.tanfed.inventry.model.TcCheckMemoChargesTable;
import com.tanfed.inventry.model.TcCheckMemoGstData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCheckMemoDto {

	private List<String> designation;
	private String voucherStatus;
	private Long id;
	private String officeName;
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
	private List<JournalVoucher> jvData;
	private List<TcCheckMemoChargesTable> chargesData;
	private List<TcCheckMemoGstData> gstData;
	private PaymentVoucher pvData;
	private List<StockRecoveryTable> recoveryData;
}
