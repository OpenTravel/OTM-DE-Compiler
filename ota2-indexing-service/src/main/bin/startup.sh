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

MANAGER_CONFIG=$SCRIPTDIR/conf/indexing-manager.xml
AGENT_CONFIG=$SCRIPTDIR/conf/indexing-agent.xml
LOG4J_CONFIG=$SCRIPTDIR/conf/log4j-manager.properties

javaw -Dota2.index.manager.config=$MANAGER_CONFIG -Dota2.index.agent.config=$AGENT_CONFIG -Dlog4j.configuration=file:/$LOG4J_CONFIG -cp $SCRIPTDIR/lib/* org.opentravel.schemacompiler.index.IndexProcessManager "$@" &