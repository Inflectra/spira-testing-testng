package com.inflectra.spiratest.addons.testnglistener.samples;

import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;
import com.thoughtworks.selenium.*;

import com.inflectra.spiratest.addons.testnglistener.*;

/**
 * A sample Selenium test using the ability to return results back to SpiraTest
 * 
 * @author		Inflectra Corporation
 * @version		3.0.0
 *
 */
@SpiraTestConfiguration(
url="http://localhost/Spira",
login="fredbloggs",
password="fredbloggs",
projectId=1,
releaseId=1,
runner=RunnerName.Selenium
)
@Test(groups={"seleniumtest"})
public class SeleniumTest
{
    private Selenium selenium;

	@BeforeClass
    public void setUp()
	{
		//Instantiate the selenium Java proxy
        String url = "http://www.google.com";
        selenium = new DefaultSelenium("localhost", 4444, "*firefox", url);
        selenium.start();
    }

	@AfterClass
    protected void tearDown()
	{
        selenium.stop();
    }

	// Sample test that searches on Google, passes correctly
	@Test(groups={"seleniumtest"})
	@SpiraTestCase(testCaseId=5)
    public void testGoogle()
	{
		//Opens up Google
        selenium.open("http://www.google.com/webhp?hl=en");

		//Verifies that the title matches
        assertEquals("Google", selenium.getTitle());
        selenium.type("q", "Selenium OpenQA");

		//Verifies that it can find the Selenium website
        assertEquals("Selenium OpenQA", selenium.getValue("q"));
        selenium.click("btnG");
        selenium.waitForPageToLoad("5000");
        assertEquals("Selenium OpenQA - Google Search", selenium.getTitle());
    }
}
