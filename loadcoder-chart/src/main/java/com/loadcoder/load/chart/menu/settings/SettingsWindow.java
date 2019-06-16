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
package com.loadcoder.load.chart.menu.settings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.loadcoder.load.chart.jfreechart.ChartFrame;
import com.loadcoder.load.chart.jfreechart.XYSeriesCollectionExtention;
import com.loadcoder.load.chart.jfreechart.XYSeriesExtension;
import com.loadcoder.load.chart.logic.ChartLogic;
import com.loadcoder.load.chart.logic.ResultChartLogic;
import com.loadcoder.load.chart.menu.MouseClickedListener;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	List<Settings> settings = new ArrayList<Settings>();
	ChartLogic chartLogic;
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

		this.chartLogic = chartLogic;
		settings.add(new ColorSettings(chartLogic,
				((XYSeriesCollectionExtention) chartLogic.getPlot().getDataset()).getSeries()));

		// DetailsSettings shall only be added for the ResultChart
		if (chartLogic instanceof ResultChartLogic) {
			settings.add(new DetailsSettings((ResultChartLogic) chartLogic));
		}

		JPanel base = new JPanel(new BorderLayout());
		getContentPane().add(base);

		JTabbedPane jtp = new JTabbedPane(JTabbedPane.LEFT);
		base.add(jtp);

		JPanel bottomButtonsBase = new JPanel();

		bottomButtonsBase.add(bottomButtons, BorderLayout.EAST);

		base.add(bottomButtonsBase, BorderLayout.SOUTH);

		setTitle("Settings");

		for (Settings setting : settings) {

			JPanel area = new JPanel(new FlowLayout(FlowLayout.LEFT));
			area.add(setting.getWindow());
			jtp.addTab(setting.getName(), area);

		}

		addBottomButtons(bottomButtons);

		pack();
		setVisible(true);
	}

	protected void handleChartSettingsActionsModel(ChartSettingsActionsModel model) {
		if (model.isRecreatePoints()) {
			ResultChartLogic resultChartLogic = (ResultChartLogic) chartLogic;
			resultChartLogic.recreateDottedSeries();
		} else if (model.isRender()) {
			chartLogic.forceRerender();
		}
	}

	protected void addBottomButtons(JPanel parent) {
		JButton applyButton = new JButton("Apply");
		applyButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				ChartSettingsActionsModel model = new ChartSettingsActionsModel();
				settings.stream().forEach((a) -> a.apply(model));
				handleChartSettingsActionsModel(model);
			}
		});

		JButton okButton = new JButton("Ok");
		okButton.addMouseListener(new MouseClickedListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				ChartSettingsActionsModel model = new ChartSettingsActionsModel();
				settings.stream().forEach((a) -> a.apply(model));
				handleChartSettingsActionsModel(model);
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

}