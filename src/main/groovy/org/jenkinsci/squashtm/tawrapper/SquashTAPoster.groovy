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
package org.jenkinsci.squashtm.tawrapper;

import static org.jenkinsci.squashtm.core.TestResult.ExecutionStatus.*

import org.jenkinsci.squashtm.core.TMServer;
import org.jenkinsci.squashtm.core.TestResult;
import org.jenkinsci.squashtm.core.TestResult.ExecutionStatus;

import groovy.json.JsonSlurper
import hudson.FilePath

import org.jenkinsci.squashtm.utils.HttpClient
import org.jenkinsci.squashtm.core.JobInformations
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.FileBuildParameter;

import static org.jenkinsci.squashtm.tawrapper.TA.PRM_TEST_SUITE_JSON
import static org.jenkinsci.squashtm.tawrapper.TA.PRM_NOTIFICATION_URL



/**
 * <p>
 * 	This class will send the test results to Squash TM, posing as Squash TA. Which means that :
 * 
 * <ol>
 * 	<li>The TestResult will be posted to the rest API usually dedicated to Squash TA,</li>
 * 	<li>in the format Squash TM is expecting</li>
 * </ol>
 * 	
 * </p>
 * 
 * <p>
 * 	The plugin choose this mode when the build was triggered by Squash TA. In this mode, each test result 
 * 	will be sent in its own HTTP request. The URL where they will be posted must be extracted from the 
 * 	test suite descriptor file sent by Squash TM. TestResult than cannot be found in the test suite descriptor 
 * 	file will not be sent, although in this case they will be logged.
 * </p>
 * 
 * @author bsiri
 *
 */
public class SquashTAPoster {

	// must be initialized 
	Collection<TMServer> knownServers = []
	JobInformations infos
	PrintStream logger
	
	// private variables	
	HttpClient httpclient
	
	
	public SquashTAPoster(){
		
	}
	
	public SquashTAPoster(Collection<TMServer> servers, JobInformations infos, PrintStream logger){
		knownServers = servers
		this.infos = infos
		this.logger = logger
	}
	
	void postResults(Collection<TestResult> results){
		try{
			logln "[TM-PLUGIN] : sending results as Squash TA"
			
			logln "[TM-PLUGIN] : identifying server : '${endpointUrl}'"
			
			TMServer server = identifyServer()
			
			if (server == null){
				logln "[TM-PLUGIN] : could not identify '${endpointUrl}' (no entry in the configuration), aborting"
				return
			}
			
			logln "[TM-PLUGIN] : identified server : ${server.identifier}"
			
			httpclient = new HttpClient(server, logger)
							
			def testmap = jsonFileToMap()
			
			results.each{			
				log "[TM-PLUGIN] : processing test '${it.name}' -> "
				
				def (TATestStatus data, String url) = prepareHttpPost (it, testmap)
				
				if (url == null){
					logln 'not part of the TA test suite definition for this build, no data sent'
				}
				else{
					httpclient.post url, data
				}
				
			}
		}
		finally{
			if (httpclient != null){
				httpclient.close()
			}
		}
	}
	
	
	// ***********************private methods ***********************
	
	// find which server instance we should post to by comparing their URLs
	TMServer identifyServer(){
		// note : instead of comparing strings, new URL(a).equals(new URL(b)) would be more robust albeit costly
		// but whatever, one step after another
		knownServers.find { normalize(it.url) == endpointUrl }
	}

	void initHttpServer(TMServer server){
		httpclient = new HttpClient(server, logger)
	}
	
	
	Map<String, Object> jsonFileToMap(){
		
		FileBuildParameter fileinfos = infos.tawrapperParameters.getParameter PRM_TEST_SUITE_JSON
		
		def filepath = (fileinfos.absoluteLocation) ? new FilePath(new File(fileinfos.location)) :
													  new FilePath(infos.basePath, fileinfos.location)
		
		def json = filepath.readToString()
		
		def testsuite = new JsonSlurper().parseText(json)
		
		/*
		 * Prepare a map out of the test list.
		 * 
		 * Also, as a side effect, we post process the script name by prepending the project name
		 * because it will make things easier later on. 
		 * 
		 * Both happen in the same loop for the sake of efficiency 
		 */
		
		def testmap = testsuite.test.collectEntries{
			it.script = "/${infos.jobName}/${it.script}"
			[(it.script.toString()) : it]	// the toString() ensure that the key won't be a GString but a regular String
		}
		
		return testmap
	}
	
	/*
	 * Returns a pair of 
	 * 	- data to be sent (as json),
	 *  - the url where to post this
	 */
	Tuple prepareHttpPost(TestResult result, Map testmap){
		TATestStatus data
		String url
		
		/*
		 *  the tricky part is to find build the url. To do so we must first 
		 *  retrieve the test by name in the collection jsonTest.
		 */
		def testdef = testmap[result.testID]
		
		if (testdef != null){
			data = new TATestStatus(result)
			url = endpointUrl + "/automated-executions/${testdef.id}/test-status"
		}
		
		return new Tuple(data, url)		
	}
	


	// *********************** utilities ***************************
	
	// returns the endpoint url parameter, with trailing slashes removed
	String getEndpointUrl(){
		normalize infos.tawrapperParameters.getParameter(PRM_NOTIFICATION_URL)?.value
	}
	
	private normalize(String url){
		return url?.replaceAll('\\/*$', "")
	}
	
	
	void log(String message){ logger.print message }
	
	void logln(String message){	logger.println message }
	

	
}	
