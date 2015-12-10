<%@ page import="com.atlassian.jira.bc.JiraServiceContext" %>
<%@ page import="com.atlassian.jira.bc.JiraServiceContextImpl" %>
<%@ page import="com.atlassian.jira.util.system.ExtendedSystemInfoUtils" %>
<%@ page import="webwork.action.CoreActionContext" %>
<%@ page import="java.util.Set" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<ww:bean id="math" name="'com.atlassian.core.bean.MathBean'"/>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/troubleshooting_and_support"/>
    <meta name="admin.active.tab" content="system_info"/>
	<title><ww:text name="'admin.systeminfo.system.info'"/></title>
</head>
<body>
    <header class="aui-page-header">
        <div class="aui-page-header-inner">
            <div class="aui-page-header-main">
                <h2><ww:text name="'admin.systeminfo.system.info'"/></h2>
            </div>
            <div class="aui-page-header-actions">
                <aui:component name="'system_information_help'" template="help.jsp" theme="'aui'" />
            </div>
        </div>
    </header>
    <ww:if test="/warningMessages/size() > 0">
        <aui:component template="module.jsp" theme="'aui'">
            <aui:param name="'contentHtml'">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'id'">environment_warnings</aui:param>
                    <aui:param name="'titleText'"><ww:text name="'admin.systeminfo.environment.warnings'"/></aui:param>
                    <aui:param name="'messageHtml'">
                        <ul>
                            <ww:iterator value="/warningMessages" status="'status'">
                                <li><ww:property value="." escape="false" /></li>
                            </ww:iterator>
                        </ul>
                    </aui:param>
                </aui:component>
            </aui:param>
        </aui:component>
    </ww:if>

    <%-- System info --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">server-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.server.info'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="system_info_table">
                <tbody>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.generalconfiguration.base.url'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/baseUrl"/></td>
                    </tr>
                <ww:iterator value="/extendedSystemInfoUtils/props(true)" status="'status'">
                    <tr>
                        <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                        <td class="cell-type-value"><ww:property value="value" /></td>
                    </tr>
                </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Patches: Shown when we have an actual set of patches, which most of the time is not the case. --%>
    <ww:if test="appliedPatches/size() > 0">
        <aui:component template="module.jsp" theme="'aui'">
            <aui:param name="'id'">patches-info</aui:param>
            <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.applied.patches'"/></aui:param>
            <aui:param name="'twixi'">true</aui:param>
            <aui:param name="'contentHtml'">
                <table class="aui aui-table-rowhover" id="applied_patches">
                    <tbody>
                        <ww:iterator value="appliedPatches" status="'status'">
                            <tr>
                                <td class="cell-type-key"><strong><ww:property value="issueKey" /></strong></td>
                                <td class="cell-type-value"><ww:property value="description" /></td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            </aui:param>
        </aui:component>
    </ww:if>

    <%-- Java VM Memory stats --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">jvm-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.java.vm.memory.statistics'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/jvmStats" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" /></td>
                        </tr>
                    </ww:iterator>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.memory.graph'"/></strong></td>
                        <td class="cell-type-value">
                            <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                <tr>
                                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                                        <td class="bar-status-bad" width="<ww:property value="."/>%">
                                            <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                     alt=""
                                                     height="15"
                                                     width="100%"
                                                     border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                                 <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                                             </ww:text>">
                                            </a>
                                        </td>
                                    </ww:property>
                                    <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)">
                                        <td class="bar-status-good" width="<ww:property value="."/>%">
                                            <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                                <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                     alt=""
                                                     height="15"
                                                     width="100%"
                                                     border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                     <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                                                 </ww:text>">
                                            </a>
                                        </td>
                                    </ww:property>
                                </tr>
                            </table>
                            <strong>
                                <ww:text name="'admin.systeminfo.memory.percent.free'">
                                    <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeMemory, /extendedSystemInfoUtils/systemInfoUtils/totalMemory)"/></ww:param>
                                </ww:text>
                            </strong>
                            (<ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalMemory"/> MB)
                            <span>(<a href="<ww:url value="'ViewSystemInfo!garbageCollection.jspa'" atltoken="false" />"><ww:text name="'admin.systeminfo.force.garbage.collection'"><ww:param name="value0">garbage collection</ww:param></ww:text></a>)</span>
                        </td>
                    </tr>
                    <ww:if test="/extendedSystemInfoUtils/jvmJava5OrGreater == true">
                        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory != 0">
                            <tr>
                                <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.perm.gen.memory.graph'"/></strong></td>
                                <td class="cell-type-value">
                                    <table  border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                        <tr>
                                        <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedPermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                                            <td bgcolor="#CC3333" width="<ww:property value="."/>%">
                                                <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                                    <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                         alt=""
                                                         height="15"
                                                         width="100%"
                                                         border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                                     <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                                                 </ww:text>">
                                                </a>
                                            </td>
                                        </ww:property>
                                        <ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)">
                                            <td bgcolor="#00CC00" width="<ww:property value="."/>%">
                                                <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                                    <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                         alt=""
                                                         height="15"
                                                         width="100%"
                                                         border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                         <ww:param name="'value0'"><ww:property value="."/></ww:param>
                                                                     </ww:text>">
                                                </a>
                                            </td>
                                        </ww:property>
                                        </tr>
                                    </table>
                                    <strong>
                                        <ww:text name="'admin.systeminfo.memory.percent.free'">
                                            <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freePermGenMemory, /extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory)"/></ww:param>
                                        </ww:text>
                                    </strong>
                                    (<ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalPermGenMemory"/> MB)
                                </td>
                            </tr>
                        </ww:if>
                        <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory != 0">
                            <!--PermGen JVMs -->
                            <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory != 0">
                                <tr>
                                    <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.nonheap.memory.graph'"/></strong></td>
                                    <td class="cell-type-value">
                                        <table border="0" cellpadding="0" cellspacing="0" style="float:left;margin-right:8px;" width="40%">
                                            <td bgcolor="#CC3333" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/>%">
                                                <a title="<ww:text name="'admin.systeminfo.used.memory'"/>" >
                                                    <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                         alt=""
                                                         height="15"
                                                         width="100%"
                                                         border="0" title="<ww:text name="'admin.systeminfo.used.memory.percent'">
                                                                     <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                                                 </ww:text>">
                                                </a>
                                            </td>
                                            <td bgcolor="#00CC00" width="<ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/>%">
                                                <a title="<ww:text name="'admin.systeminfo.free.memory'"/>" >
                                                    <img src="<%= request.getContextPath() %>/images/border/spacer.gif"
                                                         alt=""
                                                         height="15"
                                                         width="100%"
                                                         border="0" title="<ww:text name="'admin.systeminfo.free.memory.percent'">
                                                                         <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                                                     </ww:text>">
                                                </a>
                                            </td>
                                        </table>
                                        <strong>
                                            <ww:text name="'admin.systeminfo.memory.percent.free'">
                                                <ww:param name="'value0'"><ww:property value="@math/percentage(/extendedSystemInfoUtils/systemInfoUtils/freeNonHeapMemory, /extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory)"/></ww:param>
                                            </ww:text>
                                        </strong>
                                        (<ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory"/> MB
                                        <ww:text name="'common.words.total'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory"/> MB)
                                    </td>
                                </tr>
                            </ww:if>
                            <!--non-PermGen JVMs -->
                            <ww:if test="/extendedSystemInfoUtils/systemInfoUtils/totalNonHeapMemory == 0">
                                <tr>
                                    <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.nonheap.memory.value'"/></strong></td>
                                    <td class="cell-type-value"><strong><ww:text name="'common.words.used'"/>: <ww:property value="/extendedSystemInfoUtils/systemInfoUtils/usedNonHeapMemory"/> MB</strong></td>
                                </tr>
                            </ww:if>
                        </ww:if>
                        <tr>
                            <td class="cell-type-key"></td>
                            <td class="cell-type-value">
                                <a href="<ww:url value="'ViewMemoryInfo.jspa'" atltoken="false" />">
                                    <ww:text name="'admin.systeminfo.java.vm.memory.statistics.more.info'"/>
                                </a>
                            </td>
                        </tr>
                    </ww:if>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- JIRA info --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">jira-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.jira.info'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="jirainfo">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/buildStats" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" /></td>
                        </tr>
                    </ww:iterator>
                    <ww:if test="/extendedSystemInfoUtils/upgradeHistory/empty == false">
                        <tr>
                            <td class="cell-type-key"></td>
                            <td class="cell-type-value">
                                <a id="view_upgrade_history" href="<ww:url value="'ViewUpgradeHistory.jspa'" atltoken="false" />">
                                    <ww:text name="'admin.systeminfo.upgrade.history.more.info'"/>
                                </a>
                            </td>
                        </tr>
                    </ww:if>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.generalconfiguration.installed.languages'"/></strong></td>
                        <td class="cell-type-value">
                            <ww:iterator value="/localeManager/installedLocales" status="'status'">
                                <ww:property value="/displayNameOfLocale(.)"/><ww:if test="@status/last == false"><br></ww:if>
                            </ww:iterator>
                        </td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.generalconfiguration.default.language'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/defaultLanguage" /><ww:if test="/extendedSystemInfoUtils/usingSystemLocale == true"> - <ww:text name="'admin.systeminfo.system.default.locale'"/></ww:if></td>
                    </tr>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- License info --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">patches-module</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.license.info'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="license_info">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/licenseInfo" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" /></td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Config info --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">configuration-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.common.config.info'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="common_config_info">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/commonConfigProperties" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" /></td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- DB Stats --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">database-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.database.statistics'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/usageStats" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" /></td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- File Paths --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">file-paths-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.file.paths'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="file_paths">
                <tbody>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.jira.local.home'"/></strong></td>
                        <td class="cell-type-value" id="file_paths_jiralocalhome"><ww:property value="/extendedSystemInfoUtils/jiraLocalHomeLocation" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.jira.shared.home'"/></strong></td>
                        <td class="cell-type-value" id="file_paths_jirahome"><ww:property value="/extendedSystemInfoUtils/jiraHomeLocation" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.entity.engine'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/entityEngineXmlPath" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.atlassian.jira.log'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/logPath" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.indexes'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/indexLocation" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.attachments'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/attachmentsLocation" /></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.location.of.backups'"/></strong></td>
                        <td class="cell-type-value"><ww:property value="/extendedSystemInfoUtils/backupLocation" /></td>
                    </tr>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Cluster Information --%>
    <ww:if test="/extendedSystemInfoUtils/clustered == true">
        <aui:component template="module.jsp" theme="'aui'">
            <aui:param name="'id'">listeners-info</aui:param>
            <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.cluster.nodes'"/></aui:param>
            <aui:param name="'twixi'">true</aui:param>
            <aui:param name="'contentHtml'">
                <table class="aui aui-table-rowhover">
                    <tbody>
                        <ww:iterator value="/extendedSystemInfoUtils/clusterNodeInformation" status="'status'">
                            <tr>
                                <td class="cell-type-key">
                                    <strong><ww:property value="key/nodeId" /></strong>
                                </td>
                                <td class="cell-type-value">
                                    <table cellpadding="2" cellspacing="0" border="0">
                                        <tr>
                                            <td><strong><ww:text name="'admin.systeminfo.cluster.node.state'"/>:</strong></td>
                                            <td><ww:property value="/text(./key/state/i18nKey)" /></td>
                                        </tr>
                                        <tr>
                                            <td><strong><ww:text name="'admin.systeminfo.cluster.node.live'"/>:</strong></td>
                                            <td><ww:property value="./value" /></td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            </aui:param>
        </aui:component>
    </ww:if>
    <%-- Listeners --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">listeners-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.listeners'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/listeners" status="'status'">
                        <tr>
                            <td class="cell-type-key">
                                <strong><ww:property value="string('name')" /></strong>
                                <div class="description secondary-text"><ww:property value="string('clazz')" /></div>
                            </td>
                            <td class="cell-type-value">
                                <ww:property value="propertySet(.)/keys('',5)">
                                <table cellpadding="2" cellspacing="0" border="0">
                                    <ww:if test=". != null">
                                        <ww:iterator value=".">
                                            <tr>
                                                <td><strong><ww:property value="." />:</strong></td>
                                                <td><ww:property value="propertySet(../..)/string(.)"/></td>
                                            </tr>
                                        </ww:iterator>
                                    </ww:if>
                                </table>
                                </ww:property>
                            </td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Services  --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">services-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.services'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/extendedSystemInfoUtils/services" status="'status'">
                        <tr>
                            <td class="cell-type-key">
                                <strong><ww:property value="./name" /></strong>
                                <div class="description secondary-text"><ww:property value="./serviceClass" /></div>
                            </td>
                            <td class="cell-type-value">
                                <table cellpadding="2" cellspacing="0" border="0">
                                    <tr>
                                        <td><strong><ww:text name="'admin.systeminfo.service.delay'"/>:</strong></td>
                                        <td><ww:property value="/extendedSystemInfoUtils/millisecondsToMinutes(./delay)"/> <ww:text name="'core.dateutils.minutes'"/></td>
                                    </tr>
                                    <ww:iterator value="/extendedSystemInfoUtils/servicePropertyMap(.)/entrySet">
                                    <tr>
                                        <td><strong><ww:property value="./key" />:</strong></td>
                                        <td><ww:property value="/text(./value)" /></td>
                                    </tr>
                                    </ww:iterator>
                                </table>
                            </td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- User Plugins --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">user-plugins-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.user.plugins'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <ww:if test="/userPlugins/size == 0">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'"><p><ww:text name="'admin.systeminfo.no.user.plugins.installed'"/></p></aui:param>
                </aui:component>
            </ww:if>
            <ww:else>
                <table class="aui aui-table-rowhover">
                    <tbody>
                        <ww:iterator value="/userPlugins/iterator" status="'status'">
                            <tr>
                                <td class="cell-type-key">
                                    <strong><ww:property value="./name" /></strong> - <ww:property value="./pluginInformation/version" />
                                    <div class="description secondary-text"><ww:text name="'admin.systeminfo.plugin.by'"/><ww:property value="./pluginInformation/vendorName" /></div>
                                </td>
                                <td class="cell-type-value">
                                    <table cellpadding="2" cellspacing="0" border="0">
                                        <tr>
                                            <td colspan="2">
                                                <ww:if test="./enabled == true">
                                                    <ww:text name="'admin.systeminfo.plugin.enabled'"/>
                                                </ww:if>
                                                <ww:else>
                                                    <ww:text name="'admin.systeminfo.plugin.disabled'"/>
                                                </ww:else>
                                            </td>
                                        </tr>
                                        <ww:iterator value="./pluginInformation/parameters/entrySet">
                                            <tr>
                                                <td><strong><ww:property value="./key" />:</strong></td>
                                                <td><ww:property value="/text(./value)" /></td>
                                            </tr>
                                        </ww:iterator>
                                    </table>
                                </td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            </ww:else>
        </aui:param>
    </aui:component>

    <%-- System Plugins --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">system-plugins-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.system.plugins'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="/systemPlugins" status="'status'">
                        <tr>
                            <td class="cell-type-key">
                                <strong><ww:property value="./name" /></strong> - <ww:property value="./pluginInformation/version" />
                                <div class="description secondary-text"><ww:text name="'admin.systeminfo.plugin.by'"/><ww:property value="./pluginInformation/vendorName" /></div>
                            </td>
                            <td class="cell-type-value">
                                <table cellpadding="2" cellspacing="0" border="0">
                                    <tr>
                                        <td colspan="2">
                                            <ww:if test="./enabled == true">
                                                <ww:text name="'admin.systeminfo.plugin.enabled'"/>
                                            </ww:if>
                                            <ww:else>
                                                <ww:text name="'admin.systeminfo.plugin.disabled'"/>
                                            </ww:else>
                                        </td>
                                    </tr>
                                    <ww:iterator value="./pluginInformation/parameters/entrySet">
                                    <tr>
                                        <td><strong><ww:property value="./key" />:</strong></td>
                                        <td><ww:property value="/text(./value)" /></td>
                                    </tr>
                                    </ww:iterator>
                                </table>
                            </td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Application Properties --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">application-properties-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.application.properties'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover" id="application_properties">
                <tbody>
                    <ww:iterator value="applicationPropertiesHTML" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" escape="true"/></td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- System Properties --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">system-properties-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.system.properties'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <ww:iterator value="systemPropertiesHTML" status="'status'">
                        <tr>
                            <td class="cell-type-key"><strong><ww:property value="key" /></strong></td>
                            <td class="cell-type-value"><ww:property value="value" escape="false"/></td>
                        </tr>
                    </ww:iterator>
                </tbody>
            </table>
        </aui:param>
    </aui:component>

    <%-- Trusted Applications --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">trusted-apps-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.trustedapps'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <%
                JiraServiceContext jiraServiceContext = (JiraServiceContext) CoreActionContext.getValueStack().findValue("/jiraServiceContext");
                JiraServiceContext trustedAppContext = new JiraServiceContextImpl(jiraServiceContext.getLoggedInUser());
                ExtendedSystemInfoUtils sysInfo = (ExtendedSystemInfoUtils) CoreActionContext.getValueStack().findValue("/extendedSystemInfoUtils");
                Set trustedApps = sysInfo.getTrustedApplications(trustedAppContext);
                if(trustedAppContext.getErrorCollection().hasAnyErrors()) {
            %>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'"><p><ww:text name="'admin.errors.trustedapps.no.permission'"/></p></aui:param>
                </aui:component>
            <% } else if(trustedApps.isEmpty()) { %>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'"><p><ww:text name="'admin.trustedapps.no.apps.configured'"/></p></aui:param>
                </aui:component>
            <% } else { %>
                <table class="aui aui-table-rowhover">
                    <tbody>
                        <ww:iterator value="/extendedSystemInfoUtils/trustedApplications(/jiraServiceContext)" status="'status'">
                            <tr>
                                <td class="cell-type-key"><strong><ww:property value="./name" /></strong></td>
                                <td class="cell-type-value">
                                    <table cellpadding="2" cellspacing="0" border="0">
                                        <tr>
                                            <td><strong><ww:text name="'admin.trustedapps.field.application.id'"/>:</strong></td>
                                            <td><ww:property value="./ID" /></td>
                                        </tr>
                                        <tr>
                                            <td><strong><ww:text name="'admin.trustedapps.field.timeout'"/>:</strong></td>
                                            <td><ww:property value="./timeout" /></td>
                                        </tr>
                                        <tr>
                                            <td><strong><ww:text name="'admin.trustedapps.field.ip.matches'"/>:</strong></td>
                                            <td><ww:iterator value="/extendedSystemInfoUtils/IPMatches(.)">
                                                    <ww:property value="." /><br/>
                                                </ww:iterator></td>
                                        </tr>
                                        <tr>
                                            <td><strong><ww:text name="'admin.trustedapps.field.url.matches'"/>:</strong></td>
                                            <td><ww:iterator value="/extendedSystemInfoUtils/urlMatches(.)">
                                                    <ww:property value="." /><br/>
                                                </ww:iterator></td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </ww:iterator>
                    </tbody>
                </table>
            <% } %>
        </aui:param>
    </aui:component>

    <%-- Client Info (useful to check client's IP address) --%>
    <aui:component template="module.jsp" theme="'aui'">
        <aui:param name="'id'">client-info</aui:param>
        <aui:param name="'headingText'"><ww:text name="'admin.systeminfo.client.info'"/></aui:param>
        <aui:param name="'twixi'">true</aui:param>
        <aui:param name="'contentHtml'">
            <table class="aui aui-table-rowhover">
                <tbody>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.remote.address'"/></strong></td>
                        <td class="cell-type-value"><%=request.getRemoteAddr()%></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.remote.host'"/></strong></td>
                        <td class="cell-type-value"><%=request.getRemoteHost()%></td>
                    </tr>
                    <tr>
                        <td class="cell-type-key"><strong><ww:text name="'admin.systeminfo.remote.port'"/></strong></td>
                        <td class="cell-type-value"><%=request.getRemotePort()%></td>
                    </tr>
                </tbody>
            </table>
        </aui:param>
    </aui:component>
</body>
</html>
