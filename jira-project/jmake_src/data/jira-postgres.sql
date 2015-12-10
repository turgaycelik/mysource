--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;

SET search_path = public, pg_catalog;

ALTER TABLE ONLY public."AO_E8B6CC_REPO_TO_CHANGESET" DROP CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_repository_id;
ALTER TABLE ONLY public."AO_E8B6CC_REPO_TO_CHANGESET" DROP CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_changeset_id;
ALTER TABLE ONLY public."AO_E8B6CC_PR_TO_COMMIT" DROP CONSTRAINT fk_ao_e8b6cc_pr_to_commit_request_id;
ALTER TABLE ONLY public."AO_E8B6CC_PR_TO_COMMIT" DROP CONSTRAINT fk_ao_e8b6cc_pr_to_commit_commit_id;
ALTER TABLE ONLY public."AO_E8B6CC_PR_PARTICIPANT" DROP CONSTRAINT fk_ao_e8b6cc_pr_participant_pull_request_id;
ALTER TABLE ONLY public."AO_E8B6CC_MESSAGE_TAG" DROP CONSTRAINT fk_ao_e8b6cc_message_tag_message_id;
ALTER TABLE ONLY public."AO_E8B6CC_MESSAGE_QUEUE_ITEM" DROP CONSTRAINT fk_ao_e8b6cc_message_queue_item_message_id;
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_TO_CHANGESET" DROP CONSTRAINT fk_ao_e8b6cc_issue_to_changeset_changeset_id;
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_TO_BRANCH" DROP CONSTRAINT fk_ao_e8b6cc_issue_to_branch_branch_id;
ALTER TABLE ONLY public."AO_E8B6CC_GIT_HUB_EVENT" DROP CONSTRAINT fk_ao_e8b6cc_git_hub_event_repository_id;
ALTER TABLE ONLY public."AO_E8B6CC_COMMIT_ISSUE_KEY" DROP CONSTRAINT fk_ao_e8b6cc_commit_issue_key_commit_id;
ALTER TABLE ONLY public."AO_E8B6CC_BRANCH" DROP CONSTRAINT fk_ao_e8b6cc_branch_repository_id;
ALTER TABLE ONLY public."AO_E8B6CC_BRANCH_HEAD_MAPPING" DROP CONSTRAINT fk_ao_e8b6cc_branch_head_mapping_repository_id;
ALTER TABLE ONLY public."AO_563AEE_TARGET_ENTITY" DROP CONSTRAINT fk_ao_563aee_target_entity_image_id;
ALTER TABLE ONLY public."AO_563AEE_OBJECT_ENTITY" DROP CONSTRAINT fk_ao_563aee_object_entity_image_id;
ALTER TABLE ONLY public."AO_563AEE_ACTIVITY_ENTITY" DROP CONSTRAINT fk_ao_563aee_activity_entity_target_id;
ALTER TABLE ONLY public."AO_563AEE_ACTIVITY_ENTITY" DROP CONSTRAINT fk_ao_563aee_activity_entity_object_id;
ALTER TABLE ONLY public."AO_563AEE_ACTIVITY_ENTITY" DROP CONSTRAINT fk_ao_563aee_activity_entity_icon_id;
ALTER TABLE ONLY public."AO_563AEE_ACTIVITY_ENTITY" DROP CONSTRAINT fk_ao_563aee_activity_entity_actor_id;
DROP INDEX public.worklog_issue;
DROP INDEX public.worklog_author;
DROP INDEX public.workflow_scheme;
DROP INDEX public.wf_entryid;
DROP INDEX public.votehistory_issue_index;
DROP INDEX public.userpref_portletconfiguration;
DROP INDEX public.user_source;
DROP INDEX public.user_sink;
DROP INDEX public.upf_fieldconfigid;
DROP INDEX public.upf_customfield;
DROP INDEX public.uk_user_name_dir_id;
DROP INDEX public.uk_user_key;
DROP INDEX public.uk_user_externalid_dir_id;
DROP INDEX public.uk_user_attr_name_lval;
DROP INDEX public.uk_mem_parent_child_type;
DROP INDEX public.uk_lower_user_name;
DROP INDEX public.uk_group_name_dir_id;
DROP INDEX public.uk_group_attr_name_lval;
DROP INDEX public.uk_directory_name;
DROP INDEX public.uk_application_name;
DROP INDEX public.uh_type_user_entity;
DROP INDEX public.type_key;
DROP INDEX public.trustedapp_id;
DROP INDEX public.subscrptn_group;
DROP INDEX public.subscrpt_user;
DROP INDEX public.sr_author;
DROP INDEX public.source_destination_node_idx;
DROP INDEX public.share_index;
DROP INDEX public.sec_security;
DROP INDEX public.sec_scheme;
DROP INDEX public.searchrequest_filternamelower;
DROP INDEX public.screenitem_scheme;
DROP INDEX public.rundetails_starttime_idx;
DROP INDEX public.rundetails_jobid_idx;
DROP INDEX public.role_player_idx;
DROP INDEX public.remotelink_issueid;
DROP INDEX public.remotelink_globalid;
DROP INDEX public.remembermetoken_username_index;
DROP INDEX public.prmssn_scheme;
DROP INDEX public.ppage_username;
DROP INDEX public.osuser_name;
DROP INDEX public.osproperty_propertykey;
DROP INDEX public.osproperty_entityname;
DROP INDEX public.osproperty_all;
DROP INDEX public.osgroup_name;
DROP INDEX public.oauth_sp_token_index;
DROP INDEX public.oauth_sp_consumer_key_index;
DROP INDEX public.oauth_sp_consumer_index;
DROP INDEX public.oauth_consumer_token_key_index;
DROP INDEX public.oauth_consumer_token_index;
DROP INDEX public.oauth_consumer_service_index;
DROP INDEX public.oauth_consumer_index;
DROP INDEX public.ntfctn_scheme;
DROP INDEX public.notif_source;
DROP INDEX public.node_source;
DROP INDEX public.node_sink;
DROP INDEX public.node_operation_idx;
DROP INDEX public.node_id_idx;
DROP INDEX public.mshipbase_user;
DROP INDEX public.mshipbase_group;
DROP INDEX public.managedconfigitem_id_type_idx;
DROP INDEX public.linktypestyle;
DROP INDEX public.linktypename;
DROP INDEX public.label_label;
DROP INDEX public.label_issue;
DROP INDEX public.label_fieldissuelabel;
DROP INDEX public.label_fieldissue;
DROP INDEX public.issuelink_type;
DROP INDEX public.issuelink_src;
DROP INDEX public.issuelink_dest;
DROP INDEX public.issue_workflow;
DROP INDEX public.issue_updated;
DROP INDEX public.issue_proj_status;
DROP INDEX public.issue_proj_num;
DROP INDEX public.issue_assignee;
DROP INDEX public.index_ao_e8b6cc_rep922992576;
DROP INDEX public.index_ao_e8b6cc_rep1082901832;
DROP INDEX public.index_ao_e8b6cc_pr_1458633226;
DROP INDEX public.index_ao_e8b6cc_pr_1105917040;
DROP INDEX public.index_ao_e8b6cc_pr_1045528152;
DROP INDEX public.index_ao_e8b6cc_mes344532677;
DROP INDEX public.index_ao_e8b6cc_mes1391090780;
DROP INDEX public.index_ao_e8b6cc_iss1325927291;
DROP INDEX public.index_ao_e8b6cc_iss1229805759;
DROP INDEX public.index_ao_e8b6cc_git1804640320;
DROP INDEX public.index_ao_e8b6cc_com1773674409;
DROP INDEX public.index_ao_e8b6cc_bra405461593;
DROP INDEX public.index_ao_e8b6cc_bra1368852151;
DROP INDEX public.index_ao_563aee_tar521440921;
DROP INDEX public.index_ao_563aee_obj696886343;
DROP INDEX public.index_ao_563aee_act995325379;
DROP INDEX public.index_ao_563aee_act972488439;
DROP INDEX public.index_ao_563aee_act1978295567;
DROP INDEX public.index_ao_563aee_act1642652291;
DROP INDEX public.idx_user_attr_dir_name_lval;
DROP INDEX public.idx_qrtz_t_nft_st_misfire_grp;
DROP INDEX public.idx_qrtz_t_nft_st_misfire;
DROP INDEX public.idx_qrtz_t_nft_st;
DROP INDEX public.idx_qrtz_t_nft_misfire;
DROP INDEX public.idx_qrtz_t_next_fire_time;
DROP INDEX public.idx_qrtz_t_n_state;
DROP INDEX public.idx_qrtz_t_n_g_state;
DROP INDEX public.idx_qrtz_t_jg;
DROP INDEX public.idx_qrtz_t_j;
DROP INDEX public.idx_qrtz_t_c;
DROP INDEX public.idx_qrtz_j_state;
DROP INDEX public.idx_qrtz_j_req_recovery;
DROP INDEX public.idx_qrtz_j_grp;
DROP INDEX public.idx_qrtz_j_g;
DROP INDEX public.idx_qrtz_ft_trig_inst_name;
DROP INDEX public.idx_qrtz_ft_tg;
DROP INDEX public.idx_qrtz_ft_t_g;
DROP INDEX public.idx_qrtz_ft_jg;
DROP INDEX public.idx_qrtz_ft_j_g;
DROP INDEX public.idx_qrtz_ft_inst_job_req_rcvry;
DROP INDEX public.idx_project_key;
DROP INDEX public.idx_old_issue_key;
DROP INDEX public.idx_mem_dir_parent_child;
DROP INDEX public.idx_mem_dir_parent;
DROP INDEX public.idx_mem_dir_child;
DROP INDEX public.idx_last_name;
DROP INDEX public.idx_group_dir_id;
DROP INDEX public.idx_group_attr_dir_name_lval;
DROP INDEX public.idx_group_active;
DROP INDEX public.idx_first_name;
DROP INDEX public.idx_email_address;
DROP INDEX public.idx_display_name;
DROP INDEX public.idx_directory_type;
DROP INDEX public.idx_directory_impl;
DROP INDEX public.idx_directory_active;
DROP INDEX public.idx_changed_value_log_id;
DROP INDEX public.idx_audit_log_created;
DROP INDEX public.idx_audit_item_log_id2;
DROP INDEX public.idx_all_project_keys;
DROP INDEX public.idx_all_project_ids;
DROP INDEX public.historystep_entryid;
DROP INDEX public.fl_scheme_assoc;
DROP INDEX public.fieldscreen_tab;
DROP INDEX public.fieldscreen_scheme;
DROP INDEX public.fieldscreen_field;
DROP INDEX public.fieldscitem_tab;
DROP INDEX public.fieldlayout_scheme;
DROP INDEX public.fieldlayout_layout;
DROP INDEX public.fieldid_optionid;
DROP INDEX public.fieldid_fieldconf;
DROP INDEX public.feature_id_userkey;
DROP INDEX public.fcs_scheme;
DROP INDEX public.fcs_issuetype;
DROP INDEX public.fcs_fieldid;
DROP INDEX public.fc_fieldid;
DROP INDEX public.favourite_index;
DROP INDEX public.ext_entity_name;
DROP INDEX public.entpropindexdoc_module;
DROP INDEX public.entityproperty_identiti;
DROP INDEX public.draft_workflow_scheme_parent;
DROP INDEX public.draft_workflow_scheme;
DROP INDEX public.confcontextprojectkey;
DROP INDEX public.confcontextfieldconfigscheme;
DROP INDEX public.confcontext;
DROP INDEX public.cluster_lock_name_idx;
DROP INDEX public.cl_username;
DROP INDEX public.cl_searchrequest;
DROP INDEX public.chgitem_field;
DROP INDEX public.chgitem_chggrp;
DROP INDEX public.chggroup_issue;
DROP INDEX public.cfvalue_issue;
DROP INDEX public.cf_userpickerfilterrole;
DROP INDEX public.cf_userpickerfiltergroup;
DROP INDEX public.cf_cfoption;
DROP INDEX public.avatar_index;
DROP INDEX public.attach_issue;
DROP INDEX public.action_issue;
DROP INDEX public.action_authorupdated;
DROP INDEX public.action_authorcreated;
ALTER TABLE ONLY public.worklog DROP CONSTRAINT pk_worklog;
ALTER TABLE ONLY public.workflowschemeentity DROP CONSTRAINT pk_workflowschemeentity;
ALTER TABLE ONLY public.workflowscheme DROP CONSTRAINT pk_workflowscheme;
ALTER TABLE ONLY public.votehistory DROP CONSTRAINT pk_votehistory;
ALTER TABLE ONLY public.versioncontrol DROP CONSTRAINT pk_versioncontrol;
ALTER TABLE ONLY public.userpickerfilterrole DROP CONSTRAINT pk_userpickerfilterrole;
ALTER TABLE ONLY public.userpickerfiltergroup DROP CONSTRAINT pk_userpickerfiltergroup;
ALTER TABLE ONLY public.userpickerfilter DROP CONSTRAINT pk_userpickerfilter;
ALTER TABLE ONLY public.userhistoryitem DROP CONSTRAINT pk_userhistoryitem;
ALTER TABLE ONLY public.userbase DROP CONSTRAINT pk_userbase;
ALTER TABLE ONLY public.userassociation DROP CONSTRAINT pk_userassociation;
ALTER TABLE ONLY public.upgradeversionhistory DROP CONSTRAINT pk_upgradeversionhistory;
ALTER TABLE ONLY public.upgradehistory DROP CONSTRAINT pk_upgradehistory;
ALTER TABLE ONLY public.trustedapp DROP CONSTRAINT pk_trustedapp;
ALTER TABLE ONLY public.trackback_ping DROP CONSTRAINT pk_trackback_ping;
ALTER TABLE ONLY public.sharepermissions DROP CONSTRAINT pk_sharepermissions;
ALTER TABLE ONLY public.serviceconfig DROP CONSTRAINT pk_serviceconfig;
ALTER TABLE ONLY public.sequence_value_item DROP CONSTRAINT pk_sequence_value_item;
ALTER TABLE ONLY public.searchrequest DROP CONSTRAINT pk_searchrequest;
ALTER TABLE ONLY public.schemepermissions DROP CONSTRAINT pk_schemepermissions;
ALTER TABLE ONLY public.schemeissuesecuritylevels DROP CONSTRAINT pk_schemeissuesecuritylevels;
ALTER TABLE ONLY public.schemeissuesecurities DROP CONSTRAINT pk_schemeissuesecurities;
ALTER TABLE ONLY public.rundetails DROP CONSTRAINT pk_rundetails;
ALTER TABLE ONLY public.resolution DROP CONSTRAINT pk_resolution;
ALTER TABLE ONLY public.replicatedindexoperation DROP CONSTRAINT pk_replicatedindexoperation;
ALTER TABLE ONLY public.remotelink DROP CONSTRAINT pk_remotelink;
ALTER TABLE ONLY public.remembermetoken DROP CONSTRAINT pk_remembermetoken;
ALTER TABLE ONLY public.qrtz_triggers DROP CONSTRAINT pk_qrtz_triggers;
ALTER TABLE ONLY public.qrtz_trigger_listeners DROP CONSTRAINT pk_qrtz_trigger_listeners;
ALTER TABLE ONLY public.qrtz_simple_triggers DROP CONSTRAINT pk_qrtz_simple_triggers;
ALTER TABLE ONLY public.qrtz_job_listeners DROP CONSTRAINT pk_qrtz_job_listeners;
ALTER TABLE ONLY public.qrtz_job_details DROP CONSTRAINT pk_qrtz_job_details;
ALTER TABLE ONLY public.qrtz_fired_triggers DROP CONSTRAINT pk_qrtz_fired_triggers;
ALTER TABLE ONLY public.qrtz_cron_triggers DROP CONSTRAINT pk_qrtz_cron_triggers;
ALTER TABLE ONLY public.qrtz_calendars DROP CONSTRAINT pk_qrtz_calendars;
ALTER TABLE ONLY public.propertytext DROP CONSTRAINT pk_propertytext;
ALTER TABLE ONLY public.propertystring DROP CONSTRAINT pk_propertystring;
ALTER TABLE ONLY public.propertynumber DROP CONSTRAINT pk_propertynumber;
ALTER TABLE ONLY public.propertyentry DROP CONSTRAINT pk_propertyentry;
ALTER TABLE ONLY public.propertydecimal DROP CONSTRAINT pk_propertydecimal;
ALTER TABLE ONLY public.propertydate DROP CONSTRAINT pk_propertydate;
ALTER TABLE ONLY public.propertydata DROP CONSTRAINT pk_propertydata;
ALTER TABLE ONLY public.projectversion DROP CONSTRAINT pk_projectversion;
ALTER TABLE ONLY public.projectroleactor DROP CONSTRAINT pk_projectroleactor;
ALTER TABLE ONLY public.projectrole DROP CONSTRAINT pk_projectrole;
ALTER TABLE ONLY public.projectcategory DROP CONSTRAINT pk_projectcategory;
ALTER TABLE ONLY public.project_key DROP CONSTRAINT pk_project_key;
ALTER TABLE ONLY public.project DROP CONSTRAINT pk_project;
ALTER TABLE ONLY public.priority DROP CONSTRAINT pk_priority;
ALTER TABLE ONLY public.portletconfiguration DROP CONSTRAINT pk_portletconfiguration;
ALTER TABLE ONLY public.portalpage DROP CONSTRAINT pk_portalpage;
ALTER TABLE ONLY public.pluginversion DROP CONSTRAINT pk_pluginversion;
ALTER TABLE ONLY public.pluginstate DROP CONSTRAINT pk_pluginstate;
ALTER TABLE ONLY public.permissionscheme DROP CONSTRAINT pk_permissionscheme;
ALTER TABLE ONLY public.os_wfentry DROP CONSTRAINT pk_os_wfentry;
ALTER TABLE ONLY public.os_historystep_prev DROP CONSTRAINT pk_os_historystep_prev;
ALTER TABLE ONLY public.os_historystep DROP CONSTRAINT pk_os_historystep;
ALTER TABLE ONLY public.os_currentstep_prev DROP CONSTRAINT pk_os_currentstep_prev;
ALTER TABLE ONLY public.os_currentstep DROP CONSTRAINT pk_os_currentstep;
ALTER TABLE ONLY public.optionconfiguration DROP CONSTRAINT pk_optionconfiguration;
ALTER TABLE ONLY public.oauthsptoken DROP CONSTRAINT pk_oauthsptoken;
ALTER TABLE ONLY public.oauthspconsumer DROP CONSTRAINT pk_oauthspconsumer;
ALTER TABLE ONLY public.oauthconsumertoken DROP CONSTRAINT pk_oauthconsumertoken;
ALTER TABLE ONLY public.oauthconsumer DROP CONSTRAINT pk_oauthconsumer;
ALTER TABLE ONLY public.notificationscheme DROP CONSTRAINT pk_notificationscheme;
ALTER TABLE ONLY public.notificationinstance DROP CONSTRAINT pk_notificationinstance;
ALTER TABLE ONLY public.notification DROP CONSTRAINT pk_notification;
ALTER TABLE ONLY public.nodeindexcounter DROP CONSTRAINT pk_nodeindexcounter;
ALTER TABLE ONLY public.nodeassociation DROP CONSTRAINT pk_nodeassociation;
ALTER TABLE ONLY public.moved_issue_key DROP CONSTRAINT pk_moved_issue_key;
ALTER TABLE ONLY public.membershipbase DROP CONSTRAINT pk_membershipbase;
ALTER TABLE ONLY public.managedconfigurationitem DROP CONSTRAINT pk_managedconfigurationitem;
ALTER TABLE ONLY public.mailserver DROP CONSTRAINT pk_mailserver;
ALTER TABLE ONLY public.listenerconfig DROP CONSTRAINT pk_listenerconfig;
ALTER TABLE ONLY public.label DROP CONSTRAINT pk_label;
ALTER TABLE ONLY public.jquartz_triggers DROP CONSTRAINT pk_jquartz_triggers;
ALTER TABLE ONLY public.jquartz_trigger_listeners DROP CONSTRAINT pk_jquartz_trigger_listeners;
ALTER TABLE ONLY public.jquartz_simprop_triggers DROP CONSTRAINT pk_jquartz_simprop_triggers;
ALTER TABLE ONLY public.jquartz_simple_triggers DROP CONSTRAINT pk_jquartz_simple_triggers;
ALTER TABLE ONLY public.jquartz_scheduler_state DROP CONSTRAINT pk_jquartz_scheduler_state;
ALTER TABLE ONLY public.jquartz_paused_trigger_grps DROP CONSTRAINT pk_jquartz_paused_trigger_grps;
ALTER TABLE ONLY public.jquartz_locks DROP CONSTRAINT pk_jquartz_locks;
ALTER TABLE ONLY public.jquartz_job_listeners DROP CONSTRAINT pk_jquartz_job_listeners;
ALTER TABLE ONLY public.jquartz_job_details DROP CONSTRAINT pk_jquartz_job_details;
ALTER TABLE ONLY public.jquartz_fired_triggers DROP CONSTRAINT pk_jquartz_fired_triggers;
ALTER TABLE ONLY public.jquartz_cron_triggers DROP CONSTRAINT pk_jquartz_cron_triggers;
ALTER TABLE ONLY public.jquartz_calendars DROP CONSTRAINT pk_jquartz_calendars;
ALTER TABLE ONLY public.jquartz_blob_triggers DROP CONSTRAINT pk_jquartz_blob_triggers;
ALTER TABLE ONLY public.jiraworkflows DROP CONSTRAINT pk_jiraworkflows;
ALTER TABLE ONLY public.jiraperms DROP CONSTRAINT pk_jiraperms;
ALTER TABLE ONLY public.jiraissue DROP CONSTRAINT pk_jiraissue;
ALTER TABLE ONLY public.jiraeventtype DROP CONSTRAINT pk_jiraeventtype;
ALTER TABLE ONLY public.jiradraftworkflows DROP CONSTRAINT pk_jiradraftworkflows;
ALTER TABLE ONLY public.jiraaction DROP CONSTRAINT pk_jiraaction;
ALTER TABLE ONLY public.issuetypescreenschemeentity DROP CONSTRAINT pk_issuetypescreenschemeentity;
ALTER TABLE ONLY public.issuetypescreenscheme DROP CONSTRAINT pk_issuetypescreenscheme;
ALTER TABLE ONLY public.issuetype DROP CONSTRAINT pk_issuetype;
ALTER TABLE ONLY public.issuestatus DROP CONSTRAINT pk_issuestatus;
ALTER TABLE ONLY public.issuesecurityscheme DROP CONSTRAINT pk_issuesecurityscheme;
ALTER TABLE ONLY public.issuelinktype DROP CONSTRAINT pk_issuelinktype;
ALTER TABLE ONLY public.issuelink DROP CONSTRAINT pk_issuelink;
ALTER TABLE ONLY public.groupbase DROP CONSTRAINT pk_groupbase;
ALTER TABLE ONLY public.globalpermissionentry DROP CONSTRAINT pk_globalpermissionentry;
ALTER TABLE ONLY public.genericconfiguration DROP CONSTRAINT pk_genericconfiguration;
ALTER TABLE ONLY public.gadgetuserpreference DROP CONSTRAINT pk_gadgetuserpreference;
ALTER TABLE ONLY public.filtersubscription DROP CONSTRAINT pk_filtersubscription;
ALTER TABLE ONLY public.fileattachment DROP CONSTRAINT pk_fileattachment;
ALTER TABLE ONLY public.fieldscreentab DROP CONSTRAINT pk_fieldscreentab;
ALTER TABLE ONLY public.fieldscreenschemeitem DROP CONSTRAINT pk_fieldscreenschemeitem;
ALTER TABLE ONLY public.fieldscreenscheme DROP CONSTRAINT pk_fieldscreenscheme;
ALTER TABLE ONLY public.fieldscreenlayoutitem DROP CONSTRAINT pk_fieldscreenlayoutitem;
ALTER TABLE ONLY public.fieldscreen DROP CONSTRAINT pk_fieldscreen;
ALTER TABLE ONLY public.fieldlayoutschemeentity DROP CONSTRAINT pk_fieldlayoutschemeentity;
ALTER TABLE ONLY public.fieldlayoutschemeassociation DROP CONSTRAINT pk_fieldlayoutschemeassociatio;
ALTER TABLE ONLY public.fieldlayoutscheme DROP CONSTRAINT pk_fieldlayoutscheme;
ALTER TABLE ONLY public.fieldlayoutitem DROP CONSTRAINT pk_fieldlayoutitem;
ALTER TABLE ONLY public.fieldlayout DROP CONSTRAINT pk_fieldlayout;
ALTER TABLE ONLY public.fieldconfiguration DROP CONSTRAINT pk_fieldconfiguration;
ALTER TABLE ONLY public.fieldconfigschemeissuetype DROP CONSTRAINT pk_fieldconfigschemeissuetype;
ALTER TABLE ONLY public.fieldconfigscheme DROP CONSTRAINT pk_fieldconfigscheme;
ALTER TABLE ONLY public.feature DROP CONSTRAINT pk_feature;
ALTER TABLE ONLY public.favouriteassociations DROP CONSTRAINT pk_favouriteassociations;
ALTER TABLE ONLY public.externalgadget DROP CONSTRAINT pk_externalgadget;
ALTER TABLE ONLY public.external_entities DROP CONSTRAINT pk_external_entities;
ALTER TABLE ONLY public.entity_property_index_document DROP CONSTRAINT pk_entity_property_index_docum;
ALTER TABLE ONLY public.entity_property DROP CONSTRAINT pk_entity_property;
ALTER TABLE ONLY public.draftworkflowschemeentity DROP CONSTRAINT pk_draftworkflowschemeentity;
ALTER TABLE ONLY public.draftworkflowscheme DROP CONSTRAINT pk_draftworkflowscheme;
ALTER TABLE ONLY public.cwd_user_attributes DROP CONSTRAINT pk_cwd_user_attributes;
ALTER TABLE ONLY public.cwd_user DROP CONSTRAINT pk_cwd_user;
ALTER TABLE ONLY public.cwd_membership DROP CONSTRAINT pk_cwd_membership;
ALTER TABLE ONLY public.cwd_group_attributes DROP CONSTRAINT pk_cwd_group_attributes;
ALTER TABLE ONLY public.cwd_group DROP CONSTRAINT pk_cwd_group;
ALTER TABLE ONLY public.cwd_directory_operation DROP CONSTRAINT pk_cwd_directory_operation;
ALTER TABLE ONLY public.cwd_directory_attribute DROP CONSTRAINT pk_cwd_directory_attribute;
ALTER TABLE ONLY public.cwd_directory DROP CONSTRAINT pk_cwd_directory;
ALTER TABLE ONLY public.cwd_application_address DROP CONSTRAINT pk_cwd_application_address;
ALTER TABLE ONLY public.cwd_application DROP CONSTRAINT pk_cwd_application;
ALTER TABLE ONLY public.customfieldvalue DROP CONSTRAINT pk_customfieldvalue;
ALTER TABLE ONLY public.customfieldoption DROP CONSTRAINT pk_customfieldoption;
ALTER TABLE ONLY public.customfield DROP CONSTRAINT pk_customfield;
ALTER TABLE ONLY public.configurationcontext DROP CONSTRAINT pk_configurationcontext;
ALTER TABLE ONLY public.component DROP CONSTRAINT pk_component;
ALTER TABLE ONLY public.columnlayoutitem DROP CONSTRAINT pk_columnlayoutitem;
ALTER TABLE ONLY public.columnlayout DROP CONSTRAINT pk_columnlayout;
ALTER TABLE ONLY public.clusternode DROP CONSTRAINT pk_clusternode;
ALTER TABLE ONLY public.clustermessage DROP CONSTRAINT pk_clustermessage;
ALTER TABLE ONLY public.clusterlockstatus DROP CONSTRAINT pk_clusterlockstatus;
ALTER TABLE ONLY public.changeitem DROP CONSTRAINT pk_changeitem;
ALTER TABLE ONLY public.changegroup DROP CONSTRAINT pk_changegroup;
ALTER TABLE ONLY public.avatar DROP CONSTRAINT pk_avatar;
ALTER TABLE ONLY public.audit_log DROP CONSTRAINT pk_audit_log;
ALTER TABLE ONLY public.audit_item DROP CONSTRAINT pk_audit_item;
ALTER TABLE ONLY public.audit_changed_value DROP CONSTRAINT pk_audit_changed_value;
ALTER TABLE ONLY public.app_user DROP CONSTRAINT pk_app_user;
ALTER TABLE ONLY public."AO_E8B6CC_SYNC_AUDIT_LOG" DROP CONSTRAINT "AO_E8B6CC_SYNC_AUDIT_LOG_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_REPO_TO_CHANGESET" DROP CONSTRAINT "AO_E8B6CC_REPO_TO_CHANGESET_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_REPOSITORY_MAPPING" DROP CONSTRAINT "AO_E8B6CC_REPOSITORY_MAPPING_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PULL_REQUEST" DROP CONSTRAINT "AO_E8B6CC_PULL_REQUEST_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PR_TO_COMMIT" DROP CONSTRAINT "AO_E8B6CC_PR_TO_COMMIT_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PR_PARTICIPANT" DROP CONSTRAINT "AO_E8B6CC_PR_PARTICIPANT_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PR_ISSUE_KEY" DROP CONSTRAINT "AO_E8B6CC_PR_ISSUE_KEY_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PROJECT_MAPPING" DROP CONSTRAINT "AO_E8B6CC_PROJECT_MAPPING_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_PROJECT_MAPPING_V2" DROP CONSTRAINT "AO_E8B6CC_PROJECT_MAPPING_V2_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_ORGANIZATION_MAPPING" DROP CONSTRAINT "AO_E8B6CC_ORGANIZATION_MAPPING_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_MESSAGE" DROP CONSTRAINT "AO_E8B6CC_MESSAGE_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_MESSAGE_TAG" DROP CONSTRAINT "AO_E8B6CC_MESSAGE_TAG_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_MESSAGE_QUEUE_ITEM" DROP CONSTRAINT "AO_E8B6CC_MESSAGE_QUEUE_ITEM_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_TO_CHANGESET" DROP CONSTRAINT "AO_E8B6CC_ISSUE_TO_CHANGESET_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_TO_BRANCH" DROP CONSTRAINT "AO_E8B6CC_ISSUE_TO_BRANCH_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_MAPPING" DROP CONSTRAINT "AO_E8B6CC_ISSUE_MAPPING_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_ISSUE_MAPPING_V2" DROP CONSTRAINT "AO_E8B6CC_ISSUE_MAPPING_V2_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_GIT_HUB_EVENT" DROP CONSTRAINT "AO_E8B6CC_GIT_HUB_EVENT_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_COMMIT" DROP CONSTRAINT "AO_E8B6CC_COMMIT_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_COMMIT_ISSUE_KEY" DROP CONSTRAINT "AO_E8B6CC_COMMIT_ISSUE_KEY_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_CHANGESET_MAPPING" DROP CONSTRAINT "AO_E8B6CC_CHANGESET_MAPPING_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_BRANCH" DROP CONSTRAINT "AO_E8B6CC_BRANCH_pkey";
ALTER TABLE ONLY public."AO_E8B6CC_BRANCH_HEAD_MAPPING" DROP CONSTRAINT "AO_E8B6CC_BRANCH_HEAD_MAPPING_pkey";
ALTER TABLE ONLY public."AO_B9A0F0_APPLIED_TEMPLATE" DROP CONSTRAINT "AO_B9A0F0_APPLIED_TEMPLATE_pkey";
ALTER TABLE ONLY public."AO_563AEE_TARGET_ENTITY" DROP CONSTRAINT "AO_563AEE_TARGET_ENTITY_pkey";
ALTER TABLE ONLY public."AO_563AEE_OBJECT_ENTITY" DROP CONSTRAINT "AO_563AEE_OBJECT_ENTITY_pkey";
ALTER TABLE ONLY public."AO_563AEE_MEDIA_LINK_ENTITY" DROP CONSTRAINT "AO_563AEE_MEDIA_LINK_ENTITY_pkey";
ALTER TABLE ONLY public."AO_563AEE_ACTOR_ENTITY" DROP CONSTRAINT "AO_563AEE_ACTOR_ENTITY_pkey";
ALTER TABLE ONLY public."AO_563AEE_ACTIVITY_ENTITY" DROP CONSTRAINT "AO_563AEE_ACTIVITY_ENTITY_pkey";
ALTER TABLE ONLY public."AO_4AEACD_WEBHOOK_DAO" DROP CONSTRAINT "AO_4AEACD_WEBHOOK_DAO_pkey";
ALTER TABLE ONLY public."AO_21D670_WHITELIST_RULES" DROP CONSTRAINT "AO_21D670_WHITELIST_RULES_pkey";
ALTER TABLE public."AO_E8B6CC_SYNC_AUDIT_LOG" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_REPO_TO_CHANGESET" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_REPOSITORY_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PULL_REQUEST" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PR_TO_COMMIT" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PR_PARTICIPANT" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PR_ISSUE_KEY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PROJECT_MAPPING_V2" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_PROJECT_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_ORGANIZATION_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_MESSAGE_TAG" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_MESSAGE_QUEUE_ITEM" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_MESSAGE" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_ISSUE_TO_CHANGESET" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_ISSUE_TO_BRANCH" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_ISSUE_MAPPING_V2" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_ISSUE_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_GIT_HUB_EVENT" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_COMMIT_ISSUE_KEY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_COMMIT" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_CHANGESET_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_BRANCH_HEAD_MAPPING" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_E8B6CC_BRANCH" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_B9A0F0_APPLIED_TEMPLATE" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_563AEE_TARGET_ENTITY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_563AEE_OBJECT_ENTITY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_563AEE_MEDIA_LINK_ENTITY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_563AEE_ACTOR_ENTITY" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_563AEE_ACTIVITY_ENTITY" ALTER COLUMN "ACTIVITY_ID" DROP DEFAULT;
ALTER TABLE public."AO_4AEACD_WEBHOOK_DAO" ALTER COLUMN "ID" DROP DEFAULT;
ALTER TABLE public."AO_21D670_WHITELIST_RULES" ALTER COLUMN "ID" DROP DEFAULT;
DROP TABLE public.worklog;
DROP TABLE public.workflowschemeentity;
DROP TABLE public.workflowscheme;
DROP TABLE public.votehistory;
DROP TABLE public.versioncontrol;
DROP TABLE public.userpickerfilterrole;
DROP TABLE public.userpickerfiltergroup;
DROP TABLE public.userpickerfilter;
DROP TABLE public.userhistoryitem;
DROP TABLE public.userbase;
DROP TABLE public.userassociation;
DROP TABLE public.upgradeversionhistory;
DROP TABLE public.upgradehistory;
DROP TABLE public.trustedapp;
DROP TABLE public.trackback_ping;
DROP TABLE public.sharepermissions;
DROP TABLE public.serviceconfig;
DROP TABLE public.sequence_value_item;
DROP TABLE public.searchrequest;
DROP TABLE public.schemepermissions;
DROP TABLE public.schemeissuesecuritylevels;
DROP TABLE public.schemeissuesecurities;
DROP TABLE public.rundetails;
DROP TABLE public.resolution;
DROP TABLE public.replicatedindexoperation;
DROP TABLE public.remotelink;
DROP TABLE public.remembermetoken;
DROP TABLE public.qrtz_triggers;
DROP TABLE public.qrtz_trigger_listeners;
DROP TABLE public.qrtz_simple_triggers;
DROP TABLE public.qrtz_job_listeners;
DROP TABLE public.qrtz_job_details;
DROP TABLE public.qrtz_fired_triggers;
DROP TABLE public.qrtz_cron_triggers;
DROP TABLE public.qrtz_calendars;
DROP TABLE public.propertytext;
DROP TABLE public.propertystring;
DROP TABLE public.propertynumber;
DROP TABLE public.propertyentry;
DROP TABLE public.propertydecimal;
DROP TABLE public.propertydate;
DROP TABLE public.propertydata;
DROP TABLE public.projectversion;
DROP TABLE public.projectroleactor;
DROP TABLE public.projectrole;
DROP TABLE public.projectcategory;
DROP TABLE public.project_key;
DROP TABLE public.project;
DROP TABLE public.priority;
DROP TABLE public.portletconfiguration;
DROP TABLE public.portalpage;
DROP TABLE public.pluginversion;
DROP TABLE public.pluginstate;
DROP TABLE public.permissionscheme;
DROP TABLE public.os_wfentry;
DROP TABLE public.os_historystep_prev;
DROP TABLE public.os_historystep;
DROP TABLE public.os_currentstep_prev;
DROP TABLE public.os_currentstep;
DROP TABLE public.optionconfiguration;
DROP TABLE public.oauthsptoken;
DROP TABLE public.oauthspconsumer;
DROP TABLE public.oauthconsumertoken;
DROP TABLE public.oauthconsumer;
DROP TABLE public.notificationscheme;
DROP TABLE public.notificationinstance;
DROP TABLE public.notification;
DROP TABLE public.nodeindexcounter;
DROP TABLE public.nodeassociation;
DROP TABLE public.moved_issue_key;
DROP TABLE public.membershipbase;
DROP TABLE public.managedconfigurationitem;
DROP TABLE public.mailserver;
DROP TABLE public.listenerconfig;
DROP TABLE public.label;
DROP TABLE public.jquartz_triggers;
DROP TABLE public.jquartz_trigger_listeners;
DROP TABLE public.jquartz_simprop_triggers;
DROP TABLE public.jquartz_simple_triggers;
DROP TABLE public.jquartz_scheduler_state;
DROP TABLE public.jquartz_paused_trigger_grps;
DROP TABLE public.jquartz_locks;
DROP TABLE public.jquartz_job_listeners;
DROP TABLE public.jquartz_job_details;
DROP TABLE public.jquartz_fired_triggers;
DROP TABLE public.jquartz_cron_triggers;
DROP TABLE public.jquartz_calendars;
DROP TABLE public.jquartz_blob_triggers;
DROP TABLE public.jiraworkflows;
DROP TABLE public.jiraperms;
DROP TABLE public.jiraissue;
DROP TABLE public.jiraeventtype;
DROP TABLE public.jiradraftworkflows;
DROP TABLE public.jiraaction;
DROP TABLE public.issuetypescreenschemeentity;
DROP TABLE public.issuetypescreenscheme;
DROP TABLE public.issuetype;
DROP TABLE public.issuestatus;
DROP TABLE public.issuesecurityscheme;
DROP TABLE public.issuelinktype;
DROP TABLE public.issuelink;
DROP TABLE public.groupbase;
DROP TABLE public.globalpermissionentry;
DROP TABLE public.genericconfiguration;
DROP TABLE public.gadgetuserpreference;
DROP TABLE public.filtersubscription;
DROP TABLE public.fileattachment;
DROP TABLE public.fieldscreentab;
DROP TABLE public.fieldscreenschemeitem;
DROP TABLE public.fieldscreenscheme;
DROP TABLE public.fieldscreenlayoutitem;
DROP TABLE public.fieldscreen;
DROP TABLE public.fieldlayoutschemeentity;
DROP TABLE public.fieldlayoutschemeassociation;
DROP TABLE public.fieldlayoutscheme;
DROP TABLE public.fieldlayoutitem;
DROP TABLE public.fieldlayout;
DROP TABLE public.fieldconfiguration;
DROP TABLE public.fieldconfigschemeissuetype;
DROP TABLE public.fieldconfigscheme;
DROP TABLE public.feature;
DROP TABLE public.favouriteassociations;
DROP TABLE public.externalgadget;
DROP TABLE public.external_entities;
DROP TABLE public.entity_property_index_document;
DROP TABLE public.entity_property;
DROP TABLE public.draftworkflowschemeentity;
DROP TABLE public.draftworkflowscheme;
DROP TABLE public.cwd_user_attributes;
DROP TABLE public.cwd_user;
DROP TABLE public.cwd_membership;
DROP TABLE public.cwd_group_attributes;
DROP TABLE public.cwd_group;
DROP TABLE public.cwd_directory_operation;
DROP TABLE public.cwd_directory_attribute;
DROP TABLE public.cwd_directory;
DROP TABLE public.cwd_application_address;
DROP TABLE public.cwd_application;
DROP TABLE public.customfieldvalue;
DROP TABLE public.customfieldoption;
DROP TABLE public.customfield;
DROP TABLE public.configurationcontext;
DROP TABLE public.component;
DROP TABLE public.columnlayoutitem;
DROP TABLE public.columnlayout;
DROP TABLE public.clusternode;
DROP TABLE public.clustermessage;
DROP TABLE public.clusterlockstatus;
DROP TABLE public.changeitem;
DROP TABLE public.changegroup;
DROP TABLE public.avatar;
DROP TABLE public.audit_log;
DROP TABLE public.audit_item;
DROP TABLE public.audit_changed_value;
DROP TABLE public.app_user;
DROP SEQUENCE public."AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq";
DROP TABLE public."AO_E8B6CC_SYNC_AUDIT_LOG";
DROP SEQUENCE public."AO_E8B6CC_REPO_TO_CHANGESET_ID_seq";
DROP TABLE public."AO_E8B6CC_REPO_TO_CHANGESET";
DROP SEQUENCE public."AO_E8B6CC_REPOSITORY_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_REPOSITORY_MAPPING";
DROP SEQUENCE public."AO_E8B6CC_PULL_REQUEST_ID_seq";
DROP TABLE public."AO_E8B6CC_PULL_REQUEST";
DROP SEQUENCE public."AO_E8B6CC_PR_TO_COMMIT_ID_seq";
DROP TABLE public."AO_E8B6CC_PR_TO_COMMIT";
DROP SEQUENCE public."AO_E8B6CC_PR_PARTICIPANT_ID_seq";
DROP TABLE public."AO_E8B6CC_PR_PARTICIPANT";
DROP SEQUENCE public."AO_E8B6CC_PR_ISSUE_KEY_ID_seq";
DROP TABLE public."AO_E8B6CC_PR_ISSUE_KEY";
DROP SEQUENCE public."AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq";
DROP TABLE public."AO_E8B6CC_PROJECT_MAPPING_V2";
DROP SEQUENCE public."AO_E8B6CC_PROJECT_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_PROJECT_MAPPING";
DROP SEQUENCE public."AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_ORGANIZATION_MAPPING";
DROP SEQUENCE public."AO_E8B6CC_MESSAGE_TAG_ID_seq";
DROP TABLE public."AO_E8B6CC_MESSAGE_TAG";
DROP SEQUENCE public."AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq";
DROP TABLE public."AO_E8B6CC_MESSAGE_QUEUE_ITEM";
DROP SEQUENCE public."AO_E8B6CC_MESSAGE_ID_seq";
DROP TABLE public."AO_E8B6CC_MESSAGE";
DROP SEQUENCE public."AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq";
DROP TABLE public."AO_E8B6CC_ISSUE_TO_CHANGESET";
DROP SEQUENCE public."AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq";
DROP TABLE public."AO_E8B6CC_ISSUE_TO_BRANCH";
DROP SEQUENCE public."AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq";
DROP TABLE public."AO_E8B6CC_ISSUE_MAPPING_V2";
DROP SEQUENCE public."AO_E8B6CC_ISSUE_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_ISSUE_MAPPING";
DROP SEQUENCE public."AO_E8B6CC_GIT_HUB_EVENT_ID_seq";
DROP TABLE public."AO_E8B6CC_GIT_HUB_EVENT";
DROP SEQUENCE public."AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq";
DROP TABLE public."AO_E8B6CC_COMMIT_ISSUE_KEY";
DROP SEQUENCE public."AO_E8B6CC_COMMIT_ID_seq";
DROP TABLE public."AO_E8B6CC_COMMIT";
DROP SEQUENCE public."AO_E8B6CC_CHANGESET_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_CHANGESET_MAPPING";
DROP SEQUENCE public."AO_E8B6CC_BRANCH_ID_seq";
DROP SEQUENCE public."AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq";
DROP TABLE public."AO_E8B6CC_BRANCH_HEAD_MAPPING";
DROP TABLE public."AO_E8B6CC_BRANCH";
DROP SEQUENCE public."AO_B9A0F0_APPLIED_TEMPLATE_ID_seq";
DROP TABLE public."AO_B9A0F0_APPLIED_TEMPLATE";
DROP SEQUENCE public."AO_563AEE_TARGET_ENTITY_ID_seq";
DROP TABLE public."AO_563AEE_TARGET_ENTITY";
DROP SEQUENCE public."AO_563AEE_OBJECT_ENTITY_ID_seq";
DROP TABLE public."AO_563AEE_OBJECT_ENTITY";
DROP SEQUENCE public."AO_563AEE_MEDIA_LINK_ENTITY_ID_seq";
DROP TABLE public."AO_563AEE_MEDIA_LINK_ENTITY";
DROP SEQUENCE public."AO_563AEE_ACTOR_ENTITY_ID_seq";
DROP TABLE public."AO_563AEE_ACTOR_ENTITY";
DROP SEQUENCE public."AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq";
DROP TABLE public."AO_563AEE_ACTIVITY_ENTITY";
DROP SEQUENCE public."AO_4AEACD_WEBHOOK_DAO_ID_seq";
DROP TABLE public."AO_4AEACD_WEBHOOK_DAO";
DROP SEQUENCE public."AO_21D670_WHITELIST_RULES_ID_seq";
DROP TABLE public."AO_21D670_WHITELIST_RULES";
DROP EXTENSION plpgsql;
DROP SCHEMA public;
--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: AO_21D670_WHITELIST_RULES; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_21D670_WHITELIST_RULES" (
    "ALLOWINBOUND" boolean,
    "EXPRESSION" text NOT NULL,
    "ID" integer NOT NULL,
    "TYPE" character varying(255) NOT NULL
);


