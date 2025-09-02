package com.tanfed.inventry.utils;

import org.springframework.stereotype.Component;

@Component
public class SlabRateCalculator {

	public static Double calculateSlabRate(double distance, double[] rates) {
		Double Rate = 0.0;

		int[] rangeLimits = { 7, 20, 50, 75, 100, 125, 150, 175, 200 };
		int limit = 1;
		int prevLimit = 1;
		Double prevRate = 0.0;

		for (int i = 1; i <= (int) distance; i++) {
			if (i <= rangeLimits[0]) {
				Rate = rates[0];
				prevRate = Rate;
			} else {
				if (i <= rangeLimits[prevLimit]) {
					Rate = prevRate + limit * rates[prevLimit];
					limit++;
				} else {
					limit = 1;
					prevLimit++;
					Rate += rates[prevLimit];
					prevRate = Rate;
					if (prevLimit >= rangeLimits.length) {
						prevLimit--;
					}
				}
			}
		}
		return Rate;
	}
}
