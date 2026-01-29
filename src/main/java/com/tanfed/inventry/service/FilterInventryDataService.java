package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.dto.CheckMemoGoodsDto;
import com.tanfed.inventry.dto.GtnDTO;
import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.*;
import com.tanfed.inventry.response.SobData;

@Service
public class FilterInventryDataService {

	private static Logger logger = LoggerFactory.getLogger(FilterInventryDataService.class);

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private TermsPriceRepo termsPriceRepo;

	@Autowired
	private OpeningStockService openingStockService;

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Autowired
	private PorequestService porequestService;

	@Autowired
	private PoRequestRepo poRequestRepo;

	@Autowired
	private PoService poService;

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	@Autowired
	private SalesReturnRepo salesReturnRepo;

	@Autowired
	private GrnService grnService;

	@Autowired
	private GrnRepo grnRepo;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private GtnRepo gtnRepo;

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	@Autowired
	private DespatchAdviceRepo despatchAdviceRepo;

	@Autowired
	private DcService dcService;

	@Autowired
	private DeliveryChellanRepo deliveryChellanRepo;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private MasterService masterService;

	public InventryData filterInventryData(String formType, LocalDate fromDate, LocalDate toDate, String officeName,
			String activity, String voucherStatus, String financialMonth, String jwt, String gtnFor) throws Exception {
		try {
			InventryData data = new InventryData();
			switch (formType) {
			case "termsAndPrice": {
				List<TermsPrice> termsPriceList = new ArrayList<TermsPrice>();
				List<TermsPrice> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = termsPriceService.getTermsPriceMasterData().stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getMasterData().getDate().isBefore(fromDate)
									&& !item.getMasterData().getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						termsPriceList.addAll(termsPriceRepo.findPendingData());
					} else if (fromDate != null && toDate != null) {
						termsPriceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						termsPriceList.addAll(termsPriceRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						termsPriceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						termsPriceList.addAll(termsPriceRepo.findPendingData());
						termsPriceList.addAll(termsPriceRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						termsPriceList.addAll(filteredLst);
					}
				}
				termsPriceList.sort(Comparator.comparing(TermsPrice::getId).reversed());
				data.setTermsAndPrice(termsPriceList);
				return data;
			}
			case "openingStock": {
				List<OpeningStock> openingStockList = new ArrayList<OpeningStock>();
				List<OpeningStock> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = openingStockService.getOpeningStockByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getAsOn().isBefore(fromDate) && !item.getAsOn().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						openingStockList.addAll(openingStockRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						openingStockList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						openingStockList.addAll(openingStockRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						openingStockList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						openingStockList.addAll(openingStockRepo.findPendingDataByOfficeName(officeName));
						openingStockList.addAll(openingStockRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						openingStockList.addAll(filteredLst);
					}
				}
				openingStockList.sort(Comparator.comparing(OpeningStock::getId).reversed());
				data.setOpeningStock(openingStockList);
				return data;
			}
			case "poRequest": {
				logger.info(officeName);
				List<PoRequest> poRequestList = new ArrayList<PoRequest>();
				List<PoRequest> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = porequestService.getPoRequestDataByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						poRequestList.addAll(poRequestRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						poRequestList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						poRequestList.addAll(poRequestRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						poRequestList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						poRequestList.addAll(poRequestRepo.findPendingDataByOfficeName(officeName));
						poRequestList.addAll(poRequestRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						poRequestList.addAll(filteredLst);
					}
				}
				poRequestList.sort(Comparator.comparing(PoRequest::getId).reversed());
				data.setPoRequest(poRequestList);
				return data;
			}
			case "purchaseOrder": {
				List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
				List<PurchaseOrder> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = poService.getPoData().stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(purchaseOrderRepo.findPendingData());
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(purchaseOrderRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(purchaseOrderRepo.findPendingData());
						purchaseOrderList.addAll(purchaseOrderRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				purchaseOrderList.sort(Comparator.comparing(PurchaseOrder::getId).reversed());
				data.setPurchaseOrder(purchaseOrderList);
				return data;
			}
			case "grn": {
				List<GRN> grnList = new ArrayList<GRN>();
				List<GRN> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = grnService.getGrnDataByOffficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						grnList.addAll(grnRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						grnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						grnList.addAll(grnRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						grnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						grnList.addAll(grnRepo.findPendingDataByOfficeName(officeName));
						grnList.addAll(grnRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						grnList.addAll(filteredLst);
					}
				}
				grnList.sort(Comparator.comparing(GRN::getId).reversed());
				data.setGrn(grnList);
				return data;
			}
			case "gtn": {
				List<GTN> gtnList = new ArrayList<GTN>();
				List<GTN> filteredLst = null;
				logger.info("gtnFor {}", gtnFor);
				if (fromDate != null && toDate != null) {
					filteredLst = gtnService.getGtnDataByOffficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getTransactionFor().equals("Sales Return")
									&& (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(item.getGtnFor()))
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(gtnRepo.findPendingDataByOfficeName(officeName).stream()
								.filter(item -> (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(item.getGtnFor()))
										&& !item.getTransactionFor().equals("Sales Return"))
								.collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(gtnRepo.findApprovedDataByOfficeName(officeName).stream()
								.filter(item -> (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(item.getGtnFor()))
										&& !item.getTransactionFor().equals("Sales Return"))
								.collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(gtnRepo.findPendingDataByOfficeName(officeName).stream()
								.filter(item -> (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(item.getGtnFor()))
										&& !item.getTransactionFor().equals("Sales Return"))
								.collect(Collectors.toList()));
						gtnList.addAll(gtnRepo.findApprovedDataByOfficeName(officeName).stream()
								.filter(item -> (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(item.getGtnFor()))
										&& !item.getTransactionFor().equals("Sales Return"))
								.collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				gtnList.sort(Comparator.comparing(GTN::getId).reversed());
				data.setGtn(gtnList);
				return data;
			}
			case "salesReturn": {
				List<GtnDTO> gtnList = new ArrayList<GtnDTO>();
				List<GtnDTO> filteredLst = null;
				logger.info("gtnFor {}", gtnFor);
				if (fromDate != null && toDate != null) {
					filteredLst = salesReturnRepo.findByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(salesReturnRepo.findPendingDataByOfficeName(officeName).stream()
								.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(salesReturnRepo.findApprovedDataByOfficeName(officeName).stream()
								.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						gtnList.addAll(salesReturnRepo.findPendingDataByOfficeName(officeName).stream()
								.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList()));
						gtnList.addAll(salesReturnRepo.findApprovedDataByOfficeName(officeName).stream()
								.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						gtnList.addAll(filteredLst);
					}
				}
				gtnList.sort(Comparator.comparing(GtnDTO::getId).reversed());
				data.setSalesReturn(gtnList);
				return data;
			}
			case "despatchAdvice": {
				List<DespatchAdvice> despatchAdviceList = new ArrayList<DespatchAdvice>();
				List<DespatchAdvice> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = despatchAdviceService.getDespatchAdviceDataByOffficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						despatchAdviceList.addAll(despatchAdviceRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						despatchAdviceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						despatchAdviceList.addAll(despatchAdviceRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						despatchAdviceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						despatchAdviceList.addAll(despatchAdviceRepo.findPendingDataByOfficeName(officeName));
						despatchAdviceList.addAll(despatchAdviceRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						despatchAdviceList.addAll(filteredLst);
					}
				}
				despatchAdviceList.sort(Comparator.comparing(DespatchAdvice::getId).reversed());
				data.setDespatchAdvice(despatchAdviceList);
				return data;
			}
			case "dc": {
				List<DeliveryChellan> dcList = new ArrayList<DeliveryChellan>();
				List<DeliveryChellan> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						dcList.addAll(deliveryChellanRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						dcList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						dcList.addAll(deliveryChellanRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						dcList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						dcList.addAll(deliveryChellanRepo.findPendingDataByOfficeName(officeName));
						dcList.addAll(deliveryChellanRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						dcList.addAll(filteredLst);
					}
				}
				dcList.sort(Comparator.comparing(DeliveryChellan::getId).reversed());
				data.setDc(dcList);
				return data;
			}
			case "invoice": {
				List<Invoice> invoiceList = new ArrayList<Invoice>();
				List<Invoice> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = invoiceService.getInvoiceDataByOffficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(invoiceRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(invoiceRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(invoiceRepo.findPendingDataByOfficeName(officeName));
						invoiceList.addAll(invoiceRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				invoiceList.sort(Comparator.comparing(Invoice::getId).reversed());
				data.setInvoice(invoiceList);
				return data;
			}
			case "priceCircular": {
				data.setPriceCircular(
						termsPriceRepo.findAll().stream().filter(item -> item.getVoucherStatus().equals("Approved")
								&& item.getMasterData().getActivity().equals(activity)).filter(item -> {
									String monthValue = String.format("%s %04d",
											item.getMasterData().getDate().getMonth(),
											item.getMasterData().getDate().getYear());
									return monthValue.equals(financialMonth);
								}).collect(Collectors.toList()));
				return data;
			}
			case "purchaseOrderRO": {
				List<PurchaseOrder> collect = purchaseOrderRepo.findAll().stream().filter(
						item -> item.getVoucherStatus().equals("Approved") && item.getActivity().equals(activity))
						.filter(item -> {
							String monthValue = String.format("%s %04d", item.getDate().getMonth(),
									item.getDate().getYear());
							logger.info("month{}", monthValue);
							logger.info("month1{}", financialMonth);
							return monthValue.equals(financialMonth);
						}).collect(Collectors.toList());
				List<PurchaseOrder> toRemovePo = new ArrayList<PurchaseOrder>();
				List<PoTableData> toRemove = new ArrayList<PoTableData>();
				collect.forEach(item -> {
					item.getTableData().forEach(temp -> {
						if (!temp.getRegion().equals(officeName)) {
							toRemove.add(temp);
						}
					});
					item.getTableData().removeAll(toRemove);
					if (item.getTableData().isEmpty()) {
						toRemovePo.add(item);
					}
				});
				collect.removeAll(toRemovePo);
				collect.sort(Comparator.comparing(PurchaseOrder::getId).reversed());
				data.setPurchaseOrderRO(collect);
				return data;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PurchaseBookingService purchaseBookingService;

	@Autowired
	private PurchaseBookingRepo purchaseBookingRepo;

	@Autowired
	private CheckMemoGoodsService checkMemoGoodsService;

	@Autowired
	private CheckMemoGoodsRepo checkMemoGoodsRepo;

	@Autowired
	private MpaService mpaService;

	@Autowired
	private MpaBillEntryRepo mpaBillEntryRepo;

	@Autowired
	private MpaCheckMemoRepo mpaCheckMemoRepo;

	@Autowired
	private TcService tcService;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	@Autowired
	private TcCheckMemoRepo tcCheckMemoRepo;

	@Autowired
	private SupplierInvoiceService supplierInvoiceService;

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	public SobData filterSobData(String formType, LocalDate fromDate, LocalDate toDate, String officeName,
			String voucherStatus, String jwt) throws Exception {
		try {
			SobData data = new SobData();
			switch (formType) {
			case "supplierInvoice": {
				List<SupplierInvoiceDetails> supplierInvoiceDetailsList = new ArrayList<SupplierInvoiceDetails>();
				List<SupplierInvoiceDetails> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = supplierInvoiceService.getSupplierInvoiceDetails().stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						supplierInvoiceDetailsList.addAll(supplierInvoiceDetailsRepo.findPendingData());
					} else if (fromDate != null && toDate != null) {
						supplierInvoiceDetailsList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						supplierInvoiceDetailsList.addAll(supplierInvoiceDetailsRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						supplierInvoiceDetailsList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						supplierInvoiceDetailsList.addAll(supplierInvoiceDetailsRepo.findPendingData());
						supplierInvoiceDetailsList.addAll(supplierInvoiceDetailsRepo.findApprovedData());
					} else if (fromDate != null && toDate != null) {
						supplierInvoiceDetailsList.addAll(filteredLst);
					}
				}
				data.setSupplierInvoice(supplierInvoiceDetailsList);
				return data;
			}
			case "grnAttach": {
				List<PurchaseOrder> purchaseOrderList = new ArrayList<PurchaseOrder>();
				List<PurchaseOrder> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = poService.getPoData().stream().filter(
							item -> (item.getSobVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getGrnData().isEmpty()
									&& item.getGrnData().stream()
											.anyMatch(itemData -> itemData.getSupplierInvoiceNo() != null)
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				} else {
//					List<PurchaseOrder> pendingList = purchaseOrderRepo.findSobPendingData();
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(filterPo(purchaseOrderRepo.findSobPendingData()));
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(filterPo(purchaseOrderRepo.findSobApprovedData()));
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						purchaseOrderList.addAll(filterPo(purchaseOrderRepo.findSobPendingData()));
						purchaseOrderList.addAll(filterPo(purchaseOrderRepo.findSobApprovedData()));
					} else if (fromDate != null && toDate != null) {
						purchaseOrderList.addAll(filteredLst);
					}
				}
				purchaseOrderList.sort(Comparator.comparing(PurchaseOrder::getId).reversed());
				data.setGrnAttach(purchaseOrderList);
				return data;
			}
			case "purchaseBooking": {
				List<PurchaseBookingDto> purchaseBookingList = new ArrayList<PurchaseBookingDto>();
				List<PurchaseBookingDto> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = purchaseBookingService.findPurchaseBookedDataByActivity(null).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.map(item -> {
								try {
									return mapPbDataToDto(item, jwt);
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
							}).collect(Collectors.toList());
				}
				List<PurchaseBookingDto> pendingData = purchaseBookingRepo.findPendingData().stream().map(item -> {
					try {
						return mapPbDataToDto(item, jwt);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}).collect(Collectors.toList());

				List<PurchaseBookingDto> approvedData = purchaseBookingRepo.findApprovedData().stream().map(item -> {
					try {
						return mapPbDataToDto(item, jwt);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}).collect(Collectors.toList());

				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						purchaseBookingList.addAll(pendingData);
					} else if (fromDate != null && toDate != null) {
						purchaseBookingList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						purchaseBookingList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						purchaseBookingList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						purchaseBookingList.addAll(pendingData);
						purchaseBookingList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						purchaseBookingList.addAll(filteredLst);
					}
				}
				purchaseBookingList.sort(Comparator.comparing(PurchaseBookingDto::getId).reversed());
				data.setPurchaseBooking(purchaseBookingList);
				return data;
			}
			case "checkMemoGoods": {
				List<CheckMemoGoodsDto> checkMemoGoodsList = new ArrayList<CheckMemoGoodsDto>();
				List<CheckMemoGoodsDto> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = checkMemoGoodsService.getCheckMemoData().stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getCmDate().isBefore(fromDate) && !item.getCmDate().isAfter(toDate))
							.map(item -> {
								try {
									return mapCmgDataToDto(item, jwt);
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
							}).collect(Collectors.toList());
				}
				List<CheckMemoGoodsDto> pendingData = checkMemoGoodsRepo.findPendingData().stream().map(item -> {
					try {
						return mapCmgDataToDto(item, jwt);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}).collect(Collectors.toList());

				List<CheckMemoGoodsDto> approvedData = checkMemoGoodsRepo.findApprovedData().stream().map(item -> {
					try {
						return mapCmgDataToDto(item, jwt);
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}
				}).collect(Collectors.toList());
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						checkMemoGoodsList.addAll(pendingData);
					} else if (fromDate != null && toDate != null) {
						checkMemoGoodsList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						checkMemoGoodsList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						checkMemoGoodsList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						checkMemoGoodsList.addAll(pendingData);
						checkMemoGoodsList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						checkMemoGoodsList.addAll(filteredLst);
					}
				}
				checkMemoGoodsList.sort(Comparator.comparing(CheckMemoGoodsDto::getId).reversed());
				data.setCheckMemoGoods(checkMemoGoodsList);
				return data;
			}

			case "tcBillEntry": {
				List<TcBillEntry> invoiceList = new ArrayList<TcBillEntry>();
				List<TcBillEntry> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = tcService.fetchTcBillEntryByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(tcBillEntryRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(tcBillEntryRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(tcBillEntryRepo.findPendingDataByOfficeName(officeName));
						invoiceList.addAll(tcBillEntryRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				invoiceList.sort(Comparator.comparing(TcBillEntry::getId).reversed());
				data.setTcBillEntry(invoiceList);
				return data;
			}

			case "tcCheckMemo": {
				List<TcCheckMemoDto> invoiceList = new ArrayList<TcCheckMemoDto>();
				List<TcCheckMemoDto> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = tcService.fetchTcCheckMemoByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.map(item -> {
								try {
									return mapTcDataToDto(item, jwt);
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
							}).collect(Collectors.toList());
				}
				List<TcCheckMemoDto> pendingData = tcCheckMemoRepo.findPendingDataByOfficeName(officeName).stream()
						.map(item -> {
							try {
								return mapTcDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
				List<TcCheckMemoDto> approvedData = tcCheckMemoRepo.findApprovedDataByOfficeName(officeName).stream()
						.map(item -> {
							try {
								return mapTcDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(pendingData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(pendingData);
						invoiceList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				invoiceList.sort(Comparator.comparing(TcCheckMemoDto::getId).reversed());
				data.setTcCheckMemo(invoiceList);
				return data;
			}

			case "mpaBillEntry": {
				List<MpaBillEntry> invoiceList = new ArrayList<MpaBillEntry>();
				List<MpaBillEntry> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = mpaService.getMpaBillEntryByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.collect(Collectors.toList());
				}
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(mpaBillEntryRepo.findPendingDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(mpaBillEntryRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(mpaBillEntryRepo.findPendingDataByOfficeName(officeName));
						invoiceList.addAll(mpaBillEntryRepo.findApprovedDataByOfficeName(officeName));
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				invoiceList.sort(Comparator.comparing(MpaBillEntry::getId).reversed());
				data.setMpaBillEntry(invoiceList);
				return data;
			}

			case "mpaCheckMemo": {
				List<MpaCheckMemoDto> invoiceList = new ArrayList<MpaCheckMemoDto>();
				List<MpaCheckMemoDto> filteredLst = null;
				if (fromDate != null && toDate != null) {
					filteredLst = mpaService.getMpaCheckMemoByOfficeName(officeName).stream()
							.filter(item -> (item.getVoucherStatus().equals(voucherStatus) || voucherStatus.isEmpty())
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate))
							.map(item -> {
								try {
									return mapMpaDataToDto(item, jwt);
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
							}).collect(Collectors.toList());
				}
				List<MpaCheckMemoDto> pendingData = mpaCheckMemoRepo.findPendingDataByOfficeName(officeName).stream()
						.map(item -> {
							try {
								return mapMpaDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());

				List<MpaCheckMemoDto> approvedData = mpaCheckMemoRepo.findApprovedDataByOfficeName(officeName).stream()
						.map(item -> {
							try {
								return mapMpaDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());

				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(pendingData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceList.addAll(pendingData);
						invoiceList.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						invoiceList.addAll(filteredLst);
					}
				}
				invoiceList.sort(Comparator.comparing(MpaCheckMemoDto::getId).reversed());
				data.setMpaCheckMemo(invoiceList);
				return data;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private GtnDTO mapSalesReturnToDto(SalesReturn salesReturn, String jwt) {
		try {
			JournalVoucher jv;
			if (salesReturn.getJvNo() != null) {
				Vouchers vouchers;
				vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", salesReturn.getJvNo(),
						jwt);
				jv = vouchers.getJournalVoucherData();
			} else {
				jv = null;
			}
			return new GtnDTO(salesReturn.getId(), salesReturn.getCreatedAt(), salesReturn.getOfficeName(),
					salesReturn.getVoucherStatus(), salesReturn.getDesignation(), salesReturn.getEmpId(),
					salesReturn.getApprovedDate(), salesReturn.getDate(), salesReturn.getGtnNo(),
					salesReturn.getActivity(), salesReturn.getMonth(), salesReturn.getSuppliedGodown(),
					salesReturn.getGodownName(), jv, salesReturn.getInvoiceNo(), salesReturn.getInvoice(),
					salesReturn.getInvoiceTableData(), salesReturn.getBillEntry());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private PurchaseBookingDto mapPbDataToDto(PurchaseBooking item, String jwt) throws Exception {
		List<JournalVoucher> jvData = new ArrayList<JournalVoucher>();
		item.getJvList().forEach(jvNo -> {
			try {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
				jvData.add(jv.getJournalVoucherData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return new PurchaseBookingDto(item.getId(), item.getDate(), item.getActivity(), item.getDesignation(),
				item.getVoucherStatus(), item.getProductCategory(), item.getSupplierName(), item.getProductName(),
				item.getPoType(), item.getPoMonth(), item.getPoNo(), item.getProductGroup(), item.getStandardUnits(),
				item.getPacking(), item.getTermsNo(), item.getTotalPoQty(), item.getGrnTableData(), item.getTotalQty(),
				item.getDirectQty(), item.getBufferQty(), item.getInputTax(), item.getMargin(), item.getDeduction(),
				item.getNet(), item.getTradeIncome(), item.getTermsData(), item.getTermsDataGeneral(),
				item.getTermsDataDirect(), item.getTermsDataBuffer(), jvData);
	}

	private CheckMemoGoodsDto mapCmgDataToDto(CheckMemoGoods item, String jwt) throws Exception {
		List<JournalVoucher> jvData = new ArrayList<JournalVoucher>();
		item.getJvNoList().forEach(jvNo -> {
			try {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
				jvData.add(jv.getJournalVoucherData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		Vouchers pv = null;
		if (item.getPvNo() != null) {
			pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", item.getPvNo(), jwt);
		}
		if (pv == null) {
			throw new Exception("No pv found");
		}
		return new CheckMemoGoodsDto(item.getActivity(), item.getCheckMemoNo(), item.getCmDate(), item.getDesignation(),
				item.getVoucherStatus(), item.getProductCategory(), item.getProductGroup(), item.getProductName(),
				item.getStandardUnits(), item.getPacking(), item.getHsnCode(), item.getSupplierName(),
				item.getSupplierAccountNo(), item.getSupplierGst(), item.getId(), item.getPoType(), item.getPoMonth(),
				item.getPoNo(), item.getPoDate(), item.getTotalPoQty(), item.getTermsNo(), item.getAdvOutstanding(),
				item.getGstRate(), item.getGstData(), item.getTotalGrnQty(), item.getTotalSupplierInvQty(),
				item.getJvQty(), item.getPurchaseJvNo(), item.getAdvanceAdjOptions(), item.getSupplierAdvanceNo(),
				item.getCurrentAdvanceQty(), item.getCalulatedBasicPrice(), item.getCalculatedTcsTdsValue(),
				item.getCalculatedTotal(), item.getCreditNoteAdjOptions(), item.getCreditNoteAdjAmount(),
				item.getCreditNoteAdjCnNo(), item.getCreditNoteCnDate(), item.getTermsData(),
				item.getTermsDataGeneral(), item.getTermsDataDirect(), item.getTermsDataBuffer(),
				item.getTotalPaymentValue(), item.getNetPaymentValue(), item.getRate(), item.getPercentageValue(),
				item.getNetPaymentAfterAdjustment(), item.getDifference(), item.getRemarks(), jvData,
				item.getPvNo() != null ? pv.getPaymentVoucherData() : null);
	}

	private TcCheckMemoDto mapTcDataToDto(TcCheckMemo item, String jwt) throws Exception {
		List<JournalVoucher> jvData = new ArrayList<JournalVoucher>();
		item.getJvNo().forEach(jvNo -> {
			try {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
				jvData.add(jv.getJournalVoucherData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		PaymentVoucher pv = item.getPvNo() == null ? null : fetchPv(item.getPvNo(), jwt);
		return new TcCheckMemoDto(item.getDesignation(), item.getVoucherStatus(), item.getId(), item.getOfficeName(),
				item.getCheckMemoNo(), item.getFinancialYear(), item.getFinancialMonth(), item.getContractFirm(),
				item.getClaimBillNo(), item.getClaimBillDate(), item.getTotalBillValue(), item.getGstReturnType(),
				item.getGstNo(), item.getDate(), item.getTotalChargesValue(), item.getTotalCGST(), item.getTotalSGST(),
				item.getTotalPaymentValue(), item.getTotalRecoveryValue(), item.getRecoveryIfAny(),
				item.getNetPaymentAfterAdjustment(), item.getTcsOrTds(), item.getRate(), item.getPercentageValue(),
				item.getNetPaymentAfterTdsTcs(), item.getRemarks(), jvData, item.getChargesData(),
				mapchargesAndGstData(item, jwt), pv, item.getRecoveryData());
	}

	private PaymentVoucher fetchPv(String pvNo, String jwt) {
		try {
			Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", pvNo, jwt);
			return pv.getPaymentVoucherData();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private List<TcCheckMemoGstData> mapchargesAndGstData(TcCheckMemo item, String jwt) {
		try {
			ContractorInfo contractorInfo = masterService.getContarctorInfoByOfficeName(jwt, item.getOfficeName())
					.stream().filter(i -> i.getContractFirm().equals(item.getContractFirm()))
					.collect(Collectors.toList()).get(0);
			List<ContractorGstData> gstData = contractorInfo.getGstData();
			List<TcCheckMemoChargesTable> chargesData = item.getChargesData();
			return chargesData.stream().map(i -> {
				ContractorGstData contractorGstData = gstData.stream()
						.filter(g -> g.getGstRateFor().equals(i.getHeadName())).collect(Collectors.toList()).get(0);
				return new TcCheckMemoGstData(i.getHeadName(), contractorGstData.getGstCategory(),
						contractorGstData.getGstRate(), contractorGstData.getSgstRate(),
						contractorGstData.getCgstRate(), contractorGstData.getIgstRate(),
						((i.getValue() / 100) * contractorGstData.getCgstRate()),
						((i.getValue() / 100) * contractorGstData.getSgstRate()));
			}).collect(Collectors.toList());

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private MpaCheckMemoDto mapMpaDataToDto(MpaCheckMemo item, String jwt) throws Exception {
		PaymentVoucher pv = null;
		if (item.getPvNo() != null) {
			Vouchers pvData = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", item.getPvNo(),
					jwt);
			pv = pvData.getPaymentVoucherData();
		}
		JournalVoucher jv = null;
		if (item.getJvNo() != null) {
			Vouchers jvData = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", item.getJvNo(),
					jwt);
			jv = jvData.getJournalVoucherData();
		}

		return new MpaCheckMemoDto(item.getId(), item.getDate(), item.getCheckMemoNo(), item.getOfficeName(), null,
				item.getTotalCalculatedValue(), item.getTotalSgstValue(), item.getTotalCgstValue(),
				item.getTotalPaymentValue(), item.getRecoveryIfAny(), item.getNetTotalDeduction(), item.getTcsOrTds(),
				item.getRate(), item.getCalculatedTcsTdsValue(), item.getNetPaymentAfterAdjustment(),
				item.getDifference(), item.getRemarks(), item.getJvNo() != null ? jv : null,
				item.getPvNo() != null ? pv : null, item.getFinancialYear(), item.getFinancialMonth(),
				item.getContractFirm(), item.getClaimBillNo(), item.getClaimBillDate(), item.getTotalBillValue(),
				item.getDesignation(), item.getVoucherStatus());
	}

	private List<PurchaseOrder> filterPo(List<PurchaseOrder> list) {
		return list.stream()
				.filter(item -> !item.getGrnData().isEmpty()
						&& item.getGrnData().stream().anyMatch(itemData -> itemData.getSupplierInvoiceNo() != null))
				.collect(Collectors.toList());
	}
}
