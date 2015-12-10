<%@ taglib uri="webwork" prefix="ww" %>
<div class="tabwrap tabs2">
<ul id="filter_type_table" class="tabs horizontal">
    <ww:if test="tabShowing('favourites') == true">
        <ww:if test="filterView == 'favourites'">
            <li class="active">
                <strong><ww:text name="'common.favourites.favourites'"/></strong>
            </li>
        </ww:if>
        <ww:else>
            <li><a href="<%= request.getContextPath() %>/secure/FilterPickerPopup.jspa?filterView=favourites&field=<ww:property value="/field"/>&showProjects=<ww:property value="/showProjects"/>"><strong><ww:text name="'common.favourites.favourites'"/></strong></a></li>
        </ww:else>
    </ww:if>
    <!--// Popular and Search is not available on Standard JIRA and only if some one is logged in -->
    <ww:if test="tabShowing('popular') == true">
        <ww:if test="filterView == 'popular'">
            <li class="active">
                <strong><ww:text name="'common.concepts.popular'"/></strong>
            </li>
        </ww:if>
        <ww:else>
            <li><a href="<%= request.getContextPath() %>/secure/FilterPickerPopup.jspa?filterView=popular&field=<ww:property value="/field"/>&showProjects=<ww:property value="/showProjects"/>"><strong><ww:text name="'common.concepts.popular'"/></strong></a></li>
        </ww:else>
    </ww:if>
    <ww:if test="tabShowing('search') == true">
        <ww:if test="filterView == 'search'">
            <li class="active">
                <strong><ww:text name="'common.concepts.search'"/></strong>
            </li>
        </ww:if>
        <ww:else>
            <li><a href="<%= request.getContextPath() %>/secure/FilterPickerPopup.jspa?filterView=search&field=<ww:property value="/field"/>&showProjects=<ww:property value="/showProjects"/>"><strong><ww:text name="'common.concepts.search'"/></strong></a></li>
        </ww:else>
    </ww:if>
    <ww:if test="tabShowing('projects') == true">
        <ww:if test="/filterView == 'projects'">
            <li class="active">
                <strong><ww:text name="'common.concepts.projects'"/></strong>
            </li>
        </ww:if>
        <ww:else>
            <li><a href="<%= request.getContextPath() %>/secure/FilterPickerPopup.jspa?showProjects=true&filterView=projects&field=<ww:property value="/field"/>"><strong><ww:text name="'common.concepts.projects'"/></strong></a></li>
        </ww:else>
    </ww:if>
    </ul>
</div>
