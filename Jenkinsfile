pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    parameters {
    choice(
            name:"Stand",
            choices: ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11'],
            description: 'Number of the Stand'
    )
    }
    stages {
        stage('Build') {
            steps {
                sh "mvn properties:set -Dproperty=ts.stand -Dnewvalue=${params.Stand} -Dfile=app.properties"
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
               always {
                        allure([
                            includeProperties: false,
                            jdk: '11.0.18',
                            properties: [],
                            reportBuildPolicy: 'ALWAYS',
                            results: [[path: 'target/allure-results']]
                      ])
               }
           }
        }
    }
}
