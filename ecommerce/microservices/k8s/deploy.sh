#!/bin/bash
# Kubernetes deployment script for E-Commerce Microservices
# Usage: ./deploy.sh [build|deploy|delete]

REGISTRY="your-registry"   # replace with your Docker Hub / ECR / GCR

build_and_push() {
  echo "Building and pushing Docker images..."
  for service in eureka-server config-server api-gateway auth-service core-service ai-pricing-service; do
    echo "Building $service..."
    cd ../$service
    mvn clean package -DskipTests
    docker build -t $REGISTRY/$service:latest .
    docker push $REGISTRY/$service:latest
    cd ../k8s
  done
  echo "All images pushed!"
}

deploy_all() {
  echo "Deploying to Kubernetes..."
  kubectl apply -f 00-namespace.yml
  kubectl apply -f 01-configmap.yml
  kubectl apply -f 02-secrets.yml

  echo "Starting databases..."
  kubectl apply -f 04-auth-db.yml
  kubectl apply -f 04-core-db.yml
  kubectl apply -f 04-pricing-db.yml
  kubectl apply -f 03-rabbitmq.yml

  echo "Waiting for databases to be ready..."
  kubectl wait --for=condition=ready pod -l app=auth-db -n ecommerce --timeout=120s
  kubectl wait --for=condition=ready pod -l app=core-db -n ecommerce --timeout=120s

  echo "Starting observability stack..."
  kubectl apply -f 05-zipkin.yml
  kubectl apply -f 06-prometheus.yml
  kubectl apply -f 07-grafana.yml

  echo "Starting core services..."
  kubectl apply -f 08-eureka-server.yml
  kubectl wait --for=condition=ready pod -l app=eureka-server -n ecommerce --timeout=120s

  kubectl apply -f 09-config-server.yml
  kubectl wait --for=condition=ready pod -l app=config-server -n ecommerce --timeout=120s

  kubectl apply -f 10-api-gateway.yml
  kubectl apply -f 11-auth-service.yml
  kubectl apply -f 12-core-service.yml
  kubectl apply -f 13-ai-pricing-service.yml
  kubectl apply -f 14-ingress.yml

  echo "Deployment complete!"
  echo "Run: kubectl get pods -n ecommerce"
}

delete_all() {
  echo "Deleting all resources..."
  kubectl delete namespace ecommerce
}

case "$1" in
  build)   build_and_push ;;
  deploy)  deploy_all ;;
  delete)  delete_all ;;
  *)       echo "Usage: ./deploy.sh [build|deploy|delete]" ;;
esac
