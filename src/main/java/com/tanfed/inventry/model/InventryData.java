package com.tanfed.inventry.model;

import java.util.List;

import com.tanfed.inventry.entity.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventryData {

	private DeliveryChellan dcData;
	private List<DeliveryChellan> dc;
	
	private GRN grnData;
	private List<GRN> grn;
	
	private GTN gtnData;
	private List<GTN> gtn;
	
	private DespatchAdvice despatchAdviceData;
	private List<DespatchAdvice> despatchAdvice;
	
	private Invoice invoiceData;
	private List<Invoice> invoice;
	
	private OpeningStock openingStockData;
	private List<OpeningStock> openingStock;
	
	private PoRequest poRequestData;
	private List<PoRequest> poRequest;
	
	private PurchaseOrder purchaseOrderData;
	private List<PurchaseOrder> purchaseOrder;
	
	private TermsPrice termsAndPriceData;
	
	private List<TermsPrice> termsAndPrice;
	private List<TermsPrice> priceCircular;
	private List<PurchaseOrder> purchaseOrderRO;
	
	private ICP1Data invoiceAckEntry;
	private ICP1Data invoiceCollectionAvailable;
	private ICP1Data presentToCCB;
	private ICP1Data collectionUpdate;
	private FundTransfer fundTransfer;
}
