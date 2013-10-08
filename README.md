# Scalatra Rest

scalatra-rest its a simple library with useful tools for develop rest apis with Scalatra.


## QueryableSupport

This is a simple trait that add the capability to extract and convert the query parameters based on an case class fields.

Example:

    case class User(id: ObjectId, name: String, password: String, enabled: Boolean)

    class UserServlet ScalatraServlet with QueryableSupport {

        ...

        get("/"){

            println paramsOf[User]

        }

        ...

    }


If we try to access to the User resource specifying some field to query:

localhost:8080/resources/user/?name=Admin&enabled=true

In this case the paramsOf[User] would return

Map("name" -> "Admin", "enabled" -> true)

This is very useful when you work with some DB frameworks like [Salat](https://github.com/novus/salat/):

    val dao = new SalatDAO[User, ObjectId]( ... ) {}

    get("/"){

        dao.find( MongoDBObject( paramsOf[User].toList ) )

    }