--
-- Name: AO_21D670_WHITELIST_RULES_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_21D670_WHITELIST_RULES_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_21D670_WHITELIST_RULES_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_21D670_WHITELIST_RULES_ID_seq" OWNED BY "AO_21D670_WHITELIST_RULES"."ID";


--
-- Name: AO_4AEACD_WEBHOOK_DAO; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_4AEACD_WEBHOOK_DAO" (
    "ENABLED" boolean,
    "ENCODED_EVENTS" text,
    "FILTER" text,
    "ID" integer NOT NULL,
    "JQL" character varying(255),
    "LAST_UPDATED" timestamp without time zone NOT NULL,
    "LAST_UPDATED_USER" character varying(255) NOT NULL,
    "NAME" text NOT NULL,
    "REGISTRATION_METHOD" character varying(255) NOT NULL,
    "URL" text NOT NULL,
    "EXCLUDE_ISSUE_DETAILS" boolean,
    "PARAMETERS" text
);


--
-- Name: AO_4AEACD_WEBHOOK_DAO_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_4AEACD_WEBHOOK_DAO_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_4AEACD_WEBHOOK_DAO_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_4AEACD_WEBHOOK_DAO_ID_seq" OWNED BY "AO_4AEACD_WEBHOOK_DAO"."ID";


--
-- Name: AO_563AEE_ACTIVITY_ENTITY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_563AEE_ACTIVITY_ENTITY" (
    "ACTIVITY_ID" bigint NOT NULL,
    "ACTOR_ID" integer,
    "CONTENT" text,
    "GENERATOR_DISPLAY_NAME" character varying(255),
    "GENERATOR_ID" character varying(767),
    "ICON_ID" integer,
    "ID" character varying(767),
    "ISSUE_KEY" character varying(255),
    "OBJECT_ID" integer,
    "POSTER" character varying(255),
    "PROJECT_KEY" character varying(255),
    "PUBLISHED" timestamp without time zone,
    "TARGET_ID" integer,
    "TITLE" character varying(255),
    "URL" character varying(767),
    "USERNAME" character varying(255),
    "VERB" character varying(767)
);


--
-- Name: AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq" OWNED BY "AO_563AEE_ACTIVITY_ENTITY"."ACTIVITY_ID";


--
-- Name: AO_563AEE_ACTOR_ENTITY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_563AEE_ACTOR_ENTITY" (
    "FULL_NAME" character varying(255),
    "ID" integer NOT NULL,
    "PROFILE_PAGE_URI" character varying(767),
    "PROFILE_PICTURE_URI" character varying(767),
    "USERNAME" character varying(255)
);


--
-- Name: AO_563AEE_ACTOR_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_563AEE_ACTOR_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_563AEE_ACTOR_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_563AEE_ACTOR_ENTITY_ID_seq" OWNED BY "AO_563AEE_ACTOR_ENTITY"."ID";


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_563AEE_MEDIA_LINK_ENTITY" (
    "DURATION" integer,
    "HEIGHT" integer,
    "ID" integer NOT NULL,
    "URL" character varying(767),
    "WIDTH" integer
);


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_563AEE_MEDIA_LINK_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_563AEE_MEDIA_LINK_ENTITY_ID_seq" OWNED BY "AO_563AEE_MEDIA_LINK_ENTITY"."ID";


--
-- Name: AO_563AEE_OBJECT_ENTITY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_563AEE_OBJECT_ENTITY" (
    "CONTENT" character varying(255),
    "DISPLAY_NAME" character varying(255),
    "ID" integer NOT NULL,
    "IMAGE_ID" integer,
    "OBJECT_ID" character varying(767),
    "OBJECT_TYPE" character varying(767),
    "SUMMARY" character varying(255),
    "URL" character varying(767)
);


--
-- Name: AO_563AEE_OBJECT_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_563AEE_OBJECT_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_563AEE_OBJECT_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_563AEE_OBJECT_ENTITY_ID_seq" OWNED BY "AO_563AEE_OBJECT_ENTITY"."ID";


--
-- Name: AO_563AEE_TARGET_ENTITY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_563AEE_TARGET_ENTITY" (
    "CONTENT" character varying(255),
    "DISPLAY_NAME" character varying(255),
    "ID" integer NOT NULL,
    "IMAGE_ID" integer,
    "OBJECT_ID" character varying(767),
    "OBJECT_TYPE" character varying(767),
    "SUMMARY" character varying(255),
    "URL" character varying(767)
);


--
-- Name: AO_563AEE_TARGET_ENTITY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_563AEE_TARGET_ENTITY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_563AEE_TARGET_ENTITY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_563AEE_TARGET_ENTITY_ID_seq" OWNED BY "AO_563AEE_TARGET_ENTITY"."ID";


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_B9A0F0_APPLIED_TEMPLATE" (
    "ID" integer NOT NULL,
    "PROJECT_ID" bigint DEFAULT 0,
    "PROJECT_TEMPLATE_MODULE_KEY" character varying(255),
    "PROJECT_TEMPLATE_WEB_ITEM_KEY" character varying(255)
);


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_B9A0F0_APPLIED_TEMPLATE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_B9A0F0_APPLIED_TEMPLATE_ID_seq" OWNED BY "AO_B9A0F0_APPLIED_TEMPLATE"."ID";


--
-- Name: AO_E8B6CC_BRANCH; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_BRANCH" (
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "REPOSITORY_ID" integer
);


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_BRANCH_HEAD_MAPPING" (
    "BRANCH_NAME" character varying(255),
    "HEAD" character varying(255),
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer
);


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_BRANCH_HEAD_MAPPING"."ID";


--
-- Name: AO_E8B6CC_BRANCH_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_BRANCH_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_BRANCH_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_BRANCH_ID_seq" OWNED BY "AO_E8B6CC_BRANCH"."ID";


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_CHANGESET_MAPPING" (
    "AUTHOR" character varying(255),
    "AUTHOR_EMAIL" character varying(255),
    "BRANCH" character varying(255),
    "DATE" timestamp without time zone,
    "FILES_DATA" text,
    "FILE_DETAILS_JSON" text,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "MESSAGE" text,
    "NODE" character varying(255),
    "PARENTS_DATA" character varying(255),
    "PROJECT_KEY" character varying(255),
    "RAW_AUTHOR" character varying(255),
    "RAW_NODE" character varying(255),
    "REPOSITORY_ID" integer DEFAULT 0,
    "SMARTCOMMIT_AVAILABLE" boolean,
    "VERSION" integer
);


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_CHANGESET_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_CHANGESET_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_CHANGESET_MAPPING"."ID";


--
-- Name: AO_E8B6CC_COMMIT; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_COMMIT" (
    "AUTHOR" character varying(255),
    "AUTHOR_AVATAR_URL" character varying(255),
    "DATE" timestamp without time zone NOT NULL,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "MESSAGE" text,
    "NODE" character varying(255),
    "RAW_AUTHOR" character varying(255)
);


--
-- Name: AO_E8B6CC_COMMIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_COMMIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_COMMIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_COMMIT_ID_seq" OWNED BY "AO_E8B6CC_COMMIT"."ID";


--
-- Name: AO_E8B6CC_COMMIT_ISSUE_KEY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_COMMIT_ISSUE_KEY" (
    "COMMIT_ID" integer,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255)
);


--
-- Name: AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq" OWNED BY "AO_E8B6CC_COMMIT_ISSUE_KEY"."ID";


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_GIT_HUB_EVENT" (
    "CREATED_AT" timestamp without time zone NOT NULL,
    "GIT_HUB_ID" character varying(255) DEFAULT '0'::character varying NOT NULL,
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer NOT NULL,
    "SAVE_POINT" boolean
);


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_GIT_HUB_EVENT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_GIT_HUB_EVENT_ID_seq" OWNED BY "AO_E8B6CC_GIT_HUB_EVENT"."ID";


--
-- Name: AO_E8B6CC_ISSUE_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_ISSUE_MAPPING" (
    "ID" integer NOT NULL,
    "ISSUE_ID" character varying(255),
    "NODE" character varying(255),
    "PROJECT_KEY" character varying(255),
    "REPOSITORY_URI" character varying(255)
);


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_MAPPING"."ID";


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_V2; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_ISSUE_MAPPING_V2" (
    "AUTHOR" character varying(255),
    "BRANCH" character varying(255),
    "DATE" timestamp without time zone,
    "FILES_DATA" text,
    "ID" integer NOT NULL,
    "ISSUE_ID" character varying(255),
    "MESSAGE" text,
    "NODE" character varying(255),
    "PARENTS_DATA" character varying(255),
    "RAW_AUTHOR" character varying(255),
    "RAW_NODE" character varying(255),
    "REPOSITORY_ID" integer DEFAULT 0,
    "VERSION" integer
);


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_MAPPING_V2"."ID";


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_ISSUE_TO_BRANCH" (
    "BRANCH_ID" integer,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255)
);


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_TO_BRANCH"."ID";


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_ISSUE_TO_CHANGESET" (
    "CHANGESET_ID" integer,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "PROJECT_KEY" character varying(255)
);


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq" OWNED BY "AO_E8B6CC_ISSUE_TO_CHANGESET"."ID";


--
-- Name: AO_E8B6CC_MESSAGE; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_MESSAGE" (
    "ADDRESS" character varying(255) NOT NULL,
    "ID" integer NOT NULL,
    "PAYLOAD" text NOT NULL,
    "PAYLOAD_TYPE" character varying(255) NOT NULL,
    "PRIORITY" integer DEFAULT 0 NOT NULL
);


--
-- Name: AO_E8B6CC_MESSAGE_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_MESSAGE_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE"."ID";


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_MESSAGE_QUEUE_ITEM" (
    "ID" integer NOT NULL,
    "LAST_FAILED" timestamp without time zone,
    "MESSAGE_ID" integer NOT NULL,
    "QUEUE" character varying(255) NOT NULL,
    "RETRIES_COUNT" integer DEFAULT 0 NOT NULL,
    "STATE" character varying(255) NOT NULL,
    "STATE_INFO" character varying(255)
);


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"."ID";


--
-- Name: AO_E8B6CC_MESSAGE_TAG; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_MESSAGE_TAG" (
    "ID" integer NOT NULL,
    "MESSAGE_ID" integer,
    "TAG" character varying(255)
);


--
-- Name: AO_E8B6CC_MESSAGE_TAG_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_MESSAGE_TAG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_MESSAGE_TAG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_MESSAGE_TAG_ID_seq" OWNED BY "AO_E8B6CC_MESSAGE_TAG"."ID";


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_ORGANIZATION_MAPPING" (
    "ACCESS_TOKEN" character varying(255),
    "ADMIN_PASSWORD" character varying(255),
    "ADMIN_USERNAME" character varying(255),
    "AUTOLINK_NEW_REPOS" boolean,
    "DEFAULT_GROUPS_SLUGS" character varying(255),
    "DVCS_TYPE" character varying(255),
    "HOST_URL" character varying(255),
    "ID" integer NOT NULL,
    "NAME" character varying(255),
    "OAUTH_KEY" character varying(255),
    "OAUTH_SECRET" character varying(255),
    "SMARTCOMMITS_FOR_NEW_REPOS" boolean
);


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_ORGANIZATION_MAPPING"."ID";


--
-- Name: AO_E8B6CC_PROJECT_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PROJECT_MAPPING" (
    "ID" integer NOT NULL,
    "PASSWORD" character varying(255),
    "PROJECT_KEY" character varying(255),
    "REPOSITORY_URI" character varying(255),
    "USERNAME" character varying(255)
);


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PROJECT_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PROJECT_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_PROJECT_MAPPING"."ID";


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_V2; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PROJECT_MAPPING_V2" (
    "ACCESS_TOKEN" character varying(255),
    "ADMIN_PASSWORD" character varying(255),
    "ADMIN_USERNAME" character varying(255),
    "ID" integer NOT NULL,
    "LAST_COMMIT_DATE" timestamp without time zone,
    "PROJECT_KEY" character varying(255),
    "REPOSITORY_NAME" character varying(255),
    "REPOSITORY_TYPE" character varying(255),
    "REPOSITORY_URL" character varying(255)
);


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq" OWNED BY "AO_E8B6CC_PROJECT_MAPPING_V2"."ID";


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PR_ISSUE_KEY" (
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "ISSUE_KEY" character varying(255),
    "PULL_REQUEST_ID" integer DEFAULT 0
);


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PR_ISSUE_KEY_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PR_ISSUE_KEY_ID_seq" OWNED BY "AO_E8B6CC_PR_ISSUE_KEY"."ID";


--
-- Name: AO_E8B6CC_PR_PARTICIPANT; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PR_PARTICIPANT" (
    "APPROVED" boolean,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "PULL_REQUEST_ID" integer,
    "ROLE" character varying(255),
    "USERNAME" character varying(255)
);


--
-- Name: AO_E8B6CC_PR_PARTICIPANT_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PR_PARTICIPANT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PR_PARTICIPANT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PR_PARTICIPANT_ID_seq" OWNED BY "AO_E8B6CC_PR_PARTICIPANT"."ID";


--
-- Name: AO_E8B6CC_PR_TO_COMMIT; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PR_TO_COMMIT" (
    "COMMIT_ID" integer NOT NULL,
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "REQUEST_ID" integer NOT NULL
);


--
-- Name: AO_E8B6CC_PR_TO_COMMIT_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PR_TO_COMMIT_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PR_TO_COMMIT_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PR_TO_COMMIT_ID_seq" OWNED BY "AO_E8B6CC_PR_TO_COMMIT"."ID";


--
-- Name: AO_E8B6CC_PULL_REQUEST; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_PULL_REQUEST" (
    "AUTHOR" character varying(255),
    "COMMENT_COUNT" integer DEFAULT 0,
    "CREATED_ON" timestamp without time zone,
    "DESTINATION_BRANCH" character varying(255),
    "DOMAIN_ID" integer DEFAULT 0 NOT NULL,
    "ID" integer NOT NULL,
    "LAST_STATUS" character varying(255),
    "NAME" character varying(255),
    "REMOTE_ID" bigint,
    "SOURCE_BRANCH" character varying(255),
    "SOURCE_REPO" character varying(255),
    "TO_REPOSITORY_ID" integer DEFAULT 0,
    "TO_REPO_ID" integer DEFAULT 0,
    "UPDATED_ON" timestamp without time zone,
    "URL" character varying(255)
);


--
-- Name: AO_E8B6CC_PULL_REQUEST_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_PULL_REQUEST_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_PULL_REQUEST_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_PULL_REQUEST_ID_seq" OWNED BY "AO_E8B6CC_PULL_REQUEST"."ID";


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_REPOSITORY_MAPPING" (
    "ACTIVITY_LAST_SYNC" timestamp without time zone,
    "DELETED" boolean,
    "FORK" boolean,
    "FORK_OF_NAME" character varying(255),
    "FORK_OF_OWNER" character varying(255),
    "FORK_OF_SLUG" character varying(255),
    "ID" integer NOT NULL,
    "LAST_CHANGESET_NODE" character varying(255),
    "LAST_COMMIT_DATE" timestamp without time zone,
    "LINKED" boolean,
    "LOGO" text,
    "NAME" character varying(255),
    "ORGANIZATION_ID" integer DEFAULT 0,
    "SLUG" character varying(255),
    "SMARTCOMMITS_ENABLED" boolean
);


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_REPOSITORY_MAPPING_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_REPOSITORY_MAPPING_ID_seq" OWNED BY "AO_E8B6CC_REPOSITORY_MAPPING"."ID";


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_REPO_TO_CHANGESET" (
    "CHANGESET_ID" integer,
    "ID" integer NOT NULL,
    "REPOSITORY_ID" integer
);


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_REPO_TO_CHANGESET_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_REPO_TO_CHANGESET_ID_seq" OWNED BY "AO_E8B6CC_REPO_TO_CHANGESET"."ID";


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE "AO_E8B6CC_SYNC_AUDIT_LOG" (
    "END_DATE" timestamp without time zone,
    "EXC_TRACE" text,
    "FIRST_REQUEST_DATE" timestamp without time zone,
    "FLIGHT_TIME_MS" integer DEFAULT 0,
    "ID" integer NOT NULL,
    "NUM_REQUESTS" integer DEFAULT 0,
    "REPO_ID" integer DEFAULT 0,
    "START_DATE" timestamp without time zone,
    "SYNC_STATUS" character varying(255),
    "SYNC_TYPE" character varying(255),
    "TOTAL_ERRORS" integer DEFAULT 0
);


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE "AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE "AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq" OWNED BY "AO_E8B6CC_SYNC_AUDIT_LOG"."ID";


--
-- Name: app_user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE app_user (
    id numeric(18,0) NOT NULL,
    user_key character varying(255),
    lower_user_name character varying(255)
);


--
-- Name: audit_changed_value; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE audit_changed_value (
    id numeric(18,0) NOT NULL,
    log_id numeric(18,0),
    name character varying(255),
    delta_from character varying(255),
    delta_to character varying(255)
);


--
-- Name: audit_item; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE audit_item (
    id numeric(18,0) NOT NULL,
    log_id numeric(18,0),
    object_type character varying(60),
    object_id character varying(255),
    object_name character varying(255),
    object_parent_id character varying(60),
    object_parent_name character varying(60)
);


--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE audit_log (
    id numeric(18,0) NOT NULL,
    remote_address character varying(60),
    created timestamp with time zone,
    author_key character varying(255),
    summary character varying(255),
    category character varying(255),
    object_type character varying(60),
    object_id character varying(255),
    object_name character varying(255),
    object_parent_id character varying(60),
    object_parent_name character varying(60),
    author_type numeric(9,0),
    search_field text
);


--
-- Name: avatar; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE avatar (
    id numeric(18,0) NOT NULL,
    filename character varying(255),
    contenttype character varying(255),
    avatartype character varying(60),
    owner character varying(255),
    systemavatar numeric(9,0)
);


--
-- Name: changegroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE changegroup (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    author character varying(255),
    created timestamp with time zone
);


--
-- Name: changeitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE changeitem (
    id numeric(18,0) NOT NULL,
    groupid numeric(18,0),
    fieldtype character varying(255),
    field character varying(255),
    oldvalue text,
    oldstring text,
    newvalue text,
    newstring text
);


--
-- Name: clusterlockstatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE clusterlockstatus (
    id numeric(18,0) NOT NULL,
    lock_name character varying(255),
    locked_by_node character varying(60)
);


--
-- Name: clustermessage; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE clustermessage (
    id numeric(18,0) NOT NULL,
    source_node character varying(60),
    destination_node character varying(60),
    claimed_by_node character varying(60),
    message character varying(255),
    message_time timestamp with time zone
);


--
-- Name: clusternode; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE clusternode (
    node_id character varying(60) NOT NULL,
    node_state character varying(60)
);


--
-- Name: columnlayout; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE columnlayout (
    id numeric(18,0) NOT NULL,
    username character varying(255),
    searchrequest numeric(18,0)
);


--
-- Name: columnlayoutitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE columnlayoutitem (
    id numeric(18,0) NOT NULL,
    columnlayout numeric(18,0),
    fieldidentifier character varying(255),
    horizontalposition numeric(18,0)
);


--
-- Name: component; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE component (
    id numeric(18,0) NOT NULL,
    project numeric(18,0),
    cname character varying(255),
    description text,
    url character varying(255),
    lead character varying(255),
    assigneetype numeric(18,0)
);


--
-- Name: configurationcontext; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE configurationcontext (
    id numeric(18,0) NOT NULL,
    projectcategory numeric(18,0),
    project numeric(18,0),
    customfield character varying(255),
    fieldconfigscheme numeric(18,0)
);


--
-- Name: customfield; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE customfield (
    id numeric(18,0) NOT NULL,
    customfieldtypekey character varying(255),
    customfieldsearcherkey character varying(255),
    cfname character varying(255),
    description text,
    defaultvalue character varying(255),
    fieldtype numeric(18,0),
    project numeric(18,0),
    issuetype character varying(255)
);


--
-- Name: customfieldoption; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE customfieldoption (
    id numeric(18,0) NOT NULL,
    customfield numeric(18,0),
    customfieldconfig numeric(18,0),
    parentoptionid numeric(18,0),
    sequence numeric(18,0),
    customvalue character varying(255),
    optiontype character varying(60),
    disabled character varying(60)
);


--
-- Name: customfieldvalue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE customfieldvalue (
    id numeric(18,0) NOT NULL,
    issue numeric(18,0),
    customfield numeric(18,0),
    parentkey character varying(255),
    stringvalue character varying(255),
    numbervalue double precision,
    textvalue text,
    datevalue timestamp with time zone,
    valuetype character varying(255)
);


--
-- Name: cwd_application; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_application (
    id numeric(18,0) NOT NULL,
    application_name character varying(255),
    lower_application_name character varying(255),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    active numeric(9,0),
    description character varying(255),
    application_type character varying(255),
    credential character varying(255)
);


--
-- Name: cwd_application_address; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_application_address (
    application_id numeric(18,0) NOT NULL,
    remote_address character varying(255) NOT NULL,
    encoded_address_binary character varying(255),
    remote_address_mask numeric(9,0)
);


--
-- Name: cwd_directory; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_directory (
    id numeric(18,0) NOT NULL,
    directory_name character varying(255),
    lower_directory_name character varying(255),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    active numeric(9,0),
    description character varying(255),
    impl_class character varying(255),
    lower_impl_class character varying(255),
    directory_type character varying(60),
    directory_position numeric(18,0)
);


--
-- Name: cwd_directory_attribute; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_directory_attribute (
    directory_id numeric(18,0) NOT NULL,
    attribute_name character varying(255) NOT NULL,
    attribute_value character varying(255)
);


--
-- Name: cwd_directory_operation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_directory_operation (
    directory_id numeric(18,0) NOT NULL,
    operation_type character varying(60) NOT NULL
);


--
-- Name: cwd_group; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_group (
    id numeric(18,0) NOT NULL,
    group_name character varying(255),
    lower_group_name character varying(255),
    active numeric(9,0),
    local numeric(9,0),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    description character varying(255),
    lower_description character varying(255),
    group_type character varying(60),
    directory_id numeric(18,0)
);


--
-- Name: cwd_group_attributes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_group_attributes (
    id numeric(18,0) NOT NULL,
    group_id numeric(18,0),
    directory_id numeric(18,0),
    attribute_name character varying(255),
    attribute_value character varying(255),
    lower_attribute_value character varying(255)
);


--
-- Name: cwd_membership; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_membership (
    id numeric(18,0) NOT NULL,
    parent_id numeric(18,0),
    child_id numeric(18,0),
    membership_type character varying(60),
    group_type character varying(60),
    parent_name character varying(255),
    lower_parent_name character varying(255),
    child_name character varying(255),
    lower_child_name character varying(255),
    directory_id numeric(18,0)
);


--
-- Name: cwd_user; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_user (
    id numeric(18,0) NOT NULL,
    directory_id numeric(18,0),
    user_name character varying(255),
    lower_user_name character varying(255),
    active numeric(9,0),
    created_date timestamp with time zone,
    updated_date timestamp with time zone,
    first_name character varying(255),
    lower_first_name character varying(255),
    last_name character varying(255),
    lower_last_name character varying(255),
    display_name character varying(255),
    lower_display_name character varying(255),
    email_address character varying(255),
    lower_email_address character varying(255),
    credential character varying(255),
    deleted_externally numeric(9,0),
    external_id character varying(255)
);


--
-- Name: cwd_user_attributes; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE cwd_user_attributes (
    id numeric(18,0) NOT NULL,
    user_id numeric(18,0),
    directory_id numeric(18,0),
    attribute_name character varying(255),
    attribute_value character varying(255),
    lower_attribute_value character varying(255)
);


--
-- Name: draftworkflowscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE draftworkflowscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text,
    workflow_scheme_id numeric(18,0),
    last_modified_date timestamp with time zone,
    last_modified_user character varying(255)
);


--
-- Name: draftworkflowschemeentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE draftworkflowschemeentity (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    workflow character varying(255),
    issuetype character varying(255)
);


--
-- Name: entity_property; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entity_property (
    id numeric(18,0) NOT NULL,
    entity_name character varying(255),
    entity_id numeric(18,0),
    property_key character varying(255),
    created timestamp with time zone,
    updated timestamp with time zone,
    json_value text
);


