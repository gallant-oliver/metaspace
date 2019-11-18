#!/bin/bash

# 参数
metaspace_config_template=metaspace-necessary-application.template
metaspace_config=atlas-application.properties
while read line
do
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
	#hive URL
	elif [ "$key" = "hive_urls" ];then
		sed -i "s#metaspace.hive.url=.*#metaspace.hive.url=${value}#g" ${metaspace_config}
	#hive principal
	elif [ "$key" = "hive_principal" ];then
		sed -i "s#metaspace.hive.principal=.*#metaspace.hive.principal=${value}#g" ${metaspace_config}
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
		sed -i "s#metaspace.database.url=.*#metaspace.database.url=jdbc:postgresql://${value}/metaspace?useUnicode=true\&characterEncoding=UTF8#g" ${metaspace_config}
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
	#keyTab文件路径
	elif [ "$key" = "keytab_dir" ];then
		sed -i "s#metaspace.kerberos.keytab=.*#metaspace.kerberos.keytab=${value}#g" ${metaspace_config}
		sed -i "s#atlas.jaas.KafkaClient.option.keyTab=.*#atlas.jaas.KafkaClient.option.keyTab=${value}#g" ${metaspace_config}
		sed -i "s#atlas.authentication.keytab=.*#atlas.authentication.keytab=${value}#g" ${metaspace_config}
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
	#metaspace HA
	elif [ "$key" = "metaspace_ha" ];then
		if [ ${#value}>0 ];then
			sed -i "s#atlas.server.ha.enabled=.*#atlas.server.ha.enabled=${value}#g" ${metaspace_config}
		fi
	#metaspace urls
	elif [ "$key" = "metaspace_urls" ];then

		IFS=',' arr=($value)
		len=${#arr[*]}
		sed -i "s#atlas.server.address.id1=.*#atlas.server.address.id1=${arr[0]}#g" ${metaspace_config}
		if [ "$len" = 2 ];then
			sed -i "s#atlas.rest.address=.*#atlas.rest.address=http://${arr[0]},http://${arr[1]}#g" ${metaspace_config}
			sed -i "s#atlas.server.address.id2=.*#atlas.server.address.id2=${arr[1]}#g" ${metaspace_config}
		else
			sed -i "s#atlas.rest.address=.*#atlas.rest.address=http://${arr[0]}#g" ${metaspace_config}
		fi
	elif [ "$key" = "current_host" ];then
		sed -i "s#atlas.server.bind.address=.*#atlas.server.bind.address=${value}#g" ${metaspace_config}
	#安全中心接口url
	elif [ "$key" = "secureplus_url" ];then
		if [ ${#value} = 0 ];then
			sed -i "s#metaspace.secureplus.enable=.*#metaspace.secureplus.enable=false#g" ${metaspace_config}
		else
			sed -i "s#metaspace.secureplus.enable=.*#metaspace.secureplus.enable=true#g" ${metaspace_config}
			sed -i "s#metaspace.secureplus.privilegeREST=.*#metaspace.secureplus.privilegeREST=http://${value}/service/privilege/hivetable#g" ${metaspace_config}
		fi
	elif [[ "$key" == "#note:"* ]];then
                continue
	else
		echo "unknown configuration:"$key
	fi
done<$metaspace_config_template