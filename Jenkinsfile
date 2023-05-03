pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install -Dmaven.test.failure.ignore=true'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        stage('Publish Results') {
          steps {
            sh 'mvn allure:report'
            allure([
                includeProperties: true,
                jdk: '11',
                properties: [[key: 'environment', value: 'dev']],
                results: [[path: 'target/allure-results']]
            ])
          }
        }
    }
}
