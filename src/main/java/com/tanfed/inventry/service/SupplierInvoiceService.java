package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.tanfed.inventry.entity.SupplierInvoiceDetails;
import com.tanfed.inventry.model.GrnAttachDto;
import com.tanfed.inventry.response.DataForSupplierInvoice;

public interface SupplierInvoiceService {

	public ResponseEntity<String> saveSupplierInvoice(String obj, MultipartFile[] files, String jwt) throws Exception;
	
	public DataForSupplierInvoice getDataForSupplierInvoice(String activity, String jwt, String supplierName, String monthOfSupply,
			String productName, String poMonth, String poNo, String officeName, String invoiceNumber, String invoiceNo) throws Exception;
	
	public ResponseEntity<String> updateSupplierInvoiceQtyForGrnAttach(GrnAttachDto obj) throws Exception;
	
	public SupplierInvoiceDetails getSupplierInvoiceByInvoiceNumber(String invoiceNumber) throws Exception;
	
	public List<SupplierInvoiceDetails> getSupplierInvoiceDetails() throws Exception;
}
