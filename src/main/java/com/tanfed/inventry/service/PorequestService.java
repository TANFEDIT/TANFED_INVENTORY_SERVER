package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.PoRequest;
import com.tanfed.inventry.model.PoTableData;
import com.tanfed.inventry.response.DataForPoRequest;

public interface PorequestService {

	public ResponseEntity<String> savePoRequest(PoRequest obj, String jwt) throws Exception;

	public ResponseEntity<String> editPoRequest(PoRequest obj, String jwt) throws Exception;

	public List<PoRequest> getPoRequestDataByOfficeName(String officeName) throws Exception;

	public DataForPoRequest getDataForPoRequest(String officeName, String activity, String jwt, String productName,
			String supplierName, String purchaseOrderType, String poNo, LocalDate date) throws Exception;

	public List<PoRequest> getPoRequestData() throws Exception;

	public ResponseEntity<String> updatePoIssueQty(List<PoTableData> obj, String productName) throws Exception;

	public void updateRejectedQty(PoTableData obj, String productName) throws Exception;

	public PoRequest getPoRequestDataByNo(String poReqNo) throws Exception;

}
