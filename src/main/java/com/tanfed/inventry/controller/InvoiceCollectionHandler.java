package com.tanfed.inventry.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tanfed.inventry.dto.FundTransferDto;
import com.tanfed.inventry.model.AdjustmentReceiptVoucher;
import com.tanfed.inventry.model.BankInfo;
import com.tanfed.inventry.model.ICViewAplApdData;
import com.tanfed.inventry.model.InventryData;
import com.tanfed.inventry.model.InvoiceCollectionObject;
import com.tanfed.inventry.model.InvoiceCollectionRegisterTable;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.response.IcRegisters;
import com.tanfed.inventry.response.InvoiceCollectionResponseData;
import com.tanfed.inventry.service.GrnService;
import com.tanfed.inventry.service.InvoiceCollectionService;
import com.tanfed.inventry.service.MasterService;
import com.tanfed.inventry.service.RegisterService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/ic")
public class InvoiceCollectionHandler {

	@Autowired
	private InvoiceCollectionService invoiceCollectionService;

	@Autowired
	private RegisterService registerService;

	@GetMapping("/fetchicdata")
	public InvoiceCollectionResponseData getDataForInvoiceCollectionsHandler(
			@RequestParam(required = false) String officeName, @RequestParam(required = false) String activity,
			@RequestParam(required = false) String monthOfSales,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate toDate,
			@RequestParam(required = false) String invoiceType, @RequestParam(required = false) String transferType,
			@RequestParam(required = false) String materialCenter, @RequestParam(required = false) String ccbBranch,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate ackEntryDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate addedToPresentDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate dueDate,
			@RequestParam(required = false) String icmNo, @RequestParam(required = false) String collectionProcess,
			@RequestParam(required = false) String accountNo, @RequestParam(required = false) String branchName,
			@RequestParam(required = false) String toBranchName,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate date,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.getDataForInvoiceCollections(officeName, activity, monthOfSales, fromDate,
				toDate, invoiceType, materialCenter, ccbBranch, ackEntryDate, addedToPresentDate, dueDate, icmNo,
				collectionProcess, accountNo, branchName, date, jwt, transferType, toBranchName);
	}

	@GetMapping("/fetchictabledata")
	public ICViewAplApdData getINCLViewAPlApdHandler(@RequestParam String formType,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate toDate,
			@RequestParam String officeName, @RequestParam(required = false) String icmNo,
			@RequestParam String voucherStatus, @RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.getINCLViewAPlApd(formType, fromDate, toDate, officeName, voucherStatus, icmNo,
				jwt);
	}

	@Autowired
	private GrnService grnService;

	@Autowired
	private MasterService masterService;

	@GetMapping("/fetchicregisterdata/{formType}")
	public IcRegisters getICRegisterDataHandler(@PathVariable String formType, @RequestParam String officeName,
			@RequestParam String month, @RequestParam String branchName, @RequestParam String godownName,
			@RequestParam String accountNo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate fromDate,
			@RequestHeader("Authorization") String jwt,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate toDate)
			throws Exception {
		IcRegisters data = new IcRegisters();
		data.setGodownNameList(grnService.getGodownNameList(jwt, officeName));
		List<BankInfo> bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName);
		data.setBranchNameList(bankInfo.stream().filter(item -> item.getAccountType().equals("Non PDS A/c"))
				.map(BankInfo::getBranchName).collect(Collectors.toSet()));
		switch (formType) {
		case "invoiceWatchingRegister": {
			List<InvoiceCollectionRegisterTable> invoiceWatchingRegister = registerService
					.getInvoiceWatchingRegister(officeName, month, fromDate, branchName, godownName, toDate);
			data.setInvoiceWatchingRegister(invoiceWatchingRegister);
			return data;
		}
		case "invoicePresentationRegister": {
			List<InvoiceCollectionRegisterTable> invoicePresentationRegister = registerService
					.getInvoicePresentationRegister(officeName, month, fromDate, branchName, toDate);
			data.setInvoicePresentationRegister(invoicePresentationRegister);
			return data;
		}
		case "invoiceCollectionRegister": {
			List<InvoiceCollectionRegisterTable> invoiceCollectionRegister = registerService
					.getInvoiceCollectionRegister(officeName, month, fromDate, branchName, toDate);
			data.setInvoiceCollectionRegister(invoiceCollectionRegister);
			return data;
		}
		case "fundTransferRegister": {
			data.setAccountNoList(bankInfo.stream().filter(item -> item.getBranchName().equals(branchName))
					.map(BankInfo::getAccountNumber).collect(Collectors.toList()));
			List<InvoiceCollectionRegisterTable> fundTransferRegister = registerService
					.getFundTransferRegister(officeName, month, fromDate, branchName, toDate, accountNo);
			data.setFundTransferRegister(fundTransferRegister);
			return data;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + formType);
		}
	}

	@PutMapping("/updateicdata")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ESTADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> updateInvoiceCollectionHandler(@RequestBody List<InvoiceCollectionObject> obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.updateInvoiceCollection(obj, jwt);
	}

	@PutMapping("/updateicapproval")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ESTADMIN', 'ROLE_ROADMIN')")
	public ResponseEntity<String> inventryVoucherApprovalHandler(@RequestBody VoucherApproval obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		String updatedStatus = invoiceCollectionService.updateAplStatusInvoiceCollection(obj, jwt);
		return new ResponseEntity<String>(updatedStatus, HttpStatus.ACCEPTED);
	}

	@PutMapping("/editicdata/{formType}")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ESTADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> editInvoiceCollectionDataHandler(@PathVariable String formType,
			@RequestBody InventryData obj, @RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.editInvoiceCollectionData(formType, obj, jwt);
	}

	@PostMapping("/savefundtransfer")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ESTADMIN', 'ROLE_ROUSER', 'ROLE_ROADMIN')")
	public ResponseEntity<String> saveFundTransferHandler(@RequestBody FundTransferDto obj,
			@RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.saveFundTransfer(obj, jwt);
	}

	@GetMapping("/fetchcollectionabstract")
	public InvoiceCollectionResponseData getCollectionAbstractDataHandler(@RequestParam String officeName,
			String branchName, String accountNo, String monthOfFundTransfer, @RequestHeader("Authorization") String jwt)
			throws Exception {
		return invoiceCollectionService.getCollectionAbstractData(officeName, branchName, accountNo,
				monthOfFundTransfer, jwt);
	}

	@PutMapping("/saveAdjReceiptforicm/{type}")
	public ResponseEntity<String> saveAdjReceiptForIcmInvoicesHandler(@PathVariable String type,
			@RequestBody AdjustmentReceiptVoucher obj, @RequestHeader("Authorization") String jwt) throws Exception {
		return invoiceCollectionService.saveAdjReceiptForIcmInvoices(obj, jwt, type);
	}
}
