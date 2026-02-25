package com.tanfed.inventry.service;

import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.dto.SupplierInvoiceInfoGrnAttach;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.entity.SupplierInvoiceDetails;
import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.GrnDataForSupplierInvoice;
import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.Vouchers;
import com.tanfed.inventry.repository.SupplierInvoiceDetailsRepo;
import com.tanfed.inventry.response.DataForSupplierInvoice;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class SupplierInvoiceServiceImpl implements SupplierInvoiceService {

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	private MasterService masterService;

	@Autowired
	private TermsPriceService termsPriceService;

//	private static Logger logger = LoggerFactory.getLogger(SupplierInvoiceServiceImpl.class);

	@Override
	public DataForSupplierInvoice getDataForSupplierInvoice(String activity, String jwt, String supplierName,
			String monthOfSupply, String productName, String poMonth, String poNo, String officeName, String termsMonth,
			String termsNo, String invoiceNumber, String invoiceNo) throws Exception {
		try {
			DataForSupplierInvoice data = new DataForSupplierInvoice();
			List<ProductMaster> productDataHandler = masterService.getProductDataHandler(jwt);
			if (activity != null && !activity.isEmpty()) {
				data.setSupplierNameList(productDataHandler.stream().filter(
						item -> item.getActivity().equals(activity) && !item.getSupplierName().startsWith("TANFED"))
						.map(ProductMaster::getSupplierName).collect(Collectors.toSet()));
				if (supplierName != null && !supplierName.isEmpty()) {
					data.setProductNameList(productDataHandler.stream().filter(
							item -> item.getActivity().equals(activity) && item.getSupplierName().equals(supplierName))
							.map(ProductMaster::getProductName).collect(Collectors.toList()));
					if (productName != null && !productName.isEmpty()) {
						ProductMaster productMaster = masterService.getProductDataHandler(jwt).stream()
								.filter(i -> i.getProductName().equals(productName))
								.reduce((first, second) -> second).orElse(null);
						data.setProductCategory(productMaster.getProductCategory());
						data.setProductGroup(productMaster.getProductGroup());
						data.setPacking(productMaster.getPacking());
						data.setStandardUnits(productMaster.getStandardUnits());
						data.setSupplierGst(productMaster.getSupplierGst());
						data.setGstRate(productMaster.getGstRate());
						data.setTermsMonthList(termsPriceService.fetchApprovedTermsMonth(activity, productName));
						if (termsMonth != null && !termsMonth.isEmpty()) {
							data.setTermsNoList(
									termsPriceService.fetchTermsByMonth(termsMonth, activity, productName, null, "SI"));
							if (termsNo != null && !termsNo.isEmpty()) {
								TermsPrice termsPrice = termsPriceService.fetchTermsByTermsNo(termsNo);
								data.setTermsDate(termsPrice.getMasterData().getDate());
								data.setPurchaseTermsPricing(termsPrice.getPurchaseTermsPricing());
								data.setPurchaseDataBuffer(termsPrice.getPurchaseDataBuffer());
								data.setPurchaseDataDirect(termsPrice.getPurchaseDataDirect());
								data.setPurchaseDataGeneral(termsPrice.getPurchaseDataGeneral());
							}
						}
						if (invoiceNumber != null && !invoiceNumber.isEmpty()) {
							SupplierInvoiceDetails supplierInvoiceDetails = supplierInvoiceDetailsRepo
									.findByInvoiceNumber(invoiceNumber);
							if (supplierInvoiceDetails != null) {
								throw new Exception("Invoice Already Present for " + invoiceNumber);
							}
						}
						fetchpoDataForGrnAttach(data, productName, monthOfSupply, poMonth, poNo, officeName);
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PoService poService;

	private void fetchpoDataForGrnAttach(DataForSupplierInvoice data, String productName, String monthOfSupply,
			String poMonth, String poNo, String officeName) throws Exception {
		try {
			List<PurchaseOrder> poDataHandler = poService.getPoData();

			data.setPoMonthList(poDataHandler.stream().filter(
					item -> item.getProductName().equals(productName) && item.getVoucherStatus().equals("Approved"))
					.map(PurchaseOrder::getDate)
					.map(date -> String.format("%s %04d",
							date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), date.getYear()))
					.collect(Collectors.toSet()));

			data.setMonthOfSupplyList(supplierInvoiceDetailsRepo.findAll().stream().filter(
					item -> item.getInvoiceQtyAvlForGrnAttach() > 0 && item.getProductName().equals(productName))
					.map(item -> item.getMonthOfSupply()).collect(Collectors.toSet()));
			if (poMonth != null && !poMonth.isEmpty()) {
				data.setPoNoList(poDataHandler.stream().filter(item -> {
					String month = String.format("%s %04d",
							item.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
							item.getDate().getYear());
					try {
						return item.getProductName().equals(productName) && month.equals(poMonth)
								&& filterGrnQty(item.getPoNo()) && item.getVoucherStatus().equals("Approved");
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}).map(PurchaseOrder::getPoNo).collect(Collectors.toList()));
				if (poNo != null && !poNo.isEmpty()) {
					PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
					if (monthOfSupply != null && !monthOfSupply.isEmpty()) {
					}
					data.setOfficeNameList(purchaseOrder.getTableData().stream().map(item -> item.getRegion())
							.collect(Collectors.toSet()));
					data.setPoDate(purchaseOrder.getDate());
					if (officeName != null && !officeName.isEmpty()) {
						data.setInvoiceTableData(supplierInvoiceDetailsRepo.findByMonthOfSupply(monthOfSupply).stream()
								.filter(item -> item.getProductName().equals(productName)
										&& item.getInvoiceQtyAvlForGrnAttach() > 0
										&& item.getInvoiceOfficeName().equals(officeName)
										&& item.getTermsNo().equals(purchaseOrder.getTermsPrice().getTermsNo()))
								.map(item -> new SupplierInvoiceInfoGrnAttach(item.getTermsNo(),
										item.getInvoiceNumber(), item.getInvoiceDate(), item.getInvoiceQty(),
										item.getInvoiceQtyAvlForGrnAttach(), item.getTotalInvoiceValue()))
								.collect(Collectors.toList()));
						data.setPoQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getTableData()
								.stream().filter(item -> item.getRegion().equals(officeName))
								.mapToDouble(item -> item.getPoIssueQty()).sum()));
						data.setTotalGrnQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getGrnData()
								.stream().filter(item -> item.getOfficeName().equals(officeName))
								.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum()));
						data.setTotalBookedQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder.getGrnData()
								.stream().filter(item -> item.getOfficeName().equals(officeName))
								.mapToDouble(item -> item.getGrnAttachQty()).sum()));
						data.setGrnTableData(purchaseOrder.getGrnData().stream()
								.filter(item -> item.getOfficeName().equals(officeName))
								.filter(item -> item.getGrnQtyAvlForGrnAttach() > 0)
								.map(item -> new GrnDataForSupplierInvoice(item.getGodownName(), item.getDcWdnRoNo(),
										item.getGrnNo(), item.getMaterialReceivedQuantity(),
										item.getGrnQtyAvlForGrnAttach()))
								.collect(Collectors.toList()));
					}
				}
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private Boolean filterGrnQty(String poNo) throws Exception {
		try {
			PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
			double totalGrnQty = purchaseOrder.getGrnData().stream()
					.mapToDouble(itemData -> itemData.getMaterialReceivedQuantity()).sum();
			double totalGrnAttachedQty = purchaseOrder.getGrnData().stream()
					.mapToDouble(itemData -> itemData.getGrnAttachQty()).sum();
			return totalGrnAttachedQty < totalGrnQty ? true : false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	@Autowired
	private AccountsService accountsService;

	@Override
	public ResponseEntity<String> saveSupplierInvoice(String obj, MultipartFile[] files, String jwt, String jvs)
			throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			List<SupplierInvoiceDetails> invoiceList = mapper.readValue(obj,
					mapper.getTypeFactory().constructCollectionType(List.class, SupplierInvoiceDetails.class));
			List<JournalVoucher> jvList = mapper.readValue(jvs,
					mapper.getTypeFactory().constructCollectionType(List.class, JournalVoucher.class));
			Vouchers voucher = new Vouchers();
			Map<String, String> jvMap = new HashMap<String, String>();
			jvList.forEach(item -> {
				voucher.setJournalVoucherData(item);
				try {
					ResponseEntity<String> responseEntity = accountsService
							.saveAccountsVouchersHandler("journalVoucher", voucher, jwt);
					String responseString = responseEntity.getBody();
					if (responseString == null) {
						throw new Exception("No jv found");
					}
					String prefix = "JV Number : ";
					int index = responseString.indexOf(prefix);
					String jvNo = responseString.substring(index + prefix.length()).trim();
					jvMap.put(item.getJvType(), jvNo);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			if (invoiceList.size() == files.length) {
				for (int i = 0; i < invoiceList.size(); i++) {
					SupplierInvoiceDetails supplierInvoiceDetails = invoiceList.get(i);

					MultipartFile multipartFile = files[i];
					supplierInvoiceDetails.setEmpId(Arrays.asList(empId));
					supplierInvoiceDetails.setFilename(multipartFile.getOriginalFilename());
					supplierInvoiceDetails.setFiletype(multipartFile.getContentType());
					supplierInvoiceDetails.setFiledata(multipartFile.getBytes());
					supplierInvoiceDetails.setVoucherStatus("Pending");
					supplierInvoiceDetails.setInvoiceQtyAvlForGrnAttach(supplierInvoiceDetails.getInvoiceQty());
					jvMap.entrySet().forEach(item -> {
						if (item.getKey().equals("Input Tax")) {
							supplierInvoiceDetails.setTaxJv(item.getValue());
						} else {
							supplierInvoiceDetails.setNetJv(item.getValue());
						}
					});
					supplierInvoiceDetailsRepo.save(supplierInvoiceDetails);
				}
			}

			return new ResponseEntity<String>("Created Successfully", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateSupplierInvoiceQtyForGrnAttach(String invoiceNo, Double qty) throws Exception {
		try {
			SupplierInvoiceDetails invoiceDetails = supplierInvoiceDetailsRepo.findByInvoiceNumber(invoiceNo);
			invoiceDetails.setInvoiceQtyAvlForGrnAttach(RoundToDecimalPlace.roundToTwoDecimalPlaces(qty));
			supplierInvoiceDetailsRepo.save(invoiceDetails);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public SupplierInvoiceDetails getSupplierInvoiceByInvoiceNumber(String invoiceNumber) throws Exception {
		try {
			SupplierInvoiceDetails invoiceDetails = supplierInvoiceDetailsRepo.findByInvoiceNumber(invoiceNumber);
			if (invoiceDetails == null) {
				throw new Exception("No data found for Invoice Number : " + invoiceNumber);
			}
			return invoiceDetails;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<SupplierInvoiceDetails> getSupplierInvoiceDetails() throws Exception {
		return supplierInvoiceDetailsRepo.findAll();
	}

}
