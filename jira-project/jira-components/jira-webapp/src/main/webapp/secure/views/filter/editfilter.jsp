<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
	<title><ww:text name="'editfilter.title'"/></title>
    <content tag="section">find_link</content>
    <meta name="decorator" content="panel-general" />
</head>
<body>
            <ww:if test="modified == true">
                <page:applyDecorator name="jirapanel">
                    <page:param name="width">100%</page:param>
                    <page:param name="title"><ww:text name="'editfilter.title'"/></page:param>
                        <ww:text name="'editfilter.mustSaveFirst'"/>
                        <ul>
                            <li><ww:text name="'navigator.hidden.operation.savecurrent'">
                                <ww:param name="'value0'"><a class="bolded" href="<ww:url value="'SaveFilter!default.jspa'"/>"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                            </ww:text></li>
                            <li><ww:text name="'editfilter.reloadfilter'">
                                <ww:param name="'value0'"><a class="bolded" href="<ww:url page="IssueNavigator.jspa?mode=hide"><ww:param name="'requestId'" value="searchRequest/id" /></ww:url>"></ww:param>
                                <ww:param name="'value1'"></a></ww:param>
                            </ww:text></li>
                        </ul>
                </page:applyDecorator>
            </ww:if>
            <ww:else>
                <page:applyDecorator id="filter-edit" name="auiform">
                    <page:param name="action">EditFilter.jspa</page:param>
                    <page:param name="submitButtonName">Save</page:param>
                    <page:param name="submitButtonText"><ww:text name="'common.words.save'"/></page:param>
                    <page:param name="cancelLinkURI"><ww:url value="/cancelURL" atltoken="false"/></page:param>

                    <aui:component template="formHeading.jsp" theme="'aui'">
                        <aui:param name="'text'"><ww:text name="'editfilter.title'"/></aui:param>
                        <aui:param name="'helpURL'">issue_filters</aui:param>
                        <aui:param name="'helpURLFragment'">#editing_filters</aui:param>
                    </aui:component>

                    <%--Used for warnings of filter share options--%>
                    <div id="share_warning"></div>

                    <aui:component name="'filterId'" template="hidden.jsp" theme="'aui'"/>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:textfield id="'filterName'" label="text('common.words.name')" mandatory="true" name="'filterName'" theme="'aui'" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <aui:textarea id="'filterDescription'" label="text('common.concepts.description')" mandatory="false" name="'filterDescription'" theme="'aui'" rows="4" />
                    </page:applyDecorator>

                    <page:applyDecorator name="auifieldgroup">
                        <ww:component template="formFieldLabel.jsp" label="text('common.favourites.favourite')" theme="'aui'"/>
                        <ww:component name="'favourite'" template="favourite-new.jsp" theme="'aui'">
                            <ww:param name="'enabled'"><ww:property value="./favourite" /></ww:param>
                            <ww:param name="'fieldId'">favourite</ww:param>
                            <ww:param name="'entityType'">SearchRequest</ww:param>
                        </ww:component>
                    </page:applyDecorator>

                    <ww:if test="/showShares == true">
                        <ww:component name="'shares'" label="text('common.sharing.shares')" template="edit-share-types.jsp" theme="'aui'">
                            <ww:param name="'shareTypeList'" value="/shareTypes"/>
                            <ww:param name="'editEnabled'" value="/editEnabled"/>
                            <ww:param name="'dataString'" value="/jsonString"/>
                            <ww:param name="'submitButtonId'">filter-edit-submit</ww:param>
                        </ww:component>
                    </ww:if>

                </page:applyDecorator>
            </ww:else>
</body>
</html>
