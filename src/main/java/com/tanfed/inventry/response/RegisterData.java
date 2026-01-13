package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.DespatchAdviceRegisterTable;
import com.tanfed.inventry.model.MovementRegister;
import com.tanfed.inventry.model.PoRegisterTable;
import com.tanfed.inventry.model.RegisterTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterData {

	private Set<String> godownNameList;
	private Set<String> productNameList;
	private Set<String> supplierNameList;
	
	private List<RegisterTable> receiptRegister;
	private List<RegisterTable> salesRegister;
	private List<RegisterTable> stockTransferIssueRegister;
	private List<RegisterTable> stockTransferReceiptRegister;
	
	private List<StockRegisterTable> stockRegister;

	private List<String> poNoList;
	private List<PoRegisterTable> poRegister;
	private List<PoRegisterTable> poAllotmentRegister;
	private List<DespatchAdviceRegisterTable> despatchAdviceRegister;
	private List<MovementRegister> movementRegister;
}
