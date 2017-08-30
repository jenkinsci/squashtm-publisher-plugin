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

import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractProject
import hudson.model.Descriptor.FormException
import hudson.model.Run
import hudson.model.TaskListener
import hudson.tasks.BuildStepDescriptor
import hudson.tasks.BuildStepMonitor
import hudson.tasks.Notifier
import hudson.tasks.Publisher
import jenkins.tasks.SimpleBuildStep
import net.sf.json.JSONObject
import org.jenkinsci.Symbol
import org.jenkinsci.squashtm.lang.Messages
import org.jenkinsci.squashtm.tawrapper.SquashTAPoster
import org.jenkinsci.squashtm.tawrapper.TestListSaver
import org.jenkinsci.squashtm.utils.JobInformationsFactory
import org.kohsuke.stapler.DataBoundConstructor
import org.kohsuke.stapler.StaplerRequest

/**
 * @author bsiri
 */
public class SquashTMPublisher extends Notifier implements SimpleBuildStep{

	// TODO : also, ensure in the global configuration that no declared instance of Squash TM could be named like that
	public static final String DUMMY_SELECTED_SERVER_NAME = '$*µdummy!!:`';
	
	/**
	 * The identifiers of the TM servers that this build-specific instance of BuildStep should post to
	 * 
	 */
	private List<SelectedServer> selectedServers = [] as List

	@DataBoundConstructor
	public SquashTMPublisher(List<SelectedServer> selectedServers){
		super()
	
		// retain only those that are actually selected
		// and that are not the 'whatever' checkbox hack
		this.selectedServers = selectedServers.findAll { it.selected && it.identifier != DUMMY_SELECTED_SERVER_NAME }
	}
		
	/*
	 * Required because inherited from hudson.task.BuildStep. This says that we don't need to prevent Jenkins to use concurrent builds.
	 * Consider using another value if some day that changes.
	 */
	@Override
	public BuildStepMonitor  getRequiredMonitorService(){
		BuildStepMonitor.NONE
	}
	
	@Override
	public boolean needsToRunAfterFinalized() {
		true
	}
	
	@Override
	public PublisherStepDescriptor getDescriptor() {
		(PublisherStepDescriptor)super.getDescriptor()
	}
	
	
	// ********************* perform method ************************************************
	
	public void perform(Run build, FilePath workspace, Launcher launcher, TaskListener listener){
		
		def logger = listener.logger
				
		// create the job information
		logger.println "[TM-PLUGIN] : extracting job information"
		JobInformations info = JobInformationsFactory.create build, workspace
				
		// extract the test data
		logger.println "[TM-PLUGIN] : collecting test result data"		
		def extractor = new TestResultActionExtractor(
			build : build,
			info : info
		)
		
		def results = extractor.collectResults()
		
		
		// saving the test list for later use in the TestListAction
		if (info.usesTAWrapper){
			logger.println "[TM-PLUGIN] : TA support is enabled"
			
			logger.println "[TM-PLUGIN] : saving the test list"
			def saver = new TestListSaver(info, logger)
			saver.saveTestList results
		}
		
		/*
		 * posting the results : 
		 * - either as Squash TA if the wrapper is enabled (and the TA wrapper was invoked for a 'run tests' build, 
		 * - either as normal. 
		 * 
		 *  For now the normal case is not yet supported.	
		 */
		
		def conf = getDescriptor()
		
		if (info.usesTAWrapper && info.tawrapperParameters.isTestRunBuild()){
			// post as Squash TA
			def poster = new SquashTAPoster(conf.tmServers, info, logger)
			poster.postResults results
		
		}else{
			// post as normal (not yet supported)
			def poster = new ResultPoster()
			poster.postResults info, results, logger
		}
				
		logger.println "[TM-PLUGIN] : job done"
	}

	
	
	// ************************* Descriptor section *************************************

	/*
	 * This is the configuration object that appear in the system configuration,
	 * and allows to register instances of Squash TM globally.
	 *
	 * TODO :
	 * 	as the plugin grows, try to make that descriptor shareable between multiple
	 * 	extension using jenkins.model.GlobalConfiguration. I also like the name
	 * 	'GlobalConfiguration' because it's self-explanatory.
	 *
	 */
	@Extension
	@Symbol("squashtm")
	public static class PublisherStepDescriptor extends BuildStepDescriptor<Publisher>{
		
		List<TMServer> tmServers
		
		public PublisherStepDescriptor(){
			load();
		}
		
		private void reset(){
			tmServers = []
		}
		
		@Override
		public String getDisplayName() {
			Messages._tmpublisher_job_plugindescr()
		}
		
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			true
		}

		
		@Override
		public boolean configure( StaplerRequest req, JSONObject json ) throws FormException {
						
			// reinitialize the parameters
			reset()
			
			req.bindJSON(this, json)
			
			// we save the configuration even when it's invalid,
			// because dumping the user conf over a petty typo in a URL and not letting the user know is so jerk-ish
			// the user had a chance to validate before applying the configuration so let's assume he knows what he's doing.
			save();

			true
			
		}
	
	}

	
}
