<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<html>
<head>
    <title><ww:text name="'common.concepts.releasenotes'" /></title>
</head>
<body>
    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">
                    <ww:property value="releaseNote" escape="false"/>
                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</body>
</html>
