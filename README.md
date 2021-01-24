# Souvenirs - API

## Database

By default, the app starts with an embedded H2 database you can access the console at the following URL : http://localhost:8080/h2-console

You can use an external PostgreSQL database by overriding environment variables :

```bash
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_DATASOURCE_URL=jdbc:postgresql://<YOUR_DB_HOST>:<YOUR_DB_PORT>/<YOUR_DB_NAME>
SPRING_DATASOURCE_USERNAME=<YOUR_DB_USERNAME>
SPRING_DATASOURCE_PASSWORD=<YOUR_DB_PASSWORD>
```

## Build

The app only :

````bash
./gradlew build
````

The app + Docker image :

````bash
./gradlew bootBuildImage --imageName=thomah/souvenirs-api
````

## Run

### Run with Gradle

```bash
./gradlew bootRun
```

### Run with Docker

```bash
docker run -d --name souvenirs-api \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
  -e SPRING_DATASOURCE_URL=<YOUR_DB_JDBC_URL> \
  -e SPRING_DATASOURCE_USERNAME=<YOUR_DB_USERNAME> \
  -e SPRING_DATASOURCE_PASSWORD=<YOUR_DB_PASSWORD> \
  -v <YOUR_HOST_DIRECTORY>:/workspace/files \
  thomah/souvenirs-api:<tag>
```

### Check if the app is running

```bash
curl --head --location 'http://localhost:8080/health'
```

Result should be like :

```bash
HTTP/1.1 200
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Content-Length: 0
Date: Sun, 24 Jan 2021 16:48:27 GMT
```