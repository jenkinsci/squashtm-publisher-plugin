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

import hudson.tasks.test.AbstractTestResultAction
import hudson.tasks.test.AggregatedTestResultAction;
import hudson.tasks.test.TestResult
import net.bytebuddy.ByteBuddy;
import spock.lang.Specification;
import hudson.model.Run
import hudson.model.Action
import hudson.model.FreeStyleBuild;
import hudson.model.Job;
import static net.bytebuddy.matcher.ElementMatchers.*

import org.jenkinsci.squashtm.core.TestResult.ExecutionStatus;
import org.objenesis.ObjenesisStd;

import static net.bytebuddy.implementation.FixedValue.*;

class TestResultActionExtractorTest extends Specification{

	
	def "should collect results in a build of a single project"(){

		given : "basic informations"
			def infos = mockInfos()
					
		and : "the test results"
			def results = mockResults()
			
		and : "the action"
			def action = Mock(AbstractTestResultAction)
			action.passedTests >> [results.success]
			action.skippedTests >> [results.notrun]
			action.failedTests >> [results.failure]
			action.getTestResultPath(_) >> { it->
				def fullname = it[0].fullName // don't know why 'it' here is an array
				"testReport/${fullname}"
			}
			
			
			
		and : "the build"
			def build = mockRun([url : 'job/25/', action : action])
		
		when :
			def extractor = new TestResultActionExtractor(build : build, info : infos)
			def output = extractor.collectResults()
			
			
		then :
		
		
			org.jenkinsci.squashtm.core.TestResult succ = output.find {it.status == ExecutionStatus.SUCCESS}
			succ.systemUnderTest == 'buggy-project-1.2.0'
			succ.projectName == 'my-job'
			succ.testID == '/my-job/my/tests/Test/success'
			succ.host == 'http://localhost:8080'
			succ.name == 'success'
			succ.path == '/my-job/my/tests/Test/success'
			succ.status == ExecutionStatus.SUCCESS
			succ.message == ''
			succ.resultURL == 'http://localhost:8080/job/25/testReport/my.tests.Test.success'

			org.jenkinsci.squashtm.core.TestResult notrun = output.find {it.status == ExecutionStatus.NOT_RUN}
			notrun.systemUnderTest == 'buggy-project-1.2.0'
			notrun.projectName == 'my-job'
			notrun.testID == '/my-job/my/tests/Test/notrun'
			notrun.host == 'http://localhost:8080'
			notrun.name == 'notrun'
			notrun.path == '/my-job/my/tests/Test/notrun'
			notrun.status == ExecutionStatus.NOT_RUN
			notrun.message == ''
			notrun.resultURL == 'http://localhost:8080/job/25/testReport/my.tests.Test.notrun'
		
			org.jenkinsci.squashtm.core.TestResult failure = output.find {it.status == ExecutionStatus.FAILURE } 
			failure.systemUnderTest == 'buggy-project-1.2.0'
			failure.projectName == 'my-job'
			failure.testID == '/my-job/my/tests/Test/failure'
			failure.host == 'http://localhost:8080'
			failure.name == 'failure'
			failure.path == '/my-job/my/tests/Test/failure'
			failure.status == ExecutionStatus.FAILURE
			failure.message == """<p>
	some error happened
	<br/>
	some stacktrace
</p>"""
			failure.resultURL == 'http://localhost:8080/job/25/testReport/my.tests.Test.failure'


	}
	
	
	def "should collect results in a build of an aggregated project"(){
		
		given : "basic informations"
			def infos = mockInfos()
					
		and : "the test results"
			def results = mockResults()
			
		and : "child build 1"
		
		def subaction1 = Mock(AbstractTestResultAction)
		subaction1.passedTests >> [results.success]
		subaction1.skippedTests >> []
		subaction1.failedTests >> []
		subaction1.getTestResultPath(_) >> { it->
			def fullname = it[0].fullName // don't know why 'it' here is an array
			"my.tests\$good-tests/testReport/${fullname}"
		}
				
		def subbuild1 = mockRun([url : 'job/25/', action : subaction1])
		
		
		and : "child build 2"
		
		
		def subaction2 = Mock(AbstractTestResultAction)
		subaction2.passedTests >> []
		subaction2.skippedTests >> [results.notrun]
		subaction2.failedTests >> [results.failure]
		subaction2.getTestResultPath(_) >> { it->
			def fullname = it[0].fullName // don't know why 'it' here is an array
			"my.tests\$bad-tests/testReport/${fullname}"
		}
				
		def subbuild2 = mockRun([url : 'job/25/', action : subaction2])
		

		
		and : "the main build"
		
		
		def mainaction = Mock(AggregatedTestResultAction)
		mainaction.childReports >> [
			[run : subbuild1],
			[run : subbuild2]			
		]
		
		def mainbuild = mockRun([url : 'job/25/', action : mainaction])
		
		
		when :
			def extractor = new TestResultActionExtractor(build : mainbuild, info : infos)
			def output = extractor.collectResults()
		
		then :
		
		
			org.jenkinsci.squashtm.core.TestResult succ = output.find {it.status == ExecutionStatus.SUCCESS}
			succ.systemUnderTest == 'buggy-project-1.2.0'
			succ.projectName == 'my-job'
			succ.testID == '/my-job/my/tests/Test/success'
			succ.host == 'http://localhost:8080'
			succ.name == 'success'
			succ.path == '/my-job/my/tests/Test/success'
			succ.status == ExecutionStatus.SUCCESS
			succ.message == ''
			succ.resultURL == 'http://localhost:8080/job/25/my.tests\$good-tests/testReport/my.tests.Test.success'

			org.jenkinsci.squashtm.core.TestResult notrun = output.find {it.status == ExecutionStatus.NOT_RUN}
			notrun.systemUnderTest == 'buggy-project-1.2.0'
			notrun.projectName == 'my-job'
			notrun.testID == '/my-job/my/tests/Test/notrun'
			notrun.host == 'http://localhost:8080'
			notrun.name == 'notrun'
			notrun.path == '/my-job/my/tests/Test/notrun'
			notrun.status == ExecutionStatus.NOT_RUN
			notrun.message == ''
			notrun.resultURL == 'http://localhost:8080/job/25/my.tests\$bad-tests/testReport/my.tests.Test.notrun'
		
			
			org.jenkinsci.squashtm.core.TestResult failure = output.find {it.status == ExecutionStatus.FAILURE } 
			failure.systemUnderTest == 'buggy-project-1.2.0'
			failure.projectName == 'my-job'
			failure.testID == '/my-job/my/tests/Test/failure'
			failure.host == 'http://localhost:8080'
			failure.name == 'failure'
			failure.path == '/my-job/my/tests/Test/failure'
			failure.status == ExecutionStatus.FAILURE
			failure.message == """<p>
	some error happened
	<br/>
	some stacktrace
</p>"""
			failure.resultURL == 'http://localhost:8080/job/25/my.tests\$bad-tests/testReport/my.tests.Test.failure'
		
		
	}
	
	
	// *************** utils ****************
	
