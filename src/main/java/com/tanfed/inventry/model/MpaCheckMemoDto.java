package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.entity.MpaBillEntry;
import com.tanfed.inventry.entity.MpaBillEntryEmpTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaCheckMemoDto {
	private Long id;
	private LocalDate date;
	private String checkMemoNo;
	private String officeName;

	private MpaBillEntry mpaBillEntry;
	private Double totalCalculatedValue;
	private Double totalSgstValue;
	private Double totalCgstValue;
	private Double totalPaymentValue;

	private Double recoveryIfAny;
	private Double netTotalDeduction;
	private String tcsOrTds;
	private Double rate;
	private Double calculatedTcsTdsValue;

	private Double netPaymentAfterAdjustment;
	private Double difference;
	private String remarks;
	private JournalVoucher jvData;
	private PaymentVoucher pvData;

	private String financialYear;
	private String financialMonth;
	private String contractFirm;
	private String claimBillNo;
	private LocalDate claimBillDate;
	private Double totalBillValue;
	private List<MpaBillEntryEmpTable> empData;
	private List<String> designation;
	private String voucherStatus;

}
