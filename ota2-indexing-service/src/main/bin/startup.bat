@REM
@REM Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off

set "SCRIPTDIR=%~dp0"

set "MANAGER_CONFIG=%SCRIPTDIR%conf\indexing-manager.xml"
set "AGENT_CONFIG=%SCRIPTDIR%conf\indexing-agent.xml"
set "LOG4J_CONFIG=%SCRIPTDIR%conf\log4j2-manager.properties"
set "JMX_CONFIG=-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=11098 -Dcom.sun.management.jmxremote.rmi.port=11098 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dota2.index.agent.jmxport=11099"

@REM set "HTTP_PROXY_HOST=proxy.example.com"
@REM set "HTTP_PROXY_PORT=8080"
@REM set "HTTPS_PROXY_HOST=proxy.example.com"
@REM set "HTTPS_PROXY_PORT=8443"
@REM set "NON_PROXY_HOSTS=localhost^|*.example.com"
@REM set "PROXY_SETTINGS=-Dhttp.proxyHost=%HTTP_PROXY_HOST% -Dhttp.proxyPort=%HTTP_PROXY_PORT% -Dhttps.proxyHost=%HTTPS_PROXY_HOST% -Dhttps.proxyPort=%HTTPS_PROXY_PORT% -Dhttp.nonProxyHosts=%NON_PROXY_HOSTS%"

start /b javaw.exe -Dota2.index.manager.config="%MANAGER_CONFIG%" -Dota2.index.agent.config="%AGENT_CONFIG%" -Dlog4j.configurationFile="%LOG4J_CONFIG%" %JMX_CONFIG% %PROXY_SETTINGS% -cp ./lib/* org.opentravel.schemacompiler.index.IndexProcessManager %*