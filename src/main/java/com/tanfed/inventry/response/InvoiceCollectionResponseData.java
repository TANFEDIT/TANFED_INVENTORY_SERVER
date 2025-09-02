package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.FundTransferMonthAndBranchAbstractTableData;
import com.tanfed.inventry.model.InvoiceCollectionP1TableData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCollectionResponseData {

	private List<String> materialCenterLst;
	private List<String> invoiceNoList;
	private Set<String> icmNoList;
	private Set<String> ccbBranchLst;
	private Set<LocalDate> ackEntryDate;
	
	private Set<LocalDate> addedToPresentDate;
	private Set<LocalDate> dueDate;

	
	private Double totalInvoicesValue;
	private List<InvoiceCollectionP1TableData> tableData;
	private List<InvoiceCollectionP1TableData> adjTableData;

	private Integer NoOfInvoicesCreated;
	private Integer NoOfInvoicesAckReceived;
	
	private Integer NoOfAvlAckInvoices;
	
	private Integer NoOfInvoicesAvlToPresent;

	private Integer NoOfInvoicesPresented;

	private Integer NoOfInvoicesCollected;
	private Double noOfInvoicesTransferedToHO;
		
	private Set<String> branchNameList;
	private List<Long> accountNoList;
	
	private Set<String> toBranchNameList;
	private List<Long> toAccountNoList;
	private List<Long> idList;
	
	private String NameOfCcb;
	private Double openingBalance;
	private Double collection;
	private Double ibrAmount;
	private Double total;
	private Double transfer;
	private Double bankCharges;
	private Double others;
	private Double closingBalance;
	private List<FundTransferMonthAndBranchAbstractTableData> abstractTable;
}
