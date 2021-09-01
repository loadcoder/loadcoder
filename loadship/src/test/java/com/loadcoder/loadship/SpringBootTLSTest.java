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
package com.loadcoder.loadship;


//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.loadcoder.loadship.utils.ChecksumUtil;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Loadship.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringBootTLSTest {

	@LocalServerPort
	private int port;

    @Test
    public void willStatusRespondWith200OK() {
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/octet-stream;UTF-8");
            String bodyToBeSent = "the body";
            String checksum = ChecksumUtil.md5(bodyToBeSent.getBytes());
            HttpEntity<String> req = new HttpEntity<>(bodyToBeSent, headers);
            ResponseEntity<String>resp = restTemplate.exchange("http://localhost:"+port+"/loadship/status", HttpMethod.GET, req, String.class);
            assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    public void testRetrieveStudentCourse() {
            
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-type", "application/octet-stream;UTF-8");
            String bodyToBeSent = "the body";
            String checksum = ChecksumUtil.md5(bodyToBeSent.getBytes());
            HttpEntity<String> req = new HttpEntity<>(bodyToBeSent, headers);

            ResponseEntity<String> resp = restTemplate.exchange("http://localhost:"+port+"/loadship/data", HttpMethod.POST, req, String.class);
            assertEquals(200, resp.getStatusCodeValue());
            ResponseEntity<byte[]>resp2 = restTemplate.exchange("http://localhost:"+port+"/loadship/data?checksum=" + checksum, HttpMethod.GET, req, byte[].class);
            byte[] respBodyBytes = resp2.getBody();
            String respBody = new String(respBodyBytes);
            assertEquals(bodyToBeSent, respBody);
    }

}