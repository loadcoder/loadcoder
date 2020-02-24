/*******************************************************************************
 * Copyright (C) 2018 Stefan Vahlgren at Loadcoder
 * 
 * This file is part of Loadcoder.
 * 
 * Loadcoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Loadcoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.loadcoder.load.chart.logic;

import static com.loadcoder.statics.Statics.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.chart.sampling.SampleConcaternator;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeChartLogicTest extends TestNGBase {

	XYPlotExtension plot;
	RuntimeChartLogic logic;

	Map<String, List<TransactionExecutionResult>> getNewListsOfLists(TransactionExecutionResult... points) {
		Map<String, List<TransactionExecutionResult>> listOfListOfList = new HashMap<String, List<TransactionExecutionResult>>();
		for (TransactionExecutionResult point : points) {
			List<TransactionExecutionResult> transactions = listOfListOfList.get(point.getName());
			if (transactions == null) {
				transactions = new ArrayList<TransactionExecutionResult>();
				listOfListOfList.put(point.getName(), transactions);
			}
			transactions.add(point);
		}
		return listOfListOfList;
	}

	@BeforeMethod
	public void setup() {

		logic = new RuntimeChartLogic(CommonSeries.values(), false);
	}

	@Test
	public void testRuntimeChart() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";
		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 1, true, null));
		HashSet<Long> hashesGettingUpdated = new HashSet<Long>();
		logic.update(listOfListOfList, hashesGettingUpdated);

		Map<String, XYSeriesExtension> commonSerieses = logic.getCommonSeriesMap();
		assertEquals(commonSerieses.size(), 2);

		for (XYSeriesExtension commonSeries : commonSerieses.values()) {
			assertTrue(commonSeries.isVisible());
			if (commonSeries.getKey().equals(CommonSeries.THROUGHPUT.getName())) {
			}
		}

		SampleGroup sampleGroup = logic.getSampleGroups().get(transactionKey);

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs + 1200, 2, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());
		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs + 2200, 3, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());
		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs + 3200, 4, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		logic.addNewConcater(2, (a) -> {
			return true;
		});

		SampleConcaternator concatter = logic.sampleConcaternatorList.get(0);
		logic.concatAndAdjustRanges(concatter, new HashSet<Long>());

		Range range = logic.getRanges().lookupCorrectRange(0);
		assertEquals(2000, range.getSampleLength());
		Sample a = sampleGroup.getExistingSample(0, 2000);
		Sample b = sampleGroup.getExistingSample(1999, 2000);
		assertEquals(a, b); // assert concaternation
	}

	@Test
	public void testSurroundingTimestamps() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";

		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 10, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs + 3000, 10, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		// verify that the surroundingTimestamps functionality are working as expected
		Map<String, XYSeriesExtension> commonSerieses = logic.getCommonSeriesMap();
		for (XYSeriesExtension commonSeries : commonSerieses.values()) {
			if (commonSeries.getKey().equals(CommonSeries.THROUGHPUT.getName())) {
				List<XYDataItem> items = commonSeries.getItems();

				assertEquals(items.size(), 4);
				assertEquals(items.get(0).getY(), 1.0D);
				assertEquals(items.get(1).getY(), 0.0D);
				assertEquals(items.get(2).getY(), 0.0D);
				assertEquals(items.get(3).getY(), 1.0D);
			}
		}
	}

	@Test
	public void testRuntimeChartOneTransaction() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";

		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 10, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		logic.getRanges().lookupCorrectRange(-1);
		logic.getRanges().lookupCorrectRange(1);

		SampleGroup sampleGroup = logic.getSampleGroups().get(transactionKey);

		logic.addNewConcater(2, (a) -> {
			return true;
		});
		SampleConcaternator concatter = logic.sampleConcaternatorList.get(0);
		logic.concatAndAdjustRanges(concatter, new HashSet<Long>());

		Range range3 = logic.getRanges().lookupCorrectRange(-1);
		assertEquals(1000, range3.getSampleLength());

		Sample a = sampleGroup.getExistingSample(0, 2000);
		Sample b = sampleGroup.getExistingSample(1999, 2000);

		assertEquals(a, b); // assert concaternation
		assertEquals(10, b.getY());
	}

	@Test
	public void testMinusXValue() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";

		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 10, true, null));
		logic.setIncomingData(listOfListOfList);
		logic.performUpdate();

		SampleGroup sampleGroup = logic.getSampleGroups().get(transactionKey);

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs - 1, 8, true, null),
				new TransactionExecutionResult(transactionKey, startTs + 10 * SECOND, 15, true, null),
				new TransactionExecutionResult(transactionKey, startTs + 3 * MINUTE, 8, true, null));

		logic.setIncomingData(listOfListOfList);
		logic.performUpdate();

		Sample first2 = sampleGroup.getExistingSample(0, 1000);
		Sample minusSample = sampleGroup.getExistingSample(-1, 1000);
		System.out.println("done");

		listOfListOfList = getNewListsOfLists();

		logic.setIncomingData(listOfListOfList);
		logic.performUpdate();

		Range r = logic.getRanges().lookupCorrectRange(10000);
		long concatenatedSampleLength = r.getSampleLength();
		assertEquals(concatenatedSampleLength, 4 * SECOND);
		Sample concatenatedSample = sampleGroup.getExistingSample(10000, concatenatedSampleLength);
		long concatenatedYValue = concatenatedSample.getY();
		assertEquals(concatenatedYValue, 15);
	}

	/**
	 * Tests that the runtime functionality works even though all transaction types
	 * are'nt represented in all batches of incoming data. Test will fail if
	 * exception is thrown
	 */
	@Test
	public void testDifferntAmountsAndTypesOfTransactions() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";

		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 10, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey + "2", startTs, 10, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

	}

	@Test
	public void testRuntimeChartUnorderedData() {
		long startTs = System.currentTimeMillis();
		String transactionKey = "foo";

		Map<String, List<TransactionExecutionResult>> listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs, 1, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		SampleGroup sampleGroup = logic.getSampleGroups().get(transactionKey);

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs - 500, 2, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs - 1500, 3, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());

		listOfListOfList = getNewListsOfLists(
				new TransactionExecutionResult(transactionKey, startTs - 2500, 4, true, null));
		logic.update(listOfListOfList, new HashSet<Long>());
		logic.addNewConcater(2, (a) -> {
			return true;
		});
		SampleConcaternator concatter = logic.sampleConcaternatorList.get(0);
		logic.concatAndAdjustRanges(concatter, new HashSet<Long>());

		Range range = logic.getRanges().lookupCorrectRange(-1);
		assertEquals(1000, range.getSampleLength());

		Sample a = sampleGroup.getExistingSample(0, 1000);
		Sample b = sampleGroup.getExistingSample(999, 1000);

		assertEquals(a, b); // assert concaternation
		assertEquals(1, b.getY());

		Sample shouldNotHaveBeenConcatedA = sampleGroup.getExistingSample(-3000, 1000);
		Sample shouldNotHaveBeenConcatedB = sampleGroup.getExistingSample(-1001, 1000);
		assertFalse(shouldNotHaveBeenConcatedA.equals(shouldNotHaveBeenConcatedB));
	}

	@Test
	public void testRuntimeLogic() {

		Map<String, List<TransactionExecutionResult>> map = new HashMap<String, List<TransactionExecutionResult>>();

		long lastMillisAfterStartWithinFirstSampleSecond = 999;
		long start = System.currentTimeMillis();
		map.put("a", new ArrayList<TransactionExecutionResult>());
		map.get("a").add(new TransactionExecutionResult(start, 10, true, null));
		map.get("a").add(
				new TransactionExecutionResult(start + lastMillisAfterStartWithinFirstSampleSecond, 20, true, null));

		long lastMillisAfterStartWithinFirstConcatenatedSample = 3999;
		map.get("a").add(new TransactionExecutionResult(start + lastMillisAfterStartWithinFirstConcatenatedSample, 60,
				true, null));
		long longEnoughAfterStartInOrderForTheFirstConcatenatorToBeStarted = 3 * MINUTE;

		map.get("a").add(new TransactionExecutionResult(
				start + longEnoughAfterStartInOrderForTheFirstConcatenatorToBeStarted, 40, true, null));

		logic.useData(map);
		XYSeries throughput = logic.getSeriesCollection().getSeries(0);
		XYSeries fails = logic.getSeriesCollection().getSeries(1);
		XYSeriesExtension c = (XYSeriesExtension) logic.getSeriesCollection().getSeries(2);
		XYDataItem firstPoint = c.getDataItem(0);
		assertEquals(firstPoint.getY().longValue(), 15L);

		XYDataItem firstThroughputPoint = throughput.getDataItem(0);
		assertEquals(firstThroughputPoint.getY().longValue(), 2L);

		map.get("a").clear();

		logic.useData(map);
		map.get("a").add(new TransactionExecutionResult(start + lastMillisAfterStartWithinFirstConcatenatedSample + 2,
				40, true, null));

		XYDataItem concatenatedThroughputPoint = throughput.getDataItem(0);
		assertEquals(concatenatedThroughputPoint.getY().longValue(), 1L);

		XYDataItem concatenatedPoint = c.getDataItem(0);
		assertEquals(concatenatedPoint.getY().longValue(), 30L);

	}

}
