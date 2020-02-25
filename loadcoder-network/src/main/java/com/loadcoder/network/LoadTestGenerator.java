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
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HarResponse;

public class LoadTestGenerator {

	static List<String> URL_MATCHERS = Arrays.asList(".*/[a-zA-Z0-9._-]*[.]{1}fileextension",
			".*/[a-zA-Z0-9._-]*[.]{1}fileextension\\?.*");
	static List<String> WEBPAGES_FILTERS = Arrays.asList("html", "htm", "aspx", "php");

	static List<String> staticRegexps = getStaticRegexps();
	static List<String> wantedStaticRegexps = getWantedStaticRegexps();

	final private Matcher matcher;

	final private String pathToHARFile;
	final private String javaPackage;
	final private String destinationJavaCodeDir;
	final private String destinationResourceDir;
	final private boolean allowOverwritingExistingFiles;

	final private File scenarioDstFile;
	final private File testDstFile;
	final private File threadInstanceFile;

	private LoadTestGenerator(String pathToHARFile, List<String> allowedRULStarts, String javaPackage,
			String destinationJavaCodeDir, String destinationResourceDir, boolean allowOverwritingExistingFiles) {
		this.pathToHARFile = pathToHARFile;
		this.javaPackage = javaPackage;
		this.destinationJavaCodeDir = destinationJavaCodeDir;
		this.destinationResourceDir = destinationResourceDir;
		this.allowOverwritingExistingFiles = allowOverwritingExistingFiles;

		this.scenarioDstFile = new File(destinationJavaCodeDir, "ScenarioLogic.java");
		this.testDstFile = new File(destinationJavaCodeDir, "GeneratedLoadTest.java");
		this.threadInstanceFile = new File(destinationJavaCodeDir, "ThreadInstance.java");

		this.matcher = getDefaultMatcher(allowedRULStarts);

	}

	static Matcher getDefaultMatcher(List<String> urlShallStartWithOneOfThese) {
		Matcher m = (url) -> {

			boolean removeUrlBecauseOfWrongStart = removeBecasueOfWrongStartOfUrl(url, urlShallStartWithOneOfThese);
			if (removeUrlBecauseOfWrongStart) {
				return false;
			}

			boolean isStatic = isUrlStatic(url);
			boolean isWantedStatic = isUrlWantedStatic(url);

			if (isStatic) {
				if (isWantedStatic) {
					return true;
				} else {
					return false;
				}
			} else {
				if (isWantedStatic) {
					return false;
				} else {
					return true;
				}
			}
		};
		return m;
	}

	static boolean removeBecasueOfWrongStartOfUrl(String url, List<String> urlShallStartWithOneOfThese) {
		if (urlShallStartWithOneOfThese == null) {
			return false;
		}
		for (String urlStart : urlShallStartWithOneOfThese) {
			if (url.startsWith(urlStart)) {
				return false;
			}
		}
		return true;
	}

	static boolean isUrlStatic(String url) {
		for (String staticRegexp : staticRegexps) {
			if (url.matches(staticRegexp)) {
				return true;
			}
		}
		return false;
	}

	static boolean isUrlWantedStatic(String url) {
		for (String wantedStaticRegexp : wantedStaticRegexps) {
			if (url.matches(wantedStaticRegexp)) {
				return true;
			}
		}
		return false;
	}

	static List<String> getWantedStaticRegexps() {
		List<String> wantedStaticsRegexps = new ArrayList<>();
		for (String webpageException : WEBPAGES_FILTERS) {
			for (String urlMatcher : URL_MATCHERS) {
				wantedStaticsRegexps.add(urlMatcher.replace("fileextension", webpageException));
			}
		}
		return wantedStaticsRegexps;
	}

	static List<String> getStaticRegexps() {
		List<String> wantedStaticsRegexps = new ArrayList<>();
		for (String urlMatcher : URL_MATCHERS) {
			wantedStaticsRegexps.add(urlMatcher.replace("fileextension", "[a-zA-Z0-9]*"));
		}
		return wantedStaticsRegexps;
	}

	protected static void printAllURLs(String pathToHARFile) {

		Har har = readHar(pathToHARFile);
		List<HarEntry> entries = har.getLog().getEntries();

		for (HarEntry entry : entries) {
			Date date = entry.getStartedDateTime();
			String url = entry.getRequest().getUrl();
			System.out.println(date + " " + url);
		}

	}

	public void sortHarEntries(List<HarEntry> entries) {
		entries.sort((a, b) -> a.getStartedDateTime().before(b.getStartedDateTime()) ? -1 : 1);
	}

