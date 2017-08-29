/**
 * <h1>Squash TA wrapper</h1>
 *
 * <h2>Overview</h2>
 * <p>
 *     This package provides optional features that integrates this build with Squash TM and/or Squash TA according to
 *     the Squash TM / Squash TA "protocol". It works by wrapping the job with a compatibility layer between Squash TM
 *     and the job, hence the name "TA wrapper". Future developments will include another - optional - feature
 *     that will also cover the exchanges between the job and Squash TA (if the job actually runs Squash TA).
 * </p>
 *
 * <p>
 *     <strong>Integration with Squash TM</strong>
 *
 *     <p>
 *     This is the most common role, that integrates any job and test technology with Squash TM. It provides the job with
 *     the necessary features to accomplish the following use-cases (that unfold across Squash TM and Jenkins) :
 *      </p>
 *
 *     <ol>
 *        <li>Binding an automated test run in Jenkins to a Squash TM test case</li>
 *        <li>Running a test-suite according to Squash TM specification and retrieving the results</li>
 *     </ol>
 *
 * </p>
 *
 * <p>
 *     <strong>Integration with Squash TA</strong>
 *
 *      <p>Note : in this version this feature is not implemented</p>
 *
 *      This role is complementary to the first one and needs to be enabled when the project is a Squash TA project only.
 *      It aims to facilitate the configuration of a Squash TA job, that can then benefit from various features that stems
 *      from the tighter integration (like handling datasets etc).
 * </p>
 *
 * <h2>How the old school TM / TA job configuration works</h2>
 *
 * <p>
 *     Let's review how TM / TA normally works before explaining what the plugin does, how and why.
 * </p>
 *
 * <p>
 *     The usual (and tedious) way to configure a TM / TA connection involve the customization of the Jenkins job as well as the mojo execution in the pom.xml itself.
 *     You can see <a href="https://bitbucket.org/nx/squash-ta-execution-server/raw/58bafc960a698c9eebe460d2b0b437245ef1bd6a/squash-ta-server/src/main/resources/noarch/template/config.xml">an example of config.xml here</a> for the job configuration, and <a href="https://bitbucket.org/nx/squash-ta-new-engine/raw/20f0048b15c6d51f9659e06e5344881b0842b3aa/api/squash-ta-project-archetype/src/main/resources/archetype-resources/pom.xml">a template pom.xml </a>
 *     for the maven mojo configuration. We can break these down as follow :
 * </p>
 *
 * <table style="border: 1px solid black; border-collapse:collapse;">
 *
 *     <thead>
 *         <tr style="border-bottom: 1px solid black; border-collapse:collapse;">
 *             <th>#</th>
 *             <th>Config item</th>
 *             <th>Config location</th>
 *             <th>Purpose</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>1</td>
 *              <td>Job parameters</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  <strong>Parameters</strong> (6 exactly) that drive the build and how results are pushed back to TM. These parameters are then passed to the maven command. Note that even the maven goal
 *                  can be specified here (the now-deprecated 'test-list', or 'run').
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>2</td>
 *              <td>Include the TA global config file</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  The listing of Squash TM instances and the necessary credentials to log in them.
 *                  This file is passed to the process as a system property in the Maven Goals along with the rest of the parameters.
 *                  Consumed in the use-case "pushing the test results on Squash TM".
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>3</td>
 *              <td>Maven goals</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  The maven command. Most of the properties fed on the command line come from the parameters (as explained above), but also more flags, and possibly other user-defined
 *                  properties.
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>4</td>
 *              <td>HTML Publisher : test list</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  Exposes the <strong>test list</strong> (a json file) produced by Squash TA as a HTML report so that Squash TM can query it, via the HTML publisher plugin.
 *                  This is an important resource that is consumed in the use-case "link an automated test to a Squash TM test case".
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>5</td>
 *              <td>HTML publisher : results</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  Publishes on Jenkins the global test report via the HTML publisher plugin. That report is the default native report produced by Squash TA. This
 *                  item is mentioned here for the sake of completion but is unrelated to our scenario of interest here.
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>6</td>
 *              <td>Surefire exporter</td>
 *              <td>Maven pom</td>
 *              <td>A plugin for the Squash TA Mojo that <strong>prints the test results to JUnit files</strong>, in addition to the aforementioned standard report.
 *              Now, on Jenkins' side, the standard behavior with Maven Job types
 *              (which a regular Squash TA is) is to automatically export the JUnit test results (no JUnit plugin needs to be explicitly configured). That's a good
 *              thing because among other things the URL to each individual test result page is part of the response expected by Squash TM.
 *              </td>
 *          </tr>
 *
 *          <tr>
 *              <td>7</td>
 *              <td>TM Callback</td>
 *              <td>Maven pom</td>
 *              <td>
 *                  Another plugin for the Squash TA Mojo, that actually notify Squash TM with the test results, i.e. it <strong>actually performs the HTTP posts</strong>. Much of the build parameters
 *                  declared in the Jenkins job configuration, then passed on to the build in the Maven Goals command line, are consumed here.
 *              </td>
 *          </tr>
 *
 *
 *      </tbody>
 *
 * </table>
 *
 * <br/>
 *
 * <p>
 *     With this in mind, here are the details of the workflow for "running a test suite and getting the result back" :
 *
 *     <ol>
 *         <li>A Squash TM user : starts a test suite using the GUI</li>
 *         <li>
 *             the Squash TM server : defines the test-suite encompassed by the user selection and the dataset they should run with.
 *             Then dispatches it to Jenkins as part of a POST "thejob/build" as an attached file, along with other parameters such as its callback URL, an external
 *             job identifier used to track the build, the maven goal ("run") etc. These are the Jenkins job parameters defined in
 *             <strong>item #1</strong>. End of Squash TM process.
 *         </li>
 *         <li>
 *             Jenkins : receives the build command, process the parameters <strong>(#1)</strong> and forwards them to the Maven Goals
 *             command line <strong>(#3)</strong>.
 *         </li>
 *
 *         <li>
 *             Maven : configures in turn the Squash TA Mojo with these parameters then run it (with the goal "run").
 *         </li>
 *
 *         <li>
 *             Squash TA : runs the test, and as the build output produces among other things the JUnit files <strong>(#6)</strong> and updates the test list <strong>(#4)</strong>.
 *         </li>
 *         <li>
 *             Squash TA : guesses (!) the URL where Jenkins will expose the JUnit test results, then posts the results to Squash TM using the callback information
 *             from the initial build request, and the credential files supplied in item <strong>#2</strong>.
 *             End of Squash TA process.
 *         </li>
 *         <li>
 *             Maven : end of Maven build.
 *         </li>
 *         <li>
 *             Jenkins : the post build steps kick-in and exposes the test list <strong>(#4)</strong> at the job-relative url '/Test_list/testTree.json',
 *             and the standard report <strong>(#5)</strong> and the JUnit results <strong>(#6)</strong>
 *             (note : although that step is not explicitly configured that way, this is a native behavior for Maven Jobs).
 *             End of Jenkins build.
 *         </li>
 *     </ol>
 *
 * </p>
 *
 * <h2>Integration with Squash TM : what the plugin does and how</h2>
 *
 * <p>
 *     The TM Integration features aims to masquerade any job as a Squash TA job, by mimicking some of the elements in the table above.
 *     Specifically we are interested with the bold elements. Indeed, from Squash TM perspective what is really required is :
 *
 *     <ul>
 *         <li>query the test list</li>
 *         <li>sending HTTP POST "thejob/build" with parameters (note : Jenkins fails when request contain unknown parameters)</li>
 *         <li>receiving test results (status, message, URL for the result page on Jenkins etc)</li>
 *     </ul>
 *
 * </p>
 *
 * <p>
 *     To that end, the plugin exposes a single job property (named 'Enable TM integration') that tweaks the main post-build-step
 *     (see the core module).
 * </p>
 *
 *
 * <h3>Build story : creating, exposing the test list</h3>
 *
 * <h4>Functional description</h4>
 *
 * <p>
 * As a build step, I want to print the test list on disk.
 * As a Jenkins Action, I want to expose that test list as a web resource of the job.
 * </p>
 *
 * <h4>Implementation</h4>
 *
 * <p><strong>classes (src/main/groovy)</strong></p>
 *
 * <ul>
 *     <li>
 *         {@link org.jenkinsci.squashtm.tawrapper.TestListSaver} : that class consumes the test results and writes them to the disk.
 *         It does so via a FileCallable as advised by the Jenkins developers and ensures that the file is available on the master instance.
 *         It is invoked as an optional step during {@link org.jenkinsci.squashtm.core.SquashTMPublisher#perform(hudson.model.Run, hudson.FilePath, hudson.Launcher, hudson.model.TaskListener)}.
 *     </li>
 *     <li>{@link org.jenkinsci.squashtm.tawrapper.TestListAction} : is the action that publishes the test list. It also define the action factory that registers it.
 *     This action is invisible in the user interface : it just staples the file to the job-relative url '/Test_list/testTree.json'.
 *     </li>
 * </ul>
 *
 *  <p><b>resources (src/main/resources)</b></p>
 *
 * <p>none</p>
 *
 * <h3>Build story : posting the test results</h3>
 *
 * <h4>Functional description</h4>
 *
 * <p>
 *     As a build step, I want to notify Squash TM with the test results using the same HTTP service than Squash TA would use.
 * </p>
 *
 * <h4>Implementation</h4>
 *
 * <p><strong>classes (src/main/groovy)</strong></p>
 *
 * <ul>
 *     <li>
 *         {@link org.jenkinsci.squashtm.tawrapper.SquashTAPoster} : it recasts the test results in the expected format and posts them.
 *         It looks up in the global config wich Squash TM should be notified using the notification url (one of the build parameters)
 *         and if an instance if found, uses the associated credentials. It is invoked by the main build step
 *         when the the build runs in a TA wrapper context.
 *     </li>
 * </ul>
 *
 * <p><b>resources (src/main/resources)</b></p>
 *
 * <p>none</p>
 *
 * <h3>Jenkins story : identifying and honoring a POST /build request</h3>
 *
 * <h4>Functional description</h4>
 *
 * <p>
 *     As Jenkins, I want to recognize HTTP requests that match a Squash TA build request and then run the said build
 *     in a TA wrapper context (instead of crashing). I do so by :
 *
 *     <ul>
 *         <li>inspecting the POST parameters and comparing with a TA build signature</li>
 *         <li>
 *             if the parameters match that of a TA build, dynamically inject them as {@link org.jenkinsci.squashtm.tawrapper.TAParametersAction}
 *             to the build context even though they do not explicitly appear in the job configuration. I am careful
 *             of other existing {@link hudson.model.ParametersAction}, which take precedence in case of conflict.
 *         </li>
 *     </ul>
 *
 *
 * <p><strong>classes (src/main/groovy)</strong></p>
 *
 *  <ul>
 *      <li>
 *          {@link org.jenkinsci.squashtm.tawrapper.SquashTAWrapperProperty} : this class defines the job property that enables the wrapper, and in particular the job override
 *          (see {@link org.jenkinsci.squashtm.tawrapper.StaplerBuildOverride}).
 *      </li>
 *      <li>
 *          {@link org.jenkinsci.squashtm.tawrapper.StaplerBuildOverride} : as the name suggests, this class overrides the regular Stapler hook.
 *          It handles the POST parameters and prepare the corresponding {@link org.jenkinsci.squashtm.tawrapper.TAParametersAction}. See its documentation for more details.
 *          Note : it passes the parameters to the {@link org.jenkinsci.squashtm.tawrapper.ParameterInjector} via a ThreadLocal because the StaplerBuildOverride
 *          cannot inject the parameters in the Build itself (it doesn't exist yet).
 *      </li>
 *      <li>
 *          {@link org.jenkinsci.squashtm.tawrapper.TAParametersAction} : a bunch of classes that represent our parameters. They implement {@link hudson.model.Action},
 *          which allows them to add them to the build actions.
 *      </li>
 *      <li>
 *          {@link org.jenkinsci.squashtm.tawrapper.ParameterInjector} : as an implementation of {@link hudson.model.Queue.QueueDecisionHandler}, its main mission here
 *          is to add our {@link org.jenkinsci.squashtm.tawrapper.TAParametersAction} to the other actions of the build before it starts. It does so by looking into
 *          the ThreadLocal of the StaplerBuildOverride. This is how the parameters become available
 *          to the main build step ({@link org.jenkinsci.squashtm.core.SquashTMPublisher}). As a side mission I also resolve the file parameters (remember that the test suite
 *          descriptor is a file). See the javadoc for details.
 *      </li>
 *  </ul>
 *
 * </p>
 *
 *
 * <h2>Integration with Squash TA : what the plugin does and how</h2>
 *
 * <p>
 *     Nothing yet !
 * </p>
 *
 */
package org.jenkinsci.squashtm.tawrapper;