package com.inflectra.spiratest.addons.testnglistener;

/**
 * This defines the 'TestRun' class that's used to store
 * the information relating to a single test run
 * 
 * @author		Inflectra Corporation
 * @version		1.5.1
 *
 */
public class TestRun
{
	public int testCaseId;
	public String message;
	public String stackTrace;
	public int executionStatusId;
	public String url;
	public String userName;
	public String password;
	public int projectId;
	public int releaseId;
	public int testSetId;
	public String testName;
	public RunnerName runner;
}