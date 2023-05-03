pipeline {
    agent any
    tools {
        maven 'M3'
        jdk 'jdk11'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], userRemoteConfigs: [[url: 'https://gitlab.colvir.ru/trackstudio/autotests']]])
            }
        }
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
                allure includeProperties: true, jdk: '11', properties: [[key: 'environment', value: 'dev']]
            }
        }
    }
}
