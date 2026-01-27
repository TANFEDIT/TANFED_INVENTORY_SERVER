package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.dto.FT_Charges_Dto;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.ClosingStockTableRepo;
import com.tanfed.inventry.repository.FundTransferRepo;
import com.tanfed.inventry.repository.OpeningStockRepo;
import com.tanfed.inventry.response.StockRegisterTable;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class RegisterServiceImpl implements RegisterService {

	@Autowired
	private GrnService grnService;

	@Autowired
	private AccountsService accountsService;

	private static Logger logger = LoggerFactory.getLogger(RegisterServiceImpl.class);

	@Override
	public List<RegisterTable> getReceiptRegisterData(String officeName, String month, String godownName,
			LocalDate fromDate, LocalDate toDate, String productName) throws Exception {
		try {
			logger.info(month);
			logger.info(productName);
			return grnService.getGrnDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter = true;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				Boolean productFilter = true;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getProductName().equals(productName);
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && godownFilter && productFilter && monthFilter;
			}).map(item -> new RegisterTable(item.getGodownName(), null, item.getGrnNo(), item.getDate(), null, null,
					item.getDcWdnRoNo(), item.getSupplierDocDate(), null, null, null, null, item.getAckDate(),
					item.getFirstPointIfmsId(), null, null, item.getSupplierName(), item.getProductName(), null,
					item.getPacking(), item.getMaterialSuppliedBags(), item.getMaterialSuppliedQuantity(),
					item.getMaterialReceivedBags(), item.getMaterialReceivedQuantity(), null, null, null, null,
					item.getBillNo())).sorted(Comparator.comparing(RegisterTable::getGrnDate))
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private InvoiceService invoiceService;

	@Override
	public List<RegisterTable> getSalesRegisterData(String officeName, String month, String godownName,
			LocalDate fromDate, LocalDate toDate, String productName) throws Exception {
		try {
			return invoiceService.getInvoiceDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				Boolean productFilter;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getTableData().stream()
							.anyMatch(data -> data.getProductName().equals(productName));
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && godownFilter && productFilter && monthFilter;
			}).map(item -> new RegisterTable(item.getGodownName(), null, null, null, null, null, null, null,
					item.getDcNo(), item.getDate(), item.getInvoiceNo(), item.getDate(), null, item.getIfmsId(),
					item.getNameOfInstitution(), item.getDistrict(), null, null,
					item.getTableData().stream()
							.filter(i -> i.getProductName().equals(productName) || productName.isEmpty())
							.map(m -> new ProductDataSalesRegister(m.getProductName(), m.getBags().toString(),
									m.getQty().toString(), m.getBasicPrice().toString(), m.getCgstAmount().toString(),
									m.getSgstAmount().toString(), m.getTotal().toString()))
							.collect(Collectors.toList()),
					null, item.getTotalNoOfBags().toString(), item.getTotalQty(), null, null, item.getTotalBasicValue(),
					item.getTotalCgstValue(), item.getTotalSgstValue(), item.getNetInvoiceAdjustment(), null))
					.sorted(Comparator.comparing(RegisterTable::getInvoiceDate)).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private GtnService gtnService;

	@Override
	public List<RegisterTable> getStockTransferIssueData(String officeName, String month, String godownName,
			LocalDate fromDate, LocalDate toDate, String productName) throws Exception {
		try {
			return gtnService.getGtnDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter = true;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				Boolean productFilter = true;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getProductName().equals(productName);
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && item.getGtnFor().equals("Issue") && godownFilter
						&& productFilter && monthFilter;
			}).map(item -> {
				Double bags = item.getGtnTableData().stream().mapToDouble(sum -> sum.getBags()).sum();
				Double qty = item.getGtnTableData().stream().mapToDouble(sum -> sum.getQty()).sum();
				return new RegisterTable(item.getGodownName(), null, null, null, item.getGtnNo(), item.getDate(), null,
						null, null, null, null, null, null, null, null, null, item.getSupplierName(),
						item.getProductName(), null, item.getGtnTableData().get(0).getPacking(), bags.toString(), qty,
						null, null, null, null, null, null, null);
			}).sorted(Comparator.comparing(RegisterTable::getGtnDate)).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<RegisterTable> getStockTransferReceiptData(String officeName, String month, String godownName,
			LocalDate fromDate, LocalDate toDate, String productName) throws Exception {
		try {
			return gtnService.getGtnDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter = true;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				Boolean productFilter = true;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getProductName().equals(productName);
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && item.getGtnFor().equals("Receipt") && godownFilter
						&& productFilter && monthFilter;
			}).filter(item -> item.getGtnTableData().stream().anyMatch(data -> data.getReceivedBags() != null))
					.map(item -> {
						Double bags = item.getGtnTableData().stream().mapToDouble(sum -> sum.getReceivedBags()).sum();
						Double qty = item.getGtnTableData().stream().mapToDouble(sum -> sum.getQty()).sum();
						return new RegisterTable(item.getGodownName(), item.getDestination(), null, null,
								item.getGtnNo(), item.getDate(), null, null, null, null, null, null, null, null, null,
								null, item.getSupplierName(), item.getProductName(), null,
								item.getGtnTableData().get(0).getPacking(), null, null, bags.toString(), qty, null,
								null, null, null, null);
					}).sorted(Comparator.comparing(RegisterTable::getGtnDate)).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private ClosingStockTableRepo closingStockTableRepo;

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Autowired
	private DcService dcService;

	@Override
	public List<StockRegisterTable> getStockRegisterData(String officeName, LocalDate fromDate, LocalDate toDate,
			String productName, String godownName, String month) throws Exception {
		try {
			List<StockRegisterTable> table = new ArrayList<>();

			List<GRN> grnList = grnService.getGrnDataByOffficeName(officeName);
			List<GTN> gtnList = gtnService.getGtnDataByOffficeName(officeName);
			List<DeliveryChellan> dcList = dcService.getDeliveryChellanDataByOffficeName(officeName);

			if (!month.isEmpty()) {
				String[] monthAndYr = month.split(" ");
				YearMonth yearMonth = YearMonth.of(Integer.valueOf(monthAndYr[1]), Month.valueOf(monthAndYr[0]));
				fromDate = yearMonth.atDay(1);
				toDate = yearMonth.atEndOfMonth();
			}
			LocalDate date = fromDate;
			while (!date.isAfter(toDate)) {
				final LocalDate currentDate = date;
				ClosingStockTable cb;
				logger.info(productName);
				logger.info(officeName);
				logger.info("{}", currentDate);
				Double ob;
				if (currentDate.equals(LocalDate.of(2025, 4, 1))) {
					ob = openingStockRepo.findByOfficeNameAndProductNameAndAsOn(officeName, productName, currentDate)
							.stream()
							.filter(item -> item.getVoucherStatus().equals("Approved")
									&& item.getGodownName().equals(godownName))
							.collect(Collectors.toList()).get(0).getQuantity();
				} else {
					int n = 1;
					do {
						LocalDate currDate = currentDate.minusDays(n++);
						cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(officeName,
								productName, currDate, godownName);
						if (currDate.equals(LocalDate.of(2025, 4, 1))) {
							ob = 0.0;
						}
					} while (cb == null);
					ob = cb.getBalance();
				}
				double receipt = grnList.stream()
						.filter(item -> "Approved".equals(item.getVoucherStatus())
								&& (godownName.isEmpty() || item.getGodownName().equals(godownName))
								&& productName.equals(item.getProductName()) && currentDate.equals(item.getDate()))
						.mapToDouble(GRN::getMaterialReceivedQuantity).sum();

				double otherReceipt = gtnList.stream()
						.filter(item -> "Approved".equals(item.getVoucherStatus()) && "Receipt".equals(item.getGtnFor())
								&& (godownName.isEmpty() || item.getGodownName().equals(godownName))
								&& productName.equals(item.getProductName()) && currentDate.equals(item.getDate()))
						.flatMapToDouble(
								item -> item.getGtnTableData().stream().mapToDouble(GtnTableData::getReceivedQty))
						.sum();

				double otherIssue = gtnList.stream()
						.filter(item -> "Approved".equals(item.getVoucherStatus()) && "Issue".equals(item.getGtnFor())
								&& (godownName.isEmpty() || item.getGodownName().equals(godownName))
								&& productName.equals(item.getProductName()) && currentDate.equals(item.getDate()))
						.flatMapToDouble(item -> item.getGtnTableData().stream().mapToDouble(GtnTableData::getQty))
						.sum();

				double issue = dcList.stream()
						.filter(item -> "Approved".equals(item.getVoucherStatus()) && currentDate.equals(item.getDate())
								&& (godownName.isEmpty() || item.getGodownName().equals(godownName)))
						.flatMapToDouble(item -> item.getDcTableData().stream()
								.filter(data -> productName.equals(data.getProductName()))
								.mapToDouble(DcTableData::getQty))
						.sum();

				double total = ob + receipt + otherReceipt;
				double closingBalance = total - issue - otherIssue;

				table.add(new StockRegisterTable(date, ob, receipt, otherReceipt, total, issue, otherIssue,
						closingBalance));

				date = date.plusDays(1);
			}
			table.sort(Comparator.comparing(StockRegisterTable::getDate));
			return table;
		} catch (Exception e) {
			throw new Exception("Error while generating stock register data", e);
		}
	}

	@Override
	public List<InvoiceCollectionRegisterTable> getInvoiceWatchingRegister(String officeName, String month,
			LocalDate fromDate, String branchName, String godownName, LocalDate toDate) throws Exception {
		try {
			return invoiceService.getInvoiceDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter = true;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				Boolean branchFilter = true;
				if (branchName.isEmpty()) {
					branchFilter = true;
				} else {
					branchFilter = item.getCcbBranch().equals(branchName);
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatusICP2() != null && item.getVoucherStatusICP2().equals("Approved")
						&& godownFilter && branchFilter && monthFilter;
			}).map(item -> {
				return new InvoiceCollectionRegisterTable(item.getActivity(), item.getGodownName(), item.getInvoiceNo(),
						item.getDate(), item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(item.getTotalQty()),
						Arrays.asList(RoundToDecimalPlace.roundToThreeDecimalPlaces(item.getNetInvoiceAdjustment())),
						item.getDueDate(), item.getAckEntryDate(), null, null, null, null, null, null, null, null, null,
						null, null);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Invoice Watching Register data", e);
		}
	}

	@Override
	public List<InvoiceCollectionRegisterTable> getInvoicePresentationRegister(String officeName, String month,
			LocalDate fromDate, String branchName, LocalDate toDate) throws Exception {
		try {
			return invoiceService.getInvoiceDataByOffficeName(officeName).stream().filter(item -> {
				Boolean branchFilter = true;
				if (branchName.isEmpty()) {
					branchFilter = true;
				} else {
					branchFilter = item.getCcbBranch().equals(branchName);
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatusICP3() != null && item.getVoucherStatusICP3().equals("Approved")
						&& branchFilter && monthFilter;
			}).map(item -> {
				return new InvoiceCollectionRegisterTable(item.getActivity(), null, item.getInvoiceNo(), item.getDate(),
						item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(), item.getTotalQty(),
						Arrays.asList(item.getNetInvoiceAdjustment()), item.getDueDate(), null, item.getDateOfPresent(),
						null, item.getCcbBranch(), null, null, null, null, null, null, null, null);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Invoice Presentation Register data", e);
		}
	}

	@Override
	public List<InvoiceCollectionRegisterTable> getInvoiceCollectionRegister(String officeName, String month,
			LocalDate fromDate, String branchName, LocalDate toDate, String jwt) throws Exception {
		try {
			List<InvoiceCollectionRegisterTable> invoiceCollectionRegister = new ArrayList<InvoiceCollectionRegisterTable>();
			invoiceCollectionRegister
					.addAll(invoiceService.getInvoiceDataByOffficeName(officeName).stream().filter(item -> {
						Boolean branchFilter = false;
						if (branchName.isEmpty()) {
							branchFilter = true;
						} else {
							branchFilter = item.getCcbBranch().equals(branchName);
						}
						Boolean monthFilter = false;
						if (item.getDateOfCollectionFromCcb() != null) {
							if (month.isEmpty()) {
								monthFilter = item.getDateOfCollectionFromCcb().stream()
										.anyMatch(i -> !i.isBefore(fromDate) && !i.isAfter(toDate));
							} else {
								monthFilter = item.getDateOfCollectionFromCcb().stream().anyMatch(
										i -> String.format("%s%s%04d", i.getMonth(), " ", i.getYear()).equals(month));
							}
						}
						return branchFilter && monthFilter;
					}).map(item -> {
						return new InvoiceCollectionRegisterTable(item.getActivity(), null, item.getInvoiceNo(),
								item.getDate(), item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(),
								RoundToDecimalPlace.roundToThreeDecimalPlaces(item.getTotalQty()),
								item.getCollectionValue(), null, null, null, item.getDateOfCollectionFromCcb(),
								item.getCcbBranch(), item.getAdjReceiptNo(), item.getIcmNo(),
								item.getDateOfCollectionFromCcb(), item.getDateOfPresent(), null, null, null, null);
					}).collect(Collectors.toList()));

			invoiceCollectionRegister.addAll(
					accountsService.getBillsAccountsFilteredDataHandler("sundryDrOb", officeName, "", null, null, jwt)
							.getSundryDrOb().stream().filter(i -> {
								Boolean branchFilter = false;
								if (branchName.isEmpty()) {
									branchFilter = true;
								} else {
									branchFilter = i.getCcbBranch().equals(branchName);
								}
								Boolean monthFilter = false;
								if (i.getDateOfCollectionFromCcb() != null) {
									if (month.isEmpty()) {
										monthFilter = i.getDateOfCollectionFromCcb().stream()
												.anyMatch(k -> !k.isBefore(fromDate) && !k.isAfter(toDate));
									} else {
										monthFilter = i.getDateOfCollectionFromCcb().stream().anyMatch(k -> String
												.format("%s%s%04d", k.getMonth(), " ", k.getYear()).equals(month));
									}
								}
								return branchFilter && monthFilter;
							}).map(item -> {
								return new InvoiceCollectionRegisterTable(item.getActivity(), null, item.getInvoiceNo(),
										item.getInvoiceDate(), item.getIfmsId(), item.getNameOfInstitution(),
										item.getDistrict(),
										RoundToDecimalPlace.roundToThreeDecimalPlaces(item.getQty()),
										item.getCollectionValue(), null, null, null, item.getDateOfCollectionFromCcb(),
										item.getCcbBranch(),
										item.getAdjReceipt().stream().map(i -> i.getVoucherNo())
												.collect(Collectors.toList()),
										item.getIcmNo(), item.getDateOfCollectionFromCcb(), item.getDateOfPresent(),
										null, null, null, null);
							}).collect(Collectors.toList()));
			return invoiceCollectionRegister;
		} catch (Exception e) {
			throw new Exception("Error while generating Invoice Collection Register data", e);
		}
	}

	@Autowired
	private FundTransferRepo fundTransferRepo;

	@Override
	public List<InvoiceCollectionRegisterTable> getFundTransferRegister(String officeName, String month,
			LocalDate fromDate, String branchName, LocalDate toDate, String accountNo) throws Exception {
		try {
			return fundTransferRepo.findByOfficeName(officeName).stream().filter(item -> {
				Boolean branchFilter = true;
				if (branchName.isEmpty()) {
					branchFilter = true;
				} else {
					branchFilter = item.getBranchName().equals(branchName);
				}
				Boolean accNoFilter = true;
				if (accountNo.isEmpty()) {
					accNoFilter = true;
				} else {
					accNoFilter = item.getAccountNo().equals(Long.valueOf(accountNo));
				}
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && branchFilter && monthFilter && accNoFilter;
			}).map(item -> {
				return mapFtRegisterData(item);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Fund Transfer Register data", e);
		}
	}

	private InvoiceCollectionRegisterTable mapFtRegisterData(FundTransfer item) {
		InvoiceCollectionRegisterTable data = new InvoiceCollectionRegisterTable();
		FT_Charges_Dto obj = new FT_Charges_Dto(item.getCurrentTransfer(), item.getBankCharges());
		data.setDate(item.getDate());
		data.setBranchName(item.getBranchName());

		if (item.getActivity().equals("Fertiliser")) {
			data.setFert(obj);
		} else if (item.getActivity().equals("Agri.Marketing")) {
			data.setAgriMark(obj);
		} else {
			data.setSpai(obj);
		}
		if (data.getFert() == null) {
			data.setFert(new FT_Charges_Dto(0.0, 0.0));
		}
		if (data.getAgriMark() == null) {
			data.setAgriMark(new FT_Charges_Dto(0.0, 0.0));
		}
		if (data.getSpai() == null) {
			data.setSpai(new FT_Charges_Dto(0.0, 0.0));
		}
		Double totalTransfer = data.getFert().getCurrentTransfer() + data.getAgriMark().getCurrentTransfer()
				+ data.getSpai().getCurrentTransfer();

		Double totalBankCharges = data.getFert().getBankCharges() + data.getAgriMark().getBankCharges()
				+ data.getSpai().getBankCharges();

		data.setTotal(new FT_Charges_Dto(totalTransfer, totalBankCharges));
		return data;
	}

	@Autowired
	private PoService poService;

	@Override
	public List<PoRegisterTable> getPoRegisterData(String officeName, String month, LocalDate fromDate,
			String supplierName, LocalDate toDate, String productName) throws Exception {
		try {
			return poService.getPoData().stream().filter(item -> {
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				Boolean productFilter = true;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getProductName().equals(productName);
				}
				Boolean supplierFilter = true;
				if (supplierName.isEmpty()) {
					supplierFilter = true;
				} else {
					supplierFilter = item.getSupplierName().equals(supplierName);
				}
				return item.getVoucherStatus().equals("Approved") && monthFilter && productFilter && supplierFilter
						&& item.getTableData().stream().map(PoTableData::getRegion).collect(Collectors.toList())
								.contains(officeName);
			}).map(item -> {
				final double[] direct = { 0.0 }, buffer = { 0.0 };
				item.getTableData().forEach(po -> {
					if (officeName.equals(po.getRegion())) {
						if ("Direct".equals(po.getIssuedFor())) {
							direct[0] = po.getPoIssueQty();
						} else {
							buffer[0] = po.getPoIssueQty();
						}
					}
				});
				double receiptDirect = item.getGrnData().stream()
						.filter(itemData -> itemData.getOfficeName().equals(officeName)
								&& itemData.getGodownType().equals("Direct")
								&& itemData.getVoucherStatus().equals("Approved"))
						.mapToDouble(itemData -> itemData.getMaterialReceivedQuantity()).sum();
				double receiptBuffer = item.getGrnData().stream()
						.filter(itemData -> itemData.getOfficeName().equals(officeName)
								&& !itemData.getGodownType().equals("Direct")
								&& itemData.getVoucherStatus().equals("Approved"))
						.mapToDouble(itemData -> itemData.getMaterialReceivedQuantity()).sum();
				double total = direct[0] + buffer[0];
				double totalReceipt = receiptDirect + receiptBuffer;
				return new PoRegisterTable(item.getActivity(), item.getPurchaseOrderType(), item.getPoNo(),
						item.getDate(), RoundToDecimalPlace.roundToTwoDecimalPlaces(direct[0]),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(buffer[0]),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(total),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(receiptDirect),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(receiptBuffer),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(totalReceipt),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(direct[0] - receiptDirect),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(buffer[0] - receiptBuffer),
						RoundToDecimalPlace.roundToTwoDecimalPlaces(total - totalReceipt), null, null, null, null, null,
						null, null, null, null);
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating PO Register data", e);
		}
	}

	@Override
	public List<PoRegisterTable> getPoAllotmentRegisterData(String officeName, String month, LocalDate fromDate,
			LocalDate toDate, String poNo) throws Exception {
		try {
			if (poNo != null && !poNo.isEmpty()) {
				PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
				return poService.getPoByPoNo(poNo).getGrnData().stream()
						.map(item -> mapPoAllotmentRegisterData(purchaseOrder, officeName, item))
						.collect(Collectors.toList());
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new Exception("Error while generating PO Allotment Register data", e);
		}
	}

	private PoRegisterTable mapPoAllotmentRegisterData(PurchaseOrder purchaseOrder, String officeName, GRN grn) {
		PoRegisterTable data = new PoRegisterTable();
		data.setDate(purchaseOrder.getDate());
		data.setSupplierName(purchaseOrder.getSupplierName());
		data.setProductName(purchaseOrder.getProductName());
		data.setGodownName(grn.getGodownName());
		final double[] direct = { 0.0 }, buffer = { 0.0 };
		purchaseOrder.getTableData().forEach(po -> {
			if (officeName.equals(po.getRegion())) {
				if ("Direct".equals(po.getIssuedFor())) {
					direct[0] = po.getPoIssueQty();
				} else {
					buffer[0] = po.getPoIssueQty();
				}
			}
		});
		double total = direct[0] + buffer[0];
		data.setPoDirectQty(direct[0]);
		data.setPoBufferQty(buffer[0]);
		data.setPoTotalQty(total);

		if ("Direct".equals(grn.getGodownType())) {
			data.setGrnNoDirect(grn.getGrnNo());
			data.setDateDirect(grn.getDate());
			data.setQtyDirect(grn.getMaterialReceivedQuantity());
		} else {
			data.setGrnNoBuffer(grn.getGrnNo());
			data.setDateBuffer(grn.getDate());
			data.setQtyBuffer(grn.getMaterialReceivedQuantity());
		}
		return data;
	}

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	@Override
	public List<DespatchAdviceRegisterTable> getDespatchAdviceRegisterData(String officeName, String month,
			LocalDate fromDate, LocalDate toDate) throws Exception {
		try {
			return despatchAdviceService.getDespatchAdviceDataByOffficeName(officeName).stream().filter(item -> {
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				return item.getVoucherStatus().equals("Approved") && monthFilter;
			}).map(item -> {
				try {
					Map<String, Double> productQtyMap = item.getTableData().stream()
							.collect(Collectors.groupingBy(DespatchAdviceTableDataEntity::getProductName,
									Collectors.summingDouble(DespatchAdviceTableDataEntity::getQty)));
					List<String> dcNoList = dcService.fetchDcData(item.getDespatchAdviceNo()).stream()
							.map(DespatchAdviseTable::getDcNo).collect(Collectors.toList());
					return new DespatchAdviceRegisterTable(item.getActivity(), item.getDespatchAdviceNo(),
							item.getIfmsId(), item.getNameOfInstitution(), item.getDistrict(), item.getGodownName(),
							productQtyMap, dcNoList, item.getStatusDisabledDate(), item.getDate());
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Despatch Advice Register data", e);
		}
	}

	@Autowired
	private PurchaseBookingService purchaseBookingService;

	@Autowired
	private CheckMemoGoodsService checkMemoGoodsService;

	@Override
	public List<PurchaseDayBookValue> getPurchaseDayBookValueData(String month, LocalDate fromDate, LocalDate toDate,
			String activity, String supplierName, String productName, String poNo) throws Exception {
		try {
			return purchaseBookingService.findPurchaseBookedDataByActivity(activity).stream().filter(item -> {
				Boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s%s%04d", item.getDate().getMonth(), " ", item.getDate().getYear())
							.equals(month);
				}
				Boolean productFilter = true;
				if (productName.isEmpty()) {
					productFilter = true;
				} else {
					productFilter = item.getProductName().equals(productName);
				}
				Boolean supplierFilter = true;
				if (supplierName.isEmpty()) {
					supplierFilter = true;
				} else {
					supplierFilter = item.getSupplierName().equals(supplierName);
				}
				Boolean poNoFilter = true;
				if (poNo.isEmpty()) {
					poNoFilter = true;
				} else {
					poNoFilter = item.getPoNo().equals(poNo);
				}
				return item.getVoucherStatus().equals("Approved") && monthFilter && productFilter && supplierFilter
						&& poNoFilter && item.getIsCheckMemoCreated().equals(true);
			}).map(item -> {
				try {
					CheckMemoGoods checkMemoGoods = checkMemoGoodsService
							.getCheckMemoGoodsByCmNo(item.getCheckMemoNo());
					Double[] doubles = mapInvoiceData(item);
					Double desc = (item.getMargin() + item.getTradeIncome()) - doubles[0];
					return new PurchaseDayBookValue(item.getCheckMemoNo(), mapRegionAndQty(item), item.getNet(),
							item.getMargin(), item.getTradeIncome(), item.getInputTax(),
							checkMemoGoods.getNetPaymentValue(), checkMemoGoods.getPercentageValue(),
							checkMemoGoods.getCalculatedTotal(), checkMemoGoods.getCreditNoteAdjAmount(), doubles[0],
							doubles[1], desc);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Purchase DayBook - Value Register data", e);
		}
	}

	private List<RegionQtyTypes> mapRegionAndQty(PurchaseBooking obj) {
		Map<String, RegionQtyTypes> regionMap = new HashMap<>();

		for (var item : obj.getGrnTableData()) {
			String region = item.getRegion();
			String type = item.getSuppliedTo();
			double qty = item.getInvoiceQty();

			regionMap.computeIfAbsent(region, k -> new RegionQtyTypes(region, 0.0, 0.0));

			if ("Direct".equals(type)) {
				regionMap.get(region).setDirect(qty);
			} else if ("Buffer".equals(type)) {
				regionMap.get(region).setBuffer(qty);
			}
		}

		return new ArrayList<>(regionMap.values());
	}

	private Double[] mapInvoiceData(PurchaseBooking obj) throws Exception {
		double allowedMargin = 0.0;
		double outputGst = 0.0;
		for (var item : obj.getGrnTableData()) {
			List<Invoice> invoices = new ArrayList<>();
			try {
				if (!item.getNonCcInvoiceData().isEmpty()) {
					for (var invData : item.getNonCcInvoiceData()) {
						Invoice invoice = invoiceService.getInvoiceDataByInvoiceNo(invData.getInvoiceNo());
						invoices.add(invoice);
					}
				} else {
					invoices.addAll(
							invoiceService.getInvoiceDataFromDateOfficeName(item.getRegion(), item.getDate()).stream()
									.filter(inv -> inv.getTableData().stream()
											.anyMatch(grn -> grn.getGrnNo().equals(item.getGrnNo())))
									.collect(Collectors.toList()));
				}
				for (var inv : invoices) {
					for (var grn : inv.getTableData()) {
						if (grn.getGrnNo().equals(item.getGrnNo())) {
							allowedMargin += grn.getMargin();
							outputGst += grn.getCgstAmount() + grn.getSgstAmount();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Double[] arr = new Double[2];
		arr[0] = allowedMargin;
		arr[1] = outputGst;
		return arr;
	}

	@Override
	public List<PurchaseDayBookQty> getPurchaseDayBookQtyData(String month, LocalDate fromDate, LocalDate toDate,
			String activity, String supplierName, String productName, String poNo) throws Exception {
		try {
			return purchaseBookingService.findPurchaseBookedDataByActivity(activity).stream().filter(item -> {
				boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s %04d", item.getDate().getMonth(), item.getDate().getYear())
							.equalsIgnoreCase(month);
				}

				boolean productFilter = productName.isEmpty() || item.getProductName().equals(productName);
				boolean supplierFilter = supplierName.isEmpty() || item.getSupplierName().equals(supplierName);
				boolean poNoFilter = poNo.isEmpty() || item.getPoNo().equals(poNo);

				return "Approved".equals(item.getVoucherStatus()) && monthFilter && productFilter && supplierFilter
						&& poNoFilter;
			}).flatMap(item -> {
				try {
					return mapPurchaseDayBookQtyData(item).stream();
				} catch (Exception e) {
					throw new RuntimeException("Error mapping PurchaseDayBookQtyData for: " + item.getPoNo(), e);
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Purchase DayBook - Qty Register data", e);
		}
	}

	private List<PurchaseDayBookQty> mapPurchaseDayBookQtyData(PurchaseBooking obj) throws Exception {
		PurchaseOrder purchaseOrder = poService.getPoByPoNo(obj.getPoNo());
		Map<String, RegionQtyTypes> regionMap = new HashMap<>();

		for (var item : purchaseOrder.getTableData()) {
			String region = item.getRegion();
			String type = item.getIssuedFor();
			double qty = item.getPoIssueQty();

			regionMap.computeIfAbsent(region, k -> new RegionQtyTypes(region, 0.0, 0.0));

			if ("Direct".equals(type)) {
				regionMap.get(region).setDirect(qty);
			} else if ("Buffer".equals(type)) {
				regionMap.get(region).setBuffer(qty);
			}
		}
		List<PurchaseDayBookQty> list = new ArrayList<PurchaseDayBookQty>();
		regionMap.entrySet().forEach(data -> {
			PurchaseDayBookQty purchaseDayBookQty = new PurchaseDayBookQty();
			purchaseDayBookQty.setCheckMemoNo(obj.getCheckMemoNo());
			purchaseDayBookQty.setRegion(data.getValue().getRegion());
			purchaseDayBookQty.setDirect(data.getValue().getDirect());
			purchaseDayBookQty.setBuffer(data.getValue().getBuffer());
			purchaseDayBookQty.setTotal(data.getValue().getDirect() + data.getValue().getBuffer());
			List<PurchaseDayBookQtyGrnData> grnDataWithoutGrouping = obj.getGrnTableData().stream()
					.filter(item -> item.getRegion().equals(data.getValue().getRegion())).map(item -> {
						return mapGrnData(item);
					}).collect(Collectors.toList());
			purchaseDayBookQty
					.setGrnData(
							grnDataWithoutGrouping
									.stream().collect(Collectors.toMap(PurchaseDayBookQtyGrnData::getGrnNo,
											Function.identity(), (existing, replacement) -> existing))
									.values().stream().toList());
			list.add(purchaseDayBookQty);
		});
		return list;
	}

	private PurchaseDayBookQtyGrnData mapGrnData(GrnTableDataForPurchaseBooking obj) {
		PurchaseDayBookQtyGrnData data = new PurchaseDayBookQtyGrnData();
		data.setGrnNo(obj.getGrnNo());
		data.setGrnDate(obj.getDate());
		if ("Direct".equals(obj.getSuppliedTo())) {
			data.setDirect(obj.getMaterialReceivedQuantity());
		} else if ("Buffer".equals(obj.getSuppliedTo())) {
			data.setBuffer(obj.getMaterialReceivedQuantity());
		}
		return data;
	}

	@Autowired
	private TcService tcService;

	@Override
	public List<TcBillRegisterTable> getTcBillRegisterData(String officeName, String month, LocalDate fromDate,
			LocalDate toDate) throws Exception {
		try {
			return tcService.fetchTcBillEntryByOfficeName(officeName).stream().filter(item -> {
				boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s %04d", item.getDate().getMonth(), item.getDate().getYear())
							.equalsIgnoreCase(month);
				}
				return monthFilter && item.getIsTcCheckMemoDone().equals(true)
						&& item.getVoucherStatus().equals("Approved");
			}).map(item -> {
				try {
					Double total = item.getTotalTransportCharges() + item.getTotalLoadingCharges()
							+ item.getTotalUnloadingCharges() + item.getTotalWagonClearanceCharges();
					TcCheckMemo tcCheckMemo = tcService.getTcCheckMemoByCheckMemoNo(item.getCheckMemoNo());
					return new TcBillRegisterTable(item.getCheckMemoNo(), item.getFinancialMonth(),
							item.getClaimBillNo(), item.getClaimBillDate(), item.getTotalBillValue(),
							item.getTotalTransportCharges(), item.getTotalLoadingCharges(),
							item.getTotalUnloadingCharges(), item.getTotalWagonClearanceCharges(), total, null,
							tcCheckMemo.getTotalCGST(), tcCheckMemo.getTotalSGST(),
							tcCheckMemo.getNetPaymentAfterAdjustment(), tcCheckMemo.getPercentageValue(),
							tcCheckMemo.getNetPaymentAfterTdsTcs(), null);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating TC Bill Register data", e);
		}
	}

	@Autowired
	private MpaService mpaService;

	@Override
	public List<TcBillRegisterTable> getMpaBillRegisterData(String officeName, String month, LocalDate fromDate,
			LocalDate toDate) throws Exception {
		try {
			return mpaService.getMpaBillEntryByOfficeName(officeName).stream().filter(item -> {
				boolean monthFilter;
				if (month.isEmpty()) {
					monthFilter = !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate);
				} else {
					monthFilter = String.format("%s %04d", item.getDate().getMonth(), item.getDate().getYear())
							.equalsIgnoreCase(month);
				}
				return monthFilter && item.getIsMpaCheckMemoDone().equals(true)
						&& item.getVoucherStatus().equals("Approved");
			}).map(item -> {
				try {
					MpaCheckMemo mpaCheckMemo = mpaService.getMpaCheckMemoByCheckMemoNo(item.getCheckMemoNo());
					return new TcBillRegisterTable(item.getCheckMemoNo(), item.getFinancialMonth(),
							item.getClaimBillNo(), item.getClaimBillDate(), item.getTotalBillValue(), null, null, null,
							null, mpaCheckMemo.getTotalPaymentValue(), null, mpaCheckMemo.getTotalCgstValue(),
							mpaCheckMemo.getTotalSgstValue(), mpaCheckMemo.getNetPaymentAfterAdjustment(),
							mpaCheckMemo.getCalculatedTcsTdsValue(), mpaCheckMemo.getTotalCalculatedValue(), null);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating TC Bill Register data", e);
		}
	}

	@Override
	public List<MovementRegister> getMovementRegisterData(String officeName, String godownName, LocalDate fromDate,
			LocalDate toDate, String outwardBatchNo) throws Exception {
		try {
			return dcService.getDeliveryChellanDataByOffficeName(officeName).stream().filter(item -> {
				Boolean godownFilter = true;
				if (godownName.isEmpty()) {
					godownFilter = true;
				} else {
					godownFilter = item.getGodownName().equals(godownName);
				}
				return godownFilter && !item.getDate().isBefore(fromDate) && !item.getDate().isAfter(toDate)
						&& item.getVoucherStatus().equals("Approved");
			}).map(i -> {
				List<ProductDataMovementRegister> productData = i.getDcTableData().stream()
						.filter(o -> (o.getOutwardBatchNo().equals(outwardBatchNo) || outwardBatchNo.isEmpty()))
						.map(k -> new ProductDataMovementRegister(k.getProductName(), k.getBags().toString(),
								k.getQty().toString(), k.getOutwardBatchNo()))
						.collect(Collectors.toList());
				if (productData.isEmpty()) {
					return java.util.Optional.<MovementRegister>empty();
				} else {
					return Optional.of(new MovementRegister(i.getActivity(), i.getGodownName(), i.getDcNo(),
							i.getDate(), i.getIfmsId(), i.getNameOfInstitution(), i.getDistrict(), i.getVehicleNo(),
							productData));
				}
			}).flatMap(Optional<MovementRegister>::stream).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception("Error while generating Movement Register data", e);
		}
	}
}
