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


import hudson.model.Run
import hudson.tasks.test.AbstractTestResultAction
import hudson.tasks.test.AggregatedTestResultAction
import static org.jenkinsci.squashtm.core.TestResult.ExecutionStatus.*
import org.jenkinsci.squashtm.core.TestResult.ExecutionStatus

/**
 * That class will read a build's TestResultAction and find the tests it holds.
 * 
 * @author bsiri
 *
 */
class TestResultActionExtractor {

	Run build
	JobInformations infos

		
	public Collection<TestResult> collectResults(){
		
		Collection<TestResult> results 
		
		AbstractTestResultAction action = build.getAction AbstractTestResultAction
		
		if (action == null){
			return []
		}
		
		if (action in  AggregatedTestResultAction){
			results = fromAggregate action			
		}
		else{
			results = fromSimple action
		}
		
		results
		
	}
	
	Collection<TestResult> fromAggregate(AggregatedTestResultAction action){

		def childReports = action.childReports
		
		def results = childReports.collect{ child ->
			
			def childbuild = child.run
			
			def childextractor = new TestResultActionExtractor(
				build : childbuild,
				infos : infos
			)
			
			return childextractor.collectResults()
			
		}
		
		return results.flatten()
		
	}
	
	Collection<TestResult> fromSimple(AbstractTestResultAction action){
				
		def tests = []
		tests << action.passedTests << action.skippedTests << action.failedTests
		tests = tests.flatten()
		
		def results = tests.collect{
			def res = new TestResult()
			def path = formatPath it
			
			res.systemUnderTest = infos.systemUnderTest
			res.projectName = infos.jobName
			res.testID = path
			res.path = path
			res.name = it.displayName
			res.status = coerceStatus it
			res.message = formatMessage it
			res.host = infos.hostURL
			res.resultURL = formatUrl action, it
			
			return res
		}
		
		results
		
	}
	
	// **************** utils ********************
	
	private String formatPath(hudson.tasks.test.TestResult jenres){
		def testpath = jenres.fullName.replace('/', '\\/').replace('.', '/')
		return "/${infos.jobName}/${testpath}"
	}
	
	private ExecutionStatus coerceStatus(hudson.tasks.test.TestResult jenres){
		(jenres.skipCount > 0) ? NOT_RUN :
		(jenres.failCount > 0) ? FAILURE :
		SUCCESS
	}
	
	private String formatMessage(hudson.tasks.test.TestResult jenres){
		def msg = ""
		
		def details = jenres.errorDetails
		def stack = jenres.errorStackTrace
		
		if (details != null){			
msg = """<p>
	${details}
	<br/>
	${stack}
</p>"""
		}
		return msg
	}
	
	private String formatUrl(AbstractTestResultAction action, hudson.tasks.test.TestResult jenres){
		def testUrl = action.getTestResultPath jenres 
		return "${infos.hostURL}/${build.url}$testUrl"
	}
		
}
