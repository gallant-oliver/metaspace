package io.zeta.metaspace.connector.oracle;

public interface OracleConnectorSQL{
    String LOGMINER_LOG_FILES_LOGMNR$ = "select filename name from v$logmnr_logs";
    String SELECT_UTL_DICTIONARY = "SELECT value FROM v$parameter where name = 'utl_file_dir'";
    String SELECT_LOG_FILES = "select MEMBER from v$logfile order by GROUP#";
    String CURRENT_DB_SCN_SQL = "select min(current_scn) CURRENT_SCN from gv$database";  
    String START_LOGMINER="begin \nSYS.DBMS_LOGMNR.start_logmnr(STARTSCN => ?, dictfilename=> ?); \nend;";
    String NEW_DBMS_LOGMNR="begin \nSYS.DBMS_LOGMNR.add_logfile(?, SYS.DBMS_LOGMNR.new); \nend;";
    String ADD_DBMS_LOGMNR="begin \nSYS.DBMS_LOGMNR.add_logfile(?, SYS.DBMS_LOGMNR.addfile); \nend;";
    String LOGMINER_SELECT_WITHSCHEMA="SELECT thread#, scn, start_scn, nvl(commit_scn,scn) commit_scn ,(xidusn||'.'||xidslt||'.'||xidsqn) AS xid,timestamp, operation_code, operation,status, SEG_TYPE_NAME ,info,seg_owner, table_name, username, sql_redo ,row_id, csf, TABLE_SPACE, SESSION_INFO, RS_ID, RBASQN, RBABLK, SEQUENCE#, TX_NAME, SEG_NAME, SEG_TYPE_NAME FROM  v$logmnr_contents  WHERE OPERATION_CODE in (1,2,3,5) and nvl(commit_scn,scn)>=? AND seg_owner ='MYTEST'";
    String STOP_LOGMINER_CMD="begin \nSYS.DBMS_LOGMNR.END_LOGMNR; \nend;";
}