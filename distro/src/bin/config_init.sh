#!/bin/bash
set -o errexit
bin=`dirname $0`
home=`cd $bin/..;pwd`
conf_dir=`cd $home/conf;pwd`
cd $conf_dir
# 参数
metaspace_config_template=metaspace-necessary-application.template
metaspace_config=atlas-application.properties
quartz_config=quartz.properties
jaas=atlas_jaas.conf
atlas_env=atlas-env.sh
sed -i "s#export ATLAS_HOME=.*#export ATLAS_HOME=${home}#g" $atlas_env
while read line
do
  if((${#line}<=0));then
      continue
  fi
  if [[ "$line" =~ ^#.* ]];then
                continue
  fi
	IFS='=' arr=($line)
	key=${arr[0]}
	value=${arr[1]}

	#hive配置文件
	if [ "$key" = "hive_conf_dir" ];then
		sed -i "s#metaspace.hive.conf=.*#metaspace.hive.conf=${value}#g" ${metaspace_config}
	#hdfs配置文件
	elif [ "$key" = "hdfs_conf_dir" ];then
		sed -i "s#metaspace.hdfs.conf=.*#metaspace.hdfs.conf=${value}#g" ${metaspace_config}
	#hbase配置文件
	elif [ "$key" = "hbase_conf_dir" ];then
		sed -i "s#metaspace.hbase.conf=.*#metaspace.hbase.conf=${value}#g" ${metaspace_config}
		sed -i "s#export HBASE_CONF_DIR=.*#export HBASE_CONF_DIR=${value}#g" $atlas_env
	#hive URL
	elif [ "$key" = "hive_urls" ];then
		sed -i "s#metaspace.hive.url=.*#metaspace.hive.url=${value}#g" ${metaspace_config}
	#hive principal
	elif [ "$key" = "hive_principal" ];then
		sed -i "s#metaspace.hive.principal=.*#metaspace.hive.principal=${value}#g" ${metaspace_config}
	#impala principal
	elif [ "$key" = "impala_principal" ];then
		sed -i "s#metaspace.impala.principal=.*#metaspace.impala.principal=${value}#g" ${metaspace_config}
	#impala url
	elif [ "$key" = "impala_url" ];then
		IFS=':' arr=($value)
		len=${#arr[*]}
		if [ "$len" = 2 ];then
			sed -i "s#metaspace.impala.url=.*#metaspace.impala.url=jdbc:impala://${value}#g" ${metaspace_config}
			sed -i "s#metaspace.impala.kerberos.jdbc=.*#metaspace.impala.kerberos.jdbc=AuthMech=1;KrbRealm=PANEL.COM;KrbHostFQDN=${arr[0]};KrbServiceName=impala#g" ${metaspace_config}
		elif [ "$len" = 1 ];then
			sed -i "s#metaspace.impala.url=.*#metaspace.impala.url=jdbc:impala://${value}:21050#g" ${metaspace_config}
			sed -i "s#metaspace.impala.kerberos.jdbc=.*#metaspace.impala.kerberos.jdbc=AuthMech=1;KrbRealm=PANEL.COM;KrbHostFQDN=${value};KrbServiceName=impala#g" ${metaspace_config}
		fi
	#sso配置
	elif [ "$key" = "sso_url" ];then
		sed -i "s#sso.login.url=.*#sso.login.url=http://${value}/login#g" ${metaspace_config}
		sed -i "s#sso.info.url=.*#sso.info.url=http://${value}/api/v2/info#g" ${metaspace_config}
		sed -i "s#sso.organization.url=.*#sso.organization.url=http://${value}/portal/api/v5/organization#g" ${metaspace_config}
		sed -i "s#sso.organization.count.url=.*#sso.organization.count.url=http://${value}/portal/api/v5/organizationsCount#g" ${metaspace_config}
		sed -i "s#sso.user.info.url=.*#sso.user.info.url=http://${value}/portal/api/v5/getAccountByID#g" ${metaspace_config}
	#数据库url
	elif [ "$key" = "database_url" ];then
		sed -i "s#metaspace.database.url=.*#metaspace.database.url=jdbc:postgresql://${value}/msdb?useUnicode=true\&characterEncoding=UTF8#g" ${metaspace_config}
		sed -i "s#^org.quartz.dataSource.msDS.URL.*#org.quartz.dataSource.msDS.URL=jdbc:postgresql://${value}/msdb?useUnicode=true\&characterEncoding=UTF8#g" ${quartz_config}
	#zookeeper urls
	elif [ "$key" = "basic_zookeeper_urls" ];then
		sed -i "s#atlas.graph.storage.hostname=.*#atlas.graph.storage.hostname=${value}#g" ${metaspace_config}
		sed -i "s#atlas.kafka.zookeeper.connect=.*#atlas.kafka.zookeeper.connect=${value}#g" ${metaspace_config}
		sed -i "s#atlas.audit.hbase.zookeeper.quorum=.*#atlas.audit.hbase.zookeeper.quorum=${value}#g" ${metaspace_config}
		sed -i "s#atlas.server.ha.zookeeper.connect=.*#atlas.server.ha.zookeeper.connect=${value}#g" ${metaspace_config}
	#solr使用zookeeper url
	elif [ "$key" = "solr_zookeeper_urls" ];then
		sed -i "s#atlas.graph.index.search.solr.zookeeper-url=.*#atlas.graph.index.search.solr.zookeeper-url=${value}#g" ${metaspace_config}
	#kafka bootstrap
	elif [ "$key" = "kafka_bootstrap_servers" ];then
		sed -i "s#atlas.kafka.bootstrap.servers=.*#atlas.kafka.bootstrap.servers=${value}#g" ${metaspace_config}
	#metaspace principal
	elif [ "$key" = "principal" ];then
		sed -i "s#atlas.authentication.principal=.*#atlas.authentication.principal=${value}#g" ${metaspace_config}
		sed -i "s#atlas.jaas.KafkaClient.option.principal=.*#atlas.jaas.KafkaClient.option.principal=${value}#g" ${metaspace_config}
		sed -i "s#atlas.authentication.principal=.*#atlas.authentication.principal=${value}#g" ${metaspace_config}
		sed -i "s#   principal=.*#   principal=\"${value}\";#g" $jaas
	#keyTab文件路径
	elif [ "$key" = "keytab_dir" ];then
		sed -i "s#metaspace.kerberos.keytab=.*#metaspace.kerberos.keytab=${value}#g" ${metaspace_config}
		sed -i "s#atlas.jaas.KafkaClient.option.keyTab=.*#atlas.jaas.KafkaClient.option.keyTab=${value}#g" ${metaspace_config}
		sed -i "s#atlas.authentication.keytab=.*#atlas.authentication.keytab=${value}#g" ${metaspace_config}
		sed -i "s#   keyTab=.*#   keyTab=\"${value}\"#g" $jaas
	#redis
	elif [ "$key" = "redis_url" ];then
		IFS=':' arr=($value)
		len=${#arr[*]}
		sed -i "s#metaspace.cache.redis.host=.*#metaspace.cache.redis.host=${arr[0]}#g" ${metaspace_config}
		if [ "$len" = 2 ];then
			sed -i "s#metaspace.cache.redis.port=.*#metaspace.cache.redis.port=${arr[1]}#g" ${metaspace_config}
		fi
	#云平台接口url
	elif [ "$key" = "mobius_url" ];then
		sed -i "s#metaspace.mobius.url=.*#metaspace.mobius.url=http://${value}/v3/bigdata/gateway#g" ${metaspace_config}
	#metaspace url
	elif [ "$key" = "metaspace_url" ];then
		sed -i "s#atlas.rest.address=.*#atlas.rest.address=http://${value}#g" ${metaspace_config}
		sed -i "s#metaspace.request.address=.*#metaspace.request.address=http://${value}#g" ${metaspace_config}
	elif [ "$key" = "current_host" ];then
		sed -i "s#atlas.server.bind.address=.*#atlas.server.bind.address=${value}#g" ${metaspace_config}
	#安全中心接口url
	elif [ "$key" = "secureplus_url" ];then
		if [ ${#value} = 0 ];then
			sed -i "s#metaspace.secureplus.enable=.*#metaspace.secureplus.enable=false#g" ${metaspace_config}
		else
			sed -i "s#metaspace.secureplus.enable=.*#metaspace.secureplus.enable=true#g" ${metaspace_config}
			sed -i "s#security.center.host=.*#security.center.host=http://${value}#g" ${metaspace_config}
		fi
	#数据库配置
	elif [ "$key" = "database" ]; then
    sed -i "/^metaspace.database.url/s/msdb/${value}/g" ${metaspace_config}
	  sed -i "/^org.quartz.dataSource.msDS.URL/s/msdb/${value}/g" $quartz_config
	elif [ "$key" = "username" ]; then
	  sed -i "s#metaspace.database.username=.*#metaspace.database.username=${value}#g" ${metaspace_config}
	  sed -i "s#^org.quartz.dataSource.msDS.user.*#org.quartz.dataSource.msDS.user=${value}#g" $quartz_config
	elif [ "$key" = "password" ]; then
	  sed -i "s#metaspace.database.password=.*#metaspace.database.password=${value}#g" $metaspace_config
	  sed -i "s#^org.quartz.dataSource.msDS.password.*#org.quartz.dataSource.msDS.password=${value}#g" $quartz_config
	elif [ "$key" = "realm" ]; then
	  sed -i "/^metaspace.impala.kerberos.jdbc/s/PANEL.COM/${value}/g" $metaspace_config
	else
		echo "unknown configuration:${key}"
	fi
done<$metaspace_config_template
rm -f 0
