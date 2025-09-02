package com.tanfed.inventry.model;

import java.util.List;
import java.util.Set;

import com.tanfed.inventry.dto.FundTransferDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ICViewAplApdData {

	private List<ICP1Data> invoice;
	private Set<String> icmNoList;
	private List<FundTransferDto> fundTransfer;
	private AdjustmentReceiptVoucher adjv;
}
