package com.tanfed.inventry.model;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementRegister {

	private String activity;
	private String godownName;
	private String dcNo;
	private LocalDate date;
	private String ifmsId;
	private String nameOfInstitution;
	private String district;
	private String vehicleNo;
	private List<ProductDataMovementRegister> productData;
}
