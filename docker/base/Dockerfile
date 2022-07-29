FROM docker.gridsumdissector.com/zeta/base/zeta-backend-base:latest
FROM docker.gridsumdissector.com/zeta/backend:1.0

LABEL describe="数据管理后端基础镜像"
ARG product=metaspace
ARG project=metaspace
ARG branch=release-1.16.0
ARG name=docker
ARG token=uGszPvyTVsiz2CNkfwnM
RUN set -x;git clone https://${name}:${token}@gitlab.gridsum.com/zeta/dev/${product}/${project}.git -b ${branch} && \
    cd ${project} && \
    mvn -B clean package -Pdist -DskipTests &&\
    mvn clean


