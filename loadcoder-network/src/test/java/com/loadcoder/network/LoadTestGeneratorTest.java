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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.network.LoadTestGenerator.TransactionNameGenerator;

import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;

public class LoadTestGeneratorTest extends TestNGBase {

	Logger log = LoggerFactory.getLogger(LoadTestGeneratorTest.class);
	@Test
	public void printAllUrlsTest() {
		LoadTestGenerator.printAllURLs("src/test/resources/loadcoder.har");
	}

	@Test
	public void testMakeTransactionFriendlyString() {
		TransactionNameGenerator gen = new TransactionNameGenerator();

		assertEquals(gen.makeTransactionFriendlyString("a%b"), "ab");
		assertEquals(gen.makeTransactionFriendlyString("a% b.c@d}e"), "abcde");
		assertEquals(gen.makeTransactionFriendlyString("a b+c"), "abc");
	}

	HarEntry getTestHarEntry(String url) {

		String[] queries = url.split("[?]");
		List<HarQueryParam> params = new ArrayList<>();
		if (queries.length > 1) {
			queries = queries[1].split("&");
			for (String query : queries) {
				HarQueryParam param = new HarQueryParam();
				String[] p = query.split("=");
				param.setName(p[0]);
				param.setValue(p[1]);
				params.add(param);
			}

		}
		HarEntry entry = new HarEntry();
		entry.setRequest(new HarRequest());
		entry.getRequest().setMethod(HttpMethod.GET);
		entry.getRequest().setUrl(url);
		entry.getRequest().setQueryString(params);
		return entry;
	}

	@Test
	public void testGenerateTransactionName() {

		TransactionNameGenerator generator = new TransactionNameGenerator();
		HarEntry entry = getTestHarEntry("https://a.com/b_1/c_2?foo=bar");
		assertEquals(generator.generateTransactionName(entry, 1, 20), "GET_c2_foo");
		assertEquals(generator.generateTransactionName(entry, 1, 20), "GET_c2_foo");

		entry = getTestHarEntry("https://b.com/b_1/c_2?foo=bar");
		assertEquals(generator.generateTransactionName(entry, 1, 20), "GET_c2_foo_1");

		entry = getTestHarEntry("https://b.com/b_1/c_2?hello=bar");
		assertEquals(generator.generateTransactionName(entry, 1, 20), "GET_c2_hello");

		entry = getTestHarEntry("https://www2.x.com/b/cart~checkout.bundle.js");
		String transactionName = generator.generateTransactionName(entry, 1, 20);
		assertEquals(transactionName, "GET_cartcheckoutbund");

	}

	@Test
	public void testFriendlyNames() {
		TransactionNameGenerator generator = new TransactionNameGenerator();
		String friendlyName = generator.makeTransactionFriendlyString("vendors~account~cart~checkout.bundle.js");
		log.info(friendlyName);
	}

	@Test
	public void testTransactionNameLengthDelimiter() {
		TransactionNameGenerator generator = new TransactionNameGenerator();
		String friendlyName = generator.limitTransactionName("abc1234567890", 5);
		assertEquals(friendlyName.length(), 5);
	}

	@Test
	public void testGetPossibleFilenameExtension() {
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/a.js"), "js");
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/a.js?foo=bar"), "js");
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/a.min.js?foo=bar"), "js");
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/a.min.js?foo=bar.jpg"), "js");
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/a.min.js?foo=bar.jpg?foo=1"), "js");

		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/bar/"), null);
		assertEquals(LoadTestGenerator.getPossibleFilenameExtension("http://foo/bar.somethingreallylong"), null);
	}

	@Test
	public void testIsStringQualifiedFilenameExtension() {
		assertTrue(LoadTestGenerator.isStringQualifiedFilenameExtension("js"));
		assertTrue(LoadTestGenerator.isStringQualifiedFilenameExtension("JS"));
		assertTrue(LoadTestGenerator.isStringQualifiedFilenameExtension("Js"));
		assertTrue(LoadTestGenerator.isStringQualifiedFilenameExtension("woff2"));

		assertFalse(LoadTestGenerator.isStringQualifiedFilenameExtension("woffwoff"));
		assertFalse(LoadTestGenerator.isStringQualifiedFilenameExtension("js-2"));

	}

	@Test
	public void testIsFilenameExtensionAKeeper() {
		assertTrue(LoadTestGenerator.isFilenameExtensionAKeeper("html"));
		assertTrue(LoadTestGenerator.isFilenameExtensionAKeeper("HTML"));
		assertTrue(LoadTestGenerator.isFilenameExtensionAKeeper("Html"));
		assertFalse(LoadTestGenerator.isFilenameExtensionAKeeper("jpg"));

	}

	@Test
	public void testRemoveBasePathOfUrl() {
		assertEquals(LoadTestGenerator.removeBasePathOfUrl("http://foo.com/bar"), "/bar");
		assertEquals(LoadTestGenerator.removeBasePathOfUrl("http://foo.com/"), "/");
		assertEquals(LoadTestGenerator.removeBasePathOfUrl("http://foo.com"), "");
		assertEquals(LoadTestGenerator.removeBasePathOfUrl("HTTP://foo.com"), "");
		assertEquals(LoadTestGenerator.removeBasePathOfUrl("Http://foo.com"), "");

	}

	@Test
	public void defaultBlackListTest() {
		Matcher m = LoadTestGenerator.getDefaultMatcher(null);

		assertTrue(m.keep("https://www.foo.com"));
		assertTrue(m.keep("https://www.foo.com/"));
		assertTrue(m.keep("https://www.foo.com/bar"));
		assertTrue(m.keep("https://www.foo.com/bar?foo=bar"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/index.html"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php?hello=bar"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php?h.e-l_lo.1=b-a_r1.2"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.manifest"));

		assertFalse(m.keep("https://www2.b.com/a/cart~checkout.bundle.js"));
		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js"));
		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js?hello=bar"));
		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js?h.e-l_lo.1=b-a_r1.2"));
	}
}
