@setlocal

@REM  the --no-daemon is due to http://stackoverflow.com/questions/22760752/how-to-enable-debug-on-my-junit-through-gradle-test-task

@call gradlew.bat --no-daemon test -Dtest.debug
