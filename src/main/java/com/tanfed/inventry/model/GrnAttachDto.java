package com.tanfed.inventry.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrnAttachDto {

	private String invoiceNo;
	private List<String> grnNo;
	private Double currentBookingQty;
}