	public static void generate(String pathToHARFile, List<String> acceptableUrlStarts, String javaPackage,
			String destinationJavaCodeDir, String destinationResourceDir, boolean allowOverwritingExistingFiles) {
		LoadTestGenerator generator = new LoadTestGenerator(pathToHARFile, acceptableUrlStarts, javaPackage,
				destinationJavaCodeDir, destinationResourceDir, allowOverwritingExistingFiles);
		generator.gen();
	}

	public void gen() {

		Har har = readHar(pathToHARFile);
		List<HarEntry> entries = har.getLog().getEntries();
		List<HarEntry> filteredEntries = new ArrayList<>();
		for (HarEntry entry : entries) {
			String url = entry.getRequest().getUrl();
			boolean result = matcher.keep(url);
			if (result) {
				filteredEntries.add(entry);
			}
		}
		sortHarEntries(filteredEntries);

		File javaDstDir = new File(destinationJavaCodeDir);
		if (!javaDstDir.exists()) {
			javaDstDir.mkdirs();
		}
		File resourceDstDir = new File(destinationResourceDir);
		if (!resourceDstDir.exists()) {
			resourceDstDir.mkdirs();
		}

		checkNoColidingFilesExists();

		generateScenario(filteredEntries, javaDstDir, resourceDstDir);
		generateTest(javaDstDir);
		generateThreadInstance(javaDstDir);
	}

	public void checkNoColidingFilesExists() {
		if (!allowOverwritingExistingFiles) {
			try {
				if (scenarioDstFile.exists()) {
					throw new FileAlreadyExistsException(scenarioDstFile.getAbsolutePath());
				}
				if (testDstFile.exists()) {
					throw new FileAlreadyExistsException(testDstFile.getAbsolutePath());
				}
				if (threadInstanceFile.exists()) {
					throw new FileAlreadyExistsException(threadInstanceFile.getAbsolutePath());
				}
			} catch (FileAlreadyExistsException e) {
				throw new RuntimeException(e);
			}
		}

	}

	public void generateScenario(List<HarEntry> entries, File javaDstDir, File resourceDstDir) {
		String scenarioLogic = readFile("src/main/resources/testgeneration_templates/ScenarioLogic.tmp");
		scenarioLogic = scenarioLogic.replace("${package}", javaPackage);

		TransactionNameGenerator transactionNameGenerator = new TransactionNameGenerator();
		int requestIterator = 0;
		for (HarEntry entry : entries) {
			String loadMethod = generateLoadMethod(entry, transactionNameGenerator, requestIterator, resourceDstDir);
			scenarioLogic = scenarioLogic.replace("${logic_end}", loadMethod + "\n" + "${logic_end}");
			requestIterator++;
		}

		scenarioLogic = scenarioLogic.replace("${logic_start}", "");
		scenarioLogic = scenarioLogic.replace("${logic_end}", "");

		writeFile(scenarioLogic.getBytes(), scenarioDstFile);
	}

	public void generateTest(File dstDir) {

		String testContent = readFile("src/main/resources/testgeneration_templates/GeneratedLoadTest.tmp");
		testContent = testContent.replace("${package}", javaPackage);

		writeFile(testContent.getBytes(), testDstFile);
	}

	public void generateThreadInstance(File dstDir) {

		String threadInstanceContent = readFile("src/main/resources/testgeneration_templates/ThreadInstance.tmp");
		threadInstanceContent = threadInstanceContent.replace("${package}", javaPackage);

		writeFile(threadInstanceContent.getBytes(), threadInstanceFile);
	}

