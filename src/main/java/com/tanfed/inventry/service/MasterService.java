package com.tanfed.inventry.service;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tanfed.inventry.model.*;

@FeignClient(name = "BASICINFO-SERVICE", url = "${SHARED_API_URL}")
public interface MasterService {

	@GetMapping("/api/inventrymaster/fetchtpconfiglist")
	public List<Terms_Price_Config> getTerms_Price_ConfigListHandler(@RequestHeader("Authorization") String jwt)
			throws Exception;

	@GetMapping("/api/inventrymaster/fetchproductdata")
	public List<ProductMaster> getProductDataHandler(@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/inventrymaster/fetchproduct")
	public ProductMaster getProductDataByProductNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String productName) throws Exception;

	@GetMapping("/api/basic-info/fetchofficedata")
	public OfficeInfo getOfficeInfoByOfficeNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

//	@GetMapping("/api/basic-info/fetchcontractoradata")
//	public ContractorInfo getContarctorInfoByContractFirmHandler(@RequestHeader("Authorization") String jwt, @RequestParam String officeName, String contractFirm);

	@GetMapping("/api/basic-info/fetchcontractorinfo")
	public ContractorInfo getContractFirmByGodownNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String godownName) throws Exception;

	@GetMapping("/api/basic-info/fetchgodowndatalist")
	public List<GodownInfo> getGodownInfoByOfficeNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

	@GetMapping("/api/basic-info/fetchgodowndata")
	public GodownInfo getGodownInfoByGodownNameHandler(@RequestParam String godownName,
			@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/basic-info/fetchdistancedata")
	public List<DistanceMapping> getDistanceData(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName, @RequestParam String godownName) throws Exception;

	@GetMapping("/api/basic-info/fetchcontractorbyoffice")
	public List<ContractorInfo> getContarctorInfoByOfficeName(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

//	@GetMapping("/api/basic-info/fetchbuyerfirmdata")
//	public BuyerFirmInfo getBuyerFirmByFirmNameHandler(@RequestHeader("Authorization") String jwt,
//			@RequestParam String nameOfInstitution) throws Exception;

	@GetMapping("/api/basic-info/fetchBuyerdatabyoffice")
	public List<BuyerFirmInfo> getBuyerFirmDataByOfficeNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

	@GetMapping("/api/basic-info/fetchbuyername")
	public List<String> getBuyerNameByOfficeNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

	@GetMapping("/api/basic-info/fetchbanklist")
	public List<BankInfo> getBankInfoByOfficeNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String officeName) throws Exception;

	@GetMapping("/api/basic-info/fetchbankinfo")
	public BankInfo getBankInfoByAccountNoHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam Long accountNumber) throws Exception;

	@GetMapping("/api/accountsmaster/fetchbeneficiarymasterdata")
	public List<BeneficiaryMaster> getBeneficiaryMasterByName(@RequestHeader("Authorization") String jwt,
			@RequestParam String beneficiaryName, @RequestParam String officeName) throws Exception;

	@GetMapping("/api/accountsmaster/fetchgstcategory")
	public Set<String> findCategoryListHandler(@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/accountsmaster/fetchgstrate")
	public List<Double> findGstRateByCategoryHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String gstCategory) throws Exception;

	@GetMapping("/api/accountsmaster/fetchgstdata")
	public GstRateData findGstDataByGstRateHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String gstCategory, Double gstRate) throws Exception;

	@GetMapping("/api/accountsmaster/fetchtaxinfolist")
	public List<TaxInfo> findTaxInfoListHandler(@RequestHeader("Authorization") String jwt) throws Exception;

	@GetMapping("/api/basic-info/fetchsupplierdata")
	public SupplierInfo getSupplierInfoBySupplierNameHandler(@RequestHeader("Authorization") String jwt,
			@RequestParam String supplierName) throws Exception;

	@GetMapping("/api/basic-info/fetchsuppliernamelist")
	public List<String> getSupplierNameHadnler(@RequestHeader("Authorization") String jwt,
			@RequestParam String activity) throws Exception;

	@GetMapping("/api/basic-info/fetchdataforob")
	public DataForOB getDataForOBForm(@RequestParam String officeName, @RequestParam String bankName,
			@RequestParam String mainBranch, @RequestParam String subBranch, @RequestParam String accountType,
			@RequestHeader("Authorization") String jwt) throws Exception;

}
