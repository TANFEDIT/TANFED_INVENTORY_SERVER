package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.PurchaseBooking;
import com.tanfed.inventry.model.PurchaseBookingDto;
import com.tanfed.inventry.response.DataForPurchaseBooking;

public interface PurchaseBookingService {

	public DataForPurchaseBooking getDataForPurchaseBooking(String jwt, String activity, String productCategory, String supplierName,
			String productName, String poType, String poMonth, String poNo) throws Exception;
	
	public ResponseEntity<String> savePurchaseBooking(PurchaseBookingDto obj, String jwt) throws Exception;
	
	public PurchaseBooking getPurchaseBookedDataByCmNo(String checkMemoNo) throws Exception;

	public List<PurchaseBooking> findPurchaseBookedDataByActivity(String activity) throws Exception;

	public void updateAccJv(PurchaseBooking purchaseBooking, String jwt) throws Exception;
}
