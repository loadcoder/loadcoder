/*******************************************************************************
 * Copyright (C) 2020 Team Loadcoder
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
package com.loadcoder.cluster.clients.grafana;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrafanaDashboardLinks {

	Logger log = LoggerFactory.getLogger(this.getClass());
	String relativeTimespanLink;
	String absoluteTimespanLink;

	public GrafanaDashboardLinks(	String relativeTimespanLink, String absoluteTimespanLink) {
		this.relativeTimespanLink = relativeTimespanLink;
		this.absoluteTimespanLink = absoluteTimespanLink;
	}

	public String getAbsoluteTimespanLink() {
		return absoluteTimespanLink;
	}
	
	public String getRelativeTimespanLink(){
		return relativeTimespanLink;
	}
	
	public void log() {
		log.info("Relative time dashboard link:{}", relativeTimespanLink);
		log.info("Absolute time dashboard link:{}", absoluteTimespanLink);

	}

	
}
