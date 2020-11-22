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
package com.loadcoder.statics;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.loadcoder.load.LoadUtility;

public class Configuration {
	
	private static Logger log = LoggerFactory.getLogger(Configuration.class);

	final ConfigHolder configHolder;
	private static Configuration config = new Configuration();
	
	public Configuration() {
		this(new ConfigHolder());
	}

	protected Configuration(ConfigHolder configHolder) {
		this.configHolder = configHolder;
	}
	
	public static Configuration getConfigurationInstance() {
		return config;
	}
	
	protected static class ConfigHolder{
		final Map<String, String> config;
		
		protected ConfigHolder(Map<String, String> config) {
			this.config = config;
		}
		
		protected ConfigHolder() {
			this.config = generationConfiguration();
		}
		
		Map<String, String> getConfig(){
			return config;
		}
		
		private Map<String, String> generationConfiguration(){
			Map<String, String> configuration = readConfigurationFile();
			populateMapWithLoadcoderEnvironments(configuration);
			return configuration;
		}
		
		File getFileFromResourceOrPath(String propertyFilePath) {
			File resourceFile = null;
			URL resourceURL = Statics.class.getClassLoader().getResource(propertyFilePath);
			if (resourceURL != null) {
				resourceFile = new File(resourceURL.getFile());
			}
			File pathFile = new File(propertyFilePath);

			File chosenFile;
			if (resourceFile != null && resourceFile.exists()) {
				chosenFile = resourceFile;
			} else if (pathFile.exists()) {
				chosenFile = pathFile;
			}else {
				throw new RuntimeException("Tried to load configuration file " + propertyFilePath + "but it could not be found."
						+ "The path to this file is configurable with jvm arg -Dconfiguration=<PATH TO CONFIG FILE>");
			}
			return chosenFile;
			
		}
		private Map<String, String> readConfigurationFile() {
			Map<String, String> result = new HashMap<String, String>();

			String propertyFilePath = System.getProperty("loadcoder.configuration");
			if (propertyFilePath == null || propertyFilePath.isEmpty()) {
				propertyFilePath = "loadcoder.conf";
			}
			
			File chosenFile = getFileFromResourceOrPath(propertyFilePath);

			log.info("Will read configuration from file {}", chosenFile);
			List<String> lines;
			try {
				lines = LoadUtility.readFile(chosenFile);
			} catch (IOException e) {
				throw new RuntimeException("Found the file" + propertyFilePath + " but it could not be read", e);
			}

			lines.stream().forEach(line -> {
				String[] splitted = line.split("[ ]*=[ ]*");
				if (splitted.length == 2) {
					result.put(splitted[0], splitted[1]);
				}
			});
			return result;
		}
	}
	
	protected ConfigHolder getConfigHolder() {
		return configHolder;
	}
	public Map<String, String> getConfiguration() {
		return configHolder.getConfig();
	}
	
	public static String getConfig(String key) {
		return config.getConfigHolder().getConfig().get(key);
	}
	
	public String getConfiguration(String key) {
		return getConfigHolder().getConfig().get(key);
	}
	
	public String getConfiguration(String key, String defaultValue) {
		String valueFromConfig = getConfigHolder().getConfig().get(key);
		String result = valueFromConfig == null ? defaultValue : valueFromConfig;
		return result;
	}
	
	public Map<String, String> getMatchingConfig(String keyMatchingRegexp) {
		Map<String, String> m = getConfigHolder().getConfig().entrySet().stream().filter(entry -> {
			boolean result = entry.getKey().matches(keyMatchingRegexp);
			return result;
			})
		.collect(Collectors.toMap(Entry<String, String>::getKey, Entry<String, String>::getValue));
		
		return m;
	}
	


	private static void populateMapWithLoadcoderEnvironments(Map<String, String> destination) {

		Map<String, String> envs = System.getenv();
		envs.entrySet().stream().forEach(entry ->{
			log.info(entry.getKey() + ":" + entry.getValue());
		});
		populateMapWithLoadcoderParameters(envs, destination);
	}
	
	private static void populateMapWithLoadcoderParameters(Map<String, String> source, Map<String, String> destination) {
		
		log.debug("loadcoder environment variables (matching LOADCODER_.*");
		source.entrySet().stream().forEach(entry->{
			if(entry.getKey().matches("LOADCODER_.*")){
				log.debug(entry.getKey() + ":" +entry.getValue());
				destination.put(entry.getKey(), entry.getValue());
			}
		});
		
	}

}
