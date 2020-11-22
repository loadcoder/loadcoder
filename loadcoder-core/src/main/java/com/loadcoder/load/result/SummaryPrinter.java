/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.load.result;

import java.util.HashMap;
import java.util.Map;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.result.Summary.DoubleToStringConvert;
import com.loadcoder.load.result.Summary.UserDefinedConverters;

public class SummaryPrinter {

	private static Map<String, Map<String, String>> valueMapToStringMap(Map<String, Map<String, ValueHolder>> valueMap,
			Map<String, DoubleToStringConvert> valueConverters, DoubleToStringConvert defaultConverter) {
		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

		valueMap.entrySet().stream().forEach(entry -> {

			Map<String, String> res = new HashMap<String, String>();
			result.put(entry.getKey(), res);
			entry.getValue().entrySet().stream().forEach(entry2 -> {

				DoubleToStringConvert converter = valueConverters.get(entry2.getKey());
				if (converter == null) {
					converter = defaultConverter;
					if (converter == null) {
						converter = entry2.getValue().getConverter();
					}
				}

				String stringValue = converter.convert(entry2.getValue());
				result.get(entry.getKey()).put(entry2.getKey(), stringValue);
			});
		});

		return result;
	}

	public static String tableAsString(Summary summary, UserDefinedConverters userDefinedConverters) {

		StringBuilder resultBuilder = new StringBuilder();
		resultBuilder.append("---Load test summary---\n");
		Map<String, DoubleToStringConvert> valueConverters = userDefinedConverters.getMap();

		summary.getOverAllSummaryList().stream().forEach(entry -> {

			StringBuilder stringBuilderHead = new StringBuilder();
			stringBuilderHead.append(entry.getName());
			stringBuilderHead.append(": ");

			DoubleToStringConvert valueHolderConverter = entry.getValue().getConverter();
			DoubleToStringConvert converter = valueConverters.get(entry.getName());
			if (converter == null) {
				converter = userDefinedConverters.getDefaultConverter();
				if (converter == null) {
					converter = valueHolderConverter;
				}
			}

			String valueToUse = converter.convert(entry.getValue());
			stringBuilderHead.append(valueToUse);
			resultBuilder.append(stringBuilderHead.toString() + "\n");
		});

		if(summary.getTransactionsSummaryValueHolderList() != null) {
			StringBuilder tableStringBuilder = doTable(summary, userDefinedConverters);
			resultBuilder.append(tableStringBuilder.toString());
		}
		
		String result = resultBuilder.toString();
		return result;
	}
	
	public static StringBuilder doTable(Summary summary, UserDefinedConverters userDefinedConverters) {
		StringBuilder resultBuilder = new StringBuilder();
		Map<String, DoubleToStringConvert> valueConverters = userDefinedConverters.getMap();
		Map<String, Integer> maxWidthPerColumn = new HashMap<String, Integer>();
		String transactionsColumnName = "Transactions";

		summary.getTransactionsSummaryValueHolderList().stream().forEach(a -> {
			maxWidthPerColumn.put(a.getName(), a.getName().length());
		});

		maxWidthPerColumn.put(transactionsColumnName, transactionsColumnName.length());
		summary.getTransactionsSummary().entrySet().stream().forEach(entry -> {
			int valueLength = entry.getKey().length();
			if (maxWidthPerColumn.get(transactionsColumnName) < valueLength) {
				maxWidthPerColumn.put(transactionsColumnName, valueLength);
			}
		});

		Map<String, Map<String, String>> valueMap = valueMapToStringMap(summary.getTransactionsSummary(),
				valueConverters, userDefinedConverters.getDefaultConverter());

		Map<String, ValueHolder> totalTransactions = summary.getAllTransactionsSummary();

		Map<String, Map<String, ValueHolder>> totalTransactions2 = new HashMap<String, Map<String, ValueHolder>>();
		totalTransactions2.put("TOTAL", totalTransactions);
		Map<String, Map<String, String>> valueMapTotalTransactions = valueMapToStringMap(totalTransactions2,
				valueConverters, userDefinedConverters.getDefaultConverter());

		String temporaryKey = "";
		for (int i = 0; i < 100000; i++) {
			if (valueMap.get("" + i) == null) {
				temporaryKey = "" + i;
				break;
			}
		}
		valueMap.put(temporaryKey, valueMapTotalTransactions.get("TOTAL"));

		valueMap.entrySet().stream().forEach(entry -> {
			entry.getValue().entrySet().stream().forEach(entry2 -> {

				int valueLength = entry2.getValue().length();
				if (maxWidthPerColumn.get(entry2.getKey()) < valueLength) {
					maxWidthPerColumn.put(entry2.getKey(), valueLength);
				}
			});
		});
		valueMap.remove(temporaryKey);

		final int columnDistance = 2;
		StringBuilder stringBuilderHead = new StringBuilder();
		int maxWidthHead = maxWidthPerColumn.get(transactionsColumnName);
		String paddedValueTransactionHead = LoadUtility.rightpad(transactionsColumnName, maxWidthHead + columnDistance);
		stringBuilderHead.append(paddedValueTransactionHead);
		summary.getTransactionsSummaryValueHolderList().stream().forEach(entry -> {
			int maxWidth = maxWidthPerColumn.get(entry.getName());
			String paddedValueTransaction = LoadUtility.rightpad(entry.getName(), maxWidth + columnDistance);
			stringBuilderHead.append(paddedValueTransaction);
		});
		resultBuilder.append(stringBuilderHead.toString() + "\n");

		valueMap.entrySet().stream().forEach(entry -> {
			StringBuilder stringBuilder = new StringBuilder();

			int maxWidthTransaction = maxWidthPerColumn.get(transactionsColumnName);
			String paddedValueTransaction = LoadUtility.rightpad(entry.getKey(), maxWidthTransaction + columnDistance);
			stringBuilder.append(paddedValueTransaction);

			summary.getTransactionsSummaryValueHolderList().stream().forEach(entry2 -> {
				String valueToPrint = entry.getValue().get(entry2.getName());

				int maxWidth = maxWidthPerColumn.get(entry2.getName());
				String paddedValue = LoadUtility.rightpad(valueToPrint, maxWidth + columnDistance);
				stringBuilder.append(paddedValue);
			});
			String line = stringBuilder.toString();
			resultBuilder.append(line + "\n");
		});

		valueMapTotalTransactions.entrySet().stream().forEach(entry -> {
			StringBuilder stringBuilder = new StringBuilder();

			int maxWidthTransaction = maxWidthPerColumn.get(transactionsColumnName);
			String paddedValueTransaction = LoadUtility.rightpad(entry.getKey(), maxWidthTransaction + columnDistance);
			stringBuilder.append(paddedValueTransaction);

			summary.getTransactionsSummaryValueHolderList().stream().forEach(entry2 -> {
				String valueToPrint = entry.getValue().get(entry2.getName());

				int maxWidth = maxWidthPerColumn.get(entry2.getName());
				String paddedValue = LoadUtility.rightpad(valueToPrint, maxWidth + columnDistance);
				stringBuilder.append(paddedValue);
			});
			String line = stringBuilder.toString();
			resultBuilder.append(line + "\n");
		});
		return resultBuilder;
	}
}
