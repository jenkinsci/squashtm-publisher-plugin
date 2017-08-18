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

import java.io.IOException
import java.util.Collection;

import javax.servlet.ServletException;

import org.kohsuke.stapler.HttpResponse
import org.kohsuke.stapler.WebMethod;

import groovy.json.JsonOutput
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job
import jenkins.model.TransientActionFactory;
import javax.annotation.Nonnull

class TestListAction implements Action {
	
	private transient Job job
		
	@Override
	public final String getUrlName() {
		"Test_list"
	}

	@Override
	public String getIconFileName() {
		null
	}

	@Override
	public String getDisplayName() {
		null
	}
	
	@WebMethod(name=["testTree", "testTree.json"])
	HttpResponse getTestTree(){		
		
		return { req, rsp, o ->
			def text = new File(job.buildDir, "testTree.json").text
			rsp.setContentType "application/json;charset=UTF-8"
			rsp.getWriter().println text
		} as HttpResponse
		
	}
	
	// ******************** the factory that registers it ********************
	
	@Extension
	public static final class TestListActionFactory extends TransientActionFactory<Job>{
		
		@Override
		public Class<Job> type(){
			Job
		}
	
		@Override
		public Collection<? extends Action> createFor(@Nonnull Job target){
			return [new TestListAction(job : target)]
		}
		
		
	}

}
