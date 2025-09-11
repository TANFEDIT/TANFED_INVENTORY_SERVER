package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.*;
import com.tanfed.inventry.model.*;
import com.tanfed.inventry.repository.PoReqTempTableRepo;
import com.tanfed.inventry.repository.PoRequestRepo;
import com.tanfed.inventry.repository.PurchaseOrderRepo;
import com.tanfed.inventry.response.DataForPo;
import com.tanfed.inventry.utils.CodeGenerator;

@Service
public class PoServiceImpl implements PoService {

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	@Autowired
	private PorequestService porequestService;

	@Autowired
	private PoRequestRepo porequestRepo;

	@Autowired
	private MasterService masterService;

	@Autowired
	private TermsPriceService termsPriceService;

	private static Logger logger = LoggerFactory.getLogger(PoServiceImpl.class);

	@Autowired
	private UserService userService;

	@Autowired
	private PoReqTempTableRepo poReqTempTableRepo;

	@Override
	public ResponseEntity<String> savePurchaseOrder(PurchaseOrder obj, String jwt) throws Exception {
		try {
			String poNo = codeGenerator.GeneratePoNo(obj.getActivity(), obj.getPoBased(), obj.getDate());
			logger.info(poNo);
			obj.setPoNo(poNo);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));

			purchaseOrderRepo.save(obj);
			obj.getTableData().forEach(item -> {
				poReqTempTableRepo.deleteItemsByProductNameAndPoReqNo(obj.getProductName(), item.getPoReqNo());
			});
			return new ResponseEntity<String>("Created Successfully" + "\n PO No :" + poNo, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editPurchaseOrder(PurchaseOrder obj, String jwt) throws Exception {
		try {
			PurchaseOrder purchaseOrder = purchaseOrderRepo.findByPoNo(obj.getPoNo()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			purchaseOrder.getEmpId().add(empId);
			purchaseOrder.setTableData(obj.getTableData());
			purchaseOrderRepo.save(obj);
			return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForPo getDataForPurchaseOrder(String activity, String productName, String jwt, String termsMonth,
			String termsNo, String poBased, String officeName, String purchaseOrderType, LocalDate date)
			throws Exception {
		try {
			DataForPo data = new DataForPo();
			if (!activity.isEmpty() && activity != null) {
				data.setProductNameList(termsPriceService.fetchApprovedProductName(activity));
				if (!productName.isEmpty() && productName != null) {
					ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt, productName);
					data.setProductCategory(productMaster.getProductCategory());
					data.setProductGroup(productMaster.getProductGroup());
					data.setStandardUnits(productMaster.getStandardUnits());
					data.setSupplierGst(productMaster.getSupplierGst());
					data.setSupplierName(productMaster.getSupplierName());
					data.setPacking(productMaster.getPacking());
					data.setTermsMonthList(termsPriceService.fetchApprovedTermsMonth(activity, productName));
					if (date != null) {
						setTermsData(data, termsMonth, termsNo, activity, productName, date);
						if (!poBased.isEmpty() && poBased.equals("Request")) {
							requestBasedPoData(data, productName, officeName, purchaseOrderType, date);
						}
					}
					if (!poBased.isEmpty() && poBased.equals("HoBased")) {
						data.setOfficeList(userService.getOfficeList().stream().map(item -> item.getOfficeName())
								.collect(Collectors.toSet()));
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	private void requestBasedPoData(DataForPo data, String productName, String officeName, String purchaseOrderType,
			LocalDate date) throws Exception {
		List<PoRequest> poReqNotFullfilledData = porequestService.getPoRequestData().stream().filter(item -> {
			double[] sum = item.getTableData().stream().reduce(new double[2], (acc, temp) -> {
				acc[0] += temp.getRequestQuantity();
				acc[1] += temp.getAlreadyIssuedQty();
				return acc;
			}, (acc1, acc2) -> {
				acc1[0] += acc2[0];
				acc1[1] += acc2[1];
				return acc1;
			});
			boolean isProductMatch = item.getTableData().stream()
					.anyMatch(temp -> productName.equals(temp.getProductName()));
			return sum[0] > sum[1] && isProductMatch && "Approved".equals(item.getVoucherStatus());
		}).collect(Collectors.toList());
		data.setOfficeList(
				poReqNotFullfilledData.stream().map(item -> item.getOfficeName()).collect(Collectors.toSet()));

		if (!officeName.isEmpty() && officeName != null) {
			List<PoReqDataForPo> reqData = poReqNotFullfilledData.stream()
					.filter(item -> officeName.equals(item.getOfficeName())
							&& item.getPurchaseOrderType().equals(purchaseOrderType)
							&& !item.getDate().isBefore(date.minusDays(30)))
					.flatMap(item -> item.getTableData().stream()
							.filter(temp -> productName.equals(temp.getProductName()))
							.map(itemData -> new PoReqDataForPo(item.getPoReqNo(), item.getPoNo(),
									itemData.getPoRequestFor(), itemData.getRequestQuantity(),
									itemData.getAlreadyIssuedQty())))
					.collect(Collectors.toList());

			List<PoReqTempTable> tempTableData = poReqTempTableRepo.findByOfficeName(officeName).stream()
					.filter(item -> productName.equals(item.getProductName())
							&& purchaseOrderType.equals(item.getPoReqType())
							&& item.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(3)))
					.collect(Collectors.toList());

			List<PoReqTempTable> toRemove = new ArrayList<PoReqTempTable>();

			reqData.forEach(item -> {
				tempTableData.forEach(temp -> {
					if (temp.getPoReqNo().equals(item.getPoReqNo())) {
						if (temp.getPoRequestFor().equals(item.getPoRequestFor())) {
							item.setAlreadyIssuedQty(item.getAlreadyIssuedQty() - temp.getIssueQty());
							try {
								updateReqQty(item.getPoReqNo(), productName, item.getPoRequestFor(),
										temp.getIssueQty());
								poReqTempTableRepo.deleteItemsByProductNameAndPoReqNo(productName, item.getPoReqNo());
							} catch (Exception e) {
								e.printStackTrace();
							}
							toRemove.add(temp);
						}
					}
				});
			});
			tempTableData.removeAll(toRemove);
			reqData.addAll(
					tempTableData.stream()
							.map(item -> new PoReqDataForPo(item.getPoReqNo(), null, item.getPoRequestFor(),
									item.getRequestQuantity(), item.getAlreadyIssuedQty()))
							.collect(Collectors.toList()));
			data.setPoReqTableData(reqData);
		}

	}

	private void updateReqQty(String poReqNo, String productName, String poReqFor, Double qty) throws Exception {
		PoRequest poRequest = porequestService.getPoRequestDataByNo(poReqNo);
		poRequest.getTableData().forEach(item -> {
			if (item.getProductName().equals(productName)) {
				if (item.getPoRequestFor().equals(poReqFor)) {
					item.setAlreadyIssuedQty(item.getAlreadyIssuedQty() - qty);
				}
			}
		});
		porequestRepo.save(poRequest);
	}

	private void setTermsData(DataForPo data, String termsMonth, String termsNo, String activity, String productName,
			LocalDate date) throws Exception {
		if (!termsMonth.isEmpty() && termsMonth != null) {
			data.setTermsNoList(termsPriceService.fetchTermsByMonth(termsMonth, activity, productName, date, "PO"));
			if (!termsNo.isEmpty() && termsNo != null) {
				data.setTermsPrice(termsPriceService.fetchTermsByTermsNo(termsNo));
			}
		}
	}

	@Override
	public List<PurchaseOrder> getPoData() throws Exception {
		try {
			return purchaseOrderRepo.findAll();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<String> getUnfullfilledPoNo(String productName, String officeName, String poNoFor, LocalDate date)
			throws Exception {
		try {
			return getPoData().stream().filter(
					item -> item.getProductName().equals(productName) && item.getVoucherStatus().equals("Approved"))
					.filter(item -> {
						double issuedQty = item.getTableData().stream()
								.filter(data -> data.getRegion().equals(officeName))
								.mapToDouble(PoTableData::getPoIssueQty).sum();

						double grnCreated = item.getGrnData().isEmpty() ? 0.0
								: item.getGrnData().stream()
										.filter(data -> data.getOfficeName().equals(officeName)
												&& data.getVoucherStatus().equals("Approved"))
										.mapToDouble(data -> data.getMaterialReceivedQuantity()).sum();

						boolean isDateMatch = false;
						boolean isRegularPo = true;
						boolean isConfirmativePoPresent = true;

						if ("grn".equals(poNoFor)) {
							logger.info("{}", date);
							isDateMatch = !item.getDate().isBefore(date.minusDays(30));
						} else if ("poReq".equals(poNoFor)) {
							isRegularPo = !"Regular".equals(item.getPurchaseOrderType());
							isDateMatch = item.getDate().isBefore(date.minusDays(30));

							if ("Confirmative".equals(item.getPurchaseOrderType())) {
								boolean hasOldPo = item.getTableData().stream()
										.anyMatch(data -> data.getOldPoNo().equals(item.getPoNo()));
								isConfirmativePoPresent = !hasOldPo;
							}
						}

						return isDateMatch && issuedQty > grnCreated && isRegularPo && isConfirmativePoPresent;
					}).map(PurchaseOrder::getPoNo).collect(Collectors.toList());

		} catch (Exception e) {
			throw new Exception("Error while fetching unfulfilled PO numbers", e);
		}
	}

	@Override
	public PurchaseOrder getPoByPoNo(String poNo) throws Exception {
		try {
			return purchaseOrderRepo.findByPoNo(poNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
