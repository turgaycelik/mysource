<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:ajax-favourite-control");
%>
<div id="fav_div_<ww:property value="parameters['fieldId']"/>">
    <a id="fav_a_<ww:property value="parameters['fieldId']"/>"
            class="fav-link icon enabled" title="<ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/>" href="#"><span><ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/></span></a>
</div>

<input id="fav_new_<ww:property value="parameters['fieldId']" />" name="favourite" type="hidden" value="<ww:if test="parameters['enabled'] == 'true'">true</ww:if><ww:else>false</ww:else>"/>

<fieldset rel="<ww:property value="parameters['fieldId']"/>" class="hidden favourite-params">
    <input type="hidden" id="remote" value="false">
    <input type="hidden" id="enabled" value="<ww:if test="parameters['enabled'] == 'true'">true</ww:if><ww:else>false</ww:else>">
    <input type="hidden" id="entityType" value="<ww:property value="parameters['entityType']"/>">
    <input type="hidden" id="fieldId" value="<ww:property value="parameters['fieldId']"/>">
    <input type="hidden" id="titleAdd" value="<ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/>"/>
    <input type="hidden" id="titleRemove" value="<ww:text name="'common.favourites.disabled.' +  parameters['entityType']"/>"/>
</fieldset>
