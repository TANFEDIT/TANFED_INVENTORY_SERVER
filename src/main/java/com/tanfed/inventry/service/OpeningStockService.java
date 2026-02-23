package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.tanfed.inventry.entity.OpeningStock;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.response.DataForOpeningStock;

public interface OpeningStockService {

	public ResponseEntity<String> saveOpeningStock(List<OpeningStock> obj, String jwt) throws Exception;

	public ResponseEntity<String> editOpeningStock(OpeningStock obj, String jwt) throws Exception;

	public List<OpeningStock> getOpeningStockByOfficeName(String officeName) throws Exception;

	public DataForOpeningStock getDataForOpeningStock(String activity, String productName, String jwt,
			String officeName) throws Exception;

	public OpeningStock getObById(String obId) throws Exception;

	public ResponseEntity<String> updateGrnQtyForDc(GrnQtyUpdateForDc obj, String despatchAdviceNo) throws Exception;

	public void revertGrnQtyForDc(GrnQtyUpdateForDc temp) throws Exception;

}
