#!/bin/bash

dt=`date +%Y%m%d_%H`
METASPACE_HOME=$1
bakdir=$2/$dt
mkdir -p ${bakdir}
echo 备份文件，备份路径：$bakdir
mv ${METASPACE_HOME}/server/webapp/metaspace ${bakdir}

echo 替换文件
cp -r metaspace ${METASPACE_HOME}/server/webapp
rm -rf ${METASPACE_HOME}/server/webapp/metaspace/config.js
cp ${bakdir}/metaspace/config.js ${METASPACE_HOME}/server/webapp/metaspace/config.js
chown -R metaspace:metaspace ${METASPACE_HOME}/server/webapp
#1.9.0-1.9.1
cp -r adapter/ ${METASPACE_HOME}/
#1.9.1-1.10.0
cp conf/measureEnv.json  ${METASPACE_HOME}/conf/
cp conf/sparkConfig.json  ${METASPACE_HOME}/conf/
