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
package com.loadcoder.load.result;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

public class ValueHolderTest {

	@Test
	public void testRounding() {
		ValueHolder v = new ValueHolder(4.39, d -> d.asDecimalString());
		String s = v.getConverter().convert(v);
		assertEquals(s, "4.39");
		
		v = new ValueHolder(4.39, d -> d.noDecimals());
		s = v.getConverter().convert(v);
		assertEquals(s, "4");
		
		v = new ValueHolder(4.51, d -> d.noDecimals());
		s = v.getConverter().convert(v);
		assertEquals(s, "5");
		
		v = new ValueHolder(4.513, d -> d.asDecimalString());
		v.useRoundedValue(3);
		s = v.getConverter().convert(v);
		assertEquals(s, "4.513");

		v = new ValueHolder(4.513, d -> d.asDecimalString());
		v.useRoundedValue(4);
		s = v.getConverter().convert(v);
		assertEquals(s, "4.513");
		
		v = new ValueHolder(4.516, d -> d.asDecimalString());
		v.useRoundedValue(2);
		s = v.getConverter().convert(v);
		assertEquals(s, "4.52");
		
		
	}
}
