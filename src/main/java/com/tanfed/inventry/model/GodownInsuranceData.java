package com.tanfed.inventry.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GodownInsuranceData {
	private Long id;

	private String hoLetterRcNo;

	private LocalDate insuranceDate;

	private LocalDate insuranceFrom;

	private LocalDate insuranceTo;

	private GodownInfo godown;
}
