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
package com.loadcoder.load;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.testng.Assert;

public class TestUtility {

	public static List<String> readFile(File file){
		try {
			return LoadUtility.readFile(file);
		}catch(IOException ioe) {
			Assert.fail("Could not read file" + file, ioe);
			return null; //will never reach this return
		}
	}
	
	public static synchronized void addNewObjectToList(List<Object> list){
		list.add(new Object());
	}
	
	public static synchronized <T> void  addValueToList(List<T> list, T value){
		list.add(value);
	}
	
	public static int random(int min, int max){
		Random rn = new Random();
		int result = rn.nextInt(max - min + 1) + min;
		return result;
	}
}
