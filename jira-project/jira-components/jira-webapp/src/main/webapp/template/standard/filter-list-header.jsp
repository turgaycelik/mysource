<%@ taglib uri="webwork" prefix="ww" %>
<thead>
    <tr>
        <ww:if test="parameters['sort'] != null && parameters['sort'] == true">
            <ww:if test="parameters['name'] != false">
                <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('name')"/>" >
                    <a id="filter_sort_name" href="<ww:property value="parameters['viewHelper']/generateSortUrl('name')"/>"><ww:text name="'common.words.name'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('name')" escape="false"/></a></th>
            </ww:if>
            <ww:if test="parameters['filterList'] != null && parameters['filterList']/size > 0">
                <ww:if test="parameters['issuecount'] != 'false'">
                    <th title="<ww:text name="'filters.issues.desc'"/>"><ww:text name="'common.concepts.issues'"/></th>
                </ww:if>
                <ww:if test="parameters['owner'] != 'false'">
                    <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('owner')"/>">
                        <a id="filter_sort_owner" href="<ww:property value="parameters['viewHelper']/generateSortUrl('owner')"/>"><ww:text name="'admin.common.words.owner'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('owner')" escape="false"/></a></th>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <th><ww:text name="'common.concepts.shared.with'"/></th>
                </ww:if>
                <ww:if test="parameters['subscriptions'] != 'false'">
                    <th><ww:text name="'managefilters.subscriptions'"/></th>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <th class="<ww:property value="parameters['viewHelper']/generateSortCssClass('favcount')"/>" title="<ww:text name="'filters.favourite.count.desc'"/>">
                        <a id="filter_sort_popularity" href="<ww:property value="parameters['viewHelper']/generateSortUrl('favcount')"/>"><ww:text name="'common.concepts.popularity'"/>&nbsp;<ww:property value="parameters['viewHelper']/generateSortIcon('favcount')" escape="false"/></a>
                    </th>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <th></th>
                </ww:if>
            </ww:if>
        </ww:if>
        <ww:else>
            <ww:if test="parameters['name'] != false">
                <th><ww:text name="'common.words.name'"/></th>
            </ww:if>
            <ww:if test="parameters['filterList'] != null && parameters['filterList']/size > 0">
                <ww:if test="parameters['issuecount'] != 'false'">
                    <th title="<ww:text name="'filters.issues.desc'"/>"><ww:text name="'common.concepts.issues'"/></th>
                </ww:if>
                <ww:if test="parameters['owner'] != 'false'">
                    <th><ww:text name="'admin.common.words.owner'"/></th>
                </ww:if>
                <ww:if test="parameters['shares'] != 'false'">
                    <th><ww:text name="'common.concepts.shared.with'"/></th>
                </ww:if>
                <ww:if test="parameters['subscriptions'] != 'false'">
                    <th><ww:text name="'managefilters.subscriptions'"/></th>
                </ww:if>
                <ww:if test="parameters['favcount'] != 'false'">
                    <th title="<ww:text name="'filters.favourite.count.desc'"/>"><ww:text name="'common.concepts.popularity'"/></th>
                </ww:if>
                <ww:if test="parameters['operations'] != 'false'">
                    <th></th>
                </ww:if>
            </ww:if>
        </ww:else>
    </tr>
</thead>