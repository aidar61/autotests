pipeline {
        agent any
        environment {
            MVN_HOME = tool 'Maven'
        }
        stages {
            stage('Build') {
                steps {
                    sh "mvn clean package"
                }
            }
            stage('Test') {
                steps {
                    sh "mvn test"
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