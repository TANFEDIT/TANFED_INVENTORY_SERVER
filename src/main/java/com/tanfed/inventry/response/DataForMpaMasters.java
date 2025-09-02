package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.ManPowerAgency;
import com.tanfed.inventry.entity.MpaEmployeeData;
import com.tanfed.inventry.model.GstRateData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForMpaMasters {

	private Set<String> gstCategoryList;
	private List<Double> gstRateList;
	private GstRateData gstData;
	
	private List<MpaEmployeeData> empDefaultData;
	private Set<String> contractFirmList;
	
	private ManPowerAgency mpaData;
	private List<MpaEmployeeData> empData;
}
