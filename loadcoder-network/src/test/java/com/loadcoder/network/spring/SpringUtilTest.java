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
package com.loadcoder.network.spring;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.loadcoder.load.scenario.ResultModel;

//@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringUtilTest {

	public class ResultModelExtension <R> extends ResultModel<R>{

		public ResultModelExtension(String transactionName) {
			super(transactionName);
		}
		
		public void setResp(R r) {
			super.setResp(r);
		}
	}
	
	@Test
	public void handleResult() {
		
		ResponseEntity<String> resp = mock(ResponseEntity.class);
		
		when(resp.getStatusCodeValue()).thenReturn(201);
		
		ResultModelExtension<ResponseEntity<String>> r = new ResultModelExtension<>("foo");
		r.setResp(resp);
		SpringHttpClient.check(r, 100);
	}
	
}
