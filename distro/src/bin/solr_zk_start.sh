#!/bin/bash
set -o errexit
# 参数
bin=`dirname $0`
metaspace_home=`cd $bin;pwd`/..
zookeeper_port=2188
solr_port=8988

# 依赖项安装目录
depdir=$metaspace_home
solr_conf=${metaspace_home}/conf/solr

# zookeeper安装目录
zookeeper_home=${depdir}/zookeeper
zookeeper_conf_dir=${zookeeper_home}/conf

echo 启动zookeeper
sh ${zookeeper_home}/bin/zkServer.sh start
res=`sh ${zookeeper_home}/bin/zkServer.sh status`
if [ "$res" = "Mode: standalone" ];then
	echo "zookeeper start success"
else
	echo "zookeeper start failure"
fi

# solr安装目录
solr_home=${depdir}/solr

echo 启动solr
${solr_home}/bin/solr start -c -z localhost:${zookeeper_port} -p ${solr_port}