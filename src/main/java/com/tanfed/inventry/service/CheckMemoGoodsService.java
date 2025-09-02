package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.dto.CheckMemoGoodsDto;
import com.tanfed.inventry.dto.DataForCheckMemoGoods;
import com.tanfed.inventry.entity.CheckMemoGoods;

public interface CheckMemoGoodsService {
	
	public ResponseEntity<String> saveCheckMemoGoods(CheckMemoGoodsDto obj, String jwt) throws Exception;
	
	public DataForCheckMemoGoods getDataForCheckMemoGoods(String activity, String checkMemoNo, String jwt, String supplierAdvanceNo, String month) throws Exception;
	
	public List<CheckMemoGoods> getCheckMemoData() throws Exception;

	public CheckMemoGoods getCheckMemoGoodsByCmNo(String checkMemoNo) throws Exception;
	
	public void updatePvInCheckMemo(String pv, Long id) throws Exception;

	public void updatePvInAcc(CheckMemoGoods checkMemoGoods, String jwt) throws Exception;
}
