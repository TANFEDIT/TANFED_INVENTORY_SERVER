package com.tanfed.inventry.model;

import java.util.List;

import com.tanfed.inventry.entity.DespatchAdviceTableDataEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DespatchAdviceData {

	private Long id;
	private String activity;
	private String despatchAdviceNo;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private String godownName;
	private List<DespatchAdviceTableDataEntity> tableData;
	private List<DespatchAdviseTable> qtyData;
}
