#!/bin/sh
bakpath="/usr/hdp/metaspace_update_backups"
bakfile=$bakpath"/metaspace1.3bak.sql"
db=metaspace
function menu {
    clear
    echo
    echo -e "\t\t\t SysAdmin Menu \n"
    echo -e "\t 1.备份数据"
    echo -e "\t 2.执行升级"
    echo -e "\t 3.补全贴源层"
    echo -e "\t 4.回滚数据"
    echo -e "\t 0.退出"
    echo -en "\t\tEnter your choice:" 
    read -n 1 option
}
function bak {
echo "检查备份位置是否可用"
if [ ! -f "$bakfile" ]; then
echo "确认可用"
echo "创建备份目录"
mkdir $bakpath
basepath=$(cd `dirname $0`; pwd)
cp $basepath/metaspace1.4update.sql $bakpath
chown postgres $bakpath -R
echo "备份数据库metasapce"
su postgres -c "pg_dump -d $db -f $bakfile"
if [ $? -ne 0 ]; then
    echo "备份失败"
else
    echo "备份完成，文件在"$bakfile
fi
else
echo $bakfile"被占用"
fi
}
function update {
echo "升级数据库"
su postgres -c "psql -d $db -f $bakpath/metaspace1.4update.sql"
if [ $? -ne 0 ]; then
    echo "升级失败"
else
    echo "升级完成"
    echo "请检查sql执行过程中有无报错信息，如果有请执行回滚，并联系开发人员"
fi
}
function get {
echo "发送请求，如果metaspace不是装在本机请手动修改脚本，替换HOSTNAME"
curl -G $HOSTNAME:21001/api/metaspace/metadata/supplementTable
}
function rollback {
echo "回滚数据"
echo "确认数据是否存在"
if [ ! -f "$bakfile" ]; then 
echo "备份文件不存在，快找找去哪了"
else
echo "文件存在，开始回滚"
su postgres -c "psql -d postgres -c 'drop database $db'"
if [ $? -eq 0 ]; then
su postgres -c "psql -d postgres -c 'create database $db'"
su postgres -c "psql -d postgres -c 'alter database $db OWNER TO metaspace'"
if [ $? -eq 0 ]; then
su postgres -c "psql -d $db -f $bakfile"
if [ $? -ne 0 ]; then
    echo "回滚失败"
else
    echo "回滚完成"
fi
fi
fi
fi
}
while  [ 1 ]
do
    menu
    case $option in
    0)
        break ;;
    1)
        bak ;;
    2)
        update ;;
    3)
        get ;;
    4)
        rollback ;;
    *)
        clear
        echo "sorry wrong selection" ;;
    esac
    echo -en "\n\n\t\tHit any key to continue"
    read -n 1 option 
done
clear
