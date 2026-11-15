# christmas-herd

## The application

christmas-herd collects animal pictures for Christmas. From a web page, the user
asks for a number of
images of an animal (cat, dog...). The application downloads them from public
image services, stores them
in a database and lists them, so they can be viewed later.

### Flow architecture

```
 browser
    │  http://localhost:91/front/
    ▼
 nginx-ingress ──── /front/  → Vue.js frontend (static files)
    │
    │  /api/*
    ▼
 tomcat-app : app-back ──────────► postgres (image table)
    │
    │  /proxy/<animal>
    ▼
 nginx-gateway (round robin + failover between the backends of an animal)
    │
    ▼
 tomcat-int : image-adapter ─────► external image services (cataas.com, placekitten.com...)
```

| Module / service           | Role                                                                                                                                                            |
|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `frontend`                 | Vue 3 + Element Plus single page app, deployed into nginx.                                                                                                      |
| `app-back`                 | REST API (Jersey) used by the frontend. Downloads the requested images through the gateway and stores them in postgres.                                         |
| `image-adapter`            | One REST resource per external image service (`/api/images/cataas`, `/api/images/placekitten`...). It returns the image as is, or `502` if the service is down. |
| `shared-api`               | Code shared by the Java modules (animal types, HTTP client).                                                                                                    |
| `deployment/nginx-ingress` | Public entry point: serves the frontend and routes `/api` to app-back.                                                                                          |
| `deployment/nginx-gateway` | Exposes `/proxy/<animal>`: round robin between the backends of an animal, with failover when one is down (max 3 connections per backend).                       |
| `deployment/postgres`      | Image database (schema in `postgres/initdb`).                                                                                                                   |

*nginx-ingress*: also "solves" CORS *nginx-gateway*: The purpose of this layer
is to add the failover/round robin

### REST API (app-back)

| Method | URL                        | Description                                                                                                                                                                 |
|--------|----------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/api/animals/requests`    | Body `{"animalType": "CAT", "count": 3}`. Downloads and stores `count` images (1 to 100, `MAX_IMAGES_PER_REQUEST`). `400` if no backend exists for this animal, `502` if the gateway returns no image. |
| `GET`  | `/api/images`              | Stored images: `id`, `createdAt`, `source` (backend which served the image), `animalType`, `size`.                                                                          |
| `GET`  | `/api/images/{id}/content` | Content of an image.                                                                                                                                                        |

Only the `cat` backends are configured in the gateway for now: requesting dogs
returns a `400`.

## Build

### Prerequisites

- JDK 21 and Maven 3
- Node.js 18+ and npm
- Docker with Docker Compose
- for the local execution: Tomcat 10.1

### Build

Maven and NPM build the artifacts directly into `artifact/`. Artifacts are
mounted
into docker images by docker compose.

```shell
mvn package -DskipTests
cd frontend
npm install
npm run build
```

## Execution

Two execution modes are available. Local execution for development (hot reload).
Docker compose is used for integration tests, simulating production
environments.

|               | docker-compose                   | local                                       |
|---------------|----------------------------------|---------------------------------------------|
| frontend      | **<http://localhost:91/front/>** | **<http://localhost:5173/front/>** (Vite)   |
| app-back      | 9083 (`/app-back`)               | 9082 (`/app-back`)                          |
| image-adapter | 9080 (`/image-adapter`)          | 8080 (`/image-adapter`)                     |
| nginx-gateway | 9081                             | none: app-back calls image-adapter directly |
| postgres      | 4444                             | 4444 (container)                            |

### With docker-compose

```shell
cd deployment
docker compose up -d --build --wait
```

*note*: There is a 'developer' network in place, to help accessing directly to
any components during development.
It's better to remove it during integration tests.

**Frontend: <http://localhost:91/front/>**. The port is `NGINX_HTTP_PORT` in
`deployment/.env`
(`90` if it is not set), with the database credentials.

Other entry points, useful to test or debug:

| URL                                                     | Service                                             |
|---------------------------------------------------------|-----------------------------------------------------|
| <http://localhost:91/api/images>                        | app-back API, through nginx-ingress                 |
| <http://localhost:9083/app-back/api/images>             | app-back, direct access to tomcat-app               |
| <http://localhost:9081/proxy/cat>                       | nginx-gateway: a cat image (round robin + failover) |
| <http://localhost:9080/image-adapter/api/images/cataas> | image-adapter, direct access to a backend           |
| (jdbc:postgresql://localhost:4444/camunda)              | postgres (database, user and password `camunda`)    |

#### Network

```plantuml
@startnwdiag
  browser [shape = actor, description = "Host\n(browser)"];
  inet [shape = cloud, description = "Internet\nexternal image services"];

  network public {
    description = "public (bridge)\nhost: 91 → nginx-ingress";
    browser;
    nginx_ingress [description = "nginx-ingress", address = ":80"];
  }
  network application {
    description = "application (internal)";
    nginx_ingress;
    tomcat_app [description = "tomcat-app\n(app-back)", address = ":8080"];
    nginx_gateway [description = "nginx-gateway", address = ":80"];
    postgres [shape = database, description = "postgres", address = ":5432"];
  }
  network integration {
    description = "integration (bridge)\nhost: 9081 → nginx-gateway\n9080 → tomcat-int";
    nginx_gateway;
    tomcat_int;
    inet;
  }
   
