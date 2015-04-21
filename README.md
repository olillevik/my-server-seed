My reference Java architecture
================================

Based on the SopraSteria referance architecture
--------------------------------

* Any change developers make during development should be reflected in the running application within few seconds
* The application starting point should minimize the number of technologies and especially limit the use of sophisticated technologies
* The application should support the latest version of any involved technologies
* The application should promote cross-tier understanding
* The application should answer deployment considerations in deployment into existing infrastructures, creating a virtualized ecosystem of servers and options in between

The goal of the reference application is to create a full stack application that any developer can master. In order to achieve this goal, we avoid "clever" technologies. This includes technologies that rely on reflection or classpath manipulation. It's quite possible to add such technologies to the reference, but we don't consider it a good starting point.

This guide covers development and deployment scenarios with an application build on the reference architecture:

* Downloading the source and getting started developing
* Deploying on a local box with just a JVM
* Deploying on an existing application server
* Deploying a cluster of servers

![Alt text](http://g.gravizo.com/g?
@startuml;
  actor User;
  cloud {;
     User -> [HTTP];
  };
  package "Application" {;
    %28%29 "Big IP";
    package "Proxy tier" {;
      node "Nginx 1";
      node "Nginx 2";
      HTTP -down- [Big IP];
      [Big IP]  -down- [Nginx 1];
      [Big IP]  -down- [Nginx 2];
    };
    package "Application tier" {;
      node "Jetty 1";
      node "Jetty 2";
      node "Jetty 3";
      [Nginx 1] -- [Jetty 1];
      [Nginx 2] -- [Jetty 1];
      [Nginx 1] -- [Jetty 2];
      [Nginx 2] -- [Jetty 2];
      [Nginx 1] -- [Jetty 3];
      [Nginx 2] -- [Jetty 3];
    };
    package "Data tier" {;
      database "pgsql 1";
      database "pgsql 2";
      [Jetty 1] -- [pgsql 1];
      [Jetty 2] -- [pgsql 1];
      [Jetty 3] -- [pgsql 1];
      [pgsql 1] .right.> [pgsql 2] : replication;
    };
  };
  @enduml
  )

Getting started developing
--------------------------------
### Prerequisites

