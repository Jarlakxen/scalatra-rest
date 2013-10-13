# Scalatra Rest

scalatra-rest its a simple library with useful tools for develop rest apis with Scalatra.


## QueryableSupport

This is a simple trait that add the capability to extract and convert the query parameters based on an case class fields.

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

#### @NotQueryable

This annotation can be used to make some field not queryable:

    case class User(id: ObjectId, name: String, @NotQueryable password: String, enabled: Boolean)

for localhost:8080/resources/user/?password=12345 the `paramsOf[User]` returns:

    Map()

This logic can be avoid by using `paramsOf[User]( ignoreNotQueryable = true )`

## QueryableViewSupport

This trait add the capability to filter the fields of the output json. For this, the trait looks for a "fields" query parameters that specifies witch fields of the json object that must be returned.

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

This trait that add the capability to easy add the cache control headers to the response. The traits supports:

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