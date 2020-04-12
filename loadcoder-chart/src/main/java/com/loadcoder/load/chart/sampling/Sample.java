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
package com.loadcoder.load.chart.sampling;

import java.util.ArrayList;
import java.util.List;

import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;

public class Sample extends SampleBase{

	private boolean locked = true;
	
	long pointsSum =0;
	
	long pointsAmount =0;
	
	boolean empty = true;

	int amountOfFails =0;
	
	String seriesName;

	private List<Point> points = new ArrayList<Point>();

	public Sample(long startTs, long length, String seriesName, boolean locked){
		this(startTs, length, seriesName);
		this.locked = locked;
	}
	
	public Sample(long startTs, long length, String seriesName){
		this.firstTs = startTs;
		this.lastTs = startTs + length -1;
		this.length = length;
		this.seriesName = seriesName;
	}
	
	public String toString(){
		return "{" + firstTs + " - " + lastTs  + ", y:" + y + "}";
	}

	public int getAmountOfFails() {
		return amountOfFails;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPointsSum(long pointSum) {
		this.pointsSum = pointSum;
	}
	
	public void setPointsAmount(long pointsAmount) {
		this.pointsAmount = pointsAmount;
	}
	
	public void setPoints(List<Point> points){
		if(locked) {
			this.pointsAmount = points.size();
		}else {
			this.points = points;
		}
	}

	public boolean isEmpty() {
		if(locked) {
			return pointsAmount == 0;
		}else {
			return points.isEmpty();
		}
	}
	
	public List<Point> scrapThisSampleAndGetPoints(){
		List<Point> toBeReturned = points;
		points = null;
		pointsSum =-1;
		pointsAmount =-1;
		return toBeReturned;
	}
	
	public void increaseFails(){
		amountOfFails++;
	}
	
	public void setFails(int amountOfFails){
		this.amountOfFails = amountOfFails;
	}

	public void setFirst(XYDataItemExtension first) {
		this.first = first;
	}

	public long getPointsSum() {
		return pointsSum;
	}
	
	public long getAmountOfPoints(){
		if(locked) {
			return pointsAmount;
		}else {
			return points.size();
		}
	}
	
	public void addPoint(Point point){
		if(locked) {
			pointsSum = pointsSum + point.getY();
			pointsAmount++;
		}else {
			points.add(point);
		}
	}
	
	public static long amountToYValue(double amount){
		long longAmount = (long)amount;
		if(amount > longAmount)
			longAmount = longAmount +1;
		return longAmount;
	}
	
	public static double avg(long sum, long amount) {
		double avg = (double)sum / amount;
		return avg;
	}
	
	public void calculateY(YCalculator calc){
		double calculated;
		if(locked) {
			calculated = avg(pointsSum, pointsAmount);
		}else {
			calculated = calc.calculateY(points);
		}
		
		this.y = Math.round(calculated);
	}
	
	public long getY(){
		return y;
	}
	
}
