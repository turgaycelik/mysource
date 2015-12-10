<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<%@ taglib uri="jiratags" prefix="jira" %>
<html>
<head>
    <ww:if test="/issueValid == true">
        <title><ww:text name="'viewissue.commentdelete.title'"/></title>
        <meta name="decorator" content="issueaction" />
        <link rel="index" href="<ww:url value="/issuePath" atltoken="false" />" />
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="/issueValid == true">
    <page:applyDecorator id="comment-delete" name="auiform">
        <page:param name="action">DeleteComment.jspa</page:param>
        <page:param name="submitButtonName">Delete</page:param>
        <page:param name="submitButtonText"><ww:text name="'common.words.delete'"/></page:param>
        <page:param name="cancelLinkURI"><ww:url value="/issuePath" atltoken="false" /></page:param>

        <aui:component template="formHeading.jsp" theme="'aui'">
            <aui:param name="'text'"><ww:text name="'viewissue.commentdelete.title'"/></aui:param>
        </aui:component>

        <aui:component name="'commentId'" template="hidden.jsp" theme="'aui'" />
        <aui:component name="'id'" template="hidden.jsp" theme="'aui'" />

        <p><ww:text name="'viewissue.commentdelete.message'" /></p>
        <page:applyDecorator name="auifieldset">

            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'comment-author'" label="text('viewissue.comment.author')" name="'comment-author'" template="formFieldValue.jsp" theme="'aui'">
                    <aui:param name="'texthtml'"><jira:formatuser userKey="/commentAuthorKey" type="'profileLink'" id="'comment_summary'" /></aui:param>
                </aui:component>
            </page:applyDecorator>
            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'comment-created'" label="text('viewissue.comment.created')" name="'comment-created'" template="formFieldValue.jsp" theme="'aui'">
                    <aui:param name="'texthtml'"><ww:property value="/dmyDateFormatter/format(/commentObject/created)" /></aui:param>
                </aui:component>
            </page:applyDecorator>
            <ww:if test="/commentObject/created/equals(/commentObject/updated) == false">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-update-author'" label="text('viewissue.comment.update.author')" name="'comment-update-author'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><jira:formatuser userKey="/commentUpdateAuthorKey" type="'profileLink'" id="'comment_summary_updated'" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-updated'" label="text('viewissue.comment.updated')" name="'comment-updated'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><ww:property value="/dmyDateFormatter/format(/commentObject/updated)" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
            </ww:if>
            <ww:if test="/commentObject/roleLevel != null">
                <page:applyDecorator name="auifieldgroup">
                    <aui:component id="'comment-visibleby'" label="text('viewissue.comment.visibleby')" name="'comment-visibleby'" template="formFieldValue.jsp" theme="'aui'">
                        <aui:param name="'texthtml'"><ww:property value="/commentObject/roleLevel" /></aui:param>
                    </aui:component>
                </page:applyDecorator>
            </ww:if>
            <page:applyDecorator name="auifieldgroup">
                <aui:component id="'comment-content'" label="text('viewissue.comment.label')" name="'comment-content'" template="formFieldValue.jsp" theme="'aui'">
                    <aui:param name="'texthtml'"><ww:property value="/renderedContent()" escape="'false'" /></aui:param>
                </aui:component>
            </page:applyDecorator>
        </page:applyDecorator>

    </page:applyDecorator>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <%@ include file="/includes/issue/generic-errors.jsp" %>
    </div>
</ww:else>
</body>
</html>
