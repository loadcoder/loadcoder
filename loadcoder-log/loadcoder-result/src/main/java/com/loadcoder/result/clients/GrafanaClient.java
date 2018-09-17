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
package com.loadcoder.result.clients;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.result.Result;

/**
 * This is an incredibly simlpe Grafana client build just upon java's net
 * package
 */
public class GrafanaClient extends HttpClient {

	Logger log = LoggerFactory.getLogger(GrafanaClient.class);

	protected static final String GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.000";
	protected static final SimpleDateFormat TIMESPAN_FORMAT = new SimpleDateFormat(
			GRAFANA_DASHBOARD_TIMESPAN_DATETIMEFORMAT);
	private final String DB_URL_TEMPLATE = "%s://%s:%s/api/dashboards/db";
	private final String DB_URL;

	final String authorizationValue;

	public GrafanaClient(String host, int port, boolean https, String authorizationValue) {
		String protocol = protocolAsString(https);
		DB_URL = String.format(DB_URL_TEMPLATE, protocol, host, port);
		this.authorizationValue = authorizationValue;
	}

	private static class Type {
		String type;
		String refId;

		private Type(String type, String refId) {
			this.type = type;
			this.refId = refId;
		}
	}

	List<Type> types = Arrays.asList(new Type("mean", "A"));

	String getFileAsString(String filename) {
		InputStream in = getClass().getResourceAsStream("/" + filename);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		List<String> fileAsLineList = new ArrayList<String>();
		reader.lines().forEach(line -> fileAsLineList.add(line));
		try {
			String fileAsString = "";
			for (String line : fileAsLineList) {
				fileAsString = fileAsString + line + "\n";
			}
			return fileAsString;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	String getTargets(Set<String> measurementNames) {

		String targetTemplate = getFileAsString("grafana_5.2.4/grafana_target_body_template.json");

		List<String> targetsList = new ArrayList<String>();
		for (String name : measurementNames) {
			for (Type t : types) {
				String target = targetTemplate.replace("${measurement_name}", name).replace("${refid}", t.refId)
						.replace("${select_type}", t.type);
				targetsList.add(target);
			}
		}
		String result = "";
		for (int i = 0; i < targetsList.size(); i++) {
			String delimiter = i == targetsList.size() - 1 ? "\n" : ",\n";
			result = result + targetsList.get(i) + delimiter;
		}
		return result;
	}

	public int createNewDashboardFromResult(Result result, String name) {
		String startTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(result.getStart(), TIMESPAN_FORMAT);
		String endTimespan = DateTimeUtil.convertMilliSecondsToFormattedDate(result.getEnd(), TIMESPAN_FORMAT);
		log.debug("panel will have timespan: " + startTimespan + " - " + endTimespan);
		String dateTimeLabel = DateTimeUtil.convertMilliSecondsToFormattedDate(result.getStart());
		String fileAsString = getFileAsString("grafana_5.2.4/grafana_post_dashboard_body_template.json");

		fileAsString = fileAsString.replace("${time_from}", startTimespan);
		fileAsString = fileAsString.replace("${time_to}", endTimespan);
		fileAsString = fileAsString.replace("${title}", name + "_" + dateTimeLabel);

		String targets = getTargets(result.getResultLists().keySet());
		fileAsString = fileAsString.replace("${targets}", targets);
		fileAsString = fileAsString.replace("${requestid}", "" + System.currentTimeMillis());

		List<Header> headers = Arrays.asList(new Header("Content-Type", "application/json"),
				new Header("Authorization", authorizationValue));
		return sendPost(fileAsString, DB_URL, headers);

	}
}
