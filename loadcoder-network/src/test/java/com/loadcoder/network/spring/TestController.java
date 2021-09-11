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


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loadcoder.load.LoadUtility;

@RestController
@RequestMapping(value = "/test")
public class TestController {

	public TestController() {
	}
    
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public String getCustomer(@RequestParam String email) {
    	return "hello " + email;
    }
    
    @RequestMapping(value = "/delay", method = RequestMethod.GET)
    public void delay(@RequestParam int delay) {
    	LoadUtility.sleep(delay);
    }
    
}