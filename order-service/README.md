# order-service (Spring Boot + Kafka)

REST -> Kafka producer -> `orders` topic -> Kafka consumer (retry + dead-letter topic).

## Run locally (plain Kafka from the docker-compose setup)
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    curl -X POST localhost:8080/orders -H 'Content-Type: application/json' \
         -d '{"customerId":"c1","amount":49.9}'

## Deploy on Kubernetes (Strimzi)
1. `kubectl apply -f k8s/kafka-resources.yaml`   (topics + user + ACLs)
2. Build and push the image:  `docker build -t <ecr-repo>/order-service:1.0.0 .`
3. Edit the image in `k8s/deployment.yaml`, then `kubectl apply -f k8s/deployment.yaml`

If the app runs in another namespace, copy the `order-service` and `prod-kafka-cluster-ca-cert`
Secrets into it (or create the KafkaUser there).

## Test the dead-letter flow
Send an order with a negative amount: it is retried 3 times, then lands in `orders.DLT`.
