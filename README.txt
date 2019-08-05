
Build Process
=============

1. Get MetaSpace sources to your local directory, for example with following commands
   $ cd <your-local-directory>
   $ git clone --recursive https://gitlab.gridsum.com/zeta/dev/metaspace/metaspace
   $ cd metaspace && git submodule update --init

2. Execute the following commands to build Apache Atlas

   $ export MAVEN_OPTS="-Xms2g -Xmx2g"
   跳过测试并生产部署包
   $ mvn clean -DskipTests package -Pdist
   加速打包,每个core启动一个编译线程
   $mvn clean package -Dmaven.compile.fork=true -Dmaven.test.skip=true -T 1C



3. After above build commands successfully complete, you should see the following files

   distro/target/apache-atlas-{project.version}-bin.tar.gz
   distro/target/apache-atlas-{project.version}-hbase-hook.tar.gz
   distro/target/apache-atlas-{project.version}-hive-hook.tar.gz
   distro/target/apache-atlas-{project.version}-kafka-hook.tar.gz
   distro/target/apache-atlas-{project.version}-sources.tar.gz

4. For more details on building and running MetaSpace, please refer to https://gitlab.gridsum.com/zeta/metaspace/wikis/home
