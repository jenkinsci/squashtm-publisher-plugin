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
package org.jenkinsci.squashtm.utils

import org.jenkinsci.squashtm.core.JobInformations
import org.jenkinsci.squashtm.tawrapper.SquashTAWrapperProperty;
import org.jenkinsci.squashtm.tawrapper.TAParametersAction;

import hudson.FilePath
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run
import jenkins.model.Jenkins;

/**
 * <p>
 * 		JobInformationFactory create instances of {@link JobInformations} using the {@link Run} informations and the {@link FilePath}. 
 * </p>
 * 
 * @author bsiri
 *
 */
public class JobInformationsFactory{
	
	
	/**
	 * Creates and fills a JobInformations. 
	 * 
	 * @param build
	 * @param workspace
	 * @param logger
	 * @return
	 */
	static JobInformations create(Run build, FilePath workspace){
				
		JobInformations infos = new JobInformations()
		
		// common infos
		fillCommonInformations infos, build, workspace
		
		// build parameters
		fillParameters infos, build
		
		// TA parameters
		fillTAParameters infos, build
				
		return infos
		
	}
	
	// ************** convenience methods ***************
	
	// override this if you need to
	static protected void fillCommonInformations(JobInformations infos, Run build, FilePath workspace){
		
		// job infos
		infos.basePath = workspace
		infos.buildPath = build.project.buildDir
		infos.jobName = build.project.name
		infos.buildNum = build.number
		
		// server infos
		// strip extra slashes from the urls
		infos.hostURL = Jenkins.instance?.rootUrl?.replaceAll('\\/*$', "")
		infos.jobURL = build.url.replaceAll('\\/*$', "")
		
		// TODO : some day, add an explicit parameter to the job configuration 
		// and use it here
		infos.systemUnderTest = infos.jobName
	}
	
	
	static protected void fillParameters(JobInformations infos, Run build){
		// locate the ParameterActions if any and add the params
		/*
		 *  Note : we don't use build.getBuildVariables() because it reduces each parameter to a string - which  
		 *  sometimes is not appropriate (for instance the stringified value for a FileParameterValue 
		 *  is insufficient to resolve where the file really is) 
		 */
		ParametersAction params = build.getAction ParametersAction
				
		params?.each{
			infos.buildParameters[it.name] = it
		}
	}
	
	static protected void fillTAParameters(JobInformations infos, Run build){
		infos.usesTAWrapper = (build.project.getProperty(SquashTAWrapperProperty) != null)
		infos.tawrapperParameters = build.getAction TAParametersAction		
	}
	
	
}
