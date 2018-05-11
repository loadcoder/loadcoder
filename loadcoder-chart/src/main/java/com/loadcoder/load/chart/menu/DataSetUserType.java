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
package com.loadcoder.load.chart.menu;

import com.loadcoder.load.chart.jfreechart.ChartFrame.DataSetUser;
import com.loadcoder.load.chart.logic.ResultChartLogic;

public class DataSetUserType {
	
	public static final DataSetUserType PERCENTREMOVALFILTER = new DataSetUserType("Highest 5%", ResultChartLogic.removePercentile(5));
	public static final DataSetUserType FAILSREMOVALFILTER = new DataSetUserType("Fails%", ResultChartLogic.removeFails());

	private String name;
	
	private DataSetUser dataSetUser;
	
	public DataSetUserType(String name, DataSetUser dataSetUser) {
		super();
		this.name = name;
		this.dataSetUser = dataSetUser;
	}

	public String getName() {
		return name;
	}

	public DataSetUser getDataSetUser() {
		return dataSetUser;
	}

}
