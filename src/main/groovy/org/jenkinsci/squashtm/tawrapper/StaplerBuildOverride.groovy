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

import hudson.model.AbstractProject
import hudson.model.ParametersDefinitionProperty

import java.util.logging.Level
import java.util.logging.Logger

import javax.servlet.ServletException

import jenkins.util.TimeDuration
import net.sf.json.JSONArray
import net.sf.json.JSONObject

import org.apache.commons.fileupload.FileItem
import org.jenkinsci.squashtm.tawrapper.TAParametersAction
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.BuildParameter
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.FileBuildParameter
import org.jenkinsci.squashtm.tawrapper.TAParametersAction.StringBuildParameter
import static org.jenkinsci.squashtm.tawrapper.TA.*
import org.kohsuke.stapler.QueryParameter
import org.kohsuke.stapler.StaplerRequest
import org.kohsuke.stapler.StaplerResponse

/**
 * <p>
 * 	This class intercepts http requests that post to the job build method (ie /job/somejob/build) and detects whether this 
 *  build request is mistaking this Job for a Squash TA Job, in which case it will help that job to pose as one. Usually such requests 
 *  are issued by Squash TM.
 * </p> 
 * 
 * <p>
 * 	This Stapler override is triggered only if the project configuration has the {@link SquashTAWrapperProperty} enabled.  
 *  It deals with the parameters that may exist in the StaplerRequest, yet were not configured explicitly in the 
 *  Job configuration (see {@link ParameterDefinitionProperty}). It thus save the administrator the hassle of configuring 
 *  them manually and clutter the job configuration with 5 additional properties.
 * </p> 
 * 
 * <p>
 * 	Note that not every build request should be interpreted as TA build request : other parties may well wish to build that job. 
 *  We test this by testing the parameters in the StaplerRequest. Then, if this build is indeed a TA build, the following will happen :
 * 	<ul>
 * 		<li>the parameters will be collected and forwarded via a threadlocal</li>
 * 		<li>those parameters that do not explicitly appear in the Job configuration will be "removed" (hidden) 
 * 			from the StaplerRequest, otherwise the build would crash due to unknown parameters. 
 * 		</li>
 *  </ul>
 *  On the other hand, if this build is not a TA build, it will carry on unaffected. The forwarded parameters will be eventually 
 *  consumed downstream by the ResultPublisherStep, if configured for this Job.
 * </p>
 *
 * @author bsiri
 *
 */
class StaplerBuildOverride {
	
	private final static Logger LOGGER = Logger.getLogger(StaplerBuildOverride.class.getName());
	
	static ThreadLocal<String> PARAMETERS_WORMHOLE = new ThreadLocal<String>();
		
	public void doBuild( StaplerRequest request, StaplerResponse rsp, @QueryParameter TimeDuration delay ) throws IOException, ServletException {
		
		AbstractProject project = request.findAncestorObject AbstractProject
		
		StaplerRequest toForward = processIfNecessary request, project
				
		project.doBuild toForward, rsp, delay
		
	}

	/** @deprecated use {@link #doBuild(StaplerRequest, StaplerResponse, TimeDuration)} */
	/*
	 * This look duplicated from the above but if we simply call this.doBuild(req, rsp, delay) Stapler will resume
	 * its course and our override would get counter-overriden.
	 */
	@Deprecated
	public void doBuild(StaplerRequest request, StaplerResponse rsp) throws IOException, ServletException {
		
		AbstractProject project = request.findAncestorObject AbstractProject
		
		StaplerRequest toForward = processIfNecessary request, project
				
		project.doBuild toForward, rsp
	}
	
	
	private void cleanWormhole(){
		PARAMETERS_WORMHOLE.set null
	}
	
	
	// *************************** the entrails *****************************

	
	// ----------------------- parameters handling --------------------------

	
	/*
	 * This method does all that is discussed in the javadoc : fetch the parameters, pass them through the hole, 
	 * and hide the parameters if necessary.
	 */
	private StaplerRequest processIfNecessary(StaplerRequest request, AbstractProject project){
		
		cleanWormhole()
		
		StaplerRequest toForward = request
		
		TAParametersAction action = collectParameters request
		
		// forward the action anyway
		PARAMETERS_WORMHOLE.set action
		
		// if this is indeed a TA build, check if the request needs to be 
		// tampered with 
		if (action.isTABuild()){			
			
			
			ParametersDefinitionProperty paramDef = project.getProperty ParametersDefinitionProperty
			
			if (paramDef != null){
				toForward = hideNonExplicitParameters request, paramDef, action
			}
		}
		
		return toForward
	}
	
