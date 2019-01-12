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
package com.loadcoder.load.chart.sampling;

import org.testng.Assert;
import org.testng.annotations.Test;

public class LockingLogic {

	double[] getArray(int amount, long value) {
		double[] arr = new double[amount];
		for (int i = 0; i < amount; i++) {
			arr[i] = value;
		}
		return arr;
	}

	double size(double[] array) {
		double size=0;
		for(int i =0;i<array.length; i++) {
			size = size + array[i];
		}
		return size;
	}
	
	double avg(double[] array) {
			double size = size(array);
			double avg = ((double)size) / array.length; 
			return avg;
	}
	
	public double[] concatIntoOneArray(double[] arr1, double[] arr2) {
		double[] a = new double[arr1.length + arr2.length];
		
		for(int i=0; i<arr1.length;i++) {
			a[i] = arr1[i];
		}
		for(int i=0; i<arr2.length;i++) {
			a[arr1.length + i] = arr2[i];
		}
		return a;
	}
	
	public void printArray(double[] arr) {
		String s = "";
		for(int i=0; i<arr.length;i++) {
			s = s + arr[i] +" ";
		}
		System.out.println(String.format("{%s}", s));
	}
	
	public double avgFor2ArrayMetadata(double avg1, int amount1, double avg2, int amount2) {
		double avg = (avg1 + avg2) / ( amount1 + amount2); 
		return avg;
	}
	
	@Test
	public void testAvgFor2Arrays() {
		double[] arr1 = getArray(3, 13);
		double sum1 = size(arr1);
		double[] arr2 = getArray(4, 18);
		double sum2 = size(arr2);
		double[] arr3 = concatIntoOneArray(arr1, arr2);
		printArray(arr3);
		
		double avg1 = avg(arr1);
		System.out.println(avg1);
		double avg2 = avg(arr2);
		double avg3 = avg(arr3);
		System.out.println("avg3: " +avg3);
			
		double avg4 = avgFor2ArrayMetadata(sum1, arr1.length, sum2, arr2.length);
		System.out.println("avg4: " +avg4);
		
		Assert.assertEquals(avg3, avg4);
		
		System.out.println(Long.MAX_VALUE);
		System.out.println(100_000_000_000L *1000L);

	}
}
