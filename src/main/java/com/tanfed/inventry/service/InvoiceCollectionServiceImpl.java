package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.dto.FundTransferDto;
import com.tanfed.inventry.entity.FundTransfer;
import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.FundTransferRepo;
import com.tanfed.inventry.repository.InvoiceRepo;
import com.tanfed.inventry.response.InvoiceCollectionResponseData;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class InvoiceCollectionServiceImpl implements InvoiceCollectionService {

	@Autowired
	private InvoiceRepo invoiceRepo;

	@Autowired
	private FundTransferRepo fundTransferRepo;

	@Autowired
	private MasterService masterService;

	@Autowired
	private UserService userService;

	@Autowired
	private CodeGenerator codeGenerator;

	private static Logger logger = LoggerFactory.getLogger(InvoiceCollectionServiceImpl.class);

	@Override
	public ResponseEntity<String> saveFundTransfer(FundTransferDto obj, String jwt) throws Exception {
		try {
			FundTransfer ft = new FundTransfer();
			logger.info("{}", obj);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			ft.setEmpId(Arrays.asList(empId));
			ft.setVoucherStatus("Pending");
			if (obj.getInvoiceNoList() != null) {
				List<String> invoiceNoList = new ArrayList<String>();
				obj.getInvoiceNoList().forEach(item -> {
					if (item.startsWith("RO")) {
						Invoice invoice = invoiceRepo.findByInvoiceNo(item).get();
						invoice.setTransferDone(true);
						invoiceRepo.save(invoice);
					} else {
						invoiceNoList.add(item);
					}
				});
				accountsService.updateFundTransferedHandler(invoiceNoList, jwt);
			}
			if (obj.getIdList() != null) {
				obj.getIdList().forEach(id -> {
					FundTransfer fundTransfer = fundTransferRepo.findById(id).get();
					fundTransfer.setTransferDone(true);
					fundTransferRepo.save(fundTransfer);
				});
			}
			ft.setTransferDone(false);

			ft.setActivity(obj.getActivity());
			ft.setOfficeName(obj.getOfficeName());
			ft.setDate(obj.getDate());
			ft.setTransferType(obj.getTransferType());
			ft.setDateOfTransfer(obj.getDateOfTransfer());
			ft.setBranchName(obj.getBranchName());
			ft.setAccountNo(obj.getAccountNo());
			ft.setToBranchName(obj.getToBranchName());
			ft.setToAccountNo(obj.getToAccountNo());
			ft.setOpeningBalance(obj.getOpeningBalance());
			ft.setCollection(obj.getCollection());
			ft.setIbrAmount(obj.getIbrAmount());
			ft.setTotal(obj.getTotal());
			ft.setIbtAmount(obj.getIbtAmount());
			ft.setCurrentTransfer(obj.getCurrentTransfer());
			ft.setBankCharges(obj.getBankCharges());
			ft.setOthers(obj.getOthers());
			ft.setClosingBalance(obj.getClosingBalance());
			ft.setInvoiceNoList(obj.getInvoiceNoList());
			ft.setIdList(obj.getIdList());
			Vouchers voucher = new Vouchers();
			List<String> pvList = new ArrayList<String>();
			if (obj.getPvData() != null && !obj.getPvData().isEmpty()) {
				obj.getPvData().forEach(item -> {
					item.setVoucherStatus("Pending");
					item.setVoucherFor("FundTransfer");
					voucher.setPaymentVoucherData(item);
					try {
						ResponseEntity<String> responseEntity = accountsService
								.saveAccountsVouchersHandler("paymentVoucher", voucher, jwt);
						String responseString = responseEntity.getBody();
						if (responseString == null) {
							throw new Exception("No data found");
						}
						String prefix = " Voucher Number : ";
						int index = responseString.indexOf(prefix);
						pvList.add(responseString.substring(index + prefix.length()).trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				ft.setPvList(pvList);
			}
			fundTransferRepo.save(ft);
			return new ResponseEntity<String>("Created Successfully!", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateInvoiceCollection(List<InvoiceCollectionObject> obj, String jwt)
			throws Exception {
		try {
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
					invoice.setVoucherStatusICP4("Pending");
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
				if ("fundTransfer".equals(collectionProcess)) {
					fundTransferData(data, collect, toBranchName, branchName, officeName, accountNo, date, jwt,
							transferType);
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
						&& !temp.getVoucherStatusICP2().equals("Approved")
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
							item.getDate().plusDays(item.getCreditDays()), item.getCcbBranch(), null, null, adj, null, null);
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
						item.getDate().plusDays(item.getCreditDays()), item.getCcbBranch(), null, null, null, null, null))
						.collect(Collectors.toList()));
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
			return temp.getDateOfPresent() != null && temp.getIsShort().equals(false) && temp.getCollectionValue()
					.stream().mapToDouble(item -> item).sum() < temp.getNetInvoiceAdjustment();
		}).collect(Collectors.toList());

		data.setIcmNoList(NoOfPresented.stream().map(item -> item.getIcmNo()).collect(Collectors.toSet()));

		data.setNoOfInvoicesPresented(NoOfPresented.size());
		data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(
				NoOfPresented.stream().mapToDouble(temp -> temp.getNetInvoiceAdjustment()).sum()));

		if (icmNo != null && !icmNo.isEmpty()) {
			data.setTableData(collect.stream()
					.filter(temp -> temp.getDateOfPresent() != null && icmNo.equals(temp.getIcmNo())
							&& temp.getIsShort().equals(false) && temp.getVoucherStatusICP3().equals("Approved")
							&& temp.getCollectionValue().stream().mapToDouble(item -> item).sum() < temp
									.getNetInvoiceAdjustment())
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

	public void fundTransferData(InvoiceCollectionResponseData data, List<Invoice> collect, String toBranchName,
			String branchName, String officeName, String accountNo, LocalDate date, String jwt, String transferType)
			throws Exception {

		List<BankInfo> bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName);

		if (date != null) {
			List<Invoice> invList = collect.stream().filter(temp -> {
				return temp.getDateOfCollectionFromCcb() != null && temp.getCcbBranch().equals(branchName)
						&& temp.getVoucherStatusICP4().equals("Approved") && temp.getTransferDone().equals(false);
			}).collect(Collectors.toList());
			List<SundryDrOb> sdrObData = fetchSdrObData(officeName, jwt, branchName, date);
			data.setTotalInvoicesValue(RoundToDecimalPlace.roundToTwoDecimalPlaces(validateDateForCollectionAmnt(
					invList.stream()
							.flatMap(inv -> IntStream
									.range(0, Math.min(inv.getDateOfCollectionFromCcb().size(),
											inv.getCollectionValue().size()))
									.mapToObj(
											i -> new AbstractMap.SimpleEntry<>(inv.getDateOfCollectionFromCcb().get(i),
													inv.getCollectionValue().get(i))))
							.collect(Collectors.groupingBy(Map.Entry::getKey,
									Collectors.summingDouble(Map.Entry::getValue))),
					date)
					+ validateDateForCollectionAmnt(sdrObData.stream()
							.flatMap(inv -> IntStream
									.range(0, Math.min(inv.getDateOfCollectionFromCcb().size(),
											inv.getCollectionValue().size()))
									.mapToObj(i -> new AbstractMap.SimpleEntry<>(
											inv.getDateOfCollectionFromCcb().get(i), inv.getCollectionValue().get(i))))
							.collect(Collectors.groupingBy(Map.Entry::getKey,
									Collectors.summingDouble(Map.Entry::getValue))),
							date)));

			data.setNoOfInvoicesTransferedToHO(fundTransferRepo.findByOfficeName(officeName).stream().filter(temp -> {
				return temp.getDate().isBefore(date) && temp.getCurrentTransfer() != null;
			}).mapToDouble(total -> total.getCurrentTransfer()).sum());

			data.setNoOfInvoicesCollected(invList.size() + sdrObData.size());
			if (transferType != null && !transferType.isEmpty()) {
				logger.info(transferType);
				logger.info("{}", bankInfo);
				data.setBranchNameList(bankInfo.stream().filter(temp -> temp.getAccountType().equals("Non PDS A/c"))
						.map(BankInfo::getBranchName).collect(Collectors.toSet()));
				if (branchName != null && !branchName.isEmpty()) {
					data.setAccountNoList(bankInfo.stream().filter(temp -> {
						return branchName.equals(temp.getBranchName()) && "Non PDS A/c".equals(temp.getAccountType());
					}).map(BankInfo::getAccountNumber).collect(Collectors.toList()));
				}
				if (transferType.equals("Branch To Branch")) {
					data.setToBranchNameList(bankInfo.stream()
							.filter(temp -> !temp.getBranchName().equals(branchName)
									&& temp.getAccountType().equals("Non PDS A/c"))
							.map(BankInfo::getBranchName).collect(Collectors.toSet()));
					if (toBranchName != null && !toBranchName.isEmpty()) {
						data.setToAccountNoList(bankInfo.stream().filter(temp -> {
							return toBranchName.equals(temp.getBranchName())
									&& "Non PDS A/c".equals(temp.getAccountType());
						}).map(BankInfo::getAccountNumber).collect(Collectors.toList()));
						logger.info("{}", data.getToAccountNoList());
					}
				}
			}

			if (accountNo != null && !accountNo.isEmpty()) {
				BankInfo bankInfoByAccNo = masterService.getBankInfoByAccountNoHandler(jwt, Long.valueOf(accountNo));
				data.setNameOfCcb(bankInfoByAccNo.getBankName());

				List<FundTransfer> ftLst = fundTransferRepo.findByAccountNo(Long.valueOf(accountNo)).stream()
						.filter(temp -> !temp.getDate().isAfter(date)).collect(Collectors.toList());

				data.setOpeningBalance(ftLst.get(ftLst.size() - 1).getClosingBalance());

				data.setCollection(RoundToDecimalPlace.roundToTwoDecimalPlaces(validateDateForCollectionAmnt(invList
						.stream()
						.flatMap(inv -> IntStream
								.range(0,
										Math.min(inv.getDateOfCollectionFromCcb().size(),
												inv.getCollectionValue().size()))
								.mapToObj(i -> new AbstractMap.SimpleEntry<>(inv.getDateOfCollectionFromCcb().get(i),
										inv.getCollectionValue().get(i))))
						.collect(Collectors.groupingBy(Map.Entry::getKey,
								Collectors.summingDouble(Map.Entry::getValue))),
						date)
						+ validateDateForCollectionAmnt(sdrObData.stream().flatMap(inv -> IntStream
								.range(0,
										Math.min(inv.getDateOfCollectionFromCcb().size(),
												inv.getCollectionValue().size()))
								.mapToObj(i -> new AbstractMap.SimpleEntry<>(inv.getDateOfCollectionFromCcb().get(i),
										inv.getCollectionValue().get(i))))
								.collect(Collectors.groupingBy(Map.Entry::getKey,
										Collectors.summingDouble(Map.Entry::getValue))),
								date)));

				List<FundTransfer> receipts = fundTransferRepo.findByToAccountNo(Long.valueOf(accountNo)).stream()
						.filter(temp -> !temp.getDate().isAfter(date) && temp.getTransferDone().equals(false))
						.collect(Collectors.toList());

				data.setIdList(receipts.stream().map(item -> item.getId()).collect(Collectors.toList()));

				data.setIbrAmount(receipts.stream().mapToDouble(item -> item.getIbtAmount()).sum());

				data.setTotal(data.getOpeningBalance() + data.getCollection() + data.getIbrAmount());

				data.setInvoiceNoList(invList.stream().map(Invoice::getInvoiceNo).toList());
				data.getInvoiceNoList().addAll(sdrObData.stream().map(item -> item.getInvoiceNo()).toList());
			}
		}
	}

	private Double validateDateForCollectionAmnt(Map<LocalDate, Double> obj, LocalDate date) {
		return obj.entrySet().stream().filter(e -> !e.getKey().isAfter(date)).mapToDouble(Map.Entry::getValue).sum();
	}

	private List<SundryDrOb> fetchSdrObData(String officeName, String jwt, String branchName, LocalDate date)
			throws Exception {
		try {
			Vouchers vouchers = accountsService.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null,
					null, jwt);
			return vouchers.getSundryDrOb().stream().filter(temp -> {
				return temp.getDateOfCollectionFromCcb() != null && temp.getCcbBranch().equals(branchName)
						&& temp.getVoucherStatusICP4().equals("Approved") && temp.getTransferDone().equals(false);
			}).collect(Collectors.toList());

		} catch (Exception e) {
			throw new Exception(e);
		}

	}

	@Override
	public InvoiceCollectionResponseData getCollectionAbstractData(String officeName, String branchName,
			String accountNo, String monthOfFundTransfer, String jwt) throws Exception {
		InvoiceCollectionResponseData data = new InvoiceCollectionResponseData();
//		List<BankInfo> bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName);
//		data.setBranchNameList(bankInfo.stream().filter(item -> item.getAccountType().equals("Non PDS A/c"))
//				.map(BankInfo::getBranchName).collect(Collectors.toSet()));
//		if (branchName != null && !branchName.isEmpty()) {
//			data.setAccountNoList(bankInfo.stream().filter(temp -> {
//				return branchName.equals(temp.getBranchName()) && "Non PDS A/c".equals(temp.getAccountType());
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
						statusMatch = voucherStatus.isEmpty() || voucherStatus.equals(temp.getVoucherStatusICP4());
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
								statusMatch = voucherStatus.isEmpty()
										|| voucherStatus.equals(temp.getVoucherStatusICP4());
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
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP1ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
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
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP2ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, jwt)).collect(Collectors.toList()));
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
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}
			case "presentToCCB": {
				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP3ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP3ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP3ByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
						invoiceLst.addAll(invoiceRepo.findICP3ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
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
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						invoiceLst.addAll(invoiceRepo.findICP4ApprovedByStatus(officeName).stream()
								.map(temp -> mapInvoiceDateToIC(temp, formType, null)).collect(Collectors.toList()));
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
					} else if (fromDate != null && toDate != null) {
						invoiceLst.addAll(invoiceFilteredLst);
					}
				}
				data.setInvoice(invoiceLst);
				return data;
			}
			case "fundTransfer": {
				List<FundTransferDto> fTLst = new ArrayList<FundTransferDto>();
				List<FundTransferDto> fundTransferFilteredLst = null;
				logger.info("{}", fromDate);
				logger.info("{}", toDate);
				if (fromDate != null && toDate != null) {
					fundTransferFilteredLst = fundTransferRepo.findByOfficeName(officeName).stream().filter(temp -> {
						Boolean statusMatch = voucherStatus.isEmpty() || voucherStatus.equals(temp.getVoucherStatus());
						return !temp.getDate().isBefore(fromDate) && !temp.getDate().isAfter(toDate) && statusMatch
								&& !temp.getActivity().equals(null);
					}).map(item -> {
						return mapftDataWithPv(item, jwt);
					}).collect(Collectors.toList());
				}

				List<FundTransferDto> pendingData = fundTransferRepo.findICP5ByStatus(officeName).stream().map(item -> {
					return mapftDataWithPv(item, jwt);
				}).collect(Collectors.toList());

				List<FundTransferDto> approvedData = fundTransferRepo.findICP5ApprovedByStatus(officeName).stream()
						.map(item -> {
							return mapftDataWithPv(item, jwt);
						}).collect(Collectors.toList());

				if (voucherStatus.equals("Pending")) {
					if (fromDate == null && toDate == null) {
						fTLst.addAll(pendingData);
					} else if (fromDate != null && toDate != null) {
						fTLst.addAll(fundTransferFilteredLst);
					}
				}
				if (voucherStatus.equals("Approved")) {
					if (fromDate == null && toDate == null) {
						fTLst.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						fTLst.addAll(fundTransferFilteredLst);
					}
				}
				if (voucherStatus.isEmpty()) {
					if (fromDate == null && toDate == null) {
						fTLst.addAll(pendingData);
						fTLst.addAll(approvedData);
					} else if (fromDate != null && toDate != null) {
						fTLst.addAll(fundTransferFilteredLst);
					}
				}
				data.setFundTransfer(fTLst);
				return data;
			}
			case "icm": {
				data.setIcmNoList(invoiceRepo.findByOfficeName(officeName).stream().filter(
						item -> item.getDateOfPresent() != null && item.getVoucherStatusICP3().equals("Approved"))
						.map(item -> item.getIcmNo()).collect(Collectors.toSet()));
				if (icmNo != null && !icmNo.isEmpty()) {
					List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(icmNo);
					if (null == byIcmNo.get(0).getAdjReceiptNo()) {
						data.setAdjv(null);
					} else {
						List<AdjustmentReceiptVoucher> adj = new ArrayList<AdjustmentReceiptVoucher>();
						byIcmNo.get(0).getAdjReceiptNo().forEach(item -> {
							Vouchers adjv;
							try {
								adjv = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
										item, jwt);
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
							BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, officeName).stream()
									.filter(itemData -> itemData.getAccountType().equals("Non PDS A/c")
											&& itemData.getBranchName().equals(item.getCcbBranch()))
									.collect(Collectors.toList()).get(0);
							accountNo = bankInfo.getAccountNumber();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return new ICP1Data(item.getId(), item.getActivity(), item.getGodownName(), item.getInvoiceNo(),
								item.getDate(), item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(),
								item.getTotalQty(), null, null, item.getNetInvoiceAdjustment(), item.getDueDate(), null,
								item.getCcbBranch(), accountNo, item.getDateOfPresent(), null, null,
								item.getVoucherStatusICP4(), null, null);
					}).collect(Collectors.toList()));
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

	private FundTransferDto mapftDataWithPv(FundTransfer item, String jwt) {
		List<PaymentVoucher> pvData = new ArrayList<PaymentVoucher>();
		item.getPvList().forEach(pvNo -> {
			try {
				Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", pvNo, jwt);
				pvData.add(pv.getPaymentVoucherData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return new FundTransferDto(item.getId(), item.getDesignation(), item.getEmpId(), item.getVoucherStatus(),
				item.getActivity(), item.getOfficeName(), item.getDate(), item.getTransferType(),
				item.getDateOfTransfer(), item.getBranchName(), item.getAccountNo(), item.getToBranchName(),
				item.getToAccountNo(), item.getOpeningBalance(), item.getCollection(), item.getIbrAmount(),
				item.getTotal(), item.getIbtAmount(), item.getCurrentTransfer(), item.getBankCharges(),
				item.getOthers(), item.getClosingBalance(), item.getTransferDone(), pvData, item.getInvoiceNoList(),
				item.getIdList());
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
			data.setVoucherStatus(temp.getVoucherStatusICP4());
			data.setDesignation(temp.getDesignationICP4());
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
			data.setVoucherStatus(temp.getVoucherStatusICP4());
			data.setDesignation(temp.getDesignationICP4());
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
				revertIcmAdjAcc(obj, jwt);
				designation = userService.getNewDesignation(empId);
				oldDesignation = invoice.getDesignationICP2();

				invoice.setVoucherStatusICP2(obj.getVoucherStatus());
				if (obj.getVoucherStatus().equals("Rejected")) {
					invoice.setAddedToPresentDate(null);
					invoice.setVoucherStatusICP2(null);
					invoice.setDueDate(null);
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

					invoice.setVoucherStatusICP4(obj.getVoucherStatus());
					if (obj.getVoucherStatus().equals("Rejected")) {
						invoice.setDateOfCollectionFromCcb(null);
						invoice.setCollectionValue(null);
						invoice.setTransferDone(false);
						invoice.setVoucherStatusICP4(null);
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

			case "fundTransfer": {
				FundTransfer fundTransfer = fundTransferRepo.findById(Long.valueOf(obj.getId())).get();

				designation = userService.getNewDesignation(empId);
				oldDesignation = fundTransfer.getDesignation();

				fundTransfer.setVoucherStatus(obj.getVoucherStatus());
				revertFundTransferPvAcc(fundTransfer, jwt);
				if (obj.getVoucherStatus().equals("Rejected")) {
					if (fundTransfer.getInvoiceNoList() != null) {
						List<String> invoiceNoList = new ArrayList<String>();
						fundTransfer.getInvoiceNoList().forEach(item -> {
							if (item.startsWith("RO")) {
								Invoice invoice = invoiceRepo.findByInvoiceNo(item).get();
								invoice.setTransferDone(false);
								invoiceRepo.save(invoice);
							} else {
								invoiceNoList.add(item);
							}
						});
						accountsService.revertFundTransferedHandler(invoiceNoList, jwt);
					}
					if (fundTransfer.getIdList() != null) {
						fundTransfer.getIdList().forEach(id -> {
							FundTransfer fundTransferData = fundTransferRepo.findById(id).get();
							fundTransferData.setTransferDone(false);
							fundTransferRepo.save(fundTransferData);
						});
					}
				}
				if (oldDesignation == null) {
					fundTransfer.setDesignation(Arrays.asList(designation));
				} else {
					fundTransfer.getDesignation().add(designation);
				}

				fundTransferRepo.save(fundTransfer);
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

			case "fundTransfer": {
				FundTransfer fundTransfer = null;
				FundTransfer fundTransferData = obj.getFundTransfer();
				fundTransfer = fundTransferRepo.findById(fundTransferData.getId()).get();
				fundTransfer.getEmpId().add(empId);
				fundTransfer.setDateOfTransfer(fundTransferData.getDateOfTransfer());
				fundTransfer.setCurrentTransfer(fundTransferData.getCurrentTransfer());
				fundTransfer.setBankCharges(fundTransferData.getBankCharges());
				fundTransfer.setOthers(fundTransferData.getOthers());
				fundTransferRepo.save(fundTransfer);
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
	public ResponseEntity<String> saveAdjReceiptForIcmInvoices(AdjustmentReceiptVoucher obj, String jwt, String type)
			throws Exception {
		try {
			BankInfo bankInfo = masterService.getBankInfoByOfficeNameHandler(jwt, obj.getOfficeName()).stream()
					.filter(itemData -> itemData.getAccountType().equals("Non PDS A/c")
							&& itemData.getBranchName().equals(obj.getBranchName()))
					.collect(Collectors.toList()).get(0);
			obj.setAccountType("Non PDS A/c");
			obj.setAccountNo(bankInfo.getAccountNumber());
			Vouchers vouchers = new Vouchers();
			vouchers.setAdjustmentReceiptVoucherData(obj);
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
				List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(obj.getIcmInvNo());
				byIcmNo.forEach(item -> {
					if (item.getAdjReceiptNo() == null) {
						item.setAdjReceiptNo(Arrays.asList(voucherNo));
						item.setAdjReceiptStatus(Arrays.asList("Pending"));
					} else {
						item.getAdjReceiptNo().add(voucherNo);
						item.getAdjReceiptStatus().add("Pending");
					}
				});
				invoiceRepo.saveAll(byIcmNo);
			} else {
				Invoice invoice = invoiceRepo.findByInvoiceNo(obj.getIcmInvNo()).get();
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
			List<Invoice> byIcmNo = invoiceRepo.findByIcmNo(String.valueOf(obj.getId()));

			Vouchers adjv = accountsService.getAccountsVoucherByVoucherNoHandler("adjustmentReceiptVoucher",
					byIcmNo.get(0).getAdjReceiptNo().get(0), jwt);
			accountsService.voucherApprovalHandler(
					new VoucherApproval(obj.getVoucherStatus(),
							String.valueOf(adjv.getAdjustmentReceiptVoucherData().getId()), "adjustmentReceiptVoucher"),
					jwt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void revertFundTransferPvAcc(FundTransfer obj, String jwt) throws Exception {
		obj.getPvList().forEach(pvNo -> {
			try {
				Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", pvNo, jwt);
				accountsService.voucherApprovalHandler(new VoucherApproval(obj.getVoucherStatus(),
						String.valueOf(pv.getPaymentVoucherData().getId()), "paymentVoucher"), jwt);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