* A git client (for Windows, we recommend [TortoiseGit](https://code.google.com/p/tortoisegit/) and [Msysgit](https://msysgit.github.io/))
* An IDE (we recommend Eclipse or IntelliJ with Infinitest plugin)
* JDK 1.8
* Maven 3
* PostgreSQL
* Recommended: An SQL client (we recommend  [dbeaver](http://dbeaver.jkiss.org/))

### Steps

1. Retrieve the source code from ....:  `git clone ...`
2. Import the project into your IDE
  * For Eclipse, run `mvn eclipse:eclipse -DdownloadSources` from the command line and do `File` > `Import...` > `Existing project into workspace` in Eclipse
  * For IntelliJ, 
3. Create the database schema
  * Log in with the root user on PostgreSQL (locally)
  * `CREATE USER seed WITH PASSWORD 'seed'`
  * `CREATE DATABASE seed OWNER seed`
  * `CREATE USER seed_test WITH PASSWORD 'seed_test'`
  * `CREATE DATABASE seed_test OWNER seed_test`
4. Run the tests
  * In Eclipse, right click on the project and select `Run As` > `JUnit test`
  * In IntelliJ, ...
5. Run the `net.openright.simpleserverseed.SeedAppServer` main class in the debugger
6. Visit the application at http://localhost:3000/
7. You can edit client HTML, JavaScript or CSS under `src/main/resources/webapp`. Any changes will be reflected when you refresh the browser
8. You can edit server side code, for example `net.openright.simpleserverseed.domain.order.OrderApiController `. Any changes that don't cross a method boundary will be reflected when you refresh, as long as you run in the debugger
9. Any changes that are not supported by the debugger will be reflected when you start `net.openright.simpleserverseed.SeedAppServer` again (starting again will shut down the old server instance)
0. To change the database schema, add new migrations under `src/main/resources/db`. The changes will be reflected when you restart the application server

### Configuration (Optional)
The application starts with a working default configuration given you have followed the steps above. Default values are set in AppConfigFile.java. Two configuration files are loaded if you create them.

1. seedapp.properties - This configuration is loaded when you run SeedAppServer.java
2. seedapp-test.properties - This configuration is loaded by the JUnit tests.

Values in the configuration are key=value pairs. The following parameters are examples of configurable parameters:
```
seed.db.username=some_database_user
seed.db.password=a_password_for_the_database_user
```
By default, the database settings use Postgresql on localhost with «seed» as username and password when running the dev server and «seed_test” when running unit tests.

Development quick guide
------------------------------

![Component diagram](http://g.gravizo.com/g?
@startuml;
  skinparam componentStyle uml2;
  package "Application" {;
    node Client {;
      [Frame] --> [Product];
      [Frame] --> [Orders];
	    [Orders] --> [jQuery.AJAX];
      package "Product" {;
        [product view 1.html] --> [ProductController.js];
        [product view 2.html] --> [ProductController.js];
        [ProductController.js] --> [jQuery.AJAX];
      };
      package "Orders";
    };
    node Server {;
      [Database.java];
      [jQuery.AJAX] --> [FrontController];
      folder "ProductServer" {;
        [FrontController] --> [ProductController];
        [Product domain];
        [ProductController] --> [ProductRepository];
        [ProductRepository] --> [Database.java];
      };
      package "OrderServer";
      [OrderServer] --> [Database.java];
      [FrontController] --> [OrderServer];
    };
    [Database.java] --> [Database];
    database Database {;
      folder "Products table";
      folder "Orders table";
    };
  };
@enduml;
)

### Adding view

E.g. a new view to list users.

1. Add 'Users' as a link to `#users` the top level menu in `src/main/resources/webapp/index.html` in the nav section
  * Notice: Indicating which menu item is active is not yet implemented 
2. Add `src/main/resources/webapp/users/index.html`
3. Make `users/index.html` display 'users/list'
4. Add `src/main/resources/webapp/users/_list.html` with the content of the list view

### Adding a JavaScript read view

E.g. a new view to list users.

1. Create a Mustache template for the user list: `<script type="text/x-handlebars-template" id="usersTemplate">`
2. Load the template `var usersTemplate = Handlebars.compile($("#usersTemplate").html());`
3. When the page is loaded, call
   `ajax.get('api/users').then(function(d) { $("#target").html(usersTemplate(d.users)); }`
4. Linking to specific items

### Adding a JavaScript write view

1. The client delegate JavaScript (with promises)
2. Posting data from a form
2. Validation with HTML5
3. Client side validation
4. Handling responses and errors

### Providing a JSON or XML endpoint

1. The naming standard and superclass for the Controller
2. Mapping to and from JSON
   * Alternative: Mapping to and from XML
3. Mapping to and from database

### Consuming a JSON or XML endpoint

1. The naming standard for the gateway and the HTTP client
2. Configuring the server address
3. Mapping to and from the network format with JSON
  * Alternative: Mapping with XML
  * Alternative: Mapping with a specialized format (EDIFACT, fixed width records)

### Publishing messages with an HTTP feed

![Feeds](http://g.gravizo.com/g?
@startuml;
  node Client;
  Client --> HTTP;
  node Server {;
    HTTP --> [FeedController] <<<&rss> rss>>;
    [FeedController] --> [Repository];
    HTTP --> [Controller];
    Controller --> [FeedCache];
    Controller --> [FeedGateway];
    [FeedGateway] --> HTTP;
  };
  database Database;
  [Repository] --> [Database];
@enduml
)

1. Starting with the XML endpoint
2. Use RSS (or Atom) - optional
3. Add a `since` http parameter
  * Micro-caching
4. Use the `since` parameter into the database

### Consuming messages with an HTTP feed

1. Starting by consuming an XML endpoint
2. Keep track of the last timestamp
  * Micro-caching

### Batching jobs for later processing

1. Create a new subclass of `com.soprasteria.infrastructure.queue.WorkItem`
2. Implement `WorkItem#process(Database)` with the logic to process one work item

### Saving data in the database

1. Creating a repository class
2. Create a new migration file under `src/main/resources/db`. This file must contain DDL


Deploying on a standalone JVM
-------------------------------------
(This alternative is based on the maven-shade-plugin approach. It's possible to use maven-dependency-plugin as well as a script that builds the classpath as well)

(Creating a self-executing _war_ (as opposed to jar) will lead to longer build times, larger deployment files and use of temp-files. If 3rd party app server support is not needed, jar-files are a better option)

1. Run `mvn package`
2. Copy `target/openright-rest-jdbc-server-1.0.0.jar` to the target server.
  * This can be a folder on the local machine. It can even be the `target` directory itself
3. Extract the base configuration `java -jar openright-rest-jdbc-server-1.0.0.jar` on the target directory. This will extract `seedapp.properties`
4. Modify the configuration settings in `seedapp.properties`
5. Start the server `java -jar openright-simple-seed-server-1.0.0.jar`
6. Visit the application at http://localhost:8000/seedapp
7. Visit the status page for the application at http://localhost:8000/status/admin

Other deployment options
---------------------------------------------------

### Deploying to an existing app server

In order to deploy to an app server, a war-file needs to be built instead of a Jar file. Here is one way to do it:

* Move `src/main/resources` to `src/main/webapp`
* Change the artifact type of the project from `jar` to `war`
* Move the Server class to `src/test` instead of `src\main`
* The call in EmbeddedWebAppContext to setBaseResource(Resource.newClassPathResource("/webapp")) must instead be setBaseResource(Resource.newResource("src/main/webapp"))

_Note: The application assumes that its dependencies are before the app server jars. Make sure that you deploy with the "prefer application classpath" option_



Creating a virtualized server ecosystem with Vagrant (TODO)
-----------------------------------------------------------
(We prefer using Vagrant to build a VMWare image that can be deployed directly on a VMWare cluster. If the application is given existing infrastructure to run on, Docker would be a better choice)

(Virtualization is an area in a lot of flux, and new solutions are likely to appear)

### Prerequisites

* VirtualBox
* Vagrant (requires Ruby)

### Running locally in VirtualBox

1. Go to the `src/main/vagrant` directory
2. Edit the IP addresses for the servers in ...
2. Create the database server: `cd pgsql-01-prime; vagrant up`
3. Create the database backup server `cd pgsql-02-standby; vagrant up`
4. Create the app servers:
  * `cd app-01; vagrant up`
  * `cd app-02; vagrant up`
  * `cd app-03; vagrant up`
5. Create the proxy servers:
  * `cd web-01; vagrant up`
  * `cd web-02; vagrant up`
6. Going to http://web-01 or http://web-02 will access the application.
7. The application should gracefully handle traffic as long as at least one app server is up
   * `cd app-01; vagrant destroy`
   * `cd app-02; vagrant destroy`
   * Refresh the application - it should still work as app-03 is running
8. When the primary database goes down, the application can recover by using the secondary
  * `ssh vagrant@pgsql-02-standby <command to make it primary>`
  * (Some magic to make the app servers switch)
  * How to create a new standby based on 02.

### Creating VMWare images
