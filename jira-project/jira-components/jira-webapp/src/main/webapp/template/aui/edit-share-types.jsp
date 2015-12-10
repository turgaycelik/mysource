<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:share-types");
%>
<page:applyDecorator name="auifieldgroup">
<jsp:include page="/template/aui/formFieldLabel.jsp" />
    <div id="share_display_div"></div>
<jsp:include page="/template/aui/formFieldError.jsp" />
</page:applyDecorator>

<page:applyDecorator name="auifieldgroup">
    <ww:component template="formFieldLabel.jsp" label="text('common.sharing.new.shares')" theme="'aui'"/>
    <ww:property value="parameters['shareTypeList']">
        <div id="share_div" style="display: none;">
            <ww:if test="parameters['editEnabled'] == false"><div style="display:none"></ww:if>
                <div id="share_display_component"><ww:if test=". != null && ./size > 0">
                    <div>
                    <select class="select medium-field" id="share_type_selector">
                        <ww:iterator value=".">
                            <option value="<ww:property value="./shareType"/>"><ww:property value="./shareTypeLabel"/></option>
                        </ww:iterator>
                    </select>
                    <ww:iterator value="." status="'typeStatus'">
                        <span id="share_<ww:property value="./shareType"/>" <ww:if test="@typeStatus/first == false">style="display:none"</ww:if>>
                            <ww:property value="./shareTypeEditor" escape="false"/>
                            <ww:if test="./addButtonNeeded == true">
                                <span class="addShare" id="share_add_<ww:property value="./shareType"/>"><span class="icon jira-icon-add"></span>&nbsp;<ww:text name="'common.sharing.add.share'"/></span>
                            </ww:if>
                        </span>
                    </ww:iterator>
                    <div class="fieldDescription" id="share_type_description"></div>
                </ww:if></div></div>

            <ww:if test="parameters['editEnabled'] == false"></div></ww:if>
        </div>
        <span id="shares_data" style="display:none;"><ww:property value="parameters['dataString']"/></span>
        <input id="share_type_hidden" name="shareValues" type="text" style="display:none;"/>
        <span id="share_trash" class="icon icon-delete shareTrash" style="display:none;"><ww:text name="'common.sharing.delete.share'"/></span>
        <span id="share_icon" class="icon jira-icon-filter-public shareIcon" style="display:none;"><ww:text name="'common.sharing.share'"/></span>
        <div class="shareItem" id="empty_share" style="display:none">
            <div title="<ww:text name="'common.sharing.shared.template.private.desc'"/>">
                <span class="icon jira-icon-filter-private shareIcon"><ww:text name="'common.sharing.private'"/></span><ww:text name="'common.sharing.not.shared'"/>
            </div>
        </div>

        <fieldset class="hidden parameters">
            <input type="hidden" title="paramSubmitButtonId" value="<ww:property value="parameters['submitButtonId']"/>"/>
        </fieldset>
        <script type="text/javascript">
            AJS.$(function() {
                <ww:iterator value=".">
                    <ww:iterator value="./translatedTemplates">
                        JIRA.Share.i18n["<ww:property value="key"/>"] = "<ww:property value="value" escape="false"/>";
                    </ww:iterator>
                </ww:iterator>
                JIRA.Share.i18n["common.sharing.remove.shares"] = "<ww:text name="'common.sharing.remove.shares'"/>";
                JIRA.Share.i18n["common.sharing.dirty.warning"] = "<ww:text name="'common.sharing.dirty.warning'"/>";
                JIRA.Share.registerEditShareTypes(AJS.params.paramSubmitButtonId);
            });
        </script>
    </ww:property>
</page:applyDecorator>

