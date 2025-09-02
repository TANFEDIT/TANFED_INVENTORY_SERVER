package com.tanfed.inventry.response;

import java.util.List;

import com.tanfed.inventry.model.InvoiceTableUpdateResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForInvoiceUpdate {

	private List<InvoiceTableUpdateResponse> invoiceData;
}
