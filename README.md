# Scalatra Rest

scalatra-rest its a simple library with useful tools for develop rest apis with Scalatra.

### Repository

Stable [OSS Sonatype](https://oss.sonatype.org/content/repositories/releases/com/github/jarlakxen/)

    "com.github.jarlakxen" %% "scalatra-rest" % "1.4"

### Changelog

1.4
- Fix type checking
- Improve pagination trait

1.3
- Fix build dependancies

1.2
- Fix bug of JsonViewSupport when the response is an ActionResult

1.1
- Fix scala release version
- Fix bug of JsonViewSupport with FutureSupport

1.0
- Add QueryableSupport trait
- Add QueryableViewSupport trait
- Add CacheControlSupport trait
- Add JsonViewSupport trait


## PaginationSupport

This trait provide useful methods for handling pagination requests. 

Example:


    class UserServlet extends ScalatraServlet with PaginationSupport {

        ...

        get("/"){
            var page = pagination
            ...
        }

        ...

    }

The method pagination returns a class with this format: `Pagination( offset : Option[Int], limit : Option[Int], sortBy : Map[String, Int] )`. The sorting order value can be 1 or -1.

The trait provide another method:

    class UserServlet extends ScalatraServlet with PaginationSupport {

        ...

        get("/"){
            paginate(List(1,2,3,4), 20)
        }

        ...

    }

The method paginate ( `def paginate( resultFunction : => Traversable[T], totalAmountFunction : => Long )` ) recive de list of values of the requested page and the total number of values. This total number of values is setted in a header in the response with the name "total_amount". You can manual set this header using the method `def totalAmount_=( value : Long )`

## QueryableSupport

This is a simple trait that adds the capability to extract and convert the query parameters based on every case class fields.

Example:

    case class User(id: ObjectId, name: String, password: String, enabled: Boolean)

    class UserServlet extends ScalatraServlet with QueryableSupport {

        ...

        get("/"){

            println paramsOf[User]

        }

        ...

    }


If we try to access to the User resource specifying some field to query:

localhost:8080/resources/user/?name=Admin&enabled=true

In this case the `paramsOf[User]` returns:

    Map("name" -> "Admin", "enabled" -> true)

This is very useful when you work with some DB frameworks like [Salat](https://github.com/novus/salat/):

    val dao = new SalatDAO[User, ObjectId]( ... ) {}

    get("/"){

        dao.find( MongoDBObject( paramsOf[User].toList ) )

    }

> Note: The trait only supports the conversion of primitive types ( Int, Long, Dloat, Double and Boolean ). If the target case class contains a complex type like Set, Maps or Optional, the type of this fields would remain as String.

#### @NotQueryable

This annotation can be used to mark some fields as not queryable:

    case class User(id: ObjectId, name: String, @NotQueryable password: String, enabled: Boolean)

for localhost:8080/resources/user/?password=12345 the `paramsOf[User]` returns:

    Map()

This logic can be avoided by using `paramsOf[User]( ignoreNotQueryable = true )`

## QueryableViewSupport

This trait adds the capability to filter the fields of the output json. For this, the trait looks for a "fields" query parameters that specifies witch fields of the json object that must be returned.

Example:

    import com.github.jarlakxen.scalatra.rest.queryable.jackson.QueryableViewSupport
    
    case class User( id : String, name : String, password : String )

    class UserServlet extends ScalatraServlet with JacksonJsonSupport with QueryableViewSupport {

        ...

        get("/"){
            contentType = formats( "json" )
            User( "1", "test", "1233456" )
        }

        ...

    }

for localhost:8080/resources/user/?fields=id,name this will return {id:1, name:test}

## CacheControlSupport

This trait adds the capability to easy add the cache control headers to the response. The traits supports:

    get("/"){
        cacheControl = NoCache
        ...
    }

    import com.github.nscala_time.time.Imports._
    get("/"){
        cacheControl = MaxAge(20.minutes)
        ...
    }

    import com.github.nscala_time.time.Imports._
    get("/"){
        cacheControl = Expires(DateTime.now + 2.hours)
        ...
    }

    import com.github.nscala_time.time.Imports._
    get("/"){
        .....
        Ok( value, MaxAge(20.minutes))
    }


## JsonViewSupport

This trait adds the capability to to filter the output json based on validations rules. This rules are written with a DSL style, and take as parameters the instance of the output object and the current user. This is useful when you need to hide fields or object depending of the privileges of the user.

Example:

    case class User(id: ObjectId, name: String, password: String, enabled: Boolean)

    class UserServlet extends ScalatraServlet with extends ScalatraServlet with ScentrySupport[User] with JacksonJsonSupport with jackson.JsonViewSupport[User, User]  {

        ...

        override val definition : ViewModule[Post, User] => Unit = { definition =>
            import definition._;

            moduleOf[Post]  // This is really important

          `object` notIf { implicit params => isNotLogged && !target.enabled }
          "password" onlyIf { implicit params => user.id == target.id }
        }

        get("/"){
            contentType = formats( "json" )
            List(User("1", "Test", "123456789", false))
        }

    }

> Note: The JsonViewSupport trait depends on having the ScentrySupport trait

In this example when you acceso the localhost:8080/resources/user/ you can get:

+ If you are loggedin ( the user method of ScentrySupport return an instance ) and you are the user "Test" you get: [{id:"1", name:"Test", password:"123456789", enabled: false}] 

+ If you are loggedin and you are not the user "Test" you get: [{id:"1", name:"Test", enabled: false}] 

+ If you are not loggedin you get: [] 


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/Jarlakxen/scalatra-rest/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

