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

REPOSITORY_CONFIG=$SCRIPTDIR/conf/ota2-repository-config.xml
LOG4J_CONFIG=$SCRIPTDIR/conf/log4j-manager.properties

JAVA_OPTS=-Dota2.repository.config=$REPOSITORY_CONFIG -Dlog4j.configuration=file:/$LOG4J_CONFIG

javaw $JAVA_OPTS -cp $SCRIPTDIR/lib/* org.opentravel.schemacompiler.index.IndexProcessManager "$@" &