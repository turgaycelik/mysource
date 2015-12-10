<%@ page import="com.atlassian.jira.ComponentManager,
                 com.atlassian.jira.action.ActionContextKit,
                 com.atlassian.jira.bc.security.login.LoginInfo,
                 com.atlassian.jira.bc.security.login.LoginLoggers,
                 com.atlassian.jira.bc.security.login.LoginReason" %>
<%@ page import="com.atlassian.jira.bc.security.login.LoginResult" %>
<%@ page import="com.atlassian.jira.bc.security.login.LoginService" %>
<%@ page import="com.atlassian.jira.config.properties.APKeys" %>
<%@ page import="com.atlassian.jira.config.properties.ApplicationProperties" %>
<%@ page import="com.atlassian.jira.util.JiraUtils" %>
<%@ page import="com.atlassian.jira.web.action.JiraWebActionSupport" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ page import="com.atlassian.seraph.filter.LoginFilter" %>
<%@ page import="com.atlassian.seraph.filter.LoginFilterRequest" %>
<%@ page import="com.opensymphony.util.TextUtils" %>
<%@ page import="webwork.action.ActionContext" %>
<%@ page import="com.atlassian.seraph.auth.AuthenticationErrorType" %>
<%@ page import="com.atlassian.jira.user.util.UserManager" %>
<%@ page import="com.atlassian.jira.security.JiraAuthenticationContext" %>
<%@ page import="com.atlassian.jira.util.JiraContactHelper" %>
<%@ page import="com.atlassian.jira.util.I18nHelper" %>
<%@ page import="com.atlassian.sal.api.user.UserRole" %>
<%@ page import="com.atlassian.jira.web.filters.JiraLoginInterceptor" %>
<%@ taglib prefix="ww" uri="webwork" %>
<%@ taglib prefix="aui" uri="webwork" %>
<%@ taglib prefix="page" uri="sitemesh-page" %>

<%
    final LoginService loginService = ComponentManager.getComponentInstanceOfType(LoginService.class);
    final ApplicationProperties applicationProperties = ComponentManager.getComponentInstanceOfType(ApplicationProperties.class);
    final UserManager userManager = ComponentManager.getComponentInstanceOfType(UserManager.class);
    final JiraAuthenticationContext jiraAuthenticationContext = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class);

    final boolean allowCookies = applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOW_COOKIES);
    final boolean publicSignUpAllowed = JiraUtils.isPublicMode();
    final boolean showPermissionViolationError = request.getParameter("permissionViolation") != null;

    // Continue to support JIRA_OPTION_USER_EXTERNALMGT for Forgot Login until we get Read-only Internal Directory
    final boolean externalUserManagement = applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    final boolean showForgotLoginDetails = !externalUserManagement && userManager.hasPasswordWritableDirectory();

    final String authStatus = LoginFilterRequest.getAuthenticationStatus(request);
    final AuthenticationErrorType authErrorType = LoginFilterRequest.getAuthenticationErrorType(request);
    final LoginResult lastLoginResult = (LoginResult) request.getAttribute(LoginService.LOGIN_RESULT);

    final LoginInfo loginInfo = lastLoginResult == null ? null : lastLoginResult.getLoginInfo();
    final boolean isElevatedSecurityCheckShown = loginService.isElevatedSecurityCheckAlwaysShown() ||
            (loginInfo != null && loginInfo.isElevatedSecurityCheckRequired());

    final boolean failedAuthorisation = lastLoginResult != null && lastLoginResult.getReason() == LoginReason.AUTHORISATION_FAILED;
    final boolean failedElevatedSecurityCheck = lastLoginResult != null && lastLoginResult.getReason() == LoginReason.AUTHENTICATION_DENIED;

    request.setAttribute("loggedInUser", jiraAuthenticationContext.getLoggedInUser() == null ? null : jiraAuthenticationContext.getLoggedInUser().getDisplayName());

    if (LoginLoggers.LOGIN_PAGE_LOG.isDebugEnabled())
    {
        LoginLoggers.LOGIN_PAGE_LOG.debug("login.jsp called with lastLoginResult : " + String.valueOf(lastLoginResult));        
    }

    // added so that webworks getText exists
    ActionContext currentContext = ActionContext.getContext();
    if (currentContext == null)
    {
        ActionContextKit.setContext(request,response,request.getContextPath());
        JiraWebActionSupport fakeAction = new JiraWebActionSupport(){    };
        ActionContext.getValueStack().pushValue(fakeAction);
    }

    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:captcha");

    request.setAttribute("os_destination", request.getParameter("os_destination") == null ? "/secure/" : TextUtils.htmlEncode(request.getParameter("os_destination")));

    // set a header so that javascript can know if they have been redirected
    if (showPermissionViolationError)
    {
        response.setHeader("X-Atlassian-Dialog-Control", "permissionviolation");
    }

    // Get the contact administrators message
    I18nHelper i18nHelper = ComponentManager.getComponentInstanceOfType(JiraAuthenticationContext.class).getI18nHelper();
    JiraContactHelper jiraContactHelper = ComponentManager.getComponentInstanceOfType(JiraContactHelper.class);
    final String contactAdministratorLink = jiraContactHelper.getAdministratorContactLinkHtml(request.getContextPath(), i18nHelper);

    // Check if we are expecting some certain user role
    UserRole userRole = JiraLoginInterceptor.getUserRole(request);
    if (userRole  != null)
    {
        request.setAttribute("userRole", userRole.toString());
    }
