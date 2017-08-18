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



import org.apache.commons.fileupload.FileItem;
import hudson.model.FileParameterValue
import hudson.model.StringParameterValue

import org.jenkinsci.squashtm.core.SquashTMPublisher;
import org.jenkinsci.squashtm.core.JobInformations
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.FileBuildParameter;
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.StringBuildParameter;

import spock.lang.Specification


class TAParametersActionTest extends Specification {

	
	// ******** Squash TA support section ********************
	
	def "should identify this as a Squash TA 'list tests' build"(){
		
		when :
		
			TAParametersAction action = new TAParametersAction(parameters:[
				[name : "operation", value : "list"] as StringBuildParameter,
				[name : "externalJobId", value : "ABCDE"] as StringBuildParameter				
			])
				
		then :
			action.isTABuild()
			action.isFetchlistBuild()
		
	}
	
	
	
	def "should identify this as a Squash TA 'run tests' build"(){
		
		when :
		
			TAParametersAction action = new TAParametersAction(parameters:[
				[name : "operation", value : "run"] as StringBuildParameter,
				[name : "externalJobId", value : "ABCDE"] as StringBuildParameter,
				[name : "notificationURL" , value : "http://somewhere"] as StringBuildParameter,
				[name : "testsuite.json", fileItem:null] as FileBuildParameter,
				
			])
				
		then :
			action.isTABuild()
			action.isTestRunBuild()
		
	}
	
	def "should identify this as neither of the above"(){
		
		when :
		
			TAParametersAction action = new TAParametersAction(parameters:[
				[name : "operation", value : "uh ?"] as StringBuildParameter
				
			])
				
		then :
			! action.isTABuild()
	}
	
	
}
