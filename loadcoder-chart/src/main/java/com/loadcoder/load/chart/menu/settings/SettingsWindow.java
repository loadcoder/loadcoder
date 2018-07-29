package com.loadcoder.load.chart.menu.settings;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.data.xy.XYSeries;

import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ResultChartLogic;
import com.loadcoder.load.chart.menu.DoubleSteppingSlider;
import com.loadcoder.load.chart.menu.MouseClickedListener;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	JColorChooser colorChooser;
	Component clickedButton;
	private Map<Component, XYSeriesExtension> selectionMap = new HashMap<Component, XYSeriesExtension>();

	final SettingsLogic settingsLogic;

	JPanel bottomButtons = new JPanel();

	public class ColorSelectionSetting {
		public Component button;
		public XYSeriesExtension series;

		public ColorSelectionSetting(Component button, XYSeriesExtension series) {
			this.button = button;
			this.series = series;
		}
	}

	public SettingsWindow(ChartFrame parent, String title, ChartLogic chartLogic) {
		super(parent, title, true);
		setPreferredSize(new Dimension(1000, 500));
		settingsLogic = new SettingsLogic(chartLogic);

		JPanel base = new JPanel(new BorderLayout());
		getContentPane().add(base);

		JTabbedPane jtp = new JTabbedPane(JTabbedPane.LEFT);
		jtp.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		base.add(jtp);

		JPanel bottomButtonsBase = new JPanel();
		bottomButtonsBase.setBorder(BorderFactory.createLineBorder(Color.ORANGE));

		bottomButtonsBase.add(bottomButtons, BorderLayout.EAST);

		bottomButtons.setBorder(BorderFactory.createLineBorder(Color.pink));
		base.add(bottomButtonsBase, BorderLayout.SOUTH);

		List<XYSeries> list = parent.getSeriesCollection().getSeries();

		setTitle("Settings");

		JPanel colorsLeftArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jtp.addTab("Colors", colorsLeftArea);

		JPanel detailsLeftArea = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jtp.addTab("Details", detailsLeftArea);

		Dictionary<Integer, Component> labelTable = new Hashtable<Integer, Component>();

		List<Double> keepFactorValues = new ArrayList<Double>();
		for (double i = 0.0001; i < 1; i = i * 10) {
			double roundMultiplicator = 1 / i;
			for (long j = 1; j < 10; j++) {
				long l = Math.round((double) j); // this is need for rounding issues with doubles
				double rounded = (double) l / roundMultiplicator;
				keepFactorValues.add(rounded);
			}
		}
		keepFactorValues.add(1D);
		labelTable.put(0, new JLabel("" + (keepFactorValues.get(0) * 100) + "%"));
		labelTable.put(keepFactorValues.size() - 1, new JLabel("100%"));

		JTextArea textField = new JTextArea();
		Double[] doubles = keepFactorValues.toArray(new Double[keepFactorValues.size()]);

		int startIndex = 0;
		double keepFactor;
		if (chartLogic instanceof ResultChartLogic) {
			ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;

			if (resultChartLogic.getKeepFactorChosen() != -1) {
				keepFactor = resultChartLogic.getKeepFactorChosen();
			} else {
				keepFactor = resultChartLogic.getKeepFactorDefault();
			}
			startIndex = DoubleSteppingSlider.getIndexOf(doubles, keepFactor);

			textField.setText(keepFactorToProcentString(keepFactor));
		}
		DoubleSteppingSlider pointsKeepFactorSlider = new DoubleSteppingSlider(doubles, startIndex);
		pointsKeepFactorSlider.setLabelTable(labelTable);

		ChangeListener listener = new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				// update text field when the slider value changes
				DoubleSteppingSlider source = (DoubleSteppingSlider) event.getSource();
				int index = source.getValue();
				double value = source.getValues()[index];

				String valueAsProcent = keepFactorToProcentString(value);
				textField.setText(valueAsProcent);

				settingsLogic.setKeepFactorSelection(value);
			}
		};
		pointsKeepFactorSlider.addChangeListener(listener);

		detailsLeftArea.add(pointsKeepFactorSlider);
		detailsLeftArea.add(textField);

		JPanel jp1 = new JPanel(new GridBagLayout());
		colorsLeftArea.add(jp1, BorderLayout.NORTH);

		JPanel colorChooserPanel = new JPanel(new BorderLayout());
		colorChooserPanel.setBorder(BorderFactory.createLineBorder(Color.RED));
		colorsLeftArea.add(colorChooserPanel, BorderLayout.NORTH);

		addSeriesOptions(list, jp1);
		addColorChooser(colorChooserPanel);
		addBottomButtons(bottomButtons);

		pack();
		setVisible(true);

	}

	String keepFactorToProcentString(double value) {
		long multiRounding = Math.round(value * 10000);
		double rounded = (double) multiRounding / 100;
		String valueAsProcent = "" + (rounded) + "%";
		return valueAsProcent;
	}

	void addBottomButtons(JPanel parent) {
		JButton applyButton = new JButton("Apply");
		applyButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				settingsLogic.applyColorSelections();
			}
		});

		JButton okButton = new JButton("Ok");
		okButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				settingsLogic.applyColorSelections();
				dispose();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});

		parent.add(okButton);
		parent.add(applyButton);
		parent.add(closeButton);
	}

	public void addSeriesOptions(List<XYSeries> list, JPanel panelToAddOptionsTo) {
		GridBagConstraints c = new GridBagConstraints();
		for (int i = 0; i < list.size(); i++) {
			XYSeriesExtension series = (XYSeriesExtension) list.get(i);

			JButton button = new JButton("");
			selectionMap.put(button, series);

			Color seriesColor = (Color) series.getColorInTheChart();
			button.setBackground(seriesColor);
			button.setPreferredSize(new Dimension(25, 25));

			button.addMouseListener(new MouseClickedListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					clickedButton = e.getComponent();
					Color c = clickedButton.getBackground();

					settingsLogic.setChosenSeries(selectionMap.get(clickedButton));
					settingsLogic.setColorChooserVisible(true);

					if (settingsLogic.isColorChooserVisible()) {
						colorChooser.setVisible(settingsLogic.isColorChooserVisible());
					}
					colorChooser.setColor(c);
				}
			});
			JLabel label = new JLabel("" + series.getKey());

			c.gridx = 0;
			c.gridy = i;

			panelToAddOptionsTo.add(button, c);
			c.gridx = 1;
			c.gridy = i;

			panelToAddOptionsTo.add(label, c);

		}
	}

	public void addColorChooser(JComponent parent) {

		// Set up color chooser for setting text color
		colorChooser = new JColorChooser(Color.GREEN);
		colorChooser.getSelectionModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Color chosenColor = colorChooser.getColor();
				clickedButton.setBackground(chosenColor);
				settingsLogic.changeSeriesColorSelection(chosenColor);
			}
		});

		colorChooser.setBorder(BorderFactory.createTitledBorder("Choose new color"));

		AbstractColorChooserPanel[] ccPanels = colorChooser.getChooserPanels();
		for (AbstractColorChooserPanel ccPanel : ccPanels) {
			String name = ccPanel.getClass().getSimpleName();
			if (!ccPanel.getDisplayName().equals("RGB"))
				colorChooser.removeChooserPanel(ccPanel);
		}
		parent.add(colorChooser);
		colorChooser.setVisible(settingsLogic.isColorChooserVisible());
	}

}