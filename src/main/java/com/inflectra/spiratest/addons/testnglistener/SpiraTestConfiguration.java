package com.inflectra.spiratest.addons.testnglistener;

import java.lang.annotation.*;

/**
 * This defines the 'SpiraTestConfiguration' annotation used to specify the authentication,
 * project and release information for the test being executed
 * 
 * @author		Inflectra Corporation
 * @version		2.3.0
 *
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value=java.lang.annotation.ElementType.TYPE)
public @interface SpiraTestConfiguration
{
	String url ();
	String login ();
	String password ();
	int projectId ();
	int releaseId () default -1;
	int testSetId () default -1;
	RunnerName runner () default RunnerName.TestNG;
}
