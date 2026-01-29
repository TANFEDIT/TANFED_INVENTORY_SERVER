package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.model.WagonDataGrn;
import com.tanfed.inventry.repository.*;

@Service
public class InventryVouchersApprovalService {

	private Logger logger = LoggerFactory.getLogger(InventryVouchersApprovalService.class);

	@Autowired
	private TermsPriceRepo termsPriceRepo;

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Autowired
	private PoRequestRepo poRequestRepo;

	@Autowired
	private PorequestService porequestService;;

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	@Autowired
	private GrnRepo grnRepo;

	@Autowired
	private GrnService grnService;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private DcService dcService;

	@Autowired
	private GtnRepo gtnRepo;

	@Autowired
	private DespatchAdviceRepo despatchAdviceRepo;

	@Autowired
	private SalesReturnRepo salesReturnRepo;

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	@Autowired
	private DeliveryChellanRepo deliveryChellanRepo;

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private UserService userService;

	@Autowired
	private ClosingStockTableRepo closingStockTableRepo;

	public String updateVoucherApproval(VoucherApproval obj, String jwt) throws Exception {
		try {
			String designation = null;
			List<String> oldDesignation = null;

			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			logger.info("{}", obj);
			switch (obj.getFormType()) {
			case "termsAndPrice": {
				TermsPrice termsPrice = termsPriceRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = termsPrice.getDesignation();

				termsPrice.setVoucherStatus(obj.getVoucherStatus());
				termsPrice.getEmpId().add(empId);
				if (obj.getVoucherStatus().equals("Approved")) {
					termsPrice.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					termsPrice.setDesignation(Arrays.asList(designation));
				} else {
					termsPrice.getDesignation().add(designation);
				}
				termsPriceRepo.save(termsPrice);
				return designation;
			}
			case "openingStock": {
				OpeningStock openingStock = openingStockRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = openingStock.getDesignation();

				openingStock.setVoucherStatus(obj.getVoucherStatus());
				openingStock.getEmpId().add(empId);
				if (obj.getVoucherStatus().equals("Approved")) {
					openingStock.setApprovedDate(LocalDate.now());
					closingStockTableRepo.save(new ClosingStockTable(null, openingStock.getOfficeName(),
							openingStock.getAsOn(), openingStock.getProductName(), openingStock.getGodownName(),
							openingStock.getQuantity()));
				}
				if (oldDesignation == null) {
					openingStock.setDesignation(Arrays.asList(designation));
				} else {
					openingStock.getDesignation().add(designation);
				}
				openingStockRepo.save(openingStock);
				return designation;
			}
			case "poRequest": {
				PoRequest poRequest = poRequestRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = poRequest.getDesignation();

				poRequest.setVoucherStatus(obj.getVoucherStatus());
				poRequest.getEmpId().add(empId);
				if (obj.getVoucherStatus().equals("Approved")) {
					poRequest.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					poRequest.setDesignation(Arrays.asList(designation));
				} else {
					poRequest.getDesignation().add(designation);
				}
				poRequestRepo.save(poRequest);
				return designation;
			}
			case "purchaseOrder": {
				PurchaseOrder purchaseOrder = purchaseOrderRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = purchaseOrder.getDesignation();

				purchaseOrder.setVoucherStatus(obj.getVoucherStatus());
				purchaseOrder.getEmpId().add(empId);
				if (obj.getVoucherStatus().equals("Approved")) {
					purchaseOrder.setApprovedDate(LocalDate.now());
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					if (purchaseOrder.getPoBased().equals("Request")) {
						purchaseOrder.getTableData().forEach(item -> {
							try {
								porequestService.updateRejectedQty(item, purchaseOrder.getProductName());
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
					}
				}
				if (oldDesignation == null) {
					purchaseOrder.setDesignation(Arrays.asList(designation));
				} else {
					purchaseOrder.getDesignation().add(designation);
				}
				purchaseOrderRepo.save(purchaseOrder);
				return designation;
			}
			case "grn": {
				GRN grn = grnRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = grn.getDesignation();

				grn.setVoucherStatus(obj.getVoucherStatus());
				grn.getEmpId().add(empId);
				grn.setUnloadingBillEntry(false);
				grn.setBillNo("");
				if (obj.getVoucherStatus().equals("Approved")) {
					grn.setApprovedDate(LocalDate.now());
					grnService.updateClosingBalance(grn);
				}
				if (obj.getVoucherStatus().equals("Approved")) {

				}
				if (oldDesignation == null) {
					grn.setDesignation(Arrays.asList(designation));
				} else {
					grn.getDesignation().add(designation);
				}
				grnRepo.save(grn);
				return designation;
			}
			case "gtn": {
				GTN gtn = gtnRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = gtn.getDesignation();

				gtn.setVoucherStatus(obj.getVoucherStatus());
				gtn.getEmpId().add(empId);
				gtn.setBillEntry(false);
				if (obj.getVoucherStatus().equals("Approved")) {
					gtn.setApprovedDate(LocalDate.now());
					if (gtn.getGtnFor().equals("Issue")) {
						gtnService.updateClosingBalanceIssue(gtn);
					} else {
						gtnService.updateClosingBalanceReceipt(gtn);
						if (gtn.getTransactionFor().endsWith("Region Direct")) {
							dcService.createDcForOtherRegionReceipt(gtn, jwt);
						}
					}
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					if (gtn.getGtnFor().equals("Issue")) {
						List<GrnQtyUpdateForDc> collect = gtn
								.getGtnTableData().stream().map(item -> new GrnQtyUpdateForDc(null,
										item.getOutwardBatchNo(), item.getQty(), null, item.getVoucherId()))
								.collect(Collectors.toList());

						grnService.revertGrnQtyForDc(collect);
						if (gtn.getTransactionFor().endsWith("Region Direct")) {
							despatchAdviceService.revertDespatchAdviceQty(gtn.getDaNo(), gtn.getGtnTableData().stream()
									.map(i -> new DcTableData(null, null, null, null, null, gtn.getProductName(), null,
											null, i.getQty(), null, null, null, null, null, null, null))
									.collect(Collectors.toList()));
						}
					} else {
						gtn.setIssuedGtnNo(null);
					}
				}
				if (oldDesignation == null) {
					gtn.setDesignation(Arrays.asList(designation));
				} else {
					gtn.getDesignation().add(designation);
				}
				gtnRepo.save(gtn);
				return designation;
			}
			case "salesReturn": {
				SalesReturn salesReturn = salesReturnRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = salesReturn.getDesignation();

				salesReturn.setVoucherStatus(obj.getVoucherStatus());
				salesReturn.getEmpId().add(empId);
				salesReturn.setBillEntry(false);
				gtnService.updateJVStatusInAcc(salesReturn.getJvNo(), obj.getVoucherStatus(), jwt);
				if (obj.getVoucherStatus().equals("Approved")) {
					salesReturn.setApprovedDate(LocalDate.now());
					gtnService.updateClosingBalanceReceipt(salesReturn);
				}
				if (oldDesignation == null) {
					salesReturn.setDesignation(Arrays.asList(designation));
				} else {
					salesReturn.getDesignation().add(designation);
				}
				salesReturnRepo.save(salesReturn);
				return designation;
			}
			case "despatchAdvice": {
				DespatchAdvice despatchAdvice = despatchAdviceRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = despatchAdvice.getDesignation();

				despatchAdvice.setVoucherStatus(obj.getVoucherStatus());
				despatchAdvice.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					despatchAdvice.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					despatchAdvice.setDesignation(Arrays.asList(designation));
				} else {
					despatchAdvice.getDesignation().add(designation);
				}
				despatchAdviceRepo.save(despatchAdvice);
				return designation;
			}
			case "dc": {
				DeliveryChellan deliveryChellan = deliveryChellanRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = deliveryChellan.getDesignation();

				deliveryChellan.setVoucherStatus(obj.getVoucherStatus());
				deliveryChellan.getEmpId().add(empId);
				deliveryChellan.setBillEntry(false);
				if (obj.getVoucherStatus().equals("Approved")) {
					deliveryChellan.setApprovedDate(LocalDate.now());
					dcService.updateClosingBalance(deliveryChellan);
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					List<GrnQtyUpdateForDc> collect = deliveryChellan.getDcTableData().stream()
							.map(item -> new GrnQtyUpdateForDc(null, item.getOutwardBatchNo(), item.getQty(), null,
									item.getVoucherId()))
							.collect(Collectors.toList());

					grnService.revertGrnQtyForDc(collect);
					despatchAdviceService.revertDespatchAdviceQty(deliveryChellan.getDespatchAdviceNo(),
							deliveryChellan.getDcTableData());
				}
				if (oldDesignation == null) {
					deliveryChellan.setDesignation(Arrays.asList(designation));
				} else {
					deliveryChellan.getDesignation().add(designation);
				}
				deliveryChellanRepo.save(deliveryChellan);
				return designation;
			}
			case "invoice": {
				Invoice invoice = invoiceRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = invoice.getDesignation();

				invoice.setVoucherStatus(obj.getVoucherStatus());
				invoice.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					invoice.setApprovedDate(LocalDate.now());
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					invoice.setDcNo(null);
					if (invoice.getCollectionMode().equals("Non CC")) {
						invoiceService.revertNonCCInvoice(invoice, jwt, null);
					}
				}
				if (oldDesignation == null) {
					invoice.setDesignation(Arrays.asList(designation));
				} else {
					invoice.getDesignation().add(designation);
				}
				invoiceRepo.save(invoice);
				return designation;
			}
			case "wagonUpdate": {
				GRN grn = grnRepo.findById(Long.valueOf(obj.getId())).orElse(null);
				WagonDataGrn wagonData = grn.getWagonData();
				if (wagonData.getActualReceiptQty() == null) {
					throw new Exception("Enter Wagon Data Before Approval!");
				}
				designation = userService.getNewDesignation(empId);
				oldDesignation = wagonData.getWagonDesignation();

				wagonData.setWagonStatus(obj.getVoucherStatus());
				grn.getEmpId().add(empId);
				grn.setWagonBillEntry(false);
				if (obj.getVoucherStatus().equals("Approved")) {
					wagonData.setWagonApprovedDate(LocalDate.now());
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					grnService.revertGrnJv(grn, jwt, null);
				}
				if (oldDesignation == null) {
					wagonData.setWagonDesignation(Arrays.asList(designation));
				} else {
					wagonData.getWagonDesignation().add(designation);
				}
				grnRepo.save(grn);
				return designation;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + obj.getFormType());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PurchaseBookingRepo purchaseBookingRepo;

	@Autowired
	private CheckMemoGoodsRepo checkMemoGoodsRepo;

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	@Autowired
	private MpaBillEntryRepo mpaBillEntryRepo;

	@Autowired
	private MpaCheckMemoRepo mpaCheckMemoRepo;

	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	@Autowired
	private TcCheckMemoRepo tcCheckMemoRepo;

	@Autowired
	private MpaService mpaService;

	@Autowired
	private TcService tcService;

	@Autowired
	private CheckMemoGoodsService checkMemoGoodsService;

	@Autowired
	private PurchaseBookingService purchaseBookingService;

	public String updateSobVoucherApproval(VoucherApproval obj, String jwt) throws Exception {
		try {
			String designation = null;
			List<String> oldDesignation = null;

			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			logger.info("{}", obj);
			switch (obj.getFormType()) {
			case "purchaseBooking": {
				PurchaseBooking purchaseBooking = purchaseBookingRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = purchaseBooking.getDesignation();

				purchaseBooking.setVoucherStatus(obj.getVoucherStatus());
				purchaseBooking.getEmpId().add(empId);
				purchaseBookingService.updateAccJv(purchaseBooking, jwt);
				if (obj.getVoucherStatus().equals("Approved")) {
					purchaseBooking.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					purchaseBooking.setDesignation(Arrays.asList(designation));
				} else {
					purchaseBooking.getDesignation().add(designation);
				}
				purchaseBookingRepo.save(purchaseBooking);
				return designation;
			}
			case "checkMemoGoods": {
				CheckMemoGoods checkMemoGoods = checkMemoGoodsRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = checkMemoGoods.getDesignation();

				checkMemoGoods.setVoucherStatus(obj.getVoucherStatus());
				checkMemoGoods.getEmpId().add(empId);
				checkMemoGoodsService.updatePvInAcc(checkMemoGoods, jwt);
				if (obj.getVoucherStatus().equals("Approved")) {
					checkMemoGoods.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					checkMemoGoods.setDesignation(Arrays.asList(designation));
				} else {
					checkMemoGoods.getDesignation().add(designation);
				}
				checkMemoGoodsRepo.save(checkMemoGoods);
				return designation;
			}
			case "supplierInvoice": {
				SupplierInvoiceDetails supplierInvoiceDetails = supplierInvoiceDetailsRepo
						.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = supplierInvoiceDetails.getDesignation();

				supplierInvoiceDetails.setVoucherStatus(obj.getVoucherStatus());
				supplierInvoiceDetails.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					supplierInvoiceDetails.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					supplierInvoiceDetails.setDesignation(Arrays.asList(designation));
				} else {
					supplierInvoiceDetails.getDesignation().add(designation);
				}
				supplierInvoiceDetailsRepo.save(supplierInvoiceDetails);
				return designation;
			}
			case "grnAttach": {
				PurchaseOrder purchaseOrder = purchaseOrderRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = purchaseOrder.getSobDesignation();

				purchaseOrder.setSobVoucherStatus(obj.getVoucherStatus());
				purchaseOrder.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					purchaseOrder.setSobApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					purchaseOrder.setSobDesignation(Arrays.asList(designation));
				} else {
					purchaseOrder.getSobDesignation().add(designation);
				}
				purchaseOrderRepo.save(purchaseOrder);
				return designation;
			}
			case "tcBillEntry": {
				TcBillEntry tcBillEntry = tcBillEntryRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = tcBillEntry.getDesignation();

				tcBillEntry.setVoucherStatus(obj.getVoucherStatus());
				tcBillEntry.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					tcBillEntry.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					tcBillEntry.setDesignation(Arrays.asList(designation));
				} else {
					tcBillEntry.getDesignation().add(designation);
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					tcService.revertBillEntryData(jwt, tcBillEntry);
				}
				tcBillEntryRepo.save(tcBillEntry);
				return designation;
			}
			case "tcCheckMemo": {
				TcCheckMemo tcCheckMemo = tcCheckMemoRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = tcCheckMemo.getDesignation();

				tcCheckMemo.setVoucherStatus(obj.getVoucherStatus());
				tcCheckMemo.getEmpId().add(empId);
				tcService.updateAccJvAndPv(tcCheckMemo, jwt);
				if (obj.getVoucherStatus().equals("Approved")) {
					tcCheckMemo.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					tcCheckMemo.setDesignation(Arrays.asList(designation));
				} else {
					tcCheckMemo.getDesignation().add(designation);
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					tcService.revertCheckMemo(jwt, tcCheckMemo);
				}
				tcCheckMemoRepo.save(tcCheckMemo);
				return designation;
			}
			case "mpaBillEntry": {
				MpaBillEntry mpaBillEntry = mpaBillEntryRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = mpaBillEntry.getDesignation();

				mpaBillEntry.setVoucherStatus(obj.getVoucherStatus());
				mpaBillEntry.getEmpId().add(empId);

				if (obj.getVoucherStatus().equals("Approved")) {
					mpaBillEntry.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					mpaBillEntry.setDesignation(Arrays.asList(designation));
				} else {
					mpaBillEntry.getDesignation().add(designation);
				}
				mpaBillEntryRepo.save(mpaBillEntry);
				return designation;
			}
			case "mpaCheckMemo": {
				MpaCheckMemo mpaCheckMemo = mpaCheckMemoRepo.findById(Long.valueOf(obj.getId())).orElse(null);

				designation = userService.getNewDesignation(empId);
				oldDesignation = mpaCheckMemo.getDesignation();

				mpaCheckMemo.setVoucherStatus(obj.getVoucherStatus());
				mpaCheckMemo.getEmpId().add(empId);
				mpaService.updateAccPvJv(mpaCheckMemo, jwt);
				if (obj.getVoucherStatus().equals("Approved")) {
					mpaCheckMemo.setApprovedDate(LocalDate.now());
				}
				if (oldDesignation == null) {
					mpaCheckMemo.setDesignation(Arrays.asList(designation));
				} else {
					mpaCheckMemo.getDesignation().add(designation);
				}
				if (obj.getVoucherStatus().equals("Rejected")) {
					mpaService.revertCheckMemo(mpaCheckMemo.getCheckMemoNo());
				}
				mpaCheckMemoRepo.save(mpaCheckMemo);
				return designation;
			}

			default:
				throw new IllegalArgumentException("Unexpected value: " + obj.getFormType());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
