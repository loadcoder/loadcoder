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
package com.loadcoder.network.recording;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.network.CodeGeneratable;
import com.loadcoder.utils.FileUtil;

import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.HarReaderException;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarHeader;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HarResponse;
import de.sstoehr.harreader.model.HttpMethod;

public class LoadTestGenerator {

	private static Logger log = LoggerFactory.getLogger(LoadTestGenerator.class);
	static List<String> URL_MATCHERS = Arrays.asList(".*/.*[.]{1}fileextension", ".*/.*[.]{1}fileextension\\?.*");

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

	final private CodeGeneratable reporting;
	final private CodeGeneratable loadBuilders;

	final private List<String> bodyMatchers;

	private LoadTestGenerator(String pathToHARFile, List<String> allowedRULStarts, String javaPackage,
			String destinationJavaCodeDir, String destinationResourceDir, boolean allowOverwritingExistingFiles,
			CodeGeneratable reporting, CodeGeneratable loadBuilders, List<String> bodyMatchers) {
		this.pathToHARFile = pathToHARFile;
		this.javaPackage = javaPackage;
		this.destinationJavaCodeDir = destinationJavaCodeDir;
		this.destinationResourceDir = destinationResourceDir;
		this.allowOverwritingExistingFiles = allowOverwritingExistingFiles;

		this.scenarioDstFile = new File(destinationJavaCodeDir, "ScenarioLogic.java");
		this.testDstFile = new File(destinationJavaCodeDir, "GeneratedLoadTest.java");
		this.threadInstanceFile = new File(destinationJavaCodeDir, "ThreadInstance.java");

		this.matcher = getDefaultMatcher(allowedRULStarts);

		this.reporting = reporting;
		this.loadBuilders = loadBuilders;
		this.bodyMatchers = bodyMatchers;
	}

	protected static String removeBasePathOfUrl(String url) {
		String[] splitted = url.split("://", 2);

		if (splitted.length != 2) {
			return null;
		}
		String urlLeft = splitted[1];
		return urlLeft.replaceFirst("[^/]*", "");
	}

	protected static String getPossibleFilenameExtension(String url) {
		String partToUse = removeBasePathOfUrl(url);
		String[] splitted = partToUse.split("/");
		if (splitted.length == 0) {
			return null;
		}
		partToUse = splitted[splitted.length - 1];

		String[] questionmarkSplit = partToUse.split("\\?");
		partToUse = questionmarkSplit[0];

		String[] dotSplit = partToUse.split("\\.");
		if (dotSplit.length <= 1) {
			return null;
		}

		partToUse = dotSplit[dotSplit.length - 1];
		if (!isStringQualifiedFilenameExtension(partToUse)) {
			return null;
		}

		return partToUse;
	}

	protected static boolean isStringQualifiedFilenameExtension(String partToUse) {
		if (partToUse.length() <= 5 && partToUse.matches("[a-zA-Z0-9]{" + partToUse.length() + "}")) {
			return true;
		}
		return false;
	}

	protected static Matcher getDefaultMatcher(List<String> urlShallStartWithOneOfThese) {
		Matcher m = (url) -> {

			boolean removeUrlBecauseOfWrongStart = removeBecasueOfWrongStartOfUrl(url, urlShallStartWithOneOfThese);
			if (removeUrlBecauseOfWrongStart) {
				return false;
			}

			String possibleFilenameExtension = getPossibleFilenameExtension(url);
			if (possibleFilenameExtension == null) {
				return true;
			}

			boolean result = isFilenameExtensionAKeeper(possibleFilenameExtension);
			return result;
		};
		return m;
	}

