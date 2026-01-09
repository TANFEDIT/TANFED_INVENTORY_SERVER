package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.InvoiceRepo;
import com.tanfed.inventry.response.*;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class InvoiceServiceImpl implements InvoiceService {

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private DcService dcService;

	@Autowired
	private MasterService masterService;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private OpeningStockService openingStockService;

	private static Logger logger = LoggerFactory.getLogger(InvoiceServiceImpl.class);

	@Override
	public ResponseEntity<String> saveInvoice(Invoice obj, String jwt) throws Exception {
		try {
			String invoiceNo = codeGenerator.invoiceNoGenerator(obj.getOfficeName(), obj.getDate());
			obj.setInvoiceNo(invoiceNo);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.setVoucherStatus("Pending");
			obj.setDueDate(obj.getDate().plusDays(obj.getCreditDays()));
			BuyerFirmInfo buyerFirmInfo;
			try {
				buyerFirmInfo = masterService.getBuyerFirmByFirmNameHandler(jwt, obj.getNameOfInstitution());
				obj.setCcbBranch(buyerFirmInfo.getBranchName());
				obj.setFirmType(buyerFirmInfo.getFirmType());
				if (obj.getGodownName() != "Direct Material Center") {
					GodownInfo godownInfo = masterService.getGodownInfoByGodownNameHandler(obj.getGodownName(), jwt);
					obj.setLicenseNoGodown(godownInfo.getLicenseNo());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			invoiceRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!" + "\n Invoice No :" + invoiceNo,
					HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editInvoice(Invoice obj, String jwt) throws Exception {
		try {
			Invoice invoice = invoiceRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			invoice.getEmpId().add(empId);
			invoiceRepo.save(invoice);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<Invoice> getInvoiceDataByOffficeName(String officeName) throws Exception {
		try {
			return invoiceRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForInvoice getDataForInvoice(String officeName, String activity, String dcNo, String jwt,
			String collectionMode, String month, String godownName) throws Exception {
		try {
			DataForInvoice data = new DataForInvoice();
			if (!officeName.isEmpty() && officeName != null) {
				data.setGodownNameList(dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
						.map(item -> item.getGodownName()).collect(Collectors.toSet()));
				if (!month.isEmpty() && month != null) {
					data.setInvoiceData(getInvoiceDataByOffficeName(officeName).stream().filter(item -> {
						String invoiceMonth = String.format("%s%s%04d",
								item.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), " ",
								item.getDate().getYear());
						return month.equals(invoiceMonth) && item.getCollectionMode().equals("Non CC")
								&& item.getAdjReceiptNo() == null;
					}).map(item -> new InvoiceDataTable(item.getInvoiceNo(), item.getInvoiceFor(),
							item.getTableData().stream().map(TableDataInvoice::getProductCategory)
									.collect(Collectors.toList()),
							item.getTableData().stream().map(TableDataInvoice::getSupplierName)
									.collect(Collectors.toList()),
							item.getTableData().stream().map(TableDataInvoice::getProductName)
									.collect(Collectors.toList()),
							item.getTotalQty(), item.getTotalInvoiceValue())).collect(Collectors.toList()));
				}
				if (!activity.isEmpty() && activity != null) {
					data.setCreditPeriodList(masterService.getTerms_Price_ConfigListHandler(jwt).stream()
							.filter(item -> item.getActivity().equals(activity))
							.map(item -> item.getSalesCreditPeriod()).filter(Objects::nonNull)
							.collect(Collectors.toSet()));
					if (!collectionMode.isEmpty() && collectionMode != null) {
						if (!godownName.isEmpty() && godownName != null) {
							List<String> dcNoList = dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
									.filter(item -> item.getActivity().equals(activity)
											&& item.getGodownName().equals(godownName))
									.filter(item -> {
										return item.getDcTableData().stream().anyMatch(
												itemData -> itemData.getCollectionMode().equals(collectionMode));
									}).map(item -> item.getDcNo()).collect(Collectors.toList());

							dcNoList.removeIf(item -> {
								List<Invoice> invoice = invoiceRepo.findByDcNo(item);
								return invoice.stream().anyMatch(inv -> inv.getCollectionMode().equals(collectionMode));
							});

							data.setDcNoList(dcNoList);
						}
					}
					if (!dcNo.isEmpty() && dcNo != null) {
						DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(dcNo);
						if (deliveryChellan == null) {
							throw new IllegalArgumentException("No data found");
						}
						mapInvoiceDataFromDc(deliveryChellan, data);
						List<InvoiceTable> invoiceTable = new ArrayList<InvoiceTable>();
						List<InvoiceTermsAndConditions> tcData = new ArrayList<InvoiceTermsAndConditions>();
						List<DcTableData> dcTableData = deliveryChellan.getDcTableData().stream()
								.filter(item -> item.getCollectionMode().equals(collectionMode))
								.collect(Collectors.toList());
						List<Double> b2cDiscount = new ArrayList<Double>();
						dcTableData.forEach(item -> {
							try {
								String batchOrCertificateNo = null;
								if (item.getOutwardBatchNo().startsWith("GR")) {
									GRN grn = grnService.getGrnDataByGrnNo(item.getOutwardBatchNo());
									batchOrCertificateNo = grn.getBatchOrCertificateNo();
								}
								Double basicPrice = 0.0, cgst = 0.0, sgst = 0.0, margin = 0.0, gstOnMargin = 0.0,
										mrp = 0.0;
								Double total;
								if (item.getOutwardBatchNo().startsWith("OB")) {
									OpeningStock openingStock = openingStockService.getObById(item.getOutwardBatchNo());
									basicPrice = openingStock.getB2bBasicPrice();
									cgst = openingStock.getB2bCgst();
									sgst = openingStock.getB2bSgst();
									margin = openingStock.getMarginToPaccs();
									gstOnMargin = openingStock.getPaccsMarginGst();
									mrp = openingStock.getB2cMrp();
									total = (basicPrice + cgst + sgst) * item.getQty();
									updateTotals(data, basicPrice * item.getQty(), cgst * item.getQty(),
											sgst * item.getQty(), total, margin, gstOnMargin);
									invoiceTable.add(new InvoiceTable(item.getOutwardBatchNo(),
											item.getProductCategory(), item.getSupplierName(), item.getProductName(),
											item.getPacking(), item.getBags(), item.getQty(), item.getHsnCode(),
											item.getGstRate(), basicPrice, cgst, sgst,
											RoundToDecimalPlace.roundToTwoDecimalPlaces(total), mrp, margin,
											gstOnMargin, batchOrCertificateNo));
								} else {
									TermsPrice termsPrice = termsPriceService.fetchTermsByTermsNo(item.getTermsNo());

									PurchaseTermsConditionsTPM purchaseTermsAndCondition = termsPrice
											.getPurchaseTermsAndCondition();
									B2bTermsConditionsTPM b2bTermsConditionsTPM = termsPrice.getB2bTermsAndConditions();
									B2cPricingTPM b2cPricingTPM = termsPrice.getB2cPrice();

									InvoiceTermsAndConditions tc = new InvoiceTermsAndConditions();
									tc.setSelectedProductName(item.getProductName());
									tc.setQty(item.getQty());
									PmodeAndValue paccsIncentive = new PmodeAndValue();
									if (purchaseTermsAndCondition.getIncentiveToFirm().equals("By Invoice Adj")) {
										paccsIncentive.setInvAdj_Cash(
												b2bTermsConditionsTPM.getIncentivePaccs() * item.getQty());
										paccsIncentive.setOnline(0.0);
									} else if (purchaseTermsAndCondition.getIncentiveToFirm().equals("By Online")) {
										paccsIncentive.setInvAdj_Cash(0.0);
										paccsIncentive
												.setOnline(b2bTermsConditionsTPM.getIncentivePaccs() * item.getQty());
									}

									tc.setIncentivePaccs(paccsIncentive);
									PmodeAndValue salesmanIncentive = new PmodeAndValue();
									PmodeAndValue secretaryIncentive = new PmodeAndValue();
									if ("B2B".equals(deliveryChellan.getSupplyTo())) {
										if (purchaseTermsAndCondition.getIncentiveToB2b().equals("By Cash")) {
											salesmanIncentive.setInvAdj_Cash(
													b2bTermsConditionsTPM.getSalesmanIncentive() * item.getQty());
											salesmanIncentive.setOnline(0.0);
											secretaryIncentive.setInvAdj_Cash(
													b2bTermsConditionsTPM.getSecretoryIncentive() * item.getQty());
											secretaryIncentive.setOnline(0.0);
										} else if (purchaseTermsAndCondition.getIncentiveToB2b().equals("By Online")) {
											salesmanIncentive.setInvAdj_Cash(0.0);
											salesmanIncentive.setOnline(
													b2bTermsConditionsTPM.getSalesmanIncentive() * item.getQty());
											secretaryIncentive.setInvAdj_Cash(0.0);
											secretaryIncentive.setOnline(
													b2bTermsConditionsTPM.getSecretoryIncentive() * item.getQty());
										}
										tc.setSalesmanIncentive(salesmanIncentive);
										tc.setSecretoryIncentive(secretaryIncentive);
									} else if ("B2C".equals(deliveryChellan.getSupplyTo())) {
										if (purchaseTermsAndCondition.getIncentiveToB2c().equals("By Invoice Adj")) {
											b2cDiscount.add(b2cPricingTPM.getB2cDiscount() * item.getQty());
										}
									}
									if (!purchaseTermsAndCondition.getIncentiveToFirm().equals("Not Applicable")
											|| !purchaseTermsAndCondition.getIncentiveToB2b()
													.equals("Not Applicable")) {
										tcData.add(
												new InvoiceTermsAndConditions(null, paccsIncentive, salesmanIncentive,
														secretaryIncentive, item.getProductName(), item.getQty()));
									}
									if ("B2B".equals(deliveryChellan.getSupplyTo())) {
										basicPrice = termsPrice.getB2bPrice().getB2bBasicPrice();
										cgst = termsPrice.getB2bPrice().getB2bCgst();
										sgst = termsPrice.getB2bPrice().getB2bSgst();
										margin = termsPrice.getB2bPrice().getMarginToPaccs();
										gstOnMargin = termsPrice.getB2bPrice().getPaccsMarginGst();
										mrp = termsPrice.getB2cPrice().getB2cMrp();
									} else if ("B2C".equals(deliveryChellan.getSupplyTo())) {
										basicPrice = termsPrice.getB2cPrice().getB2cBasicPrice();
										cgst = termsPrice.getB2cPrice().getB2cCgst();
										sgst = termsPrice.getB2cPrice().getB2cSgst();
										mrp = termsPrice.getB2cPrice().getB2cMrp();
									}
									total = (basicPrice + cgst + sgst) * item.getQty();
									updateTotals(data, basicPrice * item.getQty(), cgst * item.getQty(),
											sgst * item.getQty(), total, margin, gstOnMargin);
									invoiceTable.add(new InvoiceTable(item.getOutwardBatchNo(),
											item.getProductCategory(), item.getSupplierName(), item.getProductName(),
											item.getPacking(), item.getBags(), item.getQty(), item.getHsnCode(),
											item.getGstRate(), basicPrice, cgst, sgst,
											RoundToDecimalPlace.roundToTwoDecimalPlaces(total), mrp, margin,
											gstOnMargin, batchOrCertificateNo));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
						data.setTableData(invoiceTable);
						data.setTcData(tcData);
						if (data.getTcData().isEmpty()) {
							data.setTcData(null);
						}
						data.setB2cDiscount(b2cDiscount.stream().mapToDouble(item -> item).sum());
					}
				}
			}
			logger.info("{}", data);
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private static void updateTotals(DataForInvoice data, double basic, double cgst, double sgst, double total,
			double margin, double gstOnMargin) {
		data.setTotalBasicValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalBasicValue() + basic));
		data.setTotalCgstValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalCgstValue() + cgst));
		data.setTotalSgstValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalSgstValue() + sgst));
		data.setTotalInvoiceValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalInvoiceValue() + total));
		data.setTotalMarginValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalMarginValue() + margin));
		data.setTotalGstOnMargin(RoundToDecimalPlace.roundToTwoDecimalPlaces(data.getTotalGstOnMargin() + gstOnMargin));
	}

	private void mapInvoiceDataFromDc(DeliveryChellan dc, DataForInvoice data) {
		data.setIfmsId(dc.getIfmsId());
		data.setNameOfInstitution(dc.getNameOfInstitution());
		data.setLicenseNo(dc.getLicenseNo());
		data.setBuyerGstNo(dc.getBuyerGstNo());
		data.setVillage(dc.getVillage());
		data.setBlock(dc.getBlock());
		data.setTaluk(dc.getTaluk());
		data.setDistrict(dc.getDistrict());
		data.setSupplyMode(dc.getSupplyMode());
		data.setSupplyTo(dc.getSupplyTo());
		data.setTotalQty(dc.getTotalQty());
		data.setTotalNoOfBags(dc.getTotalBags());
		data.setDespatchAdviceNo(dc.getDespatchAdviceNo());

	}

	@Override
	public List<DespatchAdviseTable> fetchInvoiceData(String despatchAdviceNo) throws Exception {
		try {
			return invoiceRepo.findByDespatchAdviceNo(despatchAdviceNo).stream()
					.filter(item -> !"Rejected".equals(item.getVoucherStatus())).flatMap(item -> {
						Map<String, Double> productQtyMap = item.getTableData().stream().collect(Collectors.groupingBy(
								TableDataInvoice::getProductName, Collectors.summingDouble(TableDataInvoice::getQty)));

						return productQtyMap.entrySet().stream().map(entry -> new DespatchAdviseTable(entry.getKey(),
								item.getInvoiceNo(), null, entry.getValue()));
					}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForInvoiceUpdate getDataForInvoiceUpdate(String officeName, String activity, String selectedOption,
			LocalDate fromDate, LocalDate toDate, String invoiceNo) throws Exception {
		try {
			DataForInvoiceUpdate data = new DataForInvoiceUpdate();
			if (officeName != null && !officeName.isEmpty()) {
				if (activity != null && !activity.isEmpty()) {
					if (fromDate != null && toDate != null) {
						if (selectedOption != null && !selectedOption.isEmpty()) {
							data.setInvoiceData(getInvoiceDataByOffficeName(officeName).stream().filter(temp -> {
								Boolean dateMatch = temp.getDate() != null && !temp.getDate().isBefore(fromDate)
										&& !temp.getDate().isAfter(toDate);
								Boolean isUpdateDone = false;
								if (selectedOption.equals("ifms-update-1")) {
									isUpdateDone = temp.getSecondPointIfmsid() == null && temp.getUpdateDate() == null;
								} else if (selectedOption.equals("ifms-update-2")) {
									isUpdateDone = temp.getSecondPointIfmsid() != null && temp.getIfmsStatus() == null
											&& temp.getAckDate() == null && temp.getUpdateDate() != null;
								}
								return temp.getActivity().equals(activity) && dateMatch && isUpdateDone;
							}).map(item -> {
								if (selectedOption.equals("ifms-update-1")) {
									return new InvoiceTableUpdateResponse(item.getInvoiceNo(), item.getDate(),
											item.getTotalQty(), null, null, null, null);
								} else if (selectedOption.equals("ifms-update-2")) {
									return new InvoiceTableUpdateResponse(item.getInvoiceNo(), item.getDate(),
											item.getTotalQty(), item.getSecondPointIfmsid(), item.getUpdateDate(), null,
											null);
								} else {
									return null;
								}
							}).collect(Collectors.toList()));
						}
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateInvoice(List<InvoiceUpdate> obj) throws Exception {
		try {
			obj.forEach(temp -> {
				Invoice invoice = invoiceRepo.findByInvoiceNo(temp.getInvoiceNo()).get();
				if (temp.getIfmsStatus() == "Reject") {
					invoice.setSecondPointIfmsid(null);
					invoice.setUpdateDate(null);
				} else {
					invoice.setSecondPointIfmsid(temp.getSecondPointIfmsid());
					invoice.setUpdateDate(temp.getUpdateDate());
					invoice.setAckDate(temp.getAckDate());
					invoice.setIfmsStatus(temp.getIfmsStatus());
				}
				invoiceRepo.save(invoice);
			});
			return new ResponseEntity<String>("Invoice Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateAdjReceiptNoInNonCcInvoice(String invoiceNo, String voucherNo, LocalDate date)
			throws Exception {
		try {
			Invoice invoice = invoiceRepo.findByInvoiceNo(invoiceNo).get();
			invoice.setDateOfCollectionFromCcb(Arrays.asList(date));
			invoice.setAdjReceiptNo(Arrays.asList(voucherNo));
			invoice.setAdjReceiptStatus(Arrays.asList("Pending"));
			invoiceRepo.save(invoice);
			return new ResponseEntity<String>("Invoice Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private SupplierInvoiceService SupplierInvoiceService;

	@Override
	public InvoiceDataForDnCn getDataForDnCn(String invoiceNo) throws Exception {
		try {
			InvoiceDataForDnCn data = new InvoiceDataForDnCn();
			if (invoiceNo.startsWith("RO")) {
				Invoice invoice = invoiceRepo.findByInvoiceNo(invoiceNo).get();
				data.setInvoiceDate(invoice.getDate());
				data.setGst(invoice.getBuyerGstNo());
				data.setIfmsId(invoice.getIfmsId());
				data.setInvoiceValue(invoice.getNetInvoiceAdjustment());
				data.setName(invoice.getNameOfInstitution());
			} else {
				SupplierInvoiceDetails supplierInvoiceDetails = SupplierInvoiceService
						.getSupplierInvoiceByInvoiceNumber(invoiceNo);
				data.setInvoiceDate(supplierInvoiceDetails.getInvoiceDate());
				data.setGst(supplierInvoiceDetails.getSupplierGst());
				data.setInvoiceValue(supplierInvoiceDetails.getTotalInvoiceValue());
				data.setName(supplierInvoiceDetails.getSupplierName());
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertNonCCInvoice(Invoice obj, String jwt, AdjustmentReceiptVoucher adjv) throws Exception {
		try {
			if (obj != null) {
				Vouchers vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
						obj.getAdjReceiptNo().get(0), jwt);
				VoucherApproval temp = new VoucherApproval("Rejected",
						String.valueOf(vouchers.getAdjustmentReceiptVoucherData().getId()), "adjustmentReceiptVoucher", null);
				accountsService.voucherApprovalHandler(temp, jwt);
			}
			if (adjv != null) {
				Invoice invoice = invoiceRepo.findByInvoiceNo(adjv.getIcmInvNo()).get();
				invoice.setAdjReceiptStatus(Arrays.asList(adjv.getVoucherStatus()));
				invoiceRepo.save(invoice);
			}

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void approveNonCCInvoice(String invoiceNo) throws Exception {
		try {
			Invoice invoice = invoiceRepo.findByInvoiceNo(invoiceNo).get();
			invoice.setAdjReceiptStatus(Arrays.asList("Approved"));
			invoiceRepo.save(invoice);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<Invoice> getInvoiceDataFromDateOfficeName(String officeName, LocalDate date) throws Exception {
		try {
			return invoiceRepo.findByOfficeName(officeName).stream().filter(item -> item.getDate().isAfter(date))
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public Invoice getInvoiceDataByInvoiceNo(String invoiceNo) throws Exception {
		try {
			return invoiceRepo.findByInvoiceNo(invoiceNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
