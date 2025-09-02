package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceMapping {

	private Long id;
	private String officeName;
	private String toRegion;
	private String godownName;
	private String district;
	private String status;
	private String type;
	private List<DistanceMapTableData> tableData;
}
