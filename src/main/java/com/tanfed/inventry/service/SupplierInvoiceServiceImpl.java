package com.tanfed.inventry.service;

import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.entity.SupplierInvoiceDetails;
import com.tanfed.inventry.model.GrnAttachDto;
import com.tanfed.inventry.model.GrnDataForSupplierInvoice;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.repository.SupplierInvoiceDetailsRepo;
import com.tanfed.inventry.response.DataForSupplierInvoice;

@Service
public class SupplierInvoiceServiceImpl implements SupplierInvoiceService {

	@Autowired
	private SupplierInvoiceDetailsRepo supplierInvoiceDetailsRepo;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	private MasterService masterService;

	private static Logger logger = LoggerFactory.getLogger(SupplierInvoiceServiceImpl.class);

	@Override
	public DataForSupplierInvoice getDataForSupplierInvoice(String activity, String jwt, String supplierName,
			String monthOfSupply, String productName, String poMonth, String poNo, String officeName,
			String invoiceNumber, String invoiceNo) throws Exception {
		try {
			DataForSupplierInvoice data = new DataForSupplierInvoice();
			List<ProductMaster> productDataHandler = masterService.getProductDataHandler(jwt);
			if (!activity.isEmpty() && activity != null) {
				data.setSupplierNameList(productDataHandler.stream().filter(
						item -> item.getActivity().equals(activity) && !item.getSupplierName().startsWith("TANFED"))
						.map(ProductMaster::getSupplierName).collect(Collectors.toSet()));
				if (!supplierName.isEmpty() && supplierName != null) {
					data.setProductNameList(productDataHandler.stream().filter(
							item -> item.getActivity().equals(activity) && item.getSupplierName().equals(supplierName))
							.map(ProductMaster::getProductName).collect(Collectors.toList()));
					if (!productName.isEmpty() && productName != null) {
						ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
								productName);
						data.setProductCategory(productMaster.getProductCategory());
						data.setProductGroup(productMaster.getProductGroup());
						data.setPacking(productMaster.getPacking());
						data.setStandardUnits(productMaster.getStandardUnits());
						data.setSupplierGst(productMaster.getSupplierGst());
						data.setGstRate(productMaster.getGstRate());
						if (!invoiceNumber.isEmpty() && invoiceNumber != null) {
							SupplierInvoiceDetails supplierInvoiceDetails = supplierInvoiceDetailsRepo
									.findByInvoiceNumber(invoiceNumber);
							if (supplierInvoiceDetails != null) {
								throw new Exception("Invoice Already Present for " + invoiceNumber);
							}
						}
						fetchpoDataForGrnAttach(data, productName, monthOfSupply, poMonth, poNo, officeName, invoiceNo);
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
			String poMonth, String poNo, String officeName, String invoiceNo) throws Exception {
		try {
			List<PurchaseOrder> poDataHandler = poService.getPoData();

			data.setPoMonthList(poDataHandler.stream().filter(item -> item.getProductName().equals(productName))
					.map(PurchaseOrder::getDate)
					.map(date -> String.format("%s %04d",
							date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), date.getYear()))
					.collect(Collectors.toSet()));

			data.setMonthOfSupplyList(supplierInvoiceDetailsRepo.findAll().stream().filter(
					item -> item.getInvoiceQtyAvlForGrnAttach() > 0 && item.getProductName().equals(productName))
					.map(item -> item.getMonthOfSupply()).collect(Collectors.toSet()));
			if (!poMonth.isEmpty() && poMonth != null) {
				data.setPoNoList(poDataHandler.stream().filter(item -> {
					String month = String.format("%s %04d",
							item.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
							item.getDate().getYear());
					try {
						return item.getProductName().equals(productName) && month.equals(poMonth)
								&& filterGrnQty(item.getPoNo());
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
				}).map(PurchaseOrder::getPoNo).collect(Collectors.toList()));
				logger.info("mos{}", monthOfSupply);
				data.setInvoiceNoList(supplierInvoiceDetailsRepo.findByMonthOfSupply(monthOfSupply).stream().filter(
						item -> item.getProductName().equals(productName) && item.getInvoiceQtyAvlForGrnAttach() > 0)
						.map(item -> item.getInvoiceNumber()).collect(Collectors.toList()));
				if (!invoiceNo.isEmpty() && invoiceNo != null) {
					SupplierInvoiceDetails supplierInvoiceDetails = supplierInvoiceDetailsRepo
							.findByInvoiceNumber(invoiceNo);
					data.setInvoiceAvlQty(supplierInvoiceDetails.getInvoiceQtyAvlForGrnAttach());
					data.setInvoiceQty(supplierInvoiceDetails.getInvoiceQty());
					data.setInvoiceDate(supplierInvoiceDetails.getInvoiceDate());
				}

				if (!poNo.isEmpty() && poNo != null) {
					PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
					data.setOfficeNameList(purchaseOrder.getTableData().stream().map(item -> item.getRegion())
							.collect(Collectors.toSet()));
					data.setPoDate(purchaseOrder.getDate());
					if (!officeName.isEmpty() && officeName != null) {
						data.setPoQty(purchaseOrder.getTableData().stream()
								.filter(item -> item.getRegion().equals(officeName))
								.mapToDouble(item -> item.getPoIssueQty()).sum());
						data.setTotalGrnQty(purchaseOrder.getGrnData().stream()
								.filter(item -> item.getOfficeName().equals(officeName))
								.mapToDouble(item -> item.getMaterialReceivedQuantity()).sum());
						data.setTotalBookedQty(purchaseOrder.getGrnData().stream()
								.filter(item -> item.getOfficeName().equals(officeName))
								.mapToDouble(item -> item.getGrnAttachQty()).sum());
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

	@Override
	public ResponseEntity<String> saveSupplierInvoice(String obj, MultipartFile[] files, String jwt) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			List<SupplierInvoiceDetails> invoiceList = mapper.readValue(obj,
					mapper.getTypeFactory().constructCollectionType(List.class, SupplierInvoiceDetails.class));
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
					supplierInvoiceDetailsRepo.save(supplierInvoiceDetails);
				}
			}

			return new ResponseEntity<String>("Created Successfully", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateSupplierInvoiceQtyForGrnAttach(GrnAttachDto obj) throws Exception {
		try {
			SupplierInvoiceDetails invoiceDetails = supplierInvoiceDetailsRepo.findByInvoiceNumber(obj.getInvoiceNo());
			invoiceDetails.setInvoiceQtyAvlForGrnAttach(
					invoiceDetails.getInvoiceQtyAvlForGrnAttach() - obj.getCurrentBookingQty());
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
