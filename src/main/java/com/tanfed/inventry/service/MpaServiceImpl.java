package com.tanfed.inventry.service;

import java.nio.file.FileSystemAlreadyExistsException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.*;
import com.tanfed.inventry.response.*;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class MpaServiceImpl implements MpaService {

	@Autowired
	private MasterService masterService;

	@Autowired
	private ManPowerAgencyRepo manPowerAgencyRepo;

	@Autowired
	private MpaEmployeeDataRepo mpaEmployeeDataRepo;

	@Autowired
	private MpaBillEntryRepo mpaBillEntryRepo;

	@Autowired
	private MpaCheckMemoRepo mpaCheckMemoRepo;

	@Override
	public DataForMpaMasters getDataForMpaMasters(String jwt, String gstCategory, Double gstRate, String formType,
			String officeName, String contractFirm) throws Exception {
		try {
			DataForMpaMasters data = new DataForMpaMasters();
			if (officeName != null && !officeName.isEmpty()) {
				data.setEmpDefaultData(mpaEmployeeDataRepo.findAll().stream()
						.filter(item -> item.getMpaData().getOfficeName().equals(officeName))
						.collect(Collectors.toList()));
				List<TaxInfo> taxInfoList = masterService.findTaxInfoListHandler(jwt);
				data.setGstCategoryList(taxInfoList.stream().map(TaxInfo::getGstCategory).collect(Collectors.toSet()));
				if (gstCategory != null && !gstCategory.isEmpty()) {
					data.setGstRateList(taxInfoList.stream().filter(item -> item.getGstCategory().equals(gstCategory))
							.map(TaxInfo::getGstRate).collect(Collectors.toList()));
					if (gstRate != null && gstRate != 0.0) {
						data.setGstData(taxInfoList.stream()
								.filter(item -> item.getGstCategory().equals(gstCategory)
										&& item.getGstRate().equals(gstRate))
								.map(item -> new GstRateData(item.getCgstRate(), item.getSgstRate(), item.getIgstRate(),
										item.getRcmRate()))
								.collect(Collectors.toList()).get(0));
					}
				}
				if (formType != null && !formType.isEmpty()) {
					data.setContractFirmList(manPowerAgencyRepo.findByOfficeName(officeName).stream()
							.map(item -> item.getContractFirm()).collect(Collectors.toSet()));
					if (contractFirm != null && !contractFirm.isEmpty()) {
						ManPowerAgency manPowerAgency = manPowerAgencyRepo.findByContractFirm(contractFirm);
						data.setMpaData(manPowerAgency);
						if (formType.equals("Update")) {
							data.setEmpData(mpaEmployeeDataRepo.findAll().stream()
									.filter(item -> item.getMpaData().getContractFirm().equals(contractFirm)
											&& item.getMpaData().getOfficeName().equals(officeName)
											&& item.getStatus().equals("Active"))
									.collect(Collectors.toList()));
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
	public ResponseEntity<String> saveManpowerAgency(String jwt, ManPowerAgency obj) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			manPowerAgencyRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> saveMpaEmployeeData(String jwt, MpaEmployeeData obj) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.setStatus("Active");
			mpaEmployeeDataRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateMpaMastersEmpData(List<MpaEmployeeData> obj) throws Exception {
		try {
			mpaEmployeeDataRepo.saveAll(obj);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForMpaBillEntry getDataForMpaBillEntry(String officeName, String contractFirm, String financialMonth,
			String engagedAs) throws Exception {
		try {
			DataForMpaBillEntry data = new DataForMpaBillEntry();
			if (officeName != null && !officeName.isEmpty()) {
				data.setFirmList(manPowerAgencyRepo.findByOfficeName(officeName).stream()
						.map(item -> item.getContractFirm()).collect(Collectors.toSet()));

				if (contractFirm != null && !contractFirm.isEmpty()) {
					ManPowerAgency manPowerAgency = manPowerAgencyRepo.findByContractFirm(contractFirm);
					data.setAppointedThrough(manPowerAgency.getAppointedThrough());
					data.setAppointedLr(manPowerAgency.getAppointedLr());
					data.setLrDate(manPowerAgency.getLrDate());
					List<MpaEmployeeData> empData = mpaEmployeeDataRepo.findAll().stream()
							.filter(item -> item.getMpaData().getContractFirm().equals(contractFirm)
									&& item.getMpaData().getOfficeName().equals(officeName)
									&& item.getStatus().equals("Active"))
							.collect(Collectors.toList());
					if (financialMonth != null && !financialMonth.isEmpty()) {
						List<MpaBillEntry> billEntryForBillMonth = mpaBillEntryRepo.findByOfficeName(officeName)
								.stream()
								.filter(item -> item.getFinancialMonth().equals(financialMonth)
										&& item.getContractFirm().equals(contractFirm)
										&& (item.getVoucherStatus().equals("Pending")
												|| item.getVoucherStatus().equals("Verified")))
								.collect(Collectors.toList());
						if (!billEntryForBillMonth.isEmpty()) {
							throw new FileSystemAlreadyExistsException("Approve Existing Bill Entry!");
						}
						List<MpaBillEntry> approvedBillEntryForBillMonth = billEntryForBillMonth.stream()
								.filter(item -> item.getFinancialMonth().equals(financialMonth)
										&& item.getContractFirm().equals(contractFirm)
										&& item.getVoucherStatus().equals("Approved"))
								.collect(Collectors.toList());
						if (!approvedBillEntryForBillMonth.isEmpty()) {
							throw new FileSystemAlreadyExistsException("Bill Entry Already Exists!");
						}
					}
					data.setEmpData(empData);
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForMpaCheckMemo getDataForMpaCheckMemo(String jwt, String officeName, String checkMemoNo, String month)
			throws Exception {
		try {
			DataForMpaCheckMemo data = new DataForMpaCheckMemo();
			if (officeName != null && !officeName.isEmpty()) {
				data.setCheckMemoNoList(mpaBillEntryRepo.findByOfficeName(officeName).stream()
						.filter(item -> item.getIsMpaCheckMemoDone().equals(false)
								&& item.getVoucherStatus().equals("Approved"))
						.map(item -> item.getCheckMemoNo()).collect(Collectors.toList()));
				if (month != null && !month.isEmpty()) {
					data.setCmData(mpaCheckMemoRepo.findByOfficeName(officeName).stream().filter(item -> {
						String cmMonth = String.format("%s%s%04d", item.getDate().getMonth(), " ",
								item.getDate().getYear());
						return cmMonth.equals(month);
					}).collect(Collectors.toList()));
				}
				if (checkMemoNo != null && !checkMemoNo.isEmpty()) {
					MpaBillEntry mpaBillEntry = mpaBillEntryRepo.findByCheckMemoNo(checkMemoNo).get();
					data.setMpaBillEntry(mpaBillEntry);
					ManPowerAgency manPowerAgency = manPowerAgencyRepo
							.findByContractFirm(mpaBillEntry.getContractFirm());
					data.setTotalCalculatedValue(
							mpaBillEntry.getEmpData().stream().mapToDouble(item -> item.getGrossAmount()).sum());
					data.setTotalCgstValue(
							(manPowerAgency.getGstData().getCgstRate() * data.getTotalCalculatedValue()) / 100);
					data.setTotalSgstValue(
							(manPowerAgency.getGstData().getSgstRate() * data.getTotalCalculatedValue()) / 100);
					data.setTotalPaymentValue(RoundToDecimalPlace.roundToThreeDecimalPlaces(
							data.getTotalCalculatedValue() + data.getTotalCgstValue() + data.getTotalSgstValue()));
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateEmpStatus(Long id, String status) throws Exception {
		try {
			MpaEmployeeData employeeData = mpaEmployeeDataRepo.findById(id).get();
			employeeData.setStatus(status);
			mpaEmployeeDataRepo.save(employeeData);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private CodeGenerator codeGenerator;

	@Override
	public ResponseEntity<String> saveMpaBillEntry(MpaBillEntry obj, String jwt) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.setVoucherStatus("Pending");
			obj.setIsMpaCheckMemoDone(false);
			String code = codeGenerator.generateCheckMemoNoMpa(obj.getClaimBillNo());
			obj.setCheckMemoNo(code);
			mpaBillEntryRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!" + "\n CheckMemo No :" + code, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private AccountsService accountsService;

	@Override
	public ResponseEntity<String> saveMpaCheckMemo(String jwt, MpaCheckMemoDto obj) throws Exception {
		try {
			MpaCheckMemo finalObj = new MpaCheckMemo();
			Vouchers voucher = new Vouchers();
			if (obj.getJvData() != null) {
				voucher.setJournalVoucherData(obj.getJvData());
				try {
					ResponseEntity<String> responseEntity = accountsService
							.saveAccountsVouchersHandler("journalVoucher", voucher, jwt);
					String responseString = responseEntity.getBody();
					if (responseString == null) {
						throw new Exception("No data found");
					}
					String prefix = "JV Number : ";
					int index = responseString.indexOf(prefix);
					finalObj.setJvNo(responseString.substring(index + prefix.length()).trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			MpaBillEntry mpaBillEntry = mpaBillEntryRepo.findByCheckMemoNo(obj.getCheckMemoNo()).get();
			mpaBillEntry.setIsMpaCheckMemoDone(true);
			mpaBillEntryRepo.save(mpaBillEntry);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			finalObj.setDate(obj.getDate());
			finalObj.setOfficeName(obj.getOfficeName());
			finalObj.setCheckMemoNo(obj.getCheckMemoNo());
			finalObj.setFinancialMonth(obj.getMpaBillEntry().getFinancialMonth());
			finalObj.setFinancialYear(obj.getMpaBillEntry().getFinancialYear());
			finalObj.setContractFirm(obj.getMpaBillEntry().getContractFirm());
			finalObj.setClaimBillNo(obj.getMpaBillEntry().getClaimBillNo());
			finalObj.setClaimBillDate(obj.getMpaBillEntry().getClaimBillDate());
			finalObj.setTotalBillValue(obj.getMpaBillEntry().getTotalBillValue());
			finalObj.setTotalCalculatedValue(obj.getTotalCalculatedValue());
			finalObj.setTotalCgstValue(obj.getTotalCgstValue());
			finalObj.setTotalSgstValue(obj.getTotalSgstValue());
			finalObj.setTotalPaymentValue(obj.getTotalPaymentValue());
			finalObj.setRecoveryIfAny(obj.getRecoveryIfAny());
			finalObj.setNetTotalDeduction(obj.getNetTotalDeduction());
			finalObj.setTcsOrTds(obj.getTcsOrTds());
			finalObj.setRate(obj.getRate());
			finalObj.setCalculatedTcsTdsValue(obj.getCalculatedTcsTdsValue());
			finalObj.setNetPaymentAfterAdjustment(obj.getNetPaymentAfterAdjustment());
			finalObj.setDifference(obj.getDifference());
			finalObj.setRemarks(obj.getRemarks());
			finalObj.setEmpId(Arrays.asList(empId));
			finalObj.setVoucherStatus("Pending");
			mpaCheckMemoRepo.save(finalObj);
			return new ResponseEntity<String>("Created Successfully!" + "\n Jv No :" + finalObj.getJvNo(),
					HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<MpaBillEntry> getMpaBillEntryByOfficeName(String officeName) throws Exception {
		try {
			return mpaBillEntryRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<MpaCheckMemo> getMpaCheckMemoByOfficeName(String officeName) throws Exception {
		try {
			return mpaCheckMemoRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updatePvNoMpaCheckMemo(Long id, String pvNo) throws Exception {
		try {
			MpaCheckMemo mpaCheckMemo = mpaCheckMemoRepo.findById(id).get();
			String prefix = "Voucher Number : ";
			int index = pvNo.indexOf(prefix);
			String code = pvNo.substring(index + prefix.length()).trim();
			mpaCheckMemo.setPvNo(code);
			mpaCheckMemoRepo.save(mpaCheckMemo);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateAccPvJv(MpaCheckMemo item, String jwt) throws Exception {
		try {
			if (item.getPvNo() != null) {
				Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", item.getPvNo(),
						jwt);
				accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
						String.valueOf(pv.getPaymentVoucherData().getId()), "paymentVoucher", null), jwt);
			}
			if (item.getJvNo() != null) {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", item.getJvNo(),
						jwt);
				accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
						String.valueOf(jv.getJournalVoucherData().getId()), "journalVoucher", null), jwt);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public MpaCheckMemo getMpaCheckMemoByCheckMemoNo(String checkMemoNo) throws Exception {
		try {
			return mpaCheckMemoRepo.findByCheckMemoNo(checkMemoNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
