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
package com.loadcoder.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadUtility {

	static Logger logger = LoggerFactory.getLogger(LoadUtility.class);

	public static void logExecutionTime(String executionName, long start) {
		long diff = System.currentTimeMillis() - start;
		logger.trace("{} took {} ms", executionName, diff);
	}

	public static String rightpad(String text, int length) {
	    return String.format("%-" + length + "." + length + "s", text);
	}
	
	public static void sleep(long millis){
		try{
			Thread.sleep(millis);
		}catch(Exception e){}
	}
	
	public static List<String> readFile(File file)throws FileNotFoundException, IOException{
		List<String> fileAsLineList = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		       // process the line.
		    	fileAsLineList.add(line);
		    }
		}
		return fileAsLineList;
	}
	
	private static final Random random = new Random();
	public static int random(int min, int max){
		int result = random.nextInt(max - min + 1) + min;
		return result;
	}
	
	public static <T> T tryCatch(TryPerformer<T> tryPerformer, CatchPerformer catchPerformer) {
		try {
			return tryPerformer.perform();
		}catch(Exception e) {
			catchPerformer.perform(e);
		}
		return null;
	}
	
	public static <T> T tryCatchFinally(TryPerformer<T> tryPerformer, CatchPerformer catchPerformer, FinallyPerformer finallyPerform) {
		try {
		return tryPerformer.perform();
		}catch(Exception e) {
			catchPerformer.perform(e);
		}finally {
			finallyPerform.perform();
		}
		return null;
	}
	
	public interface TryPerformer <T>{
		T perform() throws Exception;
	}
	
	public interface FinallyPerformer{
		void perform();
	}
	
	public interface CatchPerformer{
		void perform(Exception cause) throws RuntimeException;
	}
}
