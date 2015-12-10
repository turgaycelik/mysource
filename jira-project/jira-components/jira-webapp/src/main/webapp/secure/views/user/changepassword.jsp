<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>
<page:applyDecorator id="change-password" name="auiform">
    <page:param name="action">ChangePassword.jspa</page:param>
    <page:param name="submitButtonName">Change</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.forms.update'"/></page:param>
    <page:param name="cancelLinkURI"><ww:url value="'/secure/ViewProfile.jspa'" atltoken="false"/></page:param>

    <aui:component template="formHeading.jsp" theme="'aui'">
        <aui:param name="'text'"><ww:text name="'common.concepts.changepassword'"/></aui:param>
    </aui:component>

    <ww:if test="/remoteUser == null">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p>
                    <ww:text name="'changepassword.must.log.in'">
                        <ww:param name="param0"><a href="<%=request.getContextPath()%>/login.jsp?os_destination=%2Fsecure%2FViewProfile.jspa"></ww:param>
                        <ww:param name="param1"></a></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:if>
    <ww:elseIf test="/remoteUser/name != /username">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'changepassword.own.profile'" /></p>
                <p>
                    <ww:text name="'changepassword.logged.in.as'">
                        <ww:param name="param0"><ww:property value="/remoteUser/displayName"/></ww:param>
                    </ww:text>
                </p>
            </aui:param>
        </aui:component>
    </ww:elseIf>
    <ww:elseIf test="/canUpdateUserPassword == false">
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">error</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'changepassword.cannot.change'" /></p>
            </aui:param>
        </aui:component>
    </ww:elseIf>
    <ww:if test="/remoteUser != null && /remoteUser/name == /username && /canUpdateUserPassword == true">
        <page:applyDecorator name="auifieldset">
            <page:param name="legend"><ww:text name="'changepassword.entry'"/></page:param>

            <aui:component name="'username'" template="hidden.jsp" theme="'aui'" value="/username"/>

            <page:applyDecorator name="auifieldgroup">
                <aui:password id="'current-password'" label="text('changepassword.current')" mandatory="'true'" name="'current'" theme="'aui'"/>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:password id="'new-password'" label="text('changepassword.new')" mandatory="'true'" name="'password'" theme="'aui'"/>
                <ww:if test="/passwordErrors/size > 0"><ul class="error"><ww:iterator value="/passwordErrors">
                    <li><ww:property value="./snippet" escape="false"/></li>
                </ww:iterator></ul></ww:if>
            </page:applyDecorator>

            <page:applyDecorator name="auifieldgroup">
                <aui:password id="'confirm-password'" label="text('changepassword.confirm')" mandatory="'true'" name="'confirm'" theme="'aui'"/>
            </page:applyDecorator>

        </page:applyDecorator>
    </ww:if>
</page:applyDecorator>