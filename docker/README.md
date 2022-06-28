## 说明
```
base文件夹中的Dockerfile是基础镜像的, 只需要编译一次,push到仓库就行了
docker目录的Dockerfile是后端的最终镜像
```
## 编译
> 基础镜像编译
>
> docker build -t docker.gridsumdissector.com/zeta/metaspace-soc/backend-base:1.0 --build-arg branch=release-1.14.1-soc .
>
>后端镜像编译,需改命令中的镜像标签和分支参数
>
> docker build -t docker.gridsumdissector.com/zeta/metaspace-soc/backend:1.14.0 --build-arg branch=release-1.14.1-soc .

## 上传镜像
> docker login docker.gridsumdissector.com
>
> docker push docker.gridsumdissector.com/zeta/metaspace/backend:1.14.0

## 运行
> -v挂载配置文件, --add-host大数据集群中的机器添加到hosts文件中, -e heap_size指定堆内存大小
>
>docker run -it -p 21001:21001 --rm -v /Users/zhuxt/Documents/metaspace_file/dev-conf:/apps/metaspace/conf  \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/hive-site.xml:/etc/hive/conf/hive-site.xml  \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/hbase-site.xml:/etc/hbase/conf/hbase-site.xml  \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/hdfs-site.xml:/etc/hadoop/conf/hdfs-site.xml  \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/core-site.xml:/etc/hadoop/conf/core-site.xml  \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/metaspace_gs-server-9142.keytab:/etc/security/keytabs/metaspace_gs-server-9142.keytab \
 -v /Users/zhuxt/Documents/metaspace_file/dev-conf/krb5.conf:/etc/krb5.conf \
 --add-host gs-server-9141:10.201.60.121 --add-host gs-server-9142:10.201.60.122 \
 --add-host gs-server-9143:10.201.60.123 --add-host gs-server-9144:10.201.60.124 \
 --add-host panel.zetadev.com:10.201.60.121 \
> -e heap_size=2g 
> docker.gridsumdissector.com/zeta/metaspace/backend:1.12.0
