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

# 解压zookeeper
tar xf ${metaspace_home}/zookeeper-*.tar.gz -C ${depdir}
mv ${metaspace_home}/zookeeper-*.tar.gz ${metaspace_home}/../
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
tar xf ${metaspace_home}/solr-*.tgz -C ${depdir}
mv ${metaspace_home}/solr-*.tgz ${metaspace_home}/../
mv ${depdir}/solr-* ${solr_home}

# 启动solr
${solr_home}/bin/solr start -c -z localhost:${zookeeper_port} -p ${solr_port} >/dev/null 2>&1

# 创建metaspace所需collection
${solr_home}/bin/solr create -p ${solr_port} -c vertex_index -d ${solr_conf} -shards 1 -replicationFactor 1
${solr_home}/bin/solr create -p ${solr_port} -c edge_index -d ${solr_conf} -shards 1 -replicationFactor 1
${solr_home}/bin/solr create -p ${solr_port} -c fulltext_index -d ${solr_conf} -shards 1 -replicationFactor 1
echo "zookeeper and solr started"
