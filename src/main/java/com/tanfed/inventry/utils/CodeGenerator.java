package com.tanfed.inventry.utils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tanfed.inventry.entity.DeliveryChellan;
import com.tanfed.inventry.entity.DespatchAdvice;
import com.tanfed.inventry.entity.GRN;
import com.tanfed.inventry.entity.GTN;
import com.tanfed.inventry.entity.Invoice;
import com.tanfed.inventry.entity.MpaBillEntry;
import com.tanfed.inventry.entity.PoRequest;
import com.tanfed.inventry.entity.PurchaseBooking;
import com.tanfed.inventry.entity.PurchaseOrder;
import com.tanfed.inventry.entity.TcBillEntry;
import com.tanfed.inventry.entity.TermsPrice;
import com.tanfed.inventry.model.Office;
import com.tanfed.inventry.repository.DeliveryChellanRepo;
import com.tanfed.inventry.repository.DespatchAdviceRepo;
import com.tanfed.inventry.repository.GrnRepo;
import com.tanfed.inventry.repository.GtnRepo;
import com.tanfed.inventry.repository.InvoiceRepo;
import com.tanfed.inventry.repository.MpaBillEntryRepo;
import com.tanfed.inventry.repository.PoRequestRepo;
import com.tanfed.inventry.repository.PurchaseBookingRepo;
import com.tanfed.inventry.repository.PurchaseOrderRepo;
import com.tanfed.inventry.repository.TcBillEntryRepo;
import com.tanfed.inventry.repository.TermsPriceRepo;
import com.tanfed.inventry.service.UserService;

@Component
public class CodeGenerator {

	private static final HashMap<String, String> activityAbbreviation = new HashMap<>();

	static {
		activityAbbreviation.put("Fertiliser", "FE");
		activityAbbreviation.put("Agri.Marketing", "AM");
		activityAbbreviation.put("Seeds", "SE");
		activityAbbreviation.put("Pesticides", "PE");
		activityAbbreviation.put("Implements", "IM");
		activityAbbreviation.put("Non Agri.com", "NA");
		activityAbbreviation.put("Others", "OT");
	}

	@Autowired
	private TermsPriceRepo termsPriceRepo;

