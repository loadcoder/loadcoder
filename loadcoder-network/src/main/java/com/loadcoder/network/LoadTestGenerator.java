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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HarResponse;

public class LoadTestGenerator {

	List<String> BLACKLIST_URL_DEFAULT = Arrays.asList(".js", ".css", ".png", ".jpg");
	BlackList blackList = new BlackList((url, matcher) -> {
		return url.endsWith(matcher);
	}, BLACKLIST_URL_DEFAULT);

	protected boolean blackListed(String url) {
		return blackList.blackListed(url);
	}

	public void generate(String pathToHARFile, String destinationDir) {

		Har har = readHar(pathToHARFile);

		List<HarEntry> entries = har.getLog().getEntries();

		File dstDir = new File(destinationDir);
		if (!dstDir.exists()) {
			dstDir.mkdirs();
		}

		generateScenario(entries, dstDir);
		generateTest(dstDir);
		generateThreadInstance(dstDir);
	}

	public void generateScenario(List<HarEntry> entries, File dstDir) {
		String scenarioLogic = readFile("src/main/resources/testgeneration_templates/ScenarioLogic.tmp");

		int requestIterator = 0;
		for (HarEntry entry : entries) {

			String url = entry.getRequest().getUrl();
			boolean blackListed = blackList.blackListed(url);
			if (blackListed) {
				continue;
			}

			String loadMethod = generateLoadMethod(entry, requestIterator);
			scenarioLogic = scenarioLogic.replace("${logic_end}", loadMethod + "\n" + "${logic_end}");
			requestIterator++;
		}

		scenarioLogic = scenarioLogic.replace("${logic_start}", "");
		scenarioLogic = scenarioLogic.replace("${logic_end}", "");

		File dstFile = new File(dstDir, "ScenarioLogic.java");
		writeFile(scenarioLogic.getBytes(), dstFile);
	}

	public void generateTest(File dstDir) {
		File dstFile = new File(dstDir, "GeneratedLoadTest.java");
		String testContent = readFile("src/main/resources/testgeneration_templates/GeneratedLoadTest.tmp");
		writeFile(testContent.getBytes(), dstFile);
	}

	public void generateThreadInstance(File dstDir) {
		File dstFile = new File(dstDir, "ThreadInstance.java");
		String threadInstanceContent = readFile("src/main/resources/testgeneration_templates/ThreadInstance.tmp");
		writeFile(threadInstanceContent.getBytes(), dstFile);
	}

	public String generateLoadMethod(HarEntry entry, int transactionIterator) {
		String loadMethodTemplate = readFile("src/main/resources/testgeneration_templates/loadmethod.tmp");

		String loadMethod = loadMethodTemplate;
		loadMethod = loadMethod.replace("${transaction_name}", "t" + transactionIterator);
		HarRequest req = entry.getRequest();

		loadMethod = loadMethod.replace("${transaction_url}", req.getUrl());

		loadMethod = loadMethod.replace("${request_variable}", "request" + transactionIterator);

		List<HarHeader> headers = req.getHeaders();
		String headerTemplate = readFile("src/main/resources/testgeneration_templates/addheader.tmp");
		for (HarHeader header : headers) {

			if (isHeaderNameSPDY(header.getName())) {
				continue;
			}
			String h = headerTemplate.replace("${header_key}", header.getName()).replace("${header_value}",
					header.getValue());
			loadMethod = loadMethod.replace("${request_building}", h + "\n${request_building}");
		}
		loadMethod = loadMethod.replace("${request_building}", "\n");

		HarResponse resp = entry.getResponse();
		loadMethod = loadMethod.replace("${expected_http_code}", "" + resp.getStatus());
		return loadMethod;
	}

	private boolean isHeaderNameSPDY(String headerName) {
		return headerName.startsWith(":");
	}

	private String readFile(String path) {
		Path p = Paths.get(path);
		try {
			byte[] src = Files.readAllBytes(p);
			String s = new String(src);
			return s;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeFile(byte[] bytes, File destination) {
		Path p2 = Paths.get(destination.getAbsolutePath());
		try {
			Files.write(p2, bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Har readHar(String pathToHARFile) {
		HarReader harReader = new HarReader();
		Har har;
		try {
			har = harReader.readFromFile(new File(pathToHARFile));
			return har;
		} catch (HarReaderException e) {
			throw new RuntimeException(e);
		}
	}
}
