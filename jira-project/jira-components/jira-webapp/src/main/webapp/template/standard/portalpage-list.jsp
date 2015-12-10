<%@ page import="com.atlassian.jira.ComponentManager" %>
<%@ page import="com.atlassian.plugin.webresource.WebResourceManager" %>
<%@ taglib uri="webwork" prefix="ww" %>
<%
    WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
    webResourceManager.requireResource("jira.webresources:share-types");
%>
<!-- PAGING SUPPORT-->
<jsp:include page="/template/standard/portalpage-list-paging.jsp" />
<table id="<ww:property value="parameters['id']"/>" class="aui dashboardList">
    <jsp:include page="/template/standard/portalpage-list-header.jsp" />
    <tbody>
    <ww:property value="parameters['portalPageList']">
        <ww:if test=". != null && ./size > 0">
            <ww:iterator value="." status="'status'">
            <tr id="pp_<ww:property value="./id" />" <ww:if test="@status/first == true">class="first-row"</ww:if><ww:if test="@status/last == true">class="last-row"</ww:if>>
                <td class="cell-type-key">
                    <jsp:include page="/template/standard/portalpage-list-name.jsp" />
                </td>
                <ww:if test="parameters['owner'] != 'false'">
                    <td class="cell-type-user">
                        <span data-field="owner">
                            <ww:if test="./systemDashboard == true">
                                <ww:text name="'portalpage.owner.system'"/>
                            </ww:if>
                            <ww:else>
                                <ww:property value="./ownerFullName" escape="false"/> (<ww:property value="./ownerUserName"/>)
                            </ww:else>
                        </span>
                    </td>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <ww:if test="parameters['adminView'] == 'true'">
                        <td>
                            <ww:component template="all-shares-list.jsp">
                                <ww:param name="'sharesview'" value="."/>
                                <ww:param name="'privatemessage'"><ww:text name="'portalpage.private.page'"/></ww:param>
                                <ww:param name="'sharedmessage'"><ww:text name="'portalpage.shared.page'"/></ww:param>
                            </ww:component>
                        </td>
                    </ww:if>
                    <ww:else>
                        <td>
                            <ww:component template="shares-list.jsp">
                                <ww:param name="'sharesview'" value="."/>
                                <ww:param name="'privatemessage'"><ww:text name="'portalpage.private.page'"/></ww:param>
                                <ww:param name="'sharedmessage'"><ww:text name="'portalpage.shared.page'"/></ww:param>
                            </ww:component>
                        </td>
                    </ww:else>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <ww:if test="parameters['usesimplefavcount'] != 'true'">
                        <ww:if test="./favourite == 'true'">
                            <td>
                                <div id="fav_count_enabled_<ww:property value="parameters['id']"/>_PortalPage_<ww:property value="./id"/>">
                                    <ww:property value="./favouriteCount"/>
                                </div>
                                <div id="fav_count_disabled_<ww:property value="parameters['id']"/>_PortalPage_<ww:property value="./id"/>" style="display:none">
                                    <ww:property value="./alternateFavouriteCount"/>
                                </div>
                            </td>
                        </ww:if>
                        <ww:else>
                            <td>
                                <div id="fav_count_disabled_<ww:property value="parameters['id']"/>_PortalPage_<ww:property value="./id"/>">
                                    <ww:property value="./favouriteCount"/>
                                </div>
                                <div id="fav_count_enabled_<ww:property value="parameters['id']"/>_PortalPage_<ww:property value="./id"/>" style="display:none">
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
                <ww:if test="parameters['ordering'] != 'false'">
                    <td class="cell-type-collapsed">
                        <a class="portal_pos_first" id="pos_first_<ww:property value="@status/index" />" href="<ww:url page="ConfigurePortalPages!moveToStart.jspa"><ww:param name="'pageId'" value="./id"/></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_first.gif"  class="sortArrow16x16" title="<ww:text name="'configureportalpages.movepage.first'"/>" alt="<ww:text name="'configureportalpages.movepage.first'"/>"></a>
                        <a class="portal_pos_up" id="pos_up_<ww:property value="@status/index" />" href="<ww:url page="ConfigurePortalPages!moveUp.jspa"><ww:param name="'pageId'" value="./id"/></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_up_blue.gif" class="sortArrow16x16" title="<ww:text name="'configureportalpages.movepage.up'"/>" alt="<ww:text name="'configureportalpages.movepage.up'"/>"></a>
                        <a class="portal_pos_down" id="pos_down_<ww:property value="@status/index" />" href="<ww:url page="ConfigurePortalPages!moveDown.jspa"><ww:param name="'pageId'" value="./id"/></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_down_blue.gif" class="sortArrow16x16" title="<ww:text name="'configureportalpages.movepage.down'"/>" alt="<ww:text name="'configureportalpages.movepage.down'"/>"></a>
                        <a class="portal_pos_last" id="pos_last_<ww:property value="@status/index" />" href="<ww:url page="ConfigurePortalPages!moveToEnd.jspa"><ww:param name="'pageId'" value="./id"/></ww:url>"><img src="<%= request.getContextPath() %>/images/icons/arrow_last.gif" class="sortArrow16x16" title="<ww:text name="'configureportalpages.movepage.last'"/>" alt="<ww:text name="'configureportalpages.movepage.last'"/>"></a>
                    </td>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <td class="cell-type-actions">
                        <ww:component template="dropdown/cog.jsp" theme="'aui'">
                            <ww:param name="'model'" value="parameters['dropDownModelProvider']/dropDownModel(., @status/index)"/>
                            <ww:if test="parameters['includeOperationsId'] != 'false'">
                                <ww:param name="'id'" value="./id + '_operations'"/>
                            </ww:if>
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
    <tbody>
</table>
<!-- PAGING SUPPORT-->
<jsp:include page="/template/standard/portalpage-list-paging.jsp" />
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