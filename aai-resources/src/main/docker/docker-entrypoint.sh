###
# ============LICENSE_START=======================================================
# org.onap.aai
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###

APP_HOME=$(pwd);
RESOURCES_HOME=${APP_HOME}/resources/;

export CHEF_CONFIG_REPO=${CHEF_CONFIG_REPO:-aai-config};
export CHEF_GIT_URL=${CHEF_GIT_URL:-http://gerrit.onap.org/r/aai};
export CHEF_CONFIG_GIT_URL=${CHEF_CONFIG_GIT_URL:-$CHEF_GIT_URL};
export CHEF_DATA_GIT_URL=${CHEF_DATA_GIT_URL:-$CHEF_GIT_URL};

export SERVER_PORT=${SERVER_PORT:-8447};

USER_ID=${LOCAL_USER_ID:-9001}
GROUP_ID=${LOCAL_GROUP_ID:-9001}

if [ $(cat /etc/passwd | grep aaiadmin | wc -l) -eq 0 ]; then

	groupadd aaiadmin -g ${GROUP_ID} || {
		echo "Unable to create the group id for ${GROUP_ID}";
		exit 1;
	}
	useradd --shell=/bin/bash -u ${USER_ID} -g ${GROUP_ID} -o -c "" -m aaiadmin || {
		echo "Unable to create the user id for ${USER_ID}";
		exit 1;
	}
fi;

chown -R aaiadmin:aaiadmin /opt/app /opt/aai/logroot /var/chef
find /opt/app/ -name "*.sh" -exec chmod +x {} +

if [ -f ${APP_HOME}/aai.sh ]; then

    gosu aaiadmin ln -s bin scripts
    gosu aaiadmin ln -s /opt/aai/logroot/AAI-RES logs

    mv ${APP_HOME}/aai.sh /etc/profile.d/aai.sh
    chmod 755 /etc/profile.d/aai.sh

    scriptName=$1;

    if [ ! -z $scriptName ]; then

        if [ -f ${APP_HOME}/bin/${scriptName} ]; then
            shift 1;
            gosu aaiadmin ${APP_HOME}/bin/${scriptName} "$@" || {
                echo "Failed to run the ${scriptName}";
                exit 1;
            }
        else
            echo "Unable to find the script ${scriptName} in ${APP_HOME}/bin";
            exit 1;
        fi;

        exit 0;
    fi;


    # If in the environment file skip create db schema there is a value set
    # Then it will skip the create db schema at startup
    # This is a workaround that will be there temporarily
    # Ideally createDBSchema should be run from chef
    # Or run without having to startup the application
    if [ -z ${SKIP_CREATE_DB_SCHEMA_AT_STARTUP} ]; then
      gosu aaiadmin /opt/app/aai-resources/scripts/createDBSchema.sh || exit 1
    fi;
fi;

JAVA_CMD="exec gosu aaiadmin java";

JVM_OPTS="${PRE_JVM_OPTS} -XX:+UnlockDiagnosticVMOptions";
JVM_OPTS="${JVM_OPTS} -XX:+UnsyncloadClass";
JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSParallelRemarkEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+UseCMSInitiatingOccupancyOnly";
JVM_OPTS="${JVM_OPTS} -XX:CMSInitiatingOccupancyFraction=70";
JVM_OPTS="${JVM_OPTS} -XX:+ScavengeBeforeFullGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSScavengeBeforeRemark";
JVM_OPTS="${JVM_OPTS} -XX:-HeapDumpOnOutOfMemoryError";
JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC";
JVM_OPTS="${JVM_OPTS} -verbose:gc";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCDetails";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCTimeStamps";
JVM_OPTS="${JVM_OPTS} -XX:MaxPermSize=512M";
JVM_OPTS="${JVM_OPTS} -XX:PermSize=512M";
JVM_OPTS="${JVM_OPTS} -server";
JVM_OPTS="${JVM_OPTS} -XX:NewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:MaxNewSize=512m";
JVM_OPTS="${JVM_OPTS} -XX:SurvivorRatio=8";
JVM_OPTS="${JVM_OPTS} -XX:+DisableExplicitGC";
JVM_OPTS="${JVM_OPTS} -verbose:gc";
JVM_OPTS="${JVM_OPTS} -XX:+UseParNewGC";
JVM_OPTS="${JVM_OPTS} -XX:+CMSParallelRemarkEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+CMSClassUnloadingEnabled";
JVM_OPTS="${JVM_OPTS} -XX:+UseConcMarkSweepGC";
JVM_OPTS="${JVM_OPTS} -XX:-UseBiasedLocking";
JVM_OPTS="${JVM_OPTS} -XX:ParallelGCThreads=4";
JVM_OPTS="${JVM_OPTS} -XX:LargePageSizeInBytes=128m";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCDetails";
JVM_OPTS="${JVM_OPTS} -XX:+PrintGCTimeStamps";
JVM_OPTS="${JVM_OPTS} -Xloggc:/opt/app/aai-resources/logs/ajsc-jetty/gc/aai_gc.log";
JVM_OPTS="${JVM_OPTS} -Dsun.net.inetaddr.ttl=180";
JVM_OPTS="${JVM_OPTS} -XX:+HeapDumpOnOutOfMemoryError";
JVM_OPTS="${JVM_OPTS} -XX:HeapDumpPath=/opt/app/aai-resources/logs/ajsc-jetty/heap-dump";
JVM_OPTS="${JVM_OPTS} ${POST_JVM_OPTS}";

JAVA_OPTS="${PRE_JAVA_OPTS} -DAJSC_HOME=$APP_HOME";
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}";
JAVA_OPTS="${JAVA_OPTS} -DBUNDLECONFIG_DIR=./resources";
JAVA_OPTS="${JAVA_OPTS} -Dserver.local.startpath=${RESOURCES_HOME}";
JAVA_OPTS="${JAVA_OPTS} -DAAI_CHEF_ENV=${AAI_CHEF_ENV}";
JAVA_OPTS="${JAVA_OPTS} -DSCLD_ENV=${SCLD_ENV}";
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom";
JAVA_OPTS="${JAVA_OPTS} -Dloader.path=$APP_HOME/resources";
JAVA_OPTS="${JAVA_OPTS} ${POST_JAVA_OPTS}";

JAVA_MAIN_JAR=$(ls lib/aai-resources*.jar);

${JAVA_CMD} ${JVM_OPTS} ${JAVA_OPTS} -jar ${JAVA_MAIN_JAR};
