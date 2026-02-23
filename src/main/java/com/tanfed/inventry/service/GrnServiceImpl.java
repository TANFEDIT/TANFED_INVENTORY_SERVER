package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.ClosingStockTableRepo;
import com.tanfed.inventry.repository.GrnRepo;
import com.tanfed.inventry.repository.OpeningStockRepo;
import com.tanfed.inventry.repository.OutwardBatchRepo;
import com.tanfed.inventry.repository.PurchaseOrderRepo;
import com.tanfed.inventry.repository.SupplierInvoiceDetailsRepo;
import com.tanfed.inventry.response.DataForGrn;
import com.tanfed.inventry.response.DataForGrnUpdate;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class GrnServiceImpl implements GrnService {

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private GrnRepo grnRepo;

	@Autowired
	private MasterService masterService;

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private PoService poService;

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	@Autowired
	private OutwardBatchRepo outwardBatchRepo;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private OpeningStockService openingStockService;

	@Autowired
	ObjectMapper mapper;

	private static Logger logger = LoggerFactory.getLogger(GrnServiceImpl.class);

	@Override
	public ResponseEntity<String> saveGrn(GRN obj, String jwt) throws Exception {
		try {
			String grnNo = codeGenerator.grnNoGenerator(obj.getOfficeName(), obj.getDate());
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setGrnNo(grnNo);
			obj.setEmpId(Arrays.asList(empId));
			obj.setGrnQtyAvlForDc(obj.getMaterialReceivedQuantity());
			obj.setGrnQtyAvlForGrnAttach(obj.getMaterialReceivedQuantity());
			obj.setIsPurchaseBooked(false);
			PurchaseOrder purchaseOrder = poService.getPoByPoNo(obj.getPoNo());

			List<GRN> collect = purchaseOrder.getGrnData().stream().filter(
					item -> !item.getVoucherStatus().equals("Approved") && !item.getVoucherStatus().equals("Rejected"))
					.collect(Collectors.toList());

			if (!collect.isEmpty()) {
				throw new Exception("Please Approve the Previous GRN");
			}
			if (obj.getGodownType().equals("Railways Godown")) {
				WagonDataGrn wagonData = obj.getWagonData();
				wagonData.setWagonStatus("Pending");
				wagonData.setRatePerMT(fetchMrpFromPoNo(obj.getPoNo()));
			}
			if (purchaseOrder.getGrnData() == null) {
				purchaseOrder.setGrnData(Arrays.asList(obj));
			} else {
				purchaseOrder.getGrnData().add(obj);
			}
			purchaseOrderRepo.save(purchaseOrder);
			if (!obj.getGodownName().equals("Direct Material Center")) {
				calculateCharges(obj, jwt);
			}
			return new ResponseEntity<String>("Created Successfully!" + "\n GRN No :" + grnNo, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void calculateCharges(GRN obj, String jwt) throws Exception {
		try {
			GRN grn = grnRepo.findByGrnNo(obj.getGrnNo()).get();

			if (!obj.getUnloadingCharges().isEmpty() || !obj.getTransportCharges().isEmpty()) {
				ContractorInfo contractorInfo = masterService.getContractFirmByGodownNameHandler(jwt,
						obj.getOfficeName(), obj.getGodownName());
				ContractorChargesData contractorChargesData = contractorInfo.getChargesData()
						.get(contractorInfo.getChargesData().size() - 1);

				if (obj.getUnloadingCharges().equals("TANFED-H&T")) {
					grn.setUnloadingChargesValue(
							contractorChargesData.getUnloadingCharges() * obj.getMaterialReceivedQuantity());
				}

				if (obj.getGodownType().equals("Railways Godown")) {
					grn.setWagonClearanceValue(
							contractorChargesData.getWagonClearance() * obj.getMaterialReceivedQuantity());
				}

			}
			grnRepo.save(grn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editGrn(GRN obj, String jwt) throws Exception {
		try {
			GRN grn = grnRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			grn.getEmpId().add(empId);

			grnRepo.save(grn);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<GRN> getGrnDataByOffficeName(String officeName) throws Exception {
		try {
			return grnRepo.findByOfficeName(officeName);

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForGrn fetchDataForGrn(String officeName, String godownType, String activity, String productName,
			String poNo, String jwt, String godownName, LocalDate date, String month) throws Exception {
		try {
			DataForGrn data = new DataForGrn();
			if (!officeName.isEmpty() && officeName != null) {
				logger.info(month);
				if (month != null && !month.isEmpty()) {
					data.setWagonGrnList(
							getGrnDataByOffficeName(officeName)
									.stream().filter(
											item -> item.getGodownType().equals("Railways Godown")
													&& !item.getVoucherStatus().equals("Rejected")
													&& month.equals(String.format("%s%s%04d", item.getDate().getMonth(),
															" ", item.getDate().getYear())))
									.collect(Collectors.toList()));
				}
				if (!activity.isEmpty() && activity != null) {
					List<PurchaseOrder> poData = poService.getPoData().stream().filter(
							item -> item.getVoucherStatus().equals("Approved") && item.getActivity().equals(activity))
							.filter(item -> {
								double issuedQty = item.getTableData().stream()
										.filter(temp -> temp.getRegion().equals(officeName))
										.mapToDouble(PoTableData::getPoIssueQty).sum();

								double grnCreated = item.getGrnData().isEmpty() ? 0.0
										: item.getGrnData().stream()
												.filter(temp -> temp.getOfficeName().equals(officeName)
														&& temp.getVoucherStatus().equals("Approved"))
												.mapToDouble(temp -> temp.getMaterialReceivedQuantity()).sum();
								Boolean isDateMatch = !item.getDate().isBefore(date.minusDays(30));
								return issuedQty > grnCreated && isDateMatch;
							}).collect(Collectors.toList());
					if (poData.isEmpty()) {
						throw new Exception("No Po Available!");
					}
					data.setPoData(poData);

					data.setProductNameList(
							poData.stream().map(item -> item.getProductName()).collect(Collectors.toSet()));
					if (godownType != null && !godownType.isEmpty()) {
						data.setGodownNameList(getGodownNameList(jwt, officeName, godownType));
					}
					if (!godownName.isEmpty() && godownName != null && !godownName.equals("Direct Material Center")) {
						GodownInfo godownInfo = masterService.getGodownInfoByGodownNameHandler(godownName, jwt);
						data.setIfmsId(godownInfo.getIfmsId());

						if (!godownInfo.getGodownType().equals("Railways Godown")) {
							List<GodownInsuranceData> insuranceList = godownInfo.getInsurance();

							if (insuranceList == null || insuranceList.isEmpty()) {
								throw new Exception("Update Godown Insurance Data!");
							}

							GodownInsuranceData godownInsuranceData = insuranceList.get(insuranceList.size() - 1);

							if (date.isBefore(godownInsuranceData.getInsuranceFrom())
									|| date.isAfter(godownInsuranceData.getInsuranceTo())) {
								throw new Exception("Update Godown Insurance Data!");
							}

							List<LicenseData> licenseList = godownInfo.getLicense();

							if (licenseList == null || licenseList.isEmpty()) {
								throw new Exception("Update Godown License Data!");
							}

							Optional<LicenseData> optionalLicense = licenseList.stream().filter(
									i -> activity.equals(i.getLicenseFor()) && "Whole Sale".equals(i.getLicenseType()))
									.reduce((first, second) -> second);

							if (optionalLicense.isEmpty()) {
								throw new Exception("Update Godown License Data!");
							}

							LicenseData licenseData = optionalLicense.get();

							if (date.isBefore(licenseData.getValidFrom()) || date.isAfter(licenseData.getValidTo())) {
								throw new Exception("Update Godown License Data!");
							}
						}

					}
					if (!productName.isEmpty() && productName != null) {
						ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
								productName);
						data.setProductCategory(productMaster.getProductCategory());
						data.setProductGroup(productMaster.getProductGroup());
						data.setStandardUnits(productMaster.getStandardUnits());
						data.setSupplierGst(productMaster.getSupplierGst());
						data.setSupplierName(productMaster.getSupplierName());

						SupplierInfo supplierInfo = masterService.getSupplierInfoBySupplierNameHandler(jwt,
								productMaster.getSupplierName());

						data.setDoor(supplierInfo.getDoor());
						data.setStreet(supplierInfo.getStreet());
						data.setDistrict(supplierInfo.getDistrict());
						data.setPincode(supplierInfo.getPincode());
						data.setPacking(productMaster.getPacking());
						data.setBatchNo(productMaster.getBatchNo());
						data.setCertification(productMaster.getCertification());
						logger.info("{}", date);
						data.setPoNoList(poService.getUnfullfilledPoNo(productName, officeName, "grn", date));
						if (!poNo.isEmpty() && poNo != null) {
							PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
							purchaseOrder.getGrnData().forEach(item -> {
								if (item.getOfficeName().equals(officeName)
										&& (item.getVoucherStatus().equals("Pending")
												|| item.getVoucherStatus().equals("Verified"))) {
									data.setAlert(true);
								} else {
									data.setAlert(false);
								}
							});
							data.setTotalPoQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder
									.getTableData().stream().filter(item -> item.getRegion().equals(officeName))
									.mapToDouble(temp -> temp.getPoIssueQty()).sum()));

							data.setTotalBufferQty(
									RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getTableData().stream()
											.filter(item -> item.getRegion().equals(officeName)
													&& item.getIssuedFor().equals("Buffer"))
											.mapToDouble(temp -> temp.getPoIssueQty()).sum()));

							data.setTotalDirectQty(
									RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getTableData().stream()
											.filter(item -> item.getRegion().equals(officeName)
													&& item.getIssuedFor().equals("Direct"))
											.mapToDouble(temp -> temp.getPoIssueQty()).sum()));

							data.setTotalGrnCreated(
									RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getGrnData().stream()
											.filter(item -> item.getOfficeName().equals(officeName)
													&& item.getVoucherStatus().equals("Approved"))
											.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum()));

							data.setTotalBufferCreated(
									RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getGrnData().stream()
											.filter(item -> item.getOfficeName().equals(officeName)
													&& !item.getGodownType().equals("Direct")
													&& item.getVoucherStatus().equals("Approved"))
											.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum()));

							data.setTotalDirectCreated(
									RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getGrnData().stream()
											.filter(item -> item.getOfficeName().equals(officeName)
													&& item.getGodownType().equals("Direct")
													&& item.getVoucherStatus().equals("Approved"))
											.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum()));

							TermsPrice termsPrice = termsPriceService
									.fetchTermsByTermsNo(purchaseOrder.getTermsPrice().getTermsNo());
							data.setModeOfSupply(termsPrice.getPurchaseTermsAndCondition().getPurchaseModeofSupply());
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
	public List<GRN> getGrnDataByOffficeAndPoNo(String officeName, String poNo) throws Exception {
		try {
			return grnRepo.findByOfficeNameAndPoNo(officeName, poNo);

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateGrnQtyForDc(List<GrnQtyUpdateForDc> obj, String despatchAdviceNo)
			throws Exception {
		try {

			obj.forEach(temp -> {
				if (temp.getVoucherId().startsWith("GT")) {
					try {
						gtnService.updateGrnQtyForDc(temp, despatchAdviceNo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (temp.getOutwardBatchNo().startsWith("GR")) {
						GRN grn = grnRepo.findByGrnNo(temp.getOutwardBatchNo()).orElse(null);
						grn.setGrnQtyAvlForDc(
								RoundToDecimalPlace.roundToTwoDecimalPlaces(grn.getGrnQtyAvlForDc() - temp.getQty()));
						grnRepo.save(grn);
						try {
							if (temp.getDcNo() != null) {
								outwardBatchRepo.save(new OutwardBatch(null, LocalDateTime.now(), temp.getDcNo(),
										temp.getVoucherId(), temp.getOutwardBatchNo(), temp.getQty(),
										grn.getProductCategory(), grn.getProductGroup(), grn.getSupplierName(),
										grn.getProductName(), grn.getPacking(), grn.getStandardUnits(),
										fetchMrpFromPoNo(grn.getPoNo()), grn.getDate(), grn.getOfficeName(),
										grn.getGodownName(), despatchAdviceNo));
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						try {
							openingStockService.updateGrnQtyForDc(temp, despatchAdviceNo);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			});
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertGrnQtyForDc(List<GrnQtyUpdateForDc> obj) throws Exception {
		try {
			obj.forEach(temp -> {
				if (temp.getVoucherId().startsWith("GT")) {
					try {
						gtnService.revertGrnQtyForDc(temp);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					if (temp.getOutwardBatchNo().startsWith("GR")) {
						GRN grn = grnRepo.findByGrnNo(temp.getOutwardBatchNo()).orElse(null);
						grn.setGrnQtyAvlForDc(
								RoundToDecimalPlace.roundToTwoDecimalPlaces(grn.getGrnQtyAvlForDc() + temp.getQty()));
						grnRepo.save(grn);
					} else {
						try {
							openingStockService.revertGrnQtyForDc(temp);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	@Override
	public ResponseEntity<String> updateGrnAttachQty(GrnAttachDto obj) throws Exception {
		try {
			obj.getGrnNo().forEach(i -> {
				try {
					attachGrn(i, obj.getInvoiceNo());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void attachGrn(String grnNo, List<String> invoiceNoList) throws Exception {

		GRN grn = grnRepo.findByGrnNo(grnNo).orElseThrow(() -> new Exception("GRN not found: " + grnNo));

		for (String invNo : invoiceNoList) {

			if (Double.compare(grn.getGrnQtyAvlForGrnAttach(), 0.0) == 0) {
				break;
			}

			SupplierInvoiceDetails invoice = fetchSupplierInvoice(invNo);

			Double invoiceQty = invoice.getInvoiceQtyAvlForGrnAttach();
			if (Double.compare(invoiceQty, 0.0) == 0) {
				continue;
			}

			double attachQty = Math.min(grn.getGrnQtyAvlForGrnAttach(), invoiceQty);

			grn.setGrnAttachQty(RoundToDecimalPlace.roundToTwoDecimalPlaces(grn.getGrnAttachQty() + attachQty));
			grn.setGrnQtyAvlForGrnAttach(grn.getGrnQtyAvlForGrnAttach() - attachQty);

			appendString(grn::getGrnAttachQtyString, grn::setGrnAttachQtyString, attachQty);
			appendString(grn::getSupplierInvoiceNo, grn::setSupplierInvoiceNo, invNo);

			invoice.setInvoiceQtyAvlForGrnAttach(invoiceQty - attachQty);

			grnRepo.save(grn);
			supplierInvoiceDetailsRepo.save(invoice);
		}
	}

	private void appendString(Supplier<String> getter, Consumer<String> setter, Object value) {
		if (getter.get() == null) {
			setter.accept(value.toString());
		} else {
			setter.accept(getter.get() + ", " + value);
		}
	}

//	private void attachGrn(String grnNo, List<String> invoiceNoList) throws Exception {
//		try {
//			int index = 0;
//			SupplierInvoiceDetails supplierInvoiceDetails;
//			Double BookngQty;
//			String invoiceNo;
//			do {
//				supplierInvoiceDetails = fetchSupplierInvoice(invoiceNoList.get(index++));
//				BookngQty = supplierInvoiceDetails.getInvoiceQtyAvlForGrnAttach();
//				invoiceNo = supplierInvoiceDetails.getInvoiceNumber();
//
//			} while (supplierInvoiceDetails.getInvoiceQtyAvlForGrnAttach() == 0.0);
//			GRN grn = grnRepo.findByGrnNo(grnNo).get();
//			if (grn.getGrnQtyAvlForGrnAttach() <= BookngQty) {
//				if (grn.getGrnAttachQtyString() == null) {
//					grn.setGrnAttachQtyString(grn.getGrnQtyAvlForGrnAttach().toString());
//				} else {
//					grn.setGrnAttachQtyString(grn.getGrnAttachQtyString() + ", " + grn.getGrnQtyAvlForGrnAttach());
//				}
//				grn.setGrnAttachQty(RoundToDecimalPlace
//						.roundToTwoDecimalPlaces(grn.getGrnAttachQty() + grn.getGrnQtyAvlForGrnAttach()));
//				BookngQty -= grn.getGrnQtyAvlForGrnAttach();
//				grn.setGrnQtyAvlForGrnAttach(0.0);
//				if (grn.getSupplierInvoiceNo() == null) {
//					grn.setSupplierInvoiceNo(invoiceNo);
//				} else {
//					grn.setSupplierInvoiceNo(grn.getSupplierInvoiceNo() + ", " + invoiceNo);
//				}
//				grnRepo.save(grn);
//				supplierInvoiceDetails.setInvoiceQtyAvlForGrnAttach(BookngQty);
//				supplierInvoiceDetailsRepo.save(supplierInvoiceDetails);
//			} else {
//				if (grn.getGrnAttachQtyString() == null) {
//					grn.setGrnAttachQtyString(BookngQty.toString());
//				} else {
//					grn.setGrnAttachQtyString(grn.getGrnAttachQtyString() + ", " + BookngQty);
//				}
//				grn.setGrnAttachQty(RoundToDecimalPlace.roundToTwoDecimalPlaces(grn.getGrnAttachQty() + BookngQty));
//				grn.setGrnQtyAvlForGrnAttach(grn.getGrnQtyAvlForGrnAttach() - BookngQty);
//				if (grn.getSupplierInvoiceNo() == null) {
//					grn.setSupplierInvoiceNo(invoiceNo);
//				} else {
//					grn.setSupplierInvoiceNo(grn.getSupplierInvoiceNo() + ", " + invoiceNo);
//				}
//				grnRepo.save(grn);
//				BookngQty = 0.0;
//				supplierInvoiceDetails.setInvoiceQtyAvlForGrnAttach(BookngQty);
//				supplierInvoiceDetailsRepo.save(supplierInvoiceDetails);
//				attachGrn(grnNo, invoiceNoList);
//			}
//
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//
//	}

	private SupplierInvoiceDetails fetchSupplierInvoice(String invoiceNo) throws Exception {
		try {
			return supplierInvoiceDetailsRepo.findByInvoiceNumber(invoiceNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

//	@Override
//	public ResponseEntity<String> updateGrnAttachQty(GrnAttachDto obj) throws Exception {
//
//		try {
//
//			Double bookingQty = obj.getCurrentBookingQty();
//
//			for (String temp : obj.getGrnNo()) {
//
//				if (bookingQty <= 0)
//					break;
//
//				GRN grn = grnRepo.findByGrnNo(temp).orElseThrow(() -> new RuntimeException("GRN not found : " + temp));
//
//				Double available = grn.getGrnQtyAvlForGrnAttach();
//				Double attachQty = Math.min(available, bookingQty);
//
//				// Attach qty string
//				if (grn.getGrnAttachQtyString() == null) {
//					grn.setGrnAttachQtyString(attachQty.toString());
//				} else {
//					grn.setGrnAttachQtyString(grn.getGrnAttachQtyString() + ", " + attachQty);
//				}
//
//				// Update quantities
//				grn.setGrnAttachQty(RoundToDecimalPlace.roundToTwoDecimalPlaces(grn.getGrnAttachQty() + attachQty));
//
//				grn.setGrnQtyAvlForGrnAttach(RoundToDecimalPlace.roundToTwoDecimalPlaces(available - attachQty));
//
//				bookingQty -= attachQty;
//
//				// Supplier invoice
//				if (grn.getSupplierInvoiceNo() == null) {
//					grn.setSupplierInvoiceNo(obj.getInvoiceNo());
//				} else {
//					grn.setSupplierInvoiceNo(grn.getSupplierInvoiceNo() + ", " + obj.getInvoiceNo());
//				}
//
//				grnRepo.save(grn);
//
//				logger.info("Remaining Booking Qty : {}", bookingQty);
//			}
//
//			supplierInvoiceService.updateSupplierInvoiceQtyForGrnAttach(obj.getInvoiceNo(), bookingQty);
//
//			return new ResponseEntity<>("Updated Successfully!", HttpStatus.ACCEPTED);
//
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//	}

	@Override
	public GRN getGrnDataByGrnNo(String grnNo) throws Exception {
		try {
			return grnRepo.findByGrnNo(grnNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public Set<String> getGodownNameList(String jwt, String officeName, String godownType) throws Exception {
		List<ContractorInfo> contractorInfoList = masterService.getContarctorInfoByOfficeName(jwt, officeName).stream()
				.filter(item -> "Active".equals(item.getStatus())).collect(Collectors.toList());

		Set<String> list = contractorInfoList.stream()
				.flatMap(item -> Stream.concat(item.getGodownName().stream(),
						item.getAdditionalGodownData().stream().flatMap(add -> add.getAdditionalGodown().stream())))
				.collect(Collectors.toSet());

		Set<String> godownNameList = masterService.getGodownInfoByOfficeNameHandler(jwt, officeName).stream()
				.filter(item -> (("Railways Godown".equals(godownType) && item.getGodownType().equals(godownType))
						|| (!"Railways Godown".equals(godownType) && !item.getGodownType().equals("Railways Godown")))
						|| godownType.isEmpty())
				.map(item -> item.getGodownName()).collect(Collectors.toSet());

		list.forEach(item -> {
			if (!godownNameList.contains(item)) {
				godownNameList.remove(item);
			}
		});
		return godownNameList;
	}

	@Override
	public List<TableDataForDc> grnTableData(String officeName, String productName, String godownName, String page)
			throws Exception {
		return getGrnDataByOffficeName(officeName).stream()
				.filter(item -> item.getProductName().equals(productName) && item.getGodownName().equals(godownName)
						&& item.getVoucherStatus().equals("Approved") && item.getGrnQtyAvlForDc() > 0)
				.filter(item -> {
					if (page.equals("gtn")) {
						try {
							String collectionModeFromPo = fetchCollectionModeFromPo(item.getPoNo());
							return collectionModeFromPo.equals("Through CC");
						} catch (Exception e) {
							e.printStackTrace();
							return false;
						}
					} else {
						return true;
					}
				}).map(item -> {
					try {
						return new TableDataForDc(item.getProductCategory(), item.getProductGroup(),
								item.getSupplierName(), item.getProductName(), item.getPacking(),
								item.getStandardUnits(),
								RoundToDecimalPlace.roundToThreeDecimalPlaces(item.getGrnQtyAvlForDc()),
								item.getGrnNo(), poService.getPoByPoNo(item.getPoNo()).getTermsPrice().getTermsNo(),
								fetchCollectionModeFromPo(item.getPoNo()), fetchMrpFromPoNo(item.getPoNo()),
								item.getDate(), "grn");
					} catch (Exception e) {
						e.printStackTrace();
						throw new InputMismatchException();
					}
				}).collect(Collectors.toList());
	}

	public Double fetchMrpFromPoNo(String poNo) throws Exception {
		return termsPriceService.fetchTermsByTermsNo(poService.getPoByPoNo(poNo).getTermsPrice().getTermsNo())
				.getB2cPrice().getB2cMrp();
	}

	public String fetchCollectionModeFromPo(String poNo) throws Exception {
		return poService.getPoByPoNo(poNo).getTermsPrice().getB2bTermsAndConditions().getB2bCollectionMode();
	}

	@Override
	public DataForGrnUpdate getDataForGrnUpdate(String officeName, String activity, String grnNo, String month)
			throws Exception {
		try {
			DataForGrnUpdate data = new DataForGrnUpdate();

			if (officeName != null && !officeName.isEmpty()) {
				if (month != null && !month.isEmpty()) {
					logger.info(month);
					data.setUpdatedGrnData(grnRepo.findByOfficeName(officeName).stream().filter(temp -> {
						Boolean conditionsMatch = temp.getFirstPointIfmsId() != null && temp.getIdCreateDate() != null
								&& temp.getAckDate() != null && temp.getIfmsStatus() != null;
						String ackMonth = null;
						if (temp.getAckDate() != null) {
							ackMonth = String.format("%s%s%04d",
									temp.getAckDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), " ",
									temp.getAckDate().getYear());
						}

						return conditionsMatch && month.equals(ackMonth);
					}).collect(Collectors.toList()));
				}
				if (activity != null && !activity.isEmpty()) {
					List<GRN> byOffice = grnRepo.findByOfficeName(officeName);
					List<String> grnNoList = byOffice.stream().filter(temp -> {
						Boolean conditionsMatch = temp.getFirstPointIfmsId() == null && temp.getIdCreateDate() == null
								&& temp.getAckDate() == null && temp.getIfmsStatus() == null;
						return conditionsMatch && temp.getActivity().equals(activity)
								&& temp.getGrnIfmsId().equals("YES");
					}).map(GRN::getGrnNo).collect(Collectors.toList());
					data.setGrnNoList(grnNoList);
				}
			}
			if (grnNo != null && !grnNo.isEmpty()) {
				GRN grn = grnRepo.findByGrnNo(grnNo).get();
				data.setGrnData(grn);
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateGrn(GRN obj) throws Exception {
		try {

			GRN grn = grnRepo.findByGrnNo(obj.getGrnNo()).get();

			grn.setFirstPointIfmsId(obj.getFirstPointIfmsId());
			grn.setIdCreateDate(obj.getIdCreateDate());
			grn.setAckDate(obj.getAckDate());
			grn.setIfmsStatus(obj.getIfmsStatus());

			grnRepo.save(grn);
			return new ResponseEntity<String>("GRN Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateWagonData(WagonDataGrn obj, String grnNo, String jwt) throws Exception {
		try {
			GRN grn = grnRepo.findByGrnNo(grnNo).get();
			if (grn.getVoucherStatus().equals("Approved")) {
				WagonDataGrn wagonData = grn.getWagonData();
				wagonData.setPossibleClearanceEndDate(obj.getPossibleClearanceEndDate());
				wagonData.setActualReceiptQty(obj.getActualReceiptQty());
				wagonData.setActualReceiptBags(obj.getActualReceiptBags());
				wagonData.setExcessOrShortageQty(obj.getExcessOrShortageQty());
				wagonData.setShortageAcceptedBy(obj.getShortageAcceptedBy());
				wagonData.setCalcValue(obj.getCalcValue());
				ContractorInfo contractorInfo = masterService.getContractFirmByGodownNameHandler(jwt,
						grn.getOfficeName(), grn.getGodownName());
				ContractorChargesData contractorChargesData = contractorInfo.getChargesData()
						.get(contractorInfo.getChargesData().size() - 1);
				grn.setWagonClearanceValue(contractorChargesData.getWagonClearance() * obj.getActualReceiptQty());
				grnRepo.save(grn);
			} else {
				throw new Exception("Approve GRN Data");
			}
			return new ResponseEntity<String>("GRN Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateJv(String grnNo, String jv) throws Exception {
		try {
			GRN grn = grnRepo.findByGrnNo(grnNo).get();
			grn.setJvNo(jv);
			grnRepo.save(grn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updatePurchaseBookingStatus(String grnNo) throws Exception {
		try {
			GRN grn = grnRepo.findByGrnNo(grnNo).get();
			grn.setIsPurchaseBooked(true);
			grnRepo.save(grn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertPurchaseBookingStatus(List<GrnTableDataForPurchaseBooking> grn) throws Exception {
		try {
			grn.forEach(item -> {
				GRN grnData = grnRepo.findByGrnNo(item.getGrnNo()).get();
				grnData.setIsPurchaseBooked(false);
				grnRepo.save(grnData);
			});

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void saveGrn(GRN grn) throws Exception {
		try {
			grnRepo.save(grn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private ClosingStockTableRepo closingStockTableRepo;

	@Override
	public void updateClosingBalance(GRN grn) throws Exception {
		try {
			ClosingStockTable cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(
					grn.getOfficeName(), grn.getProductName(), grn.getDate(), grn.getGodownName());
			if (cb == null) {
				int n = 1;
				while (cb == null) {
					LocalDate date = grn.getDate().minusDays(n++);
					cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(grn.getOfficeName(),
							grn.getProductName(), date, grn.getGodownName());
					if (date.equals(LocalDate.of(2025, 3, 30))) {
						closingStockTableRepo.save(new ClosingStockTable(null, grn.getOfficeName(), grn.getDate(),
								grn.getProductName(), grn.getGodownName(), grn.getMaterialReceivedQuantity()));
						break;
					}
				}
				if (cb != null) {
					closingStockTableRepo
							.save(new ClosingStockTable(null, grn.getOfficeName(), grn.getDate(), grn.getProductName(),
									grn.getGodownName(), cb.getBalance() + grn.getMaterialReceivedQuantity()));
				}
			} else {
				cb.setBalance(cb.getBalance() + grn.getMaterialReceivedQuantity());
				closingStockTableRepo.save(cb);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertGrnJv(GRN grn, String jwt, String grnNo) throws Exception {
		try {
			if (grnNo != null) {
				GRN grnData = grnRepo.findByGrnNo(grnNo).get();
				grnData.setJvNo(null);
				grnRepo.save(grnData);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public GRN getGrnDataByRrNo(String rrNo) throws Exception {
		try {
			return grnRepo.findByDcWdnRoNo(rrNo);
		} catch (Exception e) {
			throw new Exception("Error Fetching GRN by RR number" + e);
		}
	}

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Override
	public void utilizeGrnQtyForGtn(GtnTableData tableData) throws Exception {
		try {
			if (tableData.getOutwardBatchNo().startsWith("GR")) {
				GRN grnData = grnRepo.findByGrnNo(tableData.getOutwardBatchNo()).get();
				grnData.setGrnQtyAvlForDc(grnData.getGrnQtyAvlForDc() - tableData.getQty());
				grnRepo.save(grnData);
			} else {
				OpeningStock openingStock = openingStockRepo.findByObId(tableData.getOutwardBatchNo()).get();
				openingStock.setQtyAvlForDc(openingStock.getQtyAvlForDc() - tableData.getQty());
				openingStockRepo.save(openingStock);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