	/*
	 * Here we look for parameters that are used in a regular Squash TA build. Each parameter is tested on its name 
	 * and type. The parameters that passed the test are returned as TAParametersAction.
	 * 
	 * You can find the list of the parameters usually instructing for a TA build org.jenkinsci.squashTM.tawrapper.TA
	 *
	 */
	
	private TAParametersAction collectParameters(StaplerRequest request){		
		try{
			JSONObject object = request.submittedForm
			JSONArray paramlist = JSONArray.fromObject(object.get ("parameter"))		
	
			def taparams = paramlist.collect{ extractFromJson it, request }
									.grep {it != null }
	
			new TAParametersAction(parameters : taparams)
		}
		// ServletException happens when the build is started as a parameterless build, 
		// in which case request.submittedForm throws this exception
		// TODO : find a way to test whether calling getSubmittedForm() would throw that exception 
		catch(ServletException ex){
			LOGGER.log(Level.FINE, "attempted to retrieve a form from a request that has none : ${ex.getMessage()}")
			return new TAParametersAction()
		}
		
	}

	
	
	/*
	 * Test whether a TA run parameter is detected in the StaplerRequest parameters, 
	 * and if they are of the right type.
	 * 
	 * TODO : maybe also check if they match their permitted values (eg : operation == 'run')  
	 */
	private BuildParameter extractFromJson(JSONObject object, StaplerRequest request){
		
		// test on the name
		String name = object.getString "name"
		
		if (! (name in PARAMETERS)){
			return null;	
		}
		
		// now test on the type
		// they are all String, except for one which is file		
		BuildParameter res = null;
		
		if (name == PRM_TEST_SUITE_JSON){
			
			def file = object.get "file"			
			FileItem item = null
			
			if (file instanceof String){
				item = request.getFileItem file
			}
			if (item != null){
				res = new FileBuildParameter(name, item)
			}
			
		}
		else{
			def value = object.get "value"
			if (value instanceof String){
				res = new StringBuildParameter(name, value)
			}
		}
	
		
		return res
		
	}
	
	/*
	 * This method will wrap the request with a proxy that will return an altered json form.
	 * This form will not show the parameters that are not explicitly requested by the job 
	 * configuration. If we omit this step, the build will crash 
	 * (see ParametersDefinitionProperty#_doBuild(...))
	 */
	private StaplerRequest hideNonExplicitParameters(StaplerRequest original, 
													ParametersDefinitionProperty jobParams, 
													TAParametersAction requestParams){
												
		JSONObject originalForm = original.submittedForm			
		JSONArray paramCopy = JSONArray.fromObject(originalForm.get("parameter"))
				
		def jobParamNames = jobParams.parameterDefinitionNames
		def hiddenNames = requestParams.parameters.collect{it.name}
		
		def toHide = paramCopy.findAll {
			def name = it.getString "name" 
			hiddenNames.contains(name) && !jobParamNames.contains(name)
		}
		
		paramCopy.removeAll toHide
		
		JSONObject forged = new JSONObject()
		forged.put "parameter", paramCopy
		
		
		return ProxyGenerator.INSTANCE.instantiateDelegate(
			[
				getSubmittedForm : {  -> forged }
			],
			[StaplerRequest],
			original
		)
	}
	
}