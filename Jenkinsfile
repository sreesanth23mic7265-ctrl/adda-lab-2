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
                bat 'mvnw.cmd clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                bat '''
                    taskkill /F /IM java.exe 2>NUL || exit /B 0

                    copy /Y target\\*.jar app.jar

                    start /B java -jar app.jar > app.log 2>&1
                '''
            }
        }
    }
}