--
-- Name: entity_property_index_document; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE entity_property_index_document (
    id numeric(18,0) NOT NULL,
    plugin_key character varying(255),
    module_key character varying(255),
    entity_key character varying(255),
    updated timestamp with time zone,
    document text
);


--
-- Name: external_entities; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE external_entities (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    entitytype character varying(255)
);


--
-- Name: externalgadget; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE externalgadget (
    id numeric(18,0) NOT NULL,
    gadget_xml text
);


--
-- Name: favouriteassociations; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE favouriteassociations (
    id numeric(18,0) NOT NULL,
    username character varying(255),
    entitytype character varying(60),
    entityid numeric(18,0),
    sequence numeric(18,0)
);


--
-- Name: feature; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE feature (
    id numeric(18,0) NOT NULL,
    feature_name character varying(255),
    feature_type character varying(10),
    user_key character varying(255)
);


--
-- Name: fieldconfigscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldconfigscheme (
    id numeric(18,0) NOT NULL,
    configname character varying(255),
    description text,
    fieldid character varying(60),
    customfield numeric(18,0)
);


--
-- Name: fieldconfigschemeissuetype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldconfigschemeissuetype (
    id numeric(18,0) NOT NULL,
    issuetype character varying(255),
    fieldconfigscheme numeric(18,0),
    fieldconfiguration numeric(18,0)
);


--
-- Name: fieldconfiguration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldconfiguration (
    id numeric(18,0) NOT NULL,
    configname character varying(255),
    description text,
    fieldid character varying(60),
    customfield numeric(18,0)
);


--
-- Name: fieldlayout; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldlayout (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description character varying(255),
    layout_type character varying(255),
    layoutscheme numeric(18,0)
);


--
-- Name: fieldlayoutitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldlayoutitem (
    id numeric(18,0) NOT NULL,
    fieldlayout numeric(18,0),
    fieldidentifier character varying(255),
    description text,
    verticalposition numeric(18,0),
    ishidden character varying(60),
    isrequired character varying(60),
    renderertype character varying(255)
);


--
-- Name: fieldlayoutscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldlayoutscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text
);


--
-- Name: fieldlayoutschemeassociation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldlayoutschemeassociation (
    id numeric(18,0) NOT NULL,
    issuetype character varying(255),
    project numeric(18,0),
    fieldlayoutscheme numeric(18,0)
);


--
-- Name: fieldlayoutschemeentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldlayoutschemeentity (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    issuetype character varying(255),
    fieldlayout numeric(18,0)
);


--
-- Name: fieldscreen; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldscreen (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description character varying(255)
);


--
-- Name: fieldscreenlayoutitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldscreenlayoutitem (
    id numeric(18,0) NOT NULL,
    fieldidentifier character varying(255),
    sequence numeric(18,0),
    fieldscreentab numeric(18,0)
);


--
-- Name: fieldscreenscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldscreenscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description character varying(255)
);


--
-- Name: fieldscreenschemeitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldscreenschemeitem (
    id numeric(18,0) NOT NULL,
    operation numeric(18,0),
    fieldscreen numeric(18,0),
    fieldscreenscheme numeric(18,0)
);


--
-- Name: fieldscreentab; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fieldscreentab (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description character varying(255),
    sequence numeric(18,0),
    fieldscreen numeric(18,0)
);


--
-- Name: fileattachment; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE fileattachment (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    mimetype character varying(255),
    filename character varying(255),
    created timestamp with time zone,
    filesize numeric(18,0),
    author character varying(255),
    zip numeric(9,0),
    thumbnailable numeric(9,0)
);


--
-- Name: filtersubscription; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE filtersubscription (
    id numeric(18,0) NOT NULL,
    filter_i_d numeric(18,0),
    username character varying(60),
    groupname character varying(60),
    last_run timestamp with time zone,
    email_on_empty character varying(10)
);


--
-- Name: gadgetuserpreference; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE gadgetuserpreference (
    id numeric(18,0) NOT NULL,
    portletconfiguration numeric(18,0),
    userprefkey character varying(255),
    userprefvalue text
);


--
-- Name: genericconfiguration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE genericconfiguration (
    id numeric(18,0) NOT NULL,
    datatype character varying(60),
    datakey character varying(60),
    xmlvalue text
);


--
-- Name: globalpermissionentry; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE globalpermissionentry (
    id numeric(18,0) NOT NULL,
    permission character varying(255),
    group_id character varying(255)
);


--
-- Name: groupbase; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE groupbase (
    id numeric(18,0) NOT NULL,
    groupname character varying(255)
);


--
-- Name: issuelink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuelink (
    id numeric(18,0) NOT NULL,
    linktype numeric(18,0),
    source numeric(18,0),
    destination numeric(18,0),
    sequence numeric(18,0)
);


--
-- Name: issuelinktype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuelinktype (
    id numeric(18,0) NOT NULL,
    linkname character varying(255),
    inward character varying(255),
    outward character varying(255),
    pstyle character varying(60)
);


--
-- Name: issuesecurityscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuesecurityscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text,
    defaultlevel numeric(18,0)
);


--
-- Name: issuestatus; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuestatus (
    id character varying(60) NOT NULL,
    sequence numeric(18,0),
    pname character varying(60),
    description text,
    iconurl character varying(255),
    statuscategory numeric(18,0)
);


--
-- Name: issuetype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuetype (
    id character varying(60) NOT NULL,
    sequence numeric(18,0),
    pname character varying(60),
    pstyle character varying(60),
    description text,
    iconurl character varying(255)
);


--
-- Name: issuetypescreenscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuetypescreenscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description character varying(255)
);


--
-- Name: issuetypescreenschemeentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE issuetypescreenschemeentity (
    id numeric(18,0) NOT NULL,
    issuetype character varying(255),
    scheme numeric(18,0),
    fieldscreenscheme numeric(18,0)
);


--
-- Name: jiraaction; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiraaction (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    author character varying(255),
    actiontype character varying(255),
    actionlevel character varying(255),
    rolelevel numeric(18,0),
    actionbody text,
    created timestamp with time zone,
    updateauthor character varying(255),
    updated timestamp with time zone,
    actionnum numeric(18,0)
);


--
-- Name: jiradraftworkflows; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiradraftworkflows (
    id numeric(18,0) NOT NULL,
    parentname character varying(255),
    descriptor text
);


--
-- Name: jiraeventtype; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiraeventtype (
    id numeric(18,0) NOT NULL,
    template_id numeric(18,0),
    name character varying(255),
    description text,
    event_type character varying(60)
);


--
-- Name: jiraissue; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiraissue (
    id numeric(18,0) NOT NULL,
    pkey character varying(255),
    issuenum numeric(18,0),
    project numeric(18,0),
    reporter character varying(255),
    assignee character varying(255),
    creator character varying(255),
    issuetype character varying(255),
    summary character varying(255),
    description text,
    environment text,
    priority character varying(255),
    resolution character varying(255),
    issuestatus character varying(255),
    created timestamp with time zone,
    updated timestamp with time zone,
    duedate timestamp with time zone,
    resolutiondate timestamp with time zone,
    votes numeric(18,0),
    watches numeric(18,0),
    timeoriginalestimate numeric(18,0),
    timeestimate numeric(18,0),
    timespent numeric(18,0),
    workflow_id numeric(18,0),
    security numeric(18,0),
    fixfor numeric(18,0),
    component numeric(18,0)
);


--
-- Name: jiraperms; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiraperms (
    id numeric(18,0) NOT NULL,
    permtype numeric(18,0),
    projectid numeric(18,0),
    groupname character varying(255)
);


--
-- Name: jiraworkflows; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jiraworkflows (
    id numeric(18,0) NOT NULL,
    workflowname character varying(255),
    creatorname character varying(255),
    descriptor text,
    islocked character varying(60)
);


--
-- Name: jquartz_blob_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_blob_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


--
-- Name: jquartz_calendars; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_calendars (
    sched_name character varying(120),
    calendar_name character varying(200) NOT NULL,
    calendar bytea
);


--
-- Name: jquartz_cron_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_cron_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120),
    time_zone_id character varying(80)
);


--
-- Name: jquartz_fired_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_fired_triggers (
    sched_name character varying(120),
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200),
    trigger_group character varying(200),
    is_volatile boolean,
    instance_name character varying(200),
    fired_time numeric(18,0),
    sched_time numeric(18,0),
    priority numeric(9,0),
    state character varying(16),
    job_name character varying(200),
    job_group character varying(200),
    is_stateful boolean,
    is_nonconcurrent boolean,
    is_update_data boolean,
    requests_recovery boolean
);


--
-- Name: jquartz_job_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_job_details (
    sched_name character varying(120),
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250),
    is_durable boolean,
    is_volatile boolean,
    is_stateful boolean,
    is_nonconcurrent boolean,
    is_update_data boolean,
    requests_recovery boolean,
    job_data bytea
);


--
-- Name: jquartz_job_listeners; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_job_listeners (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    job_listener character varying(200) NOT NULL
);


--
-- Name: jquartz_locks; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_locks (
    sched_name character varying(120),
    lock_name character varying(40) NOT NULL
);


--
-- Name: jquartz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_paused_trigger_grps (
    sched_name character varying(120),
    trigger_group character varying(200) NOT NULL
);


--
-- Name: jquartz_scheduler_state; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_scheduler_state (
    sched_name character varying(120),
    instance_name character varying(200) NOT NULL,
    last_checkin_time numeric(18,0),
    checkin_interval numeric(18,0)
);


--
-- Name: jquartz_simple_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_simple_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count numeric(18,0),
    repeat_interval numeric(18,0),
    times_triggered numeric(18,0)
);


--
-- Name: jquartz_simprop_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_simprop_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    str_prop_1 character varying(512),
    str_prop_2 character varying(512),
    str_prop_3 character varying(512),
    int_prop_1 numeric(9,0),
    int_prop_2 numeric(9,0),
    long_prop_1 numeric(18,0),
    long_prop_2 numeric(18,0),
    dec_prop_1 numeric(13,4),
    dec_prop_2 numeric(13,4),
    bool_prop_1 boolean,
    bool_prop_2 boolean
);


--
-- Name: jquartz_trigger_listeners; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_trigger_listeners (
    trigger_name character varying(200),
    trigger_group character varying(200) NOT NULL,
    trigger_listener character varying(200) NOT NULL
);


--
-- Name: jquartz_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE jquartz_triggers (
    sched_name character varying(120),
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_volatile boolean,
    description character varying(250),
    next_fire_time numeric(18,0),
    prev_fire_time numeric(18,0),
    priority numeric(9,0),
    trigger_state character varying(16),
    trigger_type character varying(8),
    start_time numeric(18,0),
    end_time numeric(18,0),
    calendar_name character varying(200),
    misfire_instr numeric(4,0),
    job_data bytea
);


--
-- Name: label; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE label (
    id numeric(18,0) NOT NULL,
    fieldid numeric(18,0),
    issue numeric(18,0),
    label character varying(255)
);


--
-- Name: listenerconfig; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE listenerconfig (
    id numeric(18,0) NOT NULL,
    clazz character varying(255),
    listenername character varying(255)
);


--
-- Name: mailserver; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE mailserver (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text,
    mailfrom character varying(255),
    prefix character varying(60),
    smtp_port character varying(60),
    protocol character varying(60),
    server_type character varying(60),
    servername character varying(255),
    jndilocation character varying(255),
    mailusername character varying(255),
    mailpassword character varying(255),
    istlsrequired character varying(60),
    timeout numeric(18,0),
    socks_port character varying(60),
    socks_host character varying(60)
);


--
-- Name: managedconfigurationitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE managedconfigurationitem (
    id numeric(18,0) NOT NULL,
    item_id character varying(255),
    item_type character varying(255),
    managed character varying(10),
    access_level character varying(255),
    source character varying(255),
    description_key character varying(255)
);


--
-- Name: membershipbase; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE membershipbase (
    id numeric(18,0) NOT NULL,
    user_name character varying(255),
    group_name character varying(255)
);


--
-- Name: moved_issue_key; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE moved_issue_key (
    id numeric(18,0) NOT NULL,
    old_issue_key character varying(255),
    issue_id numeric(18,0)
);


--
-- Name: nodeassociation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE nodeassociation (
    source_node_id numeric(18,0) NOT NULL,
    source_node_entity character varying(60) NOT NULL,
    sink_node_id numeric(18,0) NOT NULL,
    sink_node_entity character varying(60) NOT NULL,
    association_type character varying(60) NOT NULL,
    sequence numeric(9,0)
);


--
-- Name: nodeindexcounter; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE nodeindexcounter (
    id numeric(18,0) NOT NULL,
    node_id character varying(60),
    sending_node_id character varying(60),
    index_operation_id numeric(18,0)
);


--
-- Name: notification; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE notification (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    event character varying(60),
    event_type_id numeric(18,0),
    template_id numeric(18,0),
    notif_type character varying(60),
    notif_parameter character varying(60)
);


--
-- Name: notificationinstance; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE notificationinstance (
    id numeric(18,0) NOT NULL,
    notificationtype character varying(60),
    source numeric(18,0),
    emailaddress character varying(255),
    messageid character varying(255)
);


--
-- Name: notificationscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE notificationscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text
);


--
-- Name: oauthconsumer; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauthconsumer (
    id numeric(18,0) NOT NULL,
    created timestamp with time zone,
    consumername character varying(255),
    consumer_key character varying(255),
    consumerservice character varying(255),
    public_key text,
    private_key text,
    description text,
    callback text,
    signature_method character varying(60),
    shared_secret text
);


--
-- Name: oauthconsumertoken; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauthconsumertoken (
    id numeric(18,0) NOT NULL,
    created timestamp with time zone,
    token_key character varying(255),
    token character varying(255),
    token_secret character varying(255),
    token_type character varying(60),
    consumer_key character varying(255)
);


--
-- Name: oauthspconsumer; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauthspconsumer (
    id numeric(18,0) NOT NULL,
    created timestamp with time zone,
    consumer_key character varying(255),
    consumername character varying(255),
    public_key text,
    description text,
    callback text,
    two_l_o_allowed character varying(60),
    executing_two_l_o_user character varying(255),
    two_l_o_impersonation_allowed character varying(60),
    three_l_o_allowed character varying(60)
);


--
-- Name: oauthsptoken; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE oauthsptoken (
    id numeric(18,0) NOT NULL,
    created timestamp with time zone,
    token character varying(255),
    token_secret character varying(255),
    token_type character varying(60),
    consumer_key character varying(255),
    username character varying(255),
    ttl numeric(18,0),
    spauth character varying(60),
    callback text,
    spverifier character varying(255),
    spversion character varying(60),
    session_handle character varying(255),
    session_creation_time timestamp with time zone,
    session_last_renewal_time timestamp with time zone,
    session_time_to_live timestamp with time zone
);


--
-- Name: optionconfiguration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE optionconfiguration (
    id numeric(18,0) NOT NULL,
    fieldid character varying(60),
    optionid character varying(60),
    fieldconfig numeric(18,0),
    sequence numeric(18,0)
);


--
-- Name: os_currentstep; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE os_currentstep (
    id numeric(18,0) NOT NULL,
    entry_id numeric(18,0),
    step_id numeric(9,0),
    action_id numeric(9,0),
    owner character varying(60),
    start_date timestamp with time zone,
    due_date timestamp with time zone,
    finish_date timestamp with time zone,
    status character varying(60),
    caller character varying(60)
);


--
-- Name: os_currentstep_prev; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE os_currentstep_prev (
    id numeric(18,0) NOT NULL,
    previous_id numeric(18,0) NOT NULL
);


--
-- Name: os_historystep; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE os_historystep (
    id numeric(18,0) NOT NULL,
    entry_id numeric(18,0),
    step_id numeric(9,0),
    action_id numeric(9,0),
    owner character varying(60),
    start_date timestamp with time zone,
    due_date timestamp with time zone,
    finish_date timestamp with time zone,
    status character varying(60),
    caller character varying(60)
);


--
-- Name: os_historystep_prev; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE os_historystep_prev (
    id numeric(18,0) NOT NULL,
    previous_id numeric(18,0) NOT NULL
);


--
-- Name: os_wfentry; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE os_wfentry (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    initialized numeric(9,0),
    state numeric(9,0)
);


--
-- Name: permissionscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE permissionscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text
);


--
-- Name: pluginstate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE pluginstate (
    pluginkey character varying(255) NOT NULL,
    pluginenabled character varying(60)
);


--
-- Name: pluginversion; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE pluginversion (
    id numeric(18,0) NOT NULL,
    pluginname character varying(255),
    pluginkey character varying(255),
    pluginversion character varying(255),
    created timestamp with time zone
);


--
-- Name: portalpage; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE portalpage (
    id numeric(18,0) NOT NULL,
    username character varying(255),
    pagename character varying(255),
    description character varying(255),
    sequence numeric(18,0),
    fav_count numeric(18,0),
    layout character varying(255),
    ppversion numeric(18,0)
);


--
-- Name: portletconfiguration; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE portletconfiguration (
    id numeric(18,0) NOT NULL,
    portalpage numeric(18,0),
    portlet_id character varying(255),
    column_number numeric(9,0),
    positionseq numeric(9,0),
    gadget_xml text,
    color character varying(255)
);


--
-- Name: priority; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE priority (
    id character varying(60) NOT NULL,
    sequence numeric(18,0),
    pname character varying(60),
    description text,
    iconurl character varying(255),
    status_color character varying(60)
);


--
-- Name: project; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE project (
    id numeric(18,0) NOT NULL,
    pname character varying(255),
    url character varying(255),
    lead character varying(255),
    description text,
    pkey character varying(255),
    pcounter numeric(18,0),
    assigneetype numeric(18,0),
    avatar numeric(18,0),
    originalkey character varying(255)
);


--
-- Name: project_key; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE project_key (
    id numeric(18,0) NOT NULL,
    project_id numeric(18,0),
    project_key character varying(255)
);


--
-- Name: projectcategory; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectcategory (
    id numeric(18,0) NOT NULL,
    cname character varying(255),
    description text
);


--
-- Name: projectrole; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectrole (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text
);


--
-- Name: projectroleactor; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectroleactor (
    id numeric(18,0) NOT NULL,
    pid numeric(18,0),
    projectroleid numeric(18,0),
    roletype character varying(255),
    roletypeparameter character varying(255)
);


--
-- Name: projectversion; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE projectversion (
    id numeric(18,0) NOT NULL,
    project numeric(18,0),
    vname character varying(255),
    description text,
    sequence numeric(18,0),
    released character varying(10),
    archived character varying(10),
    url character varying(255),
    startdate timestamp with time zone,
    releasedate timestamp with time zone
);


--
-- Name: propertydata; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertydata (
    id numeric(18,0) NOT NULL,
    propertyvalue oid
);


--
-- Name: propertydate; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertydate (
    id numeric(18,0) NOT NULL,
    propertyvalue timestamp with time zone
);


--
-- Name: propertydecimal; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertydecimal (
    id numeric(18,0) NOT NULL,
    propertyvalue double precision
);


--
-- Name: propertyentry; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertyentry (
    id numeric(18,0) NOT NULL,
    entity_name character varying(255),
    entity_id numeric(18,0),
    property_key character varying(255),
    propertytype numeric(9,0)
);


--
-- Name: propertynumber; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertynumber (
    id numeric(18,0) NOT NULL,
    propertyvalue numeric(18,0)
);


--
-- Name: propertystring; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertystring (
    id numeric(18,0) NOT NULL,
    propertyvalue text
);


--
-- Name: propertytext; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE propertytext (
    id numeric(18,0) NOT NULL,
    propertyvalue text
);


--
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_calendars (
    id numeric(18,0),
    calendar_name character varying(255) NOT NULL,
    calendar text
);


--
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_cron_triggers (
    id numeric(18,0) NOT NULL,
    trigger_id numeric(18,0),
    cronexperssion character varying(255)
);


--
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_fired_triggers (
    id numeric(18,0),
    entry_id character varying(255) NOT NULL,
    trigger_id numeric(18,0),
    trigger_listener character varying(255),
    fired_time timestamp with time zone,
    trigger_state character varying(255)
);


--
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_job_details (
    id numeric(18,0) NOT NULL,
    job_name character varying(255),
    job_group character varying(255),
    class_name character varying(255),
    is_durable character varying(60),
    is_stateful character varying(60),
    requests_recovery character varying(60),
    job_data character varying(255)
);


--
-- Name: qrtz_job_listeners; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_job_listeners (
    id numeric(18,0) NOT NULL,
    job numeric(18,0),
    job_listener character varying(255)
);


--
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_simple_triggers (
    id numeric(18,0) NOT NULL,
    trigger_id numeric(18,0),
    repeat_count numeric(9,0),
    repeat_interval numeric(18,0),
    times_triggered numeric(9,0)
);


--
-- Name: qrtz_trigger_listeners; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_trigger_listeners (
    id numeric(18,0) NOT NULL,
    trigger_id numeric(18,0),
    trigger_listener character varying(255)
);


--
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE qrtz_triggers (
    id numeric(18,0) NOT NULL,
    trigger_name character varying(255),
    trigger_group character varying(255),
    job numeric(18,0),
    next_fire timestamp with time zone,
    trigger_state character varying(255),
    trigger_type character varying(60),
    start_time timestamp with time zone,
    end_time timestamp with time zone,
    calendar_name character varying(255),
    misfire_instr numeric(9,0)
);


--
-- Name: remembermetoken; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE remembermetoken (
    id numeric(18,0) NOT NULL,
    created timestamp with time zone,
    token character varying(255),
    username character varying(255)
);


--
-- Name: remotelink; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE remotelink (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    globalid character varying(255),
    title character varying(255),
    summary text,
    url text,
    iconurl text,
    icontitle text,
    relationship character varying(255),
    resolved character(1),
    statusname character varying(255),
    statusdescription text,
    statusiconurl text,
    statusicontitle text,
    statusiconlink text,
    statuscategorykey character varying(255),
    statuscategorycolorname character varying(255),
    applicationtype character varying(255),
    applicationname character varying(255)
);


--
-- Name: replicatedindexoperation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE replicatedindexoperation (
    id numeric(18,0) NOT NULL,
    index_time timestamp with time zone,
    node_id character varying(60),
    affected_index character varying(60),
    entity_type character varying(60),
    affected_ids text,
    operation character varying(60)
);


--
-- Name: resolution; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE resolution (
    id character varying(60) NOT NULL,
    sequence numeric(18,0),
    pname character varying(60),
    description text,
    iconurl character varying(255)
);


--
-- Name: rundetails; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE rundetails (
    id numeric(18,0) NOT NULL,
    job_id character varying(255),
    start_time timestamp with time zone,
    run_duration numeric(18,0),
    run_outcome character(1),
    info_message character varying(255)
);


--
-- Name: schemeissuesecurities; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE schemeissuesecurities (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    security numeric(18,0),
    sec_type character varying(255),
    sec_parameter character varying(255)
);


--
-- Name: schemeissuesecuritylevels; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE schemeissuesecuritylevels (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text,
    scheme numeric(18,0)
);


--
-- Name: schemepermissions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE schemepermissions (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    permission numeric(18,0),
    perm_type character varying(255),
    perm_parameter character varying(255)
);


--
-- Name: searchrequest; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE searchrequest (
    id numeric(18,0) NOT NULL,
    filtername character varying(255),
    authorname character varying(255),
    description text,
    username character varying(255),
    groupname character varying(255),
    projectid numeric(18,0),
    reqcontent text,
    fav_count numeric(18,0),
    filtername_lower character varying(255)
);


--
-- Name: sequence_value_item; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sequence_value_item (
    seq_name character varying(60) NOT NULL,
    seq_id numeric(18,0)
);


--
-- Name: serviceconfig; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE serviceconfig (
    id numeric(18,0) NOT NULL,
    delaytime numeric(18,0),
    clazz character varying(255),
    servicename character varying(255)
);


--
-- Name: sharepermissions; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE sharepermissions (
    id numeric(18,0) NOT NULL,
    entityid numeric(18,0),
    entitytype character varying(60),
    sharetype character varying(10),
    param1 character varying(255),
    param2 character varying(60)
);


--
-- Name: trackback_ping; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE trackback_ping (
    id numeric(18,0) NOT NULL,
    issue numeric(18,0),
    url character varying(255),
    title character varying(255),
    blogname character varying(255),
    excerpt character varying(255),
    created timestamp with time zone
);


--
-- Name: trustedapp; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE trustedapp (
    id numeric(18,0) NOT NULL,
    application_id character varying(255),
    name character varying(255),
    public_key text,
    ip_match text,
    url_match text,
    timeout numeric(18,0),
    created timestamp with time zone,
    created_by character varying(255),
    updated timestamp with time zone,
    updated_by character varying(255)
);


--
-- Name: upgradehistory; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE upgradehistory (
    id numeric(18,0),
    upgradeclass character varying(255) NOT NULL,
    targetbuild character varying(255)
);


--
-- Name: upgradeversionhistory; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE upgradeversionhistory (
    id numeric(18,0),
    timeperformed timestamp with time zone,
    targetbuild character varying(255) NOT NULL,
    targetversion character varying(255)
);


--
-- Name: userassociation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userassociation (
    source_name character varying(60) NOT NULL,
    sink_node_id numeric(18,0) NOT NULL,
    sink_node_entity character varying(60) NOT NULL,
    association_type character varying(60) NOT NULL,
    sequence numeric(9,0),
    created timestamp with time zone
);


--
-- Name: userbase; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userbase (
    id numeric(18,0) NOT NULL,
    username character varying(255),
    password_hash character varying(255)
);


--
-- Name: userhistoryitem; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userhistoryitem (
    id numeric(18,0) NOT NULL,
    entitytype character varying(10),
    entityid character varying(60),
    username character varying(255),
    lastviewed numeric(18,0),
    data text
);


--
-- Name: userpickerfilter; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userpickerfilter (
    id numeric(18,0) NOT NULL,
    customfield numeric(18,0),
    customfieldconfig numeric(18,0),
    enabled character varying(60)
);


--
-- Name: userpickerfiltergroup; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userpickerfiltergroup (
    id numeric(18,0) NOT NULL,
    userpickerfilter numeric(18,0),
    groupname character varying(255)
);


--
-- Name: userpickerfilterrole; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE userpickerfilterrole (
    id numeric(18,0) NOT NULL,
    userpickerfilter numeric(18,0),
    projectroleid numeric(18,0)
);


--
-- Name: versioncontrol; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE versioncontrol (
    id numeric(18,0) NOT NULL,
    vcsname character varying(255),
    vcsdescription character varying(255),
    vcstype character varying(255)
);


--
-- Name: votehistory; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE votehistory (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    votes numeric(18,0),
    "timestamp" timestamp with time zone
);


--
-- Name: workflowscheme; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE workflowscheme (
    id numeric(18,0) NOT NULL,
    name character varying(255),
    description text
);


--
-- Name: workflowschemeentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE workflowschemeentity (
    id numeric(18,0) NOT NULL,
    scheme numeric(18,0),
    workflow character varying(255),
    issuetype character varying(255)
);


--
-- Name: worklog; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE worklog (
    id numeric(18,0) NOT NULL,
    issueid numeric(18,0),
    author character varying(255),
    grouplevel character varying(255),
    rolelevel numeric(18,0),
    worklogbody text,
    created timestamp with time zone,
    updateauthor character varying(255),
    updated timestamp with time zone,
    startdate timestamp with time zone,
    timeworked numeric(18,0)
);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_21D670_WHITELIST_RULES" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_21D670_WHITELIST_RULES_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_4AEACD_WEBHOOK_DAO" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_4AEACD_WEBHOOK_DAO_ID_seq"'::regclass);


--
-- Name: ACTIVITY_ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY" ALTER COLUMN "ACTIVITY_ID" SET DEFAULT nextval('"AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTOR_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_563AEE_ACTOR_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_MEDIA_LINK_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_563AEE_MEDIA_LINK_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_OBJECT_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_563AEE_OBJECT_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_TARGET_ENTITY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_563AEE_TARGET_ENTITY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_B9A0F0_APPLIED_TEMPLATE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_B9A0F0_APPLIED_TEMPLATE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_BRANCH_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_CHANGESET_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_CHANGESET_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_COMMIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT_ISSUE_KEY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_GIT_HUB_EVENT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_MAPPING_V2" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_MESSAGE_TAG_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ORGANIZATION_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PROJECT_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PROJECT_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PROJECT_MAPPING_V2" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_ISSUE_KEY" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_ISSUE_KEY_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_PARTICIPANT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PR_TO_COMMIT_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PULL_REQUEST" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_PULL_REQUEST_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_REPOSITORY_MAPPING" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_REPOSITORY_MAPPING_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_REPO_TO_CHANGESET_ID_seq"'::regclass);


--
-- Name: ID; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_AUDIT_LOG" ALTER COLUMN "ID" SET DEFAULT nextval('"AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq"'::regclass);


--
-- Data for Name: AO_21D670_WHITELIST_RULES; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_21D670_WHITELIST_RULES" ("ALLOWINBOUND", "EXPRESSION", "ID", "TYPE") FROM stdin;
f	http://www.atlassian.com/*	1	WILDCARD_EXPRESSION
\.


--
-- Name: AO_21D670_WHITELIST_RULES_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_21D670_WHITELIST_RULES_ID_seq"', 1, true);


--
-- Data for Name: AO_4AEACD_WEBHOOK_DAO; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_4AEACD_WEBHOOK_DAO" ("ENABLED", "ENCODED_EVENTS", "FILTER", "ID", "JQL", "LAST_UPDATED", "LAST_UPDATED_USER", "NAME", "REGISTRATION_METHOD", "URL", "EXCLUDE_ISSUE_DETAILS", "PARAMETERS") FROM stdin;
\.


--
-- Name: AO_4AEACD_WEBHOOK_DAO_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_4AEACD_WEBHOOK_DAO_ID_seq"', 1, false);


--
-- Data for Name: AO_563AEE_ACTIVITY_ENTITY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_563AEE_ACTIVITY_ENTITY" ("ACTIVITY_ID", "ACTOR_ID", "CONTENT", "GENERATOR_DISPLAY_NAME", "GENERATOR_ID", "ICON_ID", "ID", "ISSUE_KEY", "OBJECT_ID", "POSTER", "PROJECT_KEY", "PUBLISHED", "TARGET_ID", "TITLE", "URL", "USERNAME", "VERB") FROM stdin;
\.


--
-- Name: AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_563AEE_ACTIVITY_ENTITY_ACTIVITY_ID_seq"', 1, false);


--
-- Data for Name: AO_563AEE_ACTOR_ENTITY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_563AEE_ACTOR_ENTITY" ("FULL_NAME", "ID", "PROFILE_PAGE_URI", "PROFILE_PICTURE_URI", "USERNAME") FROM stdin;
\.


--
-- Name: AO_563AEE_ACTOR_ENTITY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_563AEE_ACTOR_ENTITY_ID_seq"', 1, false);


--
-- Data for Name: AO_563AEE_MEDIA_LINK_ENTITY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_563AEE_MEDIA_LINK_ENTITY" ("DURATION", "HEIGHT", "ID", "URL", "WIDTH") FROM stdin;
\.


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_563AEE_MEDIA_LINK_ENTITY_ID_seq"', 1, false);


--
-- Data for Name: AO_563AEE_OBJECT_ENTITY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_563AEE_OBJECT_ENTITY" ("CONTENT", "DISPLAY_NAME", "ID", "IMAGE_ID", "OBJECT_ID", "OBJECT_TYPE", "SUMMARY", "URL") FROM stdin;
\.


--
-- Name: AO_563AEE_OBJECT_ENTITY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_563AEE_OBJECT_ENTITY_ID_seq"', 1, false);


--
-- Data for Name: AO_563AEE_TARGET_ENTITY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_563AEE_TARGET_ENTITY" ("CONTENT", "DISPLAY_NAME", "ID", "IMAGE_ID", "OBJECT_ID", "OBJECT_TYPE", "SUMMARY", "URL") FROM stdin;
\.


--
-- Name: AO_563AEE_TARGET_ENTITY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_563AEE_TARGET_ENTITY_ID_seq"', 1, false);


--
-- Data for Name: AO_B9A0F0_APPLIED_TEMPLATE; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_B9A0F0_APPLIED_TEMPLATE" ("ID", "PROJECT_ID", "PROJECT_TEMPLATE_MODULE_KEY", "PROJECT_TEMPLATE_WEB_ITEM_KEY") FROM stdin;
\.


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_B9A0F0_APPLIED_TEMPLATE_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_BRANCH; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_BRANCH" ("ID", "NAME", "REPOSITORY_ID") FROM stdin;
\.


--
-- Data for Name: AO_E8B6CC_BRANCH_HEAD_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_BRANCH_HEAD_MAPPING" ("BRANCH_NAME", "HEAD", "ID", "REPOSITORY_ID") FROM stdin;
\.


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_BRANCH_HEAD_MAPPING_ID_seq"', 1, false);


--
-- Name: AO_E8B6CC_BRANCH_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_BRANCH_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_CHANGESET_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_CHANGESET_MAPPING" ("AUTHOR", "AUTHOR_EMAIL", "BRANCH", "DATE", "FILES_DATA", "FILE_DETAILS_JSON", "ID", "ISSUE_KEY", "MESSAGE", "NODE", "PARENTS_DATA", "PROJECT_KEY", "RAW_AUTHOR", "RAW_NODE", "REPOSITORY_ID", "SMARTCOMMIT_AVAILABLE", "VERSION") FROM stdin;
\.


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_CHANGESET_MAPPING_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_COMMIT; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_COMMIT" ("AUTHOR", "AUTHOR_AVATAR_URL", "DATE", "DOMAIN_ID", "ID", "MESSAGE", "NODE", "RAW_AUTHOR") FROM stdin;
\.


--
-- Name: AO_E8B6CC_COMMIT_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_COMMIT_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_COMMIT_ISSUE_KEY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_COMMIT_ISSUE_KEY" ("COMMIT_ID", "DOMAIN_ID", "ID", "ISSUE_KEY") FROM stdin;
\.


--
-- Name: AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_COMMIT_ISSUE_KEY_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_GIT_HUB_EVENT; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_GIT_HUB_EVENT" ("CREATED_AT", "GIT_HUB_ID", "ID", "REPOSITORY_ID", "SAVE_POINT") FROM stdin;
\.


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_GIT_HUB_EVENT_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_ISSUE_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_ISSUE_MAPPING" ("ID", "ISSUE_ID", "NODE", "PROJECT_KEY", "REPOSITORY_URI") FROM stdin;
\.


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_ISSUE_MAPPING_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_ISSUE_MAPPING_V2; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_ISSUE_MAPPING_V2" ("AUTHOR", "BRANCH", "DATE", "FILES_DATA", "ID", "ISSUE_ID", "MESSAGE", "NODE", "PARENTS_DATA", "RAW_AUTHOR", "RAW_NODE", "REPOSITORY_ID", "VERSION") FROM stdin;
\.


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_ISSUE_MAPPING_V2_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_ISSUE_TO_BRANCH; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_ISSUE_TO_BRANCH" ("BRANCH_ID", "ID", "ISSUE_KEY") FROM stdin;
\.


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_ISSUE_TO_BRANCH_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_ISSUE_TO_CHANGESET; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_ISSUE_TO_CHANGESET" ("CHANGESET_ID", "ID", "ISSUE_KEY", "PROJECT_KEY") FROM stdin;
\.


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_ISSUE_TO_CHANGESET_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_MESSAGE; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_MESSAGE" ("ADDRESS", "ID", "PAYLOAD", "PAYLOAD_TYPE", "PRIORITY") FROM stdin;
\.


