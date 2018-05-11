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
package com.loadcoder.load.chart.menu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AboutPopup extends JDialog implements ActionListener {
	
	private static final String LOADCODER_COPYRIGHT_NOTICE = 
	"Loadcoder. A loadtesting tool for javacoders\n"
	+ "Copyright (C) 2018 Stefan Vahlgren at Loadcoder\n"
	+ "Loadcoder is licensed under the GNU GENERAL PUBLIC LICENSE Version 3 or later.\n"
	+ "You can find a copy of this license at https://www.gnu.org/licenses/gpl-3.0.txt\n"
	+ "For more informaiton about Loadcoder, visit http://loadcoder.com";

	
	private static final String JFREECHART_PROMINENT_NOTICE = 
	"This software uses the JFreeChart library.\n"
	+ "(C) Copyright 2000-2016, by Object Refinery Limited and Contributors.\n"
	+ "The JFreeChart Library is licensed under the GNU Lesser General Public Library, version 2.1.\n"
	+ "You can find a copy of this license at https://www.gnu.org/licenses/lgpl-2.1.en.html\n"
	+ "For more information about JFreeChart, visit http://www.jfree.org/jfreechart/";
	
	private static final String GUAVA_PROMINENT_NOTICE = 
	"This software uses the Guava library.\n"
	+ "Copyright (C) 2011 The Guava Authors.\n"
	+ "The Guava Library is licensed under the Apache License, Version 2.0.\n"
	+ "You can find a copy of this license at http://www.apache.org/licenses/LICENSE-2.0\n"
	+ "For more information about Guava, visit https://github.com/google/guava/";
	
	public static void showAboutPopup(JFrame parent) {
		String message = 
				LOADCODER_COPYRIGHT_NOTICE
				+"\n\n\n" + JFREECHART_PROMINENT_NOTICE
				+"\n\n\n" + GUAVA_PROMINENT_NOTICE;
		new AboutPopup(parent, "About", message);
	}
	public AboutPopup(JFrame parent, String title, String message) {
		super(parent, title, true);
		if (parent != null) {
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation();
			setLocation(p.x + parentSize.width / 4, p.y + parentSize.height / 4);
		}
		JPanel messagePane = new JPanel();
		JTextArea jTextArea = new JTextArea(message);
		jTextArea.setEditable(false);
		messagePane.add(jTextArea);
		getContentPane().add(messagePane);
		JPanel buttonPane = new JPanel();
		JButton button = new JButton("OK");
		buttonPane.add(button);
		button.addActionListener(this);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		setVisible(false);
		dispose();
	}
}
