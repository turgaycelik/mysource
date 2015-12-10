<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<page:applyDecorator id="edit-profile" name="auiform">
    <page:param name="action">EditProfile.jspa</page:param>
    <page:param name="submitButtonName">Edit</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/ViewProfile.jspa'" atltoken="false"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'editprofile.title'"/></aui:param>
    </aui:component>

    <ww:if test="/remoteUser == null">
        <page:param name="useCustomButtons">true</page:param>
        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'session.timeout.message.title'"/></p>
                <p>
                    <ww:text name="'editprofile.must.log.in'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa"></ww:param>
                        <ww:param name="param1"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:elseIf test="/remoteUser/name != /username">
        <page:param name="useCustomButtons">true</page:param>
        <aui:component template="formDescriptionBlock.jsp" theme="'aui'">
            <aui:param name="'messageHtml'">
                <p><ww:text name="'editprofile.own.profile'"/></p>
                <p>
                    <ww:text name="'editprofile.logged.in.as'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/secure/ViewProfile.jspa"><ww:property value="/remoteUser/displayName"/></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:elseIf>
    <ww:else>
        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'preferences.update.user.details'"/></page:param>

            <aui:component name="'username'" template="hidden.jsp" theme="'aui'" value="/username"/>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield id="'fullname'" label="text('common.words.fullname')" mandatory="'true'" maxlength="'255'" name="'fullName'" size="'medium'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:textfield id="'email'" label="text('common.words.email')" mandatory="'true'" maxlength="'255'" name="'email'" size="'medium'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <page:param name="description"><ww:text name="'user.profile.password.reason'"/></page:param>
                <aui:password id="'password'" label="text('common.words.password')" mandatory="'true'" maxlength="'255'" name="'password'" size="'medium'" theme="'aui'"/>
            </page:applyDecorator>

        </page:applyDecorator>
    </ww:else>
</page:applyDecorator>