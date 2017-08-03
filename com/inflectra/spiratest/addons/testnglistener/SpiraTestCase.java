package com.inflectra.spiratest.addons.testnglistener;

import java.lang.annotation.*;

/**
 * This defines the 'SpiraTestCase' annotation used to specify the
 * SpiraTest test case that the TestNG test maps to
 * 
 * @author		Inflectra Corporation
 * @version		1.2.0
 *
 */
@Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value=java.lang.annotation.ElementType.METHOD)
public @interface SpiraTestCase
{
	int testCaseId ();
}