# MasterAPI

This RESTful framework was developed by following:

## <a name="getting_started"></a>Getting started

- Clone or download the project from [github](https://github.com/SUNDEWMYANMAR/master-api)
- Open project in Java IDE such as Intellij, Eclipse, NetBeans, etc ...
- Clone following setting files:
  - [example.log4j2.xml](./admin/src/main/resources/example.log4j2.xml) => __log4j2.xml__
- Create system directories:
    - File upload directory. (Example: /var/www/master-api/upload/)
    - Report directory. (Example: /var/www/master-api/reports/)
    - Log directory. (Example: /var/www/master-api/log/)
- Edit require properties from **log4j2.xml** file.

### Require config to edit in **log4j2.xml**

Modified output directory.

```xml

<Property name="LOG_ROOT">{log directory path}/</Property>
```

Run Spring Boot Application

```bash
> ./gradlew clean bootRun
```

### Setup System

Open Browser and Enter this URL [http://locahost:8080/setup](http://localhost:8080/setup)

- Fill configuration fields and Submit
- Copy generated configuration codes and save as
  a [application.properties](./admin/src/resources/application.properties) file.
- Reload Server again
- !Finished

### Ready for your API system now!

```bash
> ./gradlew clean bootRun
```

Ready Output log:

```bash
2020-01-28 13:41:44.597  INFO 2727 --- [main] com.sdm.Application   : System is running...
```

[Ref: Spring Boot application.properties](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html)

### Generate war file to upload web server!

```bash
> ./gradlew clean bootWar
```

___

## Version 1.8.0

- Multi Module Project
- Apple Authentication

## Version 1.7.1

- i18n Messaging

## Version 1.7

- Telenor SMS Service

## Version 1.6

- Payment Modules

## Version 1.5.1
- Data Auditor
- Setup Form to generate application.properties

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

- [System Configurations](./core/src/main/java/com/sdm/core/config)
- [Base Controller & Helper](./core/src/main/java/com/sdm/core/controller)
- [Database Helper](./core/src/main/java/com/sdm/core/db)
- [Exception Handling](./core/src/main/java/com/sdm/core/exception)
- [Base Models](./core/src/main/java/com/sdm/core/model)
- [Utils & Plugins](./core/src/main/java/com/sdm/core/util)

### [System Authorization and Authentication](./src/main/java/com/sdm/auth)

- System auth by user:password
- System auth by Facebook
- System auth by Google
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
- Image Caching & Resizing

### [Facebook Messenger Bot](./src/main/java/com/sdm/facebook)

- Messenger Bot Activate
- Messenger Bot Listener
- Message Log

### [Notification](./src/main/java/com/sdm/notification)

- [Firebase Messaging](https://firebase.google.com/docs/cloud-messaging/server)

### [Payment Module](./src/main/java/com/sdm/payment)

- AGD Payment (OnePay)
- CB Payment (CBPay)
- UAB Payment (Sai Sai Pay)
- YOMA Payment (Wave)
- KBZ Payment (KPay)
- MPU Payment

### [SMS Module]('./src/main/java/com/sdm/sms)

- Telenor SMS Messaging

----

## Default RestFUL Controller

### ReadController

|method|http-method|url/{path}?{query_params}|description|
|------|------|------|------|
|getPagingByFilter|GET|name(s)?{size, pageSize, page, sort, filter}|Get data by pagination and Global Filter|
|getPagingByAdvancedFilter|POST|name(s)/advanced{?page,size,sort}|Get Data by pagination and Advanced Filter|
|getAll|GET|name(s)/all|Get all data|
|getById|GET|name(s)/{id}|Get data by unique id(PK)|
|getStructure|GET|name(s)/struct|Get UI Structure|

### WriteController

|method|http-method|url/{path}?{query_params}|description|
|------|------|------|------|
|create|POST|name(s)|Create new data
|update|PUT|name(s)/{id}|Modified data by unique id(PK)|
|partialUpdate|PATCH|name(s)/{id}|Partially modified data by unique id(PK)|
|remove|DELETE|name(s)/{id}|Remove data by unique id(PK)|
|multiRemove|DELETE|name(s)|Remove multiple data|
|import|POST|name(s)/import|Create, Modified data by data list.|

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

public class Name extends DefaultEntity {
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
public class NameResponse {
    ...
}
```

### Service Class

```java

@Service
public class NameService {
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

## Reference Documentation

For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
* [Spring Web](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-developing-web-applications)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-jpa-and-spring-data)
* [Spring Security](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-security)
* [Java Mail Sender](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-email)
* [Quartz Scheduler](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.quartz)
* [Hibernate](https://hibernate.org/)
* [Thymeleaf Templating Engine](https://www.thymeleaf.org/)
* [Jasper Reporting](https://www.jaspersoft.com/)
* [Swagger Open API](https://swagger.io/specification/)
* [Lombok](https://projectlombok.org/)
* [JWT](https://jwt.io/)

