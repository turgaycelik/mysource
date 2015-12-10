<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="ui" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>

<ww:declare id="varCssClass" value="parameters['cssClass']"><ww:if test="."><ww:property value="."/></ww:if></ww:declare>
<jsp:include page="/template/aui/formFieldLabel.jsp" />
<ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.avatar.avatar'">
    <ui:param name="'tagName'" value="'span'" />
    <ui:param name="'size'" value="parameters['size']" />
    <ui:param name="'avatarImageUrl'" value="parameters['src']" />
    <ui:param name="'accessibilityText'" value="parameters['title']" />
    <ui:param name="'isProject'" value="parameters['isProject']" />
    <ui:param name="'imageClasses'">jira-inline-avatar-picker-trigger<ww:property value="parameters['imageClasses']"><ww:if test="."> <ww:property value="."/></ww:if></ww:property></ui:param>
</ui:soy>
<span id="attach-max-size" class="hidden">130000000</span>
<span id="default-avatar-id" class="hidden"><ww:property value="parameters['defaultId']"><ww:if test="."><ww:property value="."/></ww:if></ww:property></span>
<span id="avatar-owner-id" class="hidden"><ww:property value="parameters['avatarOwnerId']"><ww:if test="."><ww:property value="."/></ww:if></ww:property></span>
<span id="avatar-owner-key" class="hidden"><ww:property value="parameters['avatarOwnerKey']"><ww:if test="."><ww:property value="."/></ww:if></ww:property></span>
<span id="avatar-type" class="hidden"><ww:property value="@varType" /></span>