--
-- Name: AO_E8B6CC_MESSAGE_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_MESSAGE_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_MESSAGE_QUEUE_ITEM" ("ID", "LAST_FAILED", "MESSAGE_ID", "QUEUE", "RETRIES_COUNT", "STATE", "STATE_INFO") FROM stdin;
\.


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_MESSAGE_QUEUE_ITEM_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_MESSAGE_TAG; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_MESSAGE_TAG" ("ID", "MESSAGE_ID", "TAG") FROM stdin;
\.


--
-- Name: AO_E8B6CC_MESSAGE_TAG_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_MESSAGE_TAG_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_ORGANIZATION_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_ORGANIZATION_MAPPING" ("ACCESS_TOKEN", "ADMIN_PASSWORD", "ADMIN_USERNAME", "AUTOLINK_NEW_REPOS", "DEFAULT_GROUPS_SLUGS", "DVCS_TYPE", "HOST_URL", "ID", "NAME", "OAUTH_KEY", "OAUTH_SECRET", "SMARTCOMMITS_FOR_NEW_REPOS") FROM stdin;
\.


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_ORGANIZATION_MAPPING_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PROJECT_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PROJECT_MAPPING" ("ID", "PASSWORD", "PROJECT_KEY", "REPOSITORY_URI", "USERNAME") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PROJECT_MAPPING_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PROJECT_MAPPING_V2; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PROJECT_MAPPING_V2" ("ACCESS_TOKEN", "ADMIN_PASSWORD", "ADMIN_USERNAME", "ID", "LAST_COMMIT_DATE", "PROJECT_KEY", "REPOSITORY_NAME", "REPOSITORY_TYPE", "REPOSITORY_URL") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PROJECT_MAPPING_V2_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PR_ISSUE_KEY; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PR_ISSUE_KEY" ("DOMAIN_ID", "ID", "ISSUE_KEY", "PULL_REQUEST_ID") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PR_ISSUE_KEY_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PR_PARTICIPANT; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PR_PARTICIPANT" ("APPROVED", "DOMAIN_ID", "ID", "PULL_REQUEST_ID", "ROLE", "USERNAME") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PR_PARTICIPANT_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PR_PARTICIPANT_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PR_TO_COMMIT; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PR_TO_COMMIT" ("COMMIT_ID", "DOMAIN_ID", "ID", "REQUEST_ID") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PR_TO_COMMIT_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PR_TO_COMMIT_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_PULL_REQUEST; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_PULL_REQUEST" ("AUTHOR", "COMMENT_COUNT", "CREATED_ON", "DESTINATION_BRANCH", "DOMAIN_ID", "ID", "LAST_STATUS", "NAME", "REMOTE_ID", "SOURCE_BRANCH", "SOURCE_REPO", "TO_REPOSITORY_ID", "TO_REPO_ID", "UPDATED_ON", "URL") FROM stdin;
\.


--
-- Name: AO_E8B6CC_PULL_REQUEST_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_PULL_REQUEST_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_REPOSITORY_MAPPING; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_REPOSITORY_MAPPING" ("ACTIVITY_LAST_SYNC", "DELETED", "FORK", "FORK_OF_NAME", "FORK_OF_OWNER", "FORK_OF_SLUG", "ID", "LAST_CHANGESET_NODE", "LAST_COMMIT_DATE", "LINKED", "LOGO", "NAME", "ORGANIZATION_ID", "SLUG", "SMARTCOMMITS_ENABLED") FROM stdin;
\.


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_REPOSITORY_MAPPING_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_REPO_TO_CHANGESET; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_REPO_TO_CHANGESET" ("CHANGESET_ID", "ID", "REPOSITORY_ID") FROM stdin;
\.


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_REPO_TO_CHANGESET_ID_seq"', 1, false);


--
-- Data for Name: AO_E8B6CC_SYNC_AUDIT_LOG; Type: TABLE DATA; Schema: public; Owner: -
--

COPY "AO_E8B6CC_SYNC_AUDIT_LOG" ("END_DATE", "EXC_TRACE", "FIRST_REQUEST_DATE", "FLIGHT_TIME_MS", "ID", "NUM_REQUESTS", "REPO_ID", "START_DATE", "SYNC_STATUS", "SYNC_TYPE", "TOTAL_ERRORS") FROM stdin;
\.


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('"AO_E8B6CC_SYNC_AUDIT_LOG_ID_seq"', 1, false);


--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY app_user (id, user_key, lower_user_name) FROM stdin;
10000	admin	admin
\.


--
-- Data for Name: audit_changed_value; Type: TABLE DATA; Schema: public; Owner: -
--

COPY audit_changed_value (id, log_id, name, delta_from, delta_to) FROM stdin;
10000	10000	Username	\N	admin
10001	10000	Full Name	\N	Admin Istrator
10002	10000	Email	\N	admin@localhost
10003	10000	Active / Inactive	\N	Active
\.


--
-- Data for Name: audit_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY audit_item (id, log_id, object_type, object_id, object_name, object_parent_id, object_parent_name) FROM stdin;
10000	10001	USER	admin	admin	1	JIRA Internal Directory
10001	10002	USER	admin	admin	1	JIRA Internal Directory
10002	10003	USER	admin	admin	1	JIRA Internal Directory
\.


--
-- Data for Name: audit_log; Type: TABLE DATA; Schema: public; Owner: -
--

COPY audit_log (id, remote_address, created, author_key, summary, category, object_type, object_id, object_name, object_parent_id, object_parent_name, author_type, search_field) FROM stdin;
10000	127.0.0.1	2014-04-03 22:18:30.999+11	\N	User created	user management	USER	admin	admin	1	JIRA Internal Directory	0	127.0.0.1 user created management admin jira internal directory istrator admin@localhost active
10001	127.0.0.1	2014-04-03 22:18:31.048+11	\N	User added to group	group management	GROUP	\N	jira-users	\N	\N	0	127.0.0.1 user added to group management jira-users admin jira internal directory
10002	127.0.0.1	2014-04-03 22:18:31.085+11	\N	User added to group	group management	GROUP	\N	jira-administrators	\N	\N	0	127.0.0.1 user added to group management jira-administrators admin jira internal directory
10003	127.0.0.1	2014-04-03 22:18:31.1+11	\N	User added to group	group management	GROUP	\N	jira-developers	\N	\N	0	127.0.0.1 user added to group management jira-developers admin jira internal directory
\.


--
-- Data for Name: avatar; Type: TABLE DATA; Schema: public; Owner: -
--

COPY avatar (id, filename, contenttype, avatartype, owner, systemavatar) FROM stdin;
10000	codegeist.png	image/png	project	\N	1
10001	eamesbird.png	image/png	project	\N	1
10002	jm_black.png	image/png	project	\N	1
10003	jm_brown.png	image/png	project	\N	1
10004	jm_orange.png	image/png	project	\N	1
10005	jm_red.png	image/png	project	\N	1
10006	jm_white.png	image/png	project	\N	1
10007	jm_yellow.png	image/png	project	\N	1
10008	monster.png	image/png	project	\N	1
10009	rainbow.png	image/png	project	\N	1
10010	kangaroo.png	image/png	project	\N	1
10011	rocket.png	image/png	project	\N	1
10100	Avatar-1.png	image/png	user	\N	1
10101	Avatar-2.png	image/png	user	\N	1
10102	Avatar-3.png	image/png	user	\N	1
10103	Avatar-4.png	image/png	user	\N	1
10104	Avatar-5.png	image/png	user	\N	1
10105	Avatar-6.png	image/png	user	\N	1
10106	Avatar-7.png	image/png	user	\N	1
10107	Avatar-8.png	image/png	user	\N	1
10108	Avatar-9.png	image/png	user	\N	1
10109	Avatar-10.png	image/png	user	\N	1
10110	Avatar-11.png	image/png	user	\N	1
10111	Avatar-12.png	image/png	user	\N	1
10112	Avatar-13.png	image/png	user	\N	1
10113	Avatar-14.png	image/png	user	\N	1
10114	Avatar-15.png	image/png	user	\N	1
10115	Avatar-16.png	image/png	user	\N	1
10116	Avatar-17.png	image/png	user	\N	1
10117	Avatar-18.png	image/png	user	\N	1
10118	Avatar-19.png	image/png	user	\N	1
10119	Avatar-20.png	image/png	user	\N	1
10120	Avatar-21.png	image/png	user	\N	1
10121	Avatar-22.png	image/png	user	\N	1
10122	Avatar-default.png	image/png	user	\N	1
10123	Avatar-unknown.png	image/png	user	\N	1
10200	cloud.png	image/png	project	\N	1
10201	config.png	image/png	project	\N	1
10202	disc.png	image/png	project	\N	1
10203	finance.png	image/png	project	\N	1
10204	hand.png	image/png	project	\N	1
10205	new_monster.png	image/png	project	\N	1
10206	power.png	image/png	project	\N	1
10207	refresh.png	image/png	project	\N	1
10208	servicedesk.png	image/png	project	\N	1
10209	settings.png	image/png	project	\N	1
10210	storm.png	image/png	project	\N	1
10211	travel.png	image/png	project	\N	1
\.


--
-- Data for Name: changegroup; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changegroup (id, issueid, author, created) FROM stdin;
\.


--
-- Data for Name: changeitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY changeitem (id, groupid, fieldtype, field, oldvalue, oldstring, newvalue, newstring) FROM stdin;
\.


--
-- Data for Name: clusterlockstatus; Type: TABLE DATA; Schema: public; Owner: -
--

COPY clusterlockstatus (id, lock_name, locked_by_node) FROM stdin;
10001	com.atlassian.jira.crowd.embedded.ofbiz.OfBizGroupDao.loadGroupCacheLock	\N
10000	com.atlassian.jira.crowd.embedded.ofbiz.OfBizUserDao.loadUserCacheLock	\N
\.


--
-- Data for Name: clustermessage; Type: TABLE DATA; Schema: public; Owner: -
--

COPY clustermessage (id, source_node, destination_node, claimed_by_node, message, message_time) FROM stdin;
\.


--
-- Data for Name: clusternode; Type: TABLE DATA; Schema: public; Owner: -
--

COPY clusternode (node_id, node_state) FROM stdin;
\.


--
-- Data for Name: columnlayout; Type: TABLE DATA; Schema: public; Owner: -
--

COPY columnlayout (id, username, searchrequest) FROM stdin;
\.


--
-- Data for Name: columnlayoutitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY columnlayoutitem (id, columnlayout, fieldidentifier, horizontalposition) FROM stdin;
\.


--
-- Data for Name: component; Type: TABLE DATA; Schema: public; Owner: -
--

COPY component (id, project, cname, description, url, lead, assigneetype) FROM stdin;
\.


--
-- Data for Name: configurationcontext; Type: TABLE DATA; Schema: public; Owner: -
--

COPY configurationcontext (id, projectcategory, project, customfield, fieldconfigscheme) FROM stdin;
10000	\N	\N	issuetype	10000
\.


--
-- Data for Name: customfield; Type: TABLE DATA; Schema: public; Owner: -
--

COPY customfield (id, customfieldtypekey, customfieldsearcherkey, cfname, description, defaultvalue, fieldtype, project, issuetype) FROM stdin;
\.


--
-- Data for Name: customfieldoption; Type: TABLE DATA; Schema: public; Owner: -
--

COPY customfieldoption (id, customfield, customfieldconfig, parentoptionid, sequence, customvalue, optiontype, disabled) FROM stdin;
\.


--
-- Data for Name: customfieldvalue; Type: TABLE DATA; Schema: public; Owner: -
--

COPY customfieldvalue (id, issue, customfield, parentkey, stringvalue, numbervalue, textvalue, datevalue, valuetype) FROM stdin;
\.


--
-- Data for Name: cwd_application; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_application (id, application_name, lower_application_name, created_date, updated_date, active, description, application_type, credential) FROM stdin;
1	crowd-embedded	crowd-embedded	2013-02-28 11:57:51.302+11	2013-02-28 11:57:51.302+11	1		CROWD	X
\.


--
-- Data for Name: cwd_application_address; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_application_address (application_id, remote_address, encoded_address_binary, remote_address_mask) FROM stdin;
\.


--
-- Data for Name: cwd_directory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_directory (id, directory_name, lower_directory_name, created_date, updated_date, active, description, impl_class, lower_impl_class, directory_type, directory_position) FROM stdin;
1	JIRA Internal Directory	jira internal directory	2013-02-28 11:57:51.308+11	2013-02-28 11:57:51.308+11	1	JIRA default internal directory	com.atlassian.crowd.directory.InternalDirectory	com.atlassian.crowd.directory.internaldirectory	INTERNAL	0
\.


--
-- Data for Name: cwd_directory_attribute; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_directory_attribute (directory_id, attribute_name, attribute_value) FROM stdin;
1	user_encryption_method	atlassian-security
\.


--
-- Data for Name: cwd_directory_operation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_directory_operation (directory_id, operation_type) FROM stdin;
1	CREATE_GROUP
1	CREATE_ROLE
1	CREATE_USER
1	DELETE_GROUP
1	DELETE_ROLE
1	DELETE_USER
1	UPDATE_GROUP
1	UPDATE_GROUP_ATTRIBUTE
1	UPDATE_ROLE
1	UPDATE_ROLE_ATTRIBUTE
1	UPDATE_USER
1	UPDATE_USER_ATTRIBUTE
\.


--
-- Data for Name: cwd_group; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_group (id, group_name, lower_group_name, active, local, created_date, updated_date, description, lower_description, group_type, directory_id) FROM stdin;
10000	jira-administrators	jira-administrators	1	0	2013-02-28 11:57:51.326+11	2013-02-28 11:57:51.326+11		\N	GROUP	1
10001	jira-developers	jira-developers	1	0	2013-02-28 11:57:51.326+11	2013-02-28 11:57:51.326+11		\N	GROUP	1
10002	jira-users	jira-users	1	0	2013-02-28 11:57:51.326+11	2013-02-28 11:57:51.326+11		\N	GROUP	1
\.


--
-- Data for Name: cwd_group_attributes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_group_attributes (id, group_id, directory_id, attribute_name, attribute_value, lower_attribute_value) FROM stdin;
\.


--
-- Data for Name: cwd_membership; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_membership (id, parent_id, child_id, membership_type, group_type, parent_name, lower_parent_name, child_name, lower_child_name, directory_id) FROM stdin;
10000	10002	10000	GROUP_USER	\N	jira-users	jira-users	admin	admin	1
10001	10000	10000	GROUP_USER	\N	jira-administrators	jira-administrators	admin	admin	1
10002	10001	10000	GROUP_USER	\N	jira-developers	jira-developers	admin	admin	1
\.


--
-- Data for Name: cwd_user; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_user (id, directory_id, user_name, lower_user_name, active, created_date, updated_date, first_name, lower_first_name, last_name, lower_last_name, display_name, lower_display_name, email_address, lower_email_address, credential, deleted_externally, external_id) FROM stdin;
10000	1	admin	admin	1	2014-04-03 22:18:30.905+11	2014-04-03 22:18:30.905+11	Admin	admin	Istrator	istrator	Admin Istrator	admin istrator	admin@localhost	admin@localhost	{PKCS5S2}xO/0BNDztYCFEtIPW4QLnBjojZQQTgKuOzgdUbajw6kBx5riKR/LsxqZ0PvdqAgA	\N	463aeb12-0dcd-4ed5-8f4b-eff367a6a3b2
\.


--
-- Data for Name: cwd_user_attributes; Type: TABLE DATA; Schema: public; Owner: -
--

COPY cwd_user_attributes (id, user_id, directory_id, attribute_name, attribute_value, lower_attribute_value) FROM stdin;
10000	10000	1	requiresPasswordChange	false	false
10001	10000	1	passwordLastChanged	1396523910894	1396523910894
10002	10000	1	password.reset.request.expiry	1396610311062	1396610311062
10003	10000	1	password.reset.request.token	25c395e8629a66bacb25fd882c2137df67ae43e2	25c395e8629a66bacb25fd882c2137df67ae43e2
\.


--
-- Data for Name: draftworkflowscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY draftworkflowscheme (id, name, description, workflow_scheme_id, last_modified_date, last_modified_user) FROM stdin;
\.


--
-- Data for Name: draftworkflowschemeentity; Type: TABLE DATA; Schema: public; Owner: -
--

COPY draftworkflowschemeentity (id, scheme, workflow, issuetype) FROM stdin;
\.


--
-- Data for Name: entity_property; Type: TABLE DATA; Schema: public; Owner: -
--

COPY entity_property (id, entity_name, entity_id, property_key, created, updated, json_value) FROM stdin;
\.


--
-- Data for Name: entity_property_index_document; Type: TABLE DATA; Schema: public; Owner: -
--

COPY entity_property_index_document (id, plugin_key, module_key, entity_key, updated, document) FROM stdin;
10000	com.atlassian.jira.dev.func-test-plugin	index-doc-conf	IssueProperty	2014-04-03 22:18:13.394+11	<?xml version="1.0" encoding="UTF-8"?>\n<index-document-configuration entity-key="IssueProperty"><key property-key="func.test.prop"><extract path="func" type="STRING"/></key></index-document-configuration>
\.


--
-- Data for Name: external_entities; Type: TABLE DATA; Schema: public; Owner: -
--

COPY external_entities (id, name, entitytype) FROM stdin;
\.


--
-- Data for Name: externalgadget; Type: TABLE DATA; Schema: public; Owner: -
--

COPY externalgadget (id, gadget_xml) FROM stdin;
\.


--
-- Data for Name: favouriteassociations; Type: TABLE DATA; Schema: public; Owner: -
--

COPY favouriteassociations (id, username, entitytype, entityid, sequence) FROM stdin;
\.


--
-- Data for Name: feature; Type: TABLE DATA; Schema: public; Owner: -
--

COPY feature (id, feature_name, feature_type, user_key) FROM stdin;
\.


--
-- Data for Name: fieldconfigscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldconfigscheme (id, configname, description, fieldid, customfield) FROM stdin;
10000	Default Issue Type Scheme	Default issue type scheme is the list of global issue types. All newly created issue types will automatically be added to this scheme.	issuetype	\N
\.


--
-- Data for Name: fieldconfigschemeissuetype; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldconfigschemeissuetype (id, issuetype, fieldconfigscheme, fieldconfiguration) FROM stdin;
10100	\N	10000	10000
\.


--
-- Data for Name: fieldconfiguration; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldconfiguration (id, configname, description, fieldid, customfield) FROM stdin;
10000	Default Configuration for Issue Type	Default configuration generated by JIRA	issuetype	\N
\.


--
-- Data for Name: fieldlayout; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldlayout (id, name, description, layout_type, layoutscheme) FROM stdin;
10000	Default Field Configuration	The default field configuration	default	\N
\.


--
-- Data for Name: fieldlayoutitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldlayoutitem (id, fieldlayout, fieldidentifier, description, verticalposition, ishidden, isrequired, renderertype) FROM stdin;
10100	10000	summary	\N	\N	false	true	jira-text-renderer
10101	10000	issuetype	\N	\N	false	true	jira-text-renderer
10102	10000	security	\N	\N	false	false	jira-text-renderer
10103	10000	priority	\N	\N	false	false	jira-text-renderer
10104	10000	duedate	\N	\N	false	false	jira-text-renderer
10105	10000	components	\N	\N	false	false	frother-control-renderer
10106	10000	versions	\N	\N	false	false	frother-control-renderer
10107	10000	fixVersions	\N	\N	false	false	frother-control-renderer
10108	10000	assignee	\N	\N	false	false	jira-text-renderer
10109	10000	reporter	\N	\N	false	true	jira-text-renderer
10110	10000	environment	For example operating system, software platform and/or hardware specifications (include as appropriate for the issue).	\N	false	false	atlassian-wiki-renderer
10111	10000	description	\N	\N	false	false	atlassian-wiki-renderer
10112	10000	timetracking	An estimate of how much work remains until this issue will be resolved.<br>The format of this is ' *w *d *h *m ' (representing weeks, days, hours and minutes - where * can be any number)<br>Examples: 4d, 5h 30m, 60m and 3w.<br>	\N	false	false	jira-text-renderer
10113	10000	resolution	\N	\N	false	false	jira-text-renderer
10114	10000	attachment	\N	\N	false	false	jira-text-renderer
10115	10000	comment	\N	\N	false	false	atlassian-wiki-renderer
10116	10000	labels	\N	\N	false	false	jira-text-renderer
10117	10000	worklog	Allows work to be logged whilst creating, editing or transitioning issues.	\N	false	false	atlassian-wiki-renderer
10118	10000	issuelinks	\N	\N	false	false	jira-text-renderer
\.


--
-- Data for Name: fieldlayoutscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldlayoutscheme (id, name, description) FROM stdin;
\.


--
-- Data for Name: fieldlayoutschemeassociation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldlayoutschemeassociation (id, issuetype, project, fieldlayoutscheme) FROM stdin;
\.


--
-- Data for Name: fieldlayoutschemeentity; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldlayoutschemeentity (id, scheme, issuetype, fieldlayout) FROM stdin;
\.


--
-- Data for Name: fieldscreen; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldscreen (id, name, description) FROM stdin;
1	Default Screen	Allows to update all system fields.
2	Workflow Screen	This screen is used in the workflow and enables you to assign issues
3	Resolve Issue Screen	Allows to set resolution, change fix versions and assign an issue.
\.


--
-- Data for Name: fieldscreenlayoutitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldscreenlayoutitem (id, fieldidentifier, sequence, fieldscreentab) FROM stdin;
10000	summary	0	10000
10001	issuetype	1	10000
10002	security	2	10000
10003	priority	3	10000
10004	duedate	4	10000
10005	components	5	10000
10006	versions	6	10000
10007	fixVersions	7	10000
10008	assignee	8	10000
10009	reporter	9	10000
10010	environment	10	10000
10011	description	11	10000
10012	timetracking	12	10000
10013	attachment	13	10000
10014	assignee	0	10001
10015	resolution	0	10002
10016	fixVersions	1	10002
10017	assignee	2	10002
10018	worklog	3	10002
10100	labels	14	10000
\.


--
-- Data for Name: fieldscreenscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldscreenscheme (id, name, description) FROM stdin;
1	Default Screen Scheme	Default Screen Scheme
\.


--
-- Data for Name: fieldscreenschemeitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldscreenschemeitem (id, operation, fieldscreen, fieldscreenscheme) FROM stdin;
10000	\N	1	1
\.


--
-- Data for Name: fieldscreentab; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fieldscreentab (id, name, description, sequence, fieldscreen) FROM stdin;
10000	Field Tab	\N	0	1
10001	Field Tab	\N	0	2
10002	Field Tab	\N	0	3
\.


--
-- Data for Name: fileattachment; Type: TABLE DATA; Schema: public; Owner: -
--

COPY fileattachment (id, issueid, mimetype, filename, created, filesize, author, zip, thumbnailable) FROM stdin;
\.


--
-- Data for Name: filtersubscription; Type: TABLE DATA; Schema: public; Owner: -
--

COPY filtersubscription (id, filter_i_d, username, groupname, last_run, email_on_empty) FROM stdin;
\.


--
-- Data for Name: gadgetuserpreference; Type: TABLE DATA; Schema: public; Owner: -
--

COPY gadgetuserpreference (id, portletconfiguration, userprefkey, userprefvalue) FROM stdin;
10000	10002	isConfigured	true
10001	10003	keys	__all_projects__
10002	10003	isConfigured	true
10003	10003	title	Your Company JIRA
10004	10003	numofentries	5
\.


--
-- Data for Name: genericconfiguration; Type: TABLE DATA; Schema: public; Owner: -
--

COPY genericconfiguration (id, datatype, datakey, xmlvalue) FROM stdin;
10000	DefaultValue	10000	<string>1</string>
\.


--
-- Data for Name: globalpermissionentry; Type: TABLE DATA; Schema: public; Owner: -
--

COPY globalpermissionentry (id, permission, group_id) FROM stdin;
10000	MANAGE_GROUP_FILTER_SUBSCRIPTIONS	jira-developers
10001	USER_PICKER	jira-developers
10002	USE	jira-users
10003	ADMINISTER	jira-administrators
10004	SYSTEM_ADMIN	jira-administrators
10005	BULK_CHANGE	jira-users
10006	CREATE_SHARED_OBJECTS	jira-users
\.


--
-- Data for Name: groupbase; Type: TABLE DATA; Schema: public; Owner: -
--

COPY groupbase (id, groupname) FROM stdin;
\.


--
-- Data for Name: issuelink; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuelink (id, linktype, source, destination, sequence) FROM stdin;
\.


--
-- Data for Name: issuelinktype; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuelinktype (id, linkname, inward, outward, pstyle) FROM stdin;
10002	Duplicate	is duplicated by	duplicates	\N
10003	Relates	relates to	relates to	\N
10100	jira_subtask_link	jira_subtask_inward	jira_subtask_outward	jira_subtask
10000	Blocks	is blocked by	blocks	\N
10001	Cloners	is cloned by	clones	\N
\.


--
-- Data for Name: issuesecurityscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuesecurityscheme (id, name, description, defaultlevel) FROM stdin;
\.


--
-- Data for Name: issuestatus; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuestatus (id, sequence, pname, description, iconurl, statuscategory) FROM stdin;
1	1	Open	The issue is open and ready for the assignee to start work on it.	/images/icons/statuses/open.png	2
4	4	Reopened	This issue was once resolved, but the resolution was deemed incorrect. From here issues are either marked assigned or resolved.	/images/icons/statuses/reopened.png	2
3	3	In Progress	This issue is being actively worked on at the moment by the assignee.	/images/icons/statuses/inprogress.png	4
5	5	Resolved	A resolution has been taken, and it is awaiting verification by reporter. From here issues are either reopened, or are closed.	/images/icons/statuses/resolved.png	3
6	6	Closed	The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.	/images/icons/statuses/closed.png	3
\.


--
-- Data for Name: issuetype; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuetype (id, sequence, pname, pstyle, description, iconurl) FROM stdin;
5	0	Sub-task	jira_subtask	The sub-task of the issue	/images/icons/issuetypes/subtask_alternate.png
1	1	Bug	\N	A problem which impairs or prevents the functions of the product.	/images/icons/issuetypes/bug.png
2	2	New Feature	\N	A new feature of the product, which has yet to be developed.	/images/icons/issuetypes/newfeature.png
3	3	Task	\N	A task that needs to be done.	/images/icons/issuetypes/task.png
4	4	Improvement	\N	An improvement or enhancement to an existing feature or task.	/images/icons/issuetypes/improvement.png
\.


--
-- Data for Name: issuetypescreenscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuetypescreenscheme (id, name, description) FROM stdin;
1	Default Issue Type Screen Scheme	The default issue type screen scheme
\.


--
-- Data for Name: issuetypescreenschemeentity; Type: TABLE DATA; Schema: public; Owner: -
--

COPY issuetypescreenschemeentity (id, issuetype, scheme, fieldscreenscheme) FROM stdin;
10000	\N	1	1
\.


--
-- Data for Name: jiraaction; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiraaction (id, issueid, author, actiontype, actionlevel, rolelevel, actionbody, created, updateauthor, updated, actionnum) FROM stdin;
\.


--
-- Data for Name: jiradraftworkflows; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiradraftworkflows (id, parentname, descriptor) FROM stdin;
\.


--
-- Data for Name: jiraeventtype; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiraeventtype (id, template_id, name, description, event_type) FROM stdin;
14	\N	Issue Comment Edited	This is the 'issue comment edited' event.	jira.system.event.type
15	\N	Issue Worklog Updated	This is the 'issue worklog updated' event.	jira.system.event.type
16	\N	Issue Worklog Deleted	This is the 'issue worklog deleted' event.	jira.system.event.type
1	\N	Issue Created	This is the 'issue created' event.	jira.system.event.type
2	\N	Issue Updated	This is the 'issue updated' event.	jira.system.event.type
3	\N	Issue Assigned	This is the 'issue assigned' event.	jira.system.event.type
4	\N	Issue Resolved	This is the 'issue resolved' event.	jira.system.event.type
5	\N	Issue Closed	This is the 'issue closed' event.	jira.system.event.type
6	\N	Issue Commented	This is the 'issue commented' event.	jira.system.event.type
7	\N	Issue Reopened	This is the 'issue reopened' event.	jira.system.event.type
8	\N	Issue Deleted	This is the 'issue deleted' event.	jira.system.event.type
9	\N	Issue Moved	This is the 'issue moved' event.	jira.system.event.type
10	\N	Work Logged On Issue	This is the 'work logged on issue' event.	jira.system.event.type
11	\N	Work Started On Issue	This is the 'work started on issue' event.	jira.system.event.type
12	\N	Work Stopped On Issue	This is the 'work stopped on issue' event.	jira.system.event.type
13	\N	Generic Event	This is the 'generic event' event.	jira.system.event.type
\.


--
-- Data for Name: jiraissue; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiraissue (id, pkey, issuenum, project, reporter, assignee, creator, issuetype, summary, description, environment, priority, resolution, issuestatus, created, updated, duedate, resolutiondate, votes, watches, timeoriginalestimate, timeestimate, timespent, workflow_id, security, fixfor, component) FROM stdin;
\.


--
-- Data for Name: jiraperms; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiraperms (id, permtype, projectid, groupname) FROM stdin;
\.


--
-- Data for Name: jiraworkflows; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jiraworkflows (id, workflowname, creatorname, descriptor, islocked) FROM stdin;
10000	classic default workflow	\N	<?xml version="1.0" encoding="UTF-8"?>\n<!DOCTYPE workflow PUBLIC "-//OpenSymphony Group//DTD OSWorkflow 2.8//EN" "http://www.opensymphony.com/osworkflow/workflow_2_8.dtd">\n<workflow>\n  <meta name="jira.description">The classic JIRA default workflow</meta>\n  <initial-actions>\n    <action id="1" name="Create Issue">\n      <meta name="opsbar-sequence">0</meta>\n      <meta name="jira.i18n.title">common.forms.create</meta>\n      <validators>\n        <validator name="" type="class">\n          <arg name="class.name">com.atlassian.jira.workflow.validator.PermissionValidator</arg>\n          <arg name="permission">Create Issue</arg>\n        </validator>\n      </validators>\n      <results>\n        <unconditional-result old-status="Finished" status="Open" step="1">\n          <post-functions>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueCreateFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n              <arg name="eventTypeId">1</arg>\n            </function>\n          </post-functions>\n        </unconditional-result>\n      </results>\n    </action>\n  </initial-actions>\n  <common-actions>\n    <action id="2" name="Close Issue" view="resolveissue">\n      <meta name="opsbar-sequence">60</meta>\n      <meta name="jira.i18n.submit">closeissue.close</meta>\n      <meta name="jira.i18n.description">closeissue.desc</meta>\n      <meta name="jira.i18n.title">closeissue.title</meta>\n      <restrict-to>\n        <conditions type="AND">\n          <condition type="class">\n            <arg name="class.name">com.atlassian.jira.workflow.condition.PermissionCondition</arg>\n            <arg name="permission">Resolve Issue</arg>\n          </condition>\n          <condition type="class">\n            <arg name="class.name">com.atlassian.jira.workflow.condition.PermissionCondition</arg>\n            <arg name="permission">Close Issue</arg>\n          </condition>\n        </conditions>\n      </restrict-to>\n      <results>\n        <unconditional-result old-status="Finished" status="Closed" step="6">\n          <post-functions>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n              <arg name="eventTypeId">5</arg>\n            </function>\n          </post-functions>\n        </unconditional-result>\n      </results>\n    </action>\n    <action id="3" name="Reopen Issue" view="commentassign">\n      <meta name="opsbar-sequence">80</meta>\n      <meta name="jira.i18n.submit">issue.operations.reopen.issue</meta>\n      <meta name="jira.i18n.description">issue.operations.reopen.description</meta>\n      <meta name="jira.i18n.title">issue.operations.reopen.issue</meta>\n      <restrict-to>\n        <conditions>\n          <condition type="class">\n            <arg name="class.name">com.atlassian.jira.workflow.condition.PermissionCondition</arg>\n            <arg name="permission">Resolve Issue</arg>\n          </condition>\n        </conditions>\n      </restrict-to>\n      <results>\n        <unconditional-result old-status="Finished" status="Reopened" step="5">\n          <post-functions>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction</arg>\n              <arg name="field.value"></arg>\n              <arg name="field.name">resolution</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n              <arg name="eventTypeId">7</arg>\n            </function>\n          </post-functions>\n        </unconditional-result>\n      </results>\n    </action>\n    <action id="4" name="Start Progress">\n      <meta name="opsbar-sequence">20</meta>\n      <meta name="jira.i18n.title">startprogress.title</meta>\n      <restrict-to>\n        <conditions>\n          <condition type="class">\n            <arg name="class.name">com.atlassian.jira.workflow.condition.AllowOnlyAssignee</arg>\n          </condition>\n        </conditions>\n      </restrict-to>\n      <results>\n        <unconditional-result old-status="Finished" status="Underway" step="3">\n          <post-functions>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction</arg>\n              <arg name="field.value"></arg>\n              <arg name="field.name">resolution</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n              <arg name="eventTypeId">11</arg>\n            </function>\n          </post-functions>\n        </unconditional-result>\n      </results>\n    </action>\n    <action id="5" name="Resolve Issue" view="resolveissue">\n      <meta name="opsbar-sequence">40</meta>\n      <meta name="jira.i18n.submit">resolveissue.resolve</meta>\n      <meta name="jira.i18n.description">resolveissue.desc.line1</meta>\n      <meta name="jira.i18n.title">resolveissue.title</meta>\n      <restrict-to>\n        <conditions>\n          <condition type="class">\n            <arg name="class.name">com.atlassian.jira.workflow.condition.PermissionCondition</arg>\n            <arg name="permission">Resolve Issue</arg>\n          </condition>\n        </conditions>\n      </restrict-to>\n      <results>\n        <unconditional-result old-status="Finished" status="Resolved" step="4">\n          <post-functions>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n            </function>\n            <function type="class">\n              <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n              <arg name="eventTypeId">4</arg>\n            </function>\n          </post-functions>\n        </unconditional-result>\n      </results>\n    </action>\n  </common-actions>\n  <steps>\n    <step id="1" name="Open">\n      <meta name="jira.status.id">1</meta>\n      <actions>\n<common-action id="4" />\n<common-action id="5" />\n<common-action id="2" />\n      </actions>\n    </step>\n    <step id="3" name="In Progress">\n      <meta name="jira.status.id">3</meta>\n      <actions>\n<common-action id="5" />\n<common-action id="2" />\n        <action id="301" name="Stop Progress">\n          <meta name="opsbar-sequence">20</meta>\n          <meta name="jira.i18n.title">stopprogress.title</meta>\n          <restrict-to>\n            <conditions>\n              <condition type="class">\n                <arg name="class.name">com.atlassian.jira.workflow.condition.AllowOnlyAssignee</arg>\n              </condition>\n            </conditions>\n          </restrict-to>\n          <results>\n            <unconditional-result old-status="Finished" status="Assigned" step="1">\n              <post-functions>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueFieldFunction</arg>\n                  <arg name="field.value"></arg>\n                  <arg name="field.name">resolution</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n                  <arg name="eventTypeId">12</arg>\n                </function>\n              </post-functions>\n            </unconditional-result>\n          </results>\n        </action>\n      </actions>\n    </step>\n    <step id="4" name="Resolved">\n      <meta name="jira.status.id">5</meta>\n      <actions>\n<common-action id="3" />\n        <action id="701" name="Close Issue" view="commentassign">\n          <meta name="opsbar-sequence">60</meta>\n          <meta name="jira.i18n.submit">closeissue.close</meta>\n          <meta name="jira.i18n.description">closeissue.desc</meta>\n          <meta name="jira.i18n.title">closeissue.title</meta>\n          <meta name="jira.description">Closing an issue indicates there is no more work to be done on it, and it has been verified as complete.</meta>\n          <restrict-to>\n            <conditions>\n              <condition type="class">\n                <arg name="class.name">com.atlassian.jira.workflow.condition.PermissionCondition</arg>\n                <arg name="permission">Close Issue</arg>\n              </condition>\n            </conditions>\n          </restrict-to>\n          <results>\n            <unconditional-result old-status="Finished" status="Closed" step="6">\n              <post-functions>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.UpdateIssueStatusFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.misc.CreateCommentFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.GenerateChangeHistoryFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.issue.IssueReindexFunction</arg>\n                </function>\n                <function type="class">\n                  <arg name="class.name">com.atlassian.jira.workflow.function.event.FireIssueEventFunction</arg>\n                  <arg name="eventTypeId">5</arg>\n                </function>\n              </post-functions>\n            </unconditional-result>\n          </results>\n        </action>\n      </actions>\n    </step>\n    <step id="5" name="Reopened">\n      <meta name="jira.status.id">4</meta>\n      <actions>\n<common-action id="5" />\n<common-action id="2" />\n<common-action id="4" />\n      </actions>\n    </step>\n    <step id="6" name="Closed">\n      <meta name="jira.status.id">6</meta>\n      <meta name="jira.issue.editable">false</meta>\n      <actions>\n<common-action id="3" />\n      </actions>\n    </step>\n  </steps>\n</workflow>\n	\N
\.


