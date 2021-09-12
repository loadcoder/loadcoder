/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
package com.loadcoder;

import java.util.List;

import com.loadcoder.load.LoadUtility;

public class DataStore<T> {

	final private List<T> dataList;

	public DataStore(List<T> dataList) {
		this.dataList = dataList;
	}

	protected List<T> getDataList() {
		return dataList;
	}
	
	public T remove() {
		synchronized (dataList) {
			return dataList.remove(0);
		}
	}

	public T remove(int index) {
		synchronized (dataList) {
			return dataList.remove(index);
		}
	}

	public T removeRandom() {
		synchronized (dataList) {
			int index = LoadUtility.random(0, dataList.size() - 1);
			T data = dataList.remove(index);
			return data;
		}
	}

	public T get() {
		synchronized (dataList) {
			return dataList.get(0);
		}
	}

	public T get(int index) {
		synchronized (dataList) {
			return dataList.get(index);
		}
	}

	public T getRandom() {
		synchronized (dataList) {
			int index = LoadUtility.random(0, dataList.size() - 1);
			T data = dataList.get(index);
			return data;
		}
	}
	
}


