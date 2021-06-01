# Developer documentation #
## Requirements ##

* Java jdk, version 1.7+
* Groovy, version 1.8+
* Gradle, version 2.14 used during the development preferred but not critical.

## Installation ##

Download the project sources and install them in your favourite IDE.
Then generate the gradle wrapper : 

```
gradle wrapper --gradle-version <your favorite version>
```

From now on, all builds should use `gradlew` (instead of gradle)

## Documentation ##

The various src/*/package-info.java contains an overview of the mission and code
structure, along with references to the important classes (which sometimes also have
their own doc).

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

This will tell Eclipse to watch this directory for class changes (thus trigger hotswap when appropriate). However we dont want it to mess with the output of the Gradle build, hence the next step.

5. Disable 'Build automatically', or if you prefer hack in your file .project and remove the build commands that do compilation job.

With this setup, you can replace code by recompiling using `gradlew build -x test` then refreshing the build output directory so that Eclipse knows it must hotswap the changes. 