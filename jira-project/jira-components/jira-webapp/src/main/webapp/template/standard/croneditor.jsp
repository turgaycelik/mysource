<%@ page import="com.atlassian.jira.web.component.cron.CronEditorWebComponent" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%--
    Renders the cron editor within a jira form. The cronEditorBean is a reference to a CronEditorBean
    that the component will use the renderer the editor.
    <ww:component name="'cron.editor.name'" template="croneditor.jsp" >
        <ww:param name="'cronEditorBean'">/cronEditorBean</ww:param>
        <ww:param name="'parameterPrefix'">filter.subscriptions.prefix</ww:param>
    </ww:component>">
--%>
<%
    CronEditorWebComponent cronEditorWebComponent = new CronEditorWebComponent();
    request.setAttribute("cronEditorWebComponent", cronEditorWebComponent);
%>
<ww:if test="parameters['cronEditorBean'] != true">
    <ww:if test="parameters['parameterPrefix'] != true">
        <ww:property
                value="@cronEditorWebComponent/html(parameters['cronEditorBean'], parameters['parameterPrefix'],  errors[parameters['name']])"
                escape="false"/>
    </ww:if>
    <ww:else>
        <ww:property value="@cronEditorWebComponent/html(parameters['cronEditorBean'], null, errors[parameters['name']])" escape="false"/>
    </ww:else>
</ww:if>