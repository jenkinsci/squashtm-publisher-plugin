/**
 *
 * <h1>Overview</h1>
 *
 * <p>
 *     <b>Warning</b> : this documentation was written for the version <b>1.0.0</b> and some elements might be obsolete by the
 *     time you read it. Also it will help if you toyed with the plugin as a user (the documentation assumes that you understand
 *     the screen and workflow, please refer to the Readme.md).
 * </p>
 *
 * <h2>Current state (version 1.0.0)</h2>
 * <p>
 *     The long term functional objectives (the roadmap) of this plugin are :
 *
 *     <h5>Tighter integration of a job with Squash TM</h5>
 *     <p>
 *         For now, the plugin must publish the available tests (no just the results) so that TM can browse them.
 *     </p>
 *
 *     <h5>Tighter integration with Squash TA (for Squash TA projects)</h5>
 *      <p>
 *          A job dedicated to Squash TA requires extra job configuration (build parameters, publishers, additional command line arguments etc).
 *          This configuration is cumbersome and error-prone. The plugin must simplify this as much as possible.
 *      </p>
 *
 *     <h5>Push the test results of a build to Squash-TM, either after each build or on demand</h5>
 *     <p>
 *         On completion a build should notify TM of what test was run, in which context and join the execution statistics
 *         (status, runtime etc).
 *     </p>
 *
 *
 *      What it does for now (version 1.0.0) is :
 *      <ul>
 *          <li>tighter integration with TM : complete</li>
 *          <li>push the test results only when Squash requests them : complete</li>
 *          <li>push test results continuously : development barely initiated, implemented features currently disabled</li>
 *          <li>tighter integration with Squash TA : development barely initiated, implemented features currently disabled</li>
 *      </ul>
 *
 * </p>
 *
 * <h2>Overall code structure</h2>
 *
 *
 * <h3>core functionality </h3>
 * <p>
 *     The core functionality (pushing the test results) is the post-build step : 1/ it looks for instances
 *     of hudson.tasks.test.AbstractTestResultAction in the build context and extract the test statistics from them,
 *     then 2/ publish them in Squash TM. The target instances of Squash TM are registered in the global settings.
 * </p>
 *
 * <p>
 *     As stated in the above, the phase "2/ publish in Squash TM" is only partially implemented. It works only when
 *     optional job property labelled "Enable integration with Squash TM" is on. More on this right below.
 * </p>
 *
 * <h3>TA wrapper</h3>
 * <p>
 *      The other functionalities, eg tighter integration with TA and TM, are optional and enabled
 *      via a job property. That job property is labelled 'Enable integration with TM' for the user, but is essentially
 *      a wrapper around the job that will make it behave like a Squash TA job : it allows Squash-TM to interact with the
 *      job just like it would with a Squash-TA-ready job (namely, all the parameters and stuffs that are usually required,
 *      see for example config.xml in https://bitbucket.org/nx/squash-ta-execution-server/raw/58bafc960a698c9eebe460d2b0b437245ef1bd6a/squash-ta-server/src/main/resources/noarch/template/config.xml).
 *      Hence the name of TA wrapper.
 *
 *      Both TA and non-TA projects benefit from it. In this state the plugin is ready for non-TA projects, however for TA
 *      projects more developments to achieve full integration are needed.
 *
 * </p>
 *
 * <h1>Overall package structure</h1>
 * <p>
 *      The code is laid in two modules :
 *
 *      <ul>
 *          <li>core : contains the main extensions and configuration beans, which together define the core logic of the plugin.
 *          See the package-info.java</li>
 *          <li>tawrapper : all the code specific to the tawrapper.</li>
 *      </ul>
 *
 *      Please refer to their respective package-info.java for a detailed explanation of what is going on.
 *
 * </p>
 *
 */
package org.jenkinsci.squashtm;