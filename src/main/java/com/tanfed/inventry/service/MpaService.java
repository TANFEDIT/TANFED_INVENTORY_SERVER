package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.ManPowerAgency;
import com.tanfed.inventry.entity.MpaBillEntry;
import com.tanfed.inventry.entity.MpaCheckMemo;
import com.tanfed.inventry.entity.MpaEmployeeData;
import com.tanfed.inventry.model.MpaCheckMemoDto;
import com.tanfed.inventry.response.DataForMpaBillEntry;
import com.tanfed.inventry.response.DataForMpaCheckMemo;
import com.tanfed.inventry.response.DataForMpaMasters;

public interface MpaService {

	public ResponseEntity<String> saveManpowerAgency(String jwt, ManPowerAgency obj) throws Exception;

	public ResponseEntity<String> saveMpaEmployeeData(String jwt, MpaEmployeeData obj) throws Exception;

	public ResponseEntity<String> updateMpaMastersEmpData(List<MpaEmployeeData> obj) throws Exception;

	public DataForMpaMasters getDataForMpaMasters(String jwt, String gstCategory, Double gstRate, String formType,
			String officeName, String contractFirm) throws Exception;

	public DataForMpaBillEntry getDataForMpaBillEntry(String officeName, String contractFirm, String financialMonth,
			String engagedAs) throws Exception;

	public DataForMpaCheckMemo getDataForMpaCheckMemo(String jwt, String officeName, String checkMemoNo, String month)
			throws Exception;

	public ResponseEntity<String> updateEmpStatus(Long id, String status) throws Exception;

	public ResponseEntity<String> saveMpaBillEntry(MpaBillEntry obj, String jwt) throws Exception;

	public ResponseEntity<String> saveMpaCheckMemo(String jwt, MpaCheckMemoDto obj) throws Exception;

	public List<MpaBillEntry> getMpaBillEntryByOfficeName(String officeName) throws Exception;

	public List<MpaCheckMemo> getMpaCheckMemoByOfficeName(String officeName) throws Exception;

	public ResponseEntity<String> updatePvNoMpaCheckMemo(Long id, String pvNo) throws Exception;

	public MpaCheckMemo getMpaCheckMemoByCheckMemoNo(String checkMemoNo) throws Exception;

	public void updateAccPvJv(MpaCheckMemo mpaCheckMemo, String jwt) throws Exception;

	public void revertCheckMemo(String cmNo) throws Exception;

}
