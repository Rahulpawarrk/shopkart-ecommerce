pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    environment {
        APP_NAME = 'shopkart-ecommerce'
        IMAGE_NAME = "rahulpawarrk/${APP_NAME}"
        REGISTRY_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('1. Checkout Source') {
            steps {
                echo '📥 Checking out repository branch from Git...'
                checkout scm
            }
        }

        stage('2. Verify Environment') {
            steps {
                echo '🔍 Checking Java, Maven, and runtime environment...'
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        stage('3. Automated Unit & Integration Tests') {
            steps {
                echo '🧪 Executing complete test suite (84 JUnit tests)...'
                sh 'mvn clean test'
            }
            post {
                always {
                    // Archive JUnit test report xmls in Jenkins UI
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('4. Compile & Package Production WAR') {
            steps {
                echo '📦 Packaging production WAR artifact (ecommerce-web.war)...'
                sh 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true, allowEmptyArchive: true
            }
        }

        stage('5. Build Docker Image') {
            steps {
                echo "🐳 Building Docker image [${IMAGE_NAME}:${REGISTRY_TAG}]..."
                sh "docker build -t ${IMAGE_NAME}:${REGISTRY_TAG} -t ${IMAGE_NAME}:latest ."
            }
        }

        stage('6. Deploy / Trigger Webhook') {
            steps {
                script {
                    echo '🚀 Triggering Continuous Deployment pipeline...'
                    // If Render or Cloud deploy webhook URL is set in Jenkins Credentials:
                    if (env.RENDER_DEPLOY_HOOK_URL) {
                        echo 'Triggering Render cloud service rebuild via deploy hook...'
                        sh 'curl -s -X POST "${RENDER_DEPLOY_HOOK_URL}"'
                    } else {
                        echo 'No RENDER_DEPLOY_HOOK_URL configured; skipping webhook trigger.'
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ BUILD #${env.BUILD_NUMBER} SUCCESS: All tests passed & Docker image built successfully."
        }
        failure {
            echo "❌ BUILD #${env.BUILD_NUMBER} FAILED: Check test execution logs or compile errors."
        }
        cleanup {
            echo '🧹 Cleaning up Jenkins workspace...'
            cleanWs deleteDirs: true, notFailBuild: true
        }
    }
}
