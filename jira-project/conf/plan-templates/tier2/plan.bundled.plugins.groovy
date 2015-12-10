// Depends on:
//     tier2/shortcuts.bundled.plugins.groovy
//     tier2/shortcuts.tier2.groovy
//     shortcuts.common.groovy

plan(key: 'BP', name: 'CI Test Bundled Plugins', description: 'https://extranet.atlassian.com/display/JIRADEV/Testing+Bundled+Plugins+Against+Master') {
    def jdk = 'JDK 1.6_patched'
    def jiraRepo = 'JIRA 6.3.x branch'
    def accTestRunnerRepo = 'acceptance-tests-runner (stable)'

    tier2Project()
    tier2Labels()

    onceADay(time: '01:15')
    repository(name: jiraRepo)
    repository(name: accTestRunnerRepo)
    branches(pattern: 'release-.*', removeAfterDays: '0')
    notifications()
    miscellaneousSettings()

    stage(name: 'Prepare', manual: 'false') {
        prepareBundledPlugins(jiraRepoName: jiraRepo, accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
    }

    stage(name: 'Test plugins', manual: 'false') {
        activeObjsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        adminQuickSearchPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        atlassianBotKiller(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        atlassianGadgets(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraAuditingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraBambooPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraChartingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraDvcsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraForSoftwareTeamsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraHealthCheckPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraHtml5AttachImagesPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraICalendarPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraImportersPluginJobs(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraInviteUserPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraIssueCollectorPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraMailPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraProjectConfigPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraProjectCreationPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraProjectCreationWithCTKPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraWelcomePlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        jiraWorkflowSharingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        myWorkPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        rpcPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        devStatusPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        triggersPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
        remoteEventsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk)
    }

    stage(name: 'Cleanup') {
        purgeMavenSandboxJob()
    }
}
