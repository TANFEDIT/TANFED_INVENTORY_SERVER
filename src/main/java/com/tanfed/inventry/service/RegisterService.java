package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import com.tanfed.inventry.model.DespatchAdviceRegisterTable;
import com.tanfed.inventry.model.InvoiceCollectionRegisterTable;
import com.tanfed.inventry.model.MovementRegister;
import com.tanfed.inventry.model.PoRegisterTable;
import com.tanfed.inventry.model.PurchaseDayBookQty;
import com.tanfed.inventry.model.PurchaseDayBookValue;
import com.tanfed.inventry.model.RegisterTable;
import com.tanfed.inventry.model.TcBillRegisterTable;
import com.tanfed.inventry.response.StockRegisterTable;

public interface RegisterService {

	public List<RegisterTable> getReceiptRegisterData(String officeName, String month, String godownName, LocalDate fromDate, 
			LocalDate toDate, String productName) throws Exception;
	
	public List<RegisterTable> getSalesRegisterData(String officeName, String month, String godownName, LocalDate fromDate, 
			LocalDate toDate, String productName, String buyerName) throws Exception;
	
	public List<RegisterTable> getStockTransferIssueData(String officeName, String month, String godownName, LocalDate fromDate, 
			LocalDate toDate, String productName) throws Exception;

	public List<RegisterTable> getStockTransferReceiptData(String officeName, String month, String godownName, LocalDate fromDate, 
			LocalDate toDate, String productName) throws Exception;
	
	public List<StockRegisterTable> getStockRegisterData(String officeName, LocalDate fromDate, LocalDate toDate, String productName, 
			String godownName, String month) throws Exception;
	
	
	
	
	public List<InvoiceCollectionRegisterTable> getInvoiceWatchingRegister(String officeName, String month, LocalDate fromDate, 
			String branchName, String godownName, LocalDate toDate) throws Exception;
	
	public List<InvoiceCollectionRegisterTable> getInvoicePresentationRegister(String officeName, String month, LocalDate fromDate, 
			String branchName, LocalDate toDate) throws Exception;
	
	public List<InvoiceCollectionRegisterTable> getInvoiceCollectionRegister(String officeName, String month, LocalDate fromDate, 
			String branchName, LocalDate toDate, String jwt) throws Exception;
	
	public List<InvoiceCollectionRegisterTable> getFundTransferRegister(String officeName, String month, LocalDate fromDate, 
			String branchName, LocalDate toDate, String accountNo, String jwt) throws Exception;
	
	
	
	
	public List<PoRegisterTable> getPoRegisterData(String officeName, String month, LocalDate fromDate, 
			String supplierName, LocalDate toDate, String productName) throws Exception;
	
	public List<PoRegisterTable> getPoAllotmentRegisterData(String officeName, String month, LocalDate fromDate, 
			LocalDate toDate, String poNo) throws Exception;
	
	public List<DespatchAdviceRegisterTable> getDespatchAdviceRegisterData(String officeName, String month, LocalDate fromDate, 
			LocalDate toDate) throws Exception;
	
	
	public List<PurchaseDayBookValue> getPurchaseDayBookValueData(String month, LocalDate fromDate, LocalDate toDate,
			String activity, String supplierName, String productName, String poNo) throws Exception;
	
	public List<PurchaseDayBookQty> getPurchaseDayBookQtyData(String month, LocalDate fromDate, LocalDate toDate,
			String activity, String supplierName, String productName, String poNo) throws Exception;
	
	public List<TcBillRegisterTable> getTcBillRegisterData(String officeName, String month, LocalDate fromDate, LocalDate toDate) throws Exception;

	public List<TcBillRegisterTable> getMpaBillRegisterData(String officeName, String month, LocalDate fromDate, LocalDate toDate) throws Exception;
	
	public List<MovementRegister> getMovementRegisterData(String officeName, String godownName, LocalDate fromDate, LocalDate toDate, String outwardBatchNo) throws Exception;
	
	
	
}
