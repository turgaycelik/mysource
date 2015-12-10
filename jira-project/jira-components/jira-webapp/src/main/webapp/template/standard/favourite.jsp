<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:ajax-favourite-control");
%>
<div id="fav_div_<ww:property value="parameters['tableId']"/>_<ww:property value="parameters['entityType']" />_<ww:property value="parameters['entityId']" />" rel="<ww:property value="parameters['entityId']" />">
    <a id="fav_a_<ww:property value="parameters['tableId']" />_<ww:property value="parameters['entityType']" />_<ww:property value="parameters['entityId']" />"
            class="fav-link icon enabled" title="<ww:if test="parameters['enabled'] == 'true'"><ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/></ww:if><ww:else><ww:text name="'common.favourites.disabled.' +  parameters['entityType']"/></ww:else>" href="#">
        <span><ww:if test="parameters['enabled'] == 'true'"><ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/></ww:if><ww:else><ww:text name="'common.favourites.disabled.' +  parameters['entityType']"/></ww:else></span></a>
</div>

<fieldset rel="<ww:property value="parameters['tableId']"/>_<ww:property value="parameters['entityType']" />_<ww:property value="parameters['entityId']"/>" class="hidden favourite-params">
    <input type="hidden" id="enabled" value="<ww:if test="parameters['enabled'] == 'true'">true</ww:if><ww:else>false</ww:else>">
    <input type="hidden" id="tableId" value="<ww:property value="parameters['tableId']" />">
    <input type="hidden" id="entityType" value="<ww:property value="parameters['entityType']"/>">
    <input type="hidden" id="entityId" value="<ww:property value="parameters['entityId']"/>">
    <ww:if test="parameters['relatedDropdown']">
        <input type="hidden" id="relatedDropdown" value="<ww:property value="parameters['relatedDropdown']"/>">
    </ww:if>
    <input type="hidden" id="removeId" value="<ww:property value="parameters['removeId']"/>">
    <input type="hidden" id="titleAdd" value="<ww:text name="'common.favourites.enabled.' +  parameters['entityType']"/>"/>
    <input type="hidden" id="titleRemove" value="<ww:text name="'common.favourites.disabled.' +  parameters['entityType']"/>"/>
    <ww:if test="parameters['removeId']">
        <input type="hidden" id="undoText" value="<ww:text name="'common.favourites.undo.' +  parameters['entityType']" />&nbsp;<a href='#'><ww:text name="'common.favourites.undo'"/></a>"/>
        <input type="hidden" id="undoEntityName" value="<ww:property value="parameters['entityName']"/>"/> 
    </ww:if>
    <input type="hidden" id="errorMsg" value="<ww:text name="'common.favourites.error.server'"/>"/>
</fieldset>

