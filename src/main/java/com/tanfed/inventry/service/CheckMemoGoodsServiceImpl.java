package com.tanfed.inventry.service;

import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.dto.CheckMemoGoodsDto;
import com.tanfed.inventry.dto.DataForCheckMemoGoods;
import com.tanfed.inventry.entity.CheckMemoGoods;
import com.tanfed.inventry.entity.PurchaseBooking;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.model.BeneficiaryMaster;
import com.tanfed.inventry.model.GrnTableDataForPurchaseBooking;
import com.tanfed.inventry.model.JournalVoucher;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.SupplierAdvance;
import com.tanfed.inventry.model.VoucherApproval;
import com.tanfed.inventry.model.Vouchers;
import com.tanfed.inventry.repository.CheckMemoGoodsRepo;
import com.tanfed.inventry.repository.PurchaseBookingRepo;

@Service
public class CheckMemoGoodsServiceImpl implements CheckMemoGoodsService {

	@Autowired
	private PurchaseBookingService purchaseBookingService;

	@Autowired
	private PurchaseBookingRepo purchaseBookingRepo;

	@Autowired
	private CheckMemoGoodsRepo checkMemoGoodsRepo;

	@Autowired
	private MasterService masterService;

	@Autowired
	private AccountsService accountsService;

	@Autowired
	private PoService poService;

	private static Logger logger = LoggerFactory.getLogger(CheckMemoGoodsServiceImpl.class);

