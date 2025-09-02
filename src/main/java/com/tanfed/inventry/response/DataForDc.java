package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tanfed.inventry.model.CompondLoadDcData;
import com.tanfed.inventry.model.TableDataForDc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForDc {

	private String dcNo;
	private String ifmsId;
	private String district;
	private String taluk;
	private String block;
	private String village;
	private String buyerGstNo;
	private String nameOfInstitution;
	private String licenseNo;
	private Set<String> godownNameList;
	private String transporterName;
	private List<String> despatchAdviceNoList;
	private Set<String> productNameList;
	private List<TableDataForDc> tableData;
	private List<CompondLoadDcData> clData;
	private Double km;
	private Double transportChargesPerQty;
	private Double loadingChargesPerQty;
	private String supplyMode;
	private String supplyTo;
	private Double despatchAdviseQty;
	private LocalDate despatchAdviseDate;
}
