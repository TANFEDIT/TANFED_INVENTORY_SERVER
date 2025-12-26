package com.tanfed.inventry.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import com.tanfed.inventry.repository.*;
import com.tanfed.inventry.response.DataForDc;
import com.tanfed.inventry.utils.CodeGenerator;
import com.tanfed.inventry.utils.SlabRateCalculator;


@Service
public class DcServiceImpl implements DcService {

	@Autowired
	private DeliveryChellanRepo deliveryChellanRepo;

	@Autowired
	private CodeGenerator codeGenerator;

	@Autowired
	private MasterService masterService;

	@Autowired
	private DespatchAdviceService despatchAdviceService;

	@Autowired
	private GrnService grnService;

	@Autowired
	private TermsPriceService termsPriceService;

	@Autowired
	private PoService poService;

	@Autowired
	private GtnService gtnService;

	@Autowired
	private GrnRepo grnRepo;

	@Autowired
	private GtnRepo gtnRepo;

	@Autowired
	private OutwardBatchRepo outwardBatchRepo;

	@Autowired
	private OpeningStockRepo openingStockRepo;

	private static Logger logger = LoggerFactory.getLogger(GrnServiceImpl.class);

	@Override
	public ResponseEntity<String> saveDc(DeliveryChellan obj, String jwt) throws Exception {
		try {
			logger.info("{}", obj);
//			setting values to entity
			for (int i = 0; i < obj.getDcTableData().size(); i++) {
				DcTableData dcData = obj.getDcTableData().get(i);
				ProductMaster productMaster = masterService.getProductDataByProductNameHandler(jwt,
						dcData.getProductName());
				dcData.setHsnCode(productMaster.getHsnCode());
				dcData.setGstRate(productMaster.getGstRate());
			}
			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			obj.setEmpId(Arrays.asList(empId));

			obj.setVoucherStatus("Pending");
			obj.setTotalBags(obj.getDcTableData().stream().mapToDouble(item -> item.getBags()).sum());
			obj.setTotalQty(obj.getDcTableData().stream().mapToDouble(item -> item.getQty()).sum());
//			Saving data in database
			deliveryChellanRepo.save(obj);
			outwardBatchRepo.deleteItemsByDcNo(obj.getDcNo());
			return new ResponseEntity<String>("Created Successfully!" + "\n DC No :" + obj.getDcNo(),
					HttpStatus.CREATED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> editDc(DeliveryChellan obj, String jwt) throws Exception {
		try {
			DeliveryChellan deliveryChellan = deliveryChellanRepo.findById(obj.getId()).get();

			String empId = JwtTokenValidator.getEmailFromJwtToken(jwt);
			deliveryChellan.getEmpId().add(empId);

			deliveryChellanRepo.save(deliveryChellan);
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<DeliveryChellan> getDeliveryChellanDataByOffficeName(String officeName) throws Exception {
		try {
			return deliveryChellanRepo.findByOfficeName(officeName);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public DataForDc getDataForDeliveryChellan(String officeName, String jwt, String ifmsId, String activity,
			LocalDate date, String despatchAdviceNo, String productName, String godownName) throws Exception {
		try {
			DataForDc data = new DataForDc();
			if (!officeName.isEmpty() && officeName != null) {
				List<DespatchAdvice> despatchAdviceData = despatchAdviceService
						.getDespatchAdviceDataByOffficeName(officeName);
				data.setDcNo(codeGenerator.dcNoGenerator(officeName, date));

				data.setClData(deliveryChellanRepo.findByOfficeName(officeName).stream()
						.filter(item -> item.getLoadType().equals("Combined Load") && item.getClNo() == null
								&& !item.getVoucherStatus().equals("Approved"))
						.map(item -> new CompondLoadDcData(item.getDcNo(), item.getNameOfInstitution(),
								item.getTotalQty(), item.getVehicleNo()))
						.collect(Collectors.toList()));

				if (!activity.isEmpty() && activity != null) {
					data.setGodownNameList(
							despatchAdviceData.stream().filter(item -> item.getVoucherStatus().equals("Approved"))
									.map(item -> item.getGodownName()).collect(Collectors.toSet()));
					if (!godownName.isEmpty() && godownName != null) {
						data.setDespatchAdviceNoList(
								despatchAdviceService.getUnfullfilledDespatchAdviceNo(officeName, activity));

						if (!despatchAdviceNo.isEmpty() && despatchAdviceNo != null) {
							DespatchAdvice despatchAdvice = despatchAdviceService
									.getDespatchAdviceDataByDespatchAdviceNo(despatchAdviceNo);
							data.setIfmsId(despatchAdvice.getIfmsId());
							data.setDistrict(despatchAdvice.getDistrict());
							data.setTaluk(despatchAdvice.getTaluk());
							data.setBlock(despatchAdvice.getBlock());
							data.setVillage(despatchAdvice.getVillage());
							data.setBuyerGstNo(despatchAdvice.getBuyerGstNo());
							data.setNameOfInstitution(despatchAdvice.getNameOfInstitution());
							data.setLicenseNo(despatchAdvice.getLicenseNo());
							data.setSupplyMode(despatchAdvice.getSupplyMode());
							data.setSupplyTo(despatchAdvice.getSupplyTo());
							data.setDespatchAdviseDate(despatchAdvice.getDate());
							data.setProductNameList(
									despatchAdvice.getTableData().stream().filter(item -> item.getQtyAvlForDc() > 0)
											.map(item -> item.getProductName()).collect(Collectors.toSet()));

							if (!godownName.equals("Direct Material Center")) {
								data.setTransporterName(
										masterService.getContractFirmByGodownNameHandler(jwt, officeName, godownName)
												.getContractFirm());

								ContractorInfo contractorInfo = masterService.getContractFirmByGodownNameHandler(jwt,
										officeName, godownName);
								ContractorChargesData contractorChargesData = contractorInfo.getChargesData()
										.get(contractorInfo.getChargesData().size() - 1);

								final double[] isHillKmPresent = { 0.0 };
								masterService.getDistanceData(jwt, officeName, godownName).forEach(item -> {
									item.getTableData().forEach(itemData -> {
										if (itemData.getName().equals(despatchAdvice.getNameOfInstitution())) {
											data.setKm(itemData.getKm() + itemData.getHillKm());
											isHillKmPresent[0] = itemData.getHillKm();
										}
									});
								});

								double[] rates = { contractorChargesData.getZero_seven(),
										contractorChargesData.getEight_twenty(),
										contractorChargesData.getTwentyone_fifty(),
										contractorChargesData.getFiftyone_seventyfive(),
										contractorChargesData.getSeventysix_hundred(),
										contractorChargesData.getHundredone_onetwentyfive(),
										contractorChargesData.getOnetwosix_onefifty(),
										contractorChargesData.getOnefiftyone_oneseventyfive(),
										contractorChargesData.getOneseventysix_twohundred(),
										contractorChargesData.getAbovetwohundredone() };
								Double transportChargesPlain = SlabRateCalculator.calculateSlabRate(data.getKm(),
										rates);

								if (isHillKmPresent[0] != 0.0) {
									Double transportChargesHill = SlabRateCalculator
											.calculateSlabRate(isHillKmPresent[0], rates);
									Double hillCharges = transportChargesHill
											+ (transportChargesHill * (contractorChargesData.getHillRate() / 100));
									data.setTransportChargesPerQty(transportChargesPlain + hillCharges);
								} else {
									data.setTransportChargesPerQty(transportChargesPlain);
								}
								data.setLoadingChargesPerQty(contractorChargesData.getLoadingCharges());
							}

							if (!productName.isEmpty() && productName != null) {
								data.setDespatchAdviseQty(roundToTwoDecimalPlaces(despatchAdvice.getTableData().stream()
										.filter(item -> item.getProductName().equals(productName))
										.map(item -> item.getQtyAvlForDc()).collect(Collectors.toList()).get(0)));
								data.setTableData(fetchTableData(officeName, productName, godownName));
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

	public List<TableDataForDc> fetchTableData(String officeName, String productName, String godownName)
			throws Exception {
		try {
			List<TableDataForDc> tableData = grnService.grnTableData(officeName, productName, godownName, "dc");
			tableData.addAll(gtnService.gtnTableData(officeName, productName, godownName));
			logger.info("{}", tableData);
			List<OutwardBatch> unfullfilledGrnQty = fetchUnfullfilledGrnQty(officeName, productName, godownName);
			List<OutwardBatch> toRemove = new ArrayList<OutwardBatch>();
			unfullfilledGrnQty.forEach(item -> {
				tableData.forEach(data -> {
					if (data.getOutwardBatchNo().equals(item.getBatchNo())) {
						data.setAvlQty(data.getAvlQty() + item.getQty());
						try {
							updateAvlQtyInGrn(data.getOutwardBatchNo(), item.getQty());
							updateAvlQtyInGtn(data.getOutwardBatchNo(), item.getQty());
						} catch (Exception e) {
							e.printStackTrace();
						}
						outwardBatchRepo.deleteById(item.getId());
						toRemove.add(item);
					}
				});
			});
			unfullfilledGrnQty.removeAll(toRemove);
			unfullfilledGrnQty.forEach(item -> {
				try {
					GRN grn = updateAvlQtyInGrn(item.getBatchNo(), item.getQty());
					tableData.add(new TableDataForDc(grn.getProductCategory(), grn.getProductGroup(),
							grn.getSupplierName(), grn.getProductName(), grn.getPacking(), grn.getStandardUnits(),
							roundToTwoDecimalPlaces(grn.getGrnQtyAvlForDc()), grn.getGrnNo(),
							poService.getPoByPoNo(grn.getPoNo()).getTermsPrice().getTermsNo(),
							fetchCollectionModeFromPo(grn.getPoNo()), fetchMrpFromPoNo(grn.getPoNo()), grn.getDate()));

					GTN gtn = updateAvlQtyInGtn(item.getBatchNo(), item.getQty());
					gtn.getGtnTableData().forEach(itemData -> {
						tableData.add(new TableDataForDc(gtn.getProductCategory(), gtn.getProductGroup(),
								gtn.getSupplierName(), gtn.getProductName(), itemData.getPacking(),
								itemData.getStandardUnits(), roundToTwoDecimalPlaces(itemData.getQtyAvlForDc()),
								gtn.getGtnNo(), gtnService.fetchTermsNoFromGrnNo(itemData.getOutwardBatchNo()),
								itemData.getCollectionMode(), itemData.getMrp(), gtn.getDate()));

						OpeningStock ob = updateAvlQtyInOb(item.getBatchNo(), item.getQty());
						tableData.add(new TableDataForDc(ob.getProductCategory(), ob.getProductGroup(),
								ob.getSupplierName(), ob.getProductName(), ob.getPacking(), ob.getStandardUnits(),
								roundToTwoDecimalPlaces(ob.getQtyAvlForDc()), ob.getObId(), null, null, null,
								ob.getAsOn()));
					});
					outwardBatchRepo.deleteById(item.getId());
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			tableData.addAll(getObData(officeName, productName, godownName));
			return tableData;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<TableDataForDc> getObData(String officeName, String productName, String godownName) {
		return openingStockRepo.findAll().stream()
				.filter(item -> item.getQtyAvlForDc() > 0 && item.getOfficeName().equals(officeName)
						&& item.getProductName().equals(productName) && item.getGodownName().equals(godownName))
				.map(item -> new TableDataForDc(item.getProductCategory(), item.getProductGroup(),
						item.getSupplierName(), item.getProductName(), item.getPacking(), item.getStandardUnits(),
						item.getQtyAvlForDc(), item.getObId(), null, null, item.getB2cMrp(), item.getAsOn()))
				.collect(Collectors.toList());
	}

	public GRN updateAvlQtyInGrn(String grnNo, Double qty) throws Exception {
		GRN grn = grnService.getGrnDataByGrnNo(grnNo);
		grn.setGrnQtyAvlForDc(grn.getGrnQtyAvlForDc() + qty);
		grnRepo.save(grn);
		return grn;
	}

	public GTN updateAvlQtyInGtn(String gtnNo, Double qty) throws Exception {
		GTN gtn = gtnService.getGtnDataByGtnNo(gtnNo);
		GtnTableData qtyUpdateForDc = gtn.getGtnTableData().get(0);
		qtyUpdateForDc.setQtyAvlForDc(qtyUpdateForDc.getQtyAvlForDc() + qty);
		gtnRepo.save(gtn);
		return gtn;
	}

	@Autowired
	private OpeningStockService openingStockService;

	public OpeningStock updateAvlQtyInOb(String obId, Double qty) {
		try {
			OpeningStock ob = openingStockService.getObById(obId);
			ob.setQtyAvlForDc(ob.getQtyAvlForDc() + qty);
			openingStockRepo.save(ob);
			return ob;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<OutwardBatch> fetchUnfullfilledGrnQty(String officeName, String productName, String godownName)
			throws Exception {
		return outwardBatchRepo.findAll().stream()
				.filter(item -> item.getProductName().equals(productName) && item.getOfficeName().equals(officeName)
						&& item.getGodownName().equals(godownName))
				.filter(item -> item.getTime().isBefore(LocalDateTime.now().minusMinutes(3)))
				.collect(Collectors.toList());
	}

	private static double roundToTwoDecimalPlaces(double value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
	}

	public Double fetchMrpFromPoNo(String poNo) throws Exception {
		return termsPriceService.fetchTermsByTermsNo(poService.getPoByPoNo(poNo).getTermsPrice().getTermsNo())
				.getB2cPrice().getB2cMrp();
	}

	public String fetchCollectionModeFromPo(String poNo) throws Exception {
		return poService.getPoByPoNo(poNo).getTermsPrice().getB2bTermsAndConditions().getB2bCollectionMode();
	}

	@Override
	public DeliveryChellan getDcDataByDcNo(String dcNo) throws Exception {
		try {
			return deliveryChellanRepo.findByDcNo(dcNo).orElseThrow();
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<DeliveryChellan> getDcDataByClNo(String clNo) throws Exception {
		try {
			return deliveryChellanRepo.findByClNo(clNo);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public List<DespatchAdviseTable> fetchDcData(String despatchAdviceNo) throws Exception {
		try {
			List<DeliveryChellan> deliveryChellanList = deliveryChellanRepo.findByDespatchAdviceNo(despatchAdviceNo);

			return deliveryChellanList.stream().filter(item -> !"Rejected".equals(item.getVoucherStatus()))
					.flatMap(item -> {
						Map<String, Double> productQtyMap = item.getDcTableData().stream()
								.collect(Collectors.groupingBy(DcTableData::getProductName,
										Collectors.summingDouble(DcTableData::getQty)));

						return productQtyMap.entrySet().stream().map(entry -> new DespatchAdviseTable(entry.getKey(),
								item.getDcNo(), null, entry.getValue()));
					}).collect(Collectors.toList());
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public ResponseEntity<String> updateCombinedLoad(List<CompondLoadDcData> obj) throws Exception {
		try {
			String code = codeGenerator.generateClNum(obj.get(0).getVehicleNo());
			obj.forEach(item -> {
				DeliveryChellan deliveryChellan = deliveryChellanRepo.findByDcNo(item.getDcNo()).get();
				deliveryChellan.setClNo(code);
				deliveryChellanRepo.save(deliveryChellan);
			});
			return new ResponseEntity<String>("Updated Successfully!", HttpStatus.ACCEPTED);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Override
	public void saveDc(DeliveryChellan obj) throws Exception {
		try {
			deliveryChellanRepo.save(obj);
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private ClosingStockTableRepo closingStockTableRepo;

	@Override
	public void updateClosingBalance(DeliveryChellan dc) throws Exception {
		try {
			dc.getDcTableData().forEach(item -> {
				ClosingStockTable cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(dc.getOfficeName(),
						item.getProductName(), dc.getDate(), dc.getGodownName());
				if (cb == null) {
					int n = 1;
					while (cb == null) {
						LocalDate date = dc.getDate().minusDays(n++);
						cb = closingStockTableRepo.findByOfficeNameAndProductNameAndDateAndGodownName(dc.getOfficeName(),
								item.getProductName(), date, dc.getGodownName());
						if (date.equals(LocalDate.of(2025, 3, 30))) {
							closingStockTableRepo.save(new ClosingStockTable(null, dc.getOfficeName(), dc.getDate(),
									item.getProductName(), dc.getGodownName(), item.getQty()));
							break;
						}
					}
					if (cb != null) {
						closingStockTableRepo.save(new ClosingStockTable(null, dc.getOfficeName(), dc.getDate(),
								item.getProductName(), dc.getGodownName(), cb.getBalance() - item.getQty()));
					}
				} else {
					cb.setBalance(cb.getBalance() - item.getQty());
					closingStockTableRepo.save(cb);
				}
			});
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
}
