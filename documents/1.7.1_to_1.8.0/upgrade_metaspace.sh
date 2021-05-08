#!/bin/bash

dt=`date +%Y%m%d_%H`
METASPACE_HOME=$1
bakdir=$2/$dt
mkdir -p ${bakdir}
echo 备份文件，备份路径：$bakdir
mv ${METASPACE_HOME}/server/webapp/metaspace ${bakdir}
mv ${METASPACE_HOME}/conf/log4j2.xml ${bakdir}

echo 替换文件
cp -r metaspace ${METASPACE_HOME}/server/webapp
cp -r log4j2.xml ${METASPACE_HOME}/conf
cp -r business_template.xlsx ${METASPACE_HOME}/conf
cp -r category_template.xlsx ${METASPACE_HOME}/conf
rm -rf ${METASPACE_HOME}/server/webapp/metaspace/config.js
cp ${bakdir}/metaspace/config.js ${METASPACE_HOME}/server/webapp/metaspace/config.js
