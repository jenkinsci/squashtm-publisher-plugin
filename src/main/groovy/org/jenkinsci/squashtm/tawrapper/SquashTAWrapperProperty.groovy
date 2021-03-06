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

import hudson.Extension
import hudson.model.AbstractProject
import hudson.model.Job
import jenkins.model.OptionalJobProperty
import jenkins.model.OptionalJobProperty.OptionalJobPropertyDescriptor
import org.jenkinsci.squashtm.lang.Messages
import org.kohsuke.stapler.DataBoundConstructor

/**
 * That class is the checkbox 'Enable TM integration', and publish the much important {@link StaplerBuildOverride}.
 *
 */
class SquashTAWrapperProperty extends OptionalJobProperty<Job<?,?>>{

	@DataBoundConstructor
	SquashTAWrapperProperty(){
		
	}
	
	@Override
	public Collection<?> getJobOverrides(){
		return [new StaplerBuildOverride()]
	}
	
	@Extension
	public static class DescriptorImpl extends OptionalJobPropertyDescriptor{
	
		@Override
		public boolean isApplicable(Class<? extends Job> jobType){
			jobType in AbstractProject
		}
		
		@Override
		public String getDisplayName(){
			return  Messages.tmpublisher_job_TMintegration()
		}
			
	}
}
