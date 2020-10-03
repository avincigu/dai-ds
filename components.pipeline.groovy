// Copyright (C) 2019-2020 Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0
//
pipeline {
    agent none
    parameters {
        booleanParam(name: 'QUICK_BUILD', defaultValue: true,
                description: 'Speeds up build by skipping gradle clean')
        choice(name: 'AGENT', choices: [
                'NRE-COMPONENT',
                'loki-n3-test',
                'Sindhu-test'
        ], description: 'Agent label')
    }
    stages {
        stage ('component-tests') {
            agent { label "${AGENT}" }
            environment { PATH = "${PATH}:/home/${USER}/voltdb9.1/bin" }
            stages {
                stage('Preparation') {
                    steps {
                        echo "Building on ${AGENT}"
                        sh 'hostname'
                        lastChanges format: 'LINE', matchWordsThreshold: '0.25', matching: 'NONE',
                                matchingMaxComparisons: '1000', showFiles: true, since: 'PREVIOUS_REVISION',
                                specificBuild: '', specificRevision: '', synchronisedScroll: true, vcsDir: ''

                        script {
                            utilities.CopyIntegrationTestScriptsToBuildDistributions()
                            utilities.FixFilesPermission()
                            utilities.CleanUpMachine('build/distributions')
                        }
                    }
                }
                stage('Clean') {
                    when { expression { "${params.QUICK_BUILD}" == 'false' } }
                    steps {
                        sh 'rm -rf build'
                        script{ utilities.InvokeGradle("clean") }
                    }
                }
                stage('Component Tests') {
                    options{ catchError(message: "Component Tests failed", stageResult: 'UNSTABLE', buildResult: 'UNSTABLE') }
                    steps {
                        RunIntegrationTests()
                    }
                }
                stage('Reports') {
                    options{ catchError(message: "Reports failed", stageResult: 'UNSTABLE', buildResult: 'UNSTABLE') }
                    steps {
                        jacoco classPattern: '**/classes/java/main/com/intel/'
                        junit '**/test-results/**/*.xml'
                    }
                }
                stage('Archive') {
                    steps {
                        sh 'rm -f *.zip'
                        zip archive: true, dir: '', glob: '**/build/jacoco/integrationTest.exec', zipFile: 'component-test-coverage.zip'
                        zip archive: true, dir: '', glob: '**/test-results/test/*.xml', zipFile: 'component-test-results.zip'
                        archiveArtifacts allowEmptyArchive: true, 'build/reports/**'
                    }
                }
            }
        }
    }
}

def RunIntegrationTests() {
    utilities.InvokeGradle("jar")
    StartHWInvDb()
    utilities.InvokeGradle("integrationTest")
    sh 'touch ./dai_core/build/test-results/integrationTest/*.xml'  // in case no new tests ran; make junit step happy
    StopHWInvDb()
}

// This is one way to setup for component level testing.  You can also use docker-compose or partially
// starts DAI.
// Currently, our docker image has some dependencies that are fulfilled after DAI is installed.  So,
// we cannot use this easily for component tests.
def StartHWInvDb() {
    def scriptDir = './inventory/src/integration/resources/scripts'
    sh "${scriptDir}/init-voltdb.sh"
    sh "${scriptDir}/start-voltdb.sh"
    sh "${scriptDir}/wait-for-voltdb.sh 21211"
    sh "sqlcmd < ${scriptDir}/load_procedures.sql"
    sh "sqlcmd < data/db/DAI-Volt-Tables.sql"
    sh "sqlcmd < data/db/DAI-Volt-Procedures.sql"
}

// Shutdown method depends on how voltdb was procured.  For example,
// if docker-compose was used to procure the voltdb, the relevant
// containers need to be shutdown.
def StopHWInvDb() {
    sh 'voltadmin shutdown --force || true'
}
