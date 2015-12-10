<%@ taglib uri="webwork" prefix="ww" %>
<%@ taglib uri="webwork" prefix="ui" %>
<%@ taglib uri="webwork" prefix="aui" %>
<%@ taglib uri="sitemesh-page" prefix="page" %>
<html>
<head>
    <ww:if test="remoteUser != null">
        <ww:if test="/showProjects == 'true'">
            <ww:if test="/showFilters == 'false'">
                <title><ww:text name="'popups.projectpicker.title'"/></title>
            </ww:if>
            <ww:else>
                <title><ww:text name="'popups.filterprojectpicker.title'"/></title>
            </ww:else>
        </ww:if>
        <ww:else>
            <title><ww:text name="'popups.filterpicker.title'"/></title>
        </ww:else>
        <script type="text/javascript">

            function submitFilter(id, element) {
                if (window.opener && window.opener.document) {
                    var wod = window.opener.document;
                    <%-- go get the field name so we know who we have picked for --%>
                    var ff = document.getElementById("picker_field").innerHTML;
                    AJS.$("#filter_" + ff + "_name", wod).text(AJS.$(element).text()).addClass("success");
                    if (wod.getElementById("filter_" + ff + "_button")){
                        wod.getElementById("filter_" + ff + "_button").innerHTML = "<ww:text name="'popups.filterpicker.changefilter'"/>";
                        wod.getElementById("filter_" + ff + "_id").setAttribute("value", id);
                    } else {
                        wod.getElementById("filter_" + ff + "_id").setAttribute("value", "filter-" + id);
                    }
                    window.close();
                }
            }

            /**
             * Submits the selected value back to the opener window.
             * @param id the numeric id of the project or filter
             * @param element the element in the page containing the name of the picked thing
             * @param type "project" or "filter" designating what we're picking.
             */
            function submitFilterOrProject(id, element, type) {
                if (window.opener && window.opener.document) {
                    if (type != "project") {
                        type = "filter"; // don't trust
                    }
                    var wod = window.opener.document;
                    <%-- go get the field name so we know who we have picked for --%>
                    var ff = document.getElementById("picker_field").innerHTML;
                    AJS.$("#filter_" + ff + "_name", wod).text(AJS.$(element).text()).addClass("success");
                    wod.getElementById("filter_" + ff + "_id").setAttribute("value", type + "-" + id);
                    if (wod.getElementById("filter_" + ff + "_button")){
                        wod.getElementById("filter_" + ff + "_button").innerHTML = "<ww:text name="'popups.filterpicker.changefilterproject'"/>";
                    }
                    window.close();
                }
            }

            /**
             * Submits the selected value back to the opener window.
             * @param id the numeric id of the project
             * @param element the element in the page containing the name of the picked thing
             */
            function submitProject(id, element) {
                if (window.opener && window.opener.document) {
                    var wod = window.opener.document;
                    <%-- go get the field name so we know who we have picked for --%>
                    var ff = document.getElementById("picker_field").innerHTML;
                    wod.getElementById("filter_" + ff + "_id").setAttribute("value", id);
                    wod.getElementById("filter_" + ff + "_name").innerHTML = element.innerHTML;
                    if (wod.getElementById("filter_" + ff + "_button")){
                        wod.getElementById("filter_" + ff + "_button").innerHTML = "<ww:text name="'popups.filterpicker.changeproject'"/>";
                    }
                    window.close();
                }
            }

        </script>
    </ww:if>
    <ww:else>
        <title><ww:text name="'common.words.error'"/></title>
        <meta name="decorator" content="message" />
    </ww:else>
