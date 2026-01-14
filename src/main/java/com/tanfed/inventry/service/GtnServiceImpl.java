package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.ClosingStockTable;
import com.tanfed.inventry.entity.DespatchAdvice;
import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.entity.GTN;
import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.entity.OutwardBatch;
import com.tanfed.inventry.entity.SalesReturn;
import com.tanfed.inventry.model.BuyerFirmInfo;
import com.tanfed.inventry.model.ContractorChargesData;
import com.tanfed.inventry.model.ContractorInfo;
import com.tanfed.inventry.model.GodownInfo;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.model.GtnInvoiceData;
import com.tanfed.inventry.model.GtnTableData;
import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.TableDataForDc;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.model.Vouchers;
import com.tanfed.inventry.repository.ClosingStockTableRepo;
import com.tanfed.inventry.repository.GtnRepo;
import com.tanfed.inventry.repository.OutwardBatchRepo;
import com.tanfed.inventry.repository.SalesReturnRepo;
import com.tanfed.inventry.response.DataForGtn;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;
import com.tanfed.inventry.utils.SlabRateCalculator;

@Service
public class GtnServiceImpl implements GtnService {

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private GtnRepo gtnRepo;

	@Autowired
	private SalesReturnRepo salesReturnRepo;

	@Autowired
	private UserService userService;

	@Autowired
	private OutwardBatchRepo outwardBatchRepo;

	private static Logger logger = LoggerFactory.getLogger(GtnServiceImpl.class);

