package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.entity.TcCheckMemo;
import com.tanfed.inventry.model.ContractorGstData;
import com.tanfed.inventry.model.StockRecoveryTable;
import com.tanfed.inventry.model.TcCheckMemoChargesTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForTcCheckMemo {

	private List<String> checkMemoNoList;

	private String financialYear;
	private String financialMonth;
	private String contractFirm;
	private String gstNo;
	private String claimBillNo;
	private LocalDate claimBillDate;
	private Double totalBillValue;
	private String gstReturnType;
	private List<ContractorGstData> gstData;
	private List<TcCheckMemoChargesTable> chargesData;

	private List<StockRecoveryTable> recoveryData;
	private List<TcCheckMemo> tcCheckMemoData;

}
