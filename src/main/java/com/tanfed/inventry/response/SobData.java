package com.tanfed.inventry.response;

import java.util.List;

import com.tanfed.inventry.dto.CheckMemoGoodsDto;
import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.MpaCheckMemoDto;
import com.tanfed.inventry.model.PurchaseBookingDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SobData {

	private SupplierInvoiceDetails supplierInvoiceDetails;
//	private List<SupplierInvoiceDetails> supplierInvoice;
	
	private PurchaseBooking purchaseBookingData;
	private List<PurchaseBookingDto> purchaseBooking;
	
	private CheckMemoGoods checkMemoGoodsData;
	private List<CheckMemoGoodsDto> checkMemoGoods;
	
	private PurchaseOrder purchaseOrderData;
	private List<PurchaseOrder> purchaseOrder;
	private List<PurchaseOrder> supplierInvoice;
	
	private List<MpaBillEntry> mpaBillEntry;
	private List<MpaCheckMemoDto> mpaCheckMemo;
	
	private List<TcBillEntry> tcBillEntry;
	private List<TcCheckMemoDto> tcCheckMemo;
}
