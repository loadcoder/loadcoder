/*******************************************************************************
 * Copyright (C) 2021 Team Loadcoder
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
import java.util.List;
import java.util.Map;

import com.loadcoder.utils.FileUtil;

public class ReadResource {
	public static void main(String[] args) {
		checkThatAssertIsEnabled();

		Map<String, String> map = FileUtil.readAllResourceFilesInResourceDir("/resourcedir");
		assert map.get("/resourcedir/testdata.txt") != null;
		assert map.get("/resourcedir/testdata.txt").contains("1\n2\n3");
		Map<String, String> map2 = FileUtil.readAllResourceFilesInResourceDir("/resourcedir2");
		assert map2.get("/resourcedir2/testdata2.txt") != null;
		assert map2.get("/resourcedir2/testdata2.txt").contains("4\n5\n6");

		String content = FileUtil.readResourceAsString("/resourcedir/testdata.txt");
		assert content.contains("1\n2\n3");
		String content2 = FileUtil.readResourceAsString("/resourcedir2/testdata2.txt");
		assert content2.contains("4\n5\n6");

		List<String> content3 = FileUtil.readResourceAsLines("/resourcedir/testdata.txt");
		assert content3.size() == 3;
		assert content3.get(0).equals("1");
		List<String> content4 = FileUtil.readResourceAsLines("/resourcedir2/testdata2.txt");
		assert content4.size() == 3;
		assert content4.get(0).equals("4");

	}

	static void checkThatAssertIsEnabled() {
		try {
			assert 1 == 2;
			throw new RuntimeException("Java assertions does not seem to be enabled. Enable it with jvm arg -ea");
		} catch (AssertionError ea) {
		}
	}
}
