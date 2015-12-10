<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<ww:if test="/noTitle == false">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'tagName'" value="'div'"/>
        <ui:param name="'mainContent'">
            <h2><ww:property value="/labelForSelectedTab"/></h2>
        </ui:param>
    </ui:soy>
</ww:if>
<ww:property value="/htmlForSelectedTab" escape="false"/>
