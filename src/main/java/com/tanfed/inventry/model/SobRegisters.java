package com.tanfed.inventry.model;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SobRegisters {

	private List<PurchaseDayBookValue> purchaseDayBookValue;
	private List<PurchaseDayBookQty> purchaseDayBookQty;
	private List<TcBillRegisterTable> tcBillRegister;
	private List<TcBillRegisterTable> mpaBillRegister;
	private List<String> poNoList;
	private Set<String> productNameList;
	private Set<String> supplierNameList;
}
