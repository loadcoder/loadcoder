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

import static com.loadcoder.load.chart.common.YCalculator.avg;
import static com.loadcoder.load.chart.common.YCalculator.max;
import static com.loadcoder.statics.Time.HOUR;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.jfree.data.xy.XYDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.chart.common.CommonSample;
import com.loadcoder.load.chart.common.CommonSampleGroup;
import com.loadcoder.load.chart.common.CommonSeries;
import com.loadcoder.load.chart.common.CommonSeriesCalculator;
import com.loadcoder.load.chart.common.CommonYCalculator;
import com.loadcoder.load.chart.common.YCalculator;
import com.loadcoder.load.chart.data.DataSet;
import com.loadcoder.load.chart.data.FilteredData;
import com.loadcoder.load.chart.data.Point;
import com.loadcoder.load.chart.data.Range;
import com.loadcoder.load.chart.data.Ranges;
import com.loadcoder.load.chart.jfreechart.ChartPanelExtension;
import com.loadcoder.load.chart.jfreechart.LoadcoderRenderer;
import com.loadcoder.load.chart.jfreechart.XYDottedSeriesExtension;
import com.loadcoder.load.chart.jfreechart.XYPlotExtension;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.menu.DataSetUserType;
import com.loadcoder.load.chart.sampling.Sample;
import com.loadcoder.load.chart.sampling.SampleGroup;
import com.loadcoder.load.chart.utilities.ChartUtils;
import com.loadcoder.load.chart.utilities.ColorUtils;
import com.loadcoder.load.jfreechartfixes.DateAxisExtension;
import com.loadcoder.load.jfreechartfixes.XYLineAndShapeRendererExtention;
import com.loadcoder.result.TransactionExecutionResult;

public abstract class ChartLogic {

	Logger log = LoggerFactory.getLogger(ChartLogic.class);
	
	private final Map<String, XYSeriesExtension> commonSeriesMap = new HashMap<String, XYSeriesExtension>();

	private final CommonSeries[] commonsToBeUsed;

	protected Long earliestX;

	protected long highestX = 0;

	private long sampleLengthToUse;

	final boolean locked;

	private FilteredData filteredData;

	private List<DataSetUserType> removalFiltersInUse = new ArrayList<DataSetUserType>();

	protected final XYSeriesCollectionExtention seriesCollection;

	protected final XYPlotExtension plot;

	protected final XYLineAndShapeRendererExtention renderer;

	protected final Map<String, Boolean> seriesVisible = new HashMap<String, Boolean>();

	protected final List<YCalculator> yCalculators = new ArrayList<YCalculator>();

	public YCalculator yCalculatorToUse = avg;

	private final Map<String, Color> existingColors;

	public final static int TARGET_AMOUNT_OF_POINTS_DEFAULT = 20_000;

	protected static final long SAMPLELENGTH_DEFAULT = 1000;
	/**
	 * customizedColors is not yet supported.
	 */
	@Deprecated
	Map<String, Color> customizedColors;

	private final List<String> seriesKeys = new ArrayList<String>();

	protected final Map<String, LegendItem> legends = new HashMap<String, LegendItem>();

	protected final List<CommonSeriesCalculator> commonSeriesCalculators = new ArrayList<CommonSeriesCalculator>();

	public Ranges getRanges() {
		return ranges;
	}

	public final List<Color> blacklistColors = new ArrayList<Color>();

	JPanel panelForButtons;
	private JMenuBar menuBar = new JMenuBar(); // Window menu bar

	/** The chart theme. */
	private static final ChartTheme currentTheme = new StandardChartTheme("JFree");

	final JFreeChart chart;
	final ChartPanel chartPanel;

	public ChartPanel getChartPanel() {
		return chartPanel;
	}

	public JFreeChart getChart() {
		return chart;
	}

