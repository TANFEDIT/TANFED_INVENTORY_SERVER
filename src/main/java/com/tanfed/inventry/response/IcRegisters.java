package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.InvoiceCollectionRegisterTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class IcRegisters {

	private List<InvoiceCollectionRegisterTable> invoiceWatchingRegister;
	private List<InvoiceCollectionRegisterTable> invoicePresentationRegister;
	private List<InvoiceCollectionRegisterTable> invoiceCollectionRegister;
	private List<InvoiceCollectionRegisterTable> fundTransferRegister;
	private Set<String> godownNameList;
	private Set<String> branchNameList;
	private List<Long> accountNoList;
}
