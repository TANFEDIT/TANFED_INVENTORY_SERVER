package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.response.DataForTPM;

public interface TermsPriceService {

	public ResponseEntity<String> saveTermsPriceMaster(TermsPrice obj, String jwt) throws Exception;
	
	public ResponseEntity<String> editTermsPriceMaster(TermsPrice obj, String jwt) throws Exception;

	public DataForTPM dataForTPM(String activity, String jwt, String supplierName, String productName) throws Exception;
	
	public List<TermsPrice> getTermsPriceMasterData() throws Exception;
	
	public Set<String> fetchApprovedProductName(String activity) throws Exception;
	
	public Set<String> fetchApprovedTermsMonth(String activity, String productName) throws Exception;
	
	public List<String> fetchTermsByMonth(String termsMonth, String activity, String productName, LocalDate date) throws Exception;
	
	public TermsPrice fetchTermsByTermsNo(String termsNo) throws Exception;
}
 