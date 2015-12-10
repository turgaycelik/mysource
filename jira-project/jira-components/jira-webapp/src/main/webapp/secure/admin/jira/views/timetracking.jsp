<%@ page import="com.atlassian.jira.component.ComponentAccessor" %>
<%@ page import="com.atlassian.plugin.webresource.UrlMode" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <meta name="admin.active.section" content="admin_system_menu/top_system_section/issue_features"/>
    <meta name="admin.active.tab" content="timetracking"/>
    <title><ww:text name="'admin.globalsettings.timetracking.jira.time.tracking'"/></title>
</head>
<body>
<ww:if test="timeTracking == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">TimeTrackingDeActivate.jspa</page:param>
        <page:param name="submitId">deactivate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.deactivate'"/></page:param>
        <page:param name="title">
            <ww:text name="'admin.globalsettings.timetracking.status'">
                <ww:param name="'value0'"><span class="status-active"></ww:param>
                <ww:param name="'value1'"><ww:text name="'admin.common.words.on'"/></ww:param>
                <ww:param name="'value2'"></span></ww:param>
            </ww:text>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">configure_timetracking</page:param>
        <page:param name="description">

            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <ww:text name="'admin.globalsettings.timetracking.instruction'">
                            <ww:param name="'value0'"><b></ww:param>
                            <ww:param name="'value1'"></b></ww:param>
                        </ww:text>
                    </p>
                </aui:param>
            </aui:component>


            <p>
            
            <ww:text name="'admin.globalsettings.timetracking.current.hours.per.day'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:property value="hoursPerDay"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text><br>
            <ww:text name="'admin.globalsettings.timetracking.current.days.per.week'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:property value="daysPerWeek"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text><br>
            <ww:text name="'admin.globalsettings.timetracking.format'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:property value="timeTrackingFormatSample"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text><br>
            <ww:text name="'admin.globalsettings.timetracking.default.unit.current'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:property value="defaultUnitText"/></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text><br>
            <ww:if test="/legacyMode == true">
                <ww:text name="'admin.globalsettings.timetracking.legacy.mode.status'">
                    <ww:param name="'value0'"><span class="status-active" id="legacy-on"></ww:param>
                    <ww:param name="'value1'"><ww:text name="'admin.common.words.on'"/></ww:param>
                    <ww:param name="'value2'"></span></ww:param>
                </ww:text><br>
            </ww:if>
            <ww:text name="'admin.globalsettings.timetracking.copy.comment.status'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"><ww:if test="/copyComment == true"><ww:text name="'admin.common.words.enabled'"/></ww:if><ww:else><ww:text name="'admin.common.words.disabled'"/></ww:else></ww:param>
                <ww:param name="'value2'"></b></ww:param>
            </ww:text><br>

            </p>
            <ww:if test="/issueOperationsPluginEnabled == false">
                <p><ww:text name="'admin.globalsettings.timetracking.issue.operations.plugin.disabled.active'">
                    <ww:param name="'value0'"><span class="status-inactive"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                    <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm#manage/com.atlassian.jira.plugin.system.issueoperations"></ww:param>
                    <ww:param name="'value3'"></a></ww:param>
                </ww:text></p>
            </ww:if>
            <ww:elseIf test="/logWorkModuleEnabled == false">
                <p><ww:text name="'admin.globalsettings.timetracking.log.work.module.disabled.active'">
                    <ww:param name="'value0'"><span class="status-inactive"></ww:param>
                    <ww:param name="'value1'"></span></ww:param>
                    <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm#manage/com.atlassian.jira.plugin.system.issueoperations"></ww:param>
                    <ww:param name="'value3'"></a></ww:param>
                </ww:text></p>
            </ww:elseIf>
            <p><ww:text name="'admin.globalsettings.timetracking.instruction2'">
                <ww:param name="'value0'"><b></ww:param>
                <ww:param name="'value1'"></b></ww:param>
                <ww:param name="'value2'"><a href="ViewPermissionSchemes.jspa"></ww:param>
                <ww:param name="'value3'"></a></ww:param>
            </ww:text></p>
            <p><ww:text name="'admin.globalsettings.timetracking.deactivate.time.tracking.below'"/></p>
        </page:param>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jiraform">
        <page:param name="action">TimeTrackingActivate.jspa</page:param>
        <page:param name="submitId">activate_submit</page:param>
        <page:param name="submitName"><ww:text name="'admin.common.words.activate'"/></page:param>
        <page:param name="title">
            <ww:text name="'admin.globalsettings.timetracking.status'">
                <ww:param name="'value0'"><span class="status-inactive"></ww:param>
                <ww:param name="'value1'"><ww:text name="'admin.common.words.off'"/></ww:param>
                <ww:param name="'value2'"></span></ww:param>
            </ww:text>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">configure_timetracking</page:param>
        <page:param name="description">
            <ww:if test="/issueOperationsPluginEnabled == false">
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'admin.globalsettings.timetracking.issue.operations.plugin.disabled.inactive'">
                                <ww:param name="'value0'"><span></ww:param>
                                <ww:param name="'value1'"></span></ww:param>
                                <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm#manage/com.atlassian.jira.plugin.system.issueoperations"></ww:param>
                                <ww:param name="'value3'"></a></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            </ww:if>
            <ww:elseIf test="/logWorkModuleEnabled == false"><p>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">info</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'admin.globalsettings.timetracking.log.work.module.disabled.inactive'">
                                <ww:param name="'value0'"><span></ww:param>
                                <ww:param name="'value1'"></span></ww:param>
                                <ww:param name="'value2'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm#manage/com.atlassian.jira.plugin.system.issueoperations"></ww:param>
                                <ww:param name="'value3'"></a></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            </ww:elseIf>
            <p><ww:text name="'admin.globalsettings.timetracking.activate.time.tracking.below'"/></p>
        </page:param>
        <ui:textfield label="text('admin.globalsettings.timetracking.hours.per.day')" name="'hoursPerDay'" size="'10'">
            <ui:param name="'description'"><ww:text name="'admin.globalsettings.timetracking.specify.working.hours'"/></ui:param>
        </ui:textfield>
        <ui:textfield label="text('admin.globalsettings.timetracking.days.per.week')" name="'daysPerWeek'" size="'10'">
            <ui:param name="'description'"><ww:text name="'admin.globalsettings.timetracking.specify.working.days'"/></ui:param>
        </ui:textfield>
        <ui:radio label="text('admin.globalsettings.timetracking.format.label')" name="'timeTrackingFormat'" list="timeTrackingFormats" listKey="'id'" listValue="'name'"/>
        <ui:select label="text('admin.globalsettings.timetracking.default.unit.label')" name="'defaultUnit'" list="units" listKey="'key'" listValue="'value'">
             <ww:param name="'description'"><ww:text name="'admin.globalsettings.timetracking.default.unit.description'"/></ww:param>
         </ui:select>
        <ui:checkbox label="text('admin.globalsettings.timetracking.legacy.mode.fieldname')" name="'legacyMode'" fieldValue="true">
                <ww:if test="/legacyMode == true"><ui:param name="'checked'">checked</ui:param></ww:if>
                <ui:param name="'description'"><ww:text name="'admin.globalsettings.timetracking.legacy.mode.desc'"/></ui:param>
        </ui:checkbox>
        <ui:checkbox label="text('admin.globalsettings.timetracking.copy.comment.fieldname')" name="'copyComment'" fieldValue="true">
                <ww:if test="/copyComment == true"><ui:param name="'checked'">checked</ui:param></ww:if>
                <ui:param name="'description'"><ww:text name="'admin.globalsettings.timetracking.copy.comment.desc'"/></ui:param>
        </ui:checkbox>
    </page:applyDecorator>
</ww:else>

<ww:if test="/onDemand == false">
<img src="<%= ComponentAccessor.getWebResourceUrlProvider().getStaticResourcePrefix(UrlMode.AUTO) %>/images/icons/marketplace-ico.png" width="16" height="16" border="0" />
<ww:text name="'admin.globalsettings.timetracking.marketplace.link'">
    <ww:param name="'value0'"><a href="<%=request.getContextPath()%>/plugins/servlet/upm/marketplace/featured?category=Worklog+%26+Time-tracking&source=time_tracking_admin" style="vertical-align: text-bottom"></ww:param>
    <ww:param name="'value1'"></a></ww:param>
</ww:text>
</ww:if>

</body>
</html>
