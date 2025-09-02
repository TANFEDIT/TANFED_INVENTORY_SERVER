package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.dto.FundTransferDto;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.response.InvoiceCollectionResponseData;

public interface InvoiceCollectionService {

	public ICViewAplApdData getINCLViewAPlApd(String formType, LocalDate fromDate, LocalDate toDate, String officeName, String voucherStatus, String icmNo, String jwt) throws Exception;

	public ResponseEntity<String> updateInvoiceCollection(List<InvoiceCollectionObject> obj, String jwt) throws Exception;

	public InvoiceCollectionResponseData getDataForInvoiceCollections(String officeName, String activity, String monthOfSales, 
			LocalDate fromDate, LocalDate toDate, String invoiceType, String materialCenter, String ccbBranch, LocalDate ackEntryDate, 
			LocalDate addedToPresentDate, LocalDate dueDate, String icmNo, String collectionProcess, String accountNo,
			String branchName, LocalDate date, String jwt, String transferType, String toBranchName) throws Exception;
	
	public ResponseEntity<String> saveFundTransfer(FundTransferDto obj, String jwt) throws Exception;
	
	public String updateAplStatusInvoiceCollection(VoucherApproval obj, String jwt) throws Exception;
	
	public ResponseEntity<String> editInvoiceCollectionData(String formType, InventryData obj, String jwt) throws Exception;

	public InvoiceCollectionResponseData getCollectionAbstractData(String officeName,
			String branchName, String accountNo, String monthOfFundTransfer, String jwt) throws Exception;

	public ResponseEntity<String> saveAdjReceiptForIcmInvoices(AdjustmentReceiptVoucher obj, String jwt, String type) throws Exception;
	
}
