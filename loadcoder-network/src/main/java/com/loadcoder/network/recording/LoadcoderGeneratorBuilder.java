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
package com.loadcoder.network.recording;

import java.util.List;

import com.loadcoder.network.CodeGeneratable;

public class LoadcoderGeneratorBuilder {

	private final String pathToHARFile;

	private final List<String> urlBeginningsWhiteList;
	private final String javaPackage;
	private final String packagePath;
	private final String resourcesPath;

	private CodeGeneratable reporting;
	private long executionDurationMillis = -1;
	private int amountOfThreads = -1;
	private int callsPerSecond = -1;
	private List<String> expectedResponseBodyParts;

	private boolean allowCodeOverWriting = false;

	/**
	 * Create the generator Builder with this constructor. Call the
	 * <code>generate</code> method when the builder is ready to generate the load
	 * test.
	 * 
	 * @param pathToHARFile the path to where the HAR file exists.
	 * @param urlBeginningsWhiteList list of beginnings of URLs that the generator will create transactions for.
	 * If the value is null, all urls will be accepted to use for transaction call generations
	 * @param javaPackage the java package where the Java code shall be created, for instance <code>com.company.loadtests</code>.
	 * This value shall correlate with the value for argument <code>packagePath</code>.
	 * @param packagePath the path to where the java the Java code shall be created,
	 * for instance <code>src/test/java/com/company/loadtests</code>. This value shall correlate with the value
	 * for argument <code>javaPackage</code>.
	 * @param resourcesPath the path where to resources shall be created. Typically for Maven projects, this would
	 * be directory src/test/resources/my-loadtest. The 
	 */
	public LoadcoderGeneratorBuilder(String pathToHARFile, List<String> urlBeginningsWhiteList, String javaPackage,
			String packagePath, String resourcesPath) {
		this.pathToHARFile = pathToHARFile;
		this.urlBeginningsWhiteList = urlBeginningsWhiteList;
		this.javaPackage = javaPackage;
		this.packagePath = packagePath;
		this.resourcesPath = resourcesPath;
	}

	public void generate() {
		LoadTestGenerator.generate(pathToHARFile, urlBeginningsWhiteList, javaPackage, packagePath, resourcesPath,
				allowCodeOverWriting, reporting, codeTemplate -> LoadTestGenerator.generateCodeLoadBuilder(codeTemplate,
						executionDurationMillis, amountOfThreads, callsPerSecond),
				expectedResponseBodyParts);
	}

	/**
	 * Set a load definition. This will generate the builder method code for the
	 * LoadBuilder object. The generated code will look something like
	 * this:<code><br>
	 * 			.amountOfThreads(3)<br>
	 *			.stopDecision(duration(120 * SECOND))<br>
	 *			.throttle(5, PER_SECOND, SHARED)
	 *			</code>
	 * 
	 * @param executionDurationMillis duration of the generated test execution
	 * @param amountOfThreads         the amount of threads the generated test will
	 *                                use
	 * @param callsPerSecond          calls per second the generated test will
	 *                                perform
	 * @return this builder instance
	 */
	public LoadcoderGeneratorBuilder load(long executionDurationMillis, int amountOfThreads, int callsPerSecond) {
		this.executionDurationMillis = executionDurationMillis;
		this.amountOfThreads = amountOfThreads;
		this.callsPerSecond = callsPerSecond;
		return this;
	}

	/**
	 * Sets a CodeGeneratable to modify the code for Loadbuilder method
	 * <code>storeAndConsumeResultRuntime</code>
	 * 
	 * @param codeModifier That takes the unchanged code as argument, and returns an
	 *                     updated version
	 * @return this builder instance
	 */
	public LoadcoderGeneratorBuilder sendResultTo(CodeGeneratable codeModifier) {
		this.reporting = codeModifier;
		return this;
	}

	/**
	 * Sets a list of expected parts of response bodies. These strings needs to
	 * exist as a part of any HTTP response body in the used HAR file. For every
	 * hit, the corresponding transaction will get a generated check in the
	 * <code>handleResult</code> method that will verify whether or not the body
	 * contains the expected body parts.
	 * 
	 * @param expectedResponseBodyParts the list of body part that shall be expected
	 *                                  as a part of one or more response bodies
	 *                                  during the load test.
	 * @return this builder instance
	 */
	public LoadcoderGeneratorBuilder checkResponseBodiesContaining(List<String> expectedResponseBodyParts) {
		this.expectedResponseBodyParts = expectedResponseBodyParts;
		return this;
	}

	/**
	 * Allow to overwrite already existing Java classes. If this method is not used,
	 * file name will throw a RuntimeException during generation if there are
	 * colliding files 
	 * @return this builder instance
	 */
	public LoadcoderGeneratorBuilder allowCodeOverWriting() {
		this.allowCodeOverWriting = true;
		return this;
	}

}
