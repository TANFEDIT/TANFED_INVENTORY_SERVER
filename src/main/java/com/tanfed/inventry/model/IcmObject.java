package com.tanfed.inventry.model;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IcmObject {

	private AdjustmentReceiptVoucher adjData;
	private List<InvoiceCollectionObject> invoices;
}
