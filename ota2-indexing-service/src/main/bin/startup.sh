#!/bin/bash
#
# Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
SCRIPTDIR="$( cd "$( dirname "$0" )" && pwd )"

JAVA_CLASSPATH=$(echo $SCRIPTDIR/lib/*.jar | tr ' ' ':')
MANAGER_CONFIG=conf/indexing-manager.xml
AGENT_CONFIG=conf/indexing-agent.xml
LOG4J_CONFIG=$SCRIPTDIR/conf/log4j2-manager.properties
JMX_CONFIG="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=11098 -Dcom.sun.management.jmxremote.rmi.port=11098 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dota2.index.agent.jmxport=11099"

#HTTP_PROXY_HOST=proxy.example.com
#HTTP_PROXY_PORT=8080
#HTTPS_PROXY_HOST=proxy.example.com
#HTTPS_PROXY_PORT=8443
#NON_PROXY_HOSTS=localhost\|*.example.com
#PROXY_SETTINGS=-Dhttp.proxyHost=$HTTP_PROXY_HOST\ -Dhttp.proxyPort=$HTTP_PROXY_PORT\ -Dhttps.proxyHost=$HTTPS_PROXY_HOST\ -Dhttps.proxyPort=$HTTPS_PROXY_PORT\ -Dhttp.nonProxyHosts=$NON_PROXY_HOSTS

if [ $# -ne 0 ]
  then
    $SCRIPTDIR/wait-for-it.sh $1 -t 60
fi

exec java -Dota2.index.manager.config=$MANAGER_CONFIG -Dota2.index.agent.config=$AGENT_CONFIG -Dlog4j.configurationFile=$LOG4J_CONFIG $JMX_CONFIG $PROXY_SETTINGS -cp "$JAVA_CLASSPATH" org.opentravel.schemacompiler.index.IndexProcessManager