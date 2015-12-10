// Depends on:
//     tier3/shortcuts.source.dist.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'CSD', name: 'CI Source Distribution', description: '') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    notifications()

    stage(name: 'Default Stage', manual: 'false') {
        sourceDistribution(repoName: repoName, jdk: jdk)
    }
}
