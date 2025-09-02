package com.tanfed.inventry.response;

import java.util.List;

import com.tanfed.inventry.entity.MpaBillEntry;
import com.tanfed.inventry.entity.MpaCheckMemo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataForMpaCheckMemo {

	private List<String> checkMemoNoList;
	private MpaBillEntry mpaBillEntry;
	private Double totalCalculatedValue;
	private Double totalSgstValue;
	private Double totalCgstValue;
	private Double totalPaymentValue;
	private List<MpaCheckMemo> cmData;
}
