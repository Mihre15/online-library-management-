pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B clean compile'
            }
        }

        stage('Test') {
            steps {
                // Runs the JUnit suite (service unit tests + Spring Boot integration
                // tests) with JaCoCo coverage. Selenium/browser tests are tagged
                // "selenium" and excluded here via pom.xml's Surefire config, since
                // they need a running frontend + real browser, not a Jenkins agent.
                sh './mvnw -B test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo 'Build and tests passed. JaCoCo report is attached to this build under "Build Artifacts".'
        }
        failure {
            echo 'Build or tests failed — check the console output and the JUnit results above.'
        }
    }
}
