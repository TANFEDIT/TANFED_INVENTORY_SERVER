package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.PoReqTempTable;
import com.tanfed.inventry.entity.PoRequest;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.model.OfficeInfo;
import com.tanfed.inventry.model.PoReqTableData;
import com.tanfed.inventry.model.PoTableData;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.repository.PoReqTempTableRepo;
import com.tanfed.inventry.repository.PoRequestRepo;
import com.tanfed.inventry.response.DataForPoRequest;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.RoundToDecimalPlace;

@Service
public class PorequestServiceImpl implements PorequestService {

	@Autowired
	private PoRequestRepo poRequestRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	@Override
	public ResponseEntity<String> savePoRequest(PoRequest obj, String jwt) throws Exception {
		try {
			if (obj.getOfficeName().equals("Head Office")) {
				throw new Exception("No Access For Head Office");
			}
			String poRequestNo = codeGenerator.generatePoRequestNo(obj.getOfficeName(), obj.getDate());
			obj.setPoReqNo(poRequestNo);
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.getTableData().forEach(item -> item.setAlreadyIssuedQty(0.0));
			poRequestRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully" + "\n PoRequest No :" + poRequestNo,
					HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editPoRequest(PoRequest obj, String jwt) throws Exception {
		try {
			PoRequest poRequest = poRequestRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			poRequest.getEmpId().add(empId);
			PoReqTableData poReqTableData = obj.getTableData().get(0);

			poRequest.getTableData().forEach(item -> {
				if (item.getId().equals(poReqTableData.getId())) {
					int index = poRequest.getTableData().indexOf(item);
					poRequest.getTableData().set(index, poReqTableData);
				}
			});
			poRequestRepo.save(poRequest);
			return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<PoRequest> getPoRequestDataByOfficeName(String officeName) throws Exception {
		try {
			return poRequestRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private MasterService masterService;

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private PoService poService;

	@Override
	public DataForPoRequest getDataForPoRequest(String officeName, String activity, String jwt, String productName,
			String supplierName, String purchaseOrderType, String poNo, LocalDate date) throws Exception {
		try {
			DataForPoRequest data = new DataForPoRequest();
			if (!officeName.isEmpty() && officeName != null) {
				OfficeInfo officeInfo = masterService.getOfficeInfoByOfficeNameHandler(jwt, officeName);
				data.setDistrictList(officeInfo.getDistrictList());
				if (!activity.isEmpty() && activity != null) {
					data.setSupplierNameList(masterService.getSupplierNameHadnler(jwt, activity));
					if (!supplierName.isEmpty() && supplierName != null) {
						data.setProductNameList(termsPriceService.fetchApprovedProductName(activity, supplierName));
						if (!productName.isEmpty() && productName != null) {
							ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
									productName);
							data.setProductCategory(productMaster.getProductCategory());
							data.setProductGroup(productMaster.getProductGroup());
							data.setStandardUnits(productMaster.getStandardUnits());
							data.setSupplierGst(productMaster.getSupplierGst());
							data.setPoPendingFromHO(getPoRequestDataByOfficeName(officeName)
									.stream().filter(
											i -> i.getVoucherStatus().equals("Approved")
													&& i.getTableData().stream()
															.anyMatch(p -> p.getAlreadyIssuedQty() == 0
																	&& p.getProductName().equals(productName)))
									.count());
							if (!purchaseOrderType.isEmpty() && purchaseOrderType.equals("Confirmative")) {
								data.setPoNoList(
										poService.getPoNoForConfirmativePOReq(productName, officeName, "poReq", date));
								if (!poNo.isEmpty() && poNo != null) {
									PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
									data.setPoQty(RoundToDecimalPlace.roundToThreeDecimalPlaces(purchaseOrder
											.getTableData().stream().filter(item -> item.getRegion().equals(officeName))
											.mapToDouble(item -> item.getPoIssueQty()).sum()));

									data.setConsumedQty(RoundToDecimalPlace
											.roundToThreeDecimalPlaces(purchaseOrder.getGrnData().stream()
													.filter(itemData -> itemData.getOfficeName().equals(officeName))
													.mapToDouble(temp -> temp.getMaterialReceivedQuantity()).sum()));

								}
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

	@Override
	public List<PoRequest> getPoRequestData() throws Exception {
		try {
			return poRequestRepo.findAll();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PoReqTempTableRepo poReqTempTableRepo;

	@Override
	public ResponseEntity<String> updatePoIssueQty(List<PoTableData> obj, String productName) throws Exception {
		try {
			obj.forEach(item -> {

				PoRequest poRequest = poRequestRepo.findByPoReqNo(item.getPoReqNo()).orElse(null);
				poRequest.getTableData().forEach(temp -> {

					if (temp.getProductName().equals(productName)) {
						if (temp.getPoRequestFor().equals(item.getPoRequestFor())) {
							temp.setAlreadyIssuedQty(RoundToDecimalPlace
									.roundToTwoDecimalPlaces(temp.getAlreadyIssuedQty() + item.getPoIssueQty()));
							poReqTempTableRepo.save(new PoReqTempTable(null, item.getPoReqNo(), productName,
									temp.getPoRequestFor(), temp.getRequestQuantity(), temp.getAlreadyIssuedQty(),
									item.getPoIssueQty(), LocalDateTime.now(), poRequest.getOfficeName(),
									poRequest.getPurchaseOrderType()));
						}
					}
				});
				poRequestRepo.save(poRequest);
			});
			return new ResponseEntity<String>("Updated Successfully", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void updateRejectedQty(PoTableData obj, String productName) throws Exception {
		try {
			PoRequest poRequest = poRequestRepo.findByPoReqNo(obj.getPoReqNo()).orElse(null);
			poRequest.getTableData().forEach(temp -> {

				if (temp.getProductName().equals(productName)) {
					temp.setAlreadyIssuedQty(RoundToDecimalPlace
							.roundToTwoDecimalPlaces(temp.getAlreadyIssuedQty() - obj.getPoIssueQty()));
				}
			});
			poRequestRepo.save(poRequest);
		} catch (Exception e) {
			throw new Exception(e);
		}

	}

	@Override
	public PoRequest getPoRequestDataByNo(String poReqNo) throws Exception {
		try {
			return poRequestRepo.findByPoReqNo(poReqNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
