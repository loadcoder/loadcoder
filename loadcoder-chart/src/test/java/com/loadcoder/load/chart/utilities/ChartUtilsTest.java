package com.loadcoder.load.chart.utilities;

import org.testng.annotations.Test;

public class ChartUtilsTest {

	@Test
	public void calculateSteppingTest() {
		
		ChartUtils.calculateStepping(1);
		
		ChartUtils.calculateStepping(0.1);
		
		ChartUtils.calculateStepping(0.001);
		
	}
}
