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

import static org.jenkinsci.squashtm.tawrapper.TA.*

import net.sf.json.JSONArray;
import net.sf.json.JSONObject

import org.jenkinsci.squashtm.tawrapper.TAParametersAction.StringBuildParameter
import org.kohsuke.stapler.StaplerRequest

import hudson.model.ParametersDefinitionProperty;
import spock.lang.Specification

class StaplerBuildOverrideTest extends Specification{
	
	StaplerBuildOverride override = new StaplerBuildOverride()
	
	def "should extract TA parameters from a JSONObject"(){
		
		expect :
			override.extractFromJson(input, null) == output
		
		where :
					
		input														|	output		
		jsonobject(PRM_OPERATION, "run")							|	new StringBuildParameter(PRM_OPERATION, "run")
		jsonobject(PRM_EXTERNAL_JOB_ID, "abxc")						|	new StringBuildParameter(PRM_EXTERNAL_JOB_ID, "abxc")
		jsonobject(PRM_NOTIFICATION_URL, "http://localhost/squash")	|	new StringBuildParameter(PRM_NOTIFICATION_URL, "http://localhost/squash")		
		jsonobject(PRM_TEST_LIST, "tests:A,B")						|	new StringBuildParameter(PRM_TEST_LIST, "tests:A,B")
	}
	
	
	def "should not extract TA parameters from a JSONObject because name or type don't match"(){
		
		expect :
			override.extractFromJson(input, null) == null
		
		where :
					
		input << [
			jsonobject(PRM_OPERATION, 1),
			jsonobject(PRM_EXTERNAL_JOB_ID, 4),
			jsonobject(PRM_NOTIFICATION_URL, 14),
			jsonobject(PRM_TEST_LIST, 4),
			jsonobject("whatever", "whatever")			
		]
	}
	
	
	def "should collect the parameters of a request"(){
		
		given :
			StaplerRequest request = Mock()
			request.getSubmittedForm() >> JSONObject.fromObject([
				"parameter" : [
						[name : "operation", value : "list"],
						[name : "bogus", value : "whatever"],
						[name : "externalJobId", value : "ABSDC"],
						[name : "notificationURL", value : -654654]	
					]	
				])
		
		when :
			def params = override.collectParameters request
		
		then :
			params.parameters == [
				new StringBuildParameter("operation", "list"),
				new StringBuildParameter("externalJobId", "ABSDC")	
			]
	}
	
	
	def "should hide the parameters that were processed in the request"(){
		
		given :
			StaplerRequest orig = Mock()
			orig.getSubmittedForm() >> JSONObject.fromObject([
				"parameter" : [
						[name : "operation", value : "list"],
						[name : "bogus", value : "whatever"],
						[name : "externalJobId", value : "ABSDC"],
						[name : "somevalue", value : -654654]	
					]		
			])
			
		and :
			ParametersDefinitionProperty jobParams = Mock()
			jobParams.getParameterDefinitionNames() >> ['bogus', 'somevalue']
			
		and :
			TAParametersAction reqParams = new TAParametersAction(
				parameters : [
						[name : "operation", value : "list"] as StringBuildParameter,
						[name : "externalJobId", value : "ABSDC"] as StringBuildParameter
				]	
			)
			
		when :
			def newRequest = override.hideNonExplicitParameters orig, jobParams, reqParams
		
		then :
			JSONObject form = newRequest.submittedForm
			JSONArray params = form.getJSONArray "parameter"
			
			! params.any { it.getString("name") in ["operation", "externalJobId"] }
			params.every { it.getString("name") in ["bogus", "somevalue"] }
		
		
	}
	
	
	// ***************** utilities ****************************
	
	def jsonobject(name, value) {
		JSONObject obj = new JSONObject()
		obj.put "name", name
		obj.put "value", value
		return obj
	}
	
}