	public ChartLogic(CommonSeries[] commonSeries, boolean locked) {
		this.locked = locked;
		this.seriesCollection = new XYSeriesCollectionExtention();

		Map<String, Color> existingColors = new HashMap<String, Color>();
		renderer = new LoadcoderRenderer(true, false, seriesCollection, existingColors);

		this.commonsToBeUsed = commonSeries == null ? CommonSeries.values() : commonSeries;
		this.existingColors = existingColors;
		for (CommonSeries s : commonsToBeUsed) {
			existingColors.put(s.getName(), s.getColor());
		}

		plot = RuntimeChartLogic.createXYPlotExtension("y", "x", seriesCollection, renderer);

		chart = createXYLineChart(null, true, true, false, plot);

		// using this constructor in order to get rid of jcharts right click popup menu
		chartPanel = new ChartPanelExtension(chart, ChartPanel.DEFAULT_WIDTH, ChartPanel.DEFAULT_HEIGHT,
				ChartPanel.DEFAULT_MINIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MINIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_MAXIMUM_DRAW_WIDTH, ChartPanel.DEFAULT_MAXIMUM_DRAW_HEIGHT,
				ChartPanel.DEFAULT_BUFFER_USED, false, false, false, false, false, false);
		initCommonSeries();

		yCalculators.add(avg);
		yCalculators.add(max);

		ColorUtils.defaultBlacklistColors.stream().forEach((blackListed) -> {
			blacklistColors.add(blackListed);
		});
	}

