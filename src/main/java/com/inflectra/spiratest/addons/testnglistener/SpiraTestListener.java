package com.inflectra.spiratest.addons.testnglistener;

import org.testng.*;

import java.lang.reflect.*;   
import java.util.*;

/**
 * This defines the 'SpiraTestConfiguration' annotation used to specify the authentication,
 * project and release information for the test being executed
 * 
 * @author		Inflectra Corporation
 * @version		3.0.1
 *
 */
public class SpiraTestListener extends TestListenerAdapter
{
	protected Vector<TestRun> testRunVector;

	/**
	 * Logs a failure with SpiraTest whenever a unit test fails
	 * 
	 * @param testResult	TestNG test result object that describes the failed test
	 */
	public void onTestFailure(ITestResult testResult)
	{
		//Extract the values out of the testResult object
		String message = "";
		String stackTrace = "";
		if (testResult.getThrowable() != null)
		{
			message = testResult.getThrowable().getMessage();
			StackTraceElement[] stackTraceElements = testResult.getThrowable().getStackTrace();
			for (int i = 0; i < stackTraceElements.length; i++)
			{
				stackTrace = stackTrace + stackTraceElements[i].toString() + " ";
			}
		}
		String classAndMethod = testResult.getName() + "(" + testResult.getTestClass().getName() + ")";

		//Handle the empty string case
		if (message == null || message.equals(""))
		{
			message = "Test Failed";
		}

		System.out.print ("Test Failed\n");
		System.out.print (message + "\n");
		System.out.print ("Stack Trace:\n" + stackTrace + "\n");

		//Create a new test run
		TestRun newTestRun = new TestRun();
		newTestRun.message = message;
		newTestRun.stackTrace = stackTrace;
		newTestRun.executionStatusId = 1;	//Failed
		newTestRun.testName = classAndMethod;

		//Populate the Test Run from the meta-data derived from 
		//the class/method name combination
		populateTestRun (classAndMethod, newTestRun, true);
		testRunVector.addElement (newTestRun);
	}

	/**
	 * Logs an event with SpiraTest whenever a unit test succeeds
	 * 
	 * @param testResult	TestNG test result object that describes the passed test
	 */
	public void onTestSuccess (ITestResult testResult)
	{
		//Extract the values out of the testResult object
		String classAndMethod = testResult.getName() + "(" + testResult.getTestClass().getName() + ")";

		//Add an entry to the list of test runs
		TestRun newTestRun = new TestRun();
		newTestRun.message = "Test Passed";
		newTestRun.stackTrace = "";
		newTestRun.executionStatusId = 2;	//Passed
		newTestRun.testName = classAndMethod;
		
		//Populate the Test Run from the meta-data derived from 
		//the class/method name combination
		populateTestRun (classAndMethod, newTestRun, false);
		testRunVector.addElement (newTestRun);
	}

	/**
	 * Called when the test run is started for a fixture
	 * 
	 * @param testContext	TestNG test context object that describes the tests to be run
	 */
	public void onStart (ITestContext testContext)
	{
		//Create a new vector of test runs
		this.testRunVector = new Vector<TestRun>();

		System.out.print ("Starting test run...\n\n");
	}

