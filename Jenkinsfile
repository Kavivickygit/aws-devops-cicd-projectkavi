
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
                checkout scm
            }
        }

        stage('Build') {
            steps {
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
                sh '''
                    ssh -i /var/lib/jenkins/.ssh/id_ed25519 \
                        -o BatchMode=yes \
                        -o IdentitiesOnly=yes \
                        -o StrictHostKeyChecking=accept-new \
                        ubuntu@$APP_SERVER \
                        "DOCKER_IMAGE='$DOCKER_IMAGE' DOCKER_TAG='$DOCKER_TAG' APP_CONTAINER='$APP_CONTAINER' bash -s" <<'REMOTE'

                    set -e

                    echo "Pulling Docker image..."
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

    post {
        success {
            echo 'BUILD, DOCKER PUSH AND DEPLOYMENT SUCCESSFUL!'
        }

        failure {
            echo 'PIPELINE FAILED! Check the console output.'
        }
    }
}