</head>
<body>
<ww:if test="remoteUser != null">
    <ui:soy moduleKey="'jira.webresources:soy-templates'" template="'JIRA.Templates.Headers.pageHeader'">
        <ui:param name="'mainContent'">
            <h1>
                <ww:if test="/showProjects == 'true'">
                    <ww:if test="/showFilters == 'false'">
                        <ww:text name="'popups.projectpicker.title'"/>
                    </ww:if>
                    <ww:else>
                        <ww:text name="'popups.filterprojectpicker.title'"/>
                    </ww:else>
                </ww:if>
                <ww:else>
                    <ww:text name="'popups.filterpicker.title'"/>
                </ww:else>
            </h1>
        </ui:param>
    </ui:soy>

    <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanel'">
        <ui:param name="'content'">
            <ui:soy moduleKey="'com.atlassian.auiplugin:aui-experimental-soy-templates'" template="'aui.page.pagePanelContent'">
                <ui:param name="'content'">

            <ww:if test="/showProjects == 'true'">
                <ww:if test="/showFilters == 'false'">
                    <p><ww:text name="'popups.projectpicker.description'"/></p>
                </ww:if>
                <ww:else>
                    <p><ww:text name="'popups.filterprojectpicker.description'"/></p>
                </ww:else>
            </ww:if>
            <ww:else>
                <p><ww:text name="'popups.filterpicker.description'"/></p>
            </ww:else>

            <jsp:include page="filterpicker-tabs.jsp" />
            <ww:if test="filterView == 'search' && tabShowing('search') == true">
                <form action="FilterPickerPopup.jspa" method="get" class="aui" id="filterSearchForm">
                    <input type="hidden" name="filterView" value="<ww:property value="/filterView"/>"/>
                    <input type="hidden" name="field" value="<ww:property value="/field"/>"/>
                    <input type="hidden" name="showProjects" value="<ww:property value="/showProjects"/>"/>
                    <table class="filterSearchInput" cellpadding="0" cellspacing="0">
                        <%-- component includes its own row --%>
                        <ui:textfield label="text('common.concepts.search')" name="'searchName'" theme="'standard'">
                            <ui:param name="'formname'" value="'filterSearchForm'"/>
                            <ui:param name="'mandatory'" value="false"/>
                            <ui:param name="'size'" value="50"/>
                            <ui:param name="'maxlength'" value="50"/>
                            <ui:param name="'description'" value="text('filters.search.text.desc')"/>
                        </ui:textfield>
                        <tr>
                            <td class="fieldLabelArea"><ww:text name="'admin.common.words.owner'"/></td>
                            <%--Already has a TD--%>
                            <ui:component label="text('admin.common.words.owner')" name="'searchOwnerUserName'" template="userselect.jsp" theme="'single'">
                                <ui:param name="'formname'" value="'filterSearchForm'"/>
                                <ui:param name="'mandatory'" value="false"/>
                            </ui:component>
                        </tr>
                        <%-- component includes its own row --%>
                        <ww:component name="'shares'" label="text('common.concepts.shared.with')" template="select-share-types.jsp" >
                            <ww:param name="'class'" value="'filterSearchInputRightAligned fieldLabelArea'"/>
                            <ww:param name="'noJavaScriptMessage'">
                                <ww:text name="'common.sharing.no.share.javascript'"/>
                            </ww:param>
                            <ww:param name="'shareTypeList'" value="/filtersViewHelper/shareTypeRendererBeans"/>
                            <ww:param name="'dataString'" value="/filtersViewHelper/searchShareTypeJSON"/>
                            <ww:param name="'valueColSpan'" value="2"/>
                            <ww:param name="'anyDescription'"><ww:text name="'common.sharing.search.template.any.desc.SearchRequest'"/></ww:param>
                        </ww:component>
                        <tr class="buttons">
                            <td colspan="2">
                                <input name="Search" type="submit" value="<ww:text name="'common.concepts.search'"/>"/>
                                <a href="#" onclick="window.close();"><ww:text name="'common.words.cancel'"/></a>
                            </td>
                        </tr>
                    </table>
                </form>
                <ww:if test="/searchRequested == true && /filters/empty == false">
                    <ww:component name="text('common.concepts.search')" template="filter-list.jsp">
                        <ww:param name="'id'" value="'mf_browse'"/>
                        <ww:param name="'filterList'" value="/filters"/>
                        <ww:param name="'operations'">false</ww:param>
                        <ww:param name="'shares'" value="true"/>
                        <ww:param name="'issuecount'" value="false"/>
                        <ww:param name="'linkissuecount'" value="false"/>

                        <ww:param name="'favourite'" value="false"/>
                        <ww:param name="'subscriptions'" value="false"/>

                        <ww:param name="'sort'" value="true"/>
                        <ww:param name="'sortColumn'" value="/sortColumn"/>

                        <ww:param name="'paging'" value="true"/>
                        <ww:param name="'pagingMessage'">
                            <ww:text name="'common.sharing.searching.results.message'">
                                <ww:param name="'value0'"><ww:property value="/startPosition"/></ww:param>
                                <ww:param name="'value1'"><ww:property value="/endPosition"/></ww:param>
                                <ww:param name="'value2'"><ww:property value="/totalResultCount"/></ww:param>
                            </ww:text>
                        </ww:param>
                        <ww:param name="'pagingPrevUrl'" value="/previousUrl"/>
                        <ww:param name="'pagingNextUrl'" value="/nextUrl"/>
                        <ww:param name="'emptyMessage'"><ww:text name="/searchEmptyMessageKey"/></ww:param>

                        <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
                        <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
                    </ww:component>
                </ww:if>
                <ww:else>
                    <ww:if test="/searchRequested == true">
                        <aui:component template="auimessage.jsp" theme="'aui'">
                            <aui:param name="'messageType'">info</aui:param>
                            <aui:param name="'messageHtml'">
                                <p><ww:text name="/searchEmptyMessageKey"/></p>
                            </aui:param>
                        </aui:component>
                    </ww:if>
                </ww:else>
            </ww:if>
            <%--//--%>
            <%--// POPULAR RESULTS SECTION HERE--%>
            <%--//--%>
            <ww:elseIf test="filterView == 'popular' && tabShowing('popular') == true">
                <ww:component name="text('common.concepts.popular')" template="filter-list.jsp">
                    <ww:param name="'id'" value="'mf_popular'"/>
                    <ww:param name="'filterList'" value="/filters"/>
                    <ww:param name="'operations'">false</ww:param>
                    <ww:param name="'shares'" value="true"/>
                    <ww:param name="'favourite'" value="false"/>
                    <ww:param name="'issuecount'" value="false"/>
                    <ww:param name="'linkissuecount'" value="false"/>

                    <ww:param name="'subscriptions'" value="false"/>

                    <ww:param name="'sort'" value="false"/>
                    <ww:param name="'sortColumn'" value="/sortColumn"/>

                    <ww:param name="'emptyMessage'"><ww:text name="'filters.no.search.results'"/></ww:param>
                    <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
                    <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
                </ww:component>
                <div class="filter-picker-cancel"><button onclick="window.close();"><ww:text name="'common.words.cancel'"/></button></div>
            </ww:elseIf>
            <%--// --%>
            <%--// FAVOURITE RESULTS SECTION HERE--%>
            <%--//--%>
            <ww:elseIf test="filterView == 'favourites' && tabShowing('favourites') == true">
                <ww:component name="text('common.favourites.favourite')" template="filter-list.jsp">
                    <ww:param name="'id'" value="'mf_favourites'"/>
                    <ww:param name="'filterList'" value="/filters"/>
                    <ww:param name="'favcount'">false</ww:param>
                    <ww:param name="'remove'">true</ww:param>
                    <ww:param name="'shares'" value="false"/>
                    <ww:param name="'favourite'" value="false"/>
                    <ww:param name="'issuecount'" value="false"/>
                    <ww:param name="'linkissuecount'" value="false"/>
                    <ww:param name="'subscriptions'" value="false"/>
                    <ww:param name="'operations'">false</ww:param>
                    <ww:param name="'emptyMessage'"><ww:text name="'filters.no.favourite'"/></ww:param>
                    <ww:param name="'returnUrl'" value="/returnUrl"/>
                    <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
                    <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
                </ww:component>
                <div class="filter-picker-cancel"><button onclick="window.close();"><ww:text name="'common.words.cancel'"/></button></div>
            </ww:elseIf>
            <%--// --%>
            <%--// PROJECTS SECTION HERE--%>
            <%--//--%>
            <ww:elseIf test="filterView == 'projects' && tabShowing('projects') == true">
                <ww:component name="text('common.concepts.projects')" template="projectcategory-list.jsp">
                    <ww:param name="'projectFetcher'" value="/"/>
                    <ww:param name="'showCategories'" value="/showCategories"/>
                    <ww:param name="'categories'" value="/categories"/>
                    <ww:param name="'id'" value="'filterpicker_projects'"/>
                    <ww:param name="'projectList'" value="/projects"/>
                    <ww:param name="'viewHelper'" value="/filtersViewHelper"/>
                    <ww:param name="'linkRenderer'" value="/filterLinkRenderer"/>
                </ww:component>
                <div class="filter-picker-cancel"><button onclick="window.close();"><ww:text name="'common.words.cancel'"/></button></div>
            </ww:elseIf>
            <ww:else>
                <aui:component template="auimessage.jsp" theme="'aui'">
                    <aui:param name="'messageType'">warning</aui:param>
                    <aui:param name="'messageHtml'">
                        <p><ww:text name="'filters.no.tab.permssion'"/><ww:property value="filterView"/>:<ww:property value="showProjects"/></p>
                    </aui:param>
                </aui:component>
                <div class="filter-picker-cancel"><button onclick="window.close();"><ww:text name="'common.words.cancel'"/></button></div>
            </ww:else>
            <%--
            Warning XSS Magic

            This invisible tag exists in the page to hold possibly javascript-hostile values for fields which have originally
            come from the url. If an evil hacker passed some concat-escaping script fragment as the field parameter and
            submitFilter() used this value to concat to produce the id of the field in the opener window, they would be able
            to execute arbitrary javascript in the client.
            --%>
            <span style="display:none;" id="picker_field"><ww:property value="/field"/></span>

                </ui:param>
            </ui:soy>
        </ui:param>
    </ui:soy>
</ww:if>
<ww:else>
    <div class="form-body">
        <header>
            <h1><ww:text name="'common.words.error'"/></h1>
        </header>
        <aui:component template="auimessage.jsp" theme="'aui'">
            <aui:param name="'messageType'">warning</aui:param>
            <aui:param name="'messageHtml'">
                <p><ww:text name="'popups.notloggedin'"/></p>
            </aui:param>
        </aui:component>
        <div class="filter-picker-cancel"><button onclick="window.close();"><ww:text name="'common.words.cancel'"/></button></div>
    </div>
</ww:else>
</body>
</html>
