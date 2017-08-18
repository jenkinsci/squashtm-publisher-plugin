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

import org.jenkinsci.squashtm.core.TestResult;
import org.jenkinsci.squashtm.core.TestResult.ExecutionStatus;
import static org.jenkinsci.squashtm.core.TestResult.ExecutionStatus.*;
import java.util.Date

/**
 * This bean contains the minimum informations expected by Squash TM 
 * when its TA API is hit. Unfortunately it null-checks against a lot of attributes
 * which don't always make sense.
 * 
 * @see org.squashtest.tm.api.testautomation.execution.dto.TestExecutionStatus
 */
public class TATestStatus {
	String testName
	String testGroupName
	String startTime
	String endTime
	
	// here are the fields we are really interested in
	String status
	String statusMessage
	String resultUrl
	
	// the json serializer explicitly asks for the getters
	
	public String getTestName(){
		testName
	}
	
	public String getTestGroupName(){
		testGroupName
	}
	
	public String getStartTime(){
		startTime
	}
	
	public String getEndTime(){
		endTime
	}
	
	public String getStatus(){
		status
	}
	
	public String getStatusMessage(){
		statusMessage
	}
	
	public String getResultUrl(){
		resultUrl
	}
	
	
	// constructors
	
	TATestStatus(){
		
	}
	
	TATestStatus(TestResult result){
		testName = result.name
		testGroupName = result.projectName
		
		// the timestamps are bogus for now
		def strdate = new Date().format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
		startTime = strdate
		endTime = strdate
		
		statusMessage=result.message
		resultUrl = result.resultURL
		
		// convert our ExecutionStatus to something compatible with
		// org.squashtest.tm.api.testautomation.execution.dto.ExecutionStatus
		status = convertStatus(result.status)
	}
	
	@Override
	public String toString(){
		return """{
			  status : ${status}
			  statusMessage : ${statusMessage},
			  resultUrl : ${resultUrl},
			}"""
	}
	
	String convertStatus(ExecutionStatus status){
		switch(status){
			case SUCCESS :
			case FAILURE :
			case NOT_RUN : return status.name();
			case BLOCKED : return 'ERROR'
			default : return 'FAILURE'
		}
	}
}