--
-- Data for Name: jquartz_blob_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_blob_triggers (sched_name, trigger_name, trigger_group, blob_data) FROM stdin;
\.


--
-- Data for Name: jquartz_calendars; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_calendars (sched_name, calendar_name, calendar) FROM stdin;
\.


--
-- Data for Name: jquartz_cron_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_cron_triggers (sched_name, trigger_name, trigger_group, cron_expression, time_zone_id) FROM stdin;
\.


--
-- Data for Name: jquartz_fired_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_fired_triggers (sched_name, entry_id, trigger_name, trigger_group, is_volatile, instance_name, fired_time, sched_time, priority, state, job_name, job_group, is_stateful, is_nonconcurrent, is_update_data, requests_recovery) FROM stdin;
\.


--
-- Data for Name: jquartz_job_details; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_job_details (sched_name, job_name, job_group, description, job_class_name, is_durable, is_volatile, is_stateful, is_nonconcurrent, is_update_data, requests_recovery, job_data) FROM stdin;
\N	com.atlassian.jira.service.DefaultServiceManager	SchedulerServiceJobs	\N	com.atlassian.scheduler.quartz1.Quartz1Job	f	f	f	\N	\N	f	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c770800000010000000007800
\N	CompatibilityPluginScheduler.JobRunnerKey.com.atlassian.jira.plugins.dvcs.ondemand.BitbucketAccountsReloadJobScheduler	SchedulerServiceJobs	\N	com.atlassian.scheduler.quartz1.Quartz1Job	f	f	f	\N	\N	f	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787000737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c770800000010000000007800
\.


--
-- Data for Name: jquartz_job_listeners; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_job_listeners (job_name, job_group, job_listener) FROM stdin;
\.


--
-- Data for Name: jquartz_locks; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_locks (sched_name, lock_name) FROM stdin;
\N	TRIGGER_ACCESS
\N	JOB_ACCESS
\N	CALENDAR_ACCESS
\N	STATE_ACCESS
\N	MISFIRE_ACCESS
\.


--
-- Data for Name: jquartz_paused_trigger_grps; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_paused_trigger_grps (sched_name, trigger_group) FROM stdin;
\.


--
-- Data for Name: jquartz_scheduler_state; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_scheduler_state (sched_name, instance_name, last_checkin_time, checkin_interval) FROM stdin;
\.


--
-- Data for Name: jquartz_simple_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_simple_triggers (sched_name, trigger_name, trigger_group, repeat_count, repeat_interval, times_triggered) FROM stdin;
\N	com.atlassian.jira.service.JiraService:10200	SchedulerServiceTriggers	-1	86400000	1
\N	com.atlassian.jira.service.JiraService:10001	SchedulerServiceTriggers	-1	43200000	1
\N	CompatibilityPluginScheduler.JobId.bitbucket-accounts-reload	SchedulerServiceTriggers	-1	100000000000020000	1
\N	com.atlassian.jira.service.JiraService:10000	SchedulerServiceTriggers	-1	60000	2
\.


--
-- Data for Name: jquartz_simprop_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_simprop_triggers (sched_name, trigger_name, trigger_group, str_prop_1, str_prop_2, str_prop_3, int_prop_1, int_prop_2, long_prop_1, long_prop_2, dec_prop_1, dec_prop_2, bool_prop_1, bool_prop_2) FROM stdin;
\.


--
-- Data for Name: jquartz_trigger_listeners; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_trigger_listeners (trigger_name, trigger_group, trigger_listener) FROM stdin;
\.


--
-- Data for Name: jquartz_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY jquartz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group, is_volatile, description, next_fire_time, prev_fire_time, priority, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr, job_data) FROM stdin;
\N	com.atlassian.jira.service.JiraService:10200	SchedulerServiceTriggers	com.atlassian.jira.service.DefaultServiceManager	SchedulerServiceJobs	f	\N	1396610268194	1396523868194	5	WAITING	SIMPLE	1396523868194	0	\N	0	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787001737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000174000a706172616d6574657273757200025b42acf317f8060854e002000078700000012baced000573720035636f6d2e676f6f676c652e636f6d6d6f6e2e636f6c6c6563742e496d6d757461626c654d61702453657269616c697a6564466f726d00000000000000000200025b00046b6579737400135b4c6a6176612f6c616e672f4f626a6563743b5b000676616c75657371007e00017870757200135b4c6a6176612e6c616e672e4f626a6563743b90ce589f1073296c020000787000000001740033636f6d2e61746c61737369616e2e6a6972612e736572766963652e536572766963654d616e616765723a7365727669636549647571007e0003000000017372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000000000000027d87800
\N	com.atlassian.jira.service.JiraService:10001	SchedulerServiceTriggers	com.atlassian.jira.service.DefaultServiceManager	SchedulerServiceJobs	f	\N	1396567078229	1396523878229	5	WAITING	SIMPLE	1396523878229	0	\N	0	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787001737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000174000a706172616d6574657273757200025b42acf317f8060854e002000078700000012baced000573720035636f6d2e676f6f676c652e636f6d6d6f6e2e636f6c6c6563742e496d6d757461626c654d61702453657269616c697a6564466f726d00000000000000000200025b00046b6579737400135b4c6a6176612f6c616e672f4f626a6563743b5b000676616c75657371007e00017870757200135b4c6a6176612e6c616e672e4f626a6563743b90ce589f1073296c020000787000000001740033636f6d2e61746c61737369616e2e6a6972612e736572766963652e536572766963654d616e616765723a7365727669636549647571007e0003000000017372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000000000000027117800
\N	CompatibilityPluginScheduler.JobId.bitbucket-accounts-reload	SchedulerServiceTriggers	CompatibilityPluginScheduler.JobRunnerKey.com.atlassian.jira.plugins.dvcs.ondemand.BitbucketAccountsReloadJobScheduler	SchedulerServiceJobs	f	\N	100001396523956546	1396523936546	5	WAITING	SIMPLE	1396523936546	0	\N	0	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787001737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000174000a706172616d6574657273707800
\N	com.atlassian.jira.service.JiraService:10000	SchedulerServiceTriggers	com.atlassian.jira.service.DefaultServiceManager	SchedulerServiceJobs	f	\N	1396524004918	1396523944918	5	WAITING	SIMPLE	1396523884918	0	\N	0	\\xaced0005737200156f72672e71756172747a2e4a6f62446174614d61709fb083e8bfa9b0cb020000787200266f72672e71756172747a2e7574696c732e537472696e674b65794469727479466c61674d61708208e8c3fbc55d280200015a0013616c6c6f77735472616e7369656e74446174617872001d6f72672e71756172747a2e7574696c732e4469727479466c61674d617013e62ead28760ace0200025a000564697274794c00036d617074000f4c6a6176612f7574696c2f4d61703b787001737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f4000000000000c7708000000100000000174000a706172616d6574657273757200025b42acf317f8060854e002000078700000012baced000573720035636f6d2e676f6f676c652e636f6d6d6f6e2e636f6c6c6563742e496d6d757461626c654d61702453657269616c697a6564466f726d00000000000000000200025b00046b6579737400135b4c6a6176612f6c616e672f4f626a6563743b5b000676616c75657371007e00017870757200135b4c6a6176612e6c616e672e4f626a6563743b90ce589f1073296c020000787000000001740033636f6d2e61746c61737369616e2e6a6972612e736572766963652e536572766963654d616e616765723a7365727669636549647571007e0003000000017372000e6a6176612e6c616e672e4c6f6e673b8be490cc8f23df0200014a000576616c7565787200106a6176612e6c616e672e4e756d62657286ac951d0b94e08b020000787000000000000027107800
\.


--
-- Data for Name: label; Type: TABLE DATA; Schema: public; Owner: -
--

COPY label (id, fieldid, issue, label) FROM stdin;
\.


--
-- Data for Name: listenerconfig; Type: TABLE DATA; Schema: public; Owner: -
--

COPY listenerconfig (id, clazz, listenername) FROM stdin;
10000	com.atlassian.jira.event.listeners.mail.MailListener	Mail Listener
10001	com.atlassian.jira.event.listeners.history.IssueAssignHistoryListener	Issue Assignment Listener
10002	com.atlassian.jira.event.listeners.search.IssueIndexListener	Issue Index Listener
10100	com.atlassian.jira.event.listeners.search.IssueIndexListener	Issue Index Listener
\.


--
-- Data for Name: mailserver; Type: TABLE DATA; Schema: public; Owner: -
--

COPY mailserver (id, name, description, mailfrom, prefix, smtp_port, protocol, server_type, servername, jndilocation, mailusername, mailpassword, istlsrequired, timeout, socks_port, socks_host) FROM stdin;
\.


--
-- Data for Name: managedconfigurationitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY managedconfigurationitem (id, item_id, item_type, managed, access_level, source, description_key) FROM stdin;
\.


--
-- Data for Name: membershipbase; Type: TABLE DATA; Schema: public; Owner: -
--

COPY membershipbase (id, user_name, group_name) FROM stdin;
\.


--
-- Data for Name: moved_issue_key; Type: TABLE DATA; Schema: public; Owner: -
--

COPY moved_issue_key (id, old_issue_key, issue_id) FROM stdin;
\.


--
-- Data for Name: nodeassociation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY nodeassociation (source_node_id, source_node_entity, sink_node_id, sink_node_entity, association_type, sequence) FROM stdin;
\.


--
-- Data for Name: nodeindexcounter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY nodeindexcounter (id, node_id, sending_node_id, index_operation_id) FROM stdin;
\.


--
-- Data for Name: notification; Type: TABLE DATA; Schema: public; Owner: -
--

COPY notification (id, scheme, event, event_type_id, template_id, notif_type, notif_parameter) FROM stdin;
10000	10000	\N	1	\N	Current_Assignee	\N
10001	10000	\N	1	\N	Current_Reporter	\N
10002	10000	\N	1	\N	All_Watchers	\N
10003	10000	\N	2	\N	Current_Assignee	\N
10004	10000	\N	2	\N	Current_Reporter	\N
10005	10000	\N	2	\N	All_Watchers	\N
10006	10000	\N	3	\N	Current_Assignee	\N
10007	10000	\N	3	\N	Current_Reporter	\N
10008	10000	\N	3	\N	All_Watchers	\N
10009	10000	\N	4	\N	Current_Assignee	\N
10010	10000	\N	4	\N	Current_Reporter	\N
10011	10000	\N	4	\N	All_Watchers	\N
10012	10000	\N	5	\N	Current_Assignee	\N
10013	10000	\N	5	\N	Current_Reporter	\N
10014	10000	\N	5	\N	All_Watchers	\N
10015	10000	\N	6	\N	Current_Assignee	\N
10016	10000	\N	6	\N	Current_Reporter	\N
10017	10000	\N	6	\N	All_Watchers	\N
10018	10000	\N	7	\N	Current_Assignee	\N
10019	10000	\N	7	\N	Current_Reporter	\N
10020	10000	\N	7	\N	All_Watchers	\N
10021	10000	\N	8	\N	Current_Assignee	\N
10022	10000	\N	8	\N	Current_Reporter	\N
10023	10000	\N	8	\N	All_Watchers	\N
10024	10000	\N	9	\N	Current_Assignee	\N
10025	10000	\N	9	\N	Current_Reporter	\N
10026	10000	\N	9	\N	All_Watchers	\N
10027	10000	\N	10	\N	Current_Assignee	\N
10028	10000	\N	10	\N	Current_Reporter	\N
10029	10000	\N	10	\N	All_Watchers	\N
10030	10000	\N	11	\N	Current_Assignee	\N
10031	10000	\N	11	\N	Current_Reporter	\N
10032	10000	\N	11	\N	All_Watchers	\N
10033	10000	\N	12	\N	Current_Assignee	\N
10034	10000	\N	12	\N	Current_Reporter	\N
10035	10000	\N	12	\N	All_Watchers	\N
10036	10000	\N	13	\N	Current_Assignee	\N
10037	10000	\N	13	\N	Current_Reporter	\N
10038	10000	\N	13	\N	All_Watchers	\N
10100	10000	\N	14	\N	Current_Assignee	\N
10101	10000	\N	14	\N	Current_Reporter	\N
10102	10000	\N	14	\N	All_Watchers	\N
10103	10000	\N	15	\N	Current_Assignee	\N
10104	10000	\N	15	\N	Current_Reporter	\N
10105	10000	\N	15	\N	All_Watchers	\N
10106	10000	\N	16	\N	Current_Assignee	\N
10107	10000	\N	16	\N	Current_Reporter	\N
10108	10000	\N	16	\N	All_Watchers	\N
\.


--
-- Data for Name: notificationinstance; Type: TABLE DATA; Schema: public; Owner: -
--

COPY notificationinstance (id, notificationtype, source, emailaddress, messageid) FROM stdin;
\.


--
-- Data for Name: notificationscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY notificationscheme (id, name, description) FROM stdin;
10000	Default Notification Scheme	\N
\.


--
-- Data for Name: oauthconsumer; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauthconsumer (id, created, consumername, consumer_key, consumerservice, public_key, private_key, description, callback, signature_method, shared_secret) FROM stdin;
\.


--
-- Data for Name: oauthconsumertoken; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauthconsumertoken (id, created, token_key, token, token_secret, token_type, consumer_key) FROM stdin;
\.


--
-- Data for Name: oauthspconsumer; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauthspconsumer (id, created, consumer_key, consumername, public_key, description, callback, two_l_o_allowed, executing_two_l_o_user, two_l_o_impersonation_allowed, three_l_o_allowed) FROM stdin;
\.


--
-- Data for Name: oauthsptoken; Type: TABLE DATA; Schema: public; Owner: -
--

COPY oauthsptoken (id, created, token, token_secret, token_type, consumer_key, username, ttl, spauth, callback, spverifier, spversion, session_handle, session_creation_time, session_last_renewal_time, session_time_to_live) FROM stdin;
\.


--
-- Data for Name: optionconfiguration; Type: TABLE DATA; Schema: public; Owner: -
--

COPY optionconfiguration (id, fieldid, optionid, fieldconfig, sequence) FROM stdin;
10100	issuetype	1	10000	0
10101	issuetype	2	10000	1
10102	issuetype	3	10000	2
10103	issuetype	4	10000	3
10104	issuetype	5	10000	4
\.


--
-- Data for Name: os_currentstep; Type: TABLE DATA; Schema: public; Owner: -
--

COPY os_currentstep (id, entry_id, step_id, action_id, owner, start_date, due_date, finish_date, status, caller) FROM stdin;
\.


--
-- Data for Name: os_currentstep_prev; Type: TABLE DATA; Schema: public; Owner: -
--

COPY os_currentstep_prev (id, previous_id) FROM stdin;
\.


--
-- Data for Name: os_historystep; Type: TABLE DATA; Schema: public; Owner: -
--

COPY os_historystep (id, entry_id, step_id, action_id, owner, start_date, due_date, finish_date, status, caller) FROM stdin;
\.


--
-- Data for Name: os_historystep_prev; Type: TABLE DATA; Schema: public; Owner: -
--

COPY os_historystep_prev (id, previous_id) FROM stdin;
\.


--
-- Data for Name: os_wfentry; Type: TABLE DATA; Schema: public; Owner: -
--

COPY os_wfentry (id, name, initialized, state) FROM stdin;
\.


--
-- Data for Name: permissionscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY permissionscheme (id, name, description) FROM stdin;
0	Default Permission Scheme	This is the default Permission Scheme. Any new projects that are created will be assigned this scheme.
\.


--
-- Data for Name: pluginstate; Type: TABLE DATA; Schema: public; Owner: -
--

COPY pluginstate (pluginkey, pluginenabled) FROM stdin;
\.


--
-- Data for Name: pluginversion; Type: TABLE DATA; Schema: public; Owner: -
--

COPY pluginversion (id, pluginname, pluginkey, pluginversion, created) FROM stdin;
10000	ActiveObjects Plugin - OSGi Bundle	com.atlassian.activeobjects.activeobjects-plugin	0.23.3	2014-04-03 22:18:13.082+11
10001	JIRA Active Objects SPI implementation	com.atlassian.activeobjects.jira.spi	0.23.3	2014-04-03 22:18:13.092+11
10002	Atlassian - Administration - Quick Search - JIRA	com.atlassian.administration.atlassian-admin-quicksearch-jira	1.5	2014-04-03 22:18:13.112+11
10003	Analytics Client Plugin	com.atlassian.analytics.analytics-client	3.10	2014-04-03 22:18:13.129+11
10004	Applinks Product Plugin	com.atlassian.applinks.applinks-plugin	4.2.0	2014-04-03 22:18:13.191+11
10005	Atlassian Docco	com.atlassian.atl-docco	0.11	2014-04-03 22:18:13.195+11
10006	atlassian-failure-cache-plugin	com.atlassian.atlassian-failure-cache-plugin	0.14	2014-04-03 22:18:13.199+11
10007	Atlassian UI Plugin	com.atlassian.auiplugin	5.4.3	2014-04-03 22:18:13.207+11
10008	ICU4J	com.atlassian.bundles.icu4j-3.8.0.1	3.8.0.1	2014-04-03 22:18:13.211+11
10009	JSON Library	com.atlassian.bundles.json-20070829.0.0.1	20070829.0.0.1	2014-04-03 22:18:13.214+11
10010	Neko HTML	com.atlassian.bundles.nekohtml-1.9.12.1	1.9.12.1	2014-04-03 22:18:13.216+11
10011	Atlassian Embedded Crowd - Administration Plugin	com.atlassian.crowd.embedded.admin	1.7.3	2014-04-03 22:18:13.232+11
10012	Functional Test Plugin	com.atlassian.functest.functest-plugin	0.6.2	2014-04-03 22:18:13.245+11
10013	Gadget Dashboard Plugin	com.atlassian.gadgets.dashboard	3.3.4	2014-04-03 22:18:13.261+11
10014	Gadget Directory Plugin	com.atlassian.gadgets.directory	3.3.4	2014-04-03 22:18:13.272+11
10015	Embedded Gadgets Plugin	com.atlassian.gadgets.embedded	3.3.4	2014-04-03 22:18:13.276+11
10016	Atlassian Gadgets OAuth Service Provider Plugin	com.atlassian.gadgets.oauth.serviceprovider	3.3.4	2014-04-03 22:18:13.281+11
10017	Opensocial Plugin	com.atlassian.gadgets.opensocial	3.3.4	2014-04-03 22:18:13.288+11
10018	Gadget Spec Publisher Plugin	com.atlassian.gadgets.publisher	3.3.4	2014-04-03 22:18:13.303+11
10019	Atlassian HealthCheck Common Module	com.atlassian.healthcheck.atlassian-healthcheck	2.0.7	2014-04-03 22:18:13.317+11
10020	HipChat Core Plugin	com.atlassian.hipchat.plugins.core	0.8.3	2014-04-03 22:18:13.328+11
10021	Atlassian HTTP Client, Apache HTTP components impl	com.atlassian.httpclient.atlassian-httpclient-plugin	0.17.3	2014-04-03 22:18:13.331+11
10022	JIRA Core Project Templates Plugin	com.atlassian.jira-core-project-templates	2.38	2014-04-03 22:18:13.352+11
10023	JIRA Issue Collector Plugin	com.atlassian.jira.collector.plugin.jira-issue-collector-plugin	1.4.10	2014-04-03 22:18:13.367+11
10024	Atlassian JIRA - Plugins - DevMode - Func Test Plugin	com.atlassian.jira.dev.func-test-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.402+11
10025	RPC JIRA Plugin	com.atlassian.jira.ext.rpc	6.0.5	2014-04-03 22:18:13.407+11
10026	JIRA iCalendar Plugin	com.atlassian.jira.extra.jira-ical-feed	1.0.16	2014-04-03 22:18:13.417+11
10027	Atlassian JIRA - Plugins - Gadgets Plugin	com.atlassian.jira.gadgets	6.3-SNAPSHOT	2014-04-03 22:18:13.433+11
10028	Atlassian JIRA - Plugins - Admin Navigation Component	com.atlassian.jira.jira-admin-navigation-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.438+11
10029	Atlassian JIRA - Plugins - Application Properties	com.atlassian.jira.jira-application-properties-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.441+11
10030	JIRA Base URL Plugin	com.atlassian.jira.jira-baseurl-plugin	1.9	2014-04-03 22:18:13.457+11
10031	JIRA Feedback Plugin	com.atlassian.jira.jira-feedback-plugin	1.12	2014-04-03 22:18:13.463+11
10032	Atlassian JIRA - Plugins - Header Plugin	com.atlassian.jira.jira-header-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.499+11
10033	Atlassian JIRA - Plugins - Invite User	com.atlassian.jira.jira-invite-user-plugin	1.16	2014-04-03 22:18:13.51+11
10034	Atlassian JIRA - Plugins - Common AppLinks Based Issue Link Plugin	com.atlassian.jira.jira-issue-link-applinks-common-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.524+11
10035	Atlassian JIRA - Plugins - Confluence Link	com.atlassian.jira.jira-issue-link-confluence-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.545+11
10036	Atlassian JIRA - Plugins - Remote JIRA Link	com.atlassian.jira.jira-issue-link-remote-jira-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.557+11
10037	Atlassian JIRA - Plugins - Issue Web Link	com.atlassian.jira.jira-issue-link-web-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.561+11
10038	jira-issue-nav-components	com.atlassian.jira.jira-issue-nav-components	6.3.4	2014-04-03 22:18:13.582+11
10039	Atlassian JIRA - Plugins - Issue Navigation	com.atlassian.jira.jira-issue-nav-plugin	6.3.4	2014-04-03 22:18:13.595+11
10040	English (Antarctica) Language Pack	com.atlassian.jira.jira-languages.en_AQ	6.3-SNAPSHOT	2014-04-03 22:18:13.6+11
10041	English (United Kingdom) Language Pack	com.atlassian.jira.jira-languages.en_UK	6.3-SNAPSHOT	2014-04-03 22:18:13.605+11
10042	English (United States) Language Pack	com.atlassian.jira.jira-languages.en_US	6.3-SNAPSHOT	2014-04-03 22:18:13.608+11
10043	Atlassian JIRA - Plugins - Mail Plugin	com.atlassian.jira.jira-mail-plugin	6.2.1	2014-04-03 22:18:13.638+11
10044	JIRA Monitoring Plugin	com.atlassian.jira.jira-monitoring-plugin	05.6.1	2014-04-03 22:18:13.644+11
10045	Atlassian JIRA - Plugins - My JIRA Home	com.atlassian.jira.jira-my-home-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.671+11
10046	JIRA Project Config Plugin	com.atlassian.jira.jira-project-config-plugin	6.3.34	2014-04-03 22:18:13.758+11
10047	JIRA Projects Plugin	com.atlassian.jira.jira-projects-plugin	1.0.29	2014-04-03 22:18:13.766+11
10048	Atlassian JIRA - Plugins - Quick Edit Plugin	com.atlassian.jira.jira-quick-edit-plugin	1.0.88	2014-04-03 22:18:13.777+11
10049	Atlassian JIRA - Plugins - Share Content Component	com.atlassian.jira.jira-share-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.801+11
10050	Atlassian JIRA - Plugins - Closure Template Renderer	com.atlassian.jira.jira-soy-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.804+11
10051	JIRA Time Zone Detection plugin	com.atlassian.jira.jira-tzdetect-plugin	1.8	2014-04-03 22:18:13.816+11
10052	Atlassian JIRA - Plugins - User Profile Plugin	com.atlassian.jira.jira-user-profile-plugin	2.0.1	2014-04-03 22:18:13.835+11
10053	Atlassian JIRA - Plugins - View Issue Panels	com.atlassian.jira.jira-view-issue-plugin	6.3-SNAPSHOT	2014-04-03 22:18:13.871+11
10054	Atlassian JIRA - Plugins - Look And Feel Logo Upload Plugin	com.atlassian.jira.lookandfeel	6.3-SNAPSHOT	2014-04-03 22:18:13.884+11
10055	JIRA Mobile	com.atlassian.jira.mobile	1.6	2014-04-03 22:18:13.903+11
10056	Atlassian JIRA - Plugins - OAuth Consumer SPI	com.atlassian.jira.oauth.consumer	6.3-SNAPSHOT	2014-04-03 22:18:13.908+11
10057	Atlassian JIRA - Plugins - OAuth Service Provider SPI	com.atlassian.jira.oauth.serviceprovider	6.3-SNAPSHOT	2014-04-03 22:18:13.911+11
10058	JIRA Bamboo Plugin	com.atlassian.jira.plugin.ext.bamboo	7.1.10	2014-04-03 22:18:13.935+11
10059	Comment Panel Plugin	com.atlassian.jira.plugin.system.comment-panel	1.0	2014-04-03 22:18:13.94+11
10060	Custom Field Types & Searchers	com.atlassian.jira.plugin.system.customfieldtypes	1.0	2014-04-03 22:18:13.946+11
10061	Issue Operations Plugin	com.atlassian.jira.plugin.system.issueoperations	1.0	2014-04-03 22:18:13.957+11
10062	Issue Tab Panels Plugin	com.atlassian.jira.plugin.system.issuetabpanels	1.0	2014-04-03 22:18:13.962+11
10063	Renderer Plugin	com.atlassian.jira.plugin.system.jirarenderers	1.0	2014-04-03 22:18:13.967+11
10064	Project Role Actors Plugin	com.atlassian.jira.plugin.system.projectroleactors	1.0	2014-04-03 22:18:13.972+11
10065	Wiki Renderer Macros Plugin	com.atlassian.jira.plugin.system.renderers.wiki.macros	1.0	2014-04-03 22:18:13.976+11
10066	Reports Plugin	com.atlassian.jira.plugin.system.reports	1.0	2014-04-03 22:18:13.981+11
10067	Workflow Plugin	com.atlassian.jira.plugin.system.workflow	1.0	2014-04-03 22:18:13.988+11
10068	JIRA Workflow Transition Tabs	com.atlassian.jira.plugin.system.workfloweditor.transition.tabs	1.0	2014-04-03 22:18:13.994+11
10069	Content Link Resolvers Plugin	com.atlassian.jira.plugin.wiki.contentlinkresolvers	1.0	2014-04-03 22:18:13.999+11
10070	Renderer Component Factories Plugin	com.atlassian.jira.plugin.wiki.renderercomponentfactories	1.0	2014-04-03 22:18:14.009+11
10071	JIRA Agile Marketing Plugin	com.atlassian.jira.plugins.greenhopper-marketing-plugin	1.0.14	2014-04-03 22:18:14.04+11
10072	Atlassian JIRA - Admin Helper Plugin	com.atlassian.jira.plugins.jira-admin-helper-plugin	1.18.3	2014-04-03 22:18:14.06+11
10073	JIRA Auditing Plugin	com.atlassian.jira.plugins.jira-auditing-plugin	1.2.0	2014-04-03 22:18:14.073+11
10074	JIRA DVCS Connector Plugin	com.atlassian.jira.plugins.jira-bitbucket-connector-plugin	2.1.2	2014-04-03 22:18:14.106+11
10075	Atlassian JIRA - Plugins - Development Integration Plugin	com.atlassian.jira.plugins.jira-development-integration-plugin	2.0.0006	2014-04-03 22:18:14.147+11
10076	Atlassian JIRA - Plugins - Healthcheck Plugin	com.atlassian.jira.plugins.jira-healthcheck-plugin	1.0.10	2014-04-03 22:18:14.156+11
10077	Bitbucket Importer Plugin for JIM	com.atlassian.jira.plugins.jira-importers-bitbucket-plugin	1.0.7	2014-04-03 22:18:14.173+11
10078	JIRA GitHub Issue Importer	com.atlassian.jira.plugins.jira-importers-github-plugin	2.0.0	2014-04-03 22:18:14.191+11
10079	JIRA Importers Plugin (JIM)	com.atlassian.jira.plugins.jira-importers-plugin	6.1.3	2014-04-03 22:18:14.216+11
10080	Redmine Importers Plugin for JIM	com.atlassian.jira.plugins.jira-importers-redmine-plugin	2.0.5	2014-04-03 22:18:14.221+11
10081	JIRA Password Policy Plugin	com.atlassian.jira.plugins.jira-password-policy-plugin	1.1.2	2014-04-03 22:18:14.229+11
10082	Atlassian JIRA - Plugins - Transition Trigger Plugin	com.atlassian.jira.plugins.jira-transition-triggers-plugin	2.0.0006	2014-04-03 22:18:14.234+11
10083	JIRA Workflow Designer Plugin	com.atlassian.jira.plugins.jira-workflow-designer	6.3.4	2014-04-03 22:18:14.254+11
10084	JIRA WebHooks Plugin	com.atlassian.jira.plugins.webhooks.jira-webhooks-plugin	1.2.6	2014-04-03 22:18:14.271+11
10085	JIRA Workflow Sharing Plugin	com.atlassian.jira.plugins.workflow.sharing.jira-workflow-sharing-plugin	1.1.25	2014-04-03 22:18:14.292+11
10086	Project Templates Plugin	com.atlassian.jira.project-templates-plugin	2.38	2014-04-03 22:18:14.31+11
10087	Atlassian JIRA - Plugins - REST Plugin	com.atlassian.jira.rest	6.3-SNAPSHOT	2014-04-03 22:18:14.353+11
10088	JIRA JSON-RPC Plugin	com.atlassian.jira.rpc.jira-json-rpc-plugin	1.0.4	2014-04-03 22:18:14.358+11
10089	JIRA TestKit - Plugin	com.atlassian.jira.tests.jira-testkit-plugin	6.3.8	2014-04-03 22:18:14.369+11
10090	JIRA Welcome Plugin	com.atlassian.jira.welcome.jira-welcome-plugin	1.1.49	2014-04-03 22:18:14.397+11
10091	FishEye Plugin	com.atlassian.jirafisheyeplugin	6.3.2	2014-04-03 22:18:14.436+11
10092	Atlassian Bot Session Killer	com.atlassian.labs.atlassian-bot-killer	1.7.5	2014-04-03 22:18:14.441+11
10093	HipChat for JIRA	com.atlassian.labs.hipchat.hipchat-for-jira-plugin	1.2.11	2014-04-03 22:18:14.447+11
10094	Workbox - Common Plugin	com.atlassian.mywork.mywork-common-plugin	1.8	2014-04-03 22:18:14.465+11
10095	Workbox - JIRA Provider Plugin	com.atlassian.mywork.mywork-jira-provider-plugin	1.8	2014-04-03 22:18:14.47+11
10096	Atlassian OAuth Admin Plugin	com.atlassian.oauth.admin	1.9.0-m3	2014-04-03 22:18:14.478+11
10097	Atlassian OAuth Consumer SPI	com.atlassian.oauth.atlassian-oauth-consumer-spi-1.9.0.m3	1.9.0.m3	2014-04-03 22:18:14.483+11
10098	Atlassian OAuth Service Provider SPI	com.atlassian.oauth.atlassian-oauth-service-provider-spi-1.9.0.m3	1.9.0.m3	2014-04-03 22:18:14.487+11
10099	Atlassian OAuth Consumer Plugin	com.atlassian.oauth.consumer	1.9.0-m3	2014-04-03 22:18:14.492+11
10100	Atlassian OAuth Service Provider Plugin	com.atlassian.oauth.serviceprovider	1.9.0-m3	2014-04-03 22:18:14.506+11
10101	Atlassian PDK Install Plugin	com.atlassian.pdkinstall	0.6	2014-04-03 22:18:14.52+11
10102	Atlassian Awareness Capability	com.atlassian.plugins.atlassian-awareness-capability	0.0.5	2014-04-03 22:18:14.539+11
10103	Atlassian Navigation Links Plugin	com.atlassian.plugins.atlassian-nav-links-plugin	3.3.2	2014-04-03 22:18:14.603+11
10104	Atlassian Plugins - Web Resources - Implementation Plugin	com.atlassian.plugins.atlassian-plugins-webresource-plugin	3.0.4	2014-04-03 22:18:14.614+11
10105	Project Creation Capability Product REST Plugin	com.atlassian.plugins.atlassian-project-creation-plugin	1.2.7	2014-04-03 22:18:14.636+11
10106	Atlassian Whitelist API Plugin	com.atlassian.plugins.atlassian-whitelist-api-plugin	1.7	2014-04-03 22:18:14.644+11
10107	Atlassian Whitelist UI Plugin	com.atlassian.plugins.atlassian-whitelist-ui-plugin	1.7	2014-04-03 22:18:14.66+11
10108	browser-metrics-plugin	com.atlassian.plugins.browser.metrics.browser-metrics-plugin	1.18	2014-04-03 22:18:14.665+11
10109	jira-help-tips	com.atlassian.plugins.helptips.jira-help-tips	0.44	2014-04-03 22:18:14.681+11
10110	Issue Status Plugin	com.atlassian.plugins.issue-status-plugin	1.1.6	2014-04-03 22:18:14.686+11
10111	Attach Image for JIRA	com.atlassian.plugins.jira-html5-attach-images	1.3.1	2014-04-03 22:18:14.706+11
10112	Project Creation Plugin SPI for JIRA	com.atlassian.plugins.jira-project-creation	1.2.7	2014-04-03 22:18:14.71+11
10113	JIRA Remote Link Aggregator Plugin	com.atlassian.plugins.jira-remote-link-aggregator-plugin	2.0.9	2014-04-03 22:18:14.716+11
10114	jquery	com.atlassian.plugins.jquery	1.7.2	2014-04-03 22:18:14.72+11
10115	Remote Link Aggregator Plugin	com.atlassian.plugins.remote-link-aggregator-plugin	2.0.9	2014-04-03 22:18:14.737+11
10116	Atlassian REST - Module Types	com.atlassian.plugins.rest.atlassian-rest-module	2.9.4	2014-04-03 22:18:14.743+11
10117	Atlassian Pretty URLs Plugin	com.atlassian.prettyurls.atlassian-pretty-urls-plugin	1.11.0	2014-04-03 22:18:14.749+11
10118	Atlassian QUnit Plugin	com.atlassian.qunit.atlassian-qunit-plugin	0.55	2014-04-03 22:18:14.756+11
10119	Platform Compatibility Testing Kit Plugin	com.atlassian.refapp.ctk	2.19.2	2014-04-03 22:18:14.772+11
10120	Atlassian JIRA - Plugins - SAL Plugin	com.atlassian.sal.jira	6.3-SNAPSHOT	2014-04-03 22:18:14.777+11
10121	scala-provider-plugin	com.atlassian.scala.plugins.scala-2.10-provider-plugin	0.5	2014-04-03 22:18:14.781+11
10122	Atlassian Soy - Plugin	com.atlassian.soy.soy-template-plugin	2.5.1	2014-04-03 22:18:14.787+11
10123	Streams Plugin	com.atlassian.streams	5.3.23	2014-04-03 22:18:14.815+11
10124	Streams Inline Actions Plugin	com.atlassian.streams.actions	5.3.23	2014-04-03 22:18:14.819+11
10125	Streams Core Plugin	com.atlassian.streams.core	5.3.23	2014-04-03 22:18:14.822+11
10126	JIRA Streams Inline Actions Plugin	com.atlassian.streams.jira.inlineactions	5.3.23	2014-04-03 22:18:14.825+11
10127	Streams API	com.atlassian.streams.streams-api-5.3.23	5.3.23	2014-04-03 22:18:14.834+11
10128	JIRA Activity Stream Plugin	com.atlassian.streams.streams-jira-plugin	5.3.23	2014-04-03 22:18:14.881+11
10129	Streams SPI	com.atlassian.streams.streams-spi-5.3.23	5.3.23	2014-04-03 22:18:14.884+11
10130	Streams Third Party Provider Plugin	com.atlassian.streams.streams-thirdparty-plugin	5.3.23	2014-04-03 22:18:14.907+11
10131	Support Tools Plugin	com.atlassian.support.stp	3.5.6	2014-04-03 22:18:14.933+11
10132	Atlassian Template Renderer API	com.atlassian.templaterenderer.api	1.5.4	2014-04-03 22:18:14.944+11
10133	Atlassian Template Renderer Velocity 1.6 Plugin	com.atlassian.templaterenderer.atlassian-template-renderer-velocity1.6-plugin	1.5.4	2014-04-03 22:18:14.949+11
10134	Atlassian Universal Plugin Manager Plugin	com.atlassian.upm.atlassian-universal-plugin-manager-plugin	2.15.2	2014-04-03 22:18:14.986+11
10135	Atlassian WebHooks Plugin	com.atlassian.webhooks.atlassian-webhooks-plugin	0.17.6	2014-04-03 22:18:15.024+11
10136	ROME: RSS/Atom syndication and publishing tools	com.springsource.com.sun.syndication-0.9.0	0.9.0	2014-04-03 22:18:15.246+11
10137	JDOM DOM Processor	com.springsource.org.jdom-1.0.0	1.0.0	2014-04-03 22:18:15.25+11
10138	Crowd REST API	crowd-rest-application-management	1.0	2014-04-03 22:18:15.275+11
10139	Crowd REST API	crowd-rest-plugin	1.0	2014-04-03 22:18:15.296+11
10140	Crowd System Password Encoders	crowd.system.passwordencoders	1.0	2014-04-03 22:18:15.301+11
10141	JIRA Footer	jira.footer	1.0	2014-04-03 22:18:15.303+11
10142	Issue Views Plugin	jira.issueviews	1.0	2014-04-03 22:18:15.308+11
10143	JQL Functions	jira.jql.function	1.0	2014-04-03 22:18:15.313+11
10144	Keyboard Shortcuts Plugin	jira.keyboard.shortcuts	1.0	2014-04-03 22:18:15.318+11
10145	JIRA Global Permissions	jira.system.global.permissions	1.0	2014-04-03 22:18:15.322+11
10146	Top Navigation Bar	jira.top.navigation.bar	1.0	2014-04-03 22:18:15.328+11
10147	JIRA Usage Hints	jira.usage.hints	1.0	2014-04-03 22:18:15.338+11
10148	User Format	jira.user.format	1.0	2014-04-03 22:18:15.342+11
10149	User Profile Panels	jira.user.profile.panels	1.0	2014-04-03 22:18:15.346+11
10150	Admin Menu Sections	jira.webfragments.admin	1.0	2014-04-03 22:18:15.359+11
10151	Browse Project Operations Sections	jira.webfragments.browse.project.links	1.0	2014-04-03 22:18:15.362+11
10152	Preset Filters Sections	jira.webfragments.preset.filters	1.0	2014-04-03 22:18:15.365+11
10153	User Navigation Bar Sections	jira.webfragments.user.navigation.bar	1.0	2014-04-03 22:18:15.369+11
10154	User Profile Links	jira.webfragments.user.profile.links	1.0	2014-04-03 22:18:15.372+11
10155	View Project Operations Sections	jira.webfragments.view.project.operations	1.0	2014-04-03 22:18:15.375+11
10156	Web Resources Plugin	jira.webresources	1.0	2014-04-03 22:18:15.39+11
10157	Apache Commons FileUpload	org.apache.commons.fileupload-1.3.1	1.3.1	2014-04-03 22:18:15.393+11
10158	Apache HttpClient OSGi bundle	org.apache.httpcomponents.httpclient-4.2.5	4.2.5	2014-04-03 22:18:15.397+11
10159	Apache HttpCore OSGi bundle	org.apache.httpcomponents.httpcore-4.2.4	4.2.4	2014-04-03 22:18:15.4+11
10160	Apache ServiceMix :: Bundles :: javax.inject	org.apache.servicemix.bundles.javax-inject-1.0.0.1	1.0.0.1	2014-04-03 22:18:15.407+11
10161	Sisu-Inject	org.eclipse.sisu.inject-0.0.0.atlassian6	0.0.0.atlassian6	2014-04-03 22:18:15.411+11
10162	ASM	org.objectweb.asm-3.3.1.v201105211655	3.3.1.v201105211655	2014-04-03 22:18:15.414+11
10163	sisu-guice	org.sonatype.sisu.guice-3.1.3	3.1.3	2014-04-03 22:18:15.417+11
10164	ROME, RSS and atOM utilitiEs for Java	rome.rome-1.0	1.0	2014-04-03 22:18:15.422+11
10165	JIRA German (Germany) Language Pack	tac.jira.languages.de_DE	6.2.0-v2r8018-2014-03-11	2014-04-03 22:18:15.439+11
10166	JIRA Spanish (Spain) Language Pack	tac.jira.languages.es_ES	6.2.0-v2r17500-2014-03-11	2014-04-03 22:18:15.443+11
10167	JIRA French (France) Language Pack	tac.jira.languages.fr_FR	6.2.0-v2r4377-2014-03-11	2014-04-03 22:18:15.446+11
10168	JIRA Japanese (Japan) Language Pack	tac.jira.languages.ja_JP	6.2.0-v2r19393-2014-03-11	2014-04-03 22:18:15.449+11
\.


