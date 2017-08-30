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

import hudson.FilePath
import hudson.model.ParameterValue
import org.jenkinsci.squashtm.tawrapper.TAParametersAction

/**
 * <p>
 * 		A JobInformations embeds the informations we need about the build and the project. 
 * 		This structure is lighter than the full {@link hudson.model.Run} object and could be serialized
 * 		and sent over the network to a slave node when relevant (Note : for now it appears
 * 		that everything happens on the master node so concerns about serialization aren't crucial for now).
 * </p>
 *
 * @author bsiri
 *
 */
/*
 * Note : just noticed that "information" has no plural form in english and thus "informations"
 * is incorrect, apologies.
 */
class JobInformations implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	
	// ************* project information *************
	
	/**
	 * <p>
	 * 	The reference that identifies the system under test. Note : unused for now. 
	 * </p>
	 * 
	 * <h3>What</h3>
	 * <p>
	 * 	This is a reference that identify the <strong>system under test</strong> (as opposed to the present project being built).
	 * 	It may designate a single project, or a whole group of services hosted on different servers etc. We recommend to
	 * 	embed the version number of this environment in the reference.
	 * </p>
	 * <p>
	 * 		In some instances the project under test is also the project being built but not always, e.g. :
	 *	  	<ul>
	 *	 		<li>sometimes the project being built is a performance test project, that fires against the system under
	 *	 		test deployed elsewhere,</li>
	 *	 		<li>the real name of the project is different from the technical name.</li>
	 *	 	</ul>
	 * </p>
	 * 
	 * <h3>Values</h3>
	 * <p>
	 * 		Value will be either : 
	 * 		<ul>
	 * 			<li>the value configured for in the job configuration if set (not implemented yet)</li>
	 * 			<li>the job name otherwise</li>
	 * 		</ul>
	 * </p>
	 */
	String systemUnderTest;
	
	
	// ************* job information *******************
	
	/**
	 * the name of the Jenkins job
	 */
	String jobName
	
	/**
	 * the base path of the project that is being built
	 */
	FilePath basePath
	
	/**
	 * the directory where Jenkins store its own information regarding the build itself. Internal usage only.
	 */
	transient File buildPath
	
	
	/**
	 * the number of the build
	 */
	int buildNum
		
	
	/**
	 * The parameters of the build, indexed by their name.
	 */
	Map<String, ParameterValue> buildParameters = [:]
	
	/**
	 * Whether the project uses the TA wrapper or not (see {@link org.jenkinsci.squashtm.tawrapper.SquashTAWrapperProperty})
	 */
	boolean usesTAWrapper = false
		
	/**
	 * The Squash TA parameters used in that Job, if the TA wrapper was enabled  
	 */
	TAParametersAction tawrapperParameters = new TAParametersAction()	

		
	// **********server informations **************
	
	/**
	 * the URL of the (master) instance of Jenkins 
	 */
	String hostURL
	
	
	/**
	 * the relative url of that job
	 */
	String jobURL
	
	
	// *************** other ****************

	@Override
	String toString(){
"""
{
	systemUnderTest : ${systemUnderTest},
	jobName : ${jobName},
	TAWrapper : ${usesTAWrapper},
	basePath : ${basePath},
	buildNum : ${buildNum},
	hostURL : ${hostURL},
	jobURL : ${jobURL}
}"""
	}
		
}
