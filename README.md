# MasterAPI 
This RESTful framework was developed by following:

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/gradle-plugin/reference/html/)
* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.3.RELEASE/maven-plugin/)
* [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#production-ready)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Java Mail Sender](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#boot-features-email)
* [Spring Security](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#boot-features-security)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service with Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

So, you have to know about these frameworks and libraries. [Getting Started](#getting_started)

## <a name="getting_started"></a>Getting started
- Clone or download the project from [github](https://github.com/SUNDEWMYANMAR/master-api)
- Open project in Java IDE such as Intellij, Eclipse, NetBeans, etc ...
- Clone following setting files:
	- [example.application.properties](./src/main/resources/example.application.properties) => __application.properties__
	- [example.logback-spring.xml](./src/main/resources/example.logback-spring.xml) => __logback-spring.xml__
- Create system directories:
    - File upload directory. (Example: /var/www/master-api/upload/)
    - Template directory. (Example: /var/www/master-api/template/)
    - Log directory. (Example: /var/www/master-api/log/)
- Copy/Paste templates files:
    - [templates](./src/main/resources/templates) => __Template directory__
- Edit require properties from **application.properties** and **logback-spring.xml** files.

### Require config to edit in **logback-spring.xml**
Modified output directory.
```xml
<property name="logPath" value="{log_directory}"/>
```

### Require properties to edit in **application.properties** 
[Ref: Spring Boot application.properties](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)

#### Database Setting
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/{database_name}
spring.datasource.username={db_user}
spring.datasource.password={db_password}
```

#### Path Setting
```properties
com.sdm.path.template={mail_template_directory}
com.sdm.path.upload={file_upload_directory}
```

#### Mail Server Setting
```properties
spring.mail.username={mail-server:username}
spring.mail.password={mail-server:password}
```

Run Spring Boot Application [Ref:]
```bash
> mvn clean spring-boot:run
```

### Generate Salt for Encryption
[http://localhost:8080/util/salt](http://localhost:8080/util/salt) <br/>
Copy/Paste encrypt salt to application.properties.
```properties
com.sdm.security.encrypt-salt={generated_encrypt_salt}
```

### Generate JWT Key For AccessToken
[http://localhost:8080/util/jwtKey](http://localhost:8080/util/jwtKey) <br/>
Copy/Paste jwt key to application.properties.
```properties
com.sdm.security.jwt-key={generated_jwt_key}
```

### Ready for your API system now!
Stop and Run Spring Boot again to test
```bash
> mvn clean spring-boot:run
```
Ready Output log:
```bash
2020-01-28 13:41:44.597  INFO 2727 --- [main] com.sdm.Application   : System is running...
```

### Generate war file to upload web server!
```bash
> mvn clean package
```
___

## Version 1.5
- Development Version

## Package Structure
### Default Package for New Module
- controller
- model
    - request
    - response
    - entity
- repository
- service

### Core System
- [System Configurations](./src/main/java/com/sdm/core/config)
- [Base Controller & Helper](./src/main/java/com/sdm/core/controller)
- [Database Helper](./src/main/java/com/sdm/core/db)
- [Exception Handling](./src/main/java/com/sdm/core/exception)
- [Base Models](./src/main/java/com/sdm/core/model)
- [Utils & Plugins](./src/main/java/com/sdm/core/util)

### [System Authorization and Authentication](./src/main/java/com/sdm/auth)
- System auth by user:password
- Default User Profile
- Auth Token Management

### [System Administration](./src/main/java/com/sdm/admin)
- User & Role Management
- Route Permission by Role
- Menu Management 
- Menu Permission by Role

### [File Management](./src/main/java/com/sdm/file)
- File Upload Download
- Generated Public URL
- Image resizing

### [Facebook Messenger Bot](./src/main/java/com/sdm/facebook)
- Messenger Bot Activate
- Messenger Bot Listener
- Message Log
----

## Default RestFUL Controller
### ReadController
|method|http-method|url/{path}?{query_params}|description|
|------|------|------|------|
|getPagingByFilter|GET|name(s)?{size, pageSize, page, sort, filter}|Get data by pagination
|getAll|GET|name(s)/all|Get all data|
|getById|GET|name(s)/{id}|Get data by unique id(PK)|
|exportByCsv|GET|name(s)/export|Export data as CSV file|
|getStructure|GET|name(s)/struct|Get UI Structure|

### WriteController
|method|http-method|url/{path}?{query_params}|description|
|------|------|------|------|
|create|POST|name(s)|Create new data
|multiCreate|POST|name(s)/multi|Create multiple data|
|update|PUT|name(s)/{id}|Modified data by unique id(PK)|
|multiUpdate|PUT|name(s)/multi|Modified multiple data|
|partialUpdate|PATCH|name(s)/{id}|Partially modified data by unique id(PK)|
|remove|DELETE|name(s)/{id}|Remove data by unique id(PK)|
|multiRemove|DELETE|name(s)|Remove multiple data|
|importByCSV|POST|name(s)/import|Create, Modified, Remove data by upload csv file.|

----

## Java Object Naming and Example
### Controller Class
```java
@RestController
@RequestMapping("/module/names")
public class NameController extends DefaultReadWriterController<Model, Primary> {

    @Autowired
    private ModelRepository repository;

    @Override
    protected DefaultRepository<Model, Primary> getRepository() {
        return this.repository;
    }
}
```

### Repository Class
```java
@Repository
@Transactional
public interface ModelRepository extends DefaultRepository<Model, Primary> {
}
```

### Model Class 
```java
import com.sdm.core.model.DefaultEntity;

public class Name extends DefaultEntity{
    ...
}
```

### Request Class
```java
public class NameRequest {
    ...
}
```

### Response Class
```java
public class NameResponse{
    ...
}
```
### Service Class
```java
@Service
public class NameService{
    ...
}
```
----
## Naming Notes
- Model without any suffix
- Model combined (Entity, Request, Response)
- Entity Name =>  module.EntityName
- Table Name => tbl_module_name(s)

### DATABASE Naming
- Table => tbl_{name_with_snake_case}
- View => vw_{name_with_snake_case}
- Procedure => proc_{name_with_snake_case}
- User Columns => {nameWithCamelCase}	



