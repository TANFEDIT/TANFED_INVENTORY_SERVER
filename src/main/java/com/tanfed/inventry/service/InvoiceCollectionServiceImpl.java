package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.InvoiceRepo;
import com.tanfed.inventry.response.InvoiceCollectionResponseData;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class InvoiceCollectionServiceImpl implements InvoiceCollectionService {

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private MasterService masterService;

	@Autowired
	private UserService userService;

	@Autowired
	private CodeGenerator codeGenerator;

	private static Logger logger = LoggerFactory.getLogger(InvoiceCollectionServiceImpl.class);

	@Override
	public ResponseEntity<String> updateInvoiceCollection(List<InvoiceCollectionObject> obj, String jwt)
			throws Exception {
		try {
			logger.info(jwt);
			final String[] code = new String[1];
			if ("presentToCCB".equals(obj.get(0).getCollectionProcess())) {
				code[0] = codeGenerator.icmNoGenerator(obj.get(0).getOfficeName());
			}
			obj.forEach(temp -> {
				if ("invoiceAckEntry".equals(temp.getCollectionProcess())) {
					Invoice invoice = invoiceRepo.findByInvoiceNo(temp.getInvoiceNo()).get();
					invoice.setAckQty(temp.getAckQty());
					invoice.setAckEntryDate(temp.getAckEntryDate());
					invoice.setCcbBranch(temp.getCcbBranch());
					invoice.setVoucherStatusICP1("Pending");
					invoiceRepo.save(invoice);
				}
				if ("invoiceCollectionAvailable".equals(temp.getCollectionProcess())) {
					Invoice invoice = invoiceRepo.findByInvoiceNo(temp.getInvoiceNo()).get();
					invoice.setVoucherStatusICP2("Pending");
					invoice.setDueDate(temp.getDueDate());
					invoice.setCollectionMethod(temp.getCollectionMethod());

					if (!"AdjReceipt".equals(temp.getCollectionMethod())) {
						invoice.setAddedToPresentDate(temp.getAddedToPresentDate());
					}
					invoiceRepo.save(invoice);
				}
				if ("presentToCCB".equals(temp.getCollectionProcess())) {
					Invoice invoice = invoiceRepo.findByInvoiceNo(temp.getInvoiceNo()).get();
					invoice.setVoucherStatusICP3("Pending");
					invoice.setCollectionMethod(temp.getCollectionProcess());
					invoice.setDateOfPresent(temp.getDateOfPresent());
					invoice.setIcmNo(code[0]);
					invoice.setIsShort(false);
					invoiceRepo.save(invoice);
				}
				if ("collectionUpdate".equals(temp.getCollectionProcess())) {
					Invoice invoice = invoiceRepo.findByInvoiceNo(temp.getInvoiceNo()).get();
					if (invoice.getDateOfCollectionFromCcb() == null) {
						invoice.setDateOfCollectionFromCcb(new ArrayList<>(List.of(temp.getDateOfCollectionFromCcb())));
						invoice.setCollectionValue(Arrays.asList(temp.getCollectionValue()));
					} else {
						invoice.getDateOfCollectionFromCcb().add(temp.getDateOfCollectionFromCcb());
						invoice.getCollectionValue().add(temp.getCollectionValue());
					}
					invoice.setTransferDone(false);
					invoice.setIsShort(temp.getIsShort());
					invoiceRepo.save(invoice);

				}
			});
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public InvoiceCollectionResponseData getDataForInvoiceCollections(String officeName, String activity,
			String monthOfSales, LocalDate fromDate, LocalDate toDate, String invoiceType, String materialCenter,
			String ccbBranch, LocalDate ackEntryDate, LocalDate addedToPresentDate, LocalDate dueDate, String icmNo,
			String collectionProcess, String accountNo, String branchName, LocalDate date, String jwt,
			String transferType, String toBranchName) throws Exception {

		InvoiceCollectionResponseData data = new InvoiceCollectionResponseData();
		if (officeName != null && !officeName.isEmpty()) {
			if (activity != null && !activity.isEmpty()) {
				List<Invoice> collect = invoiceRepo.findByActivityAndOfficeName(activity, officeName).stream()
						.filter(temp -> temp.getCollectionMode().equals("Through CC")).collect(Collectors.toList());
				if (collectionProcess.equals("invoiceAckEntry")) {
					invoiceAckEntryData(data, officeName, collect, monthOfSales, fromDate, toDate, invoiceType,
							materialCenter, jwt);
				}
				if ("invoiceCollectionAvailable".equals(collectionProcess)) {
					invoiceCollectionAvailableData(data, collect, activity, officeName, invoiceType, ccbBranch,
							ackEntryDate, jwt);
				}
				if ("presentToCCB".equals(collectionProcess)) {
					presentToCCBData(data, collect, invoiceType, ccbBranch, dueDate, addedToPresentDate);
				}
				if ("collectionUpdate".equals(collectionProcess)) {
					collectionUpdateData(data, collect, icmNo);
				}
			}
		}
		return data;
	}

	public void invoiceAckEntryData(InvoiceCollectionResponseData data, String officeName, List<Invoice> collect,
			String monthOfSales, LocalDate fromDate, LocalDate toDate, String invoiceType, String materialCenter,
			String jwt) throws Exception {
		List<String> godownList = masterService.getGodownInfoByOfficeNameHandler(jwt, officeName).stream()
				.map(GodownInfo::getGodownName).collect(Collectors.toList());
		godownList.add("Direct Material Center");
		data.setMaterialCenterLst(godownList);
		if (monthOfSales != null && !monthOfSales.isEmpty()) {
			double[] result = collect.stream().filter(temp -> temp.getDate().getMonth().toString().equals(monthOfSales))
					.mapToDouble(temp -> temp.getNetInvoiceAdjustment()).collect(() -> new double[2], (acc, value) -> {
						acc[0]++;
						acc[1] += value;
					}, (acc1, acc2) -> {
						acc1[0] += acc2[0];
						acc1[1] += acc2[1];
					});
			data.setNoOfInvoicesCreated((int) result[0]);
			data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(result[1]));
			data.setNoOfInvoicesAckReceived(Math.toIntExact(collect.stream().filter(temp -> {
				return temp.getAckQty() != null && temp.getDate().getMonth().toString().equals(monthOfSales)
						&& temp.getAckEntryDate() != null;
			}).count()));
		}
		if (fromDate != null && toDate != null) {
			if (invoiceType != null && !invoiceType.isEmpty()) {
				if (materialCenter != null && !materialCenter.isEmpty()) {
					data.setTableData(collect.stream().filter(temp -> {
						Boolean dateMatch = !temp.getDate().isBefore(fromDate) && !temp.getDate().isAfter(toDate);
						return dateMatch && invoiceType.equals(temp.getSupplyTo())
								&& materialCenter.equals(temp.getGodownName()) && temp.getAckQty() == null
								&& temp.getAckEntryDate() == null;
					}).map(item -> new InvoiceCollectionP1TableData(item.getInvoiceNo(), item.getDate(),
							item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getNetInvoiceAdjustment()),
							item.getDate().plusDays(item.getCreditDays()), item.getCcbBranch(), item.getGodownName(),
							null, null, null, null)).collect(Collectors.toList()));
				}
			}
		}
	}

	public void invoiceCollectionAvailableData(InvoiceCollectionResponseData data, List<Invoice> collect,
			String activity, String officeName, String invoiceType, String ccbBranch, LocalDate ackEntryDate,
			String jwt) {
		List<Invoice> NoOfAvlAckInvoices = collect.stream().filter(temp -> {
			return temp.getAckQty() != null && temp.getAckEntryDate() != null && temp.getAddedToPresentDate() == null;
		}).collect(Collectors.toList());

		Set<String> ccbBranchlst = new HashSet<String>();
		Set<LocalDate> ackEntryDatelst = new HashSet<LocalDate>();

		NoOfAvlAckInvoices.forEach(temp -> {
			ccbBranchlst.add(temp.getCcbBranch());
			ackEntryDatelst.add(temp.getAckEntryDate());
		});
		data.setNoOfAvlAckInvoices(NoOfAvlAckInvoices.size());
		data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(
				NoOfAvlAckInvoices.stream().mapToDouble(temp -> temp.getNetInvoiceAdjustment()).sum()));
		data.setCcbBranchLst(ccbBranchlst);
		data.setAckEntryDate(ackEntryDatelst);
		data.setAdjTableData(invoiceRepo.findByActivityAndOfficeName(activity, officeName).stream()
				.filter(temp -> null == temp.getAddedToPresentDate() && null != temp.getVoucherStatusICP2()
						&& !temp.getVoucherStatusICP2().equals("Approved") && temp.getAdjReceiptNo() != null
						&& temp.getCollectionMethod().equals("AdjReceipt"))
				.map(item -> {
					AdjustmentReceiptVoucher adj = null;
					if (item.getCollectionMethod().equals("AdjReceipt")) {
						if (item.getAdjReceiptNo() != null) {
							try {
								Vouchers adjv = accountsService.getAccountsVoucherByVoucherNoHandler(
										"adjustmentReceiptVoucher", item.getAdjReceiptNo().get(0), jwt);
								adj = adjv.getAdjustmentReceiptVoucherData();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					return new InvoiceCollectionP1TableData(item.getInvoiceNo(), item.getDate(), item.getIfmsId(),
							item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getNetInvoiceAdjustment()),
							item.getDate().plusDays(item.getCreditDays()), item.getCcbBranch(), null, null, adj, null,
							null);
				}).collect(Collectors.toList()));
		if (invoiceType != null && !invoiceType.isEmpty()) {
			if (ccbBranch != null && !ccbBranch.isEmpty()) {
				data.setTableData(collect.stream().filter(temp -> {
					return temp.getAckEntryDate() != null && ackEntryDate.equals(temp.getAckEntryDate())
							&& invoiceType.equals(temp.getSupplyTo()) && ccbBranch.equals(temp.getCcbBranch())
							&& temp.getCollectionMethod() == null && temp.getAddedToPresentDate() == null
							&& temp.getVoucherStatusICP1().equals("Approved");
				}).map(item -> new InvoiceCollectionP1TableData(item.getInvoiceNo(), item.getDate(), item.getIfmsId(),
						item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getNetInvoiceAdjustment()),
						item.getDate().plusDays(item.getCreditDays()), item.getCcbBranch(), null, null, null, null,
						null)).collect(Collectors.toList()));
			}
		}
	}

	public void presentToCCBData(InvoiceCollectionResponseData data, List<Invoice> collect, String invoiceType,
			String ccbBranch, LocalDate dueDate, LocalDate addedToPresentDate) {
		List<Invoice> NoOfInvoicesAvlToPresent = collect.stream().filter(temp -> {
			return temp.getAddedToPresentDate() != null && !"AdjReceipt".equals(temp.getCollectionMethod())
					&& temp.getDateOfPresent() == null;
		}).collect(Collectors.toList());
		Set<String> ccbBranchlst = new HashSet<String>();
		Set<LocalDate> dueDatelst = new HashSet<LocalDate>();
		Set<LocalDate> addedToPresentDatelst = new HashSet<LocalDate>();
		NoOfInvoicesAvlToPresent.forEach(temp -> {
			ccbBranchlst.add(temp.getCcbBranch());
			addedToPresentDatelst.add(temp.getAddedToPresentDate());
			dueDatelst.add(temp.getDueDate());
		});

		data.setNoOfInvoicesAvlToPresent(NoOfInvoicesAvlToPresent.size());
		data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(
				NoOfInvoicesAvlToPresent.stream().mapToDouble(temp -> temp.getNetInvoiceAdjustment()).sum()));
		data.setCcbBranchLst(ccbBranchlst);
		data.setAddedToPresentDate(addedToPresentDatelst);
		data.setDueDate(dueDatelst);

		if (invoiceType != null && !invoiceType.isEmpty()) {
			if (ccbBranch != null && !ccbBranch.isEmpty()) {
				if (dueDate != null) {
					data.setTableData(collect.stream().filter(temp -> {
						return temp.getAddedToPresentDate() != null && temp.getDueDate() != null
								&& addedToPresentDate.equals(temp.getAddedToPresentDate())
								&& !"AdjReceipt".equals(temp.getCollectionMethod())
								&& temp.getVoucherStatusICP2().equals("Approved")
								&& invoiceType.equals(temp.getSupplyTo()) && ccbBranch.equals(temp.getCcbBranch())
								&& temp.getDueDate().isEqual(dueDate) && temp.getDateOfPresent() == null;
					}).map(item -> new InvoiceCollectionP1TableData(item.getInvoiceNo(), item.getDate(),
							item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getNetInvoiceAdjustment()), null,
							item.getCcbBranch(), null, null, null, null, null)).collect(Collectors.toList()));
				}
			}
		}
	}

	public void collectionUpdateData(InvoiceCollectionResponseData data, List<Invoice> collect, String icmNo) {
		List<Invoice> NoOfPresented = collect.stream().filter(temp -> {
			return temp.getDateOfPresent() != null && temp.getIsShort().equals(false)
					&& (temp.getCollectionValue() == null || (temp.getCollectionValue().stream()
							.mapToDouble(item -> item).sum() < temp.getNetInvoiceAdjustment()));
		}).collect(Collectors.toList());

		data.setIcmNoList(NoOfPresented.stream().map(item -> item.getIcmNo()).collect(Collectors.toSet()));

		data.setNoOfInvoicesPresented(NoOfPresented.size());
		data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(
				NoOfPresented.stream().mapToDouble(temp -> temp.getNetInvoiceAdjustment()).sum()));

		if (icmNo != null && !icmNo.isEmpty()) {
			data.setTableData(collect.stream()
					.filter(temp -> temp.getDateOfPresent() != null && icmNo.equals(temp.getIcmNo())
							&& temp.getIsShort().equals(false) && temp.getVoucherStatusICP3().equals("Approved")
							&& (temp.getCollectionValue() == null || (temp.getCollectionValue().stream()
									.mapToDouble(sum -> sum).sum() < temp.getNetInvoiceAdjustment())))
					.map(item -> new InvoiceCollectionP1TableData(item.getInvoiceNo(), item.getDate(), item.getIfmsId(),
							item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
							RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getNetInvoiceAdjustment()), null,
							item.getCcbBranch(), null, null, null, fetchCollectedValue(item), item.getIsShort()))
					.collect(Collectors.toList()));
		}
	}

	private Double fetchCollectedValue(Invoice inv) {
		return inv.getCollectionValue() == null ? 0.0
				: inv.getCollectionValue().stream().mapToDouble(item -> item).sum();
	}

	@Override
	public InvoiceCollectionResponseData getCollectionAbstractData(String officeName, String branchName,
			String accountNo, String monthOfFundTransfer, String jwt) throws Exception {
		InvoiceCollectionResponseData data = new InvoiceCollectionResponseData();
//		List<BankInfo> bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName);
//		data.setBranchNameList(bankInfo.stream().filter(item -> item.getAccountType().equals("Non PDS A/c Fert"))
//				.map(BankInfo::getBranchName).collect(Collectors.toSet()));
//		if (branchName != null && !branchName.isEmpty()) {
//			data.setAccountNoList(bankInfo.stream().filter(temp -> {
//				return branchName.equals(temp.getBranchName()) && "Non PDS A/c Fert".equals(temp.getAccountType());
//			}).map(BankInfo::getAccountNumber).collect(Collectors.toList()));
//			if (monthOfFundTransfer != null && !monthOfFundTransfer.isEmpty()) {
//
//				String[] monthAndYr = monthOfFundTransfer.split(" ");
//				YearMonth yearMonth = YearMonth.of(Integer.valueOf(monthAndYr[1]),
//						Month.valueOf(monthAndYr[0].toUpperCase()));
//				int lengthOfMonth = yearMonth.lengthOfMonth();
//
//				List<FundTransferMonthAndBranchAbstractTableData> abstractTable = new ArrayList<FundTransferMonthAndBranchAbstractTableData>();
//
//				if (accountNo != null && !accountNo.isEmpty()) {
//					List<FundTransfer> ftbyDateOfTransfer = fundTransferRepo.findByOfficeName(officeName).stream()
//							.filter(item -> item.getBranchName().equals(branchName)
//									&& item.getAccountNo().equals(Long.valueOf(accountNo))
//									&& !item.getDate().isAfter(yearMonth.atDay(1)))
//							.collect(Collectors.toList());
//
//					List<Invoice> invoices = invoiceRepo.findByOfficeName(officeName).stream()
//							.filter(item -> item.getCollectionMode().equals("Through CC")
//									&& item.getCcbBranch().equals(branchName) && item.getTransferDone().equals(false)
//									&& null != item.getDateOfCollectionFromCcb()
//									&& item.getDateOfCollectionFromCcb().isBefore(yearMonth.atDay(1)))
//							.collect(Collectors.toList());
//
//					Double ob = ftbyDateOfTransfer.isEmpty() ? 0.0
//							: ftbyDateOfTransfer.get(ftbyDateOfTransfer.size() - 1).getClosingBalance()
//									+ invoices.stream().mapToDouble(item -> item.getCollectionValue()).sum();
//
//					for (int i = 1; i <= lengthOfMonth; i++) {
//						FundTransferMonthAndBranchAbstractTableData tableObj = new FundTransferMonthAndBranchAbstractTableData();
//
//						LocalDate localDate = yearMonth.atDay(i);
//						tableObj.setDate(localDate);
//
//						List<FundTransfer> byDateOfTransfer = fundTransferRepo.findByDate(localDate).stream()
//								.filter(item -> item.getOfficeName().equals(officeName)
//										&& item.getBranchName().equals(branchName)
//										&& item.getAccountNo().equals(Long.valueOf(accountNo))
//										&& item.getDate().equals(localDate))
//								.collect(Collectors.toList());
//
//						List<Invoice> byDateOfCollectionFromCcb = invoiceRepo.findByDateOfCollectionFromCcb(localDate)
//								.stream()
//								.filter(item -> item.getCollectionMode().equals("Through CC")
//										&& item.getVoucherStatusICP4().equals("Approved")
//										&& item.getCcbBranch().equals(branchName))
//								.collect(Collectors.toList());
//
//						tableObj.setOpeningBalance(RoundToDecimalPlace.roundToTwoDecimalPlaces(ob));
//
//						tableObj.setCollection(RoundToDecimalPlace.roundToTwoDecimalPlaces(
//								byDateOfCollectionFromCcb.stream().mapToDouble(Invoice::getCollectionValue).sum()));
//
//						tableObj.setIbrAmount(fundTransferRepo.findByToAccountNo(Long.valueOf(accountNo)).stream()
//								.filter(temp -> temp.getDate().equals(localDate))
//								.mapToDouble(item -> item.getIbtAmount()).sum());
//
//						tableObj.setTotal(RoundToDecimalPlace
//								.roundToTwoDecimalPlaces(ob + tableObj.getCollection() + tableObj.getIbrAmount()));
//
//						tableObj.setCurrentTransfer(RoundToDecimalPlace.roundToTwoDecimalPlaces(
//								byDateOfTransfer.stream().mapToDouble(FundTransfer::getCurrentTransfer).sum()));
//
//						tableObj.setIbtAmount(RoundToDecimalPlace.roundToTwoDecimalPlaces(
//								byDateOfTransfer.stream().filter(item -> item.getIbtAmount() != null)
//										.mapToDouble(FundTransfer::getIbtAmount).sum()));
//
//						tableObj.setBankCharges(RoundToDecimalPlace.roundToTwoDecimalPlaces(
//								byDateOfTransfer.stream().mapToDouble(FundTransfer::getBankCharges).sum()));
//
//						tableObj.setOthers(RoundToDecimalPlace.roundToTwoDecimalPlaces(
//								byDateOfTransfer.stream().mapToDouble(FundTransfer::getOthers).sum()));
//
//						tableObj.setClosingBalance(tableObj.getTotal() - (tableObj.getCurrentTransfer()
//								+ tableObj.getIbtAmount() + tableObj.getBankCharges() + tableObj.getOthers()));
//
//						ob = tableObj.getClosingBalance();
//						abstractTable.add(tableObj);
//					}
//				}
//				data.setAbstractTable(abstractTable);
//			}
//		}
		return data;
	}

	@Override
	public ICViewAplApdData getINCLViewAPlApd(String formType, LocalDate fromDate, LocalDate toDate, String officeName,
			String voucherStatus, String icmNo, String jwt) throws Exception {
		try {
			ICViewAplApdData data = new ICViewAplApdData();
			List<ICP1Data> invoiceLst = new ArrayList<ICP1Data>();
			List<ICP1Data> invoiceFilteredLst = new ArrayList<ICP1Data>();
			if (fromDate != null && toDate != null) {
				invoiceRepo.findByOfficeName(officeName).stream().filter(temp -> {
					LocalDate date = temp.getDate();
					Boolean statusMatch = false;
					Boolean pageMatch = false;
					if (formType.equals("invoiceAckEntry") && temp.getAckEntryDate() != null) {
						statusMatch = voucherStatus.isEmpty() || voucherStatus.equals(temp.getVoucherStatusICP1());
						pageMatch = true;
					}
					if (formType.equals("invoiceCollectionAvailable") && temp.getCollectionMethod() != null) {
						statusMatch = voucherStatus.isEmpty() || voucherStatus.equals(temp.getVoucherStatusICP2());
						pageMatch = true;
					}
					if (formType.equals("presentToCCB") && temp.getDateOfPresent() != null) {
						statusMatch = voucherStatus.isEmpty() || voucherStatus.equals(temp.getVoucherStatusICP3());
						pageMatch = true;
					}
					if (formType.equals("collectionUpdate") && temp.getDateOfCollectionFromCcb() != null) {
						statusMatch = voucherStatus.isEmpty();
						pageMatch = true;
					}
					return pageMatch && !date.isBefore(fromDate) && !date.isAfter(toDate) && statusMatch;
				}).collect(Collectors.toList()).forEach(temp -> {
					invoiceFilteredLst.add(mapInvoiceDateToIC(temp, formType, null));
				});
				accountsService.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
						.getSundryDrOb().stream().filter(temp -> {
							LocalDate date = temp.getInvoiceDate();
							Boolean statusMatch = false;
							Boolean pageMatch = false;
							if (formType.equals("invoiceAckEntry") && temp.getAckEntryDate() != null) {
								statusMatch = voucherStatus.isEmpty()
										|| voucherStatus.equals(temp.getVoucherStatusICP1());
								pageMatch = true;
							}
							if (formType.equals("invoiceCollectionAvailable") && temp.getCollectionMethod() != null) {
								statusMatch = voucherStatus.isEmpty()
										|| voucherStatus.equals(temp.getVoucherStatusICP2());
								pageMatch = true;
							}
							if (formType.equals("presentToCCB") && temp.getDateOfPresent() != null) {
								statusMatch = voucherStatus.isEmpty()
										|| voucherStatus.equals(temp.getVoucherStatusICP3());
								pageMatch = true;
							}
							if (formType.equals("collectionUpdate") && temp.getDateOfCollectionFromCcb() != null) {
								statusMatch = voucherStatus.isEmpty();
								pageMatch = true;
							}
							return pageMatch && !date.isBefore(fromDate) && !date.isAfter(toDate) && statusMatch;
						}).collect(Collectors.toList()).forEach(temp -> {
							invoiceFilteredLst.add(mapSdrObDateToIC(temp, formType, null));
						});
			}
			switch (formType) {
			case "invoiceAckEntry": {
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP1ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP1ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP1ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(invoiceRepo.findICP1ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}
			case "invoiceCollectionAvailable": {
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP2ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, jwt)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP2ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, jwt)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP2ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, jwt)).collect(Collectors.toList()));
						invoiceLst.addAll(invoiceRepo.findICP2ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, jwt)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}
			case "presentToCCB": {
				data.setIcmNoList(invoiceRepo.findByOfficeName(officeName).stream().filter(
						item -> item.getDateOfPresent() != null && item.getCollectionMethod().equals("presentToCCB"))
						.map(item -> item.getIcmNo()).collect(Collectors.toSet()));
				data.getIcmNoList()
						.addAll(accountsService
								.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
								.getSundryDrOb().stream()
								.filter(item -> item.getDateOfPresent() != null
										&& item.getCollectionMethod().equals("presentToCCB"))
								.map(item -> item.getIcmNo()).collect(Collectors.toSet()));
				if (icmNo != null && !icmNo.isEmpty()) {
					if (icmNo.startsWith("OB")) {
						List<ICP1Data> byIcmNo = accountsService
								.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
								.getSundryDrOb().stream()
								.filter(item -> item.getDateOfPresent() != null
										&& item.getCollectionMethod().equals("presentToCCB")
										&& item.getIcmNo().equals(icmNo))
								.map(temp -> mapSdrObDateToIC(temp, formType, jwt)).collect(Collectors.toList());
						invoiceLst.addAll(byIcmNo);
					} else {
						List<ICP1Data> byIcmNo = invoiceRepo.findByIcmNo(icmNo).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList());
						invoiceLst.addAll(byIcmNo);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}
			case "collectionUpdate": {
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP4ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP4ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP4ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(invoiceRepo.findICP4ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(mapSdrobData(officeName, jwt, voucherStatus, formType));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}

			case "icm": {
				data.setIcmNoList(invoiceRepo.findByOfficeName(officeName).stream().filter(
						item -> item.getDateOfPresent() != null && item.getVoucherStatusICP3().equals("Approved"))
						.map(item -> item.getIcmNo()).collect(Collectors.toSet()));
				data.getIcmNoList()
						.addAll(accountsService
								.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
								.getSundryDrOb().stream()
								.filter(item -> item.getDateOfPresent() != null
										&& item.getVoucherStatusICP3().equals("Approved"))
								.map(item -> item.getIcmNo()).collect(Collectors.toSet()));
				if (icmNo != null && !icmNo.isEmpty()) {
					if (icmNo.startsWith("OB")) {
						List<SundryDrOb> byIcmNo = accountsService
								.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
								.getSundryDrOb().stream()
								.filter(item -> item.getDateOfPresent() != null
										&& item.getCollectionMethod().equals("presentToCCB")
										&& item.getIcmNo().equals(icmNo))
								.collect(Collectors.toList());

						Set<AdjustmentReceiptVoucher> adjData = byIcmNo.stream()
								.flatMap(i -> i.getAdjReceipt().stream()).collect(Collectors.toSet());
						if (adjData.isEmpty()) {
							data.setAdjv(null);
						} else {
							data.setAdjv(adjData.stream().toList());
						}
						data.setInvoice(byIcmNo.stream().map(item -> {
							Long accountNo = 0l;
							try {
								BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName)
										.stream()
										.filter(itemData -> itemData.getAccountType().equals("Non PDS A/c Fert")
												&& itemData.getBranchName().equals(item.getCcbBranch()))
										.collect(Collectors.toList()).get(0);
								accountNo = bankInfo.getAccountNumber();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return new ICP1Data(item.getId(), item.getActivity(), null, item.getInvoiceNo(),
									item.getInvoiceDate(), item.getIfmsId(), item.getNameOfInstitution(),
									item.getDistrict(), item.getQty(), null, null, item.getAmount(), item.getDueDate(),
									null, item.getCcbBranch(), accountNo, item.getDateOfPresent(), null, null, null,
									null, null, null);
						}).collect(Collectors.toList()));
						BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName).stream()
								.filter(itemData -> itemData.getAccountNumber()
										.equals(data.getInvoice().get(0).getAccountNo()))
								.collect(Collectors.toList()).get(0);
						data.setBankName(bankInfo.getBankName());
						data.setDoor(bankInfo.getDoor());
						data.setStreet(bankInfo.getStreet());
						data.setDistrict(bankInfo.getDistrict());
						data.setPincode(bankInfo.getPincode());

					} else {
						List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(icmNo);
						Set<String> adjNoList = byIcmNo.stream().filter(i -> i.getAdjReceiptNo() != null)
								.flatMap(i -> i.getAdjReceiptNo().stream()).collect(Collectors.toSet());
						if (adjNoList.isEmpty() || adjNoList == null) {
							data.setAdjv(null);
						} else {
							List<AdjustmentReceiptVoucher> adj = new ArrayList<AdjustmentReceiptVoucher>();
							adjNoList.forEach(item -> {
								Vouchers adjv;
								try {
									adjv = accountsService.getAccountsVoucherByVoucherNoHandler(
											"adjustmentReceiptVoucher", item, jwt);
									adj.add(adjv.getAdjustmentReceiptVoucherData());
								} catch (Exception e) {
									e.printStackTrace();
								}
							});
							data.setAdjv(adj);
						}
						data.setInvoice(byIcmNo.stream().map(item -> {
							Long accountNo = 0l;
							try {
								BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName)
										.stream()
										.filter(itemData -> itemData.getAccountType().equals("Non PDS A/c Fert")
												&& itemData.getBranchName().equals(item.getCcbBranch()))
										.collect(Collectors.toList()).get(0);
								accountNo = bankInfo.getAccountNumber();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return new ICP1Data(item.getId(), item.getActivity(), item.getGodownName(),
									item.getInvoiceNo(), item.getDate(), item.getIfmsId(), item.getNameOfInstitution(),
									item.getDistrict(), item.getTotalQty(), null, null, item.getNetInvoiceAdjustment(),
									item.getDueDate(), null, item.getCcbBranch(), accountNo, item.getDateOfPresent(),
									null, null, null, null, null, null);
						}).collect(Collectors.toList()));
						BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName).stream()
								.filter(itemData -> itemData.getAccountNumber()
										.equals(data.getInvoice().get(0).getAccountNo()))
								.collect(Collectors.toList()).get(0);
						data.setBankName(bankInfo.getBankName());
						data.setDoor(bankInfo.getDoor());
						data.setStreet(bankInfo.getStreet());
						data.setDistrict(bankInfo.getDistrict());
						data.setPincode(bankInfo.getPincode());
					}
				}
				return data;
			}
			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private List<ICP1Data> mapSdrobData(String officeName, String jwt, String status, String formType)
			throws Exception {
		return accountsService.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
				.getSundryDrOb().stream().filter(temp -> {
					Boolean statusMatch = false;
					Boolean pageMatch = false;
					if (formType.equals("invoiceAckEntry")) {
						statusMatch = status.isEmpty() || status.equals(temp.getVoucherStatusICP1());
						pageMatch = temp.getAckEntryDate() != null;
					}
					if (formType.equals("invoiceCollectionAvailable")) {
						statusMatch = status.isEmpty() || status.equals(temp.getVoucherStatusICP2());
						pageMatch = temp.getCollectionMethod() != null;
					}
					if (formType.equals("presentToCCB")) {
						statusMatch = status.isEmpty() || status.equals(temp.getVoucherStatusICP3());
						pageMatch = temp.getDateOfPresent() != null;
					}
					if (formType.equals("collectionUpdate")) {
						statusMatch = status.isEmpty();
						pageMatch = temp.getTransferDone() != null;
					}
					return pageMatch && statusMatch;
				}).map(item -> {
					return mapSdrObDateToIC(item, formType, jwt);
				}).collect(Collectors.toList());
	}

	private ICP1Data mapInvoiceDateToIC(Invoice temp, String formType, String jwt) {
		ICP1Data data = new ICP1Data();
		if (formType.equals("invoiceAckEntry")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setMaterialCenter(temp.getGodownName());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getTotalQty());
			data.setAckQty(temp.getAckQty());
			data.setAckEntryDate(temp.getAckEntryDate());
			data.setVoucherStatus(temp.getVoucherStatusICP1());
			data.setDesignation(temp.getDesignationICP1());
			data.setType("invoice");
		}
		if (formType.equals("invoiceCollectionAvailable")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getTotalQty());
			data.setValue(temp.getNetInvoiceAdjustment());
			data.setDueDate(temp.getDueDate());
			data.setDateAddedToPresent(temp.getAddedToPresentDate());
			data.setVoucherStatus(temp.getVoucherStatusICP2());
			data.setDesignation(temp.getDesignationICP2());
			data.setType("invoice");
			if (temp.getCollectionMethod().equals("AdjReceipt")) {
				try {
					List<AdjustmentReceiptVoucher> adj = new ArrayList<AdjustmentReceiptVoucher>();
					temp.getAdjReceiptNo().forEach(item -> {
						Vouchers adjv;
						try {
							adjv = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
									item, jwt);
							adj.add(adjv.getAdjustmentReceiptVoucherData());
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					data.setAdjData(adj);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (formType.equals("presentToCCB")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getTotalQty());
			data.setValue(temp.getNetInvoiceAdjustment());
			data.setCcbBranch(temp.getCcbBranch());
			data.setDateOfPresent(temp.getDateOfPresent());
			data.setVoucherStatus(temp.getVoucherStatusICP3());
			data.setDesignation(temp.getDesignationICP3());
			data.setType("invoice");
		}
		if (formType.equals("collectionUpdate")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getTotalQty());
			data.setValue(temp.getNetInvoiceAdjustment());
			data.setCcbBranch(temp.getCcbBranch());
			data.setDateOfCollection(temp.getDateOfCollectionFromCcb());
			data.setCollectionValue(temp.getCollectionValue());
			data.setDesignation(temp.getDesignationICP4());
			data.setType("invoice");
		}
		return data;
	}

	private ICP1Data mapSdrObDateToIC(SundryDrOb temp, String formType, String jwt) {
		ICP1Data data = new ICP1Data();
		if (formType.equals("invoiceAckEntry")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getInvoiceDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getQty());
			data.setAckQty(temp.getAckQty());
			data.setAckEntryDate(temp.getAckEntryDate());
			data.setVoucherStatus(temp.getVoucherStatusICP1());
			data.setDesignation(temp.getDesignationICP1());
			data.setType("sdrOb");
		}
		if (formType.equals("invoiceCollectionAvailable")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getInvoiceDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getQty());
			data.setValue(temp.getAmount());
			data.setDueDate(temp.getDueDate());
			data.setDateAddedToPresent(temp.getAddedToPresentDate());
			data.setVoucherStatus(temp.getVoucherStatusICP2());
			data.setDesignation(temp.getDesignationICP2());
			data.setType("sdrOb");
			if (temp.getCollectionMethod().equals("AdjReceipt")) {
				try {
					data.setAdjData(temp.getAdjReceipt());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		if (formType.equals("presentToCCB")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getInvoiceDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getQty());
			data.setValue(temp.getAmount());
			data.setCcbBranch(temp.getCcbBranch());
			data.setDateOfPresent(temp.getDateOfPresent());
			data.setVoucherStatus(temp.getVoucherStatusICP3());
			data.setDesignation(temp.getDesignationICP3());
			data.setType("sdrOb");
		}
		if (formType.equals("collectionUpdate")) {
			data.setId(temp.getId());
			data.setActivity(temp.getActivity());
			data.setInvoiceNo(temp.getInvoiceNo());
			data.setInvoiceDate(temp.getInvoiceDate());
			data.setIfmsId(temp.getIfmsId());
			data.setName(temp.getNameOfInstitution());
			data.setDistrict(temp.getDistrict());
			data.setQty(temp.getQty());
			data.setValue(temp.getAmount());
			data.setCcbBranch(temp.getCcbBranch());
			data.setDateOfCollection(temp.getDateOfCollectionFromCcb());
			data.setCollectionValue(temp.getCollectionValue());
			data.setDesignation(temp.getDesignationICP4());
			data.setType("sdrOb");
		}
		return data;
	}

	@Override
	public String updateAplStatusInvoiceCollection(VoucherApproval obj, String jwt) throws Exception {
		try {
			String designation = null;
			List<String> oldDesignation = null;

			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);

			switch (obj.getFormType()) {
			case "invoiceAckEntry": {
				Invoice invoice = invoiceRepo.findById(Long.valueOf(obj.getId())).get();

				designation = userService.getNewDesignation(empId);
				oldDesignation = invoice.getDesignationICP1();

				invoice.setVoucherStatusICP1(obj.getVoucherStatus());
				if (obj.getVoucherStatus().equals("Rejected")) {
					invoice.setAckEntryDate(null);
					invoice.setAckQty(null);
					invoice.setVoucherStatusICP1(null);
				}
				if (oldDesignation == null) {
					invoice.setDesignationICP1(Arrays.asList(designation));
				} else {
					invoice.getDesignationICP1().add(designation);
				}

				invoiceRepo.save(invoice);
				return designation;
			}

			case "invoiceCollectionAvailable": {
				Invoice invoice = invoiceRepo.findById(Long.valueOf(obj.getId())).get();
				if (invoice.getCollectionMethod().equals("AdjReceipt")) {
					Vouchers adjv = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
							obj.getAdjNo(), jwt);
					accountsService.voucherApprovalHandler(new VoucherApproval(obj.getVoucherStatus(),
							String.valueOf(adjv.getAdjustmentReceiptVoucherData().getId()), "adjustmentReceiptVoucher",
							null), jwt);
					if (obj.getVoucherStatus().equals("Approved")) {
						invoice.setCollectionValue(
								Arrays.asList(adjv.getAdjustmentReceiptVoucherData().getReceivedAmount()));
						invoice.setDateOfCollectionFromCcb(
								new ArrayList<>(List.of(adjv.getAdjustmentReceiptVoucherData().getDateOfCollection())));
						invoice.setTransferDone(false);
					}
				}
				designation = userService.getNewDesignation(empId);
				oldDesignation = invoice.getDesignationICP2();

				invoice.setVoucherStatusICP2(obj.getVoucherStatus());
				if (obj.getVoucherStatus().equals("Rejected")) {
					invoice.setAddedToPresentDate(null);
					invoice.setVoucherStatusICP2(null);
					invoice.setDueDate(null);
					invoice.setCollectionMethod(null);
					invoice.setAdjReceiptNo(null);
					invoice.setAdjReceiptStatus(null);
				}
				if (oldDesignation == null) {
					invoice.setDesignationICP2(Arrays.asList(designation));
				} else {
					invoice.getDesignationICP2().add(designation);
				}

				invoiceRepo.save(invoice);
				return designation;
			}

			case "presentToCCB": {
				Invoice invoice = invoiceRepo.findById(Long.valueOf(obj.getId())).get();

				designation = userService.getNewDesignation(empId);
				oldDesignation = invoice.getDesignationICP3();

				invoice.setVoucherStatusICP3(obj.getVoucherStatus());
				if (obj.getVoucherStatus().equals("Rejected")) {
					invoice.setDateOfPresent(null);
					invoice.setIcmNo(null);
					invoice.setVoucherStatusICP3(null);
				}
				if (oldDesignation == null) {
					invoice.setDesignationICP3(Arrays.asList(designation));
				} else {
					invoice.getDesignationICP3().add(designation);
				}

				invoiceRepo.save(invoice);
				return designation;
			}

			case "icm": {
				List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(obj.getId());
				revertIcmAdjAcc(obj, jwt);
				byIcmNo.forEach(invoice -> {
					String designationIcp4 = userService.getNewDesignation(empId);
					List<String> oldDesignationIcp4 = invoice.getDesignationICP4();

					if (obj.getVoucherStatus().equals("Rejected")) {
						List<String> adjNoLst = invoice.getAdjReceiptNo().stream().collect(Collectors.toList());
						if (adjNoLst.contains(obj.getAdjNo())) {
							int index = adjNoLst.indexOf(obj.getAdjNo());
							invoice.getAdjReceiptNo().remove(index);
							invoice.getCollectionValue().remove(index);
							invoice.getDateOfCollectionFromCcb().remove(index);
							invoice.setIsShort(false);
						}
						invoice.setTransferDone(false);
					}
					if (oldDesignationIcp4 == null) {
						invoice.setDesignationICP4(Arrays.asList(designationIcp4));
					} else {
						invoice.getDesignationICP4().add(designationIcp4);
					}
					invoiceRepo.save(invoice);
				});

				return designation;
			}

			default:
				throw new IllegalArgumentException("Unexpected value: " + obj.getFormType());
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editInvoiceCollectionData(String formType, InventryData obj, String jwt)
			throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			switch (formType) {
			case "invoiceAckEntry": {
				Invoice invoiceAckEntry = null;
				ICP1Data invoiceAckEntryData = obj.getInvoiceAckEntry();
				invoiceAckEntry = invoiceRepo.findById(invoiceAckEntryData.getId()).get();
				invoiceAckEntry.getEmpId().add(empId);
				invoiceAckEntry.setAckEntryDate(invoiceAckEntryData.getAckEntryDate());
				invoiceRepo.save(invoiceAckEntry);
				return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
			}

			case "invoiceCollectionAvailable": {
				Invoice invoiceCollectionAvailable = null;
				ICP1Data invoiceCollectionAvailableData = obj.getInvoiceCollectionAvailable();
				invoiceCollectionAvailable = invoiceRepo.findById(invoiceCollectionAvailableData.getId()).get();
				invoiceCollectionAvailable.getEmpId().add(empId);
				invoiceCollectionAvailable
						.setAddedToPresentDate(invoiceCollectionAvailableData.getDateAddedToPresent());
				invoiceRepo.save(invoiceCollectionAvailable);
				return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
			}

			case "presentToCCB": {
				Invoice presentToCCB = null;
				ICP1Data presentToCCBData = obj.getPresentToCCB();
				presentToCCB = invoiceRepo.findById(presentToCCBData.getId()).get();
				presentToCCB.getEmpId().add(empId);
				presentToCCB.setDateOfPresent(presentToCCBData.getDateOfPresent());
				invoiceRepo.save(presentToCCB);
				return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
			}

			case "collectionUpdate": {
				Invoice collectionUpdate = null;
				ICP1Data collectionUpdateData = obj.getCollectionUpdate();
				collectionUpdate = invoiceRepo.findById(collectionUpdateData.getId()).get();
				collectionUpdate.getEmpId().add(empId);
//				collectionUpdate.setDateOfCollectionFromCcb(collectionUpdateData.getDateOfCollection());
				invoiceRepo.save(collectionUpdate);
				return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
			}

			default:
				throw new IllegalArgumentException("Unexpected value: " + formType);
			}
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private AccountsService accountsService;

	@Override
	public ResponseEntity<String> saveAdjReceiptForIcmInvoices(IcmObject obj, String jwt, String type)
			throws Exception {
		try {
			BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, obj.getAdjData().getOfficeName())
					.stream()
					.filter(itemData -> itemData.getAccountType().equals("Non PDS A/c Fert")
							&& itemData.getBranchName().equals(obj.getAdjData().getBranchName()))
					.collect(Collectors.toList()).get(0);
			obj.getAdjData().setAccountType("Non PDS A/c Fert");
			obj.getAdjData().setAccountNo(bankInfo.getAccountNumber());
			obj.getAdjData().setContraEntry("No");
			Vouchers vouchers = new Vouchers();
			vouchers.setAdjustmentReceiptVoucherData(obj.getAdjData());
			ResponseEntity<String> responseEntity = accountsService
					.saveAccountsVouchersHandler("adjustmentReceiptVoucher", vouchers, jwt);
			String responseString = responseEntity.getBody();
			if (responseString == null) {
				throw new Exception("No data found");
			}
			String prefix = "Voucher Number: ";
			int index = responseString.indexOf(prefix);
			String voucherNo = responseString.substring(index + prefix.length()).trim();
			if (type.equals("icm")) {
				List<String> invoiceList = obj.getInvoices().stream().map(i -> i.getInvoiceNo())
						.collect(Collectors.toList());
				List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(obj.getAdjData().getIcmInvNo());
				byIcmNo.forEach(item -> {
					if (invoiceList.contains(item.getInvoiceNo())) {
						try {
							if (item.getAdjReceiptNo() == null) {
								item.setAdjReceiptNo(Arrays.asList(voucherNo));
							} else {
								item.getAdjReceiptNo().add(voucherNo);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
				invoiceRepo.saveAll(byIcmNo);
			} else {
				Invoice invoice = invoiceRepo.findByInvoiceNo(obj.getAdjData().getIcmInvNo()).get();
				invoice.setAdjReceiptNo(Arrays.asList(voucherNo));
				invoice.setAdjReceiptStatus(Arrays.asList("Pending"));
				invoiceRepo.save(invoice);
			}
			return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void revertIcmAdjAcc(VoucherApproval obj, String jwt) throws Exception {
		try {

			Vouchers adjv = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
					obj.getAdjNo(), jwt);
			accountsService.voucherApprovalHandler(new VoucherApproval(obj.getVoucherStatus(),
					String.valueOf(adjv.getAdjustmentReceiptVoucherData().getId()), "adjustmentReceiptVoucher", null),
					jwt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
