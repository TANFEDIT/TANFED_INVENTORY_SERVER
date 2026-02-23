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
import com.tanfed.inventry.dto.GrnDto;
import com.tanfed.inventry.dto.GtnDTO;
import com.tanfed.inventry.dto.SupplierInvoiceDto;
import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.*;
import com.tanfed.inventry.response.SobData;

@Service
public class FilterInventryDataService {

	private static Logger logger = LoggerFactory.getLogger(FilterInventryDataService.class);

	@Autowired
	private TermsPriceRepo termsPriceRepo;

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Autowired
	private PoRequestRepo poRequestRepo;

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	@Autowired
	private SalesReturnRepo salesReturnRepo;

	@Autowired
	private GrnRepo grnRepo;

	@Autowired
	private GtnRepo gtnRepo;

	@Autowired
	private DespatchAdviceRepo despatchAdviceRepo;

	@Autowired
	private DeliveryChellanRepo deliveryChellanRepo;

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private MasterService masterService;

	public InventryData filterInventryData(String formType, LocalDate fromDate, LocalDate toDate, String officeName,
			String activity, String voucherStatus, String financialMonth, String jwt, String gtnFor) throws Exception {
		try {
			InventryData data = new InventryData();
			logger.info(formType);
			switch (formType) {
			case "termsAndPrice": {
				List<TermsPrice> filteredLst = termsPriceRepo.findAll().stream().filter(i -> ((!i.getVoucherStatus()
						.equals("Rejected") && (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
						&& (fromDate == null || (fromDate != null && !i.getMasterData().getDate().isBefore(fromDate)))
						&& (toDate == null || (toDate != null && !i.getMasterData().getDate().isAfter(toDate))))))
						.collect(Collectors.toList());

				filteredLst.sort(Comparator.comparing(TermsPrice::getId).reversed());
				data.setTermsAndPrice(filteredLst);
				return data;
			}
			case "openingStock": {
				List<OpeningStock> filteredLst = openingStockRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getAsOn().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getAsOn().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(OpeningStock::getId).reversed());
				data.setOpeningStock(filteredLst);
				return data;
			}
			case "poRequest": {
				List<PoRequest> filteredLst = poRequestRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(PoRequest::getId).reversed());
				data.setPoRequest(filteredLst);
				return data;
			}
			case "purchaseOrder": {
				List<PurchaseOrder> filteredLst = purchaseOrderRepo.findAll().stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(PurchaseOrder::getId).reversed());
				data.setPurchaseOrder(filteredLst);
				return data;
			}
			case "grn": {
				List<GrnDto> filteredLst = grnRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(i -> mapGrnToDto(i, jwt)).collect(Collectors.toList());

				filteredLst.sort(Comparator.comparing(GrnDto::getId).reversed());
				data.setGrn(filteredLst);
				return data;
			}
			case "gtn": {
				List<GTN> filteredLst = gtnRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate)))))
								&& (gtnFor == null || gtnFor.isEmpty() || gtnFor.equals(i.getGtnFor()))
								&& !i.getTransactionFor().equals("Sales Return"))
						.collect(Collectors.toList());

				filteredLst.sort(Comparator.comparing(GTN::getId).reversed());
				data.setGtn(filteredLst);
				return data;
			}
			case "salesReturn": {
				List<GtnDTO> filteredLst = salesReturnRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(item -> mapSalesReturnToDto(item, jwt)).collect(Collectors.toList());

				filteredLst.sort(Comparator.comparing(GtnDTO::getId).reversed());
				data.setSalesReturn(filteredLst);
				return data;
			}
			case "despatchAdvice": {

				List<DespatchAdvice> filteredLst = despatchAdviceRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(DespatchAdvice::getId).reversed());
				data.setDespatchAdvice(filteredLst);
				return data;
			}
			case "dc": {
				List<DeliveryChellan> filteredLst = deliveryChellanRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(DeliveryChellan::getId).reversed());
				data.setDc(filteredLst);
				return data;
			}
			case "invoice": {
				List<Invoice> filteredLst = invoiceRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(Invoice::getId).reversed());
				data.setInvoice(filteredLst);
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
	private PurchaseBookingRepo purchaseBookingRepo;

	@Autowired
	private CheckMemoGoodsRepo checkMemoGoodsRepo;

	@Autowired
	private MpaBillEntryRepo mpaBillEntryRepo;

	@Autowired
	private MpaCheckMemoRepo mpaCheckMemoRepo;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	@Autowired
	private TcCheckMemoRepo tcCheckMemoRepo;

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	public SobData filterSobData(String formType, LocalDate fromDate, LocalDate toDate, String officeName,
			String voucherStatus, String jwt) throws Exception {
		try {
			SobData data = new SobData();
			switch (formType) {
			case "supplierInvoice": {
				List<SupplierInvoiceDto> filteredList = supplierInvoiceDetailsRepo.findAll().stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(i -> mapToDto(i, jwt)).collect(Collectors.toList());

				data.setSupplierInvoice(filteredList);
				return data;
			}
			case "grnAttach": {
				List<PurchaseOrder> filteredLst = purchaseOrderRepo.findAll().stream()
						.filter(i -> i.getVoucherStatus().equals("Approved")
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))
						.collect(Collectors.toList());

				filteredLst.sort(Comparator.comparing(PurchaseOrder::getId).reversed());
				data.setGrnAttach(filteredLst);
				return data;
			}
			case "purchaseBooking": {
				List<PurchaseBookingDto> filteredLst = purchaseBookingRepo.findAll().stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(i -> {
							try {
								return mapPbDataToDto(i, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
				filteredLst.sort(Comparator.comparing(PurchaseBookingDto::getId).reversed());
				data.setPurchaseBooking(filteredLst);
				return data;
			}
			case "checkMemoGoods": {
				List<CheckMemoGoodsDto> checkMemoGoodsList = checkMemoGoodsRepo.findAll().stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getCmDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getCmDate().isAfter(toDate))))))
						.map(i -> {
							try {
								return mapCmgDataToDto(i, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
				checkMemoGoodsList.sort(Comparator.comparing(CheckMemoGoodsDto::getId).reversed());
				data.setCheckMemoGoods(checkMemoGoodsList);
				return data;
			}

			case "tcBillEntry": {
				List<TcBillEntry> invoiceList = tcBillEntryRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				invoiceList.sort(Comparator.comparing(TcBillEntry::getId).reversed());
				data.setTcBillEntry(invoiceList);
				return data;
			}

			case "tcCheckMemo": {
				List<TcCheckMemoDto> invoiceList = tcCheckMemoRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(item -> {
							try {
								return mapTcDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
				invoiceList.sort(Comparator.comparing(TcCheckMemoDto::getId).reversed());
				data.setTcCheckMemo(invoiceList);
				return data;
			}

			case "mpaBillEntry": {
				List<MpaBillEntry> invoiceList = mpaBillEntryRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.collect(Collectors.toList());
				invoiceList.sort(Comparator.comparing(MpaBillEntry::getId).reversed());
				data.setMpaBillEntry(invoiceList);
				return data;
			}

			case "mpaCheckMemo": {
				List<MpaCheckMemoDto> invoiceList = mpaCheckMemoRepo.findByOfficeName(officeName).stream()
						.filter(i -> ((!i.getVoucherStatus().equals("Rejected")
								&& (voucherStatus.isEmpty() || i.getVoucherStatus().equals(voucherStatus))
								&& (fromDate == null || (fromDate != null && !i.getDate().isBefore(fromDate)))
								&& (toDate == null || (toDate != null && !i.getDate().isAfter(toDate))))))
						.map(item -> {
							try {
								return mapMpaDataToDto(item, jwt);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}).collect(Collectors.toList());
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

	private SupplierInvoiceDto mapToDto(SupplierInvoiceDetails i, String jwt) {
		List<JournalVoucher> jvData = new ArrayList<JournalVoucher>();
		try {
			if (i.getNetJv() != null) {
				Vouchers vouchers;
				vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", i.getNetJv(), jwt);
				jvData.add(vouchers.getJournalVoucherData());
			}
			if (i.getTaxJv() != null) {
				Vouchers vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", i.getTaxJv(),
						jwt);
				jvData.add(vouchers.getJournalVoucherData());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new SupplierInvoiceDto(i.getId(), i.getVoucherStatus(), i.getDesignation(), i.getApprovedDate(),
				i.getInvoiceQtyAvlForGrnAttach(), i.getEmpId(), i.getDate(), i.getCreatedAt(), i.getInvoiceNumber(),
				i.getInvoiceQty(), i.getInvoiceDate(), i.getTotalInvoiceValue(), i.getTotalBasicPrice(),
				i.getTotalCgstValue(), i.getTotalSgstValue(), i.getMonthOfSupply(), i.getTermsMonth(), i.getTermsNo(),
				i.getActivity(), i.getProductName(), i.getSupplierName(), i.getSupplierGst(), i.getFilename(),
				i.getFiletype(), i.getFiledata(), jvData, termsPriceRepo.findByTermsNo(i.getTermsNo()).get());
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

	private GrnDto mapGrnToDto(GRN grn, String jwt) {
		try {
			JournalVoucher jv;
			if (grn.getJvNo() != null) {
				Vouchers vouchers;
				vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", grn.getJvNo(), jwt);
				jv = vouchers.getJournalVoucherData();
			} else {
				jv = null;
			}
			return new GrnDto(grn.getId(), grn.getGrnNo(), grn.getVoucherStatus(), grn.getEmpId(), grn.getDesignation(),
					grn.getOfficeName(), grn.getCreatedAt(), grn.getApprovedDate(), grn.getDate(), grn.getActivity(),
					grn.getProductName(), grn.getGodownType(), grn.getSupplierName(), grn.getGodownName(),
					grn.getIfmsId(), grn.getSupplyFrom(), grn.getProductCategory(), grn.getProductGroup(),
					grn.getSupplierGst(), grn.getStandardUnits(), grn.getDoor(), grn.getStreet(), grn.getPincode(),
					grn.getDistrict(), grn.getPoNo(), grn.getModeOfSupply(), grn.getTotalPoQty(), grn.getGrnCreated(),
					grn.getDcWdnRoNo(), grn.getSupplierDocDate(), grn.getSupplierTransport(), grn.getVehicleNo(),
					grn.getTransportCharges(), grn.getUnloadingCharges(), grn.getUnloadingChargesValue(),
					grn.getWagonClearanceValue(), grn.getMaterialReceivedBags(), grn.getMaterialReceivedQuantity(),
					grn.getPacking(), grn.getMaterialSuppliedBags(), grn.getMaterialSuppliedQuantity(),
					grn.getBatchOrCertificateNo(), grn.getExpiryDate(), grn.getBatchNo(), grn.getCertification(),
					grn.getGrnIfmsId(), grn.getBillNo(), grn.getFirstPointIfmsId(), grn.getIdCreateDate(),
					grn.getAckDate(), grn.getIfmsStatus(), grn.getGrnQtyAvlForGrnAttach(), grn.getGrnAttachQty(),
					grn.getGrnAttachQtyString(), grn.getSupplierInvoiceNo(), grn.getGrnQtyAvlForDc(),
					grn.getWagonData(), jv, grn.getIsPurchaseBooked(), grn.getUnloadingBillEntry(),
					grn.getWagonBillEntry());
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
				item.getNet(), item.getOthers(), item.getNetPriceAfterDeduction(), item.getTradeIncome(),
				item.getTermsData(), item.getTermsDataGeneral(), item.getTermsDataDirect(), item.getTermsDataBuffer(),
				jvData);
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
		MpaBillEntry mpaBillEntry = mpaBillEntryRepo.findByCheckMemoNo(item.getCheckMemoNo()).get();

		return new MpaCheckMemoDto(item.getId(), item.getDate(), item.getCheckMemoNo(), item.getOfficeName(), null,
				item.getTotalCalculatedValue(), item.getTotalSgstValue(), item.getTotalCgstValue(),
				item.getTotalPaymentValue(), item.getRecoveryIfAny(), item.getNetTotalDeduction(), item.getTcsOrTds(),
				item.getRate(), item.getCalculatedTcsTdsValue(), item.getNetPaymentAfterAdjustment(),
				item.getDifference(), item.getRemarks(), item.getJvNo() != null ? jv : null,
				item.getPvNo() != null ? pv : null, item.getFinancialYear(), item.getFinancialMonth(),
				item.getContractFirm(), item.getClaimBillNo(), item.getClaimBillDate(), item.getTotalBillValue(),
				mpaBillEntry.getEmpData(), item.getDesignation(), item.getVoucherStatus());
	}

}