--
-- Data for Name: portalpage; Type: TABLE DATA; Schema: public; Owner: -
--

COPY portalpage (id, username, pagename, description, sequence, fav_count, layout, ppversion) FROM stdin;
10000	\N	System Dashboard	\N	0	0	AA	0
\.


--
-- Data for Name: portletconfiguration; Type: TABLE DATA; Schema: public; Owner: -
--

COPY portletconfiguration (id, portalpage, portlet_id, column_number, positionseq, gadget_xml, color) FROM stdin;
10000	10000	\N	0	0	rest/gadgets/1.0/g/com.atlassian.jira.gadgets:introduction-gadget/gadgets/introduction-gadget.xml	\N
10001	10000	\N	0	1	rest/gadgets/1.0/g/com.atlassian.jira.gadgets:admin-gadget/gadgets/admin-gadget.xml	\N
10002	10000	\N	1	0	rest/gadgets/1.0/g/com.atlassian.jira.gadgets:assigned-to-me-gadget/gadgets/assigned-to-me-gadget.xml	\N
10003	10000	\N	1	1	rest/gadgets/1.0/g/com.atlassian.streams.streams-jira-plugin:activitystream-gadget/gadgets/activitystream-gadget.xml	\N
\.


--
-- Data for Name: priority; Type: TABLE DATA; Schema: public; Owner: -
--

COPY priority (id, sequence, pname, description, iconurl, status_color) FROM stdin;
1	1	Blocker	Blocks development and/or testing work, production could not run.	/images/icons/priorities/blocker.png	#cc0000
2	2	Critical	Crashes, loss of data, severe memory leak.	/images/icons/priorities/critical.png	#ff0000
3	3	Major	Major loss of function.	/images/icons/priorities/major.png	#009900
4	4	Minor	Minor loss of function, or other problem where easy workaround is present.	/images/icons/priorities/minor.png	#006600
5	5	Trivial	Cosmetic problem like misspelt words or misaligned text.	/images/icons/priorities/trivial.png	#003300
\.


--
-- Data for Name: project; Type: TABLE DATA; Schema: public; Owner: -
--

COPY project (id, pname, url, lead, description, pkey, pcounter, assigneetype, avatar, originalkey) FROM stdin;
\.


--
-- Data for Name: project_key; Type: TABLE DATA; Schema: public; Owner: -
--

COPY project_key (id, project_id, project_key) FROM stdin;
\.


--
-- Data for Name: projectcategory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projectcategory (id, cname, description) FROM stdin;
\.


--
-- Data for Name: projectrole; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projectrole (id, name, description) FROM stdin;
10000	Users	A project role that represents users in a project
10001	Developers	A project role that represents developers in a project
10002	Administrators	A project role that represents administrators in a project
\.


--
-- Data for Name: projectroleactor; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projectroleactor (id, pid, projectroleid, roletype, roletypeparameter) FROM stdin;
10000	\N	10000	atlassian-group-role-actor	jira-users
10001	\N	10001	atlassian-group-role-actor	jira-developers
10002	\N	10002	atlassian-group-role-actor	jira-administrators
\.


--
-- Data for Name: projectversion; Type: TABLE DATA; Schema: public; Owner: -
--

COPY projectversion (id, project, vname, description, sequence, released, archived, url, startdate, releasedate) FROM stdin;
\.


--
-- Data for Name: propertydata; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertydata (id, propertyvalue) FROM stdin;
\.


--
-- Data for Name: propertydate; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertydate (id, propertyvalue) FROM stdin;
\.


--
-- Data for Name: propertydecimal; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertydecimal (id, propertyvalue) FROM stdin;
\.


--
-- Data for Name: propertyentry; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertyentry (id, entity_name, entity_id, property_key, propertytype) FROM stdin;
6	jira.properties	1	jira.avatar.user.anonymous.id	5
7	jira.properties	1	jira.scheme.default.issue.type	5
8	jira.properties	1	jira.constant.default.resolution	5
9	jira.properties	1	jira.whitelist.disabled	1
10	jira.properties	1	jira.whitelist.rules	6
11	jira.properties	1	jira.option.timetracking	1
12	jira.properties	1	jira.timetracking.estimates.legacy.behaviour	1
1	jira.properties	1	jira.version.patched	5
3	jira.properties	1	jira.avatar.default.id	5
5	jira.properties	1	jira.avatar.user.default.id	5
10400	jira.properties	1	jira.i18n.language.index	5
10401	jira.properties	1	jira.sid.key	5
10403	com.atlassian.jira.plugins.jira-workflow-designer	1	jira.workflow.layout:8a6044147cf2c19c02d099279cfbfd47	6
10404	jira.properties	1	jira.option.allowunassigned	1
10500	jira.properties	1	jira.user.avatar.gravatar.enabled	1
10501	jira.properties	1	jira.version	5
10502	jira.properties	1	jira.downgrade.minimum.version	5
10600	jira.properties	1	jira.webresource.flushcounter	5
10601	jira.properties	1	webwork.i18n.encoding	5
10602	jira.properties	1	jira.title	5
10603	jira.properties	1	jira.baseurl	5
10604	jira.properties	1	jira.mode	5
10605	jira.properties	1	jira.path.index.use.default.directory	1
10606	jira.properties	1	jira.option.indexing	1
10607	jira.properties	1	jira.path.attachments	5
10608	jira.properties	1	jira.path.attachments.use.default.directory	1
10609	jira.properties	1	jira.option.allowattachments	1
10610	ServiceConfig	10001	USE_DEFAULT_DIRECTORY	5
10611	jira.properties	1	jira.path.backup	5
10612	jira.properties	1	License20	6
10613	jira.properties	1	jira.edition	5
10623	jira.properties	1	com.atlassian.activeobjects.admin.ActiveObjectsPluginToTablesMapping	6
10624	jira.properties	1	org.apache.shindig.common.crypto.BlobCrypter:key	5
10625	BambooServerProperties	1	bamboo.config.version	2
10626	jira.properties	1	AO_4AEACD_#	5
10627	jira.properties	1	jira.webresource.superbatch.flushcounter	5
10628	jira.properties	1	com.atlassian.jira.util.index.IndexingCounterManagerImpl.counterValue	3
10629	jira.properties	1	jira.setup	5
10630	jira.properties	1	jira.option.user.externalmanagement	1
10631	jira.properties	1	jira.option.voting	1
10632	jira.properties	1	jira.option.watching	1
10633	jira.properties	1	jira.option.issuelinking	1
10634	jira.properties	1	jira.option.emailvisible	5
10635	jira.properties	1	jira.option.allowsubtasks	1
10636	fisheye-jira-plugin.properties	1	FISH-375-fixed	5
10637	fisheye-jira-plugin.properties	1	fisheye.ual.migration.complete	5
10638	fisheye-jira-plugin.properties	1	fisheye.ual.crucible.enabled.property.fix.complete	5
10640	jira.properties	1	com.atlassian.sal.jira:build	5
10642	jira.properties	1	com.atlassian.jira.jira-mail-plugin:build	5
10643	jira.properties	1	com.atlassian.plugins.custom_apps.hasCustomOrder	5
10644	jira.properties	1	com.atlassian.plugins.atlassian-nav-links-plugin:build	5
10645	jira.properties	1	com.atlassian.jira.project-templates-plugin:build	5
10646	jira.properties	1	com.atlassian.jira.plugins.jira-importers-plugin:build	5
10647	jira.properties	1	com.atlassian.jira.plugin.ext.bamboo:build	5
10650	jira.properties	1	com.atlassian.upm.mail.impl.PluginSettingsLicenseEmailStorelicense-emails_v2	5
10651	jira.properties	1	com.atlassian.upm.mail.impl.PluginSettingsUserEmailSettingsStore:use78b9c0b572719a68385340a04c056a94	5
10652	jira.properties	1	com.atlassian.upm:notifications:dismissal-plugin.request	5
10654	jira.properties	1	com.atlassian.upm:notifications:dismissal-evaluation.expired	5
10656	jira.properties	1	com.atlassian.upm:notifications:dismissal-edition.mismatch	5
10657	jira.properties	1	com.atlassian.upm:notifications:dismissal-maintenance.expired	5
10659	jira.properties	1	com.atlassian.upm:notifications:dismissal-new.licenses	5
10660	jira.properties	1	com.atlassian.upm:notifications:dismissal-updated.licenses	5
10661	jira.properties	1	com.atlassian.upm:notifications:dismissal-auto.updated.plugin	5
10662	jira.properties	1	com.atlassian.upm:notifications:dismissal-auto.updated.upm	5
10663	jira.properties	1	com.atlassian.upm.request.PluginSettingsPluginRequestStore:requests:requests_v2	5
10665	jira.properties	1	com.atlassian.upm.atlassian-universal-plugin-manager-plugin:build	5
10666	jira.properties	1	com.atlassian.jira.plugins.jira-workflow-designer:build	5
10668	jira.properties	1	com.atlassian.plugins.atlassian-whitelist-api-plugin:whitelist.enabled	5
10669	jira.properties	1	com.atlassian.plugins.atlassian-whitelist-api-plugin:build	5
10671	jira.properties	1	com.atlassian.jira.plugins.webhooks.jira-webhooks-plugin:build	5
10681	jira.properties	1	dvcs.connector.bitbucket.url	5
10682	jira.properties	1	dvcs.connector.github.url	5
10685	jira.properties	1	AO_E8B6CC_#	5
10686	jira.properties	1	com.atlassian.jira.plugins.jira-bitbucket-connector-plugin:build	5
10687	jira.properties	1	com.atlassian.jira.lookandfeel:isDefaultFavicon	5
10688	jira.properties	1	com.atlassian.jira.lookandfeel:usingCustomFavicon	5
10689	jira.properties	1	com.atlassian.jira.lookandfeel:customDefaultFaviconURL	5
10690	jira.properties	1	com.atlassian.jira.lookandfeel:customDefaultFaviconHiresURL	5
10691	jira.properties	1	com.atlassian.jira.lookandfeel:faviconWidth	5
10692	jira.properties	1	com.atlassian.jira.lookandfeel:faviconHeight	5
10693	jira.properties	1	jira.lf.favicon.url	5
10694	jira.properties	1	jira.lf.favicon.hires.url	5
10695	jira.properties	1	com.atlassian.jira.lookandfeel:build	5
10697	jira.properties	1	com.atlassian.crowd.embedded.admin:build	5
10698	jira.properties	1	com.atlassian.jira.gadgets:build	5
10699	jira.properties	1	com.atlassian.refapp.ctk:build	5
10700	jira.properties	1	com.atlassian.analytics.client.configuration..analytics_enabled	5
10701	jira.properties	1	jira-header-plugin.studio-tab-migration-complete	5
10702	jira.properties	1	com.atlassian.upm:notifications:notification-plugin.request	5
10703	jira.properties	1	com.atlassian.upm:notifications:notification-edition.mismatch	5
10704	jira.properties	1	com.atlassian.upm:notifications:notification-evaluation.expired	5
10705	jira.properties	1	com.atlassian.upm:notifications:notification-evaluation.nearlyexpired	5
10706	jira.properties	1	com.atlassian.upm:notifications:notification-maintenance.expired	5
10707	jira.properties	1	com.atlassian.upm:notifications:notification-maintenance.nearlyexpired	5
10708	jira.properties	1	com.atlassian.upm.log.PluginSettingsAuditLogService:log:upm_audit_log_v3	5
10709	jira.properties	1	com.atlassian.analytics.client.configuration..policy_acknowledged	5
10710	jira.properties	1	com.atlassian.upm:notifications:notification-update	5
\.


--
-- Data for Name: propertynumber; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertynumber (id, propertyvalue) FROM stdin;
9	0
11	1
12	0
10404	1
10500	0
10605	1
10606	1
10608	1
10609	1
10625	22
10628	0
10630	0
10631	1
10632	1
10633	1
10635	1
\.


--
-- Data for Name: propertystring; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertystring (id, propertyvalue) FROM stdin;
10624	NN0uH9zLZhdAtUb+stOCGo0L3nOuKRvpWtxyPGjRK1Y=
10626	1
10627	2
10629	true
10634	show
10636	1
10637	1
3	10011
5	10122
6	10123
7	10000
8	1
10400	english-moderate-stemming
10401	BE23-NVGI-YLLT-BUIE
10638	1
10640	2
10642	2
10643	false
10644	1
10645	2001
10646	1
10647	1
10650	#java.util.List\n
10651	#java.util.List\n
10652	#java.util.List\n
10654	#java.util.List\n
10656	#java.util.List\n
10657	#java.util.List\n
10659	#java.util.List\n
10660	#java.util.List\n
10661	#java.util.List\n
10662	#java.util.List\n
10663	#java.util.List\n
10665	3
10666	1
10668	true
10669	4
10671	2
10681	https://bitbucket.org
10682	https://github.com
1	6318
10501	6.3-SNAPSHOT
10502	6.2.1
10601	UTF-8
10602	Your Company JIRA
10603	http://localhost:8090/jira
10604	public
10607	/home/lukasz/workspace/jira/jirahome/data/attachments
10610	true
10611	/home/lukasz/workspace/jira/jirahome/export
10613	enterprise
10685	13
10686	2
10687	false
10688	false
10689	/favicon.ico
10690	/images/64jira.png
10691	64
10692	64
10693	/favicon.ico
10694	/images/64jira.png
10600	4
10695	1
10697	3
10698	1
10699	1
10700	true
10701	migrated
10702	#java.util.List\n
10703	#java.util.List\n
10704	#java.util.List\n
10705	#java.util.List\n
10706	#java.util.List\n
10707	#java.util.List\n
10708	#java.util.List\n{"userKey":"JIRA","date":1396523921468,"i18nKey":"upm.auditLog.upm.startup","entryType":"UPM_STARTUP","params":[]}
10709	true
10710	#java.util.List\ncom.atlassian.support.stp\ncom.atlassian.jira.extra.jira-ical-feed
\.


--
-- Data for Name: propertytext; Type: TABLE DATA; Schema: public; Owner: -
--

