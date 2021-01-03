# Souvenirs - API

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

**Prerequisite :** A PostgreSQL database must be installed and match configuration in `src/main/resources/application.properties`

```bash
./gradlew bootRun
```

### Run with Docker

**Prerequisite :** Make sure you have an **external** PostgreSQL database for this one :

```bash
docker run -d --name souvenirs-api \
  -e SPRING_DATASOURCE_URL=<YOUR_DB_JDBC_URL> \
  -e SPRING_DATASOURCE_USERNAME=<YOUR_DB_USERNAME> \
  -e SPRING_DATASOURCE_PASSWORD=<YOUR_DB_PASSWORD> `
  thomah/souvenirs-api:latest
```