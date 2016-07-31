@echo off
setlocal

set JAVA_OPTS=-Xms128M -Xmx256M -Xss1M
set CLASSPATH=.\*;.\lib\*

"%JAVA_HOME%"\bin\java %JAVA_OPTS% -cp "%CLASSPATH%" org.mos91.tcpping.TCPPing %*
