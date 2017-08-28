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
 * <h2>How the old school TM / TA job configuration is emulated</h2>
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
 *             <th>Config item</th>
 *             <th>Config location</th>
 *             <th>Purpose</th>
 *         </tr>
 *     </thead>
 *
 *     <tbody>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>Job parameters</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  Various parameters (6 exactly) that drive the build and how results are pushed back to TM. These parameters are then passed to the maven command. Note that even the maven goal
 *                  can be specified here (the now-deprecated 'test-list', or 'run').
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>Maven goals</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  The maven command. Most of the properties fed on the command line come from the parameters (as explained above), but also more flags, and eventually user-defined
 *                  properties if required.
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>HTML publisher : results</td>
 *              <td>Jenkins job conf</td>
 *              <td>Publishes on Jenkins the global test report via the HTML publisher plugin. That report is the default, native report produced by Squash TA.</td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>HTML Publisher : test list</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  Exposes the test list (a json file) produced by Squash TA as a HTML report so that Squash TM can query it, via the HTML publisher plugin.
 *                  It is consumed in the use-case "link an automated test to a Squash TM test case".
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>Include the TA global config file</td>
 *              <td>Jenkins job conf</td>
 *              <td>
 *                  The listing of Squash TM instances and how to login them.
 *                  This file is passed to the process as a system property in the Maven Goals along with the rest of the parameters.
 *                  Consumed in the use-case "pushing the test results on Squash TM".
 *              </td>
 *          </tr>
 *
 *          <tr style="border-bottom: 1px solid black;">
 *              <td>TODO : the maven conf part</td>
 *              <td></td>
 *              <td></td>
 *          </tr>
 *
 *      </tbody>
 *
 * </table>
 *
 *
 *
 *
 */
package org.jenkinsci.squashtm.tawrapper;