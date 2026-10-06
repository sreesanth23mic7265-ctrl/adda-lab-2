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
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    pkill -f "java -jar app.jar" || true

                    cp target/*.jar app.jar

                    nohup java -jar app.jar > app.log 2>&1 &
                '''
            }
        }
    }
}