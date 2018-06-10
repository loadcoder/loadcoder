package com.loadcoder.statics;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import static com.loadcoder.statics.LogbackLogging.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.loadcoder.load.TestUtility;
import com.loadcoder.load.testng.TestNGBase;
import com.loadcoder.result.Logs;

import junit.framework.Assert;


public class LogbackLoggingTest extends TestNGBase{

	Logger resultLog = LoggerFactory.getLogger(Logs.class);
	Logger infoLog = LoggerFactory.getLogger(this.getClass());
			
	@Test
	public void setResultDirectoryTest(Method method) {
		String sharedDirForLogsPath = String.format("target/%s/%s", method.getName(), System.currentTimeMillis());
		setResultDestination(new File(sharedDirForLogsPath));
		
		resultLog.info("{} result", method.getName());
		infoLog.info("{} info", method.getName());
		
		List<String> content = TestUtility.readFile(new File(sharedDirForLogsPath + "/info-logback.log"));
		Assert.assertEquals(1, content.size());
		Assert.assertEquals(content.get(0), method.getName()+ " info");
	}
	
	@Test
	public void getNewLogDirTest(Method method) {
		File sharedDirForLogs = getNewLogDir("target", method.getName());
		
		setResultDestination(sharedDirForLogs);
		
		resultLog.info("{} result", method.getName());
		infoLog.info("{} info", method.getName());
		
		List<String> content = TestUtility.readFile(new File(sharedDirForLogs + "/result-logback.log"));
		Assert.assertEquals(1, content.size());
		Assert.assertEquals(content.get(0), method.getName()+ " result");
	}
}
