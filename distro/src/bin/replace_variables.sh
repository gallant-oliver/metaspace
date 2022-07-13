#!/bin/bash
# author zhuxt
# 用环境变量替换配置文件中的变量占位符
if [ $# != 2 ];then
   echo "USAGE: $0 <变量前缀> <配置文件所在目录> "
   exit 1
fi
replace() {
    v=$1    
    variable="\${${v%%=*}}"
    value=${v#*=}
    # 将value中的'/' 前面都加一个 '\'
    value=${value//\//\\\/}
    
    for file in `grep $variable -rl $conf_dir`;do
        echo "替换占位符: $file: $variable -> $value"
        sed -i "s/$variable/$value/g" $file
    done
}
# 环境变量前缀,一般以产品名称_开头 
prefix=$1
# 配置文件路径
conf_dir=$2
#公共变量
array=("SSO_URL" "POSTGRESQL_HOST" "POSTGRESQL_PORT" "POSTGRESQL_USERNAME" "POSTGRESQL_PASSWORD" "REDIS_HOST" "REDIS_PORT" "REDIS_PASSWORD")

for element in ${array[@]};do
    kv=`env |grep $element`
    if [ "$kv" != "" ]; then
        replace $kv
    fi
done
for v in `env |grep ZETA_`;do
    replace $v
done
echo "公共变量全部替换完成" 

# 获取环境变量
for v in `env|grep $1`;do
    replace $v
done
echo "自定义变量全部替换完成"
