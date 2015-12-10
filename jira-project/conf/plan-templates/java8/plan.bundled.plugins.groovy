plan(key: 'BPJ8', name: 'CI Test Bundled Plugins - Java 8', description: 'https://extranet.atlassian.com/display/JIRADEV/Testing+Bundled+Plugins+Against+Master') {
    def jdk7 = 'JDK 1.7'
    def jdk8 = 'JDK 1.8'
    def jiraRepo = 'JIRA 6.3.x branch'
    def accTestRunnerRepo = 'acceptance-tests-runner (stable)'

    java8Project()
    java8Labels()

    noTrigger(jiraRepoName: jiraRepo, accTestRunnerRepoName: accTestRunnerRepo)
    branches(pattern: 'issue/JDEV-28412-plan-templates', removeAfterDays: '0')
    notifications()
    miscellaneousSettings()

    stage(name: 'Prepare', manual: 'false') {
        prepareBundledPlugins(jiraRepoName: jiraRepo, accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk7)
    }

    stage(name: 'Test plugins', manual: 'false') {
        activeObjsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        adminQuickSearchPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        atlassianBotKiller(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        atlassianGadgets(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraAuditingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraBambooPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraChartingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraDvcsPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraHealthCheckPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraHtml5AttachImagesPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraICalendarPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraImportersPluginJobs(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraInviteUserPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraIssueCollectorPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraMailPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraProjectConfigPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraProjectCreationPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraProjectCreationWithCTKPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraWelcomePlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        jiraWorkflowSharingPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        myWorkPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
        rpcPlugin(accTestRunnerRepoName: accTestRunnerRepo, jdk: jdk8)
    }

    stage(name: 'Cleanup') {
        purgeMavenSandboxJob()
    }
}
