package com.tanfed.inventry.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.OpeningStock;
import com.tanfed.inventry.entity.OutwardBatch;
import com.tanfed.inventry.model.GrnQtyUpdateForDc;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.Terms_Price_Config;
import com.tanfed.inventry.repository.OpeningStockRepo;
import com.tanfed.inventry.repository.OutwardBatchRepo;
import com.tanfed.inventry.response.DataForOpeningStock;
import com.tanfed.inventry.utils.CodeGenerator;

@Service
public class OpeningStockServiceImpl implements OpeningStockService {

	@Autowired
	private OpeningStockRepo openingStockRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private OutwardBatchRepo outwardBatchRepo;

	@Override
	public ResponseEntity<String> saveOpeningStock(List<OpeningStock> obj, String jwt) throws Exception {
		try {
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);

			obj.forEach(item -> {
				item.setEmpId(Arrays.asList(empId));
				item.setObId(codeGenerator.generateOpeningStockId());
			});
			openingStockRepo.saveAll(obj);
			return new ResponseEntity<String>("Created Successfully", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editOpeningStock(OpeningStock obj, String jwt) throws Exception {
		try {
			OpeningStock openingStock = openingStockRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			openingStock.getEmpId().add(empId);
			openingStockRepo.save(openingStock);
			return new ResponseEntity<String>("Updated Successfully", HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<OpeningStock> getOpeningStockByOfficeName(String officeName) throws Exception {
		try {
			return openingStockRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private MasterService masterService;

	@Autowired
	private GrnService grnService;

	@Override
	public DataForOpeningStock getDataForOpeningStock(String activity, String productName, String jwt,
			String officeName) throws Exception {
		try {
			DataForOpeningStock data = new DataForOpeningStock();
			List<ProductMaster> getProductData = masterService.getProductDataHandler(jwt);
			if (!activity.isEmpty() && activity != null) {
				data.setProductNameList(getProductData.stream().filter(item -> item.getActivity().equals(activity))
						.map(ProductMaster::getProductName).collect(Collectors.toList()));
				data.setGodownNameList(grnService.getGodownNameList(jwt, officeName, ""));
				if (!productName.isEmpty() && productName != null) {
					ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt, productName);
					data.setBatchNo(productMaster.getBatchNo());
					data.setCertification(productMaster.getCertification());
					data.setProductCategory(productMaster.getProductCategory());
					data.setProductGroup(productMaster.getProductGroup());
					data.setStandardUnits(productMaster.getStandardUnits());
					data.setSupplierGst(productMaster.getSupplierGst());
					data.setSupplierName(productMaster.getSupplierName());
					data.setPacking(productMaster.getPacking());
					data.setSupplyModeList(masterService.getTerms_Price_ConfigListHandler(jwt).stream()
							.filter(item -> item.getActivity().equals(activity)).map(Terms_Price_Config::getSupplyMode)
							.filter(Objects::nonNull).collect(Collectors.toList()));

				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateGrnQtyForDc(GrnQtyUpdateForDc obj, String despatchAdviceNo) throws Exception {
		try {
			OpeningStock ob = openingStockRepo.findByObId(obj.getOutwardBatchNo()).orElse(null);
			ob.setQtyAvlForDc(ob.getQtyAvlForDc() - obj.getQty());
			openingStockRepo.save(ob);
			try {
				if (obj.getDcNo() != null) {
					outwardBatchRepo.save(new OutwardBatch(null, LocalDateTime.now(), obj.getDcNo(),
							obj.getOutwardBatchNo(), obj.getQty(), ob.getProductCategory(), ob.getProductGroup(),
							ob.getSupplierName(), ob.getProductName(), ob.getPacking(), ob.getStandardUnits(), null,
							ob.getAsOn(), ob.getOfficeName(), ob.getGodownName(), despatchAdviceNo));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public OpeningStock getObById(String obId) throws Exception {
		return openingStockRepo.findByObId(obId).orElseThrow();
	}

}
