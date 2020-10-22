pipeline {
    agent { label 'NRE-MASTER-CD' }

    stages {
        stage('Preparation') {
            steps {
                sh 'hostname'
                lastChanges since: 'PREVIOUS_REVISION', format: 'SIDE', matching: 'LINE'
            }
        }
        stage('Run Master CD') {
            steps {
                build job: 'master-continuous-delivery-pipeline',
                        parameters: [string(name: 'tag', value: '@smokeTest')],
                        quietPeriod: 0, wait: true
            }
        }
    }
}
