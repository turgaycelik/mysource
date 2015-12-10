<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib prefix="jira" uri="jiratags" %>

<jira:web-resource-require modules="jira.webresources:share-types"/>

<!-- PAGING SUPPORT-->
<jsp:include page="/template/standard/filter-list-paging.jsp" />

<table id="<ww:property value="parameters['id']"/>" cellspacing="0" cellpadding="0" class="aui">
    <jsp:include page="/template/standard/filter-list-header.jsp" />
    <tbody>

    <ww:property value="parameters['filterList']">
        <ww:if test=". != null && ./size > 0">
            <ww:iterator value="." status="'myStatus'">
                <tr id="mf_<ww:property value="./id" />" data-filter-id="<ww:property value="./id" />">
                    <td>
                        <jsp:include page="/template/standard/filter-list-name.jsp" />
                    </td>
                <ww:if test="parameters['issuecount'] != 'false'">
                    <td>
                        <ww:if test="./issueCount == -1">
                            <ww:text name="'managefilters.error.too.complex'"/>
                        </ww:if>
                        <ww:elseIf test="./issueCount == 0 || parameters['linkissuecount'] == 'false'">
                            <ww:property value="./issueCount" />
                        </ww:elseIf>
                        <ww:else>
                            <a href="<ww:url value="'IssueNavigator.jspa?mode=hide'"><ww:param name="'requestId'" value="./id" /></ww:url>"><ww:property value="./issueCount" /></a>
                        </ww:else>
                    </td>
                </ww:if>
                <ww:if test="parameters['owner'] != 'false'">
                    <td>
                        <span data-filter-field="owner-full-name"><ww:property value="./ownerFullName" escape="false"/></span> (<ww:property value="./ownerUserName"/>)
                    </td>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <ww:if test="parameters['adminView'] == 'true'">
                        <td>
                            <ww:component template="all-shares-list.jsp">
                                <ww:param name="'sharesview'" value="."/>
                                <ww:param name="'privatemessage'"><ww:text name="'common.sharing.shared.display.private'"/></ww:param>
                                <ww:param name="'sharedmessage'"><ww:text name="'common.filters.shared'"/></ww:param>
                            </ww:component>
                        </td>
                    </ww:if>
                    <ww:else>
                        <td>
                            <ww:component template="shares-list.jsp">
                                <ww:param name="'sharesview'" value="."/>
                                <ww:param name="'privatemessage'"><ww:text name="'common.sharing.shared.display.private'"/></ww:param>
                                <ww:param name="'sharedmessage'"><ww:text name="'common.filters.shared'"/></ww:param>
                            </ww:component>
                        </td>

                    </ww:else>
                </ww:if>
                <ww:if test="parameters['subscriptions'] != 'false'">
                    <td>
                        <ww:if test="./subscriptionCount == 0">
                            <ww:text name="'common.words.none'"/> - <a id="subscribe_<ww:property value="./name"/>" class="trigger-dialog" href="<ww:url value="'FilterSubscription!default.jspa'"><ww:param name="'filterId'" value="./id" /></ww:url>"><ww:text name="'managefilters.subscribe'"/></a>
                        </ww:if>
                        <ww:elseIf test="./subscriptionCount == 1">
                            <a href="<ww:url value="'ViewSubscriptions.jspa'"><ww:param name="'filterId'" value="./id" /></ww:url>"><ww:property value="./subscriptionCount"/> <ww:text name="'managefilters.subscription'"/></a>
                        </ww:elseIf>
                        <ww:else>
                            <a href="<ww:url value="'ViewSubscriptions.jspa'"><ww:param name="'filterId'" value="./id" /></ww:url>"><ww:property value="./subscriptionCount"/> <ww:text name="'managefilters.subscriptions'"/></a>
                        </ww:else>
                    </td>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <ww:if test="parameters['usesimplefavcount'] != 'true'">
                        <ww:if test="./favourite == 'true'">
                            <td>
                                <div id="fav_count_enabled_<ww:property value="parameters['id']"/>_SearchRequest_<ww:property value="./id"/>">
                                    <ww:property value="./favouriteCount"/>
                                </div>
                                <div id="fav_count_disabled_<ww:property value="parameters['id']"/>_SearchRequest_<ww:property value="./id"/>" style="display:none">
                                    <ww:property value="./alternateFavouriteCount"/>
                                </div>
                            </td>
                        </ww:if>
                        <ww:else>
                            <td>
                                <div id="fav_count_disabled_<ww:property value="parameters['id']"/>_SearchRequest_<ww:property value="./id"/>">
                                    <ww:property value="./favouriteCount"/>
                                </div>
                                <div id="fav_count_enabled_<ww:property value="parameters['id']"/>_SearchRequest_<ww:property value="./id"/>" style="display:none">
                                    <ww:property value="./alternateFavouriteCount"/>
                                </div>
                            </td>
                        </ww:else>
                    </ww:if>
                    <ww:else>
                        <td>
                            <ww:property value="./favouriteCount"/>
                        </td>
                    </ww:else>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <td>
                        <ww:component template="dropdown/cog.jsp" theme="'aui'">
                            <ww:param name="'model'" value="parameters['dropDownModelProvider']/dropDownModel(.,@myStatus/index)"/>
                            <ww:param name="'id'" value="./id + '_operations'"/>
                        </ww:component>
                    </td>
                </ww:if>
                </tr>
            </ww:iterator>
        </ww:if>
        <ww:else>
            <tr>
                <td><ww:property value="parameters['emptyMessage']"/></td>
            </tr>
        </ww:else>
    </ww:property>
    </tbody>
</table>
<!-- PAGING SUPPORT-->
<jsp:include page="/template/standard/filter-list-paging.jsp" />

<table id="<ww:property value="parameters['id']"/>_empty" class="aui" cellspacing="0" cellpadding="0" style="display:none">
    <thead>
        <tr class="filter_list">
            <ww:if test="parameters['name'] != false">
                <th><ww:text name="'common.words.name'"/></th>
            </ww:if>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><ww:property value="parameters['emptyMessage']"/></td>
        </tr>
    </tbody>
</table>
