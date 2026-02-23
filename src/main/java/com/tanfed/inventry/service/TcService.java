package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.TcBillEntry;
import com.tanfed.inventry.entity.TcBillEntryTempTable;
import com.tanfed.inventry.entity.TcCheckMemo;
import com.tanfed.inventry.response.DataForTcBillEntry;
import com.tanfed.inventry.response.DataForTcCheckMemo;

public interface TcService {

	public DataForTcBillEntry getDataForTcBillEntry(String officeName, String jwt, String godownName, String claimFor,
			String idNo, LocalDate fromDate, LocalDate toDate, String loadType, String clNo, String claimBillNo)
			throws Exception;

	public ResponseEntity<String> updateBillEnteredDcGtnGrn(String jwt, TcBillEntryTempTable obj) throws Exception;

	public ResponseEntity<String> saveTcBillEntry(String jwt, TcBillEntry obj) throws Exception;

	public DataForTcCheckMemo getDataForTcCheckMemo(String jwt, String officeName, String checkMemoNo, String month)
			throws Exception;

	public List<TcBillEntry> fetchTcBillEntryByOfficeName(String officeName);

	public List<TcCheckMemo> fetchTcCheckMemoByOfficeName(String officeName);

	public ResponseEntity<String> saveTcCheckMemo(String jwt, TcCheckMemoDto obj) throws Exception;

	public ResponseEntity<String> revertTcDataInTempTable(Long id) throws Exception;

	public void updateAccJvAndPv(TcCheckMemo tcCheckMemo, String jwt) throws Exception;

	public TcCheckMemo getTcCheckMemoByCheckMemoNo(String checkMemoNo) throws Exception;

	public ResponseEntity<String> updatePvNoTcCheckMemo(Long id, String pvNo) throws Exception;

	public void revertBillEntryData(String jwt, TcBillEntry obj) throws Exception;

	public void revertCheckMemo(String jwt, TcCheckMemo tcCheckMemo) throws Exception;

}
