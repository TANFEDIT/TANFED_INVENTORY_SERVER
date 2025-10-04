package com.tanfed.inventry.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tanfed.inventry.entity.DcTableData;
import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.response.*;
import com.tanfed.inventry.service.*;

@RestController
@RequestMapping("/api/inventry")
public class InventryHandler {

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private PorequestService porequestService;

	@Autowired
	private PoService poService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	@Autowired
	private DcService dcService;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private OpeningStockService openingStockService;

	private static Logger logger = LoggerFactory.getLogger(InventryHandler.class);

	@PostMapping("/saveinventrydata/{formType}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN', 'ROLE_FERTUSER', 'ROLE_MARKUSER', 'ROLE_SPAIUSER',"
			+ " 'ROLE_FERTADMIN', 'ROLE_MARKADMIN', 'ROLE_SPAIADMIN')")
	public ResponseEntity<String> saveInventryDataHandler(@PathVariable String formType, @RequestBody InventryData obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		try {
			switch (formType) {
			case "termsAndPrice": {
				return termsPriceService.saveTermsPriceMaster(obj.getTermsAndPriceData(), jwt);
			}
			case "openingStock": {
				return openingStockService.saveOpeningStock(obj.getOpeningStock(), jwt);
			}
			case "poRequest": {
				return porequestService.savePoRequest(obj.getPoRequestData(), jwt);
			}
			case "purchaseOrder": {
				return poService.savePurchaseOrder(obj.getPurchaseOrderData(), jwt);
			}
			case "grn": {
				return grnService.saveGrn(obj.getGrnData(), jwt);
			}
			case "gtn": {
				return gtnService.saveGtn(obj.getGtnData(), jwt, obj.getSalesReturnData());
			}
			case "despatchAdvice": {
				return despatchAdviceService.saveDespatchAdvice(obj.getDespatchAdviceData(), jwt);
			}
			case "dc": {
				return dcService.saveDc(obj.getDcData(), jwt);
			}
			case "invoice": {
				return invoiceService.saveInvoice(obj.getInvoiceData(), jwt);
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@GetMapping("/datafortpm")
	public DataForTPM dataForTPMHandler(@RequestHeader("Authorization") String jwt, @RequestParam String activity,
			String supplierName, String productName) throws Exception {
		return termsPriceService.dataForTPM(activity, jwt, supplierName, productName);
	}

	@GetMapping("/fetchdataforopeningstock")
	public DataForOpeningStock getDataForOpeningStockHandler(@RequestParam String activity, String productName,
			String officeName, @RequestHeader("Authorization") String jwt) throws Exception {
		return openingStockService.getDataForOpeningStock(activity, productName, jwt, officeName);
	}

	@GetMapping("/fetchdataforporequest")
	public DataForPoRequest getDataForPoRequestHandler(@RequestParam String officeName, String activity,
			String productName, String purchaseOrderType, String poNo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return porequestService.getDataForPoRequest(officeName, activity, jwt, productName, purchaseOrderType, poNo,
				date);
	}

	@GetMapping("/fetchdataforpo")
	public DataForPo getDataForPurchaseOrder(@RequestParam String activity, String productName, String termsMonth,
			String termsNo, String poBased, String officeName, String purchaseOrderType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return poService.getDataForPurchaseOrder(activity, productName, jwt, termsMonth, termsNo, poBased, officeName,
				purchaseOrderType, date);
	}

	@GetMapping("/fetchdataforgrn")
	public DataForGrn fetchDataForGrnHandler(@RequestParam String officeName, String godownType, String activity,
			String productName, String month, String poNo, String godownName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return grnService.fetchDataForGrn(officeName, godownType, activity, productName, poNo, jwt, godownName, date,
				month);
	}

	@GetMapping("/fetchdataforgtn")
	public DataForGtn fetchDataForGtnHandler(@RequestParam String officeName, String productName, String activity,
			String gtnFor, String month, String suppliedGodown, String invoiceNo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, String rrNo,
			String transactionFor, String godownName, String toRegion, String issuedGtnNo, String destination,
			String transportCharges, String loadingCharges, String unloadingCharges,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return gtnService.getDataForGtn(officeName, productName, activity, gtnFor, rrNo, date, transactionFor, jwt,
				godownName, toRegion, issuedGtnNo, destination, transportCharges, loadingCharges, unloadingCharges,
				month, suppliedGodown, invoiceNo);
	}

	@GetMapping("/fetchdatafordespatchadvice")
	public DataForDespatchAdvice getDataForDespatchAdviceHandler(@RequestParam String officeName, String activity,
			String ifmsId, String productName, String month, String godownName,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return despatchAdviceService.getDataForDespatchAdvice(officeName, activity, ifmsId, productName, jwt, month,
				godownName);
	}

	@GetMapping("/fetchdatafordc")
	public DataForDc getDataForDeliveryChellanHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			String ifmsId, String activity, String despatchAdviceNo, String productName, String godownName)
			throws Exception {
		return dcService.getDataForDeliveryChellan(officeName, jwt, ifmsId, activity, date, despatchAdviceNo,
				productName, godownName);
	}

	@GetMapping("/fetchdataforinvoice")
	public DataForInvoice getDataForInvoiceHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, String activity, String dcNo, String collectionMode, String month,
			String godownName) throws Exception {
		return invoiceService.getDataForInvoice(officeName, activity, dcNo, jwt, collectionMode, month, godownName);
	}

	@GetMapping("/fetchdataforgrnupdate")
	public DataForGrnUpdate getDataForGrnUpdateHandler(@RequestParam String officeName, String activity, String grnNo,
			String month) throws Exception {
		return grnService.getDataForGrnUpdate(officeName, activity, grnNo, month);
	}

	@GetMapping("/fetchdataforinvoiceupdate")
	public DataForInvoiceUpdate getDataForInvoiceUpdateHandler(@RequestParam String officeName, String activity,
			String selectedOption, String invoiceNo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate)
			throws Exception {
		return invoiceService.getDataForInvoiceUpdate(officeName, activity, selectedOption, fromDate, toDate,
				invoiceNo);
	}

	@PutMapping("/inventryedit/{formType}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROADMIN', 'ROLE_FERTADMIN', 'ROLE_MARKADMIN', 'ROLE_SPAIADMIN')")
	public ResponseEntity<String> putMethodName(@RequestHeader("Authorization") String jwt,
			@PathVariable String formType, @RequestBody InventryData obj) throws Exception {
		try {
			switch (formType) {
			case "termsAndPrice": {
				return termsPriceService.editTermsPriceMaster(obj.getTermsAndPriceData(), jwt);
			}
			case "poRequest": {
				return porequestService.editPoRequest(obj.getPoRequestData(), jwt);
			}
			case "purchaseOrder": {
				return poService.editPurchaseOrder(obj.getPurchaseOrderData(), jwt);
			}
			case "grn": {
				return grnService.editGrn(obj.getGrnData(), jwt);
			}
			case "gtn": {
				return gtnService.editGtn(obj.getGtnData(), jwt);
			}
			case "despatchAdvice": {
				return despatchAdviceService.editDespatchAdvice(obj.getDespatchAdviceData(), jwt);
			}
			case "dc": {
				return dcService.editDc(obj.getDcData(), jwt);
			}
			case "invoice": {
				return invoiceService.editInvoice(obj.getInvoiceData(), jwt);
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private InventryVouchersApprovalService inventryVouchersApprovalService;

	@PutMapping("/updateinventryapproval")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROADMIN', 'ROLE_FERTADMIN', 'ROLE_MARKADMIN', 'ROLE_SPAIADMIN')")
	public ResponseEntity<String> inventryVoucherApprovalHandler(@RequestBody VoucherApproval obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		String updatedStatus = inventryVouchersApprovalService.updateVoucherApproval(obj, jwt);
		return new ResponseEntity<String>(updatedStatus, HttpStatus.ACCEPTED);
	}

	@PutMapping("/updategrnfordc/{despatchAdviceNo}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> updateGrnQtyForDcHandler(@RequestBody List<GrnQtyUpdateForDc> obj,
			@PathVariable String despatchAdviceNo) throws Exception {
		despatchAdviceService.updateDespatchAdviceQty(despatchAdviceNo,
				obj.stream()
						.map(item -> new DcTableData(null, null, item.getOutwardBatchNo(), null, null,
								item.getProductName(), null, null, item.getQty(), null, null, null, null, null, null))
						.collect(Collectors.toList()));
		return grnService.updateGrnQtyForDc(obj);
	}

	@PutMapping("/updategrn")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> updateGrnHandler(@RequestBody GRN obj) throws Exception {
		return grnService.updateGrn(obj);
	}

	@PutMapping("/updateinvoice")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> updateInvoiceHandler(@RequestBody List<InvoiceUpdate> obj) throws Exception {
		return invoiceService.updateInvoice(obj);
	}

	@PutMapping("/updatepoissueinporequest/{productName}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_FERTUSER', 'ROLE_MARKUSER', 'ROLE_SPAIUSER',"
			+ " 'ROLE_FERTADMIN', 'ROLE_MARKADMIN', 'ROLE_SPAIADMIN')")
	public ResponseEntity<String> updatePoQtyinPoreqHandler(@PathVariable String productName,
			@RequestBody List<PoTableData> obj) throws Exception {
		return porequestService.updatePoIssueQty(obj, productName);
	}

	@PutMapping("/updatedespatchstatus/{id}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROADMIN')")
	public void updateDespatchAdviceStatusHandler(@PathVariable Long id) throws Exception {
		despatchAdviceService.updateDespatchAdviceStatus(id);
	}

	@PutMapping("/updatewagon/{grnNo}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROADMIN')")
	public ResponseEntity<String> updateWagonDataHandler(@PathVariable String grnNo, @RequestBody WagonDataGrn obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return grnService.updateWagonData(obj, grnNo, jwt);
	}

	@PutMapping("/updatejvgrn/{grnNo}/{jv}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public void updateJvHandler(@PathVariable String grnNo, @RequestHeader("Authorization") String jwt,
			@PathVariable String jv) throws Exception {
		grnService.updateJv(grnNo, jv);
	}

	@PutMapping("/revertjvingrn/{grnNo}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public void revertJvHandler(@PathVariable String grnNo, @RequestHeader("Authorization") String jwt)
			throws Exception {
		grnService.revertGrnJv(null, jwt, grnNo);
	}

	@PutMapping("/updatenoncc/{invoiceNo}")
	public ResponseEntity<String> updateAdjReceiptNoInNonCcInvoiceHandler(@PathVariable String invoiceNo,
			@RequestParam String voucherNo, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date)
			throws Exception {
		logger.info("voucherNo{}", voucherNo);
		return invoiceService.updateAdjReceiptNoInNonCcInvoice(invoiceNo, voucherNo, date);
	}

	@PutMapping("/updatecombinedload")
	public ResponseEntity<String> updateCombinedLoadHandler(@RequestBody List<CompondLoadDcData> obj) throws Exception {
		return dcService.updateCombinedLoad(obj);
	}

	@PutMapping("/revertnonccinvoice")
	public void revertNonCCInvoiceHandler(@RequestParam AdjustmentReceiptVoucher adjv,
			@RequestHeader("Authorization") String jwt) throws Exception {
		invoiceService.revertNonCCInvoice(null, jwt, adjv);
	}

	@PutMapping("/approvenonccinvoice")
	public void approveNonCCInvoiceHandler(@RequestParam String invoiceNo) throws Exception {
		invoiceService.approveNonCCInvoice(invoiceNo);
	}

	@PutMapping("/updatejvforsalesreturn/{gtnNo}")
	public ResponseEntity<String> updateJvForSalesReturnHandler(@PathVariable String gtnNo,
			@RequestBody JournalVoucher jv, @RequestHeader("Authorization") String jwt) throws Exception {
		return gtnService.updateJvForSalesReturn(gtnNo, jv, jwt);
	}

	@Autowired
	private FilterInventryDataService filterInventryDataService;

	@GetMapping("/fetchinventryfilterdata/{formType}")
	public InventryData filterInventryDataHandler(@PathVariable String formType, @RequestParam String officeName,
			@RequestParam(required = false) String gtnFor, @RequestParam String voucherStatus,
			@RequestParam(required = false) String financialMonth, @RequestParam(required = false) String activity,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return filterInventryDataService.filterInventryData(formType, fromDate, toDate, officeName, activity,
				voucherStatus, financialMonth, jwt, gtnFor);
	}

	@GetMapping("/fetchinvoicebyoffice")
	public List<Invoice> getInvoiceDataByOfficenameHandler(@RequestParam String officeName) throws Exception {
		return invoiceService.getInvoiceDataByOffficeName(officeName);
	}

	@GetMapping("/fetchterms")
	public List<TermsPrice> getTermsDataHandler() throws Exception {
		return termsPriceService.getTermsPriceMasterData();
	}

	@GetMapping("/fetchtermsbytermsno")
	public TermsPrice getTermsDataByTermsNoHandler(String termsNo) throws Exception {
		return termsPriceService.fetchTermsByTermsNo(termsNo);
	}

	@GetMapping("/fetchinvoicedatafordncn")
	public InvoiceDataForDnCn getDataForDnCnHandler(@RequestParam String invoiceNo) throws Exception {
		return invoiceService.getDataForDnCn(invoiceNo);
	}

	@Autowired
	private RegisterService registerService;

	@Autowired
	private MasterService masterService;

	@GetMapping("/fetchinventryregisterdata/{formType}")
	public RegisterData getInventryRegisterData(@PathVariable String formType, @RequestParam String officeName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestHeader("Authorization") String jwt, @RequestParam String poNo, @RequestParam String supplierName,
			@RequestParam String godownName, @RequestParam String productName, @RequestParam String month)
			throws Exception {
		RegisterData data = new RegisterData();

		data.setGodownNameList(grnService.getGodownNameList(jwt, officeName));
		data.setSupplierNameList(masterService.getProductDataHandler(jwt).stream()
				.filter(item -> !item.getSupplierName().startsWith("TANFED")).map(ProductMaster::getSupplierName)
				.collect(Collectors.toSet()));
		data.setProductNameList(masterService.getProductDataHandler(jwt).stream()
				.filter(item -> (item.getSupplierName().equals(supplierName) || supplierName.isEmpty()))
				.map(ProductMaster::getProductName).collect(Collectors.toSet()));
		if (!month.isEmpty() || (fromDate != null && toDate != null)) {
			switch (formType) {
			case "receiptRegister": {
				data.setReceiptRegister(registerService.getReceiptRegisterData(officeName, month, godownName, fromDate,
						toDate, productName));
				return data;
			}
			case "salesRegister": {
				data.setSalesRegister(registerService.getSalesRegisterData(officeName, month, godownName, fromDate,
						toDate, productName));
				return data;
			}
			case "stockTransferIssueRegister": {
				data.setStockTransferIssueRegister(registerService.getStockTransferIssueData(officeName, month,
						godownName, fromDate, toDate, productName));
				return data;
			}
			case "stockTransferReceiptRegister": {
				data.setStockTransferReceiptRegister(registerService.getStockTransferReceiptData(officeName, month,
						godownName, fromDate, toDate, productName));
				return data;
			}
			case "stockRegister": {
				data.setStockRegister(registerService.getStockRegisterData(officeName, fromDate, toDate, productName,
						godownName, month));
				return data;
			}
			case "poRegister": {
				data.setPoRegister(registerService.getPoRegisterData(officeName, month, fromDate, supplierName, toDate,
						productName));
				return data;
			}
			case "poAllotmentRegister": {
				data.setPoNoList(poService.getPoData().stream().filter(item -> {
					Boolean monthFilter;
					if (month.isEmpty()) {
						monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
					} else {
						monthFilter = String
								.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
								.equals(month);
					}
					return item.getVoucherStatus().equals("Approved") && monthFilter && item.getTableData().stream()
							.map(PoTableData::getRegion).collect(Collectors.toList()).contains(officeName);
				}).map(item -> item.getPoNo()).collect(Collectors.toList()));
				data.setPoAllotmentRegister(
						registerService.getPoAllotmentRegisterData(officeName, month, fromDate, toDate, poNo));
				return data;
			}
			case "despatchAdviceRegister": {
				data.setDespatchAdviceRegister(
						registerService.getDespatchAdviceRegisterData(officeName, month, fromDate, toDate));
				return data;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} else {
			return data;
		}

	}

}
