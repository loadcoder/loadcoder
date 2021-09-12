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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;

import com.loadcoder.network.recording.LoadTestGenerator.TransactionNameGenerator;

import de.sstoehr.harreader.model.HarEntry;
import de.sstoehr.harreader.model.HarQueryParam;
import de.sstoehr.harreader.model.HarRequest;
import de.sstoehr.harreader.model.HttpMethod;

public class GenerateLoadTestFromHarTest {

	

	
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
