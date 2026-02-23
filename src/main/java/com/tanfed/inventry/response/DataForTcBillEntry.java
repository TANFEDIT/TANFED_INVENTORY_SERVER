package com.tanfed.inventry.response;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.TcBillEntryTempTable;
import com.tanfed.inventry.model.ContractorTenderData;
import com.tanfed.inventry.model.ProductClassificationTableBillEntry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForTcBillEntry {

	private Set<String> GodownNameList;
	private String contractFirm;
	private String contractThrough;
	private List<ContractorTenderData> tenderData;
	private List<String> idNoList;
	private Set<String> clNoList;

	private Long id;
	private Double ackQty;
	private String supplierName;
	private String productName;
	private String packing;
	private String bags;
	private Double calcUnloadingCharges;
	private Double calcWagonClearanceCharges;
	private LocalDate date;

	private Double calcTransportCharges;
	private Double transportCharges;
	private Double transportChargesPerQty;
	private Double calcLoadingCharges;
	private Double loadingCharges;
	private Double loadingChargesPerQty;
	private String ifmsId;
	private String godownBuyerName;
	private Double qty;

	private List<ProductClassificationTableBillEntry> tableData;
	private List<TcBillEntryTempTable> chargesData;
}
