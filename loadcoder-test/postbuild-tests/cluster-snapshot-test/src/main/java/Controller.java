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

import org.springframework.http.ResponseEntity;

import com.loadcoder.cluster.clients.docker.LoadcoderCluster;
import com.loadcoder.cluster.clients.docker.MasterContainers;
import com.loadcoder.cluster.util.ZipUtil;
import com.loadcoder.cluster.util.ZipUtil.ZipBuilderFileAdder.ZipDefinition;
import com.loadcoder.load.LoadUtility;
import com.loadcoder.network.spring.SpringHttpClient;

public class Controller {

	public static void main(String[] args){
		LoadcoderCluster cluster = new LoadcoderCluster();
		
		cluster.setupMaster(MasterContainers.LOADSHIP);
		
		System.out.println("wait until Loadship is up......");
		
		SpringHttpClient client = new SpringHttpClient();
		for(int i =0; i<10; i++) {
			try{
				ResponseEntity<String> result = client.http("http://172.17.0.1:6210/loadship/status");
				if(result.getStatusCodeValue() == 200) {
					break;
				}
				
			}catch(RuntimeException e) {
				LoadUtility.sleep(2000);
				continue;
			}
			
		}
		
		String projectAbsolutePath = "";
		if(args.length > 0) {
			projectAbsolutePath = args[0];
		}
		File f = new File(projectAbsolutePath).getAbsoluteFile();
		for(int i=0; i<3; i++) {
			f = f.getParentFile();
		}
		
		System.out.println("Loadcoder root dir:" + f.getAbsolutePath());
		
		ZipDefinition de = ZipUtil.zipBuilder(f.getAbsolutePath())
		.whitelist()
		.addFile("loadcoder-.*/target/.*1.0.0-SNAPSHOT.jar")
		.addFile("loadcoder-test/postbuild-tests/cluster-snapshot-test")
		.changeFilePathInZip((relPath, file) -> file.getName().matches("loadcoder.*.jar")  ? "loadcoder/" + file.getName() : relPath)
		.changeFilePathInZip((relPath, file) -> relPath.contains("cluster-snapshot-test") ? relPath.split("cluster-snapshot-test", 2)[1] : relPath)
		.build();
				byte[] bytes = de.zipToBytes();
		
		cluster.uploadTest(bytes);
		cluster.startNewExecution(1);
	}
}