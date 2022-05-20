# Nuvalence Work Manager

## Prerequisites

Make sure you have the following installed:

1. Java 11+
2. Docker
3. [Camunda Modeler](Make sure you have done the following before you can deploy)

## Run Locally
1. run this command: `./gradlew clean build composeUp`
2. [view docs](http://localhost:8080/swagger-ui.html)

The app can be brought down via:

`./gradlew composeDown`

## Deploying Camunda BPMN diagrams
1. Open the desired BPMN diagram in the Camunda Modeler application
2. Click on the 'Deploy Current Diagram' icon on the bottom-left corner of the window
3. Select a Deployment Name and provide `http://localhost:8080/engine-rest` as the REST Endpoint URL, click 'Deploy'
4. [Camunda Cockpit](http://localhost:8080/camunda/app/cockpit/default/)
5. [Camunda Tasklist](http://localhost:8080/camunda/app/tasklist/default/)
6. Username and Password for Cockpit and Tasklist are admin/admin

## Querying Postgres locally via IntelliJ
1. Open the database tab in the top right
2. Add new datasource `PostgresSQL`
3. Add host as `localhost` and the port as `5438`
4. Add your database as `workmanagerpostgres`
5. Add your user as `root` and password as `root`
6. Hit apply and save

## Querying Postgres locally via pgAdmin
1. Go to the local instance of [pgAdmin](http://localhost:5050/)
2. Log in with Email Address / Username: `admin@admin.com` and password `root`
3. Register a server with the following details:
   1. Name: `workmanagerpostgres` (can be anything though)
   2. Host name/address: `workmanagerpostgres`
   3. Port: `5438`
   4. Maintenance database: `workmanagerpostgres`
   5. Username: `root`
   6. Password: `root`
   7. Toggle `Save password?` on

NOTE: These details are also in the [docker-compose.yml](./docker-compose.yml) file.

## Debugging docker-compose locally via IntelliJ

These instructions were loosely taken from:
`https://www.jetbrains.com/help/idea/run-and-debug-a-spring-boot-application-using-docker-compose.html`

1. Open the [docker-compose.yml](./docker-compose.yml) file
2. Build/run the application via: `./gradlew clean build composeUp`
   1. Can also be accomplished by clicking the icon (that looks like a double right-facing triangle) in the gutter of docker-compose.yml next to `services`
3. Create a remote debug configuration:
   1. Click the icon (that looks like a bug) in the gutter next to `command` under the `workmanager` service
   2. Set `Use module classpath` to `work-manager`
   3. Double-click the Docker Compose run configuration in the Before launch list.
      1. If it is not in the list, click the Add button and select Launch Docker before debug
   4. Change the Custom command to the following: `java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -jar -Dspring.profiles.active=dcompose application.jar`
   5. Apply changes.
4. Click the icon (that looks like a bug) in the gutter next to `command` under the `workmanager` service
5. Select `Debug 'Remote JVM Debug with compose'`

### Documentation
- [tools and frameworks](./docs/tools.md)

## Contributors
The Nuvalence Work Manager was originally a private project with contributions from:
- [@JayStGelais](https://github.com/JayStGelais)
- [@gcusano](https://github.com/gcusano)
- [@apengu](https://github.com/apengu)
- [@bsambrook](https://github.com/bsambrook)
- [@katt-mim](https://github.com/katt-mim)
- [@dtsong](https://github.com/dtsong)
- [@franklincm](https://github.com/franklincm)
- [@Mark-The-Dev](https://github.com/Mark-The-Dev)
- [@gcastro12](https://github.com/gcastro12)
- [@LPMarin](https://github.com/LPMarin)
