--
-- PostgreSQL database dump
--

-- Dumped from database version 10.5
-- Dumped by pg_dump version 10.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: business2table; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.business2table (
    businessid character varying(255) DEFAULT NULL::character varying NOT NULL,
    tableguid character varying(255) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.business2table OWNER TO metaspace;

--
-- Name: business_relation; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.business_relation (
    categoryguid character varying(255) DEFAULT NULL::character varying NOT NULL,
    relationshipguid character varying(255) DEFAULT NULL::character varying NOT NULL,
    businessid character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.business_relation OWNER TO metaspace;

--
-- Name: businessinfo; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.businessinfo (
    businessid character varying(255) DEFAULT NULL::character varying NOT NULL,
    departmentid character varying(255) DEFAULT NULL::character varying,
    name character varying(255) DEFAULT NULL::character varying,
    module character varying(255) DEFAULT NULL::character varying,
    description character varying(255) DEFAULT NULL::character varying,
    owner character varying(255) DEFAULT NULL::character varying,
    manager character varying(255) DEFAULT NULL::character varying,
    maintainer character varying(255) DEFAULT NULL::character varying,
    dataassets character varying(255) DEFAULT NULL::character varying,
    businesslastupdate character varying(255) DEFAULT NULL::character varying,
    businessoperator character varying(255) DEFAULT NULL::character varying,
    technicallastupdate character varying(255) DEFAULT NULL::character varying,
    technicaloperator character varying(255) DEFAULT NULL::character varying,
    technicalstatus smallint,
    businessstatus smallint,
    submitter character varying,
    ticketnumber character varying,
    submissiontime character varying,
    level2categoryid character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.businessinfo OWNER TO metaspace;

--
-- Name: category; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.category (
    guid text NOT NULL,
    description text,
    name text,
    upbrothercategoryguid text,
    downbrothercategoryguid text,
    parentcategoryguid text,
    qualifiedname text,
    categorytype smallint,
    level smallint
);


ALTER TABLE public.category OWNER TO metaspace;

--
-- Name: module; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.module (
    moduleid integer NOT NULL,
    modulename character varying(255) DEFAULT NULL::character varying,
    type integer
);


ALTER TABLE public.module OWNER TO metaspace;

--
-- Name: COLUMN module.moduleid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.module.moduleid IS '权限id';


--
-- Name: COLUMN module.modulename; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.module.modulename IS '权限名';


--
-- Name: COLUMN module.type; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.module.type IS '模块类型';


--
-- Name: privilege; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.privilege (
    privilegeid character varying(255) DEFAULT NULL::character varying NOT NULL,
    privilegename character varying(255) DEFAULT NULL::character varying,
    description character varying(255) DEFAULT NULL::character varying,
    createtime character varying(255) DEFAULT NULL::character varying,
    edit smallint,
    delete smallint
);


ALTER TABLE public.privilege OWNER TO metaspace;

--
-- Name: COLUMN privilege.privilegeid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.privilegeid IS '方案id';


--
-- Name: COLUMN privilege.privilegename; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.privilegename IS '方案名';


--
-- Name: COLUMN privilege.description; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.description IS '方案描述';


--
-- Name: COLUMN privilege.createtime; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.createtime IS '创建时间';


--
-- Name: COLUMN privilege.edit; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.edit IS '是否可编辑';


--
-- Name: COLUMN privilege.delete; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.privilege.delete IS '是否可删除';


--
-- Name: privilege2module; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.privilege2module (
    privilegeid character varying(255) DEFAULT NULL::character varying NOT NULL,
    moduleid integer NOT NULL
);


ALTER TABLE public.privilege2module OWNER TO metaspace;

--
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_blob_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    blob_data bytea
);


ALTER TABLE public.qrtz_blob_triggers OWNER TO metaspace;

--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_calendars (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    calendar_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    calendar bytea NOT NULL
);


ALTER TABLE public.qrtz_calendars OWNER TO metaspace;

--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_cron_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    cron_expression character varying(120) DEFAULT NULL::character varying NOT NULL,
    time_zone_id character varying(80) DEFAULT NULL::character varying
);


ALTER TABLE public.qrtz_cron_triggers OWNER TO metaspace;

--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_fired_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    entry_id character varying(95) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    instance_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    fired_time bigint NOT NULL,
    sched_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) DEFAULT NULL::character varying NOT NULL,
    job_name character varying(200) DEFAULT NULL::character varying,
    job_group character varying(200) DEFAULT NULL::character varying,
    is_nonconcurrent boolean,
    requests_recovery boolean
);


