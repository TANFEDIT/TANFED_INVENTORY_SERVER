package com.tanfed.inventry.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.entity.PurchaseBooking;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.entity.SupplierInvoiceDetails;
import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.GrnTableDataForPurchaseBooking;
import com.tanfed.inventry.model.NonCCInvoiceTableData;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.PurchaseBookingDto;
import com.tanfed.inventry.model.PurchaseTermsPricingTPM;
import com.tanfed.inventry.model.TermsDataForPurchaseBooking;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.model.Vouchers;
import com.tanfed.inventry.repository.PurchaseBookingRepo;
import com.tanfed.inventry.response.DataForPurchaseBooking;
import com.tanfed.inventry.utils.CodeGenerator;

@Service
public class PurchaseBookingServiceImpl implements PurchaseBookingService {

	@Autowired
	private MasterService masterService;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private PoService poService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private SupplierInvoiceService supplierInvoiceService;

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private PurchaseBookingRepo purchaseBookingRepo;

	private static Logger logger = LoggerFactory.getLogger(PurchaseBookingServiceImpl.class);

	@Override
	public ResponseEntity<String> savePurchaseBooking(PurchaseBookingDto obj, String jwt) throws Exception {
		try {
			PurchaseBooking pb = new PurchaseBooking();

			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			String code = codeGenerator.generateCheckMemoNo(obj.getPoNo());
			pb.setCheckMemoNo(code);
			pb.setVoucherStatus("Pending");
			pb.setEmpId(Arrays.asList(empId));
			pb.setIsCheckMemoCreated(false);
			pb.setActivity(obj.getActivity());
			pb.setProductCategory(obj.getProductCategory());
			pb.setSupplierName(obj.getSupplierName());
			pb.setProductName(obj.getProductName());
			pb.setProductGroup(obj.getProductGroup());
			pb.setPoType(obj.getPoType());
			pb.setPoMonth(obj.getPoMonth());
			pb.setPoNo(obj.getPoNo());
			pb.setStandardUnits(obj.getStandardUnits());
			pb.setPacking(obj.getPacking());
			pb.setTermsNo(obj.getTermsNo());
			pb.setTotalPoQty(obj.getTotalPoQty());
			pb.setGrnTableData(obj.getGrnTableData());
			pb.setTotalQty(obj.getTotalQty());
			pb.setDirectQty(obj.getDirectQty());
			pb.setBufferQty(obj.getBufferQty());
			pb.setInputTax(obj.getInputTax());
			pb.setMargin(obj.getMargin());
			pb.setDeduction(obj.getDeduction());
			pb.setNet(obj.getNet());
			pb.setTradeIncome(obj.getTradeIncome());
			pb.setTermsData(obj.getTermsData());
			pb.setTermsDataGeneral(obj.getTermsDataGeneral());
			pb.setTermsDataDirect(obj.getTermsDataDirect());
			pb.setTermsDataBuffer(obj.getTermsDataBuffer());
			pb.setDate(obj.getDate());
			Vouchers voucher = new Vouchers();
			List<String> jvNoList = new ArrayList<String>();
			obj.getJvData().forEach(item -> {
				voucher.setJournalVoucherData(item);
				try {
					ResponseEntity<String> responseEntity = accountsService
							.saveAccountsVouchersHandler("journalVoucher", voucher, jwt);
					String responseString = responseEntity.getBody();
					if(responseString == null) {
						throw new Exception("No data found");
					}
					String prefix = "JV Number : ";
					int index = responseString.indexOf(prefix);
					jvNoList.add(responseString.substring(index + prefix.length()).trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			pb.setJvList(jvNoList);
			obj.getGrnTableData().forEach(item -> {
				try {
					grnService.updatePurchaseBookingStatus(item.getGrnNo());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			purchaseBookingRepo.save(pb);
			return new ResponseEntity<String>("Created Successfully!" + "\n CheckMemo No :" + code, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForPurchaseBooking getDataForPurchaseBooking(String jwt, String activity, String productCategory,
			String supplierName, String productName, String poType, String poMonth, String poNo) throws Exception {
		try {
			DataForPurchaseBooking data = new DataForPurchaseBooking();
			List<ProductMaster> productDataHandler = masterService.getProductDataHandler(jwt);

			if (!activity.isEmpty() && activity != null) {
				data.setProductCategoryList(productDataHandler.stream().filter(
						item -> item.getActivity().equals(activity) && !item.getSupplierName().startsWith("TANFED"))
						.map(ProductMaster::getProductCategory).collect(Collectors.toSet()));

				if (!productCategory.isEmpty() && productCategory != null) {
					data.setSupplierNameList(productDataHandler.stream()
							.filter(item -> item.getActivity().equals(activity)
									&& item.getProductCategory().equals(productCategory))
							.map(ProductMaster::getSupplierName).collect(Collectors.toSet()));

					if (!supplierName.isEmpty() && supplierName != null) {
						data.setProductNameList(productDataHandler.stream()
								.filter(item -> item.getActivity().equals(activity)
										&& item.getProductCategory().equals(productCategory)
										&& item.getSupplierName().equals(supplierName))
								.map(ProductMaster::getProductName).collect(Collectors.toList()));

						if (!productName.isEmpty() && productName != null) {
							ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
									productName);
							data.setProductGroup(productMaster.getProductGroup());
							data.setPacking(productMaster.getPacking());
							data.setStandardUnits(productMaster.getStandardUnits());
							data.setSupplierGst(productMaster.getSupplierGst());
							List<PurchaseOrder> poDataHandler = poService.getPoData();

							data.setPoMonthList(
									poDataHandler.stream()
											.filter(item -> item.getProductName().equals(productName)
													&& item.getVoucherStatus().equals("Approved"))
											.map(PurchaseOrder::getDate)
											.map(date -> String.format("%s %04d",
													date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
													date.getYear()))
											.collect(Collectors.toSet()));

							if (!poMonth.isEmpty() && poMonth != null) {
								if (!poType.isEmpty() && poType != null) {
									List<String> poNoList = poDataHandler.stream().filter(item -> {
										String month = String.format("%s %04d", item.getDate().getMonth()
												.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
												item.getDate().getYear());
										boolean grnMatch = item.getGrnData().stream()
												.anyMatch(itemData -> itemData.getGrnQtyAvlForGrnAttach() == 0.0
														&& itemData.getIsPurchaseBooked().equals(false));
										return item.getProductName().equals(productName)
												&& item.getPurchaseOrderType().equals(poType) && month.equals(poMonth)
												&& item.getVoucherStatus().equals("Approved")
												&& item.getGrnData().size() > 0 && grnMatch;
									}).map(PurchaseOrder::getPoNo).collect(Collectors.toList());

									List<String> toRemove = new ArrayList<String>();
									poNoList.forEach(item -> {
										List<PurchaseBooking> purchaseBookingData = purchaseBookingRepo
												.findByPoNo(item);
										if (!purchaseBookingData.isEmpty()) {
											if (purchaseBookingData.get(0).getTotalPoQty() == purchaseBookingData
													.stream().mapToDouble(sum -> sum.getTotalQty()).sum()) {
												toRemove.add(item);
											}
										}
									});
									poNoList.removeAll(toRemove);
									data.setPoNoList(poNoList);

									if (!poNo.isEmpty() && poNo != null) {
										data.setBookedQty(purchaseBookingRepo.findByPoNo(poNo).stream()
												.mapToDouble(sum -> sum.getTotalQty()).sum());

										PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
										data.setTotalPoQty(purchaseOrder.getTableData().stream()
												.mapToDouble(item -> item.getPoIssueQty()).sum());

										data.setTermsNo(purchaseOrder.getTermsPrice().getTermsNo());
										TermsPrice priceMaster = termsPriceService
												.fetchTermsByTermsNo(purchaseOrder.getTermsPrice().getTermsNo());

										List<GrnTableDataForPurchaseBooking> grnData = purchaseOrder.getGrnData()
												.stream().filter(item -> item.getGrnQtyAvlForGrnAttach() == 0.0
														&& item.getIsPurchaseBooked().equals(false))
												.flatMap(item -> {
													try {
														return prepareGrnTableDataForPurchaseBooking(item).stream();
													} catch (Exception e) {
														e.printStackTrace();
														return Stream.empty();
													}
												}).collect(Collectors.toList());
										logger.info("{}", grnData);
//										if(grnData.isEmpty()) {
//											throw new Exception("Please Attach Grn with Supplier Invoice!");
//										}
										data.setGrnTableData(grnData);

										if (priceMaster.getB2bTermsAndConditions().getB2bCollectionMode()
												.equals("Through CC")) {
											data.setDirectQty(purchaseOrder.getGrnData().stream()
													.filter(item -> item.getGrnQtyAvlForGrnAttach() == 0.0
															&& item.getIsPurchaseBooked().equals(false)
															&& item.getGodownType().equals("Direct"))
													.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum());

											data.setBufferQty(purchaseOrder.getGrnData().stream()
													.filter(item -> item.getGrnQtyAvlForGrnAttach() == 0.0
															&& item.getIsPurchaseBooked().equals(false)
															&& !item.getGodownType().equals("Direct"))
													.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum());

											data.setTotalQty(data.getDirectQty() + data.getBufferQty());

											data.setTermsData(prepareTermsData(priceMaster, data.getTotalQty(),
													data.getDirectQty(), data.getBufferQty()));

											data.setTermsDataGeneral(priceMaster.getPurchaseDataGeneral().stream()
													.map(item -> new TermsDataForPurchaseBooking(null, item.getTerm(),
															item.getValue(), data.getTotalQty(),
															roundToTwoDecimalPlaces(
																	item.getValue() * data.getTotalQty()),
															null))
													.collect(Collectors.toList()));

											data.setTermsDataDirect(priceMaster.getPurchaseDataDirect().stream()
													.map(item -> new TermsDataForPurchaseBooking(null, item.getTerm(),
															item.getValue(), data.getDirectQty(),
															roundToTwoDecimalPlaces(
																	item.getValue() * data.getDirectQty()),
															null))
													.collect(Collectors.toList()));

											data.setTermsDataBuffer(priceMaster.getPurchaseDataBuffer().stream()
													.map(item -> new TermsDataForPurchaseBooking(null, item.getTerm(),
															item.getValue(), data.getBufferQty(),
															roundToTwoDecimalPlaces(
																	item.getValue() * data.getBufferQty()),
															null))
													.collect(Collectors.toList()));

											data.setInputTax(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getGstValue()
															* data.getTotalQty()));

											data.setMargin(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getMargin()
															* data.getTotalQty()
															+ priceMaster.getPurchaseTermsPricing().getGstOnMargin()
																	* data.getTotalQty()));

											data.setNet(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getBasicPrice()
															* data.getTotalQty() + data.getInputTax()));

											data.setDeduction(roundToTwoDecimalPlaces(
													((priceMaster.getPurchaseTermsPricing().getMargin()
															* data.getTotalQty())
															+ (priceMaster.getPurchaseTermsPricing().getGstOnMargin()
																	* data.getTotalQty()))
															/ 2));

											data.setTradeIncome(roundToTwoDecimalPlaces(fetchTradeIncome(priceMaster,
													data.getTotalQty(), data.getDirectQty(), data.getBufferQty())));
										} else {

											Map<String, Double> grnNoToInvoiceQtySum = data.getGrnTableData().stream()
													.collect(Collectors.toMap(GrnTableDataForPurchaseBooking::getGrnNo,
															grn -> grn.getNonCcInvoiceData() != null
																	? grn.getNonCcInvoiceData().stream()
																			.map(NonCCInvoiceTableData::getInvoiceQty)
																			.filter(Objects::nonNull)
																			.mapToDouble(Double::doubleValue).sum()
																	: 0.0,
															(existing, replacement) -> existing));

											data.setTotalQty(grnNoToInvoiceQtySum.entrySet().stream()
													.mapToDouble(item -> item.getValue()).sum());

											data.setDirectQty(data.getTotalQty());

											data.setTermsData(
													prepareTermsData(priceMaster, data.getTotalQty(), null, null));

											data.setInputTax(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getGstValue()
															* data.getTotalQty()));

											data.setMargin(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getMargin()
															* data.getTotalQty()
															+ priceMaster.getPurchaseTermsPricing().getGstOnMargin()
																	* data.getTotalQty()));

											data.setNet(roundToTwoDecimalPlaces(
													priceMaster.getPurchaseTermsPricing().getBasicPrice()
															* data.getTotalQty() + data.getInputTax()));

											data.setDeduction(roundToTwoDecimalPlaces(
													((priceMaster.getPurchaseTermsPricing().getMargin()
															* data.getTotalQty()) / 2)
															+ (priceMaster.getPurchaseTermsPricing().getGstOnMargin()
																	* data.getTotalQty())));
										}
									}
								}
							}
						}
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private Double fetchTradeIncome(TermsPrice terms, Double totalQty, Double directQty, Double bufferQty)
			throws Exception {
		try {
			double generalIncome = terms.getPurchaseDataGeneral().stream()
					.mapToDouble(item -> item.getValue() * totalQty).sum();

			double directIncome = terms.getPurchaseDataDirect().stream()
					.mapToDouble(item -> item.getValue() * directQty).sum();

			double bufferIncome = terms.getPurchaseDataBuffer().stream()
					.mapToDouble(item -> item.getValue() * bufferQty).sum();

			return generalIncome + directIncome + bufferIncome;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private List<TermsDataForPurchaseBooking> prepareTermsData(TermsPrice terms, Double totalQty, Double directQty,
			Double bufferQty) {
		List<TermsDataForPurchaseBooking> data = new ArrayList<TermsDataForPurchaseBooking>();
		PurchaseTermsPricingTPM purchaseTermsPricing = terms.getPurchaseTermsPricing();
		data.add(new TermsDataForPurchaseBooking(null, "Basic Price", purchaseTermsPricing.getBasicPrice(), totalQty,
				roundToTwoDecimalPlaces(purchaseTermsPricing.getBasicPrice() * totalQty), null));

		data.add(new TermsDataForPurchaseBooking(null, "GST", purchaseTermsPricing.getGstValue(), totalQty,
				roundToTwoDecimalPlaces(purchaseTermsPricing.getGstValue() * totalQty), null));

		data.add(new TermsDataForPurchaseBooking(null, "Margin", purchaseTermsPricing.getMargin(), totalQty,
				roundToTwoDecimalPlaces(purchaseTermsPricing.getMargin() * totalQty), null));

		data.add(new TermsDataForPurchaseBooking(null, "GST on Margin", purchaseTermsPricing.getGstOnMargin(), totalQty,
				roundToTwoDecimalPlaces(purchaseTermsPricing.getGstOnMargin() * totalQty), null));

		data.add(new TermsDataForPurchaseBooking(null, "Net Price", purchaseTermsPricing.getNetPrice(), totalQty,
				roundToTwoDecimalPlaces(purchaseTermsPricing.getNetPrice() * totalQty), null));

		return data;
	}

	private List<GrnTableDataForPurchaseBooking> prepareGrnTableDataForPurchaseBooking(GRN item) throws Exception {
		try {
			List<GrnTableDataForPurchaseBooking> data = new ArrayList<GrnTableDataForPurchaseBooking>();
			String suppliedTo = null;
			if (item.getGodownType().equals("Direct")) {
				suppliedTo = "Direct";
			} else {
				suppliedTo = "Buffer";
			}
			List<NonCCInvoiceTableData> nonCcInvoiceTable = prepareNonCcInvoiceTable(item);
			if (item.getSupplierInvoiceNo().contains(", ")) {
				String[] spInvNo = item.getSupplierInvoiceNo().split(", ");
				String[] invQty = item.getGrnAttachQtyString().split(", ");
				for (int i = 0; i < spInvNo.length; i++) {
					SupplierInvoiceDetails invoiceDetails = supplierInvoiceService
							.getSupplierInvoiceByInvoiceNumber(spInvNo[i]);
					data.add(new GrnTableDataForPurchaseBooking(null, item.getGodownName(), item.getGodownType(),
							item.getGrnNo(), item.getDate(), item.getMaterialSuppliedQuantity(), spInvNo[i],
							Double.parseDouble(invQty[i]), invoiceDetails.getInvoiceDate(), suppliedTo,
							item.getOfficeName(), nonCcInvoiceTable));
				}
			} else {
				SupplierInvoiceDetails invoiceDetails = supplierInvoiceService
						.getSupplierInvoiceByInvoiceNumber(item.getSupplierInvoiceNo());
				data.add(new GrnTableDataForPurchaseBooking(null, item.getGodownName(), item.getGodownType(),
						item.getGrnNo(), item.getDate(), item.getMaterialSuppliedQuantity(),
						item.getSupplierInvoiceNo(), item.getGrnAttachQty(), invoiceDetails.getInvoiceDate(),
						suppliedTo, item.getOfficeName(), nonCcInvoiceTable));
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private InvoiceService invoiceService;

	private List<NonCCInvoiceTableData> prepareNonCcInvoiceTable(GRN grn) throws Exception {
		try {
			return invoiceService.getInvoiceDataByOffficeName(grn.getOfficeName()).stream()
					.filter(item -> item.getCollectionMode().equals("Non CC") && item.getAdjReceiptNo() != null
							&& item.getTableData().stream()
									.anyMatch(itemData -> itemData.getGrnNo().equals(grn.getGrnNo())))
					.flatMap(item -> item.getTableData().stream()
							.filter(itemData -> grn.getGrnNo().equals(itemData.getGrnNo()))
							.map(itemData -> new NonCCInvoiceTableData(null, item.getIfmsId(),
									item.getNameOfInstitution(), item.getDistrict(), item.getInvoiceNo(),
									item.getDate(), itemData.getQty(), item.getNetInvoiceAdjustment(),
									item.getDateOfCollectionFromCcb().get(0))))
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private static double roundToTwoDecimalPlaces(double value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	@Override
	public PurchaseBooking getPurchaseBookedDataByCmNo(String checkMemoNo) throws Exception {
		try {
			PurchaseBooking purchaseBooking = purchaseBookingRepo.findByCheckMemoNo(checkMemoNo).get();
			if (purchaseBooking == null) {
				throw new Exception("No data found!");
			}
			return purchaseBooking;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<PurchaseBooking> findPurchaseBookedDataByActivity(String activity) throws Exception {
		try {
			if (activity != null && !activity.isEmpty()) {
				return purchaseBookingRepo.findByActivity(activity);
			} else {
				return purchaseBookingRepo.findAll();
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateAccJv(PurchaseBooking item, String jwt) throws Exception {
		item.getJvList().forEach(jvNo -> {
			try {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
				accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
						String.valueOf(jv.getJournalVoucherData().getId()), "journalVoucher", null), jwt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
