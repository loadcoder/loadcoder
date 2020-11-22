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
package com.loadcoder.network;

import okhttp3.Request;

public class OKHttpRequestBuilder extends Request.Builder {
	public OKHttpRequestBuilder add(BuilderAddable builderAddable) {
		builderAddable.add(this);
		return this;
	}
	
	@Override
	public OKHttpRequestBuilder header(String header, String value) {
		super.header(header, value);
		return this;
	}
	
	@Override
	public OKHttpRequestBuilder url(String url) {
		super.url(url);
		return this;
	}
}
