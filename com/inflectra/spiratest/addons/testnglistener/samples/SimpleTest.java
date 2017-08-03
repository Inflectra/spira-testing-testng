package com.inflectra.spiratest.addons.testnglistener.samples;

import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;

import com.inflectra.spiratest.addons.testnglistener.*;

/**
 * Some simple tests using the ability to return results back to SpiraTest
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
	testSetId=1
)
@Test(groups={"unittest"})
public class SimpleTest
{
	protected int fValue1;
	protected int fValue2;

	/**
	 * Sets up the unit test
	 */
	@BeforeClass
	public void setUp()
	{
		fValue1= 2;
		fValue2= 3;
	}

	/**
	 * Tests the addition of the two values
	 */
	@Test(groups={"unittest"})
	@SpiraTestCase(testCaseId=5)
	public void testAdd()
	{
		double result = fValue1 + fValue2;

		// forced failure result == 5
		assertTrue (result == 6);
	}

	/**
	 * Tests division by zero
	 */
	@Test(groups={"unittest"})
	@SpiraTestCase(testCaseId=5)
	public void testDivideByZero()
	{
		int zero = 0;
		int result = 8 / zero;
		result++; // avoid warning for not using result
	}

	/**
	 * Tests two equal values
	 */
	@Test(groups={"unittest"})
	@SpiraTestCase(testCaseId=6)
	public void testEquals()
	{
		assertEquals(12, 12);
		assertEquals(12L, 12L);
		assertEquals(new Long(12), new Long(12));

		assertEquals("Size", 12, 13);
		assertEquals("Capacity", 12.0, 11.99, 0.0);
	}

	/**
	 * Tests success
	 */
	@Test(groups={"unittest"})
	@SpiraTestCase(testCaseId=6)
	public void testSuccess()
	{
		//Successful test
		assertEquals(12, 12);
	}
	
	/**
	 * Tests parameterized methods
	 */
	@Test(groups={"unittest"})
	@SpiraTestCase(testCaseId=2)
	@Parameters({"login", "password"})
	public void testParameters(String login, String password)
	{		
		//Test login
		assertEquals("librarian", login);

		//Test password
		assertEquals("PleaseChange", password);
	}
}