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
import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.loadcoder.load.LoadUtility;
import com.loadcoder.load.scenario.LoadScenario;
import com.loadcoder.network.loadship.LoadshipClient;
import com.loadcoder.network.spring.SpringHttpClient;
import com.loadcoder.result.Result;
import com.loadcoder.utils.FileUtil;
import static com.loadcoder.statics.LogbackLogging.*;


public class LoadTest {
	public static void main(String[] args){
		File logDir = getNewLogDir("target", "loadTest");
		
		setResultDestination(logDir);
		
		LoadScenario s = new LoadScenario() {
			
			@Override
			public void loadScenario() {
				load("foo", ()->{}).perform();
			}
		};
		
		Result r = s.test();
		System.out.println(r.getAmountOfTransactions());
		
		SpringHttpClient client = new SpringHttpClient();
		ResponseEntity<String> resp = client.http("http://172.17.0.1:6210/loadship/status");

		LoadUtility.sleep(500);
		File f = getLatestResultFile("target", "loadTest");
		String logFileContent = FileUtil.readFile(f);
		System.out.println(logFileContent);
		
		LoadshipClient.postFile("http://172.17.0.1:6210", "loadship/" + logDir.getName(), "testfile.txt", "content");
		String content = LoadshipClient.getFile("http://172.17.0.1:6210", "loadship/" + logDir.getName(), "testfile.txt");
		System.out.println(content);
	}
	}
