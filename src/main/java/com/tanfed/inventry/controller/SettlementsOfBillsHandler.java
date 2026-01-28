package com.tanfed.inventry.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.TcBillEntryRepo;
import com.tanfed.inventry.dto.CheckMemoGoodsDto;
import com.tanfed.inventry.dto.DataForCheckMemoGoods;
import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.response.*;
import com.tanfed.inventry.service.*;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/sob")
public class SettlementsOfBillsHandler {

	@Autowired
	private SupplierInvoiceService supplierInvoiceService;

	@Autowired
	private PurchaseBookingService purchaseBookingService;

	@Autowired
	private CheckMemoGoodsService checkMemoGoodsService;

	@Autowired
	private FilterInventryDataService filterInventryDataService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private MpaService mpaService;

	@Autowired
	private TcService tcService;

	@PostMapping("/savesupplierinvoice")
	public ResponseEntity<String> saveSupplierInvoiceHandler(@RequestPart String obj, @RequestPart String jvs,
			@RequestParam MultipartFile[] files, @RequestHeader("Authorization") String jwt) throws Exception {
		return supplierInvoiceService.saveSupplierInvoice(obj, files, jwt, jvs);
	}

	@PostMapping("/savepurchasebooking")
	public ResponseEntity<String> savePurchaseBookingHandler(@RequestBody PurchaseBookingDto obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return purchaseBookingService.savePurchaseBooking(obj, jwt);
	}

	@PostMapping("/savecheckmemo")
	public ResponseEntity<String> saveCheckMemoGoodsHandler(@RequestBody CheckMemoGoodsDto obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return checkMemoGoodsService.saveCheckMemoGoods(obj, jwt);
	}

	@PostMapping("/savempamaster")
	public ResponseEntity<String> saveManPowerAgencyMasterHandler(@RequestBody ManPowerAgency obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return mpaService.saveManpowerAgency(jwt, obj);
	}

	@PostMapping("/savempamaster/empdata")
	public ResponseEntity<String> saveMpaEmployeeDataHandler(@RequestBody MpaEmployeeData obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return mpaService.saveMpaEmployeeData(jwt, obj);
	}

	@PostMapping("/savempa/billentry")
	public ResponseEntity<String> saveMpaBillEntryHandler(@RequestBody MpaBillEntry obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return mpaService.saveMpaBillEntry(obj, jwt);
	}

	@PostMapping("/savempa/checkmemo")
	public ResponseEntity<String> saveMpaCheckMemoHandler(@RequestBody MpaCheckMemoDto obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return mpaService.saveMpaCheckMemo(jwt, obj);
	}

	@PostMapping("/updatebillentry")
	public ResponseEntity<String> updateBillEnteredDcGtnGrnHandler(@RequestHeader("Authorization") String jwt,
			@RequestBody TcBillEntryTempTable obj) throws Exception {
		return tcService.updateBillEnteredDcGtnGrn(jwt, obj);
	}

	@PostMapping("/savetcbillentry")
	public ResponseEntity<String> saveTcBillEntryHandler(@RequestHeader("Authorization") String jwt,
			@RequestBody TcBillEntry obj) throws Exception {
		return tcService.saveTcBillEntry(jwt, obj);
	}

	@PostMapping("/savetccheckmemo")
	public ResponseEntity<String> saveTcCheckMemoHandler(@RequestHeader("Authorization") String jwt,
			@RequestBody TcCheckMemoDto obj) throws Exception {
		return tcService.saveTcCheckMemo(jwt, obj);
	}

	@GetMapping("/fetchsobfilterdata")
	public SobData filterSobDataHsndler(@RequestParam String formType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam String officeName, @RequestParam String voucherStatus,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return filterInventryDataService.filterSobData(formType, fromDate, toDate, officeName, voucherStatus, jwt);
	}

	@GetMapping("/fetchdataforsupplierinvoice")
	public DataForSupplierInvoice getDataForSupplierInvoiceHandler(@RequestParam String activity,
			@RequestParam String termsMonth, @RequestParam String termsNo, @RequestParam String monthOfSupply,
			@RequestParam String supplierName, @RequestParam String productName, @RequestParam String poMonth,
			@RequestParam String poNo, @RequestParam String officeName, @RequestParam String invoiceNumber,
			@RequestParam String invoiceNo, @RequestHeader("Authorization") String jwt) throws Exception {
		return supplierInvoiceService.getDataForSupplierInvoice(activity, jwt, supplierName, monthOfSupply, productName,
				poMonth, poNo, officeName, termsMonth, termsNo, invoiceNumber, invoiceNo);

	}

