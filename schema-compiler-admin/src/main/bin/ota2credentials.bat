@echo off
FOR /R ./lib %%a in (*.jar) DO CALL :AddToPath %%a
java -cp %CLASSPATH% org.opentravel.schemacompiler.admin.CredentialsManager %*
GOTO :EOF

:AddToPath
SET CLASSPATH=%1;%CLASSPATH%
GOTO :EOF