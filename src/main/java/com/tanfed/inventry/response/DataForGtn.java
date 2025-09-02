package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.GTN;
import com.tanfed.inventry.model.TableDataForDc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForGtn {

	private Set<String> godownNameList;
	private Set<String> productNameList;
	private String productCategory;
	private String productGroup;
	private String supplierName;
	private String supplierGst;
	private String packing;
	private String standardUnits;
	
	private List<String> officeList;
	private Set<String> designationList;
	private List<String> gtnNoList;
	private List<String> rrNoList;
	private LocalDate rrDate;
	private GTN gtnData;
	private String transporterName;
	private Double km;
	private Double transportChargesPerQty;
	private Double loadingChargesPerQty;
	private Double unloadingChargesPerQty;
	private String buyerName;
	private String buyerDistrict;
	private List<TableDataForDc> tableData;
	private String fromIfmsId;
	private String toIfmsId;
	private String buyerGstNo;
}
