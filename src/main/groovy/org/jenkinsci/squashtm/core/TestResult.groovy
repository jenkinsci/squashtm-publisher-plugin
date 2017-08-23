/*
 *     The MIT License
 *
 *     Copyright (C) 2016-2017 Henix, henix.fr
 *
 *     Permission is hereby granted, free of charge, to any person obtaining a copy
 *     of this software and associated documentation files (the "Software"), to deal
 *     in the Software without restriction, including without limitation the rights
 *     to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *     copies of the Software, and to permit persons to whom the Software is
 *     furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *     IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *     FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *     AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *     LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *     OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *     THE SOFTWARE.
 */
package org.jenkinsci.squashtm.core

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includeFields=true, includes='systemUnderTest,projectName,testID,host,name,path,status,resultURL,message')
public class TestResult implements Serializable{
	
	/**
	 * The reference of the system under test (usually a name and a version)
	 */
	private String systemUnderTest = ""
	
	/**
	 * The name of the project this test belongs to (note that it can differ from the project under test)
	 */
	private String projectName
	
	/**
	 * An identifier for this test. No harm done if it is arbitrary. Its desirable properties are :
	 * <ul>
	 * 	<li>should be unique for each test (across all jobs in this server)</li>
	 * 	<li>optionally, should be predictable according to the test it represents (this will ease the processing on the Squash TM side)</li>
	 * </ul>
	 * 
	 * A good convention would be /project/the/qualified/test/Classname/methodName
	 */
	private String testID
	
	/**
	 * The URL of this Jenkins instance 
	 */
	private String host
	
	/**
	 * a friendly name for this test if possible
	 */
	private String name
	
	/**
	 * Absolute path representing that test. It should start with /jobname and then any '/' delimited string. Double '/' should be avoided. As long as you meet these requirements anything goes.
	 * For instance a method 'testMethod' of a JUnit class 'org.randomcode.tests.MyTestClass' hosted in job 'TheJob' would naturally be : '/TheJob/org/randomcode/tests/MyTestClass/testMethod'.
	 * But simply using '/TheJob/testMethod' would be fine. Of course in that later case a conflict occurs, consequences are on you. 
	 */
	private String path
		
	/**
	 * The pass/fail status for that test
	 */
	private ExecutionStatus status
	
	/**
	 * An optional message about why the test failed, if it failed. 
	 */
	private String message
	
	/**
	 * The URL where the result can be browsed.
	 */
	private String resultURL 
	
	
	public static enum ExecutionStatus{
		SUCCESS,
		FAILURE,
		BLOCKED,
		NOT_RUN
	}
	
	public TestResult(){
		super()
	}
	
	

	public String toString(){
		return """{
	systemUnderTest : ${systemUnderTest},
	projectName : ${projectName},
	testID : ${testID},
	host : ${host},
	name : ${name},
	path : ${path},
	status : ${status},
	message : ${message},
	resultURL : ${resultURL}
},
"""
	}
}
