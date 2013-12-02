#!/bin/bash

SCRIPTDIR="$( cd "$( dirname "$0" )" && pwd )"
CLASSPATH=$(JARS=($SCRIPTDIR/lib/*.jar); IFS=:; echo "${JARS[*]}")
java -cp $CLASSPATH com.sabre.schemacompiler.admin.CredentialsManager "$@"