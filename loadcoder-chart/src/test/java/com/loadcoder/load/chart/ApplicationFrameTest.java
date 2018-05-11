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
package com.loadcoder.load.chart;

import java.awt.Color;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.loadcoder.load.chart.jfreechart.XYDataItemExtension;

public class ApplicationFrameTest extends ApplicationFrame{

	private static final long serialVersionUID = 1L;

	XYSeriesCollection data;
	long firstTs =0;

	long minutIterator =1;

	XYDataItemExtension test;

	XYSeries firstSeries;
	XYDataItemExtension first;
	boolean addFirst;
	
	JFreeChart chart;
	ChartPanel chartPanel;
	XYPlot plot;
	XYLineAndShapeRenderer renderer;
	
	static class TimeWindow{
		public long getStart() {
			return start;
		}
		public long getEnd() {
			return end;
		}
		long start;
		long end;

		TimeWindow(long start, long end){
			this.start = start;
			this.end = end;
		}
		boolean isTimestampWithinThisWindow(long timestamp){
			if(timestamp > start && timestamp < end)
				return true;
			return false;
		}
	}

	public ApplicationFrameTest(){
		super("");
	}

	XYDataItem xgetDataItem(XYSeries series, long x){
		int index = series.indexOf(x);
		XYDataItem existing = (XYDataItem) series.getItems().get(index);
		return existing;
	}

	public ApplicationFrameTest showChart(){
		
		data = new XYSeriesCollection();

		XYSeries xySeries = new XYSeries("Throughput (TPS)", true, false);
		data.addSeries(xySeries);

		Thread t = new Thread(()->{
			XYSeries series3 = new XYSeries("autoupdate", true, false);
			data.addSeries(series3);
			XYDataItem item = null;
			XYDataItem item2 = null;
			int i =0;
			minutIterator =10;
			while(true){
				long ts = System.currentTimeMillis();
				XYDataItemExtension itemToAdd = new XYDataItemExtension(ts, minutIterator++);
				series3.add(itemToAdd, true);
				i++;
				if(i==5){
					item = itemToAdd;
				}
				if(i==6){
					item2 = itemToAdd;
				}
				
				if(item2 != null){
					item.setY(minutIterator+2);
					item2.setY(minutIterator+2);
					
					series3.addOrUpdate(item);
					series3.addOrUpdate(item2);

				}
				try{Thread.sleep(500);}catch(Exception e){}
			}
		});
						t.start();
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		chart = ChartFactory.createXYLineChart(
				null,
				"X", 
				"Y", 
				data,
				PlotOrientation.VERTICAL,
				true,
				true,
				false
				);

		chartPanel = new ChartPanel(chart);

		plot = (XYPlot)chart.getPlot();
		
		DateAxis dateAxis = new DateAxis();
		
		dateAxis.setDateFormatOverride(new SimpleDateFormat("dd-MM-yyyy")); 
		plot.setDomainAxis(dateAxis);
		
		renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		chartPanel.addChartMouseListener(new ChartMouseListener() {

			public void chartMouseClicked(ChartMouseEvent e) {
				Object entity = e.getEntity();
				if(entity instanceof LegendItemEntity){
					LegendItemEntity legendItemEntity = (LegendItemEntity)entity;
					Comparable pushedLegend = legendItemEntity.getSeriesKey();
					List<XYSeries> lista = data.getSeries();
					int iterator =0;
					for(XYSeries xy : lista){	

						Comparable c = xy.getKey();
						if(pushedLegend.compareTo(c) ==0){
							break;
						}
						iterator++;
					}
					boolean currentVisibility = true;
					try{
						//invert the visibility
						currentVisibility = renderer.getSeriesLinesVisible(iterator) ? false : true;
					}catch(NullPointerException exc){
						//		        		System.out.println(exc);
						currentVisibility = false;
					}
					renderer.setSeriesLinesVisible(iterator, currentVisibility);

				}
			}

			public void chartMouseMoved(ChartMouseEvent e) {}

		});
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));

		JButton button2 = new JButton("Save in Result Dir");
		button2.addActionListener((a)->{
			renderer.setSeriesVisibleInLegend(0, true, true);
			renderer.setSeriesVisibleInLegend(1, true, true);
		});

		buttonPanel.add(button2);

		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setZoomAroundAnchor(true);
		panel.add(chartPanel);
		panel.add(buttonPanel);

		panel.addMouseWheelListener(new MouseWheelListener(){

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				scrolling(e);
				plot.panDomainAxes(30, null, null);
			}
		});
		setContentPane(panel);

		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setBackgroundPaint(Color.WHITE);

		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
		return this;
	}

    public void scrolling(MouseWheelEvent e) {
    	System.out.println("x:" +e.getX() + " y:" +e.getY());
    	
        if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL) return;
        if (e.getWheelRotation()< 0) increaseZoom(chartPanel, true);
        else                          decreaseZoom(chartPanel, true);
    }
    
    public synchronized void increaseZoom(JComponent chart, boolean saveAction){
        ChartPanel ch = (ChartPanel)chart;
        zoomChartAxis(ch, true);
    }  
    
    public synchronized void decreaseZoom(JComponent chart, boolean saveAction){
        ChartPanel ch = (ChartPanel)chart;
        zoomChartAxis(ch, false);
    }  
    
    private void zoomChartAxis(ChartPanel chartP, boolean increase){              
        int width = chartP.getMaximumDrawWidth() - chartP.getMinimumDrawWidth();
        int height = chartP.getMaximumDrawHeight() - chartP.getMinimumDrawWidth();        
        if(increase){
           chartP.zoomInBoth(width/2, height/2);
        }else{
           chartP.zoomOutBoth(width/2, height/2);
        }
    }
    
	public void waitUntilClosed(){
		while(isDisplayable())
			try{Thread.sleep(1000);}catch(Exception e){}
	}
	
	public static void main(String[] args){
		ApplicationFrameTest f = new ApplicationFrameTest();
		f.showChart();
		f.waitUntilClosed();
	}
	
}
