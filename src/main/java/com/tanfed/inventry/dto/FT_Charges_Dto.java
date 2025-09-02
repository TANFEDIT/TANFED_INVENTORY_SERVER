package com.tanfed.inventry.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class FT_Charges_Dto {

	private Double currentTransfer;
	private Double bankCharges;
}
