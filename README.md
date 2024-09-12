# Event Tracking service

### Overview
REST service to track and record statistics of arbitrary data within a defined time period.

## Quickstart running service with Docker
1. Execute command `docker run -d --name event-tracking-service -p 8080:8080 ghcr.io/voityvaiti/event-tracking-service:latest`.
2. To shut down, execute `docker stop event-tracking-service`.

## Running service manually
1. Clone the project from the repository.
2. Build the project using Maven: `mvn clean install`.
3. Run the application: `java -jar target/event-tracking-service-0.0.1-SNAPSHOT.jar`.

## Working with Application
- After start up, application will listen to `8080` port.
- Access the application endpoints using an API testing tool (e.g. Postman) or web browser to view documentation.
- To view API documentation browse `/swagger-ui`. Or you can get raw documentation using `/api-docs` path.

## Requirements
- **Java Development Kit (JDK):** JDK 17 or later should be installed.
- **Maven:** Maven should be installed for building and managing the project dependencies.
- **IDE:** An Integrated Development Environment (IDE) such as IntelliJ IDEA, Eclipse, or NetBeans can be used for development.

## Project Structure
- **Project Type:** Maven
- **Java Version:** 17
- **Framework:** Spring Boot 3.3.3

## Additional Notes
- **Packaging:** The application is packaged as a JAR file.
- **Artifact ID:** event-tracking-service
- **Group ID:** com.myproject
- **Version:** 0.0.1-SNAPSHOT