
pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'kavivickydocker/aws-devops-app'
        DOCKER_TAG = "${BUILD_NUMBER}"
        APP_SERVER = '172.31.39.190'
        APP_CONTAINER = 'aws-devops-app'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building Spring Boot application'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Verify JAR') {
            steps {
                sh 'ls -lh target/*.jar'
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Building Docker image'
                sh 'docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .'
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                        echo "$DOCKER_PASS" | docker login \
                            -u "$DOCKER_USER" --password-stdin

                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}

                        docker logout
                    '''
                }
            }
        }

        stage('Deploy to Application EC2') {
            steps {
                echo 'Deploying application to EC2'

                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: 'app-server-ssh',
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    )
                ]) {
                    sh '''
                        ssh -i "$SSH_KEY" \
                            -o BatchMode=yes \
                            -o StrictHostKeyChecking=accept-new \
                            "$SSH_USER@$APP_SERVER" \
                            "DOCKER_IMAGE='$DOCKER_IMAGE' DOCKER_TAG='$DOCKER_TAG' APP_CONTAINER='$APP_CONTAINER' bash -s" <<'REMOTE'

                        set -e

                        echo "Pulling latest Docker image..."

                        sudo docker pull "${DOCKER_IMAGE}:${DOCKER_TAG}"

                        echo "Stopping old container..."

                        sudo docker stop "$APP_CONTAINER" || true

                        sudo docker rm "$APP_CONTAINER" || true

                        echo "Starting new container..."

                        sudo docker run -d \
                            --name "$APP_CONTAINER" \
                            --restart unless-stopped \
                            -p 8080:8080 \
                            "${DOCKER_IMAGE}:${DOCKER_TAG}"

                        echo "Checking application health..."

                        for i in $(seq 1 30); do
                            if curl -fsS http://localhost:8080/ >/dev/null; then
                                echo "Application deployed successfully!"
                                exit 0
                            fi

                            sleep 2
                        done

                        echo "Application health check failed!"
                        sudo docker logs --tail 50 "$APP_CONTAINER"
                        exit 1

REMOTE
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'BUILD, DOCKER PUSH AND DEPLOYMENT SUCCESSFUL!'
        }

        failure {
            echo 'PIPELINE FAILED! Check the console output.'
        }
    }
}
