// Depends on:
//     tier3/shortcuts.qa.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'QA', name: 'QA Standalone', description: '') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repositoryPolling(repoName: repoName)
    notifications()
    miscellaneousConfig()

    stage(name: 'Default Stage', manual: 'false') {
        qaStandalone(repoName: repoName, jdk: jdk)
    }
}