	@Override
	public ResponseEntity<String> saveGtn(GTN obj, String jwt, SalesReturn salesReturn) throws Exception {
		try {
			if (obj != null) {
				String gtnNo = codeGenerator.gtnNoGenerator(obj.getOfficeName(), obj.getDate());
				String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
				logger.info(empId);
				obj.setGtnNo(gtnNo);
				obj.setVoucherStatus("Pending");
				obj.setEmpId(Arrays.asList(empId));
				obj.getGtnTableData().forEach(item -> {
					item.setGtn(obj);
				});
				if(obj.getGtnFor().equals("Issue")) {
					utilizeGtnReceiptQtyForIssue(obj.getGtnTableData());
				}
				if (!obj.getTransactionFor().equals("Sales Return")) {
					obj.getGtnTableData().forEach(temp -> {
						temp.setQtyAvlForDc(temp.getReceivedQty());
					});
				}
//			GTN gtn = calculateCharges(obj);
				gtnRepo.save(obj);
				return new ResponseEntity<String>("Created Successfully!" + "\n GTN No :" + gtnNo, HttpStatus.CREATED);
			} else {
				return saveSalesReturn(salesReturn, jwt);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void utilizeGtnReceiptQtyForIssue(List<GtnTableData> tableData) throws Exception {
		try {
			for (var i : tableData) {
				if (i.getVoucherId().startsWith("GT")) {
					GTN gtn = gtnRepo.findByGtnNo(i.getVoucherId()).get();
					for (var item : gtn.getGtnTableData()) {
						if (item.getOutwardBatchNo().equals(i.getOutwardBatchNo())) {
							item.setQtyAvlForDc(
									RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getQtyAvlForDc() - i.getQty()));
						}
					}
					gtnRepo.save(gtn);
				} else {
					grnService.utilizeGrnQtyForGtn(i);
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> saveSalesReturn(SalesReturn obj, String jwt) throws Exception {
		try {
			String gtnNo = codeGenerator.gtnNoGenerator(obj.getOfficeName(), obj.getDate());
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			logger.info(empId);
			obj.setGtnNo(gtnNo);
			obj.setVoucherStatus("Pending");
			obj.setEmpId(Arrays.asList(empId));

			obj.getInvoiceTableData().forEach(temp -> {
				temp.setSalesReturn(obj);
			});

			salesReturnRepo.save(obj);
			updateQty(obj.getInvoiceTableData());
			return new ResponseEntity<String>("Created Successfully!" + "\n GTN No :" + gtnNo, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void updateQty(List<GtnInvoiceData> invoiceTableData) throws Exception {
		try {
			invoiceTableData.forEach(item -> {
				try {
					if (item.getGrnNo().startsWith("GR")) {
						GRN grn = grnService.getGrnDataByGrnNo(item.getGrnNo());
						grn.setGrnQtyAvlForDc(RoundToDecimalPlace
								.roundToTwoDecimalPlaces(grn.getGrnQtyAvlForDc() + item.getReturnQty()));
						grnService.saveGrn(grn);
					} else {
						GTN gtn = gtnRepo.findByGtnNo(item.getGrnNo()).get();
						double qty = item.getReturnQty();
						for (var temp : gtn.getGtnTableData()) {
							if (qty <= 0)
								break;
							double available = temp.getQtyAvlForDc();
							if (available >= qty) {
								temp.setQtyAvlForDc(RoundToDecimalPlace.roundToTwoDecimalPlaces(available + qty));
								qty = 0;
							} else {
								temp.setQtyAvlForDc(0.0);
								qty -= available;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}

	}

	@Override
	public ResponseEntity<String> editGtn(GTN obj, String jwt) throws Exception {
		try {
			GTN gtn = gtnRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			gtn.getEmpId().add(empId);

			gtnRepo.save(gtn);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<GTN> getGtnDataByOffficeName(String officeName) throws Exception {
		try {
			return gtnRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private MasterService masterService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private DcService dcService;

	@Autowired
	private OpeningStockService openingStockService;

	@Override
	public DataForGtn getDataForGtn(String officeName, String productName, String activity, String gtnFor, String rrNo,
			LocalDate date, String transactionFor, String jwt, String godownName, String toRegion, String issuedGtnNo,
			String destination, String transportCharges, String loadingCharges, String unloadingCharges, String month,
			String suppliedGodown, String invoiceNo, String daNo) throws Exception {
		try {
			DataForGtn data = new DataForGtn();
			logger.info(daNo);
			switch (gtnFor) {
			case "Issue":
				handleIssueGTN(data, officeName, productName, activity, transactionFor, jwt, godownName, toRegion, date,
						destination, transportCharges, loadingCharges, unloadingCharges, rrNo, daNo);
				break;
			case "Receipt":
				handleReceiptGTN(data, officeName, transactionFor, issuedGtnNo, month, suppliedGodown, invoiceNo, jwt,
						godownName);
				break;
			}
			if (hasText(month)) {
				data.setGtnSalesReturn(
						salesReturnRepo.findByOfficeName(officeName).stream()
								.filter(item -> item.getJvNo() == null && String
										.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
										.equals(month))
								.collect(Collectors.toList()));
			}
			return data;
		} catch (Exception e) {
			throw new Exception("Error in getDataForGtn: " + e.getMessage(), e);
		}
	}

	private boolean hasText(String str) {
		return str != null && !str.trim().isEmpty();
	}

	private Set<String> fetchProduct(String officeName) throws Exception {
		Set<String> productNames;
		productNames = grnService.getGrnDataByOffficeName(officeName).stream()
				.filter(item -> "Approved".equals(item.getVoucherStatus()) && item.getGrnQtyAvlForDc() > 0)
				.map(item -> item.getProductName()).collect(Collectors.toSet());
		if (productNames.isEmpty()) {
			productNames = openingStockService.getOpeningStockByOfficeName(officeName).stream()
					.filter(item -> item.getQtyAvlForDc() > 0).map(item -> item.getProductName())
					.collect(Collectors.toSet());
		}
		return productNames;
	}

	private void handleIssueGTN(DataForGtn data, String officeName, String productName, String activity,
			String transactionFor, String jwt, String godownName, String toRegion, LocalDate date, String destination,
			String transportCharges, String loadingCharges, String unloadingCharges, String rrNo, String daNo)
			throws Exception {

		data.setProductNameList(fetchProduct(officeName));

		if (hasText(productName)) {
			populateProductDetails(data, jwt, productName);
		}

		if (!hasText(transactionFor))
			return;
		if (transactionFor.contains("RH")) {
			Set<String> godownNameList = masterService.getGodownInfoByOfficeNameHandler(jwt, officeName).stream()
					.filter(item -> item.getGodownType().equals("Railways Godown")).map(item -> item.getGodownName())
					.collect(Collectors.toSet());
			Set<String> list = grnService.getGodownNameList(jwt, officeName, "");

			list.forEach(item -> {
				if (!godownNameList.contains(item)) {
					godownNameList.remove(item);
				}
			});
			data.setGodownNameList(godownNameList);
		} else {
			data.setGodownNameList(grnService.getGodownNameList(jwt, officeName, ""));
		}

		if (hasText(productName) && hasText(godownName)) {
			data.setRrNoList(grnService.getGrnDataByOffficeName(officeName).stream().filter(
					item -> productName.equals(item.getProductName()) && godownName.equals(item.getGodownName()))
					.map(GRN::getDcWdnRoNo).collect(Collectors.toList()));
			if (hasText(rrNo)) {
				data.setRrDate(grnService.getGrnDataByRrNo(rrNo).getSupplierDocDate());
			}
			data.setTableData(grnService.grnTableData(officeName, productName, godownName, "gtn"));
			data.getTableData().addAll(gtnTableData(officeName, productName, godownName));
			data.getTableData().addAll(dcService.getObData(officeName, productName, godownName));
		}

		handleIntraRegionTransfers(data, transactionFor, godownName, date, jwt, officeName, destination);

		data.setOfficeList(
				userService.getOfficeList().stream().map(item -> item.getOfficeName()).collect(Collectors.toList()));

		if (hasText(toRegion)) {
			handleInterRegionTransfers(data, officeName, toRegion, transactionFor, destination, godownName, jwt, daNo);
		}

		if ((hasText(godownName) && hasText(destination)) || hasText(toRegion)) {
			validateChargesNeed(data, transactionFor, jwt, officeName, godownName, destination, transportCharges,
					loadingCharges, unloadingCharges);
		}
	}

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	private void handleReceiptGTN(DataForGtn data, String officeName, String transactionFor, String issuedGtnNo,
			String month, String suppliedGodown, String invoiceNo, String jwt, String godownName) throws Exception {
		if (hasText(transactionFor)) {

			if (transactionFor.equals("Sales Return") && hasText(month)) {
				List<Invoice> invoiceList = invoiceService.getInvoiceDataByOffficeName(officeName).stream()
						.filter(item -> {
							return String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
									.equals(month) && item.getVoucherStatus().equals("Approved");
						}).collect(Collectors.toList());
				Invoice invoice = null;
				data.setSuppliedGodownNameList(
						invoiceList.stream().map(item -> item.getGodownName()).collect(Collectors.toSet()));
				if (hasText(suppliedGodown)) {
					data.setInvoiceNoList(
							invoiceList.stream().map(item -> item.getInvoiceNo()).collect(Collectors.toList()));
					if (hasText(invoiceNo)) {
						invoice = invoiceService.getInvoiceDataByInvoiceNo(invoiceNo);
						if (invoice == null) {
							throw new Exception("No Invoice found");
						}
						data.setInvoice(invoice);
					}
				}
				data.setGodownNameList(grnService.getGodownNameList(jwt, officeName, ""));
				if (hasText(godownName)) {
					ContractorInfo contractorInfo = getContractor(jwt, officeName, godownName);
					ContractorChargesData contractorChargesData = contractorInfo.getChargesData()
							.get(contractorInfo.getChargesData().size() - 1);
					if (invoice == null) {
						throw new Exception("No Invoice found");
					}
					calculateCharges(data, jwt, officeName, godownName, invoice.getNameOfInstitution(),
							contractorChargesData);
					data.setTransporterName(contractorInfo.getContractFirm());
					data.setUnloadingChargesPerQty(contractorChargesData.getUnloadingCharges());
				}
			}
			List<String> gtnNoList = new ArrayList<>();
			if (transactionFor.contains("Other Region")) {
				gtnNoList = fetchGtnNoList(gtnRepo.findByToRegion(officeName), transactionFor, officeName);
			} else {
				gtnNoList = fetchGtnNoList(getGtnDataByOffficeName(officeName), transactionFor, officeName);
			}
			data.setGtnNoList(gtnNoList);
		}

		if (hasText(issuedGtnNo)) {
			Optional<GTN> gtnOpt = gtnRepo.findByGtnNo(issuedGtnNo);
			if (gtnOpt.isPresent()) {
				GTN gtn = gtnOpt.get();
				data.setGtnData(gtn);
				ContractorInfo contractor = getContractor(jwt, gtn.getOfficeName(), gtn.getGodownName());
				ContractorChargesData lastCharge = contractor.getChargesData()
						.get(contractor.getChargesData().size() - 1);
				data.setUnloadingChargesPerQty(lastCharge.getUnloadingCharges());
			}
		}
	}

	private void populateProductDetails(DataForGtn data, String jwt, String productName) throws Exception {
		ProductMaster product = masterService.getProductDataByProductNameHandler(jwt, productName);
		data.setProductCategory(product.getProductCategory());
		data.setProductGroup(product.getProductGroup());
		data.setStandardUnits(product.getStandardUnits());
		data.setSupplierGst(product.getSupplierGst());
		data.setSupplierName(product.getSupplierName());
		data.setPacking(product.getPacking());
	}

	private void handleIntraRegionTransfers(DataForGtn data, String transactionFor, String godownName, LocalDate date,
			String jwt, String officeName, String destination) throws Exception {
		if (transactionFor.equals("RH to Buffer (Intra)") || transactionFor.equals("Buffer To Buffer (Intra)")
				|| transactionFor.equals("Wholesale To Retail (Intra)")) {
			if (hasText(godownName)) {
				data.setDesignationList(grnService.getGodownNameList(jwt, officeName, "").stream()
						.filter(item -> !item.equals(godownName)).collect(Collectors.toSet()));

				GodownInfo godownInfo = masterService.getGodownInfoByGodownNameHandler(godownName, jwt);
				validateGodownInsurance(godownInfo, date);
				data.setFromIfmsId(godownInfo.getIfmsId());

				if (hasText(destination)) {
					GodownInfo toGodown = masterService.getGodownInfoByGodownNameHandler(destination, jwt);
					data.setToIfmsId(toGodown.getIfmsId());
				}
			}
		}
	}

	private void handleInterRegionTransfers(DataForGtn data, String officeName, String toRegion, String transactionFor,
			String destination, String godownName, String jwt, String daNo) throws Exception {
		String originalOffice = officeName;
		officeName = toRegion;

		if (transactionFor.contains("Other Region Buffer")) {
			data.setDesignationList(grnService.getGodownNameList(jwt, officeName, ""));

			if (hasText(destination)) {
				GodownInfo toGodown = masterService.getGodownInfoByGodownNameHandler(destination, jwt);
				GodownInfo fromGodown = masterService.getGodownInfoByGodownNameHandler(godownName, jwt);
				data.setToIfmsId(toGodown.getIfmsId());
				data.setFromIfmsId(fromGodown.getIfmsId());
			}

		} else if (transactionFor.contains("Other Region Direct")) {
			data.setDesignationList(new HashSet<>(masterService.getBuyerNameByOfficeNameHandler(jwt, officeName)));

			if (hasText(destination)) {
				BuyerFirmInfo buyer = masterService.getBuyerFirmByFirmNameHandler(jwt, destination);
				data.setBuyerName(buyer.getNameOfInstitution());
				data.setBuyerDistrict(buyer.getDistrict());
				data.setBuyerTaluk(buyer.getTaluk());
				data.setBuyerBlock(buyer.getBlock());
				data.setBuyerVillage(buyer.getVillage());
				data.setToIfmsId(buyer.getIfmsIdNo());
				data.setBuyerGstNo(buyer.getBuyerGstNo());
				data.setDaNoList(despatchAdviceService.fetchOtherRegionDaNoList(originalOffice, officeName,
						buyer.getNameOfInstitution()));
				if (hasText(daNo)) {
					DespatchAdvice despatchAdvice = despatchAdviceService.getDespatchAdviceDataByDespatchAdviceNo(daNo);
					data.setDaProduct(despatchAdvice.getTableData().get(0).getProductName());
					data.setDaQty(despatchAdvice.getTableData().get(0).getQty().toString());
				}
				GodownInfo fromGodown = masterService.getGodownInfoByGodownNameHandler(godownName, jwt);
				data.setFromIfmsId(fromGodown.getIfmsId());
			}
		}

		officeName = originalOffice;
	}

	private void validateGodownInsurance(GodownInfo godownInfo, LocalDate date) throws Exception {
		if (date.isBefore(godownInfo.getInsuranceFrom()) || date.isAfter(godownInfo.getInsuranceTo())
				|| date.isBefore(godownInfo.getValidityFrom()) || date.isAfter(godownInfo.getValidityTo())) {
			throw new Exception("Update Godown License and Insurance Data!");
		}
	}

	private List<String> fetchGtnNoList(List<GTN> gtn, String transactionFor, String officeName) throws Exception {
		List<String> gtnNoList = gtn.stream()
				.filter(item -> item.getGtnFor().equals("Issue") && item.getVoucherStatus().equals("Approved")
						&& item.getTransactionFor().equals(transactionFor))
				.map(item -> item.getGtnNo()).collect(Collectors.toList());

		getGtnDataByOffficeName(officeName).forEach(item -> {
			if (gtnNoList.contains(item.getIssuedGtnNo())) {
				gtnNoList.remove(item.getIssuedGtnNo());
			}
		});
		return gtnNoList;
	}

	private ContractorInfo getContractor(String jwt, String officeName, String godownName) throws Exception {
		return masterService.getContractFirmByGodownNameHandler(jwt, officeName, godownName);
	}

	private void validateChargesNeed(DataForGtn data, String transactionFor, String jwt, String officeName,
			String godownName, String destination, String transportCharges, String loadingCharges,
			String unloadingCharges) throws Exception {

		ContractorInfo contractorInfo = getContractor(jwt, officeName, godownName);
		ContractorChargesData contractorChargesData = contractorInfo.getChargesData()
				.get(contractorInfo.getChargesData().size() - 1);
		data.setTransporterName(contractorInfo.getContractFirm());
		if (transactionFor.equals("RH to Buffer (Intra)")) {
			calculateCharges(data, jwt, officeName, godownName, destination, contractorChargesData);
		} else if (transactionFor.equals("Buffer To Buffer (Intra)")
				|| transactionFor.equals("RH To Other Region Buffer")
				|| transactionFor.equals("RH To Other Region Direct")) {
			if (transportCharges.equals("TANFED")) {
				calculateCharges(data, jwt, officeName, godownName, destination, contractorChargesData);
			}
			if (loadingCharges.equals("TANFED")) {
				data.setLoadingChargesPerQty(contractorChargesData.getLoadingCharges());
			}
		}
	}

	private void calculateCharges(DataForGtn data, String jwt, String officeName, String godownName, String destination,
			ContractorChargesData contractorChargesData) throws Exception {
		final double[] isHillKmPresent = { 0.0 };
		masterService.getDistanceData(jwt, officeName, godownName).forEach(item -> {
			item.getTableData().forEach(itemData -> {
				if (itemData.getName().equals(destination)) {
					data.setKm(itemData.getKm());
					isHillKmPresent[0] = itemData.getHillKm();
				}
			});
		});

		double[] rates = { contractorChargesData.getZero_seven(), contractorChargesData.getEight_twenty(),
				contractorChargesData.getTwentyone_fifty(), contractorChargesData.getFiftyone_seventyfive(),
				contractorChargesData.getSeventysix_hundred(), contractorChargesData.getHundredone_onetwentyfive(),
				contractorChargesData.getOnetwosix_onefifty(), contractorChargesData.getOnefiftyone_oneseventyfive(),
				contractorChargesData.getOneseventysix_twohundred(), contractorChargesData.getAbovetwohundredone() };
		Double transportChargesPlain = SlabRateCalculator.calculateSlabRate(data.getKm(), rates);

		if (isHillKmPresent[0] != 0.0) {
			Double transportChargesHill = SlabRateCalculator.calculateSlabRate(isHillKmPresent[0], rates);
			Double hillCharges = transportChargesHill
					+ (transportChargesHill * (contractorChargesData.getHillRate() / 100));
			data.setTransportChargesPerQty(transportChargesPlain + hillCharges);
		} else {
			data.setTransportChargesPerQty(transportChargesPlain);

		}
	}

	@Override
	public List<TableDataForDc> gtnTableData(String officeName, String productName, String godownName)
			throws Exception {
		return getGtnDataByOffficeName(officeName).stream()
				.filter(item -> item.getProductName().equals(productName) && item.getVoucherStatus().equals("Approved")
						&& item.getDestination().equals(godownName) && item.getGtnFor().equals("Receipt"))
				.filter(item -> item.getGtnTableData().stream().anyMatch(data -> data.getQtyAvlForDc() > 0))
				.flatMap(itemData -> itemData.getGtnTableData().stream()
						.map(item -> new TableDataForDc(itemData.getProductCategory(), itemData.getProductGroup(),
								itemData.getSupplierName(), itemData.getProductName(), item.getPacking(),
								item.getStandardUnits(), item.getQtyAvlForDc(), item.getOutwardBatchNo(),
								fetchTermsNoFromGrnNo(item.getOutwardBatchNo()), item.getCollectionMode(),
								item.getMrp(), itemData.getDate(), itemData.getGtnNo())))
				.collect(Collectors.toList());
	}

	@Autowired
	private PoService poService;

	@Override
	public String fetchTermsNoFromGrnNo(String grnNo) {
		try {
			GRN grn = grnService.getGrnDataByGrnNo(grnNo);
			return poService.getPoByPoNo(grn.getPoNo()).getTermsPrice().getTermsNo();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public ResponseEntity<String> updateGrnQtyForDc(GrnQtyUpdateForDc obj, String despatchAdviceNo) throws Exception {
		try {
			logger.info("{}", obj);

//				fetch gtn data by grnNo and set new qty and save in repository
			GTN gtn = gtnRepo.findByGtnNo(obj.getVoucherId()).orElse(null);
			for (var item : gtn.getGtnTableData()) {
				if (item.getOutwardBatchNo().equals(obj.getOutwardBatchNo())) {
					item.setQtyAvlForDc(
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getQtyAvlForDc() - obj.getQty()));
				}
			}
			gtnRepo.save(gtn);
			try {
				if (obj.getDcNo() != null) {
					outwardBatchRepo.save(new OutwardBatch(null, LocalDateTime.now(), obj.getDcNo(), obj.getVoucherId(),
							obj.getOutwardBatchNo(), obj.getQty(), gtn.getProductCategory(), gtn.getProductGroup(),
							gtn.getSupplierName(), gtn.getProductName(), gtn.getGtnTableData().get(0).getPacking(),
							gtn.getGtnTableData().get(0).getStandardUnits(), gtn.getGtnTableData().get(0).getMrp(),
							gtn.getDate(), gtn.getOfficeName(), gtn.getDestination(), despatchAdviceNo));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertGrnQtyForDc(GrnQtyUpdateForDc obj) throws Exception {
		try {

//				fetch gtn data by grnNo and set new qty and save in repository
			GTN gtn = gtnRepo.findByGtnNo(obj.getVoucherId()).orElse(null);
			for (var item : gtn.getGtnTableData()) {
				if (item.getOutwardBatchNo().equals(obj.getOutwardBatchNo())) {
					item.setQtyAvlForDc(
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getQtyAvlForDc() + obj.getQty()));
				}
			}
			gtnRepo.save(gtn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public GTN getGtnDataByGtnNo(String gtnNo) throws Exception {
		try {
			return gtnRepo.findByGtnNo(gtnNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public GTN getReceiptGtnDataByGtnNo(String gtnNo) throws Exception {
		try {
			return gtnRepo.findByIssuedGtnNo(gtnNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void saveGtn(GTN gtn) throws Exception {
		try {
			gtnRepo.save(gtn);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private ClosingStockTableRepo closingStockTableRepo;

	@Override
	public void updateClosingBalanceIssue(GTN gtn) throws Exception {
		try {
			ClosingStockTable cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(
					gtn.getOfficeName(), gtn.getProductName(), gtn.getDate(), gtn.getGodownName());
			if (cb == null) {
				int n = 1;
				while (cb == null) {
					LocalDate date = gtn.getDate().minusDays(n++);
					cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(gtn.getOfficeName(),
							gtn.getProductName(), date, gtn.getGodownName());
					if (date.equals(LocalDate.of(2025, 3, 30)) && cb == null) {
						closingStockTableRepo.save(new ClosingStockTable(null, gtn.getOfficeName(), gtn.getDate(),
								gtn.getProductName(), gtn.getGodownName(),
								gtn.getGtnTableData().stream().mapToDouble(item -> item.getQty()).sum()));
						break;
					}
				}
				if (cb != null) {
					closingStockTableRepo.save(new ClosingStockTable(null, gtn.getOfficeName(), gtn.getDate(),
							gtn.getProductName(), gtn.getGodownName(),
							cb.getBalance() - gtn.getGtnTableData().stream().mapToDouble(item -> item.getQty()).sum()));
				}
			} else {
				cb.setBalance(
						cb.getBalance() - gtn.getGtnTableData().stream().mapToDouble(item -> item.getQty()).sum());
				closingStockTableRepo.save(cb);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateClosingBalanceReceipt(GTN gtn) throws Exception {
		try {
			String region = gtn.getTransactionFor().contains("Other Region") ? gtn.getToRegion() : gtn.getOfficeName();

			ClosingStockTable cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(region,
					gtn.getProductName(), gtn.getDate(), gtn.getDestination());
			if (cb == null) {
				int n = 1;
				while (cb == null) {
					LocalDate date = gtn.getDate().minusDays(n++);
					cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(region,
							gtn.getProductName(), date, gtn.getDestination());
					if (date.equals(LocalDate.of(2025, 3, 30)) && cb == null) {
						closingStockTableRepo.save(new ClosingStockTable(null, region, gtn.getDate(),
								gtn.getProductName(), gtn.getDestination(),
								gtn.getGtnTableData().stream().mapToDouble(item -> item.getReceivedQty()).sum()));
						break;
					}
				}
				if (cb != null) {
					closingStockTableRepo.save(new ClosingStockTable(null, region, gtn.getDate(), gtn.getProductName(),
							gtn.getDestination(), cb.getBalance()
									+ gtn.getGtnTableData().stream().mapToDouble(item -> item.getReceivedQty()).sum()));
				}
			} else {
				cb.setBalance(cb.getBalance()
						+ gtn.getGtnTableData().stream().mapToDouble(item -> item.getReceivedQty()).sum());
				closingStockTableRepo.save(cb);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private AccountsService accountsService;

	@Override
	public ResponseEntity<String> updateJvForSalesReturn(String gtnNo, JournalVoucher jv, String jwt) throws Exception {
		try {
			Vouchers voucher = new Vouchers();
			voucher.setJournalVoucherData(jv);
			ResponseEntity<String> responseEntity = accountsService.saveAccountsVouchersHandler("journalVoucher",
					voucher, jwt);
			String responseString = responseEntity.getBody();
			if (responseString == null) {
				throw new Exception("No data found");
			}
			String prefix = "JV Number : ";
			int index = responseString.indexOf(prefix);
			String jvNo = responseString.substring(index + prefix.length()).trim();
			SalesReturn salesReturn = salesReturnRepo.findByGtnNo(gtnNo).get();
			salesReturn.setJvNo(jvNo);
			salesReturnRepo.save(salesReturn);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateJVStatusInAcc(String jvNo, String status, String jwt) throws Exception {
		try {
			Vouchers vouchers = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
			VoucherApproval data = new VoucherApproval(status, String.valueOf(vouchers.getJournalVoucherData().getId()),
					"journalVoucher", null);
			accountsService.voucherApprovalHandler(data, jwt);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateClosingBalanceReceipt(SalesReturn salesReturn) throws Exception {
		try {
			salesReturn.getInvoiceTableData().forEach(item -> {
				ClosingStockTable cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(
						salesReturn.getOfficeName(), item.getProductName(), salesReturn.getDate(),
						salesReturn.getGodownName());
				if (cb == null) {
					int n = 1;
					while (cb == null) {
						LocalDate date = salesReturn.getDate().minusDays(n++);
						cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(
								salesReturn.getOfficeName(), item.getProductName(), date, salesReturn.getGodownName());
						if (date.equals(LocalDate.of(2025, 3, 30)) && cb == null) {
							closingStockTableRepo.save(
									new ClosingStockTable(null, salesReturn.getOfficeName(), salesReturn.getDate(),
											item.getProductName(), salesReturn.getGodownName(), item.getReturnQty()));
							break;
						}
					}
					if (cb != null) {
						closingStockTableRepo.save(new ClosingStockTable(null, salesReturn.getOfficeName(),
								salesReturn.getDate(), item.getProductName(), salesReturn.getGodownName(),
								cb.getBalance() + item.getReturnQty()));
					}
				} else {
					cb.setBalance(cb.getBalance() + item.getReturnQty());
					closingStockTableRepo.save(cb);
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
