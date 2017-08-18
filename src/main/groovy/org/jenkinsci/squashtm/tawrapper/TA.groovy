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
package org.jenkinsci.squashtm.tawrapper

import java.util.Map;

class TA {
	
	/*
	 * Here are the parameters commonly used in a Squash TA build request
	 */
	public static final PRM_OPERATION = 'operation'
	public static final PRM_EXTERNAL_JOB_ID = 'externalJobId'
	public static final PRM_NOTIFICATION_URL = 'notificationURL'
	public static final PRM_TEST_LIST = 'testList'
	public static final PRM_TEST_SUITE_JSON = 'testsuite.json'
	
	
	
	
	/**
	 * Used to know whether this build is a Squash TA build
	 * Note : we could relax this constraint a bit, no need to check every
	 * single parameters after all
	 */
	public static final Set<String> PARAMETERS = [
		PRM_OPERATION ,
		PRM_EXTERNAL_JOB_ID,
		PRM_NOTIFICATION_URL,
		PRM_TEST_LIST,
		PRM_TEST_SUITE_JSON
	].asImmutable()

	
	
	private TA(){
		super()
	}
	
	
	
}
