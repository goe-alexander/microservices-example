# Microservices Example

### To run the project build the docker images using the spotify MAVEN plugin:
```bash
 sudo mvn clean package docker:build
```

### Then use the docker composer file to run the containers:
```bash
sudo docker-compose -f docker/common/docker-compose.yml up
```