	@Override
	public DataForCheckMemoGoods getDataForCheckMemoGoods(String activity, String checkMemoNo, String jwt,
			String supplierAdvanceNo, String month) throws Exception {
		try {
			DataForCheckMemoGoods data = new DataForCheckMemoGoods();
			if (month != null && !month.isEmpty()) {
				data.setCheckMemoGoods(getCheckMemoData().stream().filter(item -> {
					String cmMonth = String.format("%s%s%04d",
							item.getCmDate().getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH), " ",
							item.getCmDate().getYear());
					return cmMonth.equals(month);
				}).collect(Collectors.toList()));
			}
			if (activity != null && !activity.isEmpty()) {
				data.setCheckMemoNoList(purchaseBookingService.findPurchaseBookedDataByActivity(activity).stream()
						.filter(item -> item.getIsCheckMemoCreated().equals(false)).map(item -> item.getCheckMemoNo())
						.collect(Collectors.toList()));

				if (checkMemoNo != null && !checkMemoNo.isEmpty()) {
					PurchaseBooking purchaseBooking = purchaseBookingService.getPurchaseBookedDataByCmNo(checkMemoNo);

					data.setPbData(purchaseBooking);
					ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
							purchaseBooking.getProductName());

					logger.info("{}", productMaster);
					data.setHsnCode(productMaster.getHsnCode());
					data.setSupplierGst(productMaster.getSupplierGst());
					data.setGstRate(productMaster.getGstRate());
					data.setGstData(productMaster.getGstData());
					logger.info("{}", productMaster.getSupplierName());

					List<BeneficiaryMaster> beneficiaryMasterByName = masterService.getBeneficiaryMasterByName(jwt,
							productMaster.getSupplierName(), "Head Office");

					BeneficiaryMaster beneficiaryMaster = beneficiaryMasterByName
							.get(beneficiaryMasterByName.size() - 1);
					data.setSupplierAccountNo(beneficiaryMaster.getAccountNo().toString());

					PurchaseOrder purchaseOrder = poService.getPoByPoNo(purchaseBooking.getPoNo());
					data.setPoDate(purchaseOrder.getDate());

					List<SupplierAdvance> saList = accountsService
							.fetchOutstandingAdvancesByProductHandler(purchaseBooking.getProductName(), jwt).stream()
							.filter(item -> item.getTermsNo().equals(purchaseBooking.getTermsNo())
									&& item.getVoucherStatus().equals("Approved") && item.getPv() != null
									&& item.getAvlAmountForCheckMemo() > 0)
							.collect(Collectors.toList());

					data.setAdvOutstanding(saList.stream().mapToDouble(item -> item.getAvlAmountForCheckMemo()).sum());
					data.setSupplierAdvanceNoList(
							saList.stream().map(item -> item.getSupplierAdvanceNo()).collect(Collectors.toList()));

					Map<String, Double> map = purchaseBooking.getGrnTableData().stream()
							.collect(Collectors.toMap(GrnTableDataForPurchaseBooking::getGrnNo,
									GrnTableDataForPurchaseBooking::getMaterialReceivedQuantity,
									(existing, replacement) -> existing));

					data.setTotalGrnQty(map.entrySet().stream().mapToDouble(item -> item.getValue()).sum());
					data.setTotalSupplierInvQty(
							purchaseBooking.getGrnTableData().stream().mapToDouble(item -> item.getInvoiceQty()).sum());

					purchaseBooking.getJvList().forEach(item -> {
						try {
							JournalVoucher journalVoucher = accountsService
									.getAccountsVoucherByVoucherNoHandler("journalVoucher", item, jwt)
									.getJournalVoucherData();
							if (journalVoucher.getJvFor().equals("Net Purchase Value")) {
								data.setPurchaseJvNo(journalVoucher.getVoucherNo());
								data.setJvQty(journalVoucher.getDerivedQty());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					});
					if (supplierAdvanceNo != null && !supplierAdvanceNo.isEmpty()) {
						SupplierAdvance supplierAdvance = saList.stream()
								.filter(item -> item.getSupplierAdvanceNo().equals(supplierAdvanceNo))
								.collect(Collectors.toList()).get(0);
						data.setAdvanceQty(supplierAdvance.getAvlQtyForCheckMemo());
						data.setAdvanceOutstanding(supplierAdvance.getAvlAmountForCheckMemo());
						data.setAdvanceBasicValue(supplierAdvance.getMultipliedBasicPrice());
						data.setAdvanceTdsTcsValue(supplierAdvance.getTotalMultipliedTdsAndTcsValue());
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> saveCheckMemoGoods(CheckMemoGoodsDto obj, String jwt) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			CheckMemoGoods cmg = new CheckMemoGoods();
			cmg.setActivity(obj.getActivity());
			cmg.setCheckMemoNo(obj.getCheckMemoNo());
			cmg.setCmDate(obj.getCmDate());

			cmg.setProductCategory(obj.getProductCategory());
			cmg.setProductGroup(obj.getProductGroup());
			cmg.setProductName(obj.getProductName());
			cmg.setStandardUnits(obj.getStandardUnits());
			cmg.setPacking(obj.getPacking());
			cmg.setHsnCode(obj.getHsnCode());
			cmg.setSupplierName(obj.getSupplierName());
			cmg.setSupplierAccountNo(obj.getSupplierAccountNo());
			cmg.setSupplierGst(obj.getSupplierGst());

			cmg.setPoType(obj.getPoType());
			cmg.setPoMonth(obj.getPoMonth());
			cmg.setPoNo(obj.getPoNo());
			cmg.setPoDate(obj.getPoDate());
			cmg.setTotalPoQty(obj.getTotalPoQty());
			cmg.setTermsNo(obj.getTermsNo());

			cmg.setAdvOutstanding(obj.getAdvOutstanding());
			cmg.setGstRate(obj.getGstRate());

			cmg.setGstData(obj.getGstData());
			cmg.setTotalGrnQty(obj.getTotalGrnQty());
			cmg.setTotalSupplierInvQty(obj.getTotalSupplierInvQty());
			cmg.setJvQty(obj.getJvQty());
			cmg.setPurchaseJvNo(obj.getPurchaseJvNo());

			cmg.setAdvanceAdjOptions(obj.getAdvanceAdjOptions());
			cmg.setSupplierAdvanceNo(obj.getSupplierAdvanceNo());
			cmg.setCurrentAdvanceQty(obj.getCurrentAdvanceQty());
			cmg.setCalulatedBasicPrice(obj.getCalulatedBasicPrice());
			cmg.setCalculatedTcsTdsValue(obj.getCalculatedTcsTdsValue());
			cmg.setCalculatedTotal(obj.getCalculatedTotal());

			cmg.setCreditNoteAdjOptions(obj.getCreditNoteAdjOptions());
			cmg.setCreditNoteAdjAmount(obj.getCreditNoteAdjAmount());
			cmg.setCreditNoteAdjCnNo(obj.getCreditNoteAdjCnNo());
			cmg.setCreditNoteCnDate(obj.getCreditNoteCnDate());

			cmg.setTermsData(obj.getTermsData());
			cmg.setTermsDataGeneral(obj.getTermsDataGeneral());
			cmg.setTermsDataDirect(obj.getTermsDataDirect());
			cmg.setTermsDataBuffer(obj.getTermsDataBuffer());

			cmg.setTotalPaymentValue(obj.getTotalPaymentValue());
			cmg.setNetPaymentValue(obj.getNetPaymentValue());
			cmg.setRate(obj.getRate());
			cmg.setPercentageValue(obj.getPercentageValue());
			cmg.setNetPaymentAfterAdjustment(obj.getNetPaymentAfterAdjustment());
			cmg.setDifference(obj.getDifference());
			cmg.setRemarks(obj.getRemarks());
			Vouchers voucher = new Vouchers();
			List<String> jvNoList = new ArrayList<String>();
			if (!obj.getJvData().isEmpty()) {
				obj.getJvData().forEach(item -> {
					voucher.setJournalVoucherData(item);
					try {
						ResponseEntity<String> responseEntity = accountsService
								.saveAccountsVouchersHandler("journalVoucher", voucher, jwt);
						String responseString = responseEntity.getBody();
						String prefix = "JV Number : ";
						int index = responseString.indexOf(prefix);
						jvNoList.add(responseString.substring(index + prefix.length()).trim());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				cmg.setJvNoList(jvNoList);
			}
			if(obj.getAdvanceAdjOptions().equals("Yes")) {
				accountsService.updateAvlQtyAndAmountHandler(obj.getSupplierAdvanceNo(), obj.getCurrentAdvanceQty(),
						obj.getCalculatedTotal());				
			}
			updatePB(obj.getCheckMemoNo());
			cmg.setVoucherStatus("Pending");
			cmg.setEmpId(Arrays.asList(empId));
			checkMemoGoodsRepo.save(cmg);
			return new ResponseEntity<String>("Created Successfully!", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void updatePB(String cmNo) throws Exception {
		PurchaseBooking purchaseBooking = purchaseBookingService.getPurchaseBookedDataByCmNo(cmNo);
		purchaseBooking.setIsCheckMemoCreated(true);
		purchaseBookingRepo.save(purchaseBooking);
	}

	@Override
	public void updatePvInCheckMemo(String pv, Long id) throws Exception {
		CheckMemoGoods checkMemoGoods = checkMemoGoodsRepo.findById(id).get();
		String prefix = "Voucher Number : ";
		int index = pv.indexOf(prefix);
		checkMemoGoods.setPvNo(pv.substring(index + prefix.length()).trim());
		checkMemoGoodsRepo.save(checkMemoGoods);
	}

	@Override
	public List<CheckMemoGoods> getCheckMemoData() throws Exception {
		return checkMemoGoodsRepo.findAll();
	}

	@Override
	public void updatePvInAcc(CheckMemoGoods item, String jwt) throws Exception {
		try {
			Vouchers pv = accountsService.getAccountsVoucherByVoucherNoHandler("paymentVoucher", item.getPvNo(), jwt);
			accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
					String.valueOf(pv.getPaymentVoucherData().getId()), "paymentVoucher"), jwt);
			item.getJvNoList().forEach(jvNo -> {
				try {
					Vouchers jv = accountsService.getAccountsVoucherByVoucherNoHandler("journalVoucher", jvNo, jwt);
					accountsService.voucherApprovalHandler(new VoucherApproval(item.getVoucherStatus(),
							String.valueOf(jv.getJournalVoucherData().getId()), "journalVoucher"), jwt);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public CheckMemoGoods getCheckMemoGoodsByCmNo(String checkMemoNo) throws Exception {
		try {
			return checkMemoGoodsRepo.findByCheckMemoNo(checkMemoNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