	public String generateCircularNo(String activity, LocalDate date) throws Exception {
		try {
			int digitCounter = 0;
			int year = date.getYear() % 100;
			int month = date.getMonthValue();
			String nxt2Ltr = activityAbbreviation.get(activity);
			String code;
			Optional<TermsPrice> byReqId;
			do {
				code = String.format("%s%02d%02d%03d", nxt2Ltr, year, month, digitCounter);
				byReqId = termsPriceRepo.findByCircularNo(code);
				digitCounter++;
			} while (byReqId.isPresent());
			return code;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	public String generateTermsNumber(String activity, LocalDate date) throws Exception {
		try {
			String year = String.valueOf(date.getYear()).substring(2); // Extract last two digits
			int random = ThreadLocalRandom.current().nextInt(10000, 100000);

			String nxt2Ltr = activityAbbreviation.get(activity);

			String generatedvalue = year + "-" + nxt2Ltr + "-" + random;
			return generatedvalue;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PoRequestRepo poRequestRepo;

	@Autowired
	private UserService userService;

	public String generatePoRequestNo(String officeName, LocalDate date) throws Exception {
		try {
//				initialize the digit counter sequence
			int digitCounter = 0;

//				fetch three digit code by officeName
			List<Office> officeList = userService.getOfficeList();
			String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
					.map(Office::getCode).collect(Collectors.toList()).get(0);

//				set four digit year and month
			int year = date.getYear() % 100;
			int month = date.getMonthValue();

//				initialize code fetch data
			String code;
			Optional<PoRequest> byReqId;

			// Loop to find a unique ID
			do {
				code = String.format("%s%02d%02d%03d", officeCode, year, month, digitCounter);
				byReqId = poRequestRepo.findByPoReqNo(code);
				digitCounter++; // Increment the counter if ID is not unique
			} while (byReqId.isPresent());

//				return response
			return code;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}

	@Autowired
	private PurchaseOrderRepo purchaseOrderRepo;

	public String GeneratePoNo(String activity, String poBased, LocalDate date) {
		int digitCounter = 0;

		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String firstLtr = null;
		String nxt2Ltr = null;

		if (poBased.equals("Request")) {
			firstLtr = "R";
		} else if (poBased.equals("HoBased")) {
			firstLtr = "H";
		}
		nxt2Ltr = activityAbbreviation.get(activity);

		String code;
		Optional<PurchaseOrder> byPoOrderNo;

		do {
			code = String.format("%s%s%02d%02d%03d", firstLtr, nxt2Ltr, year, month, digitCounter);
			byPoOrderNo = purchaseOrderRepo.findByPoNo(code);
			digitCounter++;
		} while (byPoOrderNo.isPresent());

		return code;
	}

	@Autowired
	private GrnRepo grnRepo;

	public String grnNoGenerator(String officeName, LocalDate date) {
		int digitCounter = 0;
		List<Office> officeList = userService.getOfficeList();
		String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
				.map(Office::getCode).collect(Collectors.toList()).get(0);

		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String code;
		Optional<GRN> byGrnNo;

		do {
			code = String.format("%s%s%02d%02d%03d", "GR", officeCode, year, month, digitCounter);
			byGrnNo = grnRepo.findByGrnNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());

		return code;
	}

	@Autowired
	private GtnRepo gtnRepo;

	public String gtnNoGenerator(String officeName, LocalDate date) {
		int digitCounter = 0;
		List<Office> officeList = userService.getOfficeList();
		String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
				.map(Office::getCode).collect(Collectors.toList()).get(0);

		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String code;
		Optional<GTN> byGrnNo;

		do {
			code = String.format("%s%s%02d%02d%03d", "GT", officeCode, year, month, digitCounter);
			byGrnNo = gtnRepo.findByGtnNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());

		return code;
	}

	@Autowired
	private DespatchAdviceRepo despatchAdviceRepo;

	public String despatchAdviceNoGenerator(String activity, String officeName) {
		int digitCounter = 0;

		List<Office> officeList = userService.getOfficeList();
		String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
				.map(Office::getCode).collect(Collectors.toList()).get(0);

		String nxt2Ltr = activityAbbreviation.get(activity);

		String code;
		Optional<DespatchAdvice> byDespatchAdviceNo;

		do {
			code = String.format("%s%s%05d", officeCode, nxt2Ltr, digitCounter);
			byDespatchAdviceNo = despatchAdviceRepo.findByDespatchAdviceNo(code);
			digitCounter++;
		} while (byDespatchAdviceNo.isPresent());
		return code;
	}

	@Autowired
	private DeliveryChellanRepo deliveryChellanRepo;

	public String dcNoGenerator(String officeName, LocalDate date) {
		int digitCounter = 0;

		List<Office> officeList = userService.getOfficeList();
		String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
				.map(Office::getCode).collect(Collectors.toList()).get(0);

		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String code;
		Optional<DeliveryChellan> byReqId;

		do {
			code = String.format("%s%02d%02d%05d", officeCode, year, month, digitCounter);
			byReqId = deliveryChellanRepo.findByDcNo(code);
			digitCounter++;
		} while (byReqId.isPresent());

		return code;
	}

	@Autowired
	private InvoiceRepo invoiceRepo;

	public String invoiceNoGenerator(String officeName, LocalDate date) {
		int digitCounter = 0;

		List<Office> officeList = userService.getOfficeList();
		String officeCode = officeList.stream().filter(item -> item.getOfficeName().equals(officeName))
				.map(Office::getCode).collect(Collectors.toList()).get(0);

		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String code;
		Optional<Invoice> byGrnNo;

		do {
			code = String.format("%s%02d%02d%s%05d", officeCode, year, month, "IN", digitCounter);
			byGrnNo = invoiceRepo.findByInvoiceNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());

		return code;
	}

	public String icmNoGenerator(String officeName) {
		int digitCounter = 0;
		LocalDate date = LocalDate.now();
		int year = date.getYear() % 100;
		int month = date.getMonthValue();

		String code;
		List<Invoice> byGrnNo;

		do {
			code = String.format("%s%02d%02d%04d", "ICM", year, month, digitCounter);
			byGrnNo = invoiceRepo.findByIcmNo(code);
			digitCounter++;
		} while (!byGrnNo.isEmpty());

		return code;
	}

	@Autowired
	private PurchaseBookingRepo purchaseBookingRepo;

	public String generateCheckMemoNo(String poNo) {
		int digitCounter = 0;

		String code;
		Optional<PurchaseBooking> byGrnNo;

		do {
			code = String.format("%s%s%s%s%02d", "CM", "/", poNo, "/", digitCounter);
			byGrnNo = purchaseBookingRepo.findByCheckMemoNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());

		return code;
	}

	public String generateClNum(String vehicleNo) {
		int digitCounter = 0;
		String code;
		List<DeliveryChellan> dcData;

		do {
			code = String.format("%s%s%02d", vehicleNo, "/", digitCounter);
			dcData = deliveryChellanRepo.findByClNo(code);
			digitCounter++;
		} while (!dcData.isEmpty());
		return code;

	}

	@Autowired
	private MpaBillEntryRepo mpaBillEntryRepo;

	public String generateCheckMemoNoMpa(String claimBillNo) {
		int digitCounter = 0;
		String code;
		Optional<MpaBillEntry> byGrnNo;
		do {
			code = String.format("%s%s%s%s%02d", "CM/MPA", "/", claimBillNo, "/", digitCounter);
			byGrnNo = mpaBillEntryRepo.findByCheckMemoNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());
		return code;
	}

	@Autowired
	private TcBillEntryRepo tcBillEntryRepo;

	public String generateCheckMemoNoTc(String claimBillNo) {
		int digitCounter = 0;
		String code;
		Optional<TcBillEntry> byGrnNo;
		do {
			code = String.format("%s%s%s%s%02d", "CM/TC", "/", claimBillNo, "/", digitCounter);
			byGrnNo = tcBillEntryRepo.findByCheckMemoNo(code);
			digitCounter++;
		} while (byGrnNo.isPresent());
		return code;
	}

	public String generateOpeningStockId() {
		int random = ThreadLocalRandom.current().nextInt(10000, 100000);
		return String.format("%s%s", "OB", random);
	}
}