%>
<page:applyDecorator id="login-form" name="auiform">
    <page:param name="action"><%= request.getContextPath() %>/login.jsp</page:param>
    <page:param name="method">post</page:param>
    <page:param name="submitButtonName">login</page:param>
    <page:param name="submitButtonText"><ww:text name="'common.concepts.login'"/></page:param>

    <page:capHide value="IFRAME">
        <% if (showForgotLoginDetails) { %>
                <page:param name="cancelLinkURI"><ww:url value="'/secure/ForgotLoginDetails.jspa'" atltoken="false"/></page:param>
                <page:param name="cancelLinkText"><ww:text name="'common.concepts.forgotpassword'"/></page:param>
        <% } %>
    </page:capHide>

    <% if (showPermissionViolationError) { %>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                    <ww:if test="@loggedInUser != null">
                        <p><ww:text name="'login.required.loggedin.permissionviolation'">
                                <ww:param name="'value0'"><ww:property value="@loggedInUser"/></ww:param>
                            </ww:text>
                        </p>
                    </ww:if>

                    <ww:if test="@userRole == 'SYSADMIN'">
                        <p><ww:text name="'login.required.sysadmin.privileges.required'"/></p>
                    </ww:if>
                    <ww:elseIf test="@userRole == 'ADMIN'">
                        <p><ww:text name="'login.required.administrator.privileges.required'"/></p>
                    </ww:elseIf>
                    <ww:elseIf test="@loggedInUser == null">
                        <p><ww:text name="'login.required.notloggedin.permissionviolation'"/></p>
                    </ww:elseIf>

                <ww:if test="@userRole == null">
                    <p>
                    <ww:text name="'contact.admin.for.perm'">
                        <ww:param name="'value0'"><%=contactAdministratorLink%></ww:param>
                    </ww:text>
                </p>
                    </ww:if>
            </aui:param>
        </aui:component>
    <% } %>

    <% if (authStatus != null) {
        if (authStatus.equals(LoginFilter.LOGIN_FAILED)) {
            if (failedAuthorisation) { %>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <ww:text name="'login.error.permission'">
                                <ww:param name="'value0'"><%=contactAdministratorLink%></ww:param>
                            </ww:text>
                        </p>
                    </aui:param>
                </aui:component>
            <% } else { %>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">error</aui:param>
                    <aui:param name="'messageHtml'">
                        <p>
                            <% if (failedElevatedSecurityCheck) { %>
                                <ww:text name="'login.error.captcha.incorrect'"/>
                            <% } else { %>
                                <ww:text name="'login.error.lpincorrect'"/>
                            <% } %>
                        </p>
                    </aui:param>
                </aui:component>
            <% }
        } else if (authStatus.equals(LoginFilter.LOGIN_ERROR)) { %>
            <aui:component template="auimessage.jsp" theme="'aui'">
                <aui:param name="'messageType'">error</aui:param>
                <aui:param name="'messageHtml'">
                    <p>
                        <% if (AuthenticationErrorType.CommunicationError.equals(authErrorType)) { %>
                            <ww:text name="'login.error.communication'"/>
                        <% } else { %>
                            <ww:text name="'login.error.misc'"/>
                        <% } %>
                    </p>
                </aui:param>
            </aui:component>
        <% }
    } %>
    <page:applyDecorator name="auifieldset">

        <page:applyDecorator name="auifieldgroup">
            <label accesskey="<ww:text name="'alt.text.username.accessKey'"/>" for="login-form-username"><ww:text name="'alt.text.username'"/></label>
            <input class="text medium-field" id="login-form-username" name="os_username" type="text" value="<ww:property value="$os_username" />" />
        </page:applyDecorator>

        <page:applyDecorator name="auifieldgroup">
            <label accesskey="<ww:text name="'alt.text.password.accessKey'"/>" for="login-form-password" id="passwordlabel"><ww:text name="'alt.text.password'"/></label>
            <input id="login-form-password" class="text medium-field" name="os_password" type="password" />
        </page:applyDecorator>
        
        <% if (allowCookies) { %>
        <page:capHide value="IFRAME">
            <page:applyDecorator name="auifieldset">
                <page:param name="type">group</page:param>
                <page:applyDecorator name="auifieldgroup">
                    <page:param name="type">checkbox</page:param>
                    <input class="checkbox" id="login-form-remember-me" name="os_cookie" type="checkbox" value="true" />
                    <label for="login-form-remember-me" accesskey="<ww:text name="'alt.text.rememberlogin.accessKey'"/>"><ww:text name="'alt.text.rememberlogin'"/></label>
                </page:applyDecorator>
            </page:applyDecorator>
        </page:capHide>
        <% } %>

        <% if (isElevatedSecurityCheckShown) { %>
        <page:applyDecorator name="auifieldgroup">
            <aui:component label="text('signup.captcha.text')" id="'os-captcha'" name="'os_captcha'"  template="captcha.jsp" theme="'aui'" >
                <aui:param name="'captchaURI'"><%=request.getContextPath() %>/captcha?__r=<%=System.currentTimeMillis()%></aui:param>
                <aui:param name="'iconText'"><ww:text name="'login.captcha.refresh'" /></aui:param>
                <aui:param name="'iconURI'">#login-form-os-captcha</aui:param>
                <aui:param name="'iconTitle'"><ww:text name="'login.captcha.refresh'" /></aui:param>
                <aui:param name="'iconCssClass'">icon-reload</aui:param>
                <aui:param name="'size'">medium</aui:param>
            </aui:component>
        </page:applyDecorator>
        <% } %>
        <page:capHide value="IFRAME">
            <div id="sign-up-hint" class="field-group">
                <% if (publicSignUpAllowed) { %>
                <ww:text name="'login.signup'">
                    <ww:param name="'value0'"><a id="signup" href="<%= request.getContextPath() %>/secure/Signup!default.jspa" tabindex="-1"></ww:param>
                    <ww:param name="'value1'"></a></ww:param>
                </ww:text>
                <% } else { %>
                <ww:text name="'login.requestaccount'">
                    <ww:param name="'value0'"><%=contactAdministratorLink%></ww:param>
                </ww:text>
                <% } %>
            </div>
        </page:capHide>
    </page:applyDecorator>
    <ww:component name="'os_destination'" template="hidden.jsp" theme="'aui'" value="$os_destination" />
    <page:capAttr name="pageCaps">
        <ww:component name="'page_caps'" template="hidden.jsp" theme="'aui'" value="@pageCaps" />
    </page:capAttr>
    <ww:component name="'user_role'" template="hidden.jsp" theme="'aui'" value="@userRole" />
</page:applyDecorator>
<%
    if (currentContext == null)
    {
        ActionContextKit.resetContext();
    }
%>
