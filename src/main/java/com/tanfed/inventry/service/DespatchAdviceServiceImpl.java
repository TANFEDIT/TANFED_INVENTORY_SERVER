package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.hibernate.PropertyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.DcTableData;
import com.tanfed.inventry.entity.DespatchAdvice;
import com.tanfed.inventry.model.BuyerFirmInfo;
import com.tanfed.inventry.model.DespatchAdviceData;
import com.tanfed.inventry.model.DespatchAdviseTable;
import com.tanfed.inventry.model.DistanceMapping;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.Terms_Price_Config;
import com.tanfed.inventry.repository.DespatchAdviceRepo;
import com.tanfed.inventry.response.DataForDespatchAdvice;
import com.tanfed.inventry.utils.CodeGenerator;

@Service
public class DespatchAdviceServiceImpl implements DespatchAdviceService {

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private DespatchAdviceRepo despatchAdviceRepo;

	@Autowired
	private GrnService grnService;

	@Autowired
	private DcService dcService;

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private MasterService masterService;

	private static Logger logger = LoggerFactory.getLogger(DespatchAdviceServiceImpl.class);

	@Override
	public ResponseEntity<String> saveDespatchAdvice(DespatchAdvice obj, String jwt) throws Exception {
		try {
			String despatchAdviceNo = codeGenerator.despatchAdviceNoGenerator(obj.getActivity(), obj.getOfficeName());
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			logger.info(empId);
			obj.setDespatchAdviceNo(despatchAdviceNo);
			obj.setEmpId(Arrays.asList(empId));
			obj.setVoucherStatus("Pending");
			obj.setStatusDisabled(false);
			despatchAdviceRepo.save(obj);
			return new ResponseEntity<String>(
					"DespatchAdvice Added Successfully" + "\n Despatch Advise No: " + despatchAdviceNo,
					HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editDespatchAdvice(DespatchAdvice obj, String jwt) throws Exception {
		try {
			DespatchAdvice despatchAdvice = despatchAdviceRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			despatchAdvice.getEmpId().add(empId);
			despatchAdvice.setTableData(obj.getTableData());
			despatchAdviceRepo.save(despatchAdvice);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<DespatchAdvice> getDespatchAdviceDataByOffficeName(String officeName) throws Exception {
		try {
			return despatchAdviceRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForDespatchAdvice getDataForDespatchAdvice(String officeName, String activity, String nameOfInstitution,
			String productName, String jwt, String month, String godownName) throws Exception {
		try {
			DataForDespatchAdvice data = new DataForDespatchAdvice();
			if (officeName != null && !officeName.isEmpty()) {
				data.setNameOfInstitutionList(masterService.getBuyerNameByOfficeNameHandler(jwt, officeName));
				data.setGodownNameList(grnService.getGodownNameList(jwt, officeName, ""));
				data.getGodownNameList().add("Direct Material Center");
				if (month != null && !month.isEmpty()) {
					data.setDespatchAdviceData(getDespatchAdviceDataByOffficeName(officeName).stream()
							.filter(item -> item.getVoucherStatus().equals("Approved")).filter(item -> {
								String despatchAdviceMonth = String.format("%s%s%04d",
										item.getDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), " ",
										item.getDate().getYear());
								return despatchAdviceMonth.equals(month) && item.getStatusDisabled().equals(false);
							}).map(item -> {
								try {
									return new DespatchAdviceData(item.getId(), item.getActivity(),
											item.getDespatchAdviceNo(), item.getIfmsId(), item.getNameOfInstitution(),
											item.getDistrict(), item.getGodownName(), item.getTableData(),
											fetchQtyDataFromDc_Invoice(item.getDespatchAdviceNo()));
								} catch (Exception e) {
									e.printStackTrace();
									return null;
								}
							}).collect(Collectors.toList()));
				}
				if (activity != null && !activity.isEmpty()) {
					data.setProductNameList(masterService.getProductDataHandler(jwt).stream()
							.filter(item -> item.getActivity().equals(activity)).map(ProductMaster::getProductName)
							.collect(Collectors.toList()));
					List<Terms_Price_Config> terms_Price_ConfigList = masterService
							.getTerms_Price_ConfigListHandler(jwt);
					data.setSupplyModeList(terms_Price_ConfigList.stream()
							.filter(item -> item.getActivity().equals(activity)).map(Terms_Price_Config::getSupplyMode)
							.filter(Objects::nonNull).collect(Collectors.toList()));
					if (godownName != null && !godownName.isEmpty()) {
						if (nameOfInstitution != null && !nameOfInstitution.isEmpty()) {
							BuyerFirmInfo buyerFirmInfo = masterService.getBuyerFirmByFirmNameHandler(jwt, nameOfInstitution);
							data.setIfmsId(buyerFirmInfo.getIfmsIdNo());
							data.setBuyerGstNo(buyerFirmInfo.getBuyerGstNo());
							data.setVillage(buyerFirmInfo.getVillage());
							data.setBlock(buyerFirmInfo.getBlock());
							data.setTaluk(buyerFirmInfo.getTaluk());
							data.setDistrict(buyerFirmInfo.getDistrict());
							data.setSupplyTo(buyerFirmInfo.getSupplyTo());
							data.setLicenseNo(buyerFirmInfo.getLicenceNo());
							if (!godownName.equals("Direct Material Center")) {
								List<DistanceMapping> distanceData = masterService.getDistanceData(jwt, officeName,
										godownName);
								List<String> buyerList = distanceData.stream().flatMap(
										item -> item.getTableData().stream().map(itemData -> itemData.getName()))
										.collect(Collectors.toList());
								if (!buyerList.contains(buyerFirmInfo.getNameOfInstitution())) {
									throw new PropertyNotFoundException(
											"No distance available for :" + buyerFirmInfo.getNameOfInstitution());
								}
							}

							if (productName != null && !productName.isEmpty()) {
								ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
										productName);

								data.setProductCategory(productMaster.getProductCategory());
								data.setProductGroup(productMaster.getProductGroup());
								data.setPacking(productMaster.getPacking());
								data.setStandardUnits(productMaster.getStandardUnits());
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
	
	private List<DespatchAdviseTable> fetchQtyDataFromDc_Invoice(String despatchAdviceNo) throws Exception {
		List<DespatchAdviseTable> data = dcService.fetchDcData(despatchAdviceNo);
		data.addAll(invoiceService.fetchInvoiceData(despatchAdviceNo));
		return data;
	}

	@Override
	public List<String> getUnfullfilledDespatchAdviceNo(String officeName, String activity) throws Exception {
		try {
			return despatchAdviceRepo.findByActivity(activity).stream()
					.filter(item -> item.getOfficeName().equals(officeName)
							&& item.getVoucherStatus().equals("Approved") && item.getStatusDisabled().equals(false))
					.filter(item -> item.getTableData().stream().anyMatch(itemData -> itemData.getQtyAvlForDc() > 0))
					.map(DespatchAdvice::getDespatchAdviceNo).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DespatchAdvice getDespatchAdviceDataByDespatchAdviceNo(String despatchAdviceNo) throws Exception {
		try {
			return despatchAdviceRepo.findByDespatchAdviceNo(despatchAdviceNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateDespatchAdviceQty(String despatchAdviceNo, List<DcTableData> obj) throws Exception {
		try {
			logger.info(despatchAdviceNo);
			DespatchAdvice despatchAdvice = despatchAdviceRepo.findByDespatchAdviceNo(despatchAdviceNo).get();
			logger.info("da : {}", despatchAdvice);
			despatchAdvice.getTableData().forEach(despatchAdviceData -> {
				obj.forEach(dcData -> {
					if (dcData.getProductName().equals(despatchAdviceData.getProductName())) {
						despatchAdviceData.setQtyAvlForDc(despatchAdviceData.getQtyAvlForDc() - dcData.getQty());
					}
				});
			});
			despatchAdviceRepo.save(despatchAdvice);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertDespatchAdviceQty(String despatchAdviceNo, List<DcTableData> obj) throws Exception {
		try {
			DespatchAdvice despatchAdvice = despatchAdviceRepo.findByDespatchAdviceNo(despatchAdviceNo).get();
			despatchAdvice.getTableData().forEach(despatchAdviceData -> {
				obj.forEach(dcData -> {
					if (dcData.getProductName().equals(despatchAdviceData.getProductName())) {
						despatchAdviceData.setQtyAvlForDc(despatchAdviceData.getQtyAvlForDc() + dcData.getQty());
					}
				});
			});
			despatchAdviceRepo.save(despatchAdvice);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateDespatchAdviceStatus(Long id) throws Exception {
		try {
			DespatchAdvice despatchAdvice = despatchAdviceRepo.findById(id).get();
			despatchAdvice.setStatusDisabled(true);
			despatchAdvice.setStatusDisabledDate(LocalDate.now());
			despatchAdviceRepo.save(despatchAdvice);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
