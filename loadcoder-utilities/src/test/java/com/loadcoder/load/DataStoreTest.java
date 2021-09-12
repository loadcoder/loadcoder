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
package com.loadcoder.load;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.jayway.jsonpath.JsonPath;
import com.loadcoder.DataStore;
import com.loadcoder.utils.FileUtil;

public class DataStoreTest {

	@Test
	public void testDataStore() {
		DataStore<String> testdata = new DataStore<>(getListFromFile());
		String a = testdata.remove();
		Assert.assertEquals(a, "leo");
	}

	private List<String> getListFromFile() {
		String fileContent = FileUtil.readResourceAsString("/utilities/testdata.json");
		return JsonPath.read(fileContent, "customers[*].name");
	}

	private List<String> getListFromMySQL() {

		List<String> result = readCustomer(getConnection());
		
		return result;
	}

	private Connection getConnection() {
		String connectionUrl = "jdbc:mysql://localhost:3306/loadtest?serverTimezone=UTC";
		String mysqlUser = "";
		String mysqlPwd = "";
		try {
			return DriverManager.getConnection(connectionUrl, mysqlUser, mysqlPwd );
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> readCustomer(Connection conn) {
		String sqlSelectAllPersons = "SELECT * FROM customer";
		List<String> result = new ArrayList<String>();
		try (PreparedStatement ps = conn.prepareStatement(sqlSelectAllPersons); ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String name = rs.getString("NAME");
				result.add(name);
			}

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return result;
	}
}
