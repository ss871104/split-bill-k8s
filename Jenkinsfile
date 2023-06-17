pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'ss871104'
        PATH = "${env.PATH}:/usr/local/bin:/opt/homebrew/bin"
    }

    tools {
        maven 'Maven'
    }

    stages {
        stage('Initialize') {
            steps {
                sh 'echo "Starting pipeline..."'
            }
        }

        stage('Maven Install') {
            steps {
                withMaven(maven: 'Maven') {
                    sh 'mvn clean install -DskipTests'
                }
            }
        }

        stage('Build Docker Images') {
            parallel {
                stage('Build api-gateway image') {
                    steps {
                        dir('api-gateway') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-api-gateway ."
                        }
                    }
                }
                stage('Build auth-service image') {
                    steps {
                        dir('auth-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-auth-service ."
                        }
                    }
                }
                stage('Build master-command-service image') {
                    steps {
                        dir('master-command-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-master-command-service ."
                        }
                    }
                }
                stage('Build master-query-service image') {
                    steps {
                        dir('master-query-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-master-query-service ."
                        }
                    }
                }
                stage('Build bill-command-service image') {
                    steps {
                        dir('bill-command-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-bill-command-service ."
                        }
                    }
                }
                stage('Build bill-query-service image') {
                    steps {
                        dir('bill-query-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-bill-query-service ."
                        }
                    }
                }
                stage('Build notification-service image') {
                    steps {
                        dir('notification-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-notification-service ."
                        }
                    }
                }
                stage('Build batch-service image') {
                    steps {
                        dir('batch-service') {
                            sh "docker build -t $DOCKER_REGISTRY/split-bill-k8s-batch-service ."
                        }
                    }
                }
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Deploy Namespaces') {
            steps {
                dir('k8s') {
                    sh 'kubectl apply -f namespace.yml'
                }
            }
        }

        stage('Deploy ConfigMaps and Secrets') {
            steps {
                dir('k8s/secret') {
                    sh 'kubectl apply -f .'
                }
                dir('k8s/configmap') {
                    sh 'kubectl apply -f .'
                }
            }
        }

        stage('Deploy DaemonSets') {
            steps {
                dir('k8s/daemonset') {
                    sh 'kubectl apply -f .'
                }
            }
        }

        stage('Deploy StatefulSets') {
            steps {
                dir('k8s/statefulset') {
                    sh 'kubectl apply -f .'
                }
            }
        }

        stage('Check Cassandra') {
            steps {
                script {
                    def cassandraResponding = false
                    while (!cassandraResponding) {
                        try {
                            def response = sh(script: "kubectl exec cassandra-0 --namespace db -- cqlsh -e 'DESCRIBE KEYSPACES;'", returnStdout: true).trim()
                            if (response.contains("system")) {
                                echo "Cassandra is responding."
                                cassandraResponding = true
                            }
                        } catch (Exception e) {
                            echo "Cassandra is not responding yet. Retrying in 30 seconds..."
                            sleep 10
                        }
                    }
                }
            }
        }

        stage('Cassandra Initialization') {
            steps {
                script {
                    sh 'chmod +x k8s/cassandra-init.sh'
                    sh 'k8s/cassandra-init.sh'
                }
            }
        }

        stage('Check Helm') {
            steps {
                sh 'helm version'
            }
        }


        stage('Deploy Ingress Controller') {
            steps {
                sh 'helm upgrade --install --namespace menstalk ingress-nginx ingress-nginx/ingress-nginx'
            }
        }


        stage('Deploy My Services to Kubernetes') {
            steps {
                dir('k8s/deployment') {
                    sh 'kubectl apply -f .'
                }
            }
        }

        stage('Check Webhook Service') {
            steps {
                script {
                    def ready = false
                    for (int i = 0; i < 30; i++) {  // Try for up to 5 minutes
                        try {
                            // Try to call the webhook service
                            sh(script: '''
                                kubectl run --rm -i --tty --restart=Never dummy --image=alpine -- sh -c "apk add curl && curl -k https://ingress-nginx-controller-admission.menstalk.svc:443"
                            ''', returnStdout: true).trim()
                            ready = true
                            break
                        } catch (Exception e) {
                            // Ignore the exception and retry after 10 seconds
                            echo 'Webhook service not ready yet, retrying in 10 seconds...'
                            sleep 10
                        }
                    }

                    if (!ready) {
                        error('Webhook service did not become ready within 5 minutes')
                    }

                    echo 'Webhook service is ready.'
                }
            }
        }


        stage('Deploy Ingress Resources') {
            steps {
                dir('k8s/ingress') {
                    sh 'kubectl apply -f .'
                }
            }
        }


        stage('Cleanup') {
            steps {
                sh '''
                  docker rmi $(docker images -f "dangling=true" -q) --force
                '''
            }
        }

    }
}
