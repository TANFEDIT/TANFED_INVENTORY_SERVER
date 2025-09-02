package com.tanfed.inventry.response;

import java.util.List;

import com.tanfed.inventry.entity.GRN;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForGrnUpdate {

	private List<String> grnNoList;
	private GRN grnData;
	private List<GRN> updatedGrnData;
}