ALTER TABLE public.qrtz_fired_triggers OWNER TO metaspace;

--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_job_details (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    job_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    job_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    description character varying(250) DEFAULT NULL::character varying,
    job_class_name character varying(250) DEFAULT NULL::character varying NOT NULL,
    is_durable boolean NOT NULL,
    is_nonconcurrent boolean NOT NULL,
    is_update_data boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);


ALTER TABLE public.qrtz_job_details OWNER TO metaspace;

--
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_locks (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    lock_name character varying(40) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.qrtz_locks OWNER TO metaspace;

--
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_paused_trigger_grps (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.qrtz_paused_trigger_grps OWNER TO metaspace;

--
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_scheduler_state (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    instance_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


ALTER TABLE public.qrtz_scheduler_state OWNER TO metaspace;

--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_simple_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


ALTER TABLE public.qrtz_simple_triggers OWNER TO metaspace;

--
-- Name: qrtz_simprop_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_simprop_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    str_prop_1 character varying(512) DEFAULT NULL::character varying,
    str_prop_2 character varying(512) DEFAULT NULL::character varying,
    str_prop_3 character varying(512) DEFAULT NULL::character varying,
    int_prop_1 integer,
    int_prop_2 integer,
    long_prop_1 bigint,
    long_prop_2 bigint,
    dec_prop_1 numeric(13,4) DEFAULT NULL::numeric,
    dec_prop_2 numeric(13,4) DEFAULT NULL::numeric,
    bool_prop_1 boolean,
    bool_prop_2 boolean
);


ALTER TABLE public.qrtz_simprop_triggers OWNER TO metaspace;

--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.qrtz_triggers (
    sched_name character varying(120) DEFAULT NULL::character varying NOT NULL,
    trigger_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    trigger_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    job_name character varying(200) DEFAULT NULL::character varying NOT NULL,
    job_group character varying(200) DEFAULT NULL::character varying NOT NULL,
    description character varying(250) DEFAULT NULL::character varying,
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) DEFAULT NULL::character varying NOT NULL,
    trigger_type character varying(8) DEFAULT NULL::character varying NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200) DEFAULT NULL::character varying,
    misfire_instr smallint,
    job_data bytea
);


ALTER TABLE public.qrtz_triggers OWNER TO metaspace;

--
-- Name: report; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.report (
    reportid character varying(255) DEFAULT NULL::character varying NOT NULL,
    reportname character varying(255) DEFAULT NULL::character varying,
    templatename character varying(255) DEFAULT NULL::character varying,
    periodcron character varying(255) DEFAULT NULL::character varying,
    orangealerts bigint,
    redalerts bigint,
    source character varying(255) DEFAULT NULL::character varying,
    buildtype integer,
    reportproducedate character varying(255) DEFAULT NULL::character varying,
    templateid character varying(255) DEFAULT NULL::character varying,
    alert smallint
);


ALTER TABLE public.report OWNER TO metaspace;

--
-- Name: report_userrule; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.report_userrule (
    reportid character varying,
    reportrulevalue double precision,
    reportrulestatus smallint,
    ruleid character varying NOT NULL,
    ruletype integer,
    rulename character varying(255) DEFAULT NULL::character varying,
    ruleinfo character varying(255) DEFAULT NULL::character varying,
    rulecolumnname character varying(255) DEFAULT NULL::character varying,
    rulecolumntype character varying(255) DEFAULT NULL::character varying,
    rulechecktype integer,
    rulecheckexpression integer,
    rulecheckthresholdunit character varying(255) DEFAULT NULL::character varying,
    refvalue double precision,
    templateruleid character varying,
    generatetime double precision
);


ALTER TABLE public.report_userrule OWNER TO metaspace;

