package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.dto.TcCheckMemoDto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.TcBillEntryRepo;
import com.tanfed.inventry.repository.TcBillEntryTempTableRepo;
import com.tanfed.inventry.repository.TcCheckMemoRepo;
import com.tanfed.inventry.response.DataForTcBillEntry;
import com.tanfed.inventry.response.DataForTcCheckMemo;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class TcServiceIpml implements TcService {

	@Autowired
	private MasterService masterService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private DcService dcService;

	@Autowired
	private TcBillEntryTempTableRepo tcBillEntryTempTableRepo;
	private static Logger logger = LoggerFactory.getLogger(TcServiceIpml.class);

	@Override
	public DataForTcBillEntry getDataForTcBillEntry(String officeName, String jwt, String godownName, String claimFor,
			String idNo, LocalDate fromDate, LocalDate toDate, String loadType, String clNo, String claimBillNo)
			throws Exception {
		try {
			DataForTcBillEntry data = new DataForTcBillEntry();
			if (officeName != null && !officeName.isEmpty()) {
				data.setGodownNameList(grnService.getGodownNameList(jwt, officeName, ""));
				if (godownName != null && !godownName.isEmpty()) {
					ContractorInfo contractorInfo = masterService.getContractFirmByGodownNameHandler(jwt, officeName,
							godownName);
					data.setContractFirm(contractorInfo.getContractFirm());
					data.setContractThrough(contractorInfo.getContractThrough());
					data.setTenderData(contractorInfo.getTenderData());
					if (claimBillNo != null && !claimBillNo.isEmpty()) {
						data.setChargesData(tcBillEntryTempTableRepo.findByClaimBillNo(claimBillNo));
					}
					if (claimFor != null && !claimFor.isEmpty()) {
						if (claimFor.equals("Wagon Clearance Charges")) {
							wagonchargesData(data, officeName, godownName, idNo, fromDate, toDate);
						}
						if (claimFor.equals("Unloading Charges")) {
							unloadingChargesData(data, officeName, godownName, idNo, fromDate, toDate);
						}
						if (claimFor.equals("Transport Only")) {
							transportChargesData(data, loadType, officeName, godownName, idNo, fromDate, toDate, clNo);
						}
						if (claimFor.equals("Transport + Loading Charges")) {
							transportAndLoadingChargesData(data, loadType, officeName, godownName, idNo, fromDate,
									toDate, clNo);
						}
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void wagonchargesData(DataForTcBillEntry data, String officeName, String godownName, String idNo,
			LocalDate fromDate, LocalDate toDate) throws Exception {
		if (fromDate != null && toDate != null) {
			data.setIdNoList(grnService.getGrnDataByOffficeName(officeName).stream()
					.filter(item -> item.getGodownName().equals(godownName) && !item.getDate().isBefore(fromDate)
							&& !item.getDate().isAfter(toDate) && item.getVoucherStatus().equals("Approved")
							&& item.getWagonData() != null && item.getWagonData().getWagonStatus().equals("Approved")
							&& item.getWagonBillEntry().equals(false))
					.map(item -> item.getGrnNo()).collect(Collectors.toList()));
			if (idNo != null && !idNo.isEmpty()) {
				GRN grn = grnService.getGrnDataByGrnNo(idNo);
				data.setAckQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(grn.getWagonData().getActualReceiptQty()));
				data.setSupplierName(grn.getSupplierName());
				data.setProductName(grn.getProductName());
				data.setPacking(grn.getPacking());
				data.setBags(grn.getMaterialReceivedBags());
				data.setCalcWagonClearanceCharges(
						RoundToDecimalPlace.roundToThreeDecimalPlaces(grn.getWagonClearanceValue()));
				data.setDate(grn.getDate());
			}
		}
	}

	private void unloadingChargesData(DataForTcBillEntry data, String officeName, String godownName, String idNo,
			LocalDate fromDate, LocalDate toDate) throws Exception {
		if (fromDate != null && toDate != null) {
			data.setIdNoList(gtnService.getGtnDataByOffficeName(officeName).stream()
					.filter(item -> item.getGtnFor().equals("Receipt") && item.getDestination().equals(godownName)
							&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate)
							&& item.getVoucherStatus().equals("Approved") && item.getBillEntry().equals(false))
					.map(item -> item.getGtnNo()).collect(Collectors.toList()));

			data.getIdNoList().addAll(grnService.getGrnDataByOffficeName(officeName).stream()
					.filter(item -> item.getGodownName().equals(godownName) && !item.getDate().isBefore(fromDate)
							&& !item.getDate().isAfter(toDate) && item.getVoucherStatus().equals("Approved")
							&& item.getUnloadingBillEntry().equals(false)
							&& item.getUnloadingCharges().equals("TANFED-H&T"))
					.map(item -> item.getGrnNo()).collect(Collectors.toList()));

			if (idNo != null && !idNo.isEmpty()) {
				if (idNo.startsWith("GT")) {
					GTN gtn = gtnService.getGtnDataByGtnNo(idNo);
					data.setAckQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(
							gtn.getGtnTableData().stream().mapToDouble(item -> item.getReceivedQty()).sum()));
					data.setSupplierName(gtn.getSupplierName());
					data.setProductName(gtn.getProductName());
					Double sum = gtn.getGtnTableData().stream().mapToDouble(item -> item.getReceivedBags()).sum();
					data.setBags(sum.toString());
					data.setCalcUnloadingCharges(
							RoundToDecimalPlace.roundToThreeDecimalPlaces(gtn.getTotalUnloadingCharges()));
					data.setDate(gtn.getDate());
					data.setPacking(gtn.getGtnTableData().get(0).getPacking());
				} else {
					GRN grn = grnService.getGrnDataByGrnNo(idNo);
					data.setAckQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(grn.getMaterialReceivedQuantity()));
					data.setSupplierName(grn.getSupplierName());
					data.setProductName(grn.getProductName());
					data.setPacking(grn.getPacking());
					data.setBags(grn.getMaterialReceivedBags());
					data.setCalcUnloadingCharges(
							RoundToDecimalPlace.roundToThreeDecimalPlaces(grn.getUnloadingChargesValue()));
					data.setDate(grn.getDate());
				}
			}
		}
	}

	private void transportChargesData(DataForTcBillEntry data, String loadType, String officeName, String godownName,
			String idNo, LocalDate fromDate, LocalDate toDate, String clNo) throws Exception {
		if (fromDate != null && toDate != null) {
			if (loadType != null && !loadType.isEmpty()) {
				if (loadType.equals("Single Load")) {
					data.setIdNoList(dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
							.filter(item -> item.getSupplyMode().equals("FOL")
									&& item.getLoadType().equals("Single Load") && !item.getDate().isBefore(fromDate)
									&& !item.getDate().isAfter(toDate) && item.getGodownName().equals(godownName)
									&& item.getVoucherStatus().equals("Approved") && item.getBillEntry().equals(false))
							.map(item -> item.getDcNo()).collect(Collectors.toList()));

					data.getIdNoList().addAll(gtnService.getGtnDataByOffficeName(officeName).stream()
							.filter(item -> item.getGodownName().equals(godownName) && item.getGtnFor().equals("Issue")
									&& (item.getTransactionFor().equals("RH to Buffer (Intra)")
											|| item.getTransactionFor().equals("RH To Other Region Buffer")
											|| item.getTransactionFor().equals("RH To Other Region Direct"))
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate)
									&& item.getVoucherStatus().equals("Approved") && item.getBillEntry().equals(false))
							.filter(item -> {
								GTN receiptGtn;
								try {
									receiptGtn = gtnService.getReceiptGtnDataByGtnNo(item.getGtnNo());
									return receiptGtn != null;
								} catch (Exception e) {
									return false;
								}
							}).map(item -> item.getGtnNo()).collect(Collectors.toList()));
				} else {
					data.setClNoList(dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
							.filter(item -> item.getSupplyMode().equals("FOL")
									&& item.getLoadType().equals("Combined Load")
									&& item.getVoucherStatus().equals("Approved") && item.getBillEntry().equals(false)
									&& item.getGodownName().equals(godownName))
							.map(item -> item.getClNo()).collect(Collectors.toSet()));
					if (clNo != null && !clNo.isEmpty()) {
						data.setIdNoList(dcService.getDcDataByClNo(clNo).stream().map(item -> item.getDcNo())
								.collect(Collectors.toList()));
					}
				}
				if (idNo != null && !idNo.isEmpty()) {
					if (idNo.startsWith("GT")) {
						GTN gtn = gtnService.getGtnDataByGtnNo(idNo);
						data.setIfmsId(gtn.getToIfmsId());
						data.setGodownBuyerName(gtn.getDestination());
						data.setQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(
								gtn.getGtnTableData().stream().mapToDouble(item -> item.getQty()).sum()));
						data.setTransportCharges(gtn.getTransportChargesValue());
						data.setTransportChargesPerQty(gtn.getTransportChargesPerQty());
						data.setTableData(gtn
								.getGtnTableData().stream().map(item -> new ProductClassificationTableBillEntry(null,
										gtn.getProductName(), item.getMrp(), item.getQty(), null, null))
								.collect(Collectors.toList()));
						GTN receiptGtn = gtnService.getReceiptGtnDataByGtnNo(idNo);
						data.setAckQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(receiptGtn.getGtnTableData()
								.stream().mapToDouble(item -> item.getReceivedQty()).sum()));
					} else {
						DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(idNo);
						data.setTransportCharges(deliveryChellan.getTransportChargesValue());
						data.setIfmsId(deliveryChellan.getIfmsId());
						data.setGodownBuyerName(deliveryChellan.getNameOfInstitution());
						data.setQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(deliveryChellan.getTotalQty()));
						data.setTransportChargesPerQty(
								deliveryChellan.getTransportChargesValue() / deliveryChellan.getTotalQty());
						data.setTableData(deliveryChellan
								.getDcTableData().stream().map(item -> new ProductClassificationTableBillEntry(null,
										item.getProductName(), item.getMrp(), item.getQty(), null, null))
								.collect(Collectors.toList()));
					}
				}
			}
		}
	}

	private void transportAndLoadingChargesData(DataForTcBillEntry data, String loadType, String officeName,
			String godownName, String idNo, LocalDate fromDate, LocalDate toDate, String clNo) throws Exception {
		if (fromDate != null && toDate != null) {
			if (loadType != null && !loadType.isEmpty()) {
				if (loadType.equals("Single Load")) {
					data.setIdNoList(dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
							.filter(item -> item.getSupplyMode().equals("FOL")
									&& item.getVoucherStatus().equals("Approved")
									&& item.getLoadType().equals("Single Load") && item.getBillEntry().equals(false)
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate)
									&& item.getGodownName().equals(godownName))
							.map(item -> item.getDcNo()).collect(Collectors.toList()));

					data.getIdNoList().addAll(gtnService.getGtnDataByOffficeName(officeName).stream()
							.filter(item -> item.getGodownName().equals(godownName) && item.getGtnFor().equals("Issue")
									&& (item.getTransactionFor().equals("Buffer To Buffer (Intra)")
											|| item.getTransactionFor().equals("Buffer To Other Region Buffer")
											|| item.getTransactionFor().equals("Buffer To Other Region Direct"))
									&& (item.getTransportCharges().equals("TANFED")
											&& item.getLoadingCharges().equals("TANFED"))
									&& !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate)
									&& item.getVoucherStatus().equals("Approved") && item.getBillEntry().equals(false))
							.map(item -> item.getGtnNo()).collect(Collectors.toList()));
				} else {
					data.setClNoList(dcService.getDeliveryChellanDataByOffficeName(officeName).stream()
							.filter(item -> item.getSupplyMode().equals("FOL")
									&& item.getLoadType().equals("Combined Load") && item.getBillEntry().equals(false)
									&& item.getGodownName().equals(godownName)
									&& item.getVoucherStatus().equals("Approved"))
							.map(item -> item.getClNo()).collect(Collectors.toSet()));
					if (clNo != null && !clNo.isEmpty()) {
						data.setIdNoList(dcService.getDcDataByClNo(clNo).stream().map(item -> item.getDcNo())
								.collect(Collectors.toList()));
					}
				}
				if (idNo != null && !idNo.isEmpty()) {
					if (idNo.startsWith("GT")) {
						GTN gtn = gtnService.getGtnDataByGtnNo(idNo);
						data.setIfmsId(gtn.getToIfmsId());
						data.setGodownBuyerName(gtn.getDestination());
						data.setQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(
								gtn.getGtnTableData().stream().mapToDouble(item -> item.getQty()).sum()));
						data.setTransportCharges(gtn.getTransportChargesValue());
						data.setLoadingCharges(gtn.getLoadingChargesValue());
						data.setLoadingChargesPerQty(gtn.getLoadingChargesPerQty());
						data.setTransportChargesPerQty(gtn.getTransportChargesPerQty());
						data.setTableData(gtn
								.getGtnTableData().stream().map(item -> new ProductClassificationTableBillEntry(null,
										gtn.getProductName(), item.getMrp(), item.getQty(), null, null))
								.collect(Collectors.toList()));
						GTN receiptGtn = gtnService.getReceiptGtnDataByGtnNo(idNo);
						data.setAckQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(receiptGtn.getGtnTableData()
								.stream().mapToDouble(item -> item.getReceivedQty()).sum()));
					} else {
						DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(idNo);
						data.setTransportCharges(deliveryChellan.getTransportChargesValue());
						data.setLoadingCharges(deliveryChellan.getLoadingChargesValue());
						data.setIfmsId(deliveryChellan.getIfmsId());
						data.setGodownBuyerName(deliveryChellan.getNameOfInstitution());
						data.setQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(deliveryChellan.getTotalQty()));
						data.setTransportChargesPerQty(
								deliveryChellan.getTransportChargesValue() / deliveryChellan.getTotalQty());
						data.setLoadingChargesPerQty(
								deliveryChellan.getLoadingChargesValue() / deliveryChellan.getTotalQty());
						data.setTableData(deliveryChellan
								.getDcTableData().stream().map(item -> new ProductClassificationTableBillEntry(null,
										item.getProductName(), item.getMrp(), item.getQty(), null, null))
								.collect(Collectors.toList()));
					}
				}
			}
		}
	}

	@Override
	public ResponseEntity<String> updateBillEnteredDcGtnGrn(String jwt, TcBillEntryTempTable obj) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			tcBillEntryTempTableRepo.save(obj);
			if (obj.getIdNo().startsWith("GT")) {
				GTN gtn = gtnService.getGtnDataByGtnNo(obj.getIdNo());
				gtn.setBillEntry(true);
				gtnService.saveGtn(gtn);
			} else if (obj.getIdNo().startsWith("GR")) {
				GRN grn = grnService.getGrnDataByGrnNo(obj.getIdNo());
				if (obj.getClaimFor().equals("Wagon Clearance Charges")) {
					grn.setWagonBillEntry(true);
				}
				if (obj.getClaimFor().equals("Unloading Charges")) {
					grn.setUnloadingBillEntry(true);
					grn.setBillNo(obj.getClaimBillNo());
				}
				grnService.saveGrn(grn);
			} else {
				DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(obj.getIdNo());
				deliveryChellan.setBillEntry(true);
				deliveryChellan.setBillNo(obj.getClaimBillNo());
				dcService.saveDc(deliveryChellan);
			}
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void revertBillEntryData(String jwt, TcBillEntry obj) throws Exception {
		try {
			obj.getChargesData().forEach(i -> {
				try {
					if (i.getIdNo().startsWith("GT")) {
						GTN gtn;
						gtn = gtnService.getGtnDataByGtnNo(i.getIdNo());
						gtn.setBillEntry(false);
						gtnService.saveGtn(gtn);
					} else if (i.getIdNo().startsWith("GR")) {
						GRN grn = grnService.getGrnDataByGrnNo(i.getIdNo());
						if (i.getClaimFor().equals("Wagon Clearance Charges")) {
							grn.setWagonBillEntry(false);
						}
						if (i.getClaimFor().equals("Unloading Charges")) {
							grn.setUnloadingBillEntry(false);
							grn.setBillNo(null);
						}
						grnService.saveGrn(grn);
					} else {
						DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(i.getIdNo());
						deliveryChellan.setBillEntry(false);
						deliveryChellan.setBillNo(null);
						dcService.saveDc(deliveryChellan);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	public void revertCheckMemo(String jwt, TcCheckMemo tcCheckMemo) throws Exception {
		try {
			TcBillEntry tcBillEntry = tcBillEntryRepo.findByCheckMemoNo(tcCheckMemo.getCheckMemoNo()).get();
			tcBillEntry.setIsTcCheckMemoDone(false);
			tcBillEntryRepo.save(tcBillEntry);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	
	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	@Override
	public ResponseEntity<String> saveTcBillEntry(String jwt, TcBillEntry obj) throws Exception {
		try {
			String code = codeGenerator.generateCheckMemoNoTc(obj.getClaimBillNo());
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.setVoucherStatus("Pending");
			obj.setIsTcCheckMemoDone(false);
			obj.setCheckMemoNo(code);
			List<TcBillEntryTempTable> byClaimBillNo = tcBillEntryTempTableRepo.findByClaimBillNo(obj.getClaimBillNo());
			if (!byClaimBillNo.isEmpty()) {
				byClaimBillNo.forEach(item -> {
					tcBillEntryTempTableRepo.deleteItemsByClaimBillNo(item.getClaimBillNo());
				});
			}
			tcBillEntryRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!" + "\n CheckMemo No :" + code, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private TcCheckMemoRepo tcCheckMemoRepo;

	@Autowired
	private AccountsService accountsService;

	@Override
	public ResponseEntity<String> saveTcCheckMemo(String jwt, TcCheckMemoDto obj) throws Exception {
		try {
			TcCheckMemo tcCm = new TcCheckMemo();
			Vouchers voucher = new Vouchers();
			List<String> jvNoList = new ArrayList<String>();
			obj.getJvData().forEach(item -> {
				voucher.setJournalVoucherData(item);
				try {
					ResponseEntity<String> responseEntity = accountsService
							.saveAccountsVouchersHandler("journalVoucher", voucher, jwt);
					String responseString = responseEntity.getBody();
					if (responseString == null) {
						throw new Exception("No data found");
					}
					String prefix = "JV Number : ";
					int index = responseString.indexOf(prefix);
					jvNoList.add(responseString.substring(index + prefix.length()).trim());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			tcCm.setJvNo(jvNoList);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			tcCm.setEmpId(Arrays.asList(empId));
			tcCm.setVoucherStatus("Pending");

			tcCm.setOfficeName(obj.getOfficeName());
			tcCm.setCheckMemoNo(obj.getCheckMemoNo());
			tcCm.setFinancialYear(obj.getFinancialYear());
			tcCm.setFinancialMonth(obj.getFinancialMonth());
			tcCm.setContractFirm(obj.getContractFirm());
			tcCm.setClaimBillNo(obj.getClaimBillNo());
			tcCm.setClaimBillDate(obj.getClaimBillDate());
			tcCm.setTotalBillValue(obj.getTotalBillValue());
			tcCm.setGstReturnType(obj.getGstReturnType());
			tcCm.setGstNo(obj.getGstNo());
			tcCm.setDate(obj.getDate());

			tcCm.setTotalChargesValue(obj.getTotalChargesValue());
			tcCm.setTotalCGST(obj.getTotalCGST());
			tcCm.setTotalSGST(obj.getTotalSGST());
			tcCm.setTotalPaymentValue(obj.getTotalPaymentValue());
			tcCm.setTotalRecoveryValue(obj.getTotalRecoveryValue());
			tcCm.setRecoveryIfAny(obj.getRecoveryIfAny());
			tcCm.setNetPaymentAfterAdjustment(obj.getNetPaymentAfterAdjustment());
			tcCm.setTcsOrTds(obj.getTcsOrTds());
			tcCm.setRate(obj.getRate());
			tcCm.setPercentageValue(obj.getPercentageValue());
			tcCm.setNetPaymentAfterTdsTcs(obj.getNetPaymentAfterTdsTcs());
			tcCm.setRemarks(obj.getRemarks());
			tcCm.setChargesData(obj.getChargesData());
			tcCm.setRecoveryData(obj.getRecoveryData());

			updateCheckMemoInBillEntry(obj.getCheckMemoNo());
			tcCheckMemoRepo.save(tcCm);
			return new ResponseEntity<String>("Created Successfully!", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void updateCheckMemoInBillEntry(String id) throws Exception {
		try {
			TcBillEntry tcBillEntry = tcBillEntryRepo.findByCheckMemoNo(id).get();
			tcBillEntry.setIsTcCheckMemoDone(true);
			tcBillEntryRepo.save(tcBillEntry);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForTcCheckMemo getDataForTcCheckMemo(String jwt, String officeName, String checkMemoNo, String month)
			throws Exception {
		try {
			DataForTcCheckMemo data = new DataForTcCheckMemo();
			if (officeName != null && !officeName.isEmpty()) {
				data.setCheckMemoNoList(tcBillEntryRepo.findByOfficeName(officeName).stream().filter(
						item -> item.getIsTcCheckMemoDone().equals(false) && item.getVoucherStatus().equals("Approved"))
						.map(item -> item.getCheckMemoNo()).collect(Collectors.toList()));
				if (month != null && !month.isEmpty()) {
					data.setTcCheckMemoData(fetchTcCheckMemoByOfficeName(officeName).stream().filter(item -> {
						String cmMonth = String.format("%s%s%04d", item.getDate().getMonth(), " ",
								item.getDate().getYear());
						logger.info(month);
						logger.info("{}", cmMonth.equals(month));
						return cmMonth.equals(month);
					}).collect(Collectors.toList()));
				}
				if (checkMemoNo != null && !checkMemoNo.isEmpty()) {
					TcBillEntry tcBillEntry = tcBillEntryRepo.findByCheckMemoNo(checkMemoNo).get();
					data.setFinancialYear(tcBillEntry.getFinancialYear());
					data.setFinancialMonth(tcBillEntry.getFinancialMonth());
					data.setContractFirm(tcBillEntry.getContractFirm());
					ContractorInfo contractorInfo = masterService.getContarctorInfoByOfficeName(jwt, officeName)
							.stream().filter(item -> item.getContractFirm().equals(tcBillEntry.getContractFirm()))
							.collect(Collectors.toList()).get(0);
					data.setGstNo(contractorInfo.getGstNo());
					data.setGstReturnType(contractorInfo.getGstReturnType());
					data.setGstData(contractorInfo.getGstData());
					data.setClaimBillDate(tcBillEntry.getClaimBillDate());
					data.setClaimBillNo(tcBillEntry.getClaimBillNo());
					data.setTotalBillValue(tcBillEntry.getTotalBillValue());
					data.setChargesData(mapChargesData(tcBillEntry));
					data.setRecoveryData(mapRecoveryData(tcBillEntry));
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private List<TcCheckMemoChargesTable> mapChargesData(TcBillEntry tcBillEntry) {
		List<TcCheckMemoChargesTable> chargesData = new ArrayList<TcCheckMemoChargesTable>();

		Optional<TcCheckMemoChargesTable> wagonChargeEntry = tcBillEntry.getChargesData().stream()
				.filter(item -> "Wagon Clearance Charges".equals(item.getClaimFor()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					double totalCharges = list.stream()
							.mapToDouble(TcBillEntryChargesTable::getCalcWagonClearanceCharges).sum();
					double totalQty = list.stream().mapToDouble(TcBillEntryChargesTable::getAckQty).sum();
					return list.isEmpty() ? Optional.empty()
							: Optional.of(new TcCheckMemoChargesTable(null, "Wagon Clearance Charges", totalQty,
									totalCharges));
				}));
		wagonChargeEntry.ifPresent(chargesData::add);

		Optional<TcCheckMemoChargesTable> unloadingChargeEntry = tcBillEntry.getChargesData().stream()
				.filter(item -> "Unloading Charges".equals(item.getClaimFor()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					double totalCharges = list.stream().mapToDouble(TcBillEntryChargesTable::getCalcUnloadingCharges)
							.sum();
					double totalQty = list.stream().mapToDouble(TcBillEntryChargesTable::getAckQty).sum();
					return list.isEmpty() ? Optional.empty()
							: Optional
									.of(new TcCheckMemoChargesTable(null, "Unloading Charges", totalQty, totalCharges));
				}));
		unloadingChargeEntry.ifPresent(chargesData::add);

		Optional<TcCheckMemoChargesTable> transportChargeEntry = tcBillEntry.getChargesData().stream()
				.filter(item -> "Transport Only".equals(item.getClaimFor())
						|| "Transport + Loading Charges".equals(item.getClaimFor()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					double totalCharges = list.stream().mapToDouble(TcBillEntryChargesTable::getCalcTransportCharges)
							.sum();
					double totalQty = list.stream().mapToDouble(TcBillEntryChargesTable::getAckQty).sum();
					return list.isEmpty() ? Optional.empty()
							: Optional.of(new TcCheckMemoChargesTable(null, "Transport Only", totalQty, totalCharges));
				}));
		transportChargeEntry.ifPresent(chargesData::add);

		Optional<TcCheckMemoChargesTable> loadingChargeEntry = tcBillEntry.getChargesData().stream()
				.filter(item -> "Transport + Loading Charges".equals(item.getClaimFor()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
					double totalCharges = list.stream().mapToDouble(TcBillEntryChargesTable::getCalcLoadingCharges)
							.sum();
					double totalQty = list.stream().mapToDouble(TcBillEntryChargesTable::getAckQty).sum();
					return list.isEmpty() ? Optional.empty()
							: Optional.of(new TcCheckMemoChargesTable(null, "Loading Only", totalQty, totalCharges));
				}));
		loadingChargeEntry.ifPresent(chargesData::add);
		return chargesData;
	}

	private List<StockRecoveryTable> mapRecoveryData(TcBillEntry tcBillEntry) {
		return tcBillEntry.getChargesData().stream()
				.filter(item -> (item.getClaimFor().equals("Transport Only")
						|| item.getClaimFor().equals("Transport + Loading Charges")) && item.getDisallowedQty() != null)
				.flatMap(item -> item.getTableData().stream()
						.map(itemData -> new StockRecoveryTable(null, item.getIdNo(), itemData.getProductName(),
								itemData.getMrp(), itemData.getDisallowedQty(), itemData.getDisallowedValue())))
				.collect(Collectors.toList());
	}

	@Override
	public List<TcBillEntry> fetchTcBillEntryByOfficeName(String officeName) {
		return tcBillEntryRepo.findByOfficeName(officeName);
	}

	@Override
	public List<TcCheckMemo> fetchTcCheckMemoByOfficeName(String officeName) {
		return tcCheckMemoRepo.findByOfficeName(officeName);
	}

	@Override
	public ResponseEntity<String> revertTcDataInTempTable(Long id) throws Exception {
		try {
			TcBillEntryTempTable tcBillEntryTempTable = tcBillEntryTempTableRepo.findById(id).get();
			if (tcBillEntryTempTable.getIdNo().startsWith("GT")) {
				GTN gtn = gtnService.getGtnDataByGtnNo(tcBillEntryTempTable.getIdNo());
				gtn.setBillEntry(false);
				gtnService.saveGtn(gtn);
			} else if (tcBillEntryTempTable.getIdNo().startsWith("GR")) {
				GRN grn = grnService.getGrnDataByGrnNo(tcBillEntryTempTable.getIdNo());
				if (tcBillEntryTempTable.getClaimFor().equals("Wagon Clearance Charges")) {
					grn.setWagonBillEntry(false);
				} else {
					grn.setUnloadingBillEntry(false);
				}
				grnService.saveGrn(grn);
			} else {
				DeliveryChellan deliveryChellan = dcService.getDcDataByDcNo(tcBillEntryTempTable.getIdNo());
				deliveryChellan.setBillEntry(false);
				dcService.saveDc(deliveryChellan);
			}
			tcBillEntryTempTableRepo.delete(tcBillEntryTempTable);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateAccJvAndPv(TcCheckMemo item, String jwt) throws Exception {
		item.getJvNo().forEach(jvNo -> {
			try {
				Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
				accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
						String.valueOf(jv.getJournalVoucherData().getId()), "journalVoucher", null), jwt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		if (item.getPvNo() != null) {
			Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", item.getPvNo(), jwt);
			accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
					String.valueOf(pv.getPaymentVoucherData().getId()), "paymentVoucher", null), jwt);
		}
	}

	@Override
	public TcCheckMemo getTcCheckMemoByCheckMemoNo(String checkMemoNo) throws Exception {
		try {
			return tcCheckMemoRepo.findByCheckMemoNo(checkMemoNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updatePvNoTcCheckMemo(Long id, String pvNo) throws Exception {
		try {
			TcCheckMemo tcCheckMemo = tcCheckMemoRepo.findById(id).get();
			String prefix = "Voucher Number : ";
			int index = pvNo.indexOf(prefix);
			String code = pvNo.substring(index + prefix.length()).trim();
			tcCheckMemo.setPvNo(code);
			tcCheckMemoRepo.save(tcCheckMemo);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