	def mockInfos(){
		return new JobInformations(
			systemUnderTest : "buggy-project-1.2.0",
			jobName : "my-job",
			hostURL : "http://localhost:8080"
		)
	}
	
	def mockResults(){
		
		TestResult success = Mock(TestResult)
		success.fullName >> "my.tests.Test.success"
		success.displayName >> "success"
		
		TestResult failure = Mock(TestResult) 
		failure.fullName >> "my.tests.Test.failure"
		failure.displayName >> "failure"
		failure.failCount >> 1
		failure.errorDetails >> "some error happened"
		failure.errorStackTrace >> "some stacktrace"
			
		TestResult notrun = Mock(TestResult)
		notrun.fullName >> "my.tests.Test.notrun"
		notrun.displayName >> "notrun"
		notrun.skipCount >> 1
		
			
		return [
			success : success, 
			failure : failure, 
			notrun : notrun
		]

		
	}
	
	
	// Ubergenericized types with no default constructors are such a pleasure to work with
	def mockRun(params){
		
		def clazz = new ByteBuddy()
						.subclass(Run)
						.method(named('getUrl')).intercept(value(params.url))
						.method(named('getAction')).intercept(value(params.action))
						.make()
						.load(getClass().getClassLoader())
						.getLoaded()
		
		return new ObjenesisStd().newInstance(clazz)
	}
	
	
}