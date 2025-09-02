package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.entity.PurchaseOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForGrn {

	private Set<String> productNameList;
	private List<String> godownNameList;
	private List<String> wagonGrnNoList;
	private List<String> filteredGodownNameList;
	private String supplierName;
	private String supplierGst;
	private String standardUnits;
	private String productCategory;
	private String productGroup;
	private String packing;
	private String batchNo;
	private String certification;
	private String ifmsId;
	private String door;
	private String street;
	private Integer pincode;
	private String district;
	private List<String> poNoList;
	private Set<String> districtList;
	private String modeOfSupply;
	private Double totalPoQty;
	private Double totalDirectQty;
	private Double totalBufferQty;
	private Double totalGrnCreated;
	private Double totalBufferCreated;
	private Double totalDirectCreated;
	private Boolean alert;
	private List<PurchaseOrder> poData;
	private List<GRN> wagonGrnList;
	
	
}
