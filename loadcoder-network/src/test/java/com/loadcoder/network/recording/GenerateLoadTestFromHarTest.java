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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.network.recording.LoadTestGenerator;
import com.loadcoder.network.recording.Matcher;
import com.loadcoder.network.recording.LoadTestGenerator.TransactionNameGenerator;

import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;

public class GenerateLoadTestFromHarTest extends TestNGBase {

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

	}

	@Test
	public void defaultBlackListTest() {
		Matcher m = LoadTestGenerator.getDefaultMatcher(null);

		assertTrue(m.keep("https://www.foo.com/foo/bar/"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/index.html"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php?hello=bar"));
		assertTrue(m.keep("https://www.foo.com/foo/bar/foo.php?h.e-l_lo.1=b-a_r1.2"));

		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js"));
		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js?hello=bar"));
		assertFalse(m.keep("https://www.foo.com/foo/bar/foo.js?h.e-l_lo.1=b-a_r1.2"));
	}

}
