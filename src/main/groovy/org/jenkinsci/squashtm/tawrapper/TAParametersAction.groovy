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

import groovy.transform.EqualsAndHashCode
import hudson.model.Action
import org.apache.commons.fileupload.FileItem

import static org.jenkinsci.squashtm.tawrapper.TA.*

public class TAParametersAction implements Action {

	
	Collection<BuildParameter> parameters = []
			
	
	BuildParameter getParameter(String name){
		parameters.find { it.name == name }
	}
	
	boolean hasParameter(String name){
		parameters.any { it.name == name }
	}
	
	void add (BuildParameter p){
		parameters << p
	}
	
	@Override
	String toString(){
		"""parameters : ${parameters},
isTaBuild : ${isTABuild()},
isFetchlistBuild : ${isFetchlistBuild()},
isTestRunBuild : ${isTestRunBuild()}
"""
	}

	/**
	 * Tests whether the parameters indicates that this build is probably intended for a Squash TA build
	 * 
	 * @return
	 */
	boolean isTABuild(){
		isFetchlistBuild() || isTestRunBuild()
	}
	
	/**
	 * Tests whether this is the now old and deprecated 'fetch test list build'. 
	 */
	boolean isFetchlistBuild(){
		hasParameter(PRM_EXTERNAL_JOB_ID) &&
		hasParameter(PRM_OPERATION) &&
		getParameter(PRM_OPERATION).value == "list"
	}
	
	/**
	 * Tests whether this is the 'run test list' operation
	 */
	boolean isTestRunBuild(){
		hasParameter(PRM_EXTERNAL_JOB_ID) &&
		hasParameter(PRM_OPERATION) &&
		hasParameter(PRM_NOTIFICATION_URL) &&
		hasParameter(PRM_TEST_SUITE_JSON) &&
		getParameter(PRM_OPERATION).value == "run"
	}
	
	// ********** unused methods : this is an invisible operation *************
	
	@Override
	public String getIconFileName() {null}

	@Override
	public String getDisplayName() {null}

	@Override
	public String getUrlName() {null}
	
	
	// ********** static classes etc ********************************************

	static interface BuildParameter extends Serializable{
		String getName()
	}
	
	@EqualsAndHashCode
	static class StringBuildParameter implements BuildParameter{
		String name
		String value
		
		StringBuildParameter(){
			
		}
		
		StringBuildParameter(String name, String value){
			this.name = name
			this.value = value
		}
		
		@Override 
		String toString(){
			"${name}:${value}"
		}
	}
	
	static class FileBuildParameter implements BuildParameter{
		String name
		transient FileItem fileItem
		
		// location of the uploaded file
		String location
		// true : the location is an absolute path, false : relative to
		boolean absoluteLocation
		
		FileBuildParameter(){
			
		}
		
		FileBuildParameter(String name, FileItem item){
			this.name = name
			this.fileItem = item
		}
		
		
	}
	
}
