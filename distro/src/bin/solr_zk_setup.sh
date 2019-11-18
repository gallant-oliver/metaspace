#!/bin/bash

# 参数
metaspace_home=$1
zookeeper_port=$2
solr_port=$3

# 依赖项安装目录
depdir=/home/metaspace/metaspace_depend
solr_conf=${metaspace_home}/conf/solr
mkdir -p ${depdir}

# zookeeper安装目录
zookeeper_home=${depdir}/zookeeper
zookeeper_conf_dir=${zookeeper_home}/conf

# 解压zookeeper
tar xvf zookeeper-*.tar.gz -C ${depdir} >/dev/null 2>&1
mv ${depdir}/zookeeper-* ${depdir}/zookeeper

# 修改配置文件
cp ${zookeeper_conf_dir}/zoo_sample.cfg ${zookeeper_conf_dir}/zoo.cfg
sed -i "s#dataDir=.*#dataDir=${zookeeper_home}/data#g" ${zookeeper_conf_dir}/zoo.cfg
sed -i "s#clientPort=.*#clientPort=${zookeeper_port}#g" ${zookeeper_conf_dir}/zoo.cfg

# 启动zookeeper
sh ${zookeeper_home}/bin/zkServer.sh start
res=`sh ${zookeeper_home}/bin/zkServer.sh status`
if [ "$res" = "Mode: standalone" ];then
	echo "zookeeper start success"
else
	echo "zookeeper start failure"
fi

# solr安装目录
solr_home=${depdir}/solr

# 解压solr
tar xvf solr-*.tgz -C ${depdir} >/dev/null 2>&1
mv ${depdir}/solr-* ${solr_home}

# 启动solr
${solr_home}/bin/solr start -c -z localhost:${zookeeper_port} -p ${solr_port} >/dev/null 2>&1

# 创建metaspace所需collection
${solr_home}/bin/solr create -p ${solr_port} -c vertex_index -d ${solr_port} -shards 1 -replicationFactor 1
${solr_home}/bin/solr create -p ${solr_port} -c edge_index -d ${solr_port} -shards 1 -replicationFactor 1
${solr_home}/bin/solr create -p ${solr_port} -c fulltext_index -d ${solr_conf} -shards 1 -replicationFactor 1