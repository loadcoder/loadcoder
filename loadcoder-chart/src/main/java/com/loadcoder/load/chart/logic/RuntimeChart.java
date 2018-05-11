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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.measure.TransactionExecutionResult;
import com.loadcoder.load.scenario.RuntimeDataUser;
import com.loadcoder.load.scenario.StartedLoad;

public class RuntimeChart extends Chart implements RuntimeDataUser{

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	RuntimeChartLogic logic;
	
	CommonSeries[] commonSeries;
	
	StartedLoad startedScenarios;
	
	public RuntimeChart() {
		this(CommonSeries.values());
	}
	
	public RuntimeChart(CommonSeries[] commonSeries) {
		super(true, false);
		this.commonSeries = commonSeries;
		logic = new RuntimeChartLogic(
				chartFrame.getSeriesCollection(),
				chartFrame.getPlot(),
				chartFrame.getRenderer(),
				chartFrame.getSeriesVisible(),
				commonSeries,
				startedScenarios,
				true
				);
		chartFrame.setVisible(true);
	}
	
	@Override
	public void useData(List<List<TransactionExecutionResult>> listOfListOfList) {
		logic.setIncomingData(listOfListOfList);
		chartFrame.getChart().setNotify(false);
		long start = System.currentTimeMillis();
		logic.doSafeUpdate();
		long diff = System.currentTimeMillis() - start;
		logger.debug("update time: {}", diff);
		logger.trace("Total Points in chart: {}", chartFrame.getTotalSize());
		chartFrame.getChart().setNotify(true);
	}
	
}