	public String generateLoadMethod(HarEntry entry, TransactionNameGenerator transactionNameGenerator,
			int transactionIterator, File resourceDstDir) {
		String loadMethodTemplate = readFile("src/main/resources/testgeneration_templates/loadmethod.tmp");

		String transactionName = transactionNameGenerator.generateTransactionName(entry, 1, 20);
		String loadMethod = loadMethodTemplate;
		loadMethod = loadMethod.replace("${transaction_name}", transactionName);
		HarRequest req = entry.getRequest();

		loadMethod = loadMethod.replace("${transaction_url}", req.getUrl());

		loadMethod = loadMethod.replace("${request_variable}", "request" + transactionIterator);

		HarHeader contentType = null;
		List<HarHeader> headers = req.getHeaders();
		String headerTemplate = readFile("src/main/resources/testgeneration_templates/addheader.tmp");
		for (HarHeader header : headers) {

			if (isHeaderNameSPDY(header.getName())) {
				continue;
			}
			String headerValue = header.getValue();
			headerValue = headerValue.replaceAll("[\"]", "\\\\\"");
			String h = headerTemplate.replace("${header_key}", header.getName()).replace("${header_value}",
					headerValue);
			loadMethod = loadMethod.replace("${request_building}", h + "\n${request_building}");

			if (header.getName().equals("content-type")) {
				contentType = header;
			}
		}

		String requestBodyTemplate = "";
		String body = entry.getRequest().getPostData().getText();
		if (body != null && !body.isEmpty()) {
			String fileName = "body" + transactionIterator + ".txt";
			File bodyFile = new File(resourceDstDir, fileName);
			writeFile(body.getBytes(), bodyFile);

			requestBodyTemplate = readFile("src/main/resources/testgeneration_templates/requestBody.tmp");
			requestBodyTemplate = requestBodyTemplate.replace("${requestbody_variable}",
					"reqBody" + transactionIterator);
			requestBodyTemplate = requestBodyTemplate.replace("${body_file}", destinationResourceDir + "/" + fileName);
			requestBodyTemplate = requestBodyTemplate.replace("${mediatype}", contentType.getValue());

			String requestMethodBodyTemplate = readFile(
					"src/main/resources/testgeneration_templates/requestMethodBody.tmp");
			requestMethodBodyTemplate = requestMethodBodyTemplate.replace("${request_http_verb}",
					entry.getRequest().getMethod().name());
			requestMethodBodyTemplate = requestMethodBodyTemplate.replace("${request_body_file}",
					"reqBody" + transactionIterator);

			loadMethod = loadMethod.replace("${request_building}", requestMethodBodyTemplate + "\n${request_building}");
		}

		loadMethod = loadMethod.replace("${request_body}", requestBodyTemplate);

		loadMethod = loadMethod.replace("${request_building}", "");

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

	public static Har readHar(String pathToHARFile) {
		HarReader harReader = new HarReader();
		Har har;
		try {
			har = harReader.readFromFile(new File(pathToHARFile));
			return har;
		} catch (HarReaderException e) {
			throw new RuntimeException(e);
		}
	}

	protected static class TransactionNameGenerator {
		final Map<String, List<UrlAmount>> urlsGeneratedTransaction;

		private static class UrlAmount {

			String url;
			int amount;

			public String getUrl() {
				return url;
			}

			public UrlAmount(String url, int amount) {
				super();
				this.url = url;
				this.amount = amount;
			}

			public int getAmount() {
				return amount;
			}

		}

		TransactionNameGenerator() {
			this.urlsGeneratedTransaction = new HashMap<String, List<UrlAmount>>();
		}

		protected String makeTransactionFriendlyString(String original) {
			return original.replaceAll("[% .,&(){}=$#@!_-]*", "");
		}

		public String generateTransactionName(HarEntry entry, int amountofUrlPartsToUse, int maxTransactionNameLength) {

			String result = "";
			String url = entry.getRequest().getUrl().replaceFirst("[a-zA-Z]*://", "");
			String[] splittedUrl = url.split("/");
			List<String> parts = new ArrayList<>();

			for (int i = splittedUrl.length - 1; i >= 0; i--) {
				String part = splittedUrl[i];
				part = part.split("[?]")[0];
				if (part.isEmpty()) {
					continue;
				}
				parts.add(makeTransactionFriendlyString(part));
			}
			String query = "";
			if (entry.getRequest().getQueryString().size() == 1) {
				HarQueryParam param = entry.getRequest().getQueryString().get(0);
				String name = makeTransactionFriendlyString(param.getName());
				query = name;
			}
			String httpMethod = entry.getRequest().getMethod().toString();
			result += httpMethod + "_";

			String urlPartToUse = null;
			for (String part : parts) {
				if (!part.isEmpty()) {
					urlPartToUse = part;
					break;
				}
			}

			result = aggregateTransactionName(httpMethod, urlPartToUse, query);
			while (result.length() > maxTransactionNameLength) {
				if (!query.isEmpty()) {
					result = aggregateTransactionName(httpMethod, "", query);
					if (result.length() <= maxTransactionNameLength) {
						break;
					} else {
						query = "";
						continue;
					}
				}
				result = aggregateTransactionName(httpMethod, urlPartToUse, query);
				break;
			}

			List<UrlAmount> amounts = urlsGeneratedTransaction.get(result);
			if (amounts == null) {
				amounts = new ArrayList<UrlAmount>();
				urlsGeneratedTransaction.put(result, amounts);
			}
			UrlAmount amountToUse = null;
			for (UrlAmount a : amounts) {
				if (a.getUrl().equals(url)) {
					amountToUse = a;
					break;
				}
			}
			if (amountToUse == null) {
				amountToUse = new UrlAmount(url, amounts.size());
				amounts.add(amountToUse);
			}
			if (amountToUse.getAmount() == 0) {
				return result;
			} else {
				return result + "_" + amountToUse.getAmount();
			}
		}

		public String aggregateTransactionName(String httpMethod, String path, String query) {
			String result = httpMethod;
			result += "_" + path;
			if (!query.isEmpty()) {
				result += "_" + query;
			}
			return result;
		}
	}
}
