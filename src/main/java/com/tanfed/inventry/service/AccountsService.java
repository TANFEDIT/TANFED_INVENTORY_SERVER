package com.tanfed.inventry.service;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tanfed.inventry.model.SupplierAdvance;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.model.Vouchers;

@FeignClient(name = "ACCOUNTS-SERVICE", url = "http://localhost:8083")
public interface AccountsService {

	@PostMapping("/api/accounts/accvouchers/{formType}")
	public ResponseEntity<String> saveAccountsVouchersHandler(@PathVariable String formType, @RequestBody Vouchers obj,
			@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/billsaccounts/fetchadvancesbyproduct")
	public List<SupplierAdvance> fetchOutstandingAdvancesByProductHandler(@RequestParam String productName,
			@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/accounts/getvouchersbyvoucherno")
	public Vouchers getAccountsVoucherByVoucherNoHandler(@RequestParam String formType, @RequestParam String voucherNo,
			@RequestHeader("Authorization") String jwt) throws Exception;

	@PutMapping("/api/accounts/voucherApproval")
	@PreAuthorize("hasAnyRole('ROLE_SUPERADMIN', 'ROLE_ACCADMIN', 'ROLE_ROADMIN')")
	public ResponseEntity<String> voucherApprovalHandler(@RequestBody VoucherApproval obj,
			@RequestHeader("Authorization") String jwt) throws Exception;

	@PutMapping("/api/billsaccounts/updatesupplieradvance/{supplierAdvanceNo}")
	public void updateAvlQtyAndAmountHandler(@PathVariable String supplierAdvanceNo, @RequestParam double qty,
			@RequestParam double amount) throws Exception;
}
