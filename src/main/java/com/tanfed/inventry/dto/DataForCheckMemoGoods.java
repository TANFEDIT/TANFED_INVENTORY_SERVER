package com.tanfed.inventry.dto;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.entity.CheckMemoGoods;
import com.tanfed.inventry.entity.PurchaseBooking;
import com.tanfed.inventry.model.GstRateData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForCheckMemoGoods {

	private List<String> checkMemoNoList;
	private PurchaseBooking pbData;
	private String hsnCode;
	private LocalDate poDate;
	private Double bookedQty;
	private String supplierGst;
	private Double gstRate;
	private GstRateData gstData;
	private Double advOutstanding;
	private Double totalGrnQty;
	private Double totalSupplierInvQty;
	private Double jvQty;
	private String purchaseJvNo;
	private List<String> supplierAdvanceNoList;
	private Double advanceQty;
	private Double advanceOutstanding;
	private Double advanceBasicValue;
	private Double advanceTdsTcsValue;
	private String supplierAccountNo;
	private List<CheckMemoGoods> checkMemoGoods;
}