--
-- Name: report_userrule2threshold; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.report_userrule2threshold (
    thresholdvalue double precision NOT NULL,
    ruleid character varying(255) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.report_userrule2threshold OWNER TO metaspace;

--
-- Name: role; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.role (
    roleid character varying(255) DEFAULT NULL::character varying NOT NULL,
    rolename character varying(255) DEFAULT NULL::character varying,
    description character varying(255) DEFAULT NULL::character varying,
    privilegeid character varying(255) DEFAULT NULL::character varying,
    updatetime character varying(255) DEFAULT NULL::character varying,
    status smallint,
    createtime character varying(255) DEFAULT NULL::character varying,
    disable smallint,
    delete smallint,
    edit smallint
);


ALTER TABLE public.role OWNER TO metaspace;

--
-- Name: COLUMN role.roleid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.roleid IS '角色id';


--
-- Name: COLUMN role.rolename; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.rolename IS '角色名';


--
-- Name: COLUMN role.description; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.description IS '角色描述';


--
-- Name: COLUMN role.privilegeid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.privilegeid IS '方案id';


--
-- Name: COLUMN role.updatetime; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.updatetime IS '角色更新时间';


--
-- Name: COLUMN role.status; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.status IS '角色是否启用，0未启用，1已启用';


--
-- Name: COLUMN role.createtime; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.createtime IS '创建时间';


--
-- Name: COLUMN role.disable; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.disable IS '是否可禁用';


--
-- Name: COLUMN role.delete; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.delete IS '是否可删除';


--
-- Name: COLUMN role.edit; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role.edit IS '是否可编辑';


--
-- Name: role2category; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.role2category (
    roleid character varying(255) DEFAULT NULL::character varying NOT NULL,
    categoryid character varying(255) DEFAULT NULL::character varying NOT NULL,
    operation smallint
);


ALTER TABLE public.role2category OWNER TO metaspace;

--
-- Name: COLUMN role2category.operation; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.role2category.operation IS '是否允许操作，0不允许，1允许';


--
-- Name: rule2buildtype; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.rule2buildtype (
    ruleid smallint NOT NULL,
    buildtype smallint NOT NULL
);


ALTER TABLE public.rule2buildtype OWNER TO metaspace;

--
-- Name: rule2checktype; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.rule2checktype (
    ruleid smallint NOT NULL,
    checktype smallint NOT NULL
);


ALTER TABLE public.rule2checktype OWNER TO metaspace;

--
-- Name: rule2datatype; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.rule2datatype (
    ruleid smallint NOT NULL,
    datatype smallint NOT NULL
);


ALTER TABLE public.rule2datatype OWNER TO metaspace;

--
-- Name: statistical; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.statistical (
    statisticalid character varying(255) DEFAULT NULL::character varying NOT NULL,
    date bigint,
    statistical character varying(255) DEFAULT NULL::character varying,
    statisticaltypeid integer
);


ALTER TABLE public.statistical OWNER TO metaspace;

--
-- Name: statisticaltype; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.statisticaltype (
    statisticaltypeid integer NOT NULL,
    name character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.statisticaltype OWNER TO metaspace;

--
-- Name: systemrule; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.systemrule (
    ruleid smallint NOT NULL,
    rulename character varying(255) DEFAULT NULL::character varying,
    ruleinfo character varying(255) DEFAULT NULL::character varying,
    ruletype smallint,
    rulecheckthresholdunit character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.systemrule OWNER TO metaspace;

--
-- Name: table2tag; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.table2tag (
    tagid character varying NOT NULL,
    tableguid character varying NOT NULL
);


ALTER TABLE public.table2tag OWNER TO metaspace;

--
-- Name: table_category; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.table_category (
    guid text NOT NULL,
    description text,
    name text,
    upbrothercategoryguid text,
    downbrothercategoryguid text,
    parentcategoryguid text,
    qualifiedname text,
    categorytype smallint
);


ALTER TABLE public.table_category OWNER TO metaspace;

--
-- Name: table_relation; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.table_relation (
    relationshipguid character varying NOT NULL,
    categoryguid character varying,
    tableguid character varying
);


ALTER TABLE public.table_relation OWNER TO metaspace;

--
-- Name: tableinfo; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.tableinfo (
    tableguid character varying(255) DEFAULT NULL::character varying NOT NULL,
    tablename character varying(255) DEFAULT NULL::character varying,
    dbname character varying(255) DEFAULT NULL::character varying,
    status character varying(255) DEFAULT NULL::character varying,
    createtime character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.tableinfo OWNER TO metaspace;

--
-- Name: tag; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.tag (
    tagid character varying NOT NULL,
    tagname character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.tag OWNER TO metaspace;

--
-- Name: template; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.template (
    templateid character varying NOT NULL,
    tableid character varying,
    buildtype smallint,
    periodcron character varying(255) DEFAULT NULL::character varying,
    starttime character varying,
    templatestatus smallint,
    templatename character varying(255) DEFAULT NULL::character varying,
    tablerulesnum smallint,
    columnrulesnum smallint,
    source character varying(255) DEFAULT NULL::character varying,
    finishedpercent numeric(53,2) DEFAULT NULL::numeric,
    shutdown boolean
);


ALTER TABLE public.template OWNER TO metaspace;

--
-- Name: template2qrtz_job; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.template2qrtz_job (
    templateid character varying NOT NULL,
    qrtz_job character varying(255) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.template2qrtz_job OWNER TO metaspace;

--
-- Name: template_userrule; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.template_userrule (
    ruleid character varying NOT NULL,
    rulename character varying(255) DEFAULT NULL::character varying,
    ruleinfo character varying(255) DEFAULT NULL::character varying,
    rulecolumnname character varying(255) DEFAULT NULL::character varying,
    rulecolumntype character varying(255) DEFAULT NULL::character varying,
    rulechecktype smallint,
    rulecheckexpression smallint,
    rulecheckthresholdunit character varying(255) DEFAULT NULL::character varying,
    templateid character varying,
    datatype smallint,
    ruletype smallint,
    systemruleid character varying,
    generatetime double precision
);


ALTER TABLE public.template_userrule OWNER TO metaspace;

--
-- Name: template_userrule2threshold; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.template_userrule2threshold (
    thresholdvalue double precision NOT NULL,
    ruleid character varying(255) DEFAULT NULL::character varying NOT NULL
);


ALTER TABLE public.template_userrule2threshold OWNER TO metaspace;

--
-- Name: users; Type: TABLE; Schema: public; Owner: metaspace
--

CREATE TABLE public.users (
    userid character varying(255) DEFAULT NULL::character varying NOT NULL,
    username character varying(255) DEFAULT NULL::character varying,
    account character varying(255) DEFAULT NULL::character varying,
    roleid character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE public.users OWNER TO metaspace;

--
-- Name: COLUMN users.userid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.users.userid IS '用户id';


--
-- Name: COLUMN users.username; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.users.username IS '用户名';


--
-- Name: COLUMN users.account; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.users.account IS '用户账号';


--
-- Name: COLUMN users.roleid; Type: COMMENT; Schema: public; Owner: metaspace
--

COMMENT ON COLUMN public.users.roleid IS '用户角色id';


--
-- Name: privilege2module blueprint2privilege_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.privilege2module
    ADD CONSTRAINT blueprint2privilege_pkey PRIMARY KEY (privilegeid, moduleid);


--
-- Name: privilege buleprint_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.privilege
    ADD CONSTRAINT buleprint_pkey PRIMARY KEY (privilegeid);


--
-- Name: business2table business2table_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.business2table
    ADD CONSTRAINT business2table_pkey PRIMARY KEY (businessid, tableguid);


--
-- Name: businessinfo business_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.businessinfo
    ADD CONSTRAINT business_pkey PRIMARY KEY (businessid);


--
-- Name: business_relation business_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.business_relation
    ADD CONSTRAINT business_relation_pkey PRIMARY KEY (relationshipguid);


--
-- Name: table_category category_copy1_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.table_category
    ADD CONSTRAINT category_copy1_pkey PRIMARY KEY (guid);


--
-- Name: module privilege_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.module
    ADD CONSTRAINT privilege_pkey PRIMARY KEY (moduleid);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_calendars qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (sched_name, calendar_name);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_fired_triggers qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (sched_name, entry_id);


--
-- Name: qrtz_job_details qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (sched_name, job_name, job_group);


--
-- Name: qrtz_locks qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (sched_name, lock_name);


--
-- Name: qrtz_paused_trigger_grps qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (sched_name, trigger_group);


--
-- Name: qrtz_scheduler_state qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (sched_name, instance_name);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (sched_name, trigger_name, trigger_group);


--
-- Name: report report_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.report
    ADD CONSTRAINT report_pkey PRIMARY KEY (reportid);


--
-- Name: report_userrule report_ruleresult_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.report_userrule
    ADD CONSTRAINT report_ruleresult_pkey PRIMARY KEY (ruleid);


--
-- Name: report_userrule2threshold report_threshold_value_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.report_userrule2threshold
    ADD CONSTRAINT report_threshold_value_pkey PRIMARY KEY (thresholdvalue, ruleid);


--
-- Name: role2category role2category_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.role2category
    ADD CONSTRAINT role2category_pkey PRIMARY KEY (roleid, categoryid);


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pkey PRIMARY KEY (roleid);


--
-- Name: rule2buildtype rule2buildtype_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.rule2buildtype
    ADD CONSTRAINT rule2buildtype_pkey PRIMARY KEY (ruleid, buildtype);


--
-- Name: rule2checktype rule2checktypeid_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.rule2checktype
    ADD CONSTRAINT rule2checktypeid_pkey PRIMARY KEY (ruleid, checktype);


--
-- Name: rule2datatype rule2datatype_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.rule2datatype
    ADD CONSTRAINT rule2datatype_pkey PRIMARY KEY (datatype, ruleid);


--
-- Name: systemrule rule_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.systemrule
    ADD CONSTRAINT rule_pkey PRIMARY KEY (ruleid);


--
-- Name: statistical statistical_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.statistical
    ADD CONSTRAINT statistical_pkey PRIMARY KEY (statisticalid);


--
-- Name: statisticaltype statisticaltype_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.statisticaltype
    ADD CONSTRAINT statisticaltype_pkey PRIMARY KEY (statisticaltypeid);


--
-- Name: template_userrule system_rule_copy1_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.template_userrule
    ADD CONSTRAINT system_rule_copy1_pkey PRIMARY KEY (ruleid);


--
-- Name: category table_catalog_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT table_catalog_pkey PRIMARY KEY (guid);


--
-- Name: tableinfo table_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.tableinfo
    ADD CONSTRAINT table_pkey PRIMARY KEY (tableguid);


--
-- Name: table_relation table_relation_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.table_relation
    ADD CONSTRAINT table_relation_pkey PRIMARY KEY (relationshipguid);


--
-- Name: tag tag_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.tag
    ADD CONSTRAINT tag_pkey PRIMARY KEY (tagid);


--
-- Name: table2tag tagid2tableid_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.table2tag
    ADD CONSTRAINT tagid2tableid_pkey PRIMARY KEY (tagid, tableguid);


--
-- Name: template2qrtz_job template2qrtz_trigger_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.template2qrtz_job
    ADD CONSTRAINT template2qrtz_trigger_pkey PRIMARY KEY (templateid, qrtz_job);


--
-- Name: template template_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.template
    ADD CONSTRAINT template_pkey PRIMARY KEY (templateid);


--
-- Name: template_userrule2threshold threshold_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.template_userrule2threshold
    ADD CONSTRAINT threshold_pkey PRIMARY KEY (thresholdvalue, ruleid);


--
-- Name: users user_pkey; Type: CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT user_pkey PRIMARY KEY (userid);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON public.qrtz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_j_g ON public.qrtz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_jg ON public.qrtz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_t_g ON public.qrtz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_tg ON public.qrtz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON public.qrtz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_j_grp ON public.qrtz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_j_req_recovery ON public.qrtz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_c ON public.qrtz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_g; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_g ON public.qrtz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_j ON public.qrtz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_jg ON public.qrtz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_n_g_state ON public.qrtz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_n_state ON public.qrtz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_next_fire_time ON public.qrtz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_nft_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_nft_st ON public.qrtz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON public.qrtz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_state; Type: INDEX; Schema: public; Owner: metaspace
--

CREATE INDEX idx_qrtz_t_state ON public.qrtz_triggers USING btree (sched_name, trigger_state);


--
-- Name: qrtz_blob_triggers qrtz_blob_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_cron_triggers qrtz_cron_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simple_triggers qrtz_simple_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_simprop_triggers qrtz_simprop_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_simprop_triggers
    ADD CONSTRAINT qrtz_simprop_triggers_sched_name_fkey FOREIGN KEY (sched_name, trigger_name, trigger_group) REFERENCES public.qrtz_triggers(sched_name, trigger_name, trigger_group);


--
-- Name: qrtz_triggers qrtz_triggers_sched_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: metaspace
--

ALTER TABLE ONLY public.qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_sched_name_fkey FOREIGN KEY (sched_name, job_name, job_group) REFERENCES public.qrtz_job_details(sched_name, job_name, job_group);


--
-- PostgreSQL database dump complete
--

