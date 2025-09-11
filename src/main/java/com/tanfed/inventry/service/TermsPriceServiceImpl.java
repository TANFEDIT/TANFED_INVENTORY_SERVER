package com.tanfed.inventry.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tanfed.inventry.config.JwtTokenValidator;
import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.ProductData;
import com.tanfed.inventry.model.ProductMaster;
import com.tanfed.inventry.model.Terms_Price_Config;
import com.tanfed.inventry.repository.TermsPriceRepo;
import com.tanfed.inventry.response.DataForTPM;
import com.tanfed.inventry.utils.CodeGenerator;

@Service
public class TermsPriceServiceImpl implements TermsPriceService {

	@Autowired
	private TermsPriceRepo termsPriceRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	private static Logger logger = LoggerFactory.getLogger(PoServiceImpl.class);

	@Override
	public ResponseEntity<String> saveTermsPriceMaster(TermsPrice obj, String jwt) throws Exception {
		try {
			logger.info("{}", obj);
			String termsNo;
			do {
				termsNo = codeGenerator.generateTermsNumber(obj.getMasterData().getActivity(),
						obj.getMasterData().getDate());
			} while (termsPriceRepo.findByTermsNo(termsNo).isPresent());
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));
			obj.setTermsNo(termsNo);
			obj.setExtentionDate(obj.getMasterData().getValidTo());
			obj.setCircularNo(
					codeGenerator.generateCircularNo(obj.getMasterData().getActivity(), obj.getMasterData().getDate()));
			termsPriceRepo.save(obj);
			return new ResponseEntity<String>("Created Successfully!" + "\n Terms No :" + termsNo, HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editTermsPriceMaster(TermsPrice obj, String jwt) throws Exception {
		try {
			TermsPrice termsPrice = termsPriceRepo.findById(obj.getId()).get();
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			termsPrice.getEmpId().add(empId);
			termsPrice.setMasterData(obj.getMasterData());
			termsPrice.setPurchaseTermsAndCondition(obj.getPurchaseTermsAndCondition());
			termsPrice.setPurchaseTermsPricing(obj.getPurchaseTermsPricing());
			termsPrice.setB2bTermsAndConditions(obj.getB2bTermsAndConditions());
			termsPrice.setB2bPrice(obj.getB2bPrice());
			termsPrice.setB2cPrice(obj.getB2cPrice());
			termsPrice.setPurchaseDataDirect(obj.getPurchaseDataDirect());
			termsPrice.setPurchaseDataBuffer(obj.getPurchaseDataBuffer());
			termsPrice.setPurchaseDataGeneral(obj.getPurchaseDataGeneral());
			termsPrice.setInputs(obj.getInputs());
			termsPrice.setExtentionDate(obj.getExtentionDate());
			termsPriceRepo.save(termsPrice);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<TermsPrice> getTermsPriceMasterData() throws Exception {
		return termsPriceRepo.findAll();
	}

	@Autowired
	private MasterService masterService;

	@Override
	public DataForTPM dataForTPM(String activity, String jwt, String supplierName, String productName)
			throws Exception {
		try {
			DataForTPM data = new DataForTPM();
			List<Terms_Price_Config> terms_Price_ConfigList = masterService.getTerms_Price_ConfigListHandler(jwt);
			List<ProductMaster> getProductData = masterService.getProductDataHandler(jwt);
			if (!activity.isEmpty() && activity != null) {

				data.setHeadNameGeneralList(terms_Price_ConfigList.stream().filter(item -> item.getHeadName() != null)
						.filter(item -> item.getActivity().equals(activity) && item.getSupplyType().equals("Both"))
						.map(Terms_Price_Config::getHeadName).filter(Objects::nonNull).collect(Collectors.toList()));
				data.setHeadNameDirectList(terms_Price_ConfigList.stream().filter(item -> item.getHeadName() != null)
						.filter(item -> item.getActivity().equals(activity) && item.getSupplyType().equals("Direct"))
						.map(Terms_Price_Config::getHeadName).filter(Objects::nonNull).collect(Collectors.toList()));
				data.setHeadNameBufferList(terms_Price_ConfigList.stream().filter(item -> item.getHeadName() != null)
						.filter(item -> item.getActivity().equals(activity) && item.getSupplyType().equals("Buffer"))
						.map(Terms_Price_Config::getHeadName).filter(Objects::nonNull).collect(Collectors.toList()));
				data.setPaymentModeList(terms_Price_ConfigList.stream()
						.filter(item -> item.getActivity().equals(activity)).map(Terms_Price_Config::getPaymentMode)
						.filter(Objects::nonNull).collect(Collectors.toList()));
				data.setSupplyModeList(terms_Price_ConfigList.stream()
						.filter(item -> item.getActivity().equals(activity)).map(Terms_Price_Config::getSupplyMode)
						.filter(Objects::nonNull).collect(Collectors.toList()));
				data.setSupplierList(getProductData.stream().filter(item -> item.getActivity().equals(activity))
						.map(ProductMaster::getSupplierName).collect(Collectors.toSet()));
				if (!supplierName.isEmpty() && supplierName != null) {
					data.setProductNameList(getProductData.stream().filter(
							item -> item.getActivity().equals(activity) && item.getSupplierName().equals(supplierName))
							.map(ProductMaster::getProductName).collect(Collectors.toList()));
					if (!productName.isEmpty() && productName != null) {
						ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
								productName);
						data.setProductData(
								new ProductData(productMaster.getSupplierGst(), productMaster.getStandardUnits(),
										productMaster.getPacking(), productMaster.getProductCategory(),
										productMaster.getProductGroup(), productMaster.getHsnCode()));
						data.setGstRate(productMaster.getGstRate());
					}
				}
			}
			return data;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public Set<String> fetchApprovedProductName(String activity) throws Exception {
		try {
			return getTermsPriceMasterData().stream()
					.filter(item -> "Approved".equals(item.getVoucherStatus())
							&& item.getMasterData().getActivity().equals(activity))
					.map(item -> item.getMasterData().getProductName()).collect(Collectors.toSet());

		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public Set<String> fetchApprovedTermsMonth(String activity, String productName) throws Exception {
		try {
			return getTermsPriceMasterData().stream()
					.filter(item -> "Approved".equals(item.getVoucherStatus())
							&& activity.equals(item.getMasterData().getActivity())
							&& productName.equals(item.getMasterData().getProductName()))
					.map(item -> item.getMasterData().getTermsForMonth()).collect(Collectors.toSet());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<String> fetchTermsByMonth(String termsMonth, String activity, String productName, LocalDate date,
			String type) throws Exception {
		try {
			return termsPriceRepo.findByTermsForMonth(termsMonth).stream()
					.filter(item -> termsMonth.equals(item.getMasterData().getTermsForMonth())
							&& activity.equals(item.getMasterData().getActivity())
							&& productName.equals(item.getMasterData().getProductName()))
					.filter(item -> type.equals("PO") ? (!date.isBefore(item.getMasterData().getValidFrom())
							&& !date.isAfter(item.getMasterData().getValidTo())) : true)
					.map(item -> item.getTermsNo()).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public TermsPrice fetchTermsByTermsNo(String termsNo) throws Exception {
		try {
			return termsPriceRepo.findByTermsNo(termsNo).get();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

}
