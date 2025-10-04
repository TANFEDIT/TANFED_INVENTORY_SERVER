package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.GTN;
import com.tanfed.inventry.entity.SalesReturn;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.TableDataForDc;
import com.tanfed.inventry.response.DataForGtn;

public interface GtnService {

	public ResponseEntity<String> saveGtn(GTN obj, String jwt, SalesReturn salesReturn)  throws Exception;

	public ResponseEntity<String> editGtn(GTN obj, String jwt)  throws Exception;
	
	public List<GTN> getGtnDataByOffficeName(String officeName) throws Exception;

	public DataForGtn getDataForGtn(String officeName, String productName, String activity, String gtnFor, String rrNo, LocalDate date, String transactionFor, 
			String jwt, String godownName, String toRegion, String issuedGtnNo, String destination, String transportCharges, String loadingCharges,
			String unloadingCharges, String month, String suppliedGodown, String invoiceNo) throws Exception;

	public List<TableDataForDc> gtnTableData(String officeName, String productName, String godownName) throws Exception;
	
	public ResponseEntity<String> updateGrnQtyForDc(GrnQtyUpdateForDc obj) throws Exception;
	
	public void revertGrnQtyForDc(GrnQtyUpdateForDc obj) throws Exception;

	public GTN getGtnDataByGtnNo(String gtnNo) throws Exception;

	public GTN getReceiptGtnDataByGtnNo(String gtnNo) throws Exception;

	public String fetchTermsNoFromGrnNo(String grnNo);
	
	public void saveGtn(GTN gtn) throws Exception;
	
	public void updateClosingBalanceIssue(GTN gtn) throws Exception;

	public void updateClosingBalanceReceipt(GTN gtn) throws Exception;

	public void updateClosingBalanceReceipt(SalesReturn salesReturn) throws Exception;
	
	public void updateJVStatusInAcc(String jvNo, String status, String jwt) throws Exception;

	public ResponseEntity<String> updateJvForSalesReturn(String gtnNo, JournalVoucher jv, String jwt) throws Exception;

	public ResponseEntity<String> saveSalesReturn(SalesReturn obj, String jwt) throws Exception;
}
