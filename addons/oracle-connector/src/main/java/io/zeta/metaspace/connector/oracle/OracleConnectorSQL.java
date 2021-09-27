package io.zeta.metaspace.connector.oracle;

public interface OracleConnectorSQL{
    String LOGMINER_LOG_FILES_LOGMNR$ = "select FILENAME from v$logmnr_logs";
    String SELECT_UTL_DICTIONARY = "SELECT value FROM v$parameter where name = 'utl_file_dir'";
    String SELECT_LOG_FILES = "select MEMBER from v$logfile order by GROUP#";
    String CURRENT_DB_SCN_SQL = "select min(current_scn) CURRENT_SCN from gv$database";
    String DICTIONARY_BUILD = "begin \ndbms_logmnr_d.build(dictionary_filename => ?, dictionary_location => ?); \nend;";
    String START_LOGMINER="begin \nSYS.DBMS_LOGMNR.start_logmnr(STARTSCN => ?, OPTIONS => SYS.DBMS_LOGMNR.DICT_FROM_ONLINE_CATALOG + SYS.DBMS_LOGMNR.COMMITTED_DATA_ONLY); \nend;";
    String NEW_DBMS_LOGMNR="begin \nSYS.DBMS_LOGMNR.add_logfile('?', SYS.DBMS_LOGMNR.new); \n";
    String ADD_DBMS_LOGMNR="SYS.DBMS_LOGMNR.add_logfile('?', SYS.DBMS_LOGMNR.addfile); \n";
    String LOGMINER_SELECT_WITHSCHEMA="SELECT thread#, scn, start_scn, nvl(commit_scn,scn) commit_scn ,(xidusn||'.'||xidslt||'.'||xidsqn) AS xid,timestamp, "
            + "operation_code, operation,status, SEG_TYPE_NAME ,info,seg_owner, table_name, username, sql_redo ,row_id, csf, TABLE_SPACE, SESSION_INFO, RS_ID, "
            + "RBASQN, RBABLK, SEQUENCE#, TX_NAME, SEG_NAME ,SESSION#,SERIAL# "
            + "FROM  v$logmnr_contents  "
            + "WHERE OPERATION_CODE in (1,2,3,5) and nvl(commit_scn,scn)>=? AND nvl(commit_scn,scn)<=? "
            + " AND (TABLE_SPACE is null or TABLE_SPACE not in ('SYSAUX','SYSTEM','UNKNOWN'))  "; // AND INFO LIKE 'USER DDL%'
    String STOP_LOGMINER_CMD="begin \nSYS.DBMS_LOGMNR.END_LOGMNR; \nend;";

    String LOGMINER_ORIGIN_SQL = "select SQL_TEXT from v$sqlarea " +
            " where last_active_time between to_date(? ,'yyyy-MM-dd HH24:mi:ss')-1/(24*60*60) and to_date(? ,'yyyy-MM-dd HH24:mi:ss') order by last_load_time desc  ";
}