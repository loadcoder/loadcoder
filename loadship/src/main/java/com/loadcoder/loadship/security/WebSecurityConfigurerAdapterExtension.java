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
package com.loadcoder.loadship.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

	@EnableWebSecurity
	public class WebSecurityConfigurerAdapterExtension extends WebSecurityConfigurerAdapter {
	 
		@Override
		protected void configure(HttpSecurity http) throws Exception {
			http.cors().and().csrf().disable();
		}
	 
		/*
		 * Create an in-memory authentication manager. We create 1 user (localhost which
		 * is the CN of the client certificate) which has a role of USER.
		 */
		@Override
		protected void configure(AuthenticationManagerBuilder auth) throws Exception {
			auth.inMemoryAuthentication().withUser("localhost").password("none").roles("USER");
		}
		
	    @Bean
	    public UserDetailsService userDetailsService() {
	        return (UserDetailsService) username -> {
	        	if(1==1) {
	        		return new User(username, "",
	                        AuthorityUtils
	                                .commaSeparatedStringToAuthorityList("ROLE_USER"));
	            } else {
	                throw new UsernameNotFoundException(String.format("User %s not found", username));
	            }
	        };
	    }
	}