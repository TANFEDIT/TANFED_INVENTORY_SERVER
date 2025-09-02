package com.tanfed.inventry.model;

import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForOB {

	private Set<String> bankList;
	private Set<String> mainBranchList;
	private Set<String> subBranchList;
	private Set<String> accTypeList;
	private List<Long> accNoList;
}