	@GetMapping("/fetchdataforpurchasebooking")
	public DataForPurchaseBooking getDataForPurchaseBookingHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String activity, @RequestParam String productCategory, @RequestParam String supplierName,
			@RequestParam String productName, @RequestParam String poType, @RequestParam String poMonth,
			@RequestParam String poNo) throws Exception {
		return purchaseBookingService.getDataForPurchaseBooking(jwt, activity, productCategory, supplierName,
				productName, poType, poMonth, poNo);
	}

	@GetMapping("/fetchdataforcmgoods")
	public DataForCheckMemoGoods getDataForCheckMemoGoodsHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String activity, @RequestParam String checkMemoNo, @RequestParam String supplierAdvanceNo,
			@RequestParam String month) throws Exception {
		return checkMemoGoodsService.getDataForCheckMemoGoods(activity, checkMemoNo, jwt, supplierAdvanceNo, month);
	}

	@GetMapping("/fetchdataformpamaster")
	public DataForMpaMasters getDataForMpaMastersHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String contractName, @RequestParam String gstCategory, @RequestParam String formType,
			@RequestParam String officeName, @RequestParam Double gstRate) throws Exception {
		return mpaService.getDataForMpaMasters(jwt, gstCategory, gstRate, formType, officeName, contractName);
	}

	@GetMapping("/fetchDataformpabillentry")
	public DataForMpaBillEntry getDataForMpaBillEntryHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String contractFirm, @RequestParam String financialMonth,
			@RequestParam String engagedAs) throws Exception {
		return mpaService.getDataForMpaBillEntry(officeName, contractFirm, financialMonth, engagedAs);
	}

	@GetMapping("/fetchdataformpacm")
	public DataForMpaCheckMemo getDataForMpaCheckMemoHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String checkMemoNo, @RequestParam String month)
			throws Exception {
		return mpaService.getDataForMpaCheckMemo(jwt, officeName, checkMemoNo, month);
	}

	@GetMapping("/fetchdatafortcbillentry")
	public DataForTcBillEntry getDataForTcBillEntryHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String godownName, @RequestParam String claimFor,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestParam String idNo, @RequestParam String loadType, @RequestParam String clNo,
			@RequestParam String claimBillNo) throws Exception {
		return tcService.getDataForTcBillEntry(officeName, jwt, godownName, claimFor, idNo, fromDate, toDate, loadType,
				clNo, claimBillNo);
	}

	@GetMapping("/fetchdatafortccheckmemo")
	public DataForTcCheckMemo getDataForTcCheckMemoHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String checkMemoNo, @RequestParam String month)
			throws Exception {
		return tcService.getDataForTcCheckMemo(jwt, officeName, checkMemoNo, month);
	}

	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	@GetMapping("/gettc")
	public TcBillEntry testTc(@RequestHeader("Authorization") String jwt) {
		return tcBillEntryRepo.findById(10l).get();
	}

	@Autowired
	private PoService poService;

	@Autowired
	private MasterService masterService;

	@Autowired
	private RegisterService registerService;

	@GetMapping("/fetchsobregisters/{formType}")
	public SobRegisters getSobRegistersHandler(@PathVariable String formType, @RequestParam String month,
			@RequestParam String officeName, @RequestParam(required = false) String activity,
			@RequestParam(required = false) String supplierName, @RequestParam(required = false) String productName,
			@RequestParam(required = false) String poNo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
			@RequestHeader("Authorization") String jwt) throws Exception {
		SobRegisters data = new SobRegisters();
		List<ProductMaster> productDataHandler = masterService.getProductDataHandler(jwt);
		if (activity != null && !activity.isEmpty()) {
			data.setPoNoList(poService.getPoData().stream().filter(item -> {
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && monthFilter && item.getActivity().equals(activity)
						&& item.getTableData().stream().map(PoTableData::getRegion).collect(Collectors.toList())
								.contains(officeName);
			}).map(item -> item.getPoNo()).collect(Collectors.toList()));
			data.setSupplierNameList(productDataHandler.stream()
					.filter(item -> item.getActivity().equals(activity) && !item.getSupplierName().startsWith("TANFED"))
					.map(ProductMaster::getSupplierName).collect(Collectors.toSet()));
			if (!supplierName.isEmpty() && supplierName != null) {
				data.setProductNameList(productDataHandler.stream().filter(
						item -> item.getActivity().equals(activity) && item.getSupplierName().equals(supplierName))
						.map(ProductMaster::getProductName).collect(Collectors.toSet()));
			}
		}
		switch (formType) {
		case "purchaseDayBookValue": {
			List<PurchaseDayBookValue> purchaseDayBookValueData = registerService.getPurchaseDayBookValueData(month,
					fromDate, toDate, activity, supplierName, productName, poNo);
			data.setPurchaseDayBookValue(purchaseDayBookValueData);
			return data;
		}
		case "purchaseDayBookQty": {
			List<PurchaseDayBookQty> purchaseDayBookQtyData = registerService.getPurchaseDayBookQtyData(month, fromDate,
					toDate, activity, supplierName, productName, poNo);
			data.setPurchaseDayBookQty(purchaseDayBookQtyData);
			return data;
		}
		case "tcBillRegister": {
			List<TcBillRegisterTable> tcBillRegisterData = registerService.getTcBillRegisterData(officeName, month,
					fromDate, toDate);
			data.setTcBillRegister(tcBillRegisterData);
			return data;
		}
		case "mpaBillRegister": {
			List<TcBillRegisterTable> mpaBillRegisterData = registerService.getMpaBillRegisterData(officeName, month,
					fromDate, toDate);
			data.setMpaBillRegister(mpaBillRegisterData);
			return data;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + formType);
		}
	}

	@Autowired
	private InventryVouchersApprovalService inventryVouchersApprovalService;

	@PutMapping("/updatesobapproval")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_FERTADMIN', 'ROLE_MARKADMIN', 'ROLE_SPAIADMIN', 'ROLE_ROADMIN')")
	public ResponseEntity<String> inventryVoucherApprovalHandler(@RequestBody VoucherApproval obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		String updatedStatus = inventryVouchersApprovalService.updateSobVoucherApproval(obj, jwt);
		return new ResponseEntity<String>(updatedStatus, HttpStatus.ACCEPTED);
	}

	@PutMapping("/updategrnattach")
	public ResponseEntity<String> updateGrnAttachQtyHandler(@RequestBody GrnAttachDto obj) throws Exception {
		try {
			return grnService.updateGrnAttachQty(obj);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@PutMapping("/updatempa/empdata")
	public ResponseEntity<String> updateMpaMastersEmpDataHandler(@RequestBody List<MpaEmployeeData> obj)
			throws Exception {
		return mpaService.updateMpaMastersEmpData(obj);
	}

	@PutMapping("/updatempstatus/{id}/{status}")
	public ResponseEntity<String> updateEmpStatusHadnler(@PathVariable Long id, @PathVariable String status)
			throws Exception {
		return mpaService.updateEmpStatus(id, status);
	}

	@PutMapping("/updatepvinmpacm/{id}/{pvNo}")
	public ResponseEntity<String> updatePvNoMpaCheckMemoHandler(@PathVariable Long id, @PathVariable String pvNo)
			throws Exception {
		return mpaService.updatePvNoMpaCheckMemo(id, pvNo);
	}
	
	@PutMapping("/updatepvintccm/{id}/{pvNo}")
	public ResponseEntity<String> updatePvNoTcCheckMemoHandler(@PathVariable Long id, @PathVariable String pvNo)
			throws Exception {
		return tcService.updatePvNoTcCheckMemo(id, pvNo);
	}

	@PutMapping("/reverttcdatatemptable/{id}")
	public ResponseEntity<String> revertTcDataInTempTableHandler(@PathVariable Long id) throws Exception {
		return tcService.revertTcDataInTempTable(id);
	}

	@PutMapping("/pathatidvincheckmemo/{id}")
	public void updatePvInCheckMemoHandler(@PathVariable Long id, @RequestParam String pv) throws Exception {
		checkMemoGoodsService.updatePvInCheckMemo(pv, id);
	}
}
