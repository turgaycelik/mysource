// Depends on:
//     tier1/shortcuts.api.checker.groovy
//     tier1/shortcuts.tier1.groovy
//     shortcuts.common.groovy

plan(key: 'API', name: 'CI API Checker', description: 'Runs the API checker') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier1Project()
    tier1Labels()

    repositoryPolling(repoName: repoName)
    branches(pattern: 'stable-issue/.*', removeAfterDays: '10')
    notifications()

    stage(name: 'Default stage', description: '', manual: 'false') {
        apiCheck(repoName: repoName, jdk: jdk)
    }
}
