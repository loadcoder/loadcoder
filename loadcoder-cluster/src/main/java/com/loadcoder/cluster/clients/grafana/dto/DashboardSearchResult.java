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
package com.loadcoder.cluster.clients.grafana.dto;

public class DashboardSearchResult {

	String uid;
	String title;
	String uri;
	String url;
	String folderTitle;
	String folderUrl;

	public DashboardSearchResult(String uid, String title, String uri, String url, String folderTitle,
			String folderUrl) {
		super();
		this.uid = uid;
		this.title = title;
		this.uri = uri;
		this.url = url;
		this.folderTitle = folderTitle;
		this.folderUrl = folderUrl;
	}
	
	public String getUid() {
		return uid;
	}
	public String getTitle() {
		return title;
	}
	public String getUri() {
		return uri;
	}
	public String getUrl() {
		return url;
	}
	public String getFolderTitle() {
		return folderTitle;
	}
	public String getFolderUrl() {
		return folderUrl;
	}
}
