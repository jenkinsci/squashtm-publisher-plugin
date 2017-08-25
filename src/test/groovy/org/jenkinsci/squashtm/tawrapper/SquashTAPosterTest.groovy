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

import org.jenkinsci.squashtm.core.JobInformations
import org.jenkinsci.squashtm.core.TMServer
import org.jenkinsci.squashtm.core.TestResult
import org.jenkinsci.squashtm.core.TestResult.ExecutionStatus;
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.BuildParameter
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.FileBuildParameter;
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.StringBuildParameter
import org.jenkinsci.squashtm.testutils.NullPrintStream;

import static org.jenkinsci.squashtm.tawrapper.TA.*

import org.apache.commons.fileupload.FileItem;

import spock.lang.Specification;

public class SquashTAPosterTest extends Specification{
	
	public static final String ENDPOINT_URL = "http://localhost:8080/squash"

	SquashTAPoster poster = new SquashTAPoster(
		knownServers : mockKnownServers(),
		info : mockJobInfos(),
		logger : new NullPrintStream()	
	)
	
	def "should return the endpoint URL defined in the build parameters"(){
		
		when :
			def url = poster.getEndpointUrl()
		
		then :
			url == ENDPOINT_URL
		
	}
	
	def "should remove extra slashes from url"(){
		
		when :
			def newurl = poster.normalize ENDPOINT_URL+'/'
		
		then :
			newurl == ENDPOINT_URL
		
	}
	
	def "should identify which server requested us"(){
		
		when :
			def server = poster.identifyServer()
		
		then :
			server.identifier == "tm-2"
	}
	
	def "should parse the test list"(){
		
		when :
			def tests = poster.jsonFileToMap()
		
		then :
			tests.keySet() == [
					 "/heroquest-test/org/bsiri/heroquest/altdorf/AltdorfSewersTest/test",
				     "/heroquest-test/org/bsiri/heroquest/heroes/BotchedTest/test",
				     "/heroquest-test/org/bsiri/heroquest/monsters/HibernateSessionFactoryTest/combatWillNotOccur",
				     "/heroquest-test/org/bsiri/heroquest/heroes/HeroTest/meetBobTheWarrior",
				     "/heroquest-test/org/bsiri/heroquest/heroes/MainTest/omgThereIsAnError",
				     "/heroquest-test/org/bsiri/heroquest/heroes/MainTest/oopsFailure",
				     "/heroquest-test/org/bsiri/heroquest/monsters/OrcTest/shouldDestroyThatRat",
				     "/heroquest-test/org/bsiri/heroquest/interfaces/MapCoordinateTest/testRelativeLocations",
				     "/heroquest-test/org/bsiri/heroquest/heroes/MainTest/thisTestIsASuccess"
			] as Set
		
	}
	
	def "should prepare data to be sent"(){
		
		given :
			def testmap = 
				['/heroquest-test/org/bsiri/heroquest/heroes/MainTest/omgThereIsAnError' : [id:"5"]]
			
			def testresult = new TestResult(name : "omgThereIsAnError", 
					testID : '/heroquest-test/org/bsiri/heroquest/heroes/MainTest/omgThereIsAnError',
					projectName : "heroquest-test", 
					message : "well... failed",
					status : ExecutionStatus.FAILURE,
					resultURL : "http://some/result/url" 
				)
		
		when :
			def (status, url) = poster.prepareHttpPost (testresult, testmap)
		
		then :
			status.testName == "omgThereIsAnError"
			status.testGroupName == "heroquest-test"
			status.status == "FAILURE"
			status.statusMessage == "well... failed"
			status.resultUrl == 'http://some/result/url'
			url == ENDPOINT_URL +"/automated-executions/5/test-status"
		
	}
	
	// ************ utils *****************
	
	
	def mockKnownServers(){
		[
			new TMServer("tm-1", 'http://localhost:7080/squash', 'admin', 'admin'),
			new TMServer("tm-2", 'http://localhost:8080/squash', 'admin', 'admin'),
			new TMServer("tm-3", 'http://localhost:8084/squash', 'admin', 'admin')
		]
	}
	
	def mockJobInfos(){
				
		TAParametersAction params = new TAParametersAction(
			parameters : [
				new StringBuildParameter(PRM_OPERATION, "run"),
				new StringBuildParameter(PRM_EXTERNAL_JOB_ID, "12345"),
				new StringBuildParameter(PRM_NOTIFICATION_URL, ENDPOINT_URL),
				new StringBuildParameter(PRM_TEST_LIST, "deprecated"),
				new FileBuildParameter(name : PRM_TEST_SUITE_JSON, 
										absoluteLocation : true, 
										location : 'src/test/resources/json-tests/heroquest-ta-testsuite.json')
			]
			
				
		)
		
		return new JobInformations(
			systemUnderTest : "heroquest-1.0.0.RC1",
			jobName : "heroquest-test",
			basePath : null,
			buildPath : null,
			buildNum : 13,
			buildParameters : [:],
			usesTAWrapper : true,
			tawrapperParameters : params,
			hostURL : "http://localhost:9080/jenkins",
			jobURL : 'job/pizza-test/13/'
			
		)
		
		
	}
	
	
	
}
