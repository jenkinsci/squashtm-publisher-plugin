@REM ---------------------------------------------------------
@REM This script helps with running the test Jenkins server
@REM
@REM General Options :
@REM -help : show this help
@REM -dryrun : simply displays the command line that will run, instead of actually running it 
@REM -dcevm : use dcevm runtime (if installed)
@REM
@REM Jenkins Options :
@REM -jenport <port> : change the port for Jenkins
@REM -nojellycache : disable the jelly cache
@REM -staplertrace : enable stapler trace
@REM -debugyui : enable yui debug
@REM -(other) : other options will be passed along to the spawned process. 
@REM
@REM Debug Options :
@REM -nodebug : disable the debugger and the options below
@REM -dbgport <port> : change the port of the java debugger 
@REM -logclasspath : will tell the JVM to log the classpath (useful to troubleshoot problems with hotswap)
@REM
@REM Note : the server is run using --no-daemon due to https://issues.jenkins-ci.org/browse/JENKINS-33336
@REM ---------------------------------------------------------


@echo off
setlocal 

@REM -------------- help --------------------------------

if [%~1] == [-help] (
	echo.
	echo.
	echo gradlewSrv : start the test Jenkins server at "http://localhost:<port>/jenkins"
	echo.
	echo default is starting the JVM with debug tools on port 8000, using the groovy hostwap agent. Other Jenkins debugging options are disabled.
	echo.
	echo -------------
	echo General Options ^: 
	echo.
	echo 	-help ^: show this help
	echo 	-dryrun ^: simply displays the command line that will run, instead of actually running it
	echo	-dcevm ^: use the dcevm runtime ^(if installed^)
	echo.
	echo Jenkins Options ^:
	echo 	-jenport ^<port^> : changes the port for Jenkins
	echo 	-nojellycache ^: disable the jelly cache
	echo 	-staplertrace ^: enable stapler trace
	echo 	-debugyui ^: enable yui debug
	echo 	-anything ^: other options will be passed along to the spawned process.
	echo.
	echo Debug Options ^:
	echo 	-nodebug ^: the JVM will not run with debugging option
	echo 	-dbgport ^<port^> : changes the port of the java debugger
	echo 	-logclasspath : will tell the JVM to log the classpath ^^(useful to troubleshoot problems with hotswap^)
	echo.
	echo.

	exit /B 0
)

@REM -------------- init variables ----------------------

REM set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_92
set JENKINS_PORT=8080
set JAVA_DBG_PORT=8000
set GRADLE_OPTS=-Xdebug -Xmx1024m -XX:MaxPermSize=512m -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=%JAVA_DBG_PORT
set LOG_CP=
set DCEVM=0

set JELL_CACHE=-Dstapler.jelly.noCache=false
set STAPL_TRACE=-Dstapler.trace=false
set DEBUG_YUI=-Ddebug.YUI=false
set OTHER_OPTS=

set DRY_RUN=0
set DEBUGMODE=1

@REM -------------- parse the arguments -----------------

:loop
IF [%~1]==[] GOTO cont


IF /I [%~1]==[-dryrun] (
	set DRY_RUN=1

) ELSE IF /I [%~1]==[-dcevm] (
 	set DCEVM=1
 
) ELSE IF /I [%~1]==[-nodebug] (
	set debugmode=0

) ELSE IF /I [%~1]==[-dbgport] (
	set JAVA_DBG_PORT=%~2
	SHIFT

) ELSE IF [~1]==[-usevm] (
	set JAVA_HOME=%~1

) ELSE IF [%~1]==[-logclasspath] (
	set LOG_CP=-XX:+UnlockDiagnosticVMOptions -XX:LogFile=jvmlogs -XX:+TraceClassLoading

)ELSE IF /I [%~1]==[-jenport] (
	set JENKINS_PORT=%~2
	SHIFT

) ELSE IF /I [%~1]==[-nojellycache] (
	set JELL_CACHE=

) ELSE IF /I [%~1]==[-staplertrace] (
	set STAPL_TRACE=

) ELSE IF /I [%~1]==[-debugyui] (
	set DEBUG_YUI=

) ELSE set OTHER_OPTS=%OTHER_OPTS% %~1%


SHIFT & GOTO loop

:cont

REM configure the gradle options : debugger, agents etc

set GRADLE_OPTS=%LOG_CP% -Xmx1024m -XX:MaxPermSize=512m  
if [%DEBUGMODE%]==[1] (
	set GRADLE_OPTS=%GRADLE_OPTS% -Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=%JAVA_DBG_PORT%  
)
if [%DCEVM%]==[1] (
	set GRADLE_OPTS=%GRADLE_OPTS% -javaagent:.\groovy-hotswap\groovyReset.jar -XXaltjvm=dcevm 
)

set ALL_ARGS=%JELL_CACHE% %STAPL_TRACE% %DEBUG_YUI% %OTHER_OPTS%

if [%DRY_RUN%] == [1] (
	echo command to execute ^:
	echo.
	echo 	command ^: gradlew.bat server -Dprefix=/jenkins -Djenkins.httpPort=%JENKINS_PORT% --no-daemon %ALL_ARGS%
	echo.
	echo 	GRADLE_OPTS = %GRADLE_OPTS%
	echo.
	echo 	JAVA_HOME = %JAVA_HOME%
	exit /B 0
)

@REM -----------------

@call gradlew.bat server -Dprefix=/jenkins -Djenkins.httpPort=%JENKINS_PORT% --no-daemon %ALL_ARGS% 
