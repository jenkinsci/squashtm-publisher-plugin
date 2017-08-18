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

import static org.jenkinsci.squashtm.core.TestResult.ExecutionStatus.*

import org.jenkinsci.squashtm.core.TestResult;
import org.jenkinsci.squashtm.tawrapper.TATestStatus;

import spock.lang.Specification
import spock.lang.Unroll


class TATestStatusTest extends Specification {

	
	@Unroll("should convert #exstatus (local name) to #tmstatus (TM name)")
	def "should convert the execution statuses to something TM will understand"(){
		
		expect :
			new TATestStatus().convertStatus(exstatus) == tmstatus
		
		where : 
		
		exstatus		|	tmstatus
		SUCCESS			|	'SUCCESS'
		FAILURE			|	'FAILURE'
		NOT_RUN			|	'NOT_RUN'
		BLOCKED			|	'ERROR'
		
	}
	
	def "should wrap correctly our TestResult (our local format) to a TATestStatus (the TM format)"(){
		
		given :
			TestResult result = new TestResult()
			result.with{
				(message, status, resultURL) = ['test is a success !', SUCCESS, 'http://jenkins/whatever/success']
			}
		when :
			TATestStatus exstatus = new TATestStatus(result)
		
		then :
			exstatus.resultUrl == 'http://jenkins/whatever/success'
			exstatus.status == 'SUCCESS'
			exstatus.statusMessage == 'test is a success !'
		
	}
	
}
