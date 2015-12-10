// Depends on:
//     tier3/shortcuts.deploy.groovy
//     tier3/shortcuts.tier3.groovy
//     shortcuts.common.groovy

plan(key: 'DEPLOYJIRA', name: 'CI Deploy', description: '') {
    def jdk = 'JDK 1.6'
    def repoName = 'JIRA 6.3.x branch'

    tier3Project()
    tier3Labels()

    repository(name: repoName)
    notifications()
    globalVariables()

    stage(name: 'Deploy Maven Artifacts', manual: 'false') {
        deployMavenArtifacts(repoName: repoName, jdk: jdk)
    }
}
