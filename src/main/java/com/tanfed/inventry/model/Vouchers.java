package com.tanfed.inventry.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vouchers {

	private JournalVoucher journalVoucherData;
	private AdjustmentReceiptVoucher adjustmentReceiptVoucherData;
	private PaymentVoucher paymentVoucherData;
}