	public static JFreeChart createXYLineChart(String title, boolean legend, boolean tooltips, boolean urls,
			XYPlot plot) {

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

	public static XYPlotExtension createXYPlotExtension(String yAxisLabel, String xAxisLabel, XYDataset dataset,
			XYLineAndShapeRendererExtention renderer) {
		NumberAxis yAxis = new NumberAxis(yAxisLabel);
		NumberAxis xAxis = new NumberAxis(xAxisLabel);
		xAxis.setAutoRangeIncludesZero(false);
		XYPlotExtension plot = new XYPlotExtension(dataset, xAxis, yAxis, renderer);

		plot.setRenderer(renderer);
		plot.getDomainAxis().setAutoRange(true);
		plot.getRangeAxis().setAutoRange(true);
		return plot;
	}

	protected List<String> getSeriesKeys() {
		return seriesKeys;
	}

	protected void addSeriesKey(String key) {
		if (!seriesKeys.contains(key)) {
			seriesKeys.add(key);
		}
	}

	protected Map<String, XYSeriesExtension> getCommonSeriesMap() {
		return commonSeriesMap;
	}

	protected abstract void update(Map<String, List<TransactionExecutionResult>> listOfListOfList,
			HashSet<Long> hashesGettingUpdated);

	protected abstract void doUpdate();

	public YCalculator getYCalculatorToUse() {
		return yCalculatorToUse;
	}

	public XYSeriesCollectionExtention getSeriesCollection() {
		return seriesCollection;
	}

	public Map<String, Color> getExistingColors() {
		return existingColors;
	}

	public XYPlotExtension getPlot() {
		return plot;
	}

	protected long getXDiff() {
		if (earliestX == null)
			return 0;
		long xDiff = highestX - earliestX;
		return xDiff;
	}

	public void doSafeUpdate() {
		synchronized (plot) {
			doUpdate();
			long xDiff = getXDiff();
			if (xDiff > 23 * HOUR) {
				plot.changeToMonthAndDayDateAxisFormat();
			}
		}
	}

	public List<DataSetUserType> getRemovalFiltersInUse() {
		return removalFiltersInUse;
	}

	protected void setFilteredData(FilteredData filteredData) {
		this.filteredData = filteredData;
	}

	public FilteredData getFilteredData() {
		return filteredData;
	}

	public long getSampleLengthToUse() {
		return sampleLengthToUse;
	}

	public void setSampleLengthToUse(long sampleLengthToUse) {
		this.sampleLengthToUse = sampleLengthToUse;
	}

	public List<YCalculator> getyCalculators() {
		return yCalculators;
	}

	void addToSeriesKeys(FilteredData filteredData, List<String> seriesKeys) {
		for (DataSet dataSet : filteredData.getDataSets()) {
			String s = dataSet.getName();
			if (!seriesKeys.contains(s))
				seriesKeys.add(s);
		}
	}

	public void initCommonSeries() {
		createCommons();
		addAllCommonSeriesToTheChart();
	}

	public void initiateChart() {

		PlotOrientation orientation = PlotOrientation.VERTICAL;
		ParamChecks.nullNotPermitted(orientation, "orientation");
		plot.setOrientation(orientation);

		panelForButtons = new JPanel();

		panelForButtons.setBackground(Color.WHITE);
		panelForButtons.setLayout(new BoxLayout(panelForButtons, BoxLayout.PAGE_AXIS));

		DateAxis dateAxis = new DateAxisExtension();

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateAxis.setDateFormatOverride(format);

		plot.setDomainAxis(dateAxis);

		getChartPanel().addChartMouseListener(new ChartMouseListener() {

			public void chartMouseClicked(ChartMouseEvent e) {
				int button = e.getTrigger().getButton();
				Object entity = e.getEntity();
				XYSeriesCollectionExtention collection = (XYSeriesCollectionExtention) plot.getDataset();
				handleClick(button, entity, collection);
			}

			public void chartMouseMoved(ChartMouseEvent e) {
			}

		});

		getChartPanel().setPreferredSize(new java.awt.Dimension(500, 270));
		getChartPanel().setZoomAroundAnchor(true);
		panelForButtons.add(getChartPanel());

		panelForButtons.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scrolling(e);
				plot.panDomainAxes(30, null, null);
			}
		});

		plot.setDomainGridlinePaint(Color.BLACK);
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setBackgroundPaint(Color.WHITE);
	}

	public JMenuBar getMenu() {
		return menuBar;
	}

	public void scrolling(MouseWheelEvent e) {

		if (e.getScrollType() != MouseWheelEvent.WHEEL_UNIT_SCROLL)
			return;
		if (e.getWheelRotation() < 0)
			increaseZoom(getChartPanel(), true);
		else
			decreaseZoom(getChartPanel(), true);
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

	public void setVisibility(XYSeriesExtension clickedSeries, int iterator, LegendItem legend, boolean visible) {

		if (clickedSeries instanceof XYDottedSeriesExtension) {
			renderer.setSeriesShapesVisible(iterator, visible);
		} else {
			renderer.setSeriesLinesVisible(iterator, visible);
		}
		seriesVisible.put(clickedSeries.getKey(), visible);
		clickedSeries.setVisible(visible);
		legend.setShapeVisible(visible);
	}

	public static void addSurroundingTimestampsAsUpdates(Set<Long> hashesGettingUpdated, long sampleStart,
			long earliest, long latest, Ranges ranges, long currentSampleLength, Set<Long> sampleTimestamps,
			Map<Long, Sample> aboutToBeUpdated) {

		// check backwards
		long iterator = sampleStart;
		while (earliest < iterator) {
			long lastTsInPrevious = iterator - 1;
			long sampleLength = ranges.findSampleLength(lastTsInPrevious);
			long firstTsInPrevious = SampleGroup.calculateFirstTs(lastTsInPrevious, sampleLength);

			boolean exists = false;
			if (sampleTimestamps.contains(firstTsInPrevious) || aboutToBeUpdated.containsKey(firstTsInPrevious)) {
				exists = true;
			}

			if (exists) {
				break;
			} else {
				if (!hashesGettingUpdated.contains(firstTsInPrevious))
					hashesGettingUpdated.add(firstTsInPrevious);
				iterator = firstTsInPrevious;
			}
		}

		// check forward
		iterator = sampleStart;
		while (latest > iterator) {
			long sampleLength = ranges.findSampleLength(iterator);
			long firstTsInNext = iterator + sampleLength;
			boolean exists = false;
			if (sampleTimestamps.contains(firstTsInNext) || aboutToBeUpdated.containsKey(firstTsInNext)) {
				exists = true;
			}

			if (exists) {
				break;
			} else {
				if (!hashesGettingUpdated.contains(firstTsInNext))
					hashesGettingUpdated.add(firstTsInNext);
				iterator = firstTsInNext;
			}
		}
	}

	public static final int MOUSE_LEFT_CLICK_CODE = 1;

	public void handleClick(int button, Object clickedObject, XYSeriesCollectionExtention serieses) {

		synchronized (plot) {
			if (clickedObject instanceof PlotEntity || clickedObject instanceof LegendItemEntity) {
				chart.setNotify(false);

				if (clickedObject instanceof PlotEntity) {
					if (button == MOUSE_LEFT_CLICK_CODE) {
					} else {
						chartPanel.restoreAutoBounds();
					}
				}
				// else is clicked on a legend
				else if (clickedObject instanceof LegendItemEntity) {
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
					if (button == MOUSE_LEFT_CLICK_CODE) {
						boolean visible = !clickedSeries.isVisible();

						setVisibility(clickedSeries, iterator, clickedLegend, visible);
					} else {
						int legendIndexIterator = 0;
						if (clickedSeries.isVisible()) {
							boolean isAllSeriesVisible = true;
							for (XYSeriesExtension xy : lista) {
								if (!xy.isVisible()) {
									isAllSeriesVisible = false;
								}
							}

							if (isAllSeriesVisible) {
								disableAllLegendsExceptOne(clickedSeries, lista);
							} else {

								for (XYSeriesExtension xy : lista) {
									LegendItem legend = xy.getLegend();
									boolean visible = true;
									setVisibility(xy, legendIndexIterator, legend, visible);
									legendIndexIterator++;
								}
							}

						} else {
							disableAllLegendsExceptOne(clickedSeries, lista);
						}
					}
				}
				chart.setNotify(true);
				serieses.fireChange();

			}
		}

	}

	private void disableAllLegendsExceptOne(XYSeriesExtension clickedSeries, List<XYSeriesExtension> lista) {
		int legendIndexIterator = 0;
		for (XYSeriesExtension xy : lista) {
			LegendItem legend = xy.getLegend();
			boolean visible = false;
			if (xy.equals(clickedSeries)) {
				visible = true;
			}
			setVisibility(xy, legendIndexIterator, legend, visible);
			legendIndexIterator++;
		}
	}

	public void createCommons() {

		Arrays.stream(commonsToBeUsed).forEach((common) -> {
			Color c = existingColors.get(common.getName());
			if (c == null) {
				c = common.getColor();
			}
			XYSeriesExtension xySeries = new XYSeriesExtension(common.getName(), true, false, c);
			commonSeriesMap.put(common.getName(), xySeries);
			commonSeriesCalculators.add(new CommonSeriesCalculator(xySeries, common.getCommonYCalculator()));
		});

	}

	public void addSeries(XYSeriesExtension serie) {
		seriesCollection.addSeries(serie);
		int indexOfSeries = seriesCollection.indexOf(serie);
		LegendItem legend = legends.get(serie.getKey());
		if (legend == null) {
			legend = plot.getRenderer().getLegendItem(0, indexOfSeries);
			Color c = existingColors.get(serie.getKey());
			legend.setFillPaint(c);

			legend.setShapeVisible(true);
			legend.setLineVisible(false);

			legends.put(serie.getKey(), legend);
			plot.getLegendItems().add(legend);
		} else {
		}
		serie.setLegend(legend);
	}

	public void removeSeries(XYSeriesExtension serie) {
		int indexOfSeries = seriesCollection.indexOf(serie);
		seriesCollection.removeSeries(indexOfSeries);
	}

	/*
	 * iterate through all timestamp that are the first one in one of the updated
	 * samples. The series that are affected by the transaction series are going to
	 * be upated below
	 */
	void updateCommonsWithSamples(HashSet<Long> hashesGettingUpdated, Map<String, SampleGroup> sampleGroups,
			Map<String, CommonSampleGroup> samplesCommonMap, List<CommonSampleGroup> sampleGroupCommonList) {

		/*
		 * iterate through all timestamp that are the first one in one of the updated
		 * samples. The series that are affected by the transaction series are going to
		 * be upated below
		 */
		for (Long l : hashesGettingUpdated) {
			
			if(l == 0) {
				System.out.println("here");
			}
			for (CommonSeriesCalculator calc : commonSeriesCalculators) {
				XYSeriesExtension series = calc.getSeries();
				
				CommonYCalculator calculator = calc.getCalculator();
				Range r = ranges.lookupCorrectRange(l);
				double amount = calculator.calculateCommonY(seriesKeys, l, sampleGroups, r.getSampleLength());

				// get or create the samplegroup for the common series
				String commonKey = series.getKey();
				CommonSampleGroup commonSampleGroup = samplesCommonMap.get(commonKey);
				if (commonSampleGroup == null) {
					commonSampleGroup = new CommonSampleGroup(series);
					samplesCommonMap.put(commonKey, commonSampleGroup);
					sampleGroupCommonList.add(commonSampleGroup);
				}

				CommonSample cs = commonSampleGroup.getAndCreateSampleAndPutInMap(l, r.getSampleLength());

				long longAmount = Sample.amountToYValue(amount);
				cs.setY(longAmount);
				if (cs.getFirst() == null) {
					cs.initDataItems();
					if(commonKey.contains("Throughput")) {
						log.trace("adding Throughput at x:{} y:{}", cs.getFirst().getX().intValue(), cs.getFirst().getY().intValue());
					}
					series.add(cs.getFirst(), false);
				} else {
					cs.updateDataItems();
				}
			}
		}
	}

	Paint getSeriesColor(String dataSetName) {
		LegendItem legend = legends.get(dataSetName);

		Paint seriesColor = null;
		if (legend == null) {
			seriesColor = getNewColor(dataSetName);
		} else {
			seriesColor = legend.getFillPaint();
		}
		return seriesColor;
	}

	void getSerieses(List<DataSet> dataSets, Map<String, XYSeriesExtension> seriesMap) {
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();

			Paint seriesColor = getSeriesColor(dataSetName);

			XYSeriesExtension seriesName = seriesMap.get(dataSetName);
			if (seriesName == null) {
				XYSeriesExtension serie = new XYSeriesExtension(dataSetName, true, false, seriesColor);
				seriesMap.put(dataSetName, serie);
			}
		}
	}

	void adjustVisibilityOfSeries(XYSeriesExtension serie) {
		int indexOfSeries = seriesCollection.indexOf(serie);
		renderer.setSeriesShape(indexOfSeries, XYDottedSeriesExtension.DOTTEDSHAPE);

		Boolean visible = seriesVisible.get(serie.getKey());

		if (visible == null) {
			visible = true;
			seriesVisible.put(serie.getKey(), visible);
		}

		serie.setVisible(visible);
		serie.getLegend().setShapeVisible(visible);
		renderer.setSeriesLinesVisible(indexOfSeries, visible);
		// is sample mode, the shapes shall always be invisible.
		renderer.setSeriesShapesVisible(indexOfSeries, false);
	}

	void addPoints(List<DataSet> dataSets, Map<String, SampleGroup> sampleGroups, Set<Long> sampleTimestamps) {
		for (DataSet dataSet : dataSets) {
			String dataSetName = dataSet.getName();
			SampleGroup sampleGroup = sampleGroups.get(dataSetName);
			createSamplesAndAddPoints(dataSetName, sampleGroup.getSeries(), dataSet, sampleGroup, sampleTimestamps);
		}
	}

	void createSamplesGroups(Map<String, XYSeriesExtension> seriesMap, Map<String, SampleGroup> sampleGroups) {
		for (String key : seriesKeys) {
			XYSeriesExtension serie = seriesMap.get(key);
			SampleGroup sampleGroup = sampleGroups.get(key);
			if (sampleGroup == null) {
				sampleGroup = new SampleGroup(sampleLengthToUse, serie, locked);
				sampleGroups.put(key, sampleGroup);
			}
		}
	}

	void createSamplesAndAddPoints(String dataSetName, XYSeriesExtension serie, DataSet dataSet,
			SampleGroup sampleGroup, Set<Long> sampleTimestamps) {
		for (Point point : dataSet.getPoints()) {
			long x = point.getX();

			if (x > highestX) {
				highestX = x;
			}
			if (earliestX == null || x < earliestX) {
				earliestX = x;
				//TODO: in runtimechart ranges in never empty. Move this if block to resultchart
				if (ranges.isRangesEmpty()) {
					Sample s = sampleGroup.getOrCreateSample(x, dataSetName, sampleLengthToUse);
					ranges.addRange(new Range(s.getFirstTs(), Long.MAX_VALUE, sampleLengthToUse));

				} else {

					/*
					 * if there are more than one range, and a new earliestTimestamp is added we
					 * must pick up the last added range here.
					 */
//					Range r = ranges.getLastRange();
					Range r = ranges.lookupCorrectRange(x);
					long sampleLengthOfTheLastAddedRange = r.getSampleLength();
					/*
					 * the last added range will get a new start equal to the firstTs for the Sample
					 * of this Point that has the earliest timestamp
					 */
					Sample s = sampleGroup.getOrCreateSample(x, dataSetName, sampleLengthOfTheLastAddedRange);
					r.setStart(s.getFirstTs());
				}
			}

			// fetch the correct Range for the point
			Range rangeToUse = ranges.lookupCorrectRange(x);

			// here the Sample for the point is either fetched of created
			Sample s = sampleGroup.getOrCreateSample(point.getX(), dataSetName, rangeToUse.getSampleLength());

			if (!sampleTimestamps.contains(s.getFirstTs())) {
				sampleTimestamps.add(s.getFirstTs());
			}

			s.addPoint(point);
			if (!point.isStatus()) {
				s.increaseFails();
			}

			// This sample needs to be recalculated and redrawn!
			if (!sampleGroup.getSamplesUnupdated().containsKey(s.getFirstTs())) {
				sampleGroup.getSamplesUnupdated().put(s.getFirstTs(), s);
			}
		}
	}

	public void addAllCommonSeriesToTheChart() {
		for (XYSeriesExtension series : getCommonSeriesMap().values()) {
			addSeries(series);
			int indexOfSeries = seriesCollection.indexOf(series);

			Boolean visible = seriesVisible.get(series.getKey());
			if (visible == null || visible) {
				renderer.setSeriesLinesVisible(indexOfSeries, true);
			} else {
				renderer.setSeriesLinesVisible(indexOfSeries, false);
			}
			renderer.setSeriesShapesVisible(indexOfSeries, false);
		}
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

	public synchronized Color getNewColor(String seriesKey) {

		Set<Color> set = new HashSet<Color>(existingColors.values());
		Color newColor = ColorUtils.getNewContrastfulColor(set, blacklistColors);
		existingColors.put(seriesKey, newColor);
		return newColor;
	}

	public void forceRerender() {
		seriesCollection.fireChange();
	}

	void updateSeriesWithSamples(Set<Long> hashesGettingUpdated, List<DataSet> dataSets,
			Map<String, SampleGroup> sampleGroups, Set<Long> sampleTimestamps, boolean dottedMode) {

		/*
		 * The new data has now been arranged into correct Samples. Time to calculate
		 * each of the affected Samples and update the points.
		 */
		for (String key : seriesKeys) {

			SampleGroup group = sampleGroups.get(key);
			XYSeriesExtension series = group.getSeries();

			Set<Long> aboutToBeUpdatedKeys = group.getSamplesUnupdated().keySet();
			for (Long unupdatedSampleKey : aboutToBeUpdatedKeys) {
				Sample sample = group.getSamplesUnupdated().get(unupdatedSampleKey);
				sample.calculateY(yCalculatorToUse);
				double y = sample.getY();

				if (dottedMode == false) {
					// y will be -1 if there are no data in the sample. No items to add to the
					// series
					if (y != -1) {
						ChartUtils.populateSeriesWithSamples(sample, series);
					}

				}

				long l = sample.getFirstTs();

				if (!hashesGettingUpdated.contains(l)) {
					hashesGettingUpdated.add(l);
				}

				long[] minmaxPoints = getFilteredData().getMinmaxPoints();

				long earliest = minmaxPoints[0];
				long latest = minmaxPoints[1];

				addSurroundingTimestampsAsUpdates(hashesGettingUpdated, l, earliest, latest, ranges, sample.getLength(),
						sampleTimestamps, group.getSamplesUnupdated());
			}
			group.getSamplesUnupdated().clear();
		}
	}
}
