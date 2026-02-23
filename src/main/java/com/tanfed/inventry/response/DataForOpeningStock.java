package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.GstRateData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForOpeningStock {

	private List<String> productNameList;
	private Set<String> godownNameList;
	private List<String> supplyModeList;
	private String supplierName;
	private String supplierGst;
	private String standardUnits;
	private String productCategory;
	private String productGroup;
	private String batchNo;
	private String certification;
	private String packing;
	private String gstCategory;
	private Double gstRate;
	private GstRateData gstData;
}
