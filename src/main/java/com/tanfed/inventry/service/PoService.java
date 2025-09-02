package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.response.DataForPo;

public interface PoService {

	public ResponseEntity<String> savePurchaseOrder(PurchaseOrder obj, String jwt)  throws Exception;

	public ResponseEntity<String> editPurchaseOrder(PurchaseOrder obj, String jwt) throws Exception;
	
	public DataForPo getDataForPurchaseOrder(String activity, String productName, String jwt, String termsMonth, String termsNo, 
			String poBased, String officeName, String purchaseOrderType, LocalDate date) throws Exception;
	
	public List<PurchaseOrder> getPoData() throws Exception;

	public List<String> getUnfullfilledPoNo(String productName, String officeName, String poNoFor, LocalDate date) throws Exception;
	
	public PurchaseOrder getPoByPoNo(String poNo) throws Exception;
		
}
