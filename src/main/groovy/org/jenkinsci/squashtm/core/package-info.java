/**
 * <h1>Squash TM Publisher core</h1>
 * 
 * <h2>Overview</h2>
 * 
 * <p>
 * 	This package contains the main components of plugin, including the publisher itself. These components implement 
 * the following use cases :
 * 
 *  <ol>
 *  	<li>User story : general configuration</li>
 *  	<li>User story : configure the publisher</li>
 *  	<li>Build story : publish the results</li>
 *  </ol>
 *  
 *  The rest of this documentation will explain how each of these are implemented and give hints on how these classes work. 
 *  This package documentation focuses on the core use cases (configuration and result publishing). However the core use
 *  cases may be altered when the job enabled the TA wrapper (which is be covered in the dedicated package {@link org.jenkinsci.squashtm.tawrapper}.
 *  Ironically, in its first version 1.0.0, <b>only the Squash TA wrapper is operational. The nominal use case is not
 *  completed yet.</b>
 * </p>
 * 
 * <h2>User story : general configuration</h2>
 * 
 * <h4>Functional description</h4>
 * 
 * <p>
 * 	As a Jenkins administrator, I need to configure the TM publisher plugin with a list of Squash TM servers. 
 * 
 *  <ol>
 *  	<li>I navigate to the global configuration page and scrolls down to the TM publisher section.</li>
 *  	<li>I click on 'add a server'</li>
 *  	<li>I fills the form : name, url, logi/password of the account on Squash TM. Remember that this account must belong to the group Test Automation Server on Squash TM.</li>
 *  	<li>I click on validate, and fixes the errors if any </li>
 *  	<li>I may repeat step 2 and onward if more servers should be registered </li>
 *  	<li>Finally I click on 'save' button</li>
 *  </ol>
 * </p>
 * 
 * <h4>Implementation</h4>
 * 
 * <p>
 * 	This is implemented using the common Jenkins framework, as described in the documentation. Here are the main elements involved in this story :
 * </p>
 * 	
 * <p><b>classes (src/main/groovy)</b></p>
 * 	<ul>
 * 		<li>{@link org.jenkinsci.squashtm.core.SquashTMPublisher.PublisherStepDescriptor} : this is the extension itself,
 * 		and the global configuration object. It contains the data persisted by Jenkins and the validation logic.</li>
 * 		<li>{@link org.jenkinsci.squashtm.core.TMServer TMServer} : these beans are the items configured in this use case</li>
 * 	</ul>
 * 
 *  <p><b>resources (src/main/resources)</b></p>
 * <ul>
 * 		<li>org.jenkinsci.squashtm.core.SquashTMPublisher/global.groovy : this is the main template. It is essentially a repeater of the TMServer template.</li>
 * 		<li>org.jenkinsci.squashtm.core.TMServer/config.goovy : this is the template for one server entry.</li>
 *  </ul>
 *  
 * </p>
 * 
 * <h2>User story : configure the publisher</h2>
 * 
 * <h4>Functional description</h4>
 * 
 * <p>
 * 	As a project/job manager, I want to add the TM publisher step to my build
 * 
 * 	<ol>
 * 		<li>I navigate to the job configuration page</li>
 * 		<li>I scroll down until the 'add a post build action' buttonmenu</li>
 * 		<li>In the dropwdown menu, I select the action 'publish test results on Squash TM'</li>
 * 		<li>(NOTE : the UI for this step has been disabled because the feature is still under development)
 * 		In the panel that just appeared, I tick the checkbox for each server that should be notified on build completion.</li>
 * 		<li>I also configure the other test result parsers that are relevant for my build (JUnit exporter, NUnit etc)</li>
 * 	</ol>
 * </p>
 * 
 * <h4>Implementation</h4>
 * 
 * <p>
 * 	This too is implemented using the Jenkins conventions so I wont expand much on this here. Please refer to https://wiki.jenkins-ci.org/display/JENKINS/Extend+Jenkins. 
 *  for details. Here are the main elements : 
 * </p>
 * 	
 * <p><b>classes (src/main/groovy)</b></p>
 * 
 * <ul>
 * 	<li>{@link org.jenkinsci.squashtm.core.SquashTMPublisher SquashTMPublisher} : this is the object being configured here. Other than that, this class has little noticeable 
 * code in this user story except for its constructor and the attribute 'selectedServers'.</li>
 * 	<li>{@link org.jenkinsci.squashtm.core.SelectedServer SelectedServer} : these are the items persisted by Jenkins. 
 * 	Note that only the items that were checked by the user will be persisted.</li>
 * </ul>
 * 
 *  <p><b>resources (src/main/resources)</b></p>
 *  
 *  <ul>
 *  	<li>org.jenkinsci.squashtm.core.SquashTMPublisher/config.groovy : this is the template that generate a check box for each TM server that was configured 
 *  		at the global level (see the previous user story), and eventually pre-checks those that were already selected (in this user story)</li>
 *  </ul>
 * 
 * <p><b>comments</b></p>
 * 
 * <p>
 * 	The view template always generate a dummy entry (an invisible checkbox for a dummy server). This is a trick that force the client to serialize
 *  the selected servers as an array when saving the form. Indeed, when only one element is present the client would serialize it as a single bean,  
 *  which in turn would trigger parsing exceptions on the server because it always expects an array. Pushing a dummy into the list ensures that 
 *  there will always be at least two elements in the list (accounting for the user selection).
 * </p>
 * 
 * <h2>Build story : publish the results</h2>
 * 
 * <h4>Functional description</h4>
 * 
 * <p>
 * 	As a build step, I want to gather the test results and post them to Squash TM. If the build was triggered using the Squash TM-TA connector and 
 * the TA wrapper is enabled, I will respond to TM via the same API than Squash TA, otherwise I will send the data to the generic API.
 * 
 * 	<ol>
 * 		<li>When the build is finished, I kick-in regardless of the build status</li>
 * 		<li>I collect the test results by inspecting the TestResultAction, and convert them into my own result type</li>
 * 		<li>If the Squash TA wrapper enabled, I print the test list on the disk. 
 * 			It'll be used in other stories of the TA mode (see package org.jenkinsci.squashtm.tawrapper)</li>
 * 		<li>I send the results to Squash TM, either using the normal mode or the TA mode (there again depending on whether the TA wrapper)</li>
 * 	</ol>
 * 
 * </p>
 * 
 * <h4>Implementation</h4>
 * 
 * <p><b>classes (src/main/groovy)</b></p>
 * 
 * <ul>
 * 		<li>{@link org.jenkinsci.squashtm.core.SquashTMPublisher SquashTMPublisher} : we focus here on the method {@link org.jenkinsci.squashtm.core.SquashTMPublisher#perform(hudson.model.Run, hudson.FilePath, hudson.Launcher, hudson.model.TaskListener) perform(...)}, 
 * 		this is where all the fun happen. </li>
 * 		<li>{@link org.jenkinsci.squashtm.core.JobInformations JobInformations} : this is a bean that stores the most needed attributes 
 * 			of the project and its current build, things like URL, names, usage of TA wrapper etc that are usually scattered around.</li>
 * 		<li>{@link org.jenkinsci.squashtm.utils.JobInformationsFactory JobInformationsFactory} : it just creates instances of JobInformations for you.</li>
 * 		<li>{@link org.jenkinsci.squashtm.core.TestResultActionExtractor TestResultActionExtractor} : its purpose is to inspect the test results of the build.</li>
 * 		<li>{@link org.jenkinsci.squashtm.core.TestResult TestResult} : this is a result bean, and they are created by the extractor</li>
 * 		<li>{@link org.jenkinsci.squashtm.core.ResultPoster ResultPoster} : this class posts the TestResult to Squash TM using the regular API.</li>
 * </ul>
 * 
 *  <p><b>resources (src/main/resources)</b></p>
 * 
 * <p>none</p>
 * 
 * <p><b>comments about gathering the results</b></p>
 *  
 * <p>
 * 	First, a few words on how Jenkins process the test results. Depending on the project configuration Jenkins will include in its build 
 *  several {@link hudson.model.Action}. Despite the name an Action does not always act on things : some objects are labelled Action 
 *  just for the sake of being made available in the build context. One such Action is {@link hudson.tasks.test.AbstractTestResultAction AbstractTestResultAction} 
 *  and its subclass tree : it doesn't actually parse the test result but merely contain them (these actions are created by other, specific build steps
 *  and the results are eventually written to disk when the build is complete).That is where our plugin will look for test results, and its the job
 *  of the TestResultActionExtractor introduced above.
 * </p>
 * 
 * <p>
 * 	There are many implementations, that deal with different file format : junit, nunit, trx, you name it. In practice most of them parse their own file then delegate to 
 *  the JUnit plugin for the rest of the work (writing to the disk, publishing etc). This is why our plugin never have to parse the result files itself,
 *  as long as other, relevant plugin were added to the same build (see the use case on the job configuration).
 * </p>   
 * 
 * <p>
 * 	The family of TestResultAction branch in two main types : single and aggregate. Single is what you expect : all your tests are gathered in that one place.
 * 	The aggregate type is used in split projects, for instance in Maven multimodule projects, or when aggregating the results of downstream builds. 
 * 	In that case the result extractor has to recursively check the sub-builds and inspect their test result action.
 * </p>
 * 
 * <p><b>comments about posting the results</b></p>
 * 
 * <p>
 * 	For now (version 1.0.0), it just not work. Only the TA Mode works. More details on this in the dedicated package.
 * </p>
 * 
 */
package org.jenkinsci.squashtm.core;