COPY propertytext (id, propertyvalue) FROM stdin;
10	http://www.atlassian.com/*\n
10403	{\n    "edgeMap": {\n        "1DEDB66F-FE5C-EDFD-54D0-4D19CDC8CECA": {\n            "actionId": 5,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1806.5,\n                    "y": 434.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1801.0,\n                    "y": 115.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1801.0,\n                "y": 115.0\n            },\n            "endStepId": 4,\n            "id": "1DEDB66F-FE5C-EDFD-54D0-4D19CDC8CECA",\n            "label": "Resolve Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1776.85,\n                "y": 355.25\n            },\n            "lineType": "straight",\n            "startNodeId": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1806.5,\n                "y": 434.0\n            },\n            "startStepId": 5\n        },\n        "3DF7CEC8-9FBC-C0D0-AFB1-4D19CE6EA230": {\n            "actionId": 2,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1469.5,\n                    "y": 113.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1614.0,\n                    "y": 226.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1614.0,\n                "y": 226.0\n            },\n            "endStepId": 6,\n            "id": "3DF7CEC8-9FBC-C0D0-AFB1-4D19CE6EA230",\n            "label": "Close Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1492.25,\n                "y": 154.25\n            },\n            "lineType": "straight",\n            "startNodeId": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1469.5,\n                "y": 113.0\n            },\n            "startStepId": 1\n        },\n        "483797F1-1BF4-5E0F-86C6-4D19CE6023A2": {\n            "actionId": 5,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1469.5,\n                    "y": 113.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1763.0,\n                    "y": 113.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1763.0,\n                "y": 113.0\n            },\n            "endStepId": 4,\n            "id": "483797F1-1BF4-5E0F-86C6-4D19CE6023A2",\n            "label": "Resolve Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1551.0,\n                "y": 104.0\n            },\n            "lineType": "straight",\n            "startNodeId": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1469.5,\n                "y": 113.0\n            },\n            "startStepId": 1\n        },\n        "517D7F32-20FB-309E-8639-4D19CE2ACB54": {\n            "actionId": 5,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1434.0,\n                    "y": 435.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1435.0,\n                    "y": 490.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1947.0,\n                    "y": 494.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1950.0,\n                    "y": 118.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1763.0,\n                    "y": 113.0\n                }\n            ],\n            "controlPoints": [\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1435.0,\n                    "y": 490.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1947.0,\n                    "y": 494.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1950.0,\n                    "y": 118.0\n                }\n            ],\n            "endNodeId": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1763.0,\n                "y": 113.0\n            },\n            "endStepId": 4,\n            "id": "517D7F32-20FB-309E-8639-4D19CE2ACB54",\n            "label": "Resolve Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1631.25,\n                "y": 479.5\n            },\n            "lineType": "poly",\n            "startNodeId": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1434.0,\n                "y": 435.0\n            },\n            "startStepId": 3\n        },\n        "58BD4605-5FB9-84EA-6952-4D19CE7B454B": {\n            "actionId": 1,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1470.0,\n                    "y": 16.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1469.5,\n                    "y": 113.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1469.5,\n                "y": 113.0\n            },\n            "endStepId": 1,\n            "id": "58BD4605-5FB9-84EA-6952-4D19CE7B454B",\n            "label": "Create Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1475.5,\n                "y": 48.5\n            },\n            "lineType": "straight",\n            "startNodeId": "15174530-AE75-04E0-1D9D-4D19CD200835",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1470.0,\n                "y": 16.0\n            },\n            "startStepId": 1\n        },\n        "92D3DEFD-13AC-06A7-E5D8-4D19CE537791": {\n            "actionId": 4,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1439.5,\n                    "y": 116.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1393.0,\n                    "y": 116.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1390.0,\n                    "y": 434.0\n                }\n            ],\n            "controlPoints": [\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1393.0,\n                    "y": 116.0\n                }\n            ],\n            "endNodeId": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1390.0,\n                "y": 434.0\n            },\n            "endStepId": 3,\n            "id": "92D3DEFD-13AC-06A7-E5D8-4D19CE537791",\n            "label": "Start Progress",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1323.65,\n                "y": 193.75\n            },\n            "lineType": "poly",\n            "startNodeId": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1439.5,\n                "y": 116.0\n            },\n            "startStepId": 1\n        },\n        "C049EE11-C5BB-F93B-36C3-4D19CDF12B8F": {\n            "actionId": 3,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1677.0,\n                    "y": 227.0\n                },\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1767.05,\n                    "y": 230.05\n                },\n                {\n                    "positiveController": null,\n                    "x": 1773.5,\n                    "y": 425.0\n                }\n            ],\n            "controlPoints": [\n                {\n                    "positiveController": {\n                        "positiveController": null,\n                        "x": 0.0,\n                        "y": 0.0\n                    },\n                    "x": 1767.05,\n                    "y": 230.05\n                }\n            ],\n            "endNodeId": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1773.5,\n                "y": 425.0\n            },\n            "endStepId": 5,\n            "id": "C049EE11-C5BB-F93B-36C3-4D19CDF12B8F",\n            "label": "Reopen Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1703.85,\n                "y": 218.5\n            },\n            "lineType": "poly",\n            "startNodeId": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1677.0,\n                "y": 227.0\n            },\n            "startStepId": 6\n        },\n        "C9EA1792-2332-8B56-A04D-4D19CD725367": {\n            "actionId": 301,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1465.0,\n                    "y": 436.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1469.5,\n                    "y": 113.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1469.5,\n                "y": 113.0\n            },\n            "endStepId": 1,\n            "id": "C9EA1792-2332-8B56-A04D-4D19CD725367",\n            "label": "Stop Progress",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1407.8,\n                "y": 308.5\n            },\n            "lineType": "straight",\n            "startNodeId": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1465.0,\n                "y": 436.0\n            },\n            "startStepId": 3\n        },\n        "CAF37138-6321-E03A-8E41-4D19CDD7DC78": {\n            "actionId": 2,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1764.5,\n                    "y": 430.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1614.0,\n                    "y": 226.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1614.0,\n                "y": 226.0\n            },\n            "endStepId": 6,\n            "id": "CAF37138-6321-E03A-8E41-4D19CDD7DC78",\n            "label": "Close Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1677.65,\n                "y": 365.0\n            },\n            "lineType": "straight",\n            "startNodeId": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1764.5,\n                "y": 430.0\n            },\n            "startStepId": 5\n        },\n        "E1F8462A-8B0A-87EA-4F70-4D19CE423C83": {\n            "actionId": 2,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1488.0,\n                    "y": 430.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1614.0,\n                    "y": 226.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1614.0,\n                "y": 226.0\n            },\n            "endStepId": 6,\n            "id": "E1F8462A-8B0A-87EA-4F70-4D19CE423C83",\n            "label": "Close Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1492.0,\n                "y": 345.0\n            },\n            "lineType": "straight",\n            "startNodeId": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1488.0,\n                "y": 430.0\n            },\n            "startStepId": 3\n        },\n        "E27D8EB8-8E49-430B-8FCB-4D19CE127171": {\n            "actionId": 3,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1840.0,\n                    "y": 130.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1846.5,\n                    "y": 428.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1846.5,\n                "y": 428.0\n            },\n            "endStepId": 5,\n            "id": "E27D8EB8-8E49-430B-8FCB-4D19CE127171",\n            "label": "Reopen Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1814.05,\n                "y": 169.5\n            },\n            "lineType": "straight",\n            "startNodeId": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1840.0,\n                "y": 130.0\n            },\n            "startStepId": 4\n        },\n        "F79E742D-A9E4-0124-D7D4-4D19CDE48C9C": {\n            "actionId": 4,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1806.5,\n                    "y": 434.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1434.0,\n                    "y": 435.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1434.0,\n                "y": 435.0\n            },\n            "endStepId": 3,\n            "id": "F79E742D-A9E4-0124-D7D4-4D19CDE48C9C",\n            "label": "Start Progress",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1607.25,\n                "y": 423.5\n            },\n            "lineType": "straight",\n            "startNodeId": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1806.5,\n                "y": 434.0\n            },\n            "startStepId": 5\n        },\n        "FD6BA267-475B-70B3-8AA4-4D19CE00BCD1": {\n            "actionId": 701,\n            "allPoints": [\n                {\n                    "positiveController": null,\n                    "x": 1763.0,\n                    "y": 113.0\n                },\n                {\n                    "positiveController": null,\n                    "x": 1614.0,\n                    "y": 226.0\n                }\n            ],\n            "controlPoints": [],\n            "endNodeId": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "endPoint": {\n                "positiveController": null,\n                "x": 1614.0,\n                "y": 226.0\n            },\n            "endStepId": 6,\n            "id": "FD6BA267-475B-70B3-8AA4-4D19CE00BCD1",\n            "label": "Close Issue",\n            "labelPoint": {\n                "positiveController": null,\n                "x": 1635.75,\n                "y": 152.25\n            },\n            "lineType": "straight",\n            "startNodeId": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "startPoint": {\n                "positiveController": null,\n                "x": 1763.0,\n                "y": 113.0\n            },\n            "startStepId": 4\n        }\n    },\n    "nodeMap": {\n        "0740FFFA-2AA1-C90A-38ED-4D19CD61899B": {\n            "id": "0740FFFA-2AA1-C90A-38ED-4D19CD61899B",\n            "inLinkIds": [\n                "F79E742D-A9E4-0124-D7D4-4D19CDE48C9C",\n                "92D3DEFD-13AC-06A7-E5D8-4D19CE537791"\n            ],\n            "isInitialAction": false,\n            "label": "In Progress",\n            "outLinkIds": [\n                "C9EA1792-2332-8B56-A04D-4D19CD725367",\n                "517D7F32-20FB-309E-8639-4D19CE2ACB54",\n                "E1F8462A-8B0A-87EA-4F70-4D19CE423C83"\n            ],\n            "rect": {\n                "height": 45.0,\n                "positiveController": null,\n                "width": 146.0,\n                "x": 1373.0,\n                "y": 419.0\n            },\n            "stepId": 3\n        },\n        "15174530-AE75-04E0-1D9D-4D19CD200835": {\n            "id": "15174530-AE75-04E0-1D9D-4D19CD200835",\n            "inLinkIds": [],\n            "isInitialAction": true,\n            "label": "Create Issue",\n            "outLinkIds": [\n                "58BD4605-5FB9-84EA-6952-4D19CE7B454B"\n            ],\n            "rect": {\n                "height": 45.0,\n                "positiveController": null,\n                "width": 157.0,\n                "x": 1405.0,\n                "y": 0.0\n            },\n            "stepId": 1\n        },\n        "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34": {\n            "id": "1C846CFB-4F0D-2F40-D0AE-4D19CDAF5D34",\n            "inLinkIds": [\n                "CAF37138-6321-E03A-8E41-4D19CDD7DC78",\n                "E1F8462A-8B0A-87EA-4F70-4D19CE423C83",\n                "FD6BA267-475B-70B3-8AA4-4D19CE00BCD1",\n                "3DF7CEC8-9FBC-C0D0-AFB1-4D19CE6EA230"\n            ],\n            "isInitialAction": false,\n            "label": "Closed",\n            "outLinkIds": [\n                "C049EE11-C5BB-F93B-36C3-4D19CDF12B8F"\n            ],\n            "rect": {\n                "height": 45.0,\n                "positiveController": null,\n                "width": 120.0,\n                "x": 1569.0,\n                "y": 210.0\n            },\n            "stepId": 6\n        },\n        "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D": {\n            "id": "6DA64EEB-08FE-2870-C90C-4D19CDA2F72D",\n            "inLinkIds": [\n                "517D7F32-20FB-309E-8639-4D19CE2ACB54",\n                "1DEDB66F-FE5C-EDFD-54D0-4D19CDC8CECA",\n                "483797F1-1BF4-5E0F-86C6-4D19CE6023A2"\n            ],\n            "isInitialAction": false,\n            "label": "Resolved",\n            "outLinkIds": [\n                "FD6BA267-475B-70B3-8AA4-4D19CE00BCD1",\n                "E27D8EB8-8E49-430B-8FCB-4D19CE127171"\n            ],\n            "rect": {\n                "height": 44.0,\n                "positiveController": null,\n                "width": 137.0,\n                "x": 1709.0,\n                "y": 97.0\n            },\n            "stepId": 4\n        },\n        "778534F4-7595-88B6-45E1-4D19CD518712": {\n            "id": "778534F4-7595-88B6-45E1-4D19CD518712",\n            "inLinkIds": [\n                "C9EA1792-2332-8B56-A04D-4D19CD725367",\n                "58BD4605-5FB9-84EA-6952-4D19CE7B454B"\n            ],\n            "isInitialAction": false,\n            "label": "Open",\n            "outLinkIds": [\n                "92D3DEFD-13AC-06A7-E5D8-4D19CE537791",\n                "483797F1-1BF4-5E0F-86C6-4D19CE6023A2",\n                "3DF7CEC8-9FBC-C0D0-AFB1-4D19CE6EA230"\n            ],\n            "rect": {\n                "height": 45.0,\n                "positiveController": null,\n                "width": 106.0,\n                "x": 1429.5,\n                "y": 97.0\n            },\n            "stepId": 1\n        },\n        "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB": {\n            "id": "A8B1A431-AC3A-6DCD-BFF0-4D19CDBCAADB",\n            "inLinkIds": [\n                "E27D8EB8-8E49-430B-8FCB-4D19CE127171",\n                "C049EE11-C5BB-F93B-36C3-4D19CDF12B8F"\n            ],\n            "isInitialAction": false,\n            "label": "Reopened",\n            "outLinkIds": [\n                "1DEDB66F-FE5C-EDFD-54D0-4D19CDC8CECA",\n                "CAF37138-6321-E03A-8E41-4D19CDD7DC78",\n                "F79E742D-A9E4-0124-D7D4-4D19CDE48C9C"\n            ],\n            "rect": {\n                "height": 45.0,\n                "positiveController": null,\n                "width": 142.0,\n                "x": 1749.5,\n                "y": 418.0\n            },\n            "stepId": 5\n        }\n    },\n    "rootIds": [\n        "15174530-AE75-04E0-1D9D-4D19CD200835"\n    ],\n    "width": 1136\n}\n
10612	AAABOQ0ODAoPeNptkMFKw0AQhu/7FAueU5JUhRYWjOmqaZu0JqkH8TKmU7uSbMvspti3d9O0iCIszGF+vm//uSq3LU/hyMMRD4bj8HY8HHJZlDz0g2s2QVOR2lu102Ka5BFf6Vo1yuKarwySeRvzCViIUVskjmvVJVnWNu9Ii80pIryAxYTQbVwWRQf2/MA9Fu+0hcpm0KBIwVr+BETKOMSnIhjMVYXaoOyxQmalzJd5UkgmD1C3J6TYQG3wQpIpqFo02zPmDmwNxijQg2rX/IKWxz2evPEiTWUeJ9G83zuKOqCw1CKr++yLq9GpQubwrqkGXaH82is6nhvd+OdGC/oArUz/tehiZ4XMRInGutlbfq7WmwqkA1IyEfevzzPvYSTnXuLHj94syqbscod/jcuWqi0Y/Hvbb3VZnjcwLAIUcecdsGBPKVNaP/ak52NDKr7Vz6gCFGkRg2FKo591qXFXY745tCptNOYNX02fj
10623	{"AO_38321B_CUSTOM_CONTENT_LINK":{"key":"com.atlassian.plugins.atlassian-nav-links-plugin","name":"Atlassian Navigation Links Plugin","version":"3.3.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_ISSUE_TO_CHANGESET":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_MESSAGE_TAG":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_COMMIT_ISSUE_KEY":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_ACTIVITY_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_21D670_WHITELIST_RULES":{"key":"com.atlassian.plugins.atlassian-whitelist-api-plugin","name":"Atlassian Whitelist API Plugin","version":"1.7","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_DEB285_BLOG_AO":{"key":"com.atlassian.jira.dev.func-test-plugin","name":"Atlassian JIRA - Plugins - DevMode - Func Test Plugin","version":"6.3-SNAPSHOT","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_4AEACD_WEBHOOK_DAO":{"key":"com.atlassian.jira.plugins.webhooks.jira-webhooks-plugin","name":"JIRA WebHooks Plugin","version":"1.2.6","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_BRANCH_HEAD_MAPPING":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_ACTOR_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_OBJECT_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_A0B856_WEB_HOOK_LISTENER_AO":{"key":"com.atlassian.webhooks.atlassian-webhooks-plugin","name":"Atlassian WebHooks Plugin","version":"0.17.6","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_BRANCH":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_PR_PARTICIPANT":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_SYNC_AUDIT_LOG":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_MESSAGE":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_REPOSITORY_MAPPING":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_DEB285_COMMENT_AO":{"key":"com.atlassian.jira.dev.func-test-plugin","name":"Atlassian JIRA - Plugins - DevMode - Func Test Plugin","version":"6.3-SNAPSHOT","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_A44657_HEALTH_CHECK_ENTITY":{"key":"com.atlassian.jira.plugins.jira-healthcheck-plugin","name":"Atlassian JIRA - Plugins - Healthcheck Plugin","version":"1.0.10","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_PULL_REQUEST":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_MESSAGE_QUEUE_ITEM":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_PR_ISSUE_KEY":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_B9A0F0_APPLIED_TEMPLATE":{"key":"com.atlassian.jira.project-templates-plugin","name":"Project Templates Plugin","version":"2.38","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_GIT_HUB_EVENT":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_TARGET_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_REPO_TO_CHANGESET":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_MEDIA_LINK_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_ISSUE_TO_BRANCH":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_ORGANIZATION_MAPPING":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_97EDAB_USERINVITATION":{"key":"com.atlassian.jira.jira-invite-user-plugin","name":"Atlassian JIRA - Plugins - Invite User","version":"1.16","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_PR_TO_COMMIT":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_COMMIT":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_E8B6CC_CHANGESET_MAPPING":{"key":"com.atlassian.jira.plugins.jira-bitbucket-connector-plugin","name":"JIRA DVCS Connector Plugin","version":"2.1.2","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"},"AO_563AEE_ACTIVITY_OBJ_ENTITY":{"key":"com.atlassian.streams.streams-thirdparty-plugin","name":"Streams Third Party Provider Plugin","version":"5.3.23","vendorName":"Atlassian","vendorUrl":"http://www.atlassian.com/"}}
\.


--
-- Data for Name: qrtz_calendars; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_calendars (id, calendar_name, calendar) FROM stdin;
\.


--
-- Data for Name: qrtz_cron_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_cron_triggers (id, trigger_id, cronexperssion) FROM stdin;
\.


--
-- Data for Name: qrtz_fired_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_fired_triggers (id, entry_id, trigger_id, trigger_listener, fired_time, trigger_state) FROM stdin;
\.


--
-- Data for Name: qrtz_job_details; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_job_details (id, job_name, job_group, class_name, is_durable, is_stateful, requests_recovery, job_data) FROM stdin;
\.


--
-- Data for Name: qrtz_job_listeners; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_job_listeners (id, job, job_listener) FROM stdin;
\.


--
-- Data for Name: qrtz_simple_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_simple_triggers (id, trigger_id, repeat_count, repeat_interval, times_triggered) FROM stdin;
\.


--
-- Data for Name: qrtz_trigger_listeners; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_trigger_listeners (id, trigger_id, trigger_listener) FROM stdin;
\.


--
-- Data for Name: qrtz_triggers; Type: TABLE DATA; Schema: public; Owner: -
--

COPY qrtz_triggers (id, trigger_name, trigger_group, job, next_fire, trigger_state, trigger_type, start_time, end_time, calendar_name, misfire_instr) FROM stdin;
\.


--
-- Data for Name: remembermetoken; Type: TABLE DATA; Schema: public; Owner: -
--

COPY remembermetoken (id, created, token, username) FROM stdin;
\.


--
-- Data for Name: remotelink; Type: TABLE DATA; Schema: public; Owner: -
--

COPY remotelink (id, issueid, globalid, title, summary, url, iconurl, icontitle, relationship, resolved, statusname, statusdescription, statusiconurl, statusicontitle, statusiconlink, statuscategorykey, statuscategorycolorname, applicationtype, applicationname) FROM stdin;
\.


--
-- Data for Name: replicatedindexoperation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY replicatedindexoperation (id, index_time, node_id, affected_index, entity_type, affected_ids, operation) FROM stdin;
\.


--
-- Data for Name: resolution; Type: TABLE DATA; Schema: public; Owner: -
--

COPY resolution (id, sequence, pname, description, iconurl) FROM stdin;
1	1	Fixed	A fix for this issue is checked into the tree and tested.	\N
2	2	Won't Fix	The problem described is an issue which will never be fixed.	\N
3	3	Duplicate	The problem is a duplicate of an existing issue.	\N
4	4	Incomplete	The problem is not completely described.	\N
5	5	Cannot Reproduce	All attempts at reproducing this issue failed, or not enough information was available to reproduce the issue. Reading the code produces no clues as to why this behavior would occur. If more information appears later, please reopen the issue.	\N
\.


--
-- Data for Name: rundetails; Type: TABLE DATA; Schema: public; Owner: -
--

COPY rundetails (id, job_id, start_time, run_duration, run_outcome, info_message) FROM stdin;
10000	com.atlassian.jira.service.JiraService:10200	2014-04-03 22:17:48.289+11	28	S	
10001	com.atlassian.jira.service.JiraService:10001	2014-04-03 22:17:58.246+11	412	S	
10003	JiraPluginScheduler:PluginRequestCheckPluginJob-job	2014-04-03 22:18:41.446+11	19	S	
10004	JiraPluginScheduler:LocalPluginLicenseNotificationPluginJob-job	2014-04-03 22:18:41.443+11	93	S	
10005	JiraPluginScheduler:com.atlassian.jira.plugin.ext.bamboo.service.PlanStatusUpdateServiceImpl:job	2014-04-03 22:18:41.534+11	2	S	
10006	JiraPluginScheduler:RemotePluginLicenseNotificationPluginJob-job	2014-04-03 22:18:41.445+11	1279	S	
10007	CompatibilityPluginScheduler.JobId.bitbucket-accounts-reload	2014-04-03 22:18:56.553+11	10	S	
10008	JiraPluginScheduler:com.atlassian.analytics.client.upload.RemoteFilterRead:job	2014-04-03 22:18:51.416+11	7414	S	
10009	com.atlassian.jira.service.JiraService:10000	2014-04-03 22:19:04.926+11	8	S	
\.


--
-- Data for Name: schemeissuesecurities; Type: TABLE DATA; Schema: public; Owner: -
--

COPY schemeissuesecurities (id, scheme, security, sec_type, sec_parameter) FROM stdin;
\.


--
-- Data for Name: schemeissuesecuritylevels; Type: TABLE DATA; Schema: public; Owner: -
--

COPY schemeissuesecuritylevels (id, name, description, scheme) FROM stdin;
\.


--
-- Data for Name: schemepermissions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY schemepermissions (id, scheme, permission, perm_type, perm_parameter) FROM stdin;
10021	0	42	projectrole	10000
10022	0	41	projectrole	10001
10023	0	40	projectrole	10000
10024	0	31	projectrole	10001
10025	0	32	projectrole	10002
10026	0	34	projectrole	10001
10027	0	35	projectrole	10000
10028	0	36	projectrole	10002
10029	0	37	projectrole	10000
10030	0	38	projectrole	10002
10031	0	39	projectrole	10000
10032	\N	22	group	jira-users
10033	0	29	projectrole	10001
10100	\N	33	group	jira-users
10101	\N	44	group	jira-administrators
10200	0	45	projectrole	10000
10000	\N	0	group	jira-administrators
10001	\N	1	group	jira-users
10002	\N	27	group	jira-developers
10003	\N	24	group	jira-developers
10004	0	23	projectrole	10002
10005	0	10	projectrole	10000
10006	0	11	projectrole	10000
10007	0	15	projectrole	10000
10008	0	19	projectrole	10000
10009	0	13	projectrole	10001
10010	0	17	projectrole	10001
10011	0	14	projectrole	10001
10012	0	21	projectrole	10000
10013	0	12	projectrole	10001
10014	0	16	projectrole	10002
10015	0	18	projectrole	10001
10016	0	25	projectrole	10001
10017	0	28	projectrole	10001
10018	0	30	projectrole	10002
10019	0	20	projectrole	10001
10020	0	43	projectrole	10002
\.


--
-- Data for Name: searchrequest; Type: TABLE DATA; Schema: public; Owner: -
--

COPY searchrequest (id, filtername, authorname, description, username, groupname, projectid, reqcontent, fav_count, filtername_lower) FROM stdin;
\.


--
-- Data for Name: sequence_value_item; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sequence_value_item (seq_name, seq_id) FROM stdin;
ConfigurationContext	10100
FieldConfigScheme	10100
FieldConfigSchemeIssueType	10200
FieldConfiguration	10100
FieldLayout	10100
FieldScreen	10000
FieldScreenLayoutItem	10200
FieldScreenScheme	10000
FieldScreenSchemeItem	10100
FieldScreenTab	10100
GadgetUserPreference	10100
GenericConfiguration	10100
Group	10010
IssueLinkType	10200
IssueTypeScreenSchemeEntity	10100
Notification	10200
NotificationScheme	10100
OAuthConsumer	10100
OptionConfiguration	10200
PortalPage	10100
PortletConfiguration	10100
ProjectRole	10100
ProjectRoleActor	10100
SchemePermissions	10300
SharePermissions	10100
ClusterLockStatus	10100
Avatar	10300
Workflow	10100
WorkflowScheme	10100
WorkflowSchemeEntity	10100
IssueType	10000
Resolution	10000
Priority	10000
Status	10000
UpgradeHistory	10200
GlobalPermissionEntry	10100
ServiceConfig	10300
UpgradeVersionHistory	10100
RunDetails	10100
ListenerConfig	10200
EntityPropertyIndexDocument	10100
PluginVersion	10200
ApplicationUser	10100
User	10100
UserAttribute	10100
AuditLog	10100
AuditChangedValue	10100
Membership	10100
AuditItem	10100
FieldLayoutItem	10200
OSPropertyEntry	10800
UserHistoryItem	10100
\.


--
-- Data for Name: serviceconfig; Type: TABLE DATA; Schema: public; Owner: -
--

COPY serviceconfig (id, delaytime, clazz, servicename) FROM stdin;
10000	60000	com.atlassian.jira.service.services.mail.MailQueueService	Mail Queue Service
10200	86400000	com.atlassian.jira.service.services.auditing.AuditLogCleaningService	Audit log cleaning service
10001	43200000	com.atlassian.jira.service.services.export.ExportService	Backup Service
\.


--
-- Data for Name: sharepermissions; Type: TABLE DATA; Schema: public; Owner: -
--

COPY sharepermissions (id, entityid, entitytype, sharetype, param1, param2) FROM stdin;
10000	10000	PortalPage	global	\N	\N
\.


--
-- Data for Name: trackback_ping; Type: TABLE DATA; Schema: public; Owner: -
--

COPY trackback_ping (id, issue, url, title, blogname, excerpt, created) FROM stdin;
\.


--
-- Data for Name: trustedapp; Type: TABLE DATA; Schema: public; Owner: -
--

COPY trustedapp (id, application_id, name, public_key, ip_match, url_match, timeout, created, created_by, updated, updated_by) FROM stdin;
\.


--
-- Data for Name: upgradehistory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY upgradehistory (id, upgradeclass, targetbuild) FROM stdin;
10000	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6081	6318
10001	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6083	6318
10002	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6085	6318
10003	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6096	6318
10004	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6100	6318
10005	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6121	6318
10006	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6122	6318
10007	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6123	6318
10008	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6124	6318
10009	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6128	6318
10010	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6129	6318
10011	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6130	6318
10012	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6131	6318
10013	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6132	6318
10014	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6133	6318
10015	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6134	6318
10016	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6135	6318
10017	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6136	6318
10018	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6137	6318
10019	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6138	6318
10020	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6139	6318
10021	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6140	6318
10022	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6141	6318
10023	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6142	6318
10024	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6151	6318
10025	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6152	6318
10026	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6153	6318
10027	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6154	6318
10028	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6200	6318
10029	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6205	6318
10030	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6206	6318
10100	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6207	6318
10101	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6208	6318
10102	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6209	6318
10103	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6210	6318
10104	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6211	6318
10105	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6251	6318
10106	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6254	6318
10107	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6256	6318
10108	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6258	6318
10109	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6301	6318
10110	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6302	6318
10111	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6304	6318
10112	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6305	6318
10113	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6306	6318
10114	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6316	6318
10115	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6317	6318
10116	com.atlassian.jira.upgrade.tasks.UpgradeTask_Build6318	6318
\.


--
-- Data for Name: upgradeversionhistory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY upgradeversionhistory (id, timeperformed, targetbuild, targetversion) FROM stdin;
10000	2014-04-03 22:17:48.211+11	6318	6.3-SNAPSHOT
\.


--
-- Data for Name: userassociation; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userassociation (source_name, sink_node_id, sink_node_entity, association_type, sequence, created) FROM stdin;
\.


--
-- Data for Name: userbase; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userbase (id, username, password_hash) FROM stdin;
\.


--
-- Data for Name: userhistoryitem; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userhistoryitem (id, entitytype, entityid, username, lastviewed, data) FROM stdin;
10000	Dashboard	10000	admin	1396523921738	\N
\.


--
-- Data for Name: userpickerfilter; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userpickerfilter (id, customfield, customfieldconfig, enabled) FROM stdin;
\.


--
-- Data for Name: userpickerfiltergroup; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userpickerfiltergroup (id, userpickerfilter, groupname) FROM stdin;
\.


--
-- Data for Name: userpickerfilterrole; Type: TABLE DATA; Schema: public; Owner: -
--

COPY userpickerfilterrole (id, userpickerfilter, projectroleid) FROM stdin;
\.


--
-- Data for Name: versioncontrol; Type: TABLE DATA; Schema: public; Owner: -
--

COPY versioncontrol (id, vcsname, vcsdescription, vcstype) FROM stdin;
\.


--
-- Data for Name: votehistory; Type: TABLE DATA; Schema: public; Owner: -
--

COPY votehistory (id, issueid, votes, "timestamp") FROM stdin;
\.


--
-- Data for Name: workflowscheme; Type: TABLE DATA; Schema: public; Owner: -
--

COPY workflowscheme (id, name, description) FROM stdin;
10000	classic	classic
\.


--
-- Data for Name: workflowschemeentity; Type: TABLE DATA; Schema: public; Owner: -
--

COPY workflowschemeentity (id, scheme, workflow, issuetype) FROM stdin;
10000	10000	classic default workflow	0
\.


--
-- Data for Name: worklog; Type: TABLE DATA; Schema: public; Owner: -
--

COPY worklog (id, issueid, author, grouplevel, rolelevel, worklogbody, created, updateauthor, updated, startdate, timeworked) FROM stdin;
\.


--
-- Name: AO_21D670_WHITELIST_RULES_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_21D670_WHITELIST_RULES"
    ADD CONSTRAINT "AO_21D670_WHITELIST_RULES_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_4AEACD_WEBHOOK_DAO_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_4AEACD_WEBHOOK_DAO"
    ADD CONSTRAINT "AO_4AEACD_WEBHOOK_DAO_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_563AEE_ACTIVITY_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY"
    ADD CONSTRAINT "AO_563AEE_ACTIVITY_ENTITY_pkey" PRIMARY KEY ("ACTIVITY_ID");


--
-- Name: AO_563AEE_ACTOR_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_563AEE_ACTOR_ENTITY"
    ADD CONSTRAINT "AO_563AEE_ACTOR_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_563AEE_MEDIA_LINK_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_563AEE_MEDIA_LINK_ENTITY"
    ADD CONSTRAINT "AO_563AEE_MEDIA_LINK_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_563AEE_OBJECT_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_563AEE_OBJECT_ENTITY"
    ADD CONSTRAINT "AO_563AEE_OBJECT_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_563AEE_TARGET_ENTITY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_563AEE_TARGET_ENTITY"
    ADD CONSTRAINT "AO_563AEE_TARGET_ENTITY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_B9A0F0_APPLIED_TEMPLATE_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_B9A0F0_APPLIED_TEMPLATE"
    ADD CONSTRAINT "AO_B9A0F0_APPLIED_TEMPLATE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_BRANCH_HEAD_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_BRANCH_HEAD_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_BRANCH_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH"
    ADD CONSTRAINT "AO_E8B6CC_BRANCH_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_CHANGESET_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_CHANGESET_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_CHANGESET_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_COMMIT_ISSUE_KEY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT_ISSUE_KEY"
    ADD CONSTRAINT "AO_E8B6CC_COMMIT_ISSUE_KEY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_COMMIT_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT"
    ADD CONSTRAINT "AO_E8B6CC_COMMIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_GIT_HUB_EVENT_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT"
    ADD CONSTRAINT "AO_E8B6CC_GIT_HUB_EVENT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_V2_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_MAPPING_V2"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_MAPPING_V2_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_TO_BRANCH_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_TO_BRANCH_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ISSUE_TO_CHANGESET_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET"
    ADD CONSTRAINT "AO_E8B6CC_ISSUE_TO_CHANGESET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_QUEUE_ITEM_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_QUEUE_ITEM_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_TAG_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_TAG_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_MESSAGE_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE"
    ADD CONSTRAINT "AO_E8B6CC_MESSAGE_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_ORGANIZATION_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_ORGANIZATION_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_ORGANIZATION_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_V2_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PROJECT_MAPPING_V2"
    ADD CONSTRAINT "AO_E8B6CC_PROJECT_MAPPING_V2_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PROJECT_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PROJECT_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_PROJECT_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_ISSUE_KEY_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PR_ISSUE_KEY"
    ADD CONSTRAINT "AO_E8B6CC_PR_ISSUE_KEY_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_PARTICIPANT_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT"
    ADD CONSTRAINT "AO_E8B6CC_PR_PARTICIPANT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PR_TO_COMMIT_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT "AO_E8B6CC_PR_TO_COMMIT_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_PULL_REQUEST_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_PULL_REQUEST"
    ADD CONSTRAINT "AO_E8B6CC_PULL_REQUEST_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_REPOSITORY_MAPPING_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_REPOSITORY_MAPPING"
    ADD CONSTRAINT "AO_E8B6CC_REPOSITORY_MAPPING_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_REPO_TO_CHANGESET_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT "AO_E8B6CC_REPO_TO_CHANGESET_pkey" PRIMARY KEY ("ID");


--
-- Name: AO_E8B6CC_SYNC_AUDIT_LOG_pkey; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY "AO_E8B6CC_SYNC_AUDIT_LOG"
    ADD CONSTRAINT "AO_E8B6CC_SYNC_AUDIT_LOG_pkey" PRIMARY KEY ("ID");


--
-- Name: pk_app_user; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY app_user
    ADD CONSTRAINT pk_app_user PRIMARY KEY (id);


--
-- Name: pk_audit_changed_value; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY audit_changed_value
    ADD CONSTRAINT pk_audit_changed_value PRIMARY KEY (id);


--
-- Name: pk_audit_item; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY audit_item
    ADD CONSTRAINT pk_audit_item PRIMARY KEY (id);


--
-- Name: pk_audit_log; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY audit_log
    ADD CONSTRAINT pk_audit_log PRIMARY KEY (id);


--
-- Name: pk_avatar; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY avatar
    ADD CONSTRAINT pk_avatar PRIMARY KEY (id);


--
-- Name: pk_changegroup; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY changegroup
    ADD CONSTRAINT pk_changegroup PRIMARY KEY (id);


--
-- Name: pk_changeitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY changeitem
    ADD CONSTRAINT pk_changeitem PRIMARY KEY (id);


--
-- Name: pk_clusterlockstatus; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY clusterlockstatus
    ADD CONSTRAINT pk_clusterlockstatus PRIMARY KEY (id);


--
-- Name: pk_clustermessage; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY clustermessage
    ADD CONSTRAINT pk_clustermessage PRIMARY KEY (id);


--
-- Name: pk_clusternode; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY clusternode
    ADD CONSTRAINT pk_clusternode PRIMARY KEY (node_id);


--
-- Name: pk_columnlayout; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY columnlayout
    ADD CONSTRAINT pk_columnlayout PRIMARY KEY (id);


--
-- Name: pk_columnlayoutitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY columnlayoutitem
    ADD CONSTRAINT pk_columnlayoutitem PRIMARY KEY (id);


--
-- Name: pk_component; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY component
    ADD CONSTRAINT pk_component PRIMARY KEY (id);


--
-- Name: pk_configurationcontext; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY configurationcontext
    ADD CONSTRAINT pk_configurationcontext PRIMARY KEY (id);


--
-- Name: pk_customfield; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY customfield
    ADD CONSTRAINT pk_customfield PRIMARY KEY (id);


--
-- Name: pk_customfieldoption; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY customfieldoption
    ADD CONSTRAINT pk_customfieldoption PRIMARY KEY (id);


--
-- Name: pk_customfieldvalue; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY customfieldvalue
    ADD CONSTRAINT pk_customfieldvalue PRIMARY KEY (id);


--
-- Name: pk_cwd_application; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_application
    ADD CONSTRAINT pk_cwd_application PRIMARY KEY (id);


--
-- Name: pk_cwd_application_address; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_application_address
    ADD CONSTRAINT pk_cwd_application_address PRIMARY KEY (application_id, remote_address);


--
-- Name: pk_cwd_directory; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_directory
    ADD CONSTRAINT pk_cwd_directory PRIMARY KEY (id);


--
-- Name: pk_cwd_directory_attribute; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_directory_attribute
    ADD CONSTRAINT pk_cwd_directory_attribute PRIMARY KEY (directory_id, attribute_name);


--
-- Name: pk_cwd_directory_operation; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_directory_operation
    ADD CONSTRAINT pk_cwd_directory_operation PRIMARY KEY (directory_id, operation_type);


--
-- Name: pk_cwd_group; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_group
    ADD CONSTRAINT pk_cwd_group PRIMARY KEY (id);


--
-- Name: pk_cwd_group_attributes; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_group_attributes
    ADD CONSTRAINT pk_cwd_group_attributes PRIMARY KEY (id);


--
-- Name: pk_cwd_membership; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_membership
    ADD CONSTRAINT pk_cwd_membership PRIMARY KEY (id);


--
-- Name: pk_cwd_user; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_user
    ADD CONSTRAINT pk_cwd_user PRIMARY KEY (id);


--
-- Name: pk_cwd_user_attributes; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY cwd_user_attributes
    ADD CONSTRAINT pk_cwd_user_attributes PRIMARY KEY (id);


--
-- Name: pk_draftworkflowscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY draftworkflowscheme
    ADD CONSTRAINT pk_draftworkflowscheme PRIMARY KEY (id);


--
-- Name: pk_draftworkflowschemeentity; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY draftworkflowschemeentity
    ADD CONSTRAINT pk_draftworkflowschemeentity PRIMARY KEY (id);


--
-- Name: pk_entity_property; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entity_property
    ADD CONSTRAINT pk_entity_property PRIMARY KEY (id);


--
-- Name: pk_entity_property_index_docum; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY entity_property_index_document
    ADD CONSTRAINT pk_entity_property_index_docum PRIMARY KEY (id);


--
-- Name: pk_external_entities; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY external_entities
    ADD CONSTRAINT pk_external_entities PRIMARY KEY (id);


--
-- Name: pk_externalgadget; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY externalgadget
    ADD CONSTRAINT pk_externalgadget PRIMARY KEY (id);


--
-- Name: pk_favouriteassociations; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY favouriteassociations
    ADD CONSTRAINT pk_favouriteassociations PRIMARY KEY (id);


--
-- Name: pk_feature; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY feature
    ADD CONSTRAINT pk_feature PRIMARY KEY (id);


--
-- Name: pk_fieldconfigscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldconfigscheme
    ADD CONSTRAINT pk_fieldconfigscheme PRIMARY KEY (id);


--
-- Name: pk_fieldconfigschemeissuetype; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldconfigschemeissuetype
    ADD CONSTRAINT pk_fieldconfigschemeissuetype PRIMARY KEY (id);


--
-- Name: pk_fieldconfiguration; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldconfiguration
    ADD CONSTRAINT pk_fieldconfiguration PRIMARY KEY (id);


--
-- Name: pk_fieldlayout; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldlayout
    ADD CONSTRAINT pk_fieldlayout PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldlayoutitem
    ADD CONSTRAINT pk_fieldlayoutitem PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldlayoutscheme
    ADD CONSTRAINT pk_fieldlayoutscheme PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutschemeassociatio; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldlayoutschemeassociation
    ADD CONSTRAINT pk_fieldlayoutschemeassociatio PRIMARY KEY (id);


--
-- Name: pk_fieldlayoutschemeentity; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldlayoutschemeentity
    ADD CONSTRAINT pk_fieldlayoutschemeentity PRIMARY KEY (id);


--
-- Name: pk_fieldscreen; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldscreen
    ADD CONSTRAINT pk_fieldscreen PRIMARY KEY (id);


--
-- Name: pk_fieldscreenlayoutitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldscreenlayoutitem
    ADD CONSTRAINT pk_fieldscreenlayoutitem PRIMARY KEY (id);


--
-- Name: pk_fieldscreenscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldscreenscheme
    ADD CONSTRAINT pk_fieldscreenscheme PRIMARY KEY (id);


--
-- Name: pk_fieldscreenschemeitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldscreenschemeitem
    ADD CONSTRAINT pk_fieldscreenschemeitem PRIMARY KEY (id);


--
-- Name: pk_fieldscreentab; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fieldscreentab
    ADD CONSTRAINT pk_fieldscreentab PRIMARY KEY (id);


--
-- Name: pk_fileattachment; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY fileattachment
    ADD CONSTRAINT pk_fileattachment PRIMARY KEY (id);


--
-- Name: pk_filtersubscription; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY filtersubscription
    ADD CONSTRAINT pk_filtersubscription PRIMARY KEY (id);


--
-- Name: pk_gadgetuserpreference; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY gadgetuserpreference
    ADD CONSTRAINT pk_gadgetuserpreference PRIMARY KEY (id);


--
-- Name: pk_genericconfiguration; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY genericconfiguration
    ADD CONSTRAINT pk_genericconfiguration PRIMARY KEY (id);


--
-- Name: pk_globalpermissionentry; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY globalpermissionentry
    ADD CONSTRAINT pk_globalpermissionentry PRIMARY KEY (id);


--
-- Name: pk_groupbase; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY groupbase
    ADD CONSTRAINT pk_groupbase PRIMARY KEY (id);


--
-- Name: pk_issuelink; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuelink
    ADD CONSTRAINT pk_issuelink PRIMARY KEY (id);


--
-- Name: pk_issuelinktype; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuelinktype
    ADD CONSTRAINT pk_issuelinktype PRIMARY KEY (id);


--
-- Name: pk_issuesecurityscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuesecurityscheme
    ADD CONSTRAINT pk_issuesecurityscheme PRIMARY KEY (id);


--
-- Name: pk_issuestatus; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuestatus
    ADD CONSTRAINT pk_issuestatus PRIMARY KEY (id);


--
-- Name: pk_issuetype; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuetype
    ADD CONSTRAINT pk_issuetype PRIMARY KEY (id);


--
-- Name: pk_issuetypescreenscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuetypescreenscheme
    ADD CONSTRAINT pk_issuetypescreenscheme PRIMARY KEY (id);


--
-- Name: pk_issuetypescreenschemeentity; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY issuetypescreenschemeentity
    ADD CONSTRAINT pk_issuetypescreenschemeentity PRIMARY KEY (id);


--
-- Name: pk_jiraaction; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiraaction
    ADD CONSTRAINT pk_jiraaction PRIMARY KEY (id);


--
-- Name: pk_jiradraftworkflows; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiradraftworkflows
    ADD CONSTRAINT pk_jiradraftworkflows PRIMARY KEY (id);


--
-- Name: pk_jiraeventtype; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiraeventtype
    ADD CONSTRAINT pk_jiraeventtype PRIMARY KEY (id);


--
-- Name: pk_jiraissue; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiraissue
    ADD CONSTRAINT pk_jiraissue PRIMARY KEY (id);


--
-- Name: pk_jiraperms; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiraperms
    ADD CONSTRAINT pk_jiraperms PRIMARY KEY (id);


--
-- Name: pk_jiraworkflows; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jiraworkflows
    ADD CONSTRAINT pk_jiraworkflows PRIMARY KEY (id);


--
-- Name: pk_jquartz_blob_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_blob_triggers
    ADD CONSTRAINT pk_jquartz_blob_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_calendars; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_calendars
    ADD CONSTRAINT pk_jquartz_calendars PRIMARY KEY (calendar_name);


--
-- Name: pk_jquartz_cron_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_cron_triggers
    ADD CONSTRAINT pk_jquartz_cron_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_fired_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_fired_triggers
    ADD CONSTRAINT pk_jquartz_fired_triggers PRIMARY KEY (entry_id);


--
-- Name: pk_jquartz_job_details; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_job_details
    ADD CONSTRAINT pk_jquartz_job_details PRIMARY KEY (job_name, job_group);


--
-- Name: pk_jquartz_job_listeners; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_job_listeners
    ADD CONSTRAINT pk_jquartz_job_listeners PRIMARY KEY (job_name, job_group, job_listener);


--
-- Name: pk_jquartz_locks; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_locks
    ADD CONSTRAINT pk_jquartz_locks PRIMARY KEY (lock_name);


--
-- Name: pk_jquartz_paused_trigger_grps; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_paused_trigger_grps
    ADD CONSTRAINT pk_jquartz_paused_trigger_grps PRIMARY KEY (trigger_group);


--
-- Name: pk_jquartz_scheduler_state; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_scheduler_state
    ADD CONSTRAINT pk_jquartz_scheduler_state PRIMARY KEY (instance_name);


--
-- Name: pk_jquartz_simple_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_simple_triggers
    ADD CONSTRAINT pk_jquartz_simple_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_simprop_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_simprop_triggers
    ADD CONSTRAINT pk_jquartz_simprop_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_jquartz_trigger_listeners; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_trigger_listeners
    ADD CONSTRAINT pk_jquartz_trigger_listeners PRIMARY KEY (trigger_group, trigger_listener);


--
-- Name: pk_jquartz_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY jquartz_triggers
    ADD CONSTRAINT pk_jquartz_triggers PRIMARY KEY (trigger_name, trigger_group);


--
-- Name: pk_label; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY label
    ADD CONSTRAINT pk_label PRIMARY KEY (id);


--
-- Name: pk_listenerconfig; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY listenerconfig
    ADD CONSTRAINT pk_listenerconfig PRIMARY KEY (id);


--
-- Name: pk_mailserver; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY mailserver
    ADD CONSTRAINT pk_mailserver PRIMARY KEY (id);


--
-- Name: pk_managedconfigurationitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY managedconfigurationitem
    ADD CONSTRAINT pk_managedconfigurationitem PRIMARY KEY (id);


--
-- Name: pk_membershipbase; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY membershipbase
    ADD CONSTRAINT pk_membershipbase PRIMARY KEY (id);


--
-- Name: pk_moved_issue_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY moved_issue_key
    ADD CONSTRAINT pk_moved_issue_key PRIMARY KEY (id);


--
-- Name: pk_nodeassociation; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nodeassociation
    ADD CONSTRAINT pk_nodeassociation PRIMARY KEY (source_node_id, source_node_entity, sink_node_id, sink_node_entity, association_type);


--
-- Name: pk_nodeindexcounter; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY nodeindexcounter
    ADD CONSTRAINT pk_nodeindexcounter PRIMARY KEY (id);


--
-- Name: pk_notification; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY notification
    ADD CONSTRAINT pk_notification PRIMARY KEY (id);


--
-- Name: pk_notificationinstance; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY notificationinstance
    ADD CONSTRAINT pk_notificationinstance PRIMARY KEY (id);


--
-- Name: pk_notificationscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY notificationscheme
    ADD CONSTRAINT pk_notificationscheme PRIMARY KEY (id);


--
-- Name: pk_oauthconsumer; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauthconsumer
    ADD CONSTRAINT pk_oauthconsumer PRIMARY KEY (id);


--
-- Name: pk_oauthconsumertoken; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauthconsumertoken
    ADD CONSTRAINT pk_oauthconsumertoken PRIMARY KEY (id);


--
-- Name: pk_oauthspconsumer; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauthspconsumer
    ADD CONSTRAINT pk_oauthspconsumer PRIMARY KEY (id);


--
-- Name: pk_oauthsptoken; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY oauthsptoken
    ADD CONSTRAINT pk_oauthsptoken PRIMARY KEY (id);


--
-- Name: pk_optionconfiguration; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY optionconfiguration
    ADD CONSTRAINT pk_optionconfiguration PRIMARY KEY (id);


--
-- Name: pk_os_currentstep; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY os_currentstep
    ADD CONSTRAINT pk_os_currentstep PRIMARY KEY (id);


--
-- Name: pk_os_currentstep_prev; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY os_currentstep_prev
    ADD CONSTRAINT pk_os_currentstep_prev PRIMARY KEY (id, previous_id);


--
-- Name: pk_os_historystep; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY os_historystep
    ADD CONSTRAINT pk_os_historystep PRIMARY KEY (id);


--
-- Name: pk_os_historystep_prev; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY os_historystep_prev
    ADD CONSTRAINT pk_os_historystep_prev PRIMARY KEY (id, previous_id);


--
-- Name: pk_os_wfentry; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY os_wfentry
    ADD CONSTRAINT pk_os_wfentry PRIMARY KEY (id);


--
-- Name: pk_permissionscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY permissionscheme
    ADD CONSTRAINT pk_permissionscheme PRIMARY KEY (id);


--
-- Name: pk_pluginstate; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY pluginstate
    ADD CONSTRAINT pk_pluginstate PRIMARY KEY (pluginkey);


--
-- Name: pk_pluginversion; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY pluginversion
    ADD CONSTRAINT pk_pluginversion PRIMARY KEY (id);


--
-- Name: pk_portalpage; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY portalpage
    ADD CONSTRAINT pk_portalpage PRIMARY KEY (id);


--
-- Name: pk_portletconfiguration; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY portletconfiguration
    ADD CONSTRAINT pk_portletconfiguration PRIMARY KEY (id);


--
-- Name: pk_priority; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY priority
    ADD CONSTRAINT pk_priority PRIMARY KEY (id);


--
-- Name: pk_project; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project
    ADD CONSTRAINT pk_project PRIMARY KEY (id);


--
-- Name: pk_project_key; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY project_key
    ADD CONSTRAINT pk_project_key PRIMARY KEY (id);


--
-- Name: pk_projectcategory; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectcategory
    ADD CONSTRAINT pk_projectcategory PRIMARY KEY (id);


--
-- Name: pk_projectrole; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectrole
    ADD CONSTRAINT pk_projectrole PRIMARY KEY (id);


--
-- Name: pk_projectroleactor; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectroleactor
    ADD CONSTRAINT pk_projectroleactor PRIMARY KEY (id);


--
-- Name: pk_projectversion; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY projectversion
    ADD CONSTRAINT pk_projectversion PRIMARY KEY (id);


--
-- Name: pk_propertydata; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertydata
    ADD CONSTRAINT pk_propertydata PRIMARY KEY (id);


--
-- Name: pk_propertydate; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertydate
    ADD CONSTRAINT pk_propertydate PRIMARY KEY (id);


--
-- Name: pk_propertydecimal; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertydecimal
    ADD CONSTRAINT pk_propertydecimal PRIMARY KEY (id);


--
-- Name: pk_propertyentry; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertyentry
    ADD CONSTRAINT pk_propertyentry PRIMARY KEY (id);


--
-- Name: pk_propertynumber; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertynumber
    ADD CONSTRAINT pk_propertynumber PRIMARY KEY (id);


--
-- Name: pk_propertystring; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertystring
    ADD CONSTRAINT pk_propertystring PRIMARY KEY (id);


--
-- Name: pk_propertytext; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY propertytext
    ADD CONSTRAINT pk_propertytext PRIMARY KEY (id);


--
-- Name: pk_qrtz_calendars; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT pk_qrtz_calendars PRIMARY KEY (calendar_name);


--
-- Name: pk_qrtz_cron_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT pk_qrtz_cron_triggers PRIMARY KEY (id);


--
-- Name: pk_qrtz_fired_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT pk_qrtz_fired_triggers PRIMARY KEY (entry_id);


--
-- Name: pk_qrtz_job_details; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT pk_qrtz_job_details PRIMARY KEY (id);


--
-- Name: pk_qrtz_job_listeners; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT pk_qrtz_job_listeners PRIMARY KEY (id);


--
-- Name: pk_qrtz_simple_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT pk_qrtz_simple_triggers PRIMARY KEY (id);


--
-- Name: pk_qrtz_trigger_listeners; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT pk_qrtz_trigger_listeners PRIMARY KEY (id);


--
-- Name: pk_qrtz_triggers; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT pk_qrtz_triggers PRIMARY KEY (id);


--
-- Name: pk_remembermetoken; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY remembermetoken
    ADD CONSTRAINT pk_remembermetoken PRIMARY KEY (id);


--
-- Name: pk_remotelink; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY remotelink
    ADD CONSTRAINT pk_remotelink PRIMARY KEY (id);


--
-- Name: pk_replicatedindexoperation; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY replicatedindexoperation
    ADD CONSTRAINT pk_replicatedindexoperation PRIMARY KEY (id);


--
-- Name: pk_resolution; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY resolution
    ADD CONSTRAINT pk_resolution PRIMARY KEY (id);


--
-- Name: pk_rundetails; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY rundetails
    ADD CONSTRAINT pk_rundetails PRIMARY KEY (id);


--
-- Name: pk_schemeissuesecurities; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY schemeissuesecurities
    ADD CONSTRAINT pk_schemeissuesecurities PRIMARY KEY (id);


--
-- Name: pk_schemeissuesecuritylevels; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY schemeissuesecuritylevels
    ADD CONSTRAINT pk_schemeissuesecuritylevels PRIMARY KEY (id);


--
-- Name: pk_schemepermissions; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY schemepermissions
    ADD CONSTRAINT pk_schemepermissions PRIMARY KEY (id);


--
-- Name: pk_searchrequest; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY searchrequest
    ADD CONSTRAINT pk_searchrequest PRIMARY KEY (id);


--
-- Name: pk_sequence_value_item; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sequence_value_item
    ADD CONSTRAINT pk_sequence_value_item PRIMARY KEY (seq_name);


--
-- Name: pk_serviceconfig; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY serviceconfig
    ADD CONSTRAINT pk_serviceconfig PRIMARY KEY (id);


--
-- Name: pk_sharepermissions; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY sharepermissions
    ADD CONSTRAINT pk_sharepermissions PRIMARY KEY (id);


--
-- Name: pk_trackback_ping; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY trackback_ping
    ADD CONSTRAINT pk_trackback_ping PRIMARY KEY (id);


--
-- Name: pk_trustedapp; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY trustedapp
    ADD CONSTRAINT pk_trustedapp PRIMARY KEY (id);


--
-- Name: pk_upgradehistory; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY upgradehistory
    ADD CONSTRAINT pk_upgradehistory PRIMARY KEY (upgradeclass);


--
-- Name: pk_upgradeversionhistory; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY upgradeversionhistory
    ADD CONSTRAINT pk_upgradeversionhistory PRIMARY KEY (targetbuild);


--
-- Name: pk_userassociation; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userassociation
    ADD CONSTRAINT pk_userassociation PRIMARY KEY (source_name, sink_node_id, sink_node_entity, association_type);


--
-- Name: pk_userbase; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userbase
    ADD CONSTRAINT pk_userbase PRIMARY KEY (id);


--
-- Name: pk_userhistoryitem; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userhistoryitem
    ADD CONSTRAINT pk_userhistoryitem PRIMARY KEY (id);


--
-- Name: pk_userpickerfilter; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userpickerfilter
    ADD CONSTRAINT pk_userpickerfilter PRIMARY KEY (id);


--
-- Name: pk_userpickerfiltergroup; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userpickerfiltergroup
    ADD CONSTRAINT pk_userpickerfiltergroup PRIMARY KEY (id);


--
-- Name: pk_userpickerfilterrole; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY userpickerfilterrole
    ADD CONSTRAINT pk_userpickerfilterrole PRIMARY KEY (id);


--
-- Name: pk_versioncontrol; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY versioncontrol
    ADD CONSTRAINT pk_versioncontrol PRIMARY KEY (id);


--
-- Name: pk_votehistory; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY votehistory
    ADD CONSTRAINT pk_votehistory PRIMARY KEY (id);


--
-- Name: pk_workflowscheme; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY workflowscheme
    ADD CONSTRAINT pk_workflowscheme PRIMARY KEY (id);


--
-- Name: pk_workflowschemeentity; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY workflowschemeentity
    ADD CONSTRAINT pk_workflowschemeentity PRIMARY KEY (id);


--
-- Name: pk_worklog; Type: CONSTRAINT; Schema: public; Owner: -; Tablespace: 
--

ALTER TABLE ONLY worklog
    ADD CONSTRAINT pk_worklog PRIMARY KEY (id);


--
-- Name: action_authorcreated; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX action_authorcreated ON jiraaction USING btree (issueid, author, created);


--
-- Name: action_authorupdated; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX action_authorupdated ON jiraaction USING btree (issueid, author, updated);


--
-- Name: action_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX action_issue ON jiraaction USING btree (issueid, actiontype);


--
-- Name: attach_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX attach_issue ON fileattachment USING btree (issueid);


--
-- Name: avatar_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX avatar_index ON avatar USING btree (avatartype, owner);


--
-- Name: cf_cfoption; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cf_cfoption ON customfieldoption USING btree (customfield);


--
-- Name: cf_userpickerfiltergroup; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cf_userpickerfiltergroup ON userpickerfiltergroup USING btree (userpickerfilter);


--
-- Name: cf_userpickerfilterrole; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cf_userpickerfilterrole ON userpickerfilterrole USING btree (userpickerfilter);


--
-- Name: cfvalue_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cfvalue_issue ON customfieldvalue USING btree (issue, customfield);


--
-- Name: chggroup_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX chggroup_issue ON changegroup USING btree (issueid);


--
-- Name: chgitem_chggrp; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX chgitem_chggrp ON changeitem USING btree (groupid);


--
-- Name: chgitem_field; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX chgitem_field ON changeitem USING btree (field);


--
-- Name: cl_searchrequest; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cl_searchrequest ON columnlayout USING btree (searchrequest);


--
-- Name: cl_username; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX cl_username ON columnlayout USING btree (username);


--
-- Name: cluster_lock_name_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX cluster_lock_name_idx ON clusterlockstatus USING btree (lock_name);


--
-- Name: confcontext; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX confcontext ON configurationcontext USING btree (projectcategory, project, customfield);


--
-- Name: confcontextfieldconfigscheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX confcontextfieldconfigscheme ON configurationcontext USING btree (fieldconfigscheme);


--
-- Name: confcontextprojectkey; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX confcontextprojectkey ON configurationcontext USING btree (project, customfield);


--
-- Name: draft_workflow_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX draft_workflow_scheme ON draftworkflowschemeentity USING btree (scheme);


--
-- Name: draft_workflow_scheme_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX draft_workflow_scheme_parent ON draftworkflowscheme USING btree (workflow_scheme_id);


--
-- Name: entityproperty_identiti; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX entityproperty_identiti ON entity_property USING btree (entity_name, entity_id, property_key);


--
-- Name: entpropindexdoc_module; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX entpropindexdoc_module ON entity_property_index_document USING btree (plugin_key, module_key);


--
-- Name: ext_entity_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ext_entity_name ON external_entities USING btree (name);


--
-- Name: favourite_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX favourite_index ON favouriteassociations USING btree (username, entitytype, entityid);


--
-- Name: fc_fieldid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fc_fieldid ON fieldconfiguration USING btree (fieldid);


--
-- Name: fcs_fieldid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fcs_fieldid ON fieldconfigscheme USING btree (fieldid);


--
-- Name: fcs_issuetype; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fcs_issuetype ON fieldconfigschemeissuetype USING btree (issuetype);


--
-- Name: fcs_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fcs_scheme ON fieldconfigschemeissuetype USING btree (fieldconfigscheme);


--
-- Name: feature_id_userkey; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX feature_id_userkey ON feature USING btree (id, user_key);


--
-- Name: fieldid_fieldconf; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldid_fieldconf ON optionconfiguration USING btree (fieldid, fieldconfig);


--
-- Name: fieldid_optionid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldid_optionid ON optionconfiguration USING btree (fieldid, optionid);


--
-- Name: fieldlayout_layout; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldlayout_layout ON fieldlayoutschemeentity USING btree (fieldlayout);


--
-- Name: fieldlayout_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldlayout_scheme ON fieldlayoutschemeentity USING btree (scheme);


--
-- Name: fieldscitem_tab; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldscitem_tab ON fieldscreenlayoutitem USING btree (fieldscreentab);


--
-- Name: fieldscreen_field; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldscreen_field ON fieldscreenlayoutitem USING btree (fieldidentifier);


--
-- Name: fieldscreen_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldscreen_scheme ON issuetypescreenschemeentity USING btree (fieldscreenscheme);


--
-- Name: fieldscreen_tab; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fieldscreen_tab ON fieldscreentab USING btree (fieldscreen);


--
-- Name: fl_scheme_assoc; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX fl_scheme_assoc ON fieldlayoutschemeassociation USING btree (project, issuetype);


--
-- Name: historystep_entryid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX historystep_entryid ON os_historystep USING btree (entry_id);


--
-- Name: idx_all_project_ids; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_all_project_ids ON project_key USING btree (project_id);


--
-- Name: idx_all_project_keys; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX idx_all_project_keys ON project_key USING btree (project_key);


--
-- Name: idx_audit_item_log_id2; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_audit_item_log_id2 ON audit_item USING btree (log_id);


--
-- Name: idx_audit_log_created; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_audit_log_created ON audit_log USING btree (created);


--
-- Name: idx_changed_value_log_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_changed_value_log_id ON audit_changed_value USING btree (log_id);


--
-- Name: idx_directory_active; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_directory_active ON cwd_directory USING btree (active);


--
-- Name: idx_directory_impl; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_directory_impl ON cwd_directory USING btree (lower_impl_class);


--
-- Name: idx_directory_type; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_directory_type ON cwd_directory USING btree (directory_type);


--
-- Name: idx_display_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_display_name ON cwd_user USING btree (lower_display_name);


--
-- Name: idx_email_address; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_email_address ON cwd_user USING btree (lower_email_address);


--
-- Name: idx_first_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_first_name ON cwd_user USING btree (lower_first_name);


--
-- Name: idx_group_active; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_group_active ON cwd_group USING btree (lower_group_name, active);


--
-- Name: idx_group_attr_dir_name_lval; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_group_attr_dir_name_lval ON cwd_group_attributes USING btree (directory_id, attribute_name, lower_attribute_value);


--
-- Name: idx_group_dir_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_group_dir_id ON cwd_group USING btree (directory_id);


--
-- Name: idx_last_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_last_name ON cwd_user USING btree (lower_last_name);


--
-- Name: idx_mem_dir_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_mem_dir_child ON cwd_membership USING btree (lower_child_name, membership_type, directory_id);


--
-- Name: idx_mem_dir_parent; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_mem_dir_parent ON cwd_membership USING btree (lower_parent_name, membership_type, directory_id);


--
-- Name: idx_mem_dir_parent_child; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_mem_dir_parent_child ON cwd_membership USING btree (lower_parent_name, lower_child_name, membership_type, directory_id);


--
-- Name: idx_old_issue_key; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX idx_old_issue_key ON moved_issue_key USING btree (old_issue_key);


--
-- Name: idx_project_key; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX idx_project_key ON project USING btree (pkey);


--
-- Name: idx_qrtz_ft_inst_job_req_rcvry; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_inst_job_req_rcvry ON jquartz_fired_triggers USING btree (sched_name, instance_name, requests_recovery);


--
-- Name: idx_qrtz_ft_j_g; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_j_g ON jquartz_fired_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_ft_jg; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_jg ON jquartz_fired_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_ft_t_g; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_t_g ON jquartz_fired_triggers USING btree (sched_name, trigger_name, trigger_group);


--
-- Name: idx_qrtz_ft_tg; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_tg ON jquartz_fired_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON jquartz_fired_triggers USING btree (sched_name, instance_name);


--
-- Name: idx_qrtz_j_g; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_j_g ON jquartz_triggers USING btree (sched_name, trigger_group);


--
-- Name: idx_qrtz_j_grp; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_j_grp ON jquartz_job_details USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_j_req_recovery ON jquartz_job_details USING btree (sched_name, requests_recovery);


--
-- Name: idx_qrtz_j_state; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_j_state ON jquartz_triggers USING btree (sched_name, trigger_state);


--
-- Name: idx_qrtz_t_c; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_c ON jquartz_triggers USING btree (sched_name, calendar_name);


--
-- Name: idx_qrtz_t_j; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_j ON jquartz_triggers USING btree (sched_name, job_name, job_group);


--
-- Name: idx_qrtz_t_jg; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_jg ON jquartz_triggers USING btree (sched_name, job_group);


--
-- Name: idx_qrtz_t_n_g_state; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_n_g_state ON jquartz_triggers USING btree (sched_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_n_state; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_n_state ON jquartz_triggers USING btree (sched_name, trigger_name, trigger_group, trigger_state);


--
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_next_fire_time ON jquartz_triggers USING btree (sched_name, next_fire_time);


--
-- Name: idx_qrtz_t_nft_misfire; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_nft_misfire ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_nft_st ON jquartz_triggers USING btree (sched_name, trigger_state, next_fire_time);


--
-- Name: idx_qrtz_t_nft_st_misfire; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_nft_st_misfire ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_state);


--
-- Name: idx_qrtz_t_nft_st_misfire_grp; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_qrtz_t_nft_st_misfire_grp ON jquartz_triggers USING btree (sched_name, misfire_instr, next_fire_time, trigger_group, trigger_state);


--
-- Name: idx_user_attr_dir_name_lval; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX idx_user_attr_dir_name_lval ON cwd_user_attributes USING btree (directory_id, attribute_name, lower_attribute_value);


--
-- Name: index_ao_563aee_act1642652291; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_act1642652291 ON "AO_563AEE_ACTIVITY_ENTITY" USING btree ("OBJECT_ID");


--
-- Name: index_ao_563aee_act1978295567; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_act1978295567 ON "AO_563AEE_ACTIVITY_ENTITY" USING btree ("TARGET_ID");


--
-- Name: index_ao_563aee_act972488439; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_act972488439 ON "AO_563AEE_ACTIVITY_ENTITY" USING btree ("ICON_ID");


--
-- Name: index_ao_563aee_act995325379; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_act995325379 ON "AO_563AEE_ACTIVITY_ENTITY" USING btree ("ACTOR_ID");


--
-- Name: index_ao_563aee_obj696886343; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_obj696886343 ON "AO_563AEE_OBJECT_ENTITY" USING btree ("IMAGE_ID");


--
-- Name: index_ao_563aee_tar521440921; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_563aee_tar521440921 ON "AO_563AEE_TARGET_ENTITY" USING btree ("IMAGE_ID");


--
-- Name: index_ao_e8b6cc_bra1368852151; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_bra1368852151 ON "AO_E8B6CC_BRANCH_HEAD_MAPPING" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_bra405461593; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_bra405461593 ON "AO_E8B6CC_BRANCH" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_com1773674409; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_com1773674409 ON "AO_E8B6CC_COMMIT_ISSUE_KEY" USING btree ("COMMIT_ID");


--
-- Name: index_ao_e8b6cc_git1804640320; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_git1804640320 ON "AO_E8B6CC_GIT_HUB_EVENT" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_iss1229805759; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_iss1229805759 ON "AO_E8B6CC_ISSUE_TO_CHANGESET" USING btree ("CHANGESET_ID");


--
-- Name: index_ao_e8b6cc_iss1325927291; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_iss1325927291 ON "AO_E8B6CC_ISSUE_TO_BRANCH" USING btree ("BRANCH_ID");


--
-- Name: index_ao_e8b6cc_mes1391090780; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_mes1391090780 ON "AO_E8B6CC_MESSAGE_TAG" USING btree ("MESSAGE_ID");


--
-- Name: index_ao_e8b6cc_mes344532677; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_mes344532677 ON "AO_E8B6CC_MESSAGE_QUEUE_ITEM" USING btree ("MESSAGE_ID");


--
-- Name: index_ao_e8b6cc_pr_1045528152; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_pr_1045528152 ON "AO_E8B6CC_PR_TO_COMMIT" USING btree ("REQUEST_ID");


--
-- Name: index_ao_e8b6cc_pr_1105917040; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_pr_1105917040 ON "AO_E8B6CC_PR_PARTICIPANT" USING btree ("PULL_REQUEST_ID");


--
-- Name: index_ao_e8b6cc_pr_1458633226; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_pr_1458633226 ON "AO_E8B6CC_PR_TO_COMMIT" USING btree ("COMMIT_ID");


--
-- Name: index_ao_e8b6cc_rep1082901832; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_rep1082901832 ON "AO_E8B6CC_REPO_TO_CHANGESET" USING btree ("REPOSITORY_ID");


--
-- Name: index_ao_e8b6cc_rep922992576; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX index_ao_e8b6cc_rep922992576 ON "AO_E8B6CC_REPO_TO_CHANGESET" USING btree ("CHANGESET_ID");


--
-- Name: issue_assignee; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issue_assignee ON jiraissue USING btree (assignee);


--
-- Name: issue_proj_num; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX issue_proj_num ON jiraissue USING btree (issuenum, project);


--
-- Name: issue_proj_status; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issue_proj_status ON jiraissue USING btree (project, issuestatus);


--
-- Name: issue_updated; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issue_updated ON jiraissue USING btree (updated);


--
-- Name: issue_workflow; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issue_workflow ON jiraissue USING btree (workflow_id);


--
-- Name: issuelink_dest; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issuelink_dest ON issuelink USING btree (destination);


--
-- Name: issuelink_src; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issuelink_src ON issuelink USING btree (source);


--
-- Name: issuelink_type; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX issuelink_type ON issuelink USING btree (linktype);


--
-- Name: label_fieldissue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX label_fieldissue ON label USING btree (issue, fieldid);


--
-- Name: label_fieldissuelabel; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX label_fieldissuelabel ON label USING btree (issue, fieldid, label);


--
-- Name: label_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX label_issue ON label USING btree (issue);


--
-- Name: label_label; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX label_label ON label USING btree (label);


--
-- Name: linktypename; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX linktypename ON issuelinktype USING btree (linkname);


--
-- Name: linktypestyle; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX linktypestyle ON issuelinktype USING btree (pstyle);


--
-- Name: managedconfigitem_id_type_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX managedconfigitem_id_type_idx ON managedconfigurationitem USING btree (item_id, item_type);


--
-- Name: mshipbase_group; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mshipbase_group ON membershipbase USING btree (group_name);


--
-- Name: mshipbase_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX mshipbase_user ON membershipbase USING btree (user_name);


--
-- Name: node_id_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX node_id_idx ON nodeindexcounter USING btree (node_id, sending_node_id);


--
-- Name: node_operation_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX node_operation_idx ON replicatedindexoperation USING btree (node_id, affected_index, operation, index_time);


--
-- Name: node_sink; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX node_sink ON nodeassociation USING btree (sink_node_id, sink_node_entity);


--
-- Name: node_source; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX node_source ON nodeassociation USING btree (source_node_id, source_node_entity);


--
-- Name: notif_source; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX notif_source ON notificationinstance USING btree (source);


--
-- Name: ntfctn_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ntfctn_scheme ON notification USING btree (scheme);


--
-- Name: oauth_consumer_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX oauth_consumer_index ON oauthconsumer USING btree (consumer_key);


--
-- Name: oauth_consumer_service_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX oauth_consumer_service_index ON oauthconsumer USING btree (consumerservice);


--
-- Name: oauth_consumer_token_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX oauth_consumer_token_index ON oauthconsumertoken USING btree (token);


--
-- Name: oauth_consumer_token_key_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX oauth_consumer_token_key_index ON oauthconsumertoken USING btree (token_key);


--
-- Name: oauth_sp_consumer_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX oauth_sp_consumer_index ON oauthspconsumer USING btree (consumer_key);


--
-- Name: oauth_sp_consumer_key_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX oauth_sp_consumer_key_index ON oauthsptoken USING btree (consumer_key);


--
-- Name: oauth_sp_token_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX oauth_sp_token_index ON oauthsptoken USING btree (token);


--
-- Name: osgroup_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX osgroup_name ON groupbase USING btree (groupname);


--
-- Name: osproperty_all; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX osproperty_all ON propertyentry USING btree (entity_id);


--
-- Name: osproperty_entityname; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX osproperty_entityname ON propertyentry USING btree (entity_name);


--
-- Name: osproperty_propertykey; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX osproperty_propertykey ON propertyentry USING btree (property_key);


--
-- Name: osuser_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX osuser_name ON userbase USING btree (username);


--
-- Name: ppage_username; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX ppage_username ON portalpage USING btree (username);


--
-- Name: prmssn_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX prmssn_scheme ON schemepermissions USING btree (scheme);


--
-- Name: remembermetoken_username_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX remembermetoken_username_index ON remembermetoken USING btree (username);


--
-- Name: remotelink_globalid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX remotelink_globalid ON remotelink USING btree (globalid);


--
-- Name: remotelink_issueid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX remotelink_issueid ON remotelink USING btree (issueid, globalid);


--
-- Name: role_player_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX role_player_idx ON projectroleactor USING btree (projectroleid, pid);


--
-- Name: rundetails_jobid_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX rundetails_jobid_idx ON rundetails USING btree (job_id);


--
-- Name: rundetails_starttime_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX rundetails_starttime_idx ON rundetails USING btree (start_time);


--
-- Name: screenitem_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX screenitem_scheme ON fieldscreenschemeitem USING btree (fieldscreenscheme);


--
-- Name: searchrequest_filternamelower; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX searchrequest_filternamelower ON searchrequest USING btree (filtername_lower);


--
-- Name: sec_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sec_scheme ON schemeissuesecurities USING btree (scheme);


--
-- Name: sec_security; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sec_security ON schemeissuesecurities USING btree (security);


--
-- Name: share_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX share_index ON sharepermissions USING btree (entityid, entitytype);


--
-- Name: source_destination_node_idx; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX source_destination_node_idx ON clustermessage USING btree (source_node, destination_node);


--
-- Name: sr_author; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX sr_author ON searchrequest USING btree (authorname);


--
-- Name: subscrpt_user; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX subscrpt_user ON filtersubscription USING btree (filter_i_d, username);


--
-- Name: subscrptn_group; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX subscrptn_group ON filtersubscription USING btree (filter_i_d, groupname);


--
-- Name: trustedapp_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX trustedapp_id ON trustedapp USING btree (application_id);


--
-- Name: type_key; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX type_key ON genericconfiguration USING btree (datatype, datakey);


--
-- Name: uh_type_user_entity; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uh_type_user_entity ON userhistoryitem USING btree (entitytype, username, entityid);


--
-- Name: uk_application_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_application_name ON cwd_application USING btree (lower_application_name);


--
-- Name: uk_directory_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX uk_directory_name ON cwd_directory USING btree (lower_directory_name);


--
-- Name: uk_group_attr_name_lval; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_group_attr_name_lval ON cwd_group_attributes USING btree (group_id, attribute_name, lower_attribute_value);


--
-- Name: uk_group_name_dir_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_group_name_dir_id ON cwd_group USING btree (lower_group_name, directory_id);


--
-- Name: uk_lower_user_name; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_lower_user_name ON app_user USING btree (lower_user_name);


--
-- Name: uk_mem_parent_child_type; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_mem_parent_child_type ON cwd_membership USING btree (parent_id, child_id, membership_type);


--
-- Name: uk_user_attr_name_lval; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX uk_user_attr_name_lval ON cwd_user_attributes USING btree (user_id, attribute_name);


--
-- Name: uk_user_externalid_dir_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX uk_user_externalid_dir_id ON cwd_user USING btree (external_id, directory_id);


--
-- Name: uk_user_key; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_user_key ON app_user USING btree (user_key);


--
-- Name: uk_user_name_dir_id; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE UNIQUE INDEX uk_user_name_dir_id ON cwd_user USING btree (lower_user_name, directory_id);


--
-- Name: upf_customfield; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX upf_customfield ON userpickerfilter USING btree (customfield);


--
-- Name: upf_fieldconfigid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX upf_fieldconfigid ON userpickerfilter USING btree (customfieldconfig);


--
-- Name: user_sink; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_sink ON userassociation USING btree (sink_node_id, sink_node_entity);


--
-- Name: user_source; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX user_source ON userassociation USING btree (source_name);


--
-- Name: userpref_portletconfiguration; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX userpref_portletconfiguration ON gadgetuserpreference USING btree (portletconfiguration);


--
-- Name: votehistory_issue_index; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX votehistory_issue_index ON votehistory USING btree (issueid);


--
-- Name: wf_entryid; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX wf_entryid ON os_currentstep USING btree (entry_id);


--
-- Name: workflow_scheme; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX workflow_scheme ON workflowschemeentity USING btree (scheme);


--
-- Name: worklog_author; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX worklog_author ON worklog USING btree (author);


--
-- Name: worklog_issue; Type: INDEX; Schema: public; Owner: -; Tablespace: 
--

CREATE INDEX worklog_issue ON worklog USING btree (issueid);


--
-- Name: fk_ao_563aee_activity_entity_actor_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_activity_entity_actor_id FOREIGN KEY ("ACTOR_ID") REFERENCES "AO_563AEE_ACTOR_ENTITY"("ID");


--
-- Name: fk_ao_563aee_activity_entity_icon_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_activity_entity_icon_id FOREIGN KEY ("ICON_ID") REFERENCES "AO_563AEE_MEDIA_LINK_ENTITY"("ID");


--
-- Name: fk_ao_563aee_activity_entity_object_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_activity_entity_object_id FOREIGN KEY ("OBJECT_ID") REFERENCES "AO_563AEE_OBJECT_ENTITY"("ID");


--
-- Name: fk_ao_563aee_activity_entity_target_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_ACTIVITY_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_activity_entity_target_id FOREIGN KEY ("TARGET_ID") REFERENCES "AO_563AEE_TARGET_ENTITY"("ID");


--
-- Name: fk_ao_563aee_object_entity_image_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_OBJECT_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_object_entity_image_id FOREIGN KEY ("IMAGE_ID") REFERENCES "AO_563AEE_MEDIA_LINK_ENTITY"("ID");


--
-- Name: fk_ao_563aee_target_entity_image_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_563AEE_TARGET_ENTITY"
    ADD CONSTRAINT fk_ao_563aee_target_entity_image_id FOREIGN KEY ("IMAGE_ID") REFERENCES "AO_563AEE_MEDIA_LINK_ENTITY"("ID");


--
-- Name: fk_ao_e8b6cc_branch_head_mapping_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH_HEAD_MAPPING"
    ADD CONSTRAINT fk_ao_e8b6cc_branch_head_mapping_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_branch_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_BRANCH"
    ADD CONSTRAINT fk_ao_e8b6cc_branch_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_commit_issue_key_commit_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_COMMIT_ISSUE_KEY"
    ADD CONSTRAINT fk_ao_e8b6cc_commit_issue_key_commit_id FOREIGN KEY ("COMMIT_ID") REFERENCES "AO_E8B6CC_COMMIT"("ID");


--
-- Name: fk_ao_e8b6cc_git_hub_event_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_GIT_HUB_EVENT"
    ADD CONSTRAINT fk_ao_e8b6cc_git_hub_event_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_issue_to_branch_branch_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_BRANCH"
    ADD CONSTRAINT fk_ao_e8b6cc_issue_to_branch_branch_id FOREIGN KEY ("BRANCH_ID") REFERENCES "AO_E8B6CC_BRANCH"("ID");


--
-- Name: fk_ao_e8b6cc_issue_to_changeset_changeset_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_ISSUE_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_issue_to_changeset_changeset_id FOREIGN KEY ("CHANGESET_ID") REFERENCES "AO_E8B6CC_CHANGESET_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_message_queue_item_message_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_QUEUE_ITEM"
    ADD CONSTRAINT fk_ao_e8b6cc_message_queue_item_message_id FOREIGN KEY ("MESSAGE_ID") REFERENCES "AO_E8B6CC_MESSAGE"("ID");


--
-- Name: fk_ao_e8b6cc_message_tag_message_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_MESSAGE_TAG"
    ADD CONSTRAINT fk_ao_e8b6cc_message_tag_message_id FOREIGN KEY ("MESSAGE_ID") REFERENCES "AO_E8B6CC_MESSAGE"("ID");


--
-- Name: fk_ao_e8b6cc_pr_participant_pull_request_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_PARTICIPANT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_participant_pull_request_id FOREIGN KEY ("PULL_REQUEST_ID") REFERENCES "AO_E8B6CC_PULL_REQUEST"("ID");


--
-- Name: fk_ao_e8b6cc_pr_to_commit_commit_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_to_commit_commit_id FOREIGN KEY ("COMMIT_ID") REFERENCES "AO_E8B6CC_COMMIT"("ID");


--
-- Name: fk_ao_e8b6cc_pr_to_commit_request_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_PR_TO_COMMIT"
    ADD CONSTRAINT fk_ao_e8b6cc_pr_to_commit_request_id FOREIGN KEY ("REQUEST_ID") REFERENCES "AO_E8B6CC_PULL_REQUEST"("ID");


--
-- Name: fk_ao_e8b6cc_repo_to_changeset_changeset_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_changeset_id FOREIGN KEY ("CHANGESET_ID") REFERENCES "AO_E8B6CC_CHANGESET_MAPPING"("ID");


--
-- Name: fk_ao_e8b6cc_repo_to_changeset_repository_id; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY "AO_E8B6CC_REPO_TO_CHANGESET"
    ADD CONSTRAINT fk_ao_e8b6cc_repo_to_changeset_repository_id FOREIGN KEY ("REPOSITORY_ID") REFERENCES "AO_E8B6CC_REPOSITORY_MAPPING"("ID");


--
-- PostgreSQL database dump complete
--

