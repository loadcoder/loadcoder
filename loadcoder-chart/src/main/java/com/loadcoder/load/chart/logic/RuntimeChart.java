/*******************************************************************************
 * Copyright (C) 2018 Team Loadcoder
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
import java.util.Map;

import javax.swing.JMenu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.scenario.RuntimeResultUser;
import com.loadcoder.load.scenario.StartedLoad;
import com.loadcoder.result.Result;
import com.loadcoder.result.TransactionExecutionResult;

public class RuntimeChart extends Chart implements RuntimeResultUser {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	StartedLoad startedScenarios;

	public RuntimeChart() {
		this(CommonSeries.values());
	}

	public static RuntimeChartLogic createNewRuntimeChartLogic() {
		return new RuntimeChartLogic(CommonSeries.values(), true);
	}

	private final RuntimeChartLogic runtimeChartLogic;

	protected RuntimeChartLogic getLogic() {
		return runtimeChartLogic;
	}
	
	public RuntimeChart(CommonSeries[] commonSeries) {
		super(true, false, new RuntimeChartLogic(commonSeries, true));
		runtimeChartLogic = (RuntimeChartLogic) this.logic;

		JMenu settingsMenu = createSettingsMenu(logic);
		JMenu aboutMenu = createAboutMenu();

		chartFrame.setContentPane(runtimeChartLogic.panelForButtons);
		chartFrame.pack();
		chartFrame.setJMenuBar(runtimeChartLogic.getMenu());
		runtimeChartLogic.getMenu().add(settingsMenu);
		runtimeChartLogic.getMenu().add(aboutMenu);

		chartFrame.setVisible(true);
	}

	@Override
	public void useData(Map<String, List<TransactionExecutionResult>> transactionsMap) {
		runtimeChartLogic.useData(transactionsMap);
	}

}
