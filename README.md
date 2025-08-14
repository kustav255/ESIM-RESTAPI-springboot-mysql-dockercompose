# ESIM-RESTAPI-springboot-mysql-dockercompose
API REST Project, developed with SpringBoot, MySQL and DockerCompose

To use this project, you will need docker (and docker compose)

Build Maven:
```
./mvnw clean install
```

Docker Compose Build and Run:
```
docker compose build
docker compose up -V
```

Use if you want to clean docker compose volumes:
```
docker compose down -v
```


Link to Swagger:
http://localhost:8080/swagger-ui/index.html
