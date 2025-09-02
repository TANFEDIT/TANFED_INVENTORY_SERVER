package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDayBookQty {

	private String checkMemoNo;
	private String region;
	private Double direct;
	private Double buffer;
	private Double total;
	private List<PurchaseDayBookQtyGrnData> grnData;
	
	private Double directBalance;
	private Double bufferBalance;
	
	private String status;
}