@end
```

| Network       | Type     | Members                                                        | Purpose                                                                            |
|---------------|----------|----------------------------------------------------------------|------------------------------------------------------------------------------------|
| `public`      | bridge   | nginx-ingress                                                  | the only entry point: port 91 (front and API)                                      |
| `application` | internal | nginx-ingress, tomcat-app, nginx-gateway, tomcat-int, postgres | `/api` proxy, gateway calls (`/proxy/<animal>`), database, gateway → image-adapter |
| `integration` | bridge   | nginx-gateway, tomcat-int                                      | outbound Internet access of image-adapter; host ports 9080 / 9081 for tests        |
| `developer`   | bridge   | tomcat-app, postgres                                           | host access for dev and tests: app-back on 9083, postgres on 4444                  |

### Locally (two Tomcat + Vite)

It is possible to deploy webapps either in two distinct Tomcat server or in only one.
The frontend runs into vite.
It's probably better to use the Postgres in docker, avoiding a laptop install. 
No Nginx required during development.

#### Network

Everything runs on the host (`localhost`), on the same network.

```plantuml
@startnwdiag
  inet [shape = cloud, description = "Internet\ncataas.com"];

  network localhost {
    description = "localhost (host)";
    browser [shape = actor, description = "browser"];
    vite [description = "Vite\n(frontend)", address = ":5173"];
    app_back [description = "Tomcat App Back\n(app-back)", address = ":9082"];
    image_adapter [description = "Tomcat Image Adapter\n(image-adapter)", address = ":8080"];
    postgres [shape = database, description = "postgres\n(Docker container)", address = ":4444"];
  }
  network internet {
    description = "Internet";
    image_adapter;
    inet;
  }
@end
```

#### Tomcat configuration (once)

The postgres driver is shipped in the war (`WEB-INF/lib`): nothing to add to the Tomcat `lib`.
**datasource** (IntelliJ only): copy the `<Resource name="jdbc/christmas" .../>` of
   `app-back/src/main/webapp/META-INF/context.xml` into `$CATALINA_HOME/conf/context.xml`. 
It's also possible to create copy this file into `conf/Catalina/localhost/app-back.xml`.


#### With IntelliJ

The run configurations are in `.idea/runConfigurations`:

| Run configuration | Artifact            | Context          | HTTP port | Settings                                        |
|-------------------|---------------------|------------------|-----------|-------------------------------------------------|
| `Image Adapter`   | `image-adapter:war` | `/image-adapter` | 8080      | none                                            |
| `App Back`        | `app-back:war`      | `/app-back`      | 9082      | see system properties in Server › VM options |

#### Without IntelliJ

One Tomcat installation (`$CATALINA_HOME`), one base directory per application.
The App Back base listens on 9082 (`conf/server.xml`: HTTP connector `port="9082"`, 
shutdown port `-1` or another free port) and sets the system properties in `bin/setenv.sh`:

```shell
export CATALINA_OPTS="-DPOSTGRES_HOST=localhost -DPOSTGRES_PORT=4444 -DPOSTGRES_DB=camunda \
  -DPOSTGRES_USER=camunda -DPOSTGRES_PASSWORD=camunda \
  -DIMAGE_URL=http://localhost:8080/image-adapter/api/images/cataas"
```

Copy `artifact/proxy/image-adapter.war` and `artifact/back/app-back.war` into
the `webapps` of each server base, then start :

#### Start
Tomcat(s):
```shell
CATALINA_BASE=<base> $CATALINA_HOME/bin/catalina.sh run
```
```shell
cd deployment && docker compose up -d --wait postgres
# start the "Image Adapter" Tomcat, then the "App Back" Tomcat
cd frontend && npm run dev # http://localhost:5173/front/
```

The Vite reverse proxy is configures to send `/api` to `http://localhost:9082/app-back/api`
This is adjustable in vite.confi.js.

## Tests

```shell
mvn verify
```
## Missing elements
Dog images are not supported on purpose. The application is designed to 
be "plug 'n play". To support a new backend server:
1. Create an AnimalResource for Dog image server. You may start with a copy of cataas package
2. register this new class into src/main/resources/META-INF/services/eu.stephane.AnimalResource
3. create a configuration for the nginx-gateway server, either copying the cat files (or using claude). Claude.ai has a declared agent which should handle your request.

*note*: some servers requires several interactions to fetch an image. It's 
recommended to create a dedicated librairy. Including it into the classpath
makes it available to the application without more effort.