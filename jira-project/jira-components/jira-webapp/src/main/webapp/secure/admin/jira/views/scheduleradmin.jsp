<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>

<html>
<head>
	<title><ww:text name="'admin.schedulerdetails.scheduler.administration'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="scheduler_details"/>
</head>

<body>

<page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.schedulerdetails.scheduler.administration'"/></page:param>
    <page:param name="description">
        <ww:text name="'admin.schedulerdetails.page.description'"/>
    </page:param>
    <page:param name="width">100%</page:param>
    <ww:property value="scheduler/metaData">
    <p>
        <ww:text name="'admin.schedulerdetails.quartz.scheduler'">
            <ww:param name="'value0'"><b><ww:property value="schedulerName" /></b></ww:param>
            <ww:param name="'value1'"><b><ww:property value="schedulerInstanceId" /></b></ww:param>
        </ww:text><br/>
        <ww:text name="'admin.schedulerdetails.scheduler.has.been.running.since'">
            <ww:param name="'value0'"><b><ww:property value="/outlookDate/formatDMYHMS(runningSince)" /></b></ww:param>
            <ww:param name="'value1'"><b><ww:property value="numJobsExecuted" /></b></ww:param>
        </ww:text><br/>
        <ww:text name="'admin.schedulerdetails.job.store.is.class'">
            <ww:param name="'value0'"><b><ww:property value="jobStoreClass" /></b></ww:param>
        </ww:text>
        <ww:if test="jobStoreSupportsPersistence == false">
            <ww:text name="'admin.schedulerdetails.this.class.does.not.support.persistence'"/>
        </ww:if>
        <ww:else>
            <ww:text name="'admin.schedulerdetails.this.class.supports.persistence'"/>
        </ww:else>
        <br/>
        <ww:text name="'admin.schedulerdetails.the.thread.pool'">
            <ww:param name="'value0'"><b><ww:property value="threadPoolClass" /></b></ww:param>
            <ww:param name="'value1'"><b><ww:property value="threadPoolSize" /></b></ww:param>
        </ww:text>
    </p>
    <p>
        <ww:text name="'admin.schedulerdetails.paused'"/>:
        <ww:if test="paused == true">
            <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
        </ww:if>
        <ww:else>
            <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
        </ww:else>
        <br/>
        <ww:text name="'admin.schedulerdetails.shutdown'"/>:
        <ww:if test="shutdown == true">
            <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
        </ww:if>
        <ww:else>
            <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
        </ww:else>
    </p>
    </ww:property>
</page:applyDecorator>



<h3 class="formtitle"><ww:text name="'admin.schedulerdetails.jobs'"/></h3>
<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="25%">
                <ww:text name="'common.words.name'"/>
            </th>
            <th width="60%">
                <ww:text name="'admin.schedulerdetails.class'"/>
            </th>
            <th width="15%">
                <ww:text name="'admin.common.words.attributes'"/>
            </th>
        </tr>
    </thead>
    <tbody>
    <ww:iterator value="scheduler/jobGroupNames" status="'outerstatus'">
        <tr class="totals">
            <td colspan="4">
                <b><ww:text name="'admin.schedulerdetails.group'"/>: <ww:property value="." /></b>
            </td>
        </tr>
        <ww:iterator value="/scheduler/jobNames(.)" status="'status'">
        <ww:property value="/scheduler/jobDetail(., ..)">
            <tr>
                <td>
                    <ww:property value="name" />
                </td>
                <td>
                    <ww:property value="jobClass/name" />
                </td>
                <td>
                    <ww:text name="'admin.schedulerdetails.volatile'"/>:
                    <ww:if test="volatile == true">
                        <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
                    </ww:if>
                    <ww:else>
                        <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
                    </ww:else><br/>
                    <ww:text name="'admin.schedulerdetails.durable'"/>:
                    <ww:if test="durable == true">
                        <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
                    </ww:if>
                    <ww:else>
                        <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
                    </ww:else><br/>
                    <ww:text name="'admin.schedulerdetails.stateful'"/>:
                    <ww:if test="stateful == true">
                        <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
                    </ww:if>
                    <ww:else>
                        <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
                    </ww:else>
                </td>
            </tr>
        </ww:property>
        </ww:iterator>
    </ww:iterator>
    </tbody>
</table>

<h3 class="formtitle"><ww:text name="'admin.schedulerdetails.triggers'"/></h3>
<table class="aui aui-table-rowhover">
    <thead>
        <tr>
            <th width="25%">
                <ww:text name="'common.words.name'"/>
            </th>
            <th width="40%">
                <ww:text name="'admin.schedulerdetails.job'"/>
            </th>
            <th width="20%">
                <ww:text name="'admin.schedulerdetails.next.fire'"/>
            </th>
            <th width="15%">
                <ww:text name="'admin.common.words.attributes'"/>
            </th>
        </tr>
    </thead>
	<tbody>
    <ww:iterator value="scheduler/triggerGroupNames" status="'outerstatus'">
        <tr class="totals">
            <td colspan="4">
                <b><ww:text name="'admin.schedulerdetails.group'"/>: <ww:property value="." /></b>
            </td>
        </tr>
        <ww:iterator value="/scheduler/triggerNames(.)" status="'status'">
        <ww:property value="/scheduler/trigger(., ..)">
            <tr>
                <td>
                    <ww:property value="name" />
                </td>
                <td>
                    <ww:property value="fullJobName" />
                </td>
                <td>
                    <ww:property value="/outlookDate/formatDMYHMS(nextFireTime)" />
                </td>
                <td>
                    <ww:text name="'admin.schedulerdetails.volatile'"/>:
                    <ww:if test="volatile == true">
                        <span class="status-active"><ww:text name="'admin.common.words.true'"/></span>
                    </ww:if>
                    <ww:else>
                        <span class="status-inactive"><ww:text name="'admin.common.words.false'"/></span>
                    </ww:else>
                </td>
            </tr>
        </ww:property>
        </ww:iterator>
    </ww:iterator>
	</tbody>
</table>

</body>
</html>
