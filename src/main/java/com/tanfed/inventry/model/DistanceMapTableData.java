package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceMapTableData {

	private Long id;
	private String ifmsId;
	private String name;
	private Double km;
	private Double hillKm;
	private String status;
}
