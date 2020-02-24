package com.loadcoder.statics;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class StaticsTest {

	@Test
	public void testConfiguration() {
		String b = Statics.getConfiguration("a");
		assertEquals(b, "b");
	
		String d = Statics.getConfiguration("c");
		assertEquals(d, "d");
		
		String f = Statics.getConfiguration("e");
		assertEquals(f, "f");
		
		String h = Statics.getConfiguration("g");
		assertEquals(h, "h");
		
	}
	
}
