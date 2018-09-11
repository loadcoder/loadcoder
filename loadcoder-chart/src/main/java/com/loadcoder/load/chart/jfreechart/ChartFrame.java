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
package com.loadcoder.load.chart.jfreechart;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.LegendItemEntity;
import org.jfree.chart.entity.PlotEntity;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;

public class ChartFrame extends ApplicationFrame {

	private static final long serialVersionUID = 1L;

	public static Logger log = LoggerFactory.getLogger(ChartFrame.class);

	ChartFrame chartFrame = this;

	private JMenuBar menuBar = new JMenuBar(); // Window menu bar

	/** The chart theme. */
	private static ChartTheme currentTheme = new StandardChartTheme("JFree");
	JFreeChart chart;

	ChartPanel chartPanel;

	XYPlotExtension plot;

	XYLineAndShapeRendererExtention renderer;

	Map<String, Boolean> seriesVisible = new HashMap<String, Boolean>();

	XYSeriesCollectionExtention seriesCollection = new XYSeriesCollectionExtention();

	List<DataSetUser> dataSetUsers = new ArrayList<DataSetUser>();

	JPanel panelForButtons;

	public JFreeChart getChart() {
		return chart;
	}

	public XYPlotExtension getPlot() {
		return plot;
	}

	public XYLineAndShapeRendererExtention getRenderer() {
		return renderer;
	}

	public Map<String, Boolean> getSeriesVisible() {
		return seriesVisible;
	}

	public interface DataSetUser {
		void useDataSet(List<DataSet> dataSets);
	}

	public ChartFrame use(DataSetUser dataSetUser) {
		dataSetUsers.add(dataSetUser);
		return this;
	}

	public static XYPlotExtension createXYPlotExtension(String yAxisLabel, String xAxisLabel, XYDataset dataset,
			XYLineAndShapeRendererExtention renderer) {
		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		XYPlotExtension plot = new XYPlotExtension(dataset, xAxis, yAxis, renderer);

		return plot;
	}

	public ChartFrame(boolean linesVisible, boolean shapesVisible) {
		super("");
		Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/polarbear.png"));
		image.getScaledInstance(200, 200, Image.SCALE_FAST);
		setIconImage(image);
		renderer = new LoadcoderRenderer(linesVisible, shapesVisible, seriesCollection);

		plot = createXYPlotExtension("X", "Y", seriesCollection, renderer);
		plot.setRenderer(renderer);
		plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);

		showChart();

		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
					chartPanel.doCopy();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	XYDataItem xgetDataItem(XYSeriesExtension series, long x) {
		int index = series.indexOf(x);
		XYDataItem existing = (XYDataItem) series.getItems().get(index);
		return existing;
	}

	public XYSeriesCollectionExtention getSeriesCollection() {
		return seriesCollection;
	}

	public int getTotalSize() {
		List l = seriesCollection.getSeries();
		int totalSize = 0;
		for (Object o : l) {
			XYSeriesExtension series = (XYSeriesExtension) o;
			int seriesSize = series.getItemCount();
			totalSize = totalSize + seriesSize;
		}
		return totalSize;
	}

	public static JFreeChart createXYLineChart(String title, PlotOrientation orientation, boolean legend,
			boolean tooltips, boolean urls, XYPlot plot) {

		ParamChecks.nullNotPermitted(orientation, "orientation");

		plot.setOrientation(orientation);
		XYItemRenderer renderer = plot.getRenderer();
		if (tooltips) {
			renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		}
		if (urls) {
			renderer.setURLGenerator(new StandardXYURLGenerator());
		}

		JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
		currentTheme.apply(chart);
		return chart;
	}

