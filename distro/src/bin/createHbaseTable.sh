#!/bin/sh 

echo "Running the <$0> file" 

## table names 
declare -a table_names=("table_stat;['info']") 

function tableExists { 
    echo "Table <$1> already exists." 
} 

function createTable { 
    echo "Table <$1> does not exist." 
    echo "Creating the Table <$1>" 

    echo "create '$1', $2" | hbase shell > log 2>&1
    echo "grant 'METASPACE', 'RWCA', 'table_stat'" | hbase shell > log 2>&1
    echo "grant 'atlas', 'RWCA', 'table_stat'" | hbase shell > log 2>&1
    cat log | grep -q '0 row(s) in' 

    if [ $? = 0 ]; then 
     echo "Table <$1> created successfully." 
    else 
     echo "Table <$1> is not created." 
    fi

} 


## iterating table names and checking table already exists. if does not exist creating table. 
for i in "${table_names[@]}" 
do 
     ## splitting the table based on ';'. First argument will be 'table_name' and second argument will be 'column_families' 
     set -- "$i" 
     IFS=";"; declare -a table_name_info=($*) 

     echo "" 
     echo "Checking the table <${table_name_info[0]}> exists or not" 
     echo "" 

     echo "exists '${table_name_info[0]}'" | hbase shell > log 2>&1 
     cat log | grep -q 'Table '${table_name_info[0]}' does exist' 

     if [ $? = 0 ]; then 
      tableExists ${table_name_info[0]} 
     else 
      createTable ${table_name_info[0]} ${table_name_info[1]} 
     fi 
done 

## deleting the log file. 
rm -rf log 
echo "" 
echo "done." 

exit