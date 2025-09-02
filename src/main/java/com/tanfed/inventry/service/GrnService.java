package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.model.GrnAttachDto;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.model.TableDataForDc;
import com.tanfed.inventry.model.WagonDataGrn;
import com.tanfed.inventry.response.DataForGrn;
import com.tanfed.inventry.response.DataForGrnUpdate;

public interface GrnService {

	public ResponseEntity<String> saveGrn(GRN obj, String jwt)  throws Exception;

	public void saveGrn(GRN obj)  throws Exception;

	public ResponseEntity<String> editGrn(GRN obj, String jwt)  throws Exception;
	
	public List<GRN> getGrnDataByOffficeName(String officeName) throws Exception;

	public List<GRN> getGrnDataByOffficeAndPoNo(String officeName, String poNo) throws Exception;
	
	public DataForGrn fetchDataForGrn(String officeName, String godownType, String activity, String productName, String poNo, String jwt, 
			String godownName, LocalDate date, String month) throws Exception;
	
	public ResponseEntity<String> updateGrnQtyForDc(List<GrnQtyUpdateForDc> obj) throws Exception;
	
	public ResponseEntity<String> updateGrnAttachQty(GrnAttachDto obj) throws Exception;
	
	public GRN getGrnDataByGrnNo(String grnNo) throws Exception;

	public GRN getGrnDataByRrNo(String rrNo) throws Exception;

	public Set<String> getGodownNameList(String jwt, String officeName) throws Exception;

	public List<TableDataForDc> grnTableData(String officeName, String productName, String godownName, String page) throws Exception;

	public ResponseEntity<String> updateGrn(GRN obj) throws Exception;

	public DataForGrnUpdate getDataForGrnUpdate(String officeName, String activity, String grnNo, String month) throws Exception;

	public void revertGrnQtyForDc(List<GrnQtyUpdateForDc> obj) throws Exception;
	
	public ResponseEntity<String> updateWagonData(WagonDataGrn obj, String grnNo, String jwt) throws Exception;
	
	public void updateJv(String grnNo, String jv) throws Exception;

	public void updatePurchaseBookingStatus(String grnNo) throws Exception;
	
	public void updateClosingBalance(GRN grn) throws Exception;

	public void revertGrnJv(GRN grn, String jwt, String grnNo) throws Exception;
}
