package com.tanfed.inventry.response;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.entity.MpaEmployeeData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForMpaBillEntry {

	private Set<String> firmList;
	
	private String appointedThrough;
	
	private String appointedLr;
	
	private String lrDate;
	
	private List<MpaEmployeeData> empData;
}
