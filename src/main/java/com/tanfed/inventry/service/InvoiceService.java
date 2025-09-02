package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.model.AdjustmentReceiptVoucher;
import com.tanfed.inventry.model.DespatchAdviseTable;
import com.tanfed.inventry.model.InvoiceUpdate;
import com.tanfed.inventry.response.DataForInvoice;
import com.tanfed.inventry.response.DataForInvoiceUpdate;
import com.tanfed.inventry.response.InvoiceDataForDnCn;


public interface InvoiceService {

	public ResponseEntity<String> saveInvoice(Invoice obj, String jwt)  throws Exception;

	public ResponseEntity<String> editInvoice(Invoice obj, String jwt)  throws Exception;

	public List<Invoice> getInvoiceDataByOffficeName(String officeName) throws Exception;

	public DataForInvoice getDataForInvoice(String officeName, String activity, String dcNo, String jwt, String collectionMode,
			String month, String godownName) throws Exception;

	public List<DespatchAdviseTable> fetchInvoiceData(String despatchAdviceNo) throws Exception;

	public DataForInvoiceUpdate getDataForInvoiceUpdate(String officeName, String activity, String selectedOption,
			LocalDate fromDate, LocalDate toDate, String invoiceNo) throws Exception;

	public ResponseEntity<String> updateInvoice(List<InvoiceUpdate> obj) throws Exception;

	public ResponseEntity<String> updateAdjReceiptNoInNonCcInvoice(String invoiceNo, String voucherNo, LocalDate date)
			throws Exception;
	
	public InvoiceDataForDnCn getDataForDnCn(String invoiceNo) throws Exception;
	
	public void revertNonCCInvoice(Invoice obj, String jwt, AdjustmentReceiptVoucher adjv) throws Exception;
	
	public List<Invoice> getInvoiceDataFromDateOfficeName(String officeName, LocalDate Date) throws Exception;
	
	public Invoice getInvoiceDataByInvoiceNo(String invoiceNo) throws Exception;
}