	protected static boolean isFilenameExtensionAKeeper(String filenameExtension) {
		boolean result = WEBPAGES_FILTERS.stream().anyMatch(okFilenameExtension -> {
			return okFilenameExtension.equalsIgnoreCase(filenameExtension);
		});
		return result;
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
			log.info(date + " " + url);
		}

	}

	public interface BodyMatcher {
		boolean matchBody(String body);
	}

	public void sortHarEntries(List<HarEntry> entries) {
		entries.sort((a, b) -> a.getStartedDateTime().before(b.getStartedDateTime()) ? -1 : 1);
	}

	/**
	 * Generate a Loadcoder test based from a har file.
	 * 
	 * @param pathToHARFile          the path to where the har file is located. A
	 *                               har file is a file with http requests and
	 *                               response, that can be recorded from various
	 *                               internet browsers and proxy servers.
	 * 
	 * @param acceptableUrlStarts    Whitelist for acceptable starts of urls that
	 *                               should be a part of the generated tests. Most
	 *                               time you don't want to generate a load test
	 *                               containing all the request from the har file.
	 *                               You probably want to filter out some of the
	 *                               requests that isn't a part of the scope for the
	 *                               test.
	 * 
	 * @param javaPackage            the java package as a string value. This value
	 *                               shall correlate with the argument
	 *                               <code>destinationJavaCodeDir</code>
	 * @param destinationJavaCodeDir the destination package to where the load test
	 *                               Java files shall be generated. This shall
	 *                               correlate with the argument for the package
	 *                               <code> destinationJavaCodeDir</code>
	 * @param destinationResourceDir the destination directory to where resource
	 *                               files for the load test shall be generated
	 * @param reporting              CodeTemplateModifier for how to generate the
	 *                               code for performing the
	 *                               storeAndConsumeResultRuntime call
	 * @param loadBuilder            CodeTemplateModifier for how to generate the
	 *                               code for the loadBuilder methods
	 */
	protected static void generate(String pathToHARFile, List<String> acceptableUrlStarts, String javaPackage,
			String destinationJavaCodeDir, String destinationResourceDir, boolean allowOverwritingExistingFiles,
			CodeGeneratable reporting, CodeGeneratable loadBuilder, List<String> bodyMatchers) {
		LoadTestGenerator generator = new LoadTestGenerator(pathToHARFile, acceptableUrlStarts, javaPackage,
				destinationJavaCodeDir, destinationResourceDir, allowOverwritingExistingFiles, reporting, loadBuilder,
				bodyMatchers);
		generator.gen();
	}

	public void gen() {

		Har har = readHar(pathToHARFile);
		List<HarEntry> entries = har.getLog().getEntries();
		List<HarEntry> filteredEntries = new ArrayList<>();
		for (HarEntry entry : entries) {
			String url = entry.getRequest().getUrl();
			entry.getResponse().getContent().getText();
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
		String scenarioLogic = FileUtil.readResourceAsString("/testgeneration_templates/ScenarioLogic.tmp");
		scenarioLogic = scenarioLogic.replace("${package}", javaPackage);

		Map<String, Map<String, List<HarEntry>>> commmonHeaders = findCommonHeaders(entries);
		Map<String, Entry<String, List<HarEntry>>> filtered = removeValueWithLeastOccurences(commmonHeaders);
		List<Entry<String, Entry<String, List<HarEntry>>>> sortedList = sortMap(filtered);
		List<Entry<String, Entry<String, List<HarEntry>>>> wantedList = wantedList(sortedList);

		List<HarEntry> toShareCommonHeaders = getEntrysToShareCommonHeaders(wantedList);
		if (toShareCommonHeaders.size() > 0) {

			List<HarHeader> headers = new ArrayList<HarHeader>();
			for (Entry<String, Entry<String, List<HarEntry>>> e : wantedList) {
				HarHeader h = new HarHeader();
				h.setName(e.getKey());
				h.setValue(e.getValue().getKey());
				headers.add(h);
			}
			String commonHeader = FileUtil.readResourceAsString("/testgeneration_templates/commonHeader.tmp");
			commonHeader = addHeaders(headers, commonHeader, "headers");
			commonHeader = commonHeader.replace("${request_building}", "");
			scenarioLogic = scenarioLogic.replace("${common_header_method}", commonHeader);

		}

		TransactionNameGenerator transactionNameGenerator = new TransactionNameGenerator();
		int requestIterator = 0;
		List<String> headerNames = wantedList.stream().map((e) -> {
			return e.getKey();
		}).collect(Collectors.toList());
		for (HarEntry entry : entries) {
			List<HarHeader> heads = null;
			if (toShareCommonHeaders.contains(entry)) {
				heads = entry.getRequest().getHeaders().stream().filter(header -> {
					return !headerNames.contains(header.getName());
				}).collect(Collectors.toList());
			} else {
				heads = entry.getRequest().getHeaders();
			}

			LoadMethod loadMethod = generateLoadMethod(entry, transactionNameGenerator, requestIterator, resourceDstDir,
					heads);

			scenarioLogic = scenarioLogic.replace("${logic_end}",
					"\t\t" + loadMethod.getMethodName() + "();\n" + "${logic_end}");

			scenarioLogic = scenarioLogic.replace("${scenario_logic_methods}",
					loadMethod.getMethod() + "\n" + "${scenario_logic_methods}");
			requestIterator++;
		}

		scenarioLogic = scenarioLogic.replace("${scenario_logic_methods}", "");
		scenarioLogic = scenarioLogic.replace("${logic_end}", "");

		FileUtil.writeFile(scenarioLogic.getBytes(), scenarioDstFile);
	}

	private List<Entry<String, Entry<String, List<HarEntry>>>> sortMap(
			Map<String, Entry<String, List<HarEntry>>> filtered) {

		List<Entry<String, Entry<String, List<HarEntry>>>> lista = filtered.entrySet().stream()
				.collect(Collectors.toList());
		lista.sort((a, b) -> {
			int maxA = a.getValue().getValue().size();
			int maxB = b.getValue().getValue().size();
			return maxA - maxB;
		});
		return lista;
	}

	private List<Entry<String, Entry<String, List<HarEntry>>>> wantedList(
			List<Entry<String, Entry<String, List<HarEntry>>>> lista) {

		int medianIndex = lista.size() / 2;
		Entry<String, Entry<String, List<HarEntry>>> medianHars = lista.get(medianIndex);
		int targetSize = medianHars.getValue().getValue().size();
		int iterationIndex = medianIndex;

		for (int i = 0; i < lista.size(); i++) {
			if (lista.get(i).getValue().getValue().size() == targetSize) {
				iterationIndex = i;
				break;
			}
		}
		List<Entry<String, Entry<String, List<HarEntry>>>> result = new ArrayList<Entry<String, Entry<String, List<HarEntry>>>>();
		for (int i = iterationIndex; i < lista.size(); i++) {
			result.add(lista.get(i));
		}
		return result;
	}

	private List<HarEntry> getEntrysToShareCommonHeaders(List<Entry<String, Entry<String, List<HarEntry>>>> lista) {

		List<HarEntry> result = lista.get(0).getValue().getValue();
		for (int i = 1; i < lista.size(); i++) {
			Entry<String, Entry<String, List<HarEntry>>> ee = lista.get(i);
			List<HarEntry> toComareWith = ee.getValue().getValue();
			result = result.stream().filter(e -> {
				return toComareWith.contains(e);
			}).collect(Collectors.toList());
		}

		return result;
	}

	private Map<String, Entry<String, List<HarEntry>>> removeValueWithLeastOccurences(
			Map<String, Map<String, List<HarEntry>>> map) {

		Map<String, Entry<String, List<HarEntry>>> result = new HashMap<String, Entry<String, List<HarEntry>>>();
		map.entrySet().stream().forEach(entry -> {
			List<Entry<String, List<HarEntry>>> list = entry.getValue().entrySet().stream()
					.collect(Collectors.toList());
			list.sort((a, b) -> {
				return b.getValue().size() - a.getValue().size();
			});
			result.put(entry.getKey(), list.get(0));

		});

		return result;
	}

	private Map<String, Map<String, List<HarEntry>>> findCommonHeaders(List<HarEntry> entries) {
		Map<String, Map<String, List<HarEntry>>> map = new HashMap<String, Map<String, List<HarEntry>>>();

		entries.stream().forEach(entry -> {
			entry.getRequest().getHeaders().stream().forEach(header -> {
				Map<String, List<HarEntry>> valueHarEntry = map.get(header.getName());
				if (valueHarEntry == null) {
					valueHarEntry = new HashMap<String, List<HarEntry>>();
					map.put(header.getName(), valueHarEntry);
				}
				List<HarEntry> list = valueHarEntry.get(header.getValue());
				if (list == null) {
					list = new ArrayList<HarEntry>();
					valueHarEntry.put(header.getValue(), list);
				}
				list.add(entry);
			});
		});
		return map;
	}

	private Map<String, Map<String, List<HarEntry>>> findCommonHeaders_old(List<HarEntry> entries) {
		Map<String, Map<String, List<HarEntry>>> map = new HashMap<String, Map<String, List<HarEntry>>>();

		entries.stream().forEach(entry -> {
			entry.getRequest().getHeaders().stream().forEach(header -> {
				Map<String, List<HarEntry>> valueHarEntry = map.get(header.getName());
				if (valueHarEntry == null) {
					valueHarEntry = new HashMap<String, List<HarEntry>>();
					map.put(header.getName(), valueHarEntry);
				}
				Map<String, List<HarEntry>> harMap = map.get(header.getName());
				List<HarEntry> list = harMap.get(header.getValue());
				if (list == null) {
					list = new ArrayList<HarEntry>();
					harMap.put(header.getValue(), list);
				}
				list.add(entry);
			});
		});
		return map;
	}

	public void generateTest(File dstDir) {
		String testContent = FileUtil.readResourceAsString("/testgeneration_templates/GeneratedLoadTest.tmp");

		testContent = testContent.replace("${package}", javaPackage);

		if (reporting != null) {
			testContent = reporting.generateCode(testContent);
		} else {
			String storeResultsRuntime = FileUtil
					.readResourceAsString("/testgeneration_templates/storeResultsRuntime.tmp");
			testContent = testContent.replace("${storeAndConsumeResultRuntime}", storeResultsRuntime);

		}
		testContent = loadBuilders.generateCode(testContent);

		testContent = testContent.replace("${importList}", "");
		FileUtil.writeFile(testContent.getBytes(), testDstFile);
	}

	public void generateThreadInstance(File dstDir) {

		String threadInstanceContent = FileUtil.readResourceAsString("/testgeneration_templates/ThreadInstance.tmp");

		threadInstanceContent = threadInstanceContent.replace("${package}", javaPackage);

		FileUtil.writeFile(threadInstanceContent.getBytes(), threadInstanceFile);
	}

	HarHeader getContentTypeHeader(List<HarHeader> headers) {
		for (HarHeader header : headers) {
			if (header.getName().equalsIgnoreCase("content-type")) {
				return header;
			}
		}
		return null;
	}

	List<String> uniqueLoadMethodNames = new ArrayList<>();

	private String createUniqueMethodName(String transactionName) {

		String suggestedMethodName = transactionName;
		int uniqueModifier = 1;
		while (uniqueLoadMethodNames.contains(suggestedMethodName)) {
			suggestedMethodName = transactionName + "_" + uniqueModifier++;
		}
		uniqueLoadMethodNames.add(suggestedMethodName);
		return suggestedMethodName;
	}

	public LoadMethod generateLoadMethod(HarEntry entry, TransactionNameGenerator transactionNameGenerator,
			int transactionIterator, File resourceDstDir, List<HarHeader> heads) {
		String loadMethodTemplate = FileUtil.readResourceAsString("/testgeneration_templates/loadmethod.tmp");

		String transactionName = transactionNameGenerator.generateTransactionName(entry, 1, 20);
		String methodName = createUniqueMethodName(transactionName);
		String loadMethod = loadMethodTemplate;
		loadMethod = loadMethod.replace("${transaction_name}", transactionName);
		loadMethod = loadMethod.replace("${method_name}", methodName);

		HarRequest req = entry.getRequest();

		loadMethod = loadMethod.replace("${transaction_url}", req.getUrl());

		loadMethod = loadMethod.replace("${request_variable}", "request" + transactionIterator);

		String headerVariable = "headers" + transactionIterator;
		loadMethod = loadMethod.replace("${header_variable}", headerVariable);

		HarHeader contentType = getContentTypeHeader(heads);

		loadMethod = addHeaders(heads, loadMethod, headerVariable);
		if (entry.getRequest().getHeaders().size() != heads.size()) {
			loadMethod = loadMethod.replace("${request_building}",
					"		addCommonHeaders(" + headerVariable + ");" + "${request_building}");

		}

		String requestBodyTemplate = "";
		String body = entry.getRequest().getPostData().getText();
		boolean methodIsGET = entry.getRequest().getMethod().equals(HttpMethod.GET);
		boolean hasBody = body != null && !body.isEmpty();
		String requestBodyVariable = "reqBody" + transactionIterator;
		if (hasBody) {
			String fileName = "body" + transactionIterator + ".txt";
			File bodyFile = new File(resourceDstDir, fileName);
			FileUtil.writeFile(body.getBytes(), bodyFile);
			requestBodyTemplate = requestBodyTemplate.replace("${requestbody_variable}", requestBodyVariable);
			loadMethod = loadMethod.replace("${body}",
					"getFileContent(\"" + destinationResourceDir + "/" + fileName + "\")");
		} else {
			loadMethod = loadMethod.replace("${body}", "\"\"");
			if (!methodIsGET) {
				requestBodyTemplate = FileUtil
						.readResourceAsString("/testgeneration_templates/getEmptyRequestBody.tmp");
				requestBodyTemplate = requestBodyTemplate.replace("${requestbody_variable}", requestBodyVariable);
			}
		}

		loadMethod = loadMethod.replace("${request_body}", requestBodyTemplate);
		loadMethod = loadMethod.replace("${request_http_verb}", entry.getRequest().getMethod().name());

		loadMethod = loadMethod.replace("${request_building}", "");

		HarResponse resp = entry.getResponse();
		loadMethod = loadMethod.replace("${expected_http_code}", "" + resp.getStatus());

		String responseBody = entry.getResponse().getContent().getText();
		if (responseBody != null && !responseBody.isEmpty()) {

			String resultHandlerAssertionTemplate = FileUtil
					.readResourceAsString("/testgeneration_templates/resulthandler_assert.tmp");

			if (bodyMatchers != null) {
				for (String matcher : bodyMatchers) {
					if (responseBody.contains(matcher)) {
						String resultHandlerAssertion = resultHandlerAssertionTemplate.replace("${expected_body_part}",
								matcher);

						loadMethod = loadMethod.replace("${result_asserts}",
								"\n" + resultHandlerAssertion + "${result_asserts}");
					}
				}
			}
		} else {
			loadMethod = loadMethod.replace("${handleResultReadResponse}", "");

		}
		loadMethod = loadMethod.replace("${result_asserts}", "");

		LoadMethod method = new LoadMethod(methodName, loadMethod);
		return method;
	}

	private String addHeaders(List<HarHeader> headers, String loadMethod, String headerVariable) {
		String headerTemplate = FileUtil.readResourceAsString("/testgeneration_templates/addheader.tmp");
		headerTemplate = headerTemplate.replace("${header_variable}", headerVariable);

		for (HarHeader header : headers) {

			if (isHeaderNameSPDY(header.getName())) {
				continue;
			}
			String headerValue = header.getValue();
			headerValue = headerValue.replaceAll("[\"]", "\\\\\"");
			String h = headerTemplate.replace("${header_key}", header.getName()).replace("${header_value}",
					headerValue);
			loadMethod = loadMethod.replace("${request_building}", "		" + h + "${request_building}");

		}
		return loadMethod;
	}

	private boolean isHeaderNameSPDY(String headerName) {
		return headerName.startsWith(":");
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
			return original.replaceAll("[^a-zA-Z0-9]*", "");
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
			result = limitTransactionName(result, maxTransactionNameLength);

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

		protected String limitTransactionName(String result, int maxTransactionNameLength) {
			if (result.length() > maxTransactionNameLength) {
				result = result.substring(0, maxTransactionNameLength);
			}
			return result;
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

	public static String generateCodeLoadBuilder(String originalCode, long durationMilliseconds, int amountOfThreads,
			int callsPerSecond) {
		String result = originalCode;
		String loadBuilderMethods = FileUtil.readResourceAsString("/testgeneration_templates/loadBuilderMethods.tmp");

		if (amountOfThreads != -1) {
			int durationSeconds = (int) (durationMilliseconds / 1000);
			loadBuilderMethods = loadBuilderMethods.replace("${threads}", "" + amountOfThreads);
			loadBuilderMethods = loadBuilderMethods.replace("${duration}", "" + durationSeconds);
			loadBuilderMethods = loadBuilderMethods.replace("${throttle}", "" + callsPerSecond);
		} else {
			loadBuilderMethods = "";
		}

		result = result.replace("${loadBuilder}", loadBuilderMethods);

		return result;
	}

}
