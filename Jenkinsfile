pipeline {

    agent any

    tools {
        maven 'Maven_3.9'   // Must match name in Jenkins → Global Tool Configuration
        jdk   'JDK_25'      // Must match name in Jenkins → Global Tool Configuration
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    stages {

        // ── 1. Checkout ────────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                echo "=== Cloning repository from GitHub ==="
                checkout scm
                echo "Branch: ${env.GIT_BRANCH}  |  Commit: ${env.GIT_COMMIT}"
            }
        }

        // ── 2. Set headless=true for CI (no credentials needed) ───────────────
        stage('Configure') {
            steps {
                echo "=== Setting headless=true for CI run ==="
                script {
                    def configFile = 'src/test/resources/config.properties'
                    def content = readFile(configFile)
                    content = content
                        .replaceAll(/headless=.*/, 'headless=true')
                        .replaceAll(/browser=.*/, 'browser=chrome')
                    writeFile file: configFile, text: content
                    echo "config.properties updated — headless=true applied."
                }
            }
        }

        // ── 3. Compile ────────────────────────────────────────────────────────
        stage('Build') {
            steps {
                echo "=== Compiling project ==="
                bat 'mvn clean compile test-compile -q'
            }
        }

        // ── 4. Run Full Test Suite ─────────────────────────────────────────────
        stage('Test') {
            steps {
                echo "=== Running all 21 tests ==="
                bat 'mvn test -Dsurefire.suiteXmlFiles=testng-all.xml -q'
            }
            post {
                always {
                    echo "=== Collecting TestNG results ==="
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ── 5. Allure Report ───────────────────────────────────────────────────
        stage('Allure Report') {
            steps {
                echo "=== Generating Allure Report ==="
                allure([
                    includeProperties: false,
                    jdk              : '',
                    properties       : [],
                    reportBuildPolicy: 'ALWAYS',
                    results          : [[path: 'allure-results']]
                ])
            }
        }
    }

    // ── Post-build actions ─────────────────────────────────────────────────────
    post {
        success {
            echo """
╔══════════════════════════════════════════════════╗
║  BUILD SUCCESS - All 21 Tests Passed!            ║
╚══════════════════════════════════════════════════╝
            """
        }
        failure {
            echo """
╔══════════════════════════════════════════════════╗
║  BUILD FAILED - Check console output above       ║
╚══════════════════════════════════════════════════╝
            """
        }
        always {
            echo "=== Archiving Allure results ==="
            archiveArtifacts artifacts: 'allure-results/**', allowEmptyArchive: true
            cleanWs()
        }
    }
}
