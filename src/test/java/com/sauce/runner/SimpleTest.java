package com.sauce.runner;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.saucelabs.saucerest.SauceREST;

public class SimpleTest {

	class Ctx {
		public WebDriver driver;
		public String jobId;
		public boolean passed;
	}
	
	private SauceREST sauceRest = new SauceREST(TestConfiguration.USERNAME, TestConfiguration.ACCESS_KEY);
	
	private ThreadLocal threadLocal = new ThreadLocal();
	
	@BeforeMethod
	public void setUp(Method method) throws MalformedURLException {
		if(threadLocal.get() == null) threadLocal.set(new Ctx());
        Ctx ctx = (Ctx) threadLocal.get();
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("name", "SimpleTest - " + method.getName());
        ctx.driver = new RemoteWebDriver(
                new URL(TestConfiguration.onDemandUrl),
                capabilities);
        ctx.driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
        ctx.jobId = ((RemoteWebDriver) ctx.driver).getSessionId().toString();
        ctx.passed = true;
	}
	
	@AfterMethod
    public void tearDown() throws Exception {
        Ctx ctx = (Ctx) threadLocal.get();
        WebDriver driver = ctx.driver;
        if (ctx.passed) {
            sauceRest.jobPassed(ctx.jobId);
        } else {
            sauceRest.jobFailed(ctx.jobId);
        }
        driver.quit();
    }
	
	@Test
	public void test() {
		  Ctx ctx = (Ctx) threadLocal.get();
	        WebDriver driver = ctx.driver;
	        
	        driver.get("ba.com");
	        Assert.assertTrue(driver.getTitle().contains("British Airways"));
	}
	
	
}
