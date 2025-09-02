package com.tanfed.inventry.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompondLoadDcData {

	private String dcNo;
	private String nameOfInstitution;
	private Double qty;
	private String vehicleNo;
}
