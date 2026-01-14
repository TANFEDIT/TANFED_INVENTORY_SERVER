package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.DeliveryChellan;
import com.tanfed.inventry.entity.GTN;
import com.tanfed.inventry.model.CompondLoadDcData;
import com.tanfed.inventry.model.DespatchAdviseTable;
import com.tanfed.inventry.model.TableDataForDc;
import com.tanfed.inventry.response.DataForDc;

public interface DcService {

	public ResponseEntity<String> saveDc(DeliveryChellan obj, String jwt)  throws Exception;

	public void saveDc(DeliveryChellan obj)  throws Exception;

	public ResponseEntity<String> editDc(DeliveryChellan obj, String jwt)  throws Exception;
	
	public List<DeliveryChellan> getDeliveryChellanDataByOffficeName(String officeName) throws Exception;

	public DeliveryChellan getDcDataByDcNo(String dcNo) throws Exception;
	
	public DataForDc getDataForDeliveryChellan(String officeName, String jwt, String ifmsId, String activity, LocalDate date, String despatchAdviceNo,
			String productName, String godownName, String dcNo) throws Exception;

	public List<DespatchAdviseTable> fetchDcData(String despatchAdviceNo) throws Exception;

	public ResponseEntity<String> updateCombinedLoad(List<CompondLoadDcData> obj) throws Exception;

	public List<DeliveryChellan> getDcDataByClNo(String clNo) throws Exception;
	
	public void updateClosingBalance(DeliveryChellan dc) throws Exception;

	public List<TableDataForDc> getObData(String officeName, String productName, String godownName);

	public void createDcForOtherRegionReceipt(GTN gtn, String jwt) throws Exception;
}
