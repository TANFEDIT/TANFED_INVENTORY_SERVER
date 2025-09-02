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
			String purchaseOrderType, String poNo, LocalDate date) throws Exception {
		try {
			DataForPoRequest data = new DataForPoRequest();
			if (!officeName.isEmpty() && officeName != null) {
				OfficeInfo officeInfo = masterService.getOfficeInfoByOfficeNameHandler(jwt, officeName);
				data.setDistrictList(officeInfo.getDistrictList());
				if (!activity.isEmpty() && activity != null) {
					data.setProductNameList(termsPriceService.fetchApprovedProductName(activity));
					if (!productName.isEmpty() && productName != null) {
						ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
								productName);
						data.setProductCategory(productMaster.getProductCategory());
						data.setProductGroup(productMaster.getProductGroup());
						data.setStandardUnits(productMaster.getStandardUnits());
						data.setSupplierGst(productMaster.getSupplierGst());
						data.setSupplierName(productMaster.getSupplierName());
						if (!purchaseOrderType.isEmpty() && purchaseOrderType.equals("Confirmative")) {
							data.setPoNoList(poService.getUnfullfilledPoNo(productName, officeName, "poReq", date));
							if (!poNo.isEmpty() && poNo != null) {
								PurchaseOrder purchaseOrder = poService.getPoByPoNo(poNo);
								data.setPoQty(purchaseOrder.getTableData().stream()
										.filter(item -> item.getRegion().equals(officeName))
										.mapToDouble(item -> item.getPoIssueQty()).sum());

								data.setAlreadyIssuedQty(purchaseOrder.getGrnData().stream()
										.filter(itemData -> itemData.getOfficeName().equals(officeName))
										.mapToDouble(temp -> temp.getMaterialReceivedQuantity()).sum());

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
//			fetching PO request by REQ no
				PoRequest poRequest = poRequestRepo.findByPoReqNo(item.getPoReqNo()).orElse(null);
				poRequest.getTableData().forEach(temp -> {

//				mapping issued QTY by district
					if (temp.getProductName().equals(productName)) {
						if (temp.getPoRequestFor().equals(item.getPoRequestFor())) {
							temp.setAlreadyIssuedQty(temp.getAlreadyIssuedQty() + item.getPoIssueQty());
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

//				mapping issued QTY by district
				if (temp.getProductName().equals(productName)) {
					temp.setAlreadyIssuedQty(temp.getAlreadyIssuedQty() - obj.getPoIssueQty());
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
