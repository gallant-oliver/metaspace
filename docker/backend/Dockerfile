FROM docker.gridsumdissector.com/library/metaspace-backend-base-hadoop3:latest-2-sc516684
LABEL describe="数据管理后端镜像"
#代码仓库项目名称
ARG project=metaspace
#代码分支, 构建时指定--build-arg branch=release-1.5.3
ARG branch=release-1.15.0
WORKDIR /usr/local/${project}
#编译打包
RUN git fetch && git checkout ${branch} && git pull && \
  mvn clean package -Pdist -DskipTests && \
  tar -xf distro/target/metaspace-*-bin.tar.gz -C ./distro && \
  set -eux; cd ./distro/metaspace-*/server/webapp/ && \
  mkdir metaspace && cd metaspace && \
  jar xf ../metaspace.war && \
  rm -rf ../metaspace.war

FROM openjdk:8u302-jre-slim-buster
WORKDIR /apps/metaspace
#拷贝部署所需文件
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/conf ./conf
COPY --from=0 /usr/local/metaspace/metaspace-adapter/target/pluginZip ./adapter
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/models ./models
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/server ./server
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/bin ./bin
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/hook ./hook
COPY --from=0 /usr/local/metaspace/distro/metaspace-*/hook-bin ./hook-bin
RUN  mkdir /apps/metaspace/logs
#时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 21001
#堆内存
ENV heap_size=2g
CMD ["bash", "bin/up.sh"]
