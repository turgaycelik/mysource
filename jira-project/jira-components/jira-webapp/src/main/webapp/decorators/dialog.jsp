<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib prefix="decorator" uri="sitemesh-decorator" %>
<ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.JiraDialog.dialogChrome'">
    <ui:param name="'bodyContent'">
        <div class="dialog-title hidden"><decorator:getProperty property="title" /></div>
        <decorator:body />
    </ui:param>
</ui:soy>
