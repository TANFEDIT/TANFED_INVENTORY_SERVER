package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.DcTableData;
import com.tanfed.inventry.entity.DespatchAdvice;
import com.tanfed.inventry.response.DataForDespatchAdvice;

public interface DespatchAdviceService {

	public ResponseEntity<String> saveDespatchAdvice(DespatchAdvice obj, String jwt)  throws Exception;

	public ResponseEntity<String> editDespatchAdvice(DespatchAdvice obj, String jwt)  throws Exception;

	public List<DespatchAdvice> getDespatchAdviceDataByOffficeName(String officeName) throws Exception;

	public DataForDespatchAdvice getDataForDespatchAdvice(String officeName, String activity, String nameOfInstitution, String productName, 
			String jwt, String month, String godownName) throws Exception;
	
	public List<String> getUnfullfilledDespatchAdviceNo(String officeName, String activity, String godownName) throws Exception;
	
	public DespatchAdvice getDespatchAdviceDataByDespatchAdviceNo(String DespatchAdviceNo) throws Exception;
	
	public void updateDespatchAdviceQty(String DespatchAdviceNo, List<DcTableData> obj) throws Exception;

	public void revertDespatchAdviceQty(String DespatchAdviceNo, List<DcTableData> obj) throws Exception;
	
	public void updateDespatchAdviceStatus(Long id) throws Exception;

	public List<String> fetchOtherRegionDaNoList(String officeName, String toRegion, String buyerName) throws Exception;
}
