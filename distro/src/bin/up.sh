#!/bin/bash

BIN=`dirname $0`
HOME=`dirname $BIN`
CONF=$HOME/conf

chmod +x $BIN/*.sh
$BIN/replace_variables.sh METASPACE_ $CONF
java -Datlas.log.dir=/apps/metaspace/logs -Datlas.log.file=application.log \
  -Datlas.home=/apps/metaspace -Datlas.conf=/apps/metaspace/conf \
  -Dembedded.solr.directory=/apps/metaspace/conf -Xms${heap_size} -Xmx${heap_size} \
  -XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m -server -XX:SoftRefLRUPolicyMSPerMB=0 \
  -XX:+CMSClassUnloadingEnabled -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled \
  -XX:+PrintTenuringDistribution -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/apps/metaspace/logs/atlas_server.hprof \
  -Xloggc:/apps/metaspace/logs/gc-worker.log -verbose:gc -XX:+UseGCLogFileRotation \
  -XX:NumberOfGCLogFiles=10 -XX:GCLogFileSize=1m -XX:+PrintGCDetails -XX:+PrintHeapAtGC \
  -XX:+PrintGCTimeStamps -Dzookeeper.sasl.client=false -Djavamelody.disabled=true \
  -Djavax.security.auth.useSubjectCredsOnly=false \
  -Djava.security.auth.login.config=/apps/metaspace/conf/atlas_jaas.conf \
  -classpath /apps/metaspace/conf:/apps/metaspace/server/webapp/metaspace/WEB-INF/classes:/apps/metaspace/server/webapp/metaspace/WEB-INF/lib/*:/apps/metaspace/libext/*:/etc/hbase/conf \
  org.apache.atlas.Atlas \
  -app /apps/metaspace/server/webapp/metaspace
