# What is it #


### Description ###
This plugin publishes the test results of a Jenkins build on Squash TM. It works on top of other publisher plugins dedicated to the test technology that used in the build (junit, nunit, mstest etc), before pushing the results.


### Limitations ###
In this version, only the passive mode is implemented (see Workflow below).


### Workflow ###
This plugin (will) support two different publishing : passive and active. In the current version only the passive mode is implemented. You can enable both modes in the same job.


**Passive mode :**

The plugin will publish the results of a build **only if it was triggered by Squash TM**, while builds triggered by other cues will be ignored (e.g. scheduled jobs or direct user input in Jenkins GUI). Functionally it mimics the interactions between Squash TM and a Squash TA job ([see user documentation here](https://sites.google.com/a/henix.fr/wiki-squash-ta/tm---ta-guide/user-guide)).


**Active mode (not implemented yet):**

The plugin will publish the results of any build, even when Squash TM did not explicitly started it.



### Dependencies ###
This plugin requires Jenkins version 1.651.3, and the JUnit plugin version 1.19+ must be installed (not tested with earlier versions).


# Usage #

## Global settings ##

As an administrator, go to the system configuration (first item in the administrator view). Scroll down to the TM Publisher section. From there you can add one or several TM servers by clicking on the 'add' button and filling the following informations : 

* server name : a label that identify a given instance of Squash TM
* server URL : the base URL of the TM server
* login : the login of the account on Squash TM that Jenkins will use to push the results. NB : that user must belong to the 'Test Automation Server' user group.
* password : the password of that user.

These will allow the plugin to identify instances of Squash TM that request for result update, and to authenticate on them.

The 'validate' button (next to 'delete') will test that the server is reachable and up. For now the credentials aren't validated yet. Potential errors in your configuration will issue a warning. Once you are done, save the configuration as usual. You can save the configuration even if warnings were reported.

## Job Settings ##

As a job manager, go to the configuration page of your job.

Jobs of all nature are supported. As a job manager, go to the configuration page of your job. In order to work the plugin requires two simple items. First, on the general job properties, tick the box 'enable Squash TA wrapper'. Then add a new post-build step : 'publish your results on Squash TM'. Note : A subconfiguration panel appears, asking your which servers should be notified. Currently this subfeature has no effect so you can just leave it as is.

That's all you need to configure TM-publisher per-se. However you will also need to configure additional test result publishers such as [JUnit plugin](https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Plugin), [NUnit plugin](https://wiki.jenkins-ci.org/display/JENKINS/NUnit+Plugin), [JSUnit plugin](https://wiki.jenkins-ci.org/display/JENKINS/JSUnit+plugin) etc. Remember that TM-publisher simply pushes tests results, it does not generate them.

Note that 'Maven project' job style implicitly handle jUnit test results : if you run a Maven job and your tests are run by a jUnit runner you don't need to configure extra test results publishers.


## Squash TM configuration ##

You will find a detailed procedure hosted on the [Squash TA documentation site](https://sites.google.com/a/henix.fr/wiki-squash-ta/tm---ta-guide/2---configuration/configuring-tm/from-1-13-0). That documentation details the steps to :

* declare an instance of Jenkins,
* declare a system user for Jenkins,
* make projects automation-ready

# Comparison with Squash TA #

The main user stories implemented by a regular Squash TM-TA stack are fullfiled by the TM-publisher, but not all. Because the TM-publisher plugin makes no assumption about the actual build tools and test runners involved in the Jenkins build, many advanced features from Squash TA are not available here due to integration problems.

The following features are supported : 

* TM user can bind a test case to a remote test hosted on Jenkins
* TM user can start automated test suites
* Test statuses are updated automatically on build completion

The following features are **NOT** supported :

* Passing datasets to the automated tests
* Running only a subset of the test suite : the job will run entirely and all tests will be run
* Dispatch the tests on available slave nodes
* Running tests in a specified order
* Real-time status updates : all results will be pushed at the end of the build

---

# Developper documentation #
## Requirements ##

* java jdk, version 1.7 minimum
* groovy, version 1.8
* Gradle, version 2.14 prefered but not critical.

## Installation ##

Download the project sources and install them in your favourite IDE.
Then generate the gradle wrapper : 

```
gradle wrapper --gradle-version <your favorite version>
```

From now on, all builds should use `gradlew` (instead of gradle)

## Useful build commands ##

Please refer to [this doc site](https://wiki.jenkins-ci.org/display/JENKINS/Gradle+JPI+Plugin)

* build : `gradlew build`
* package : `gradlew jpi`
* unit tests : `gradlew test`
* run in server : `gradlew server` (access at http://localhost:8080)
* debug in server : `gradlewSrv` 
* push to maven repository : `gradlew publishToPrivateRepo`
* perform release : `gradlew release`

## Additional tools ##

### gradlewTest ###

This is a simple script that help you to run `gradlew test` in debug mode. See the comments inside and decide whether this useful to you.

### gradlewSrv ###

Note : gradlewSrv is a convenient shortcut that sets your JVM right, but also works around a bug due to gradle daemon. It also disable much of Jenkins default debugging options, which are rather performance demanding, however you may enable them if you need to (run gradlewSrv -help and see what is in there). See also next section (about hotswap).

### Support for hotswap in Eclipse ###

I could not get Eclipse to hotswap code out of the box for several reasons. I basically had to do the same as in [this thread](http://stackoverflow.com/questions/31127533/is-hot-code-replace-supposed-to-work-for-groovy-in-eclipse/31143994#31143994). The use of DCEVM + groovyReset handle nicely whatever Java bytecode your Groovy is translated to, which could be rejected by the native JPDA hotswap feature otherwise. Here is the checklist : 

1. Install a very specific version of the JDK, one supported by [DCEVM](https://dcevm.github.io/) extensions. For instance you will find an appropriate JDK 8 [here](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-javase8-2177648.html#jdk-8u92-oth-JPR)
2. Download the corresponding [DCEVM extension](https://dcevm.github.io/) and patch that JVM with it. You only need the JVM patch part, the HotswapAgent would not work here (Groovy is not supported yet).
3. Edit the script gradlewSrv accordingly (check JAVA_HOME and the flag -XXaltjvm, make the whole thing point to your patched JVM).
 
4. Edit your Eclipse .classpath and set :
```
	<classpathentry kind="output" path="build/classes/main"/>
```
instead of
```
	<classpathentry kind="output" path="bin"/>
```

This will tell Eclipse to watch this directory for class changes (thus trigger hotswap when appropriate). However we dont want it to mess with the output of the Gradle build, so step 5 is :

5. Disable 'Build automatically', or if you prefer hack in your file .project and remove the build commands that do compilation job.

With this setup, you can replace code by recompiling using `gradlew build -x test` then refreshing the build output directory so that Eclipse knows it must hotswap the changes. 