	public ChartFrame showChart() {

		panelForButtons = new JPanel();

		panelForButtons.setBackground(Color.WHITE);
		panelForButtons.setLayout(new BoxLayout(panelForButtons, BoxLayout.PAGE_AXIS));

		chart = createXYLineChart(null, PlotOrientation.VERTICAL, true, true, false, plot);

		// using this constructor in order to get rid of jcharts right click popup menu
		chartPanel = new ChartPanelExtension(chart, ChartPanel.DEFAULT_WIDTH, ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED, false, false, false, false, false, false);

		setJMenuBar(menuBar);

		DateAxis dateAxis = new DateAxis();

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateAxis.setDateFormatOverride(format);

		plot.setDomainAxis(dateAxis);

		chartPanel.addChartMouseListener(new ChartMouseListener() {

			public void chartMouseClicked(ChartMouseEvent e) {
				int button = e.getTrigger().getButton();
				Object entity = e.getEntity();
				handleClick(button, entity, seriesCollection);
			}

			public void chartMouseMoved(ChartMouseEvent e) {
			}

		});

		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setZoomAroundAnchor(true);
		panelForButtons.add(chartPanel);

		panelForButtons.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scrolling(e);
				plot.panDomainAxes(30, null, null);
			}
		});

		setContentPane(panelForButtons);

		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setBackgroundPaint(Color.WHITE);

		pack();
		RefineryUtilities.centerFrameOnScreen(this);

		return this;
	}

	void setVisibility(XYSeriesExtension clickedSeries, int iterator, LegendItem legend, boolean visible) {

		if (clickedSeries instanceof XYDottedSeriesExtension) {
			renderer.setSeriesShapesVisible(iterator, visible);
		} else {
			renderer.setSeriesLinesVisible(iterator, visible);
		}
		seriesVisible.put(clickedSeries.getKey(), visible);
		clickedSeries.setVisible(visible);
		// legend.setLineVisible(visible);
		legend.setShapeVisible(visible);
	}

	public void handleClick(int button, Object clickedObject, XYSeriesCollectionExtention serieses) {

		synchronized (plot) {

			if (clickedObject instanceof PlotEntity) {
				chart.setNotify(false);
				if (button == 1) {
				} else {
					chartPanel.restoreAutoBounds();
				}
				chart.setNotify(true);
				serieses.fireChange();
			} else if (clickedObject instanceof LegendItemEntity) {
				chart.setNotify(false);
				LegendItemEntity legendItemEntity = (LegendItemEntity) clickedObject;
				Comparable pushedLegend = legendItemEntity.getSeriesKey();
				List<XYSeriesExtension> lista = serieses.getSeries();
				int iterator = 0;
				XYSeriesExtension clickedSeries = null;

				for (XYSeriesExtension xy : lista) {
					String c = xy.getKey();
					if (pushedLegend.compareTo(c) == 0) {
						clickedSeries = xy;
						break;
					}
					iterator++;
				}

				LegendItem clickedLegend = clickedSeries.getLegend();
				if (button == 1) {
					boolean visible = !clickedSeries.isVisible();
					setVisibility(clickedSeries, iterator, clickedLegend, visible);
				} else {
					int iterator2 = 0;
					for (XYSeriesExtension xy : lista) {
						LegendItem legend = xy.getLegend();

						boolean visible = false;
						if (xy.equals(clickedSeries))
							visible = true;
						setVisibility(xy, iterator2, legend, visible);
						iterator2++;
					}
				}
				chart.setNotify(true);
				serieses.fireChange();
			}
		}
	}

	public void copy() {
		chartPanel.doCopy();
	}

	void addPanel(JPanel resultChartPanel) {
		panelForButtons.add(resultChartPanel);
	}

	public JMenuBar getMenu() {
		return menuBar;
	}

	public void scrolling(MouseWheelEvent e) {

		if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL)
			return;
		if (e.getWheelRotation() < 0)
			increaseZoom(chartPanel, true);
		else
			decreaseZoom(chartPanel, true);
	}

	public void increaseZoom(JComponent chart, boolean saveAction) {
		synchronized (plot) {
			ChartPanel ch = (ChartPanel) chart;
			zoomChartAxis(ch, true);
		}
	}

	public void decreaseZoom(JComponent chart, boolean saveAction) {
		synchronized (plot) {
			ChartPanel ch = (ChartPanel) chart;
			zoomChartAxis(ch, false);
		}
	}

	private void zoomChartAxis(ChartPanel chartP, boolean increase) {
		int width = chartP.getMaximumDrawWidth() - chartP.getMinimumDrawWidth();
		int height = chartP.getMaximumDrawHeight() - chartP.getMinimumDrawWidth();
		if (increase) {
			chartP.zoomInBoth(width / 2, height / 2);
		} else {
			chartP.zoomOutBoth(width / 2, height / 2);
		}
	}

	public void waitUntilClosed() {
		while (isDisplayable())
			LoadUtility.sleep(1000);
	}
}
