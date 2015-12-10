<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:share-types");
%>
<jsp:include page="/template/standard/controlheader.jsp" />
<div>
    <ww:property value="parameters['shareTypeList']">
        <div id="share_div" style="display:none">
            <select id="share_type_selector" name="searchShareType">
                <option value="any"><ww:text name="'common.sharing.shared.description.anyone'"/></option>
                <ww:iterator value=".">
                    <option value="<ww:property value="./shareType"/>">
                        <ww:property value="./shareTypeLabel"/></option>
                </ww:iterator>
            </select>
            <span id="share_any" style="display:none"></span>
            <ww:iterator value="." status="'typeStatus'">
                    <span id="share_<ww:property value="./shareType"/>" style="display:none"><ww:property value="./shareTypeSelector" escape="false"/></span>
            </ww:iterator>
        </div>
        <div class="fieldDescription" id="share_type_description"></div>
        <span id="shares_data" style="display:none;"><ww:property value="parameters['dataString']"/></span>
        <fieldset class="hidden parameters">
            <input type="hidden" title="paramAnyDescription" value="<ww:property value="parameters['anyDescription']"/>"/>
        </fieldset>
        <script type="text/javascript">
            AJS.$(function() {
                AJS.populateParameters();
                <ww:iterator value=".">
                    <ww:iterator value="./translatedTemplates">
                        JIRA.Share.i18n["<ww:property value="key"/>"] = "<ww:property value="value" escape="false"/>";
                    </ww:iterator>
                    JIRA.Share.i18n["share_any_description"] = AJS.params.paramAnyDescription;
                </ww:iterator>
                JIRA.Share.registerSelectShareTypes();
            });
        </script>
    </ww:property>
</div>
<jsp:include page="/template/standard/controlfooter.jsp" />
