<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'admin.jellyrunner.jelly.runner'"/></title>
    <meta name="admin.active.section" content="admin_system_menu/advanced_menu_section/advanced_section"/>
    <meta name="admin.active.tab" content="jelly_runner"/>
</head>
<body>
<ww:if test="allowedToRun == true">
    <page:applyDecorator name="jiraform">
        <page:param name="action">JellyRunner.jspa</page:param>
        <page:param name="title"><ww:text name="'admin.jellyrunner.jelly.runner'"/></page:param>
        <page:param name="description">
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">info</aui:param>
                <aui:param name="'messageHtml'">
                    <ww:text name="'admin.jellyrunner.note'">
                        <ww:param name="'value0'"><span></ww:param>
                        <ww:param name="'value1'"></span></ww:param>
                    </ww:text>
                </aui:param>
            </aui:component>
            <p><ww:text name="'admin.jellyrunner.instruction'"/></p>
        </page:param>
        <page:param name="width">100%</page:param>
        <page:param name="helpURL">jelly</page:param>
        <page:param name="submitId">run_submit</page:param>
        <page:param name="submitName"><ww:text name="'common.forms.run.now'"/></page:param>
        <ui:textfield label="text('admin.jellyrunner.jelly.script.file.path')" name="'filename'" size="'60'" maxlength="'255'" />
        <tr>
            <td class="fieldLabelArea"><ww:text name="'common.words.or'"/></td>
            <td class="fieldValueArea">&nbsp;</td>
        </tr>
        <ui:textarea label="text('admin.jellyrunner.jelly.script.xml')" name="'script'" rows="'40'" cols="'80'"/>
    </page:applyDecorator>
</ww:if>
<ww:else>
    <page:applyDecorator name="jirapanel">
    <page:param name="title"><ww:text name="'admin.jellyrunner.jelly.runner'"/></page:param>
    <page:param name="helpURL">jelly</page:param>
    <page:param name="description">
    <p>
        <ww:bean name="'com.atlassian.jira.web.util.HelpUtil'" id="helpUtil" />
        <ww:property value="@helpUtil/helpPath('jelly')">
        <ww:text name="'admin.jellyrunner.disabled'">
            <ww:param name="'value0'"><a href="<ww:property value="url" />" target="_jirahelp"></ww:param>
            <ww:param name="'value1'"></a></ww:param>
        </ww:text><br>
        </ww:property>
    </p>
    </page:param>
    </page:applyDecorator>
</ww:else>
</body>
</html>