	/**
	 * Called when the test run is finished for a fixture
	 * 
	 * @param testContext	TestNG test context object that describes the tests to be run
	 */
	public void onFinish (ITestContext testContext)
	{
		System.out.print ("Test run finished with " + testContext.getFailedTests().getAllResults().size() + " Failures.\n\n");

		try
		{
			//Instantiate the web service proxy class
			SpiraTestExecute spiraTestExecute = new SpiraTestExecute();

			//Now we need to iterate through the vector and call the SpiraTest API to record the results
			//We need to record both passes and failures
			int successCount = 0;
			int errorCount = 0;
			int notConfigured = 0;
			for (int i = 0; i < this.testRunVector.size(); i++)
			{
				TestRun testRun = (TestRun) this.testRunVector.elementAt(i);
			
				if (testRun.url != null) {
					//Get the current date/time
					Date now = new Date();
	
					//Populate the web service proxy with the connection info, then execute the API method
					spiraTestExecute.url = testRun.url;
					spiraTestExecute.userName = testRun.userName;
					spiraTestExecute.password = testRun.password;
					spiraTestExecute.projectId = testRun.projectId;
					int testRunId = spiraTestExecute.recordTestRun (
						null,
						testRun.testCaseId,
						(testRun.releaseId == -1) ? null : testRun.releaseId,
						(testRun.testSetId == -1) ? null : testRun.testSetId,
						now,
						now,
						testRun.executionStatusId,
						testRun.runner.toString(),
						testRun.testName,
						1,
						testRun.message,
						testRun.stackTrace
						);
					if (testRunId == -1)
					{
						errorCount++;
					}
					else
					{
						successCount++;
					}
				}
				else {
					notConfigured++;
				}
			}
			//Print out how many results transmitted successfully to SpiraTest
			System.out.print (successCount + " test results were successfully transmitted to SpiraTest (" + errorCount + " errors) (" + notConfigured + " not configured).\n\n");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		} 
	}

	/**
	 * Populates the test run object from the annotations associated with the test case (class and method)s
	 * 
	 * @param classAndMethod	The class and method name in the form 'method(class)'
	 * @param displayMessage	Should we display a message or not
	 * @param testRun		The test run object to be populated
	 */
	protected void populateTestRun (String classAndMethod, TestRun testRun, boolean displayMessage)
	{
		//Get the test class and test method names separately
		//The header contains "method(class)"
		String [] classAndMethodArray = classAndMethod.split ("[()]");
		String methodName = classAndMethodArray [0];
		String className = classAndMethodArray [1];

		//Now try and extract the metadata from the test case
		try
		{
			//Get a handle to the class and method
			Class<?> testClass = Class.forName(className);
			//We can't use getMethod() because we may have parameterized tests
			//for which we won't know the matching signature
			Method testMethod = null;
			Method[] possibleMethods = testClass.getMethods();
			for (Method possibleMethod : possibleMethods)
			{
				if (possibleMethod.getName().equals(methodName))
				{
					testMethod = possibleMethod;
				}
			}

			if (testMethod == null)
			{
				throw new NoSuchMethodException("Unable to find test method: " + methodName);
			}
			
			//Extract the SpiraTest test case id - if present
			if (testMethod.isAnnotationPresent(SpiraTestCase.class))
			{
				SpiraTestCase methodAnnotation = testMethod.getAnnotation (SpiraTestCase.class);
				testRun.testCaseId = methodAnnotation.testCaseId();
				if (displayMessage)
				{
					System.out.print ("Matches SpiraTest test case id: TC" + testRun.testCaseId + "\n\n");
				}
			}
			else
			{
				System.out.print ("SpiraTest Annotation not Found on method '" + methodName + "'!\n\n");
			}

			//Extract the SpiraTest configuration data - if present
			if (testClass.isAnnotationPresent(SpiraTestConfiguration.class))
			{
				SpiraTestConfiguration classAnnotation = testClass.getAnnotation (SpiraTestConfiguration.class);
				testRun.url = classAnnotation.url();
				testRun.userName = classAnnotation.login();
				testRun.password = classAnnotation.password();
				testRun.projectId = classAnnotation.projectId();
				testRun.releaseId = classAnnotation.releaseId();
				testRun.testSetId = classAnnotation.testSetId();
				testRun.runner = classAnnotation.runner();
			}
			else
			{
				System.out.print ("SpiraTest Annotation not Found on class '" + className + "'!\n\n");
			}
		}
		catch(NoSuchMethodException e)
		{
			System.out.println(e);
		} 
		catch(ClassNotFoundException e)
		{
			System.out.println(e);
		} 
	}
}
