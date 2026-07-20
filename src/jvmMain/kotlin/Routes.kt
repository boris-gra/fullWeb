import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
//import javax.script.ScriptEngineManager

var message = ""
var loadTestingKeys = ""

// CSP заголовок для защиты от XSS атак
private val contentSecurityPolicy = listOf(
    "default-src 'self'",
    "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
    "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
    "font-src 'self' https://fonts.gstatic.com",
    "img-src 'self' data: https:",
    "connect-src 'self' *",
    "frame-ancestors 'none'",
    "base-uri 'self'",
    "form-action 'self'"
).joinToString("; ")

fun Route.getAndPost() {
    //https://ktor.io/docs/routing.html#parameters
    post(JSR223) {
        rowFieldsValueMap = call.receive()
        call.respond(
            if ((System.getenv(JSR223) ?: HIDE) != HIDE)
                runScript(rowFieldsValueMap["src"] ?: "")
            else ""
        )
    }
    post(LOADENV) {
        call.respond(startEnv)
    }
    get("/") {
        call.respondText(
            (this::class.java.classLoader.getResource("index.html")?.readText() ?: "")
                .replace("<!--META_SECURITY-->", if ((System.getenv("SERVER_TYPE") ?: SERVER_TYPE) == SERVER_TYPE) META_SECURITY else ""),
            ContentType.Text.Html,
            // Добавляем CSP заголовок для защиты от XSS
            buildHeaders {
                append(HttpHeaders.ContentSecurityPolicy, contentSecurityPolicy)
                append("X-Content-Type-Options", "nosniff")
                append("X-Frame-Options", "DENY")
                append("X-XSS-Protection", "1; mode=block")
                append("Referrer-Policy", "strict-origin-when-cross-origin")
            }
        )
    }
    get("/com/{command}") {
        call.parameters["command"]
            ?.let { command ->
                // Валидация команды для предотвращения инъекций OS команд
                if (!command.matches(Regex("^[a-zA-Z0-9_\\-\\.]+$"))) {
                    call.respondText("Invalid command format", HttpStatusCode.BadRequest)
                    return@let
                }
                println(command)
                val result =
                    Runtime.getRuntime() // https://stackoverflow.com/questions/70035178/kotlin-script-to-run-os-command-and-parse-output
                        .exec(command.split(" ").toTypedArray())
                        .inputStream
                        .readAllBytes()
                call.respond(result)
            }
    }
    get("/{key}/") {
        call.parameters["key"]
            ?.let {
                if (loadTestingKeys.contains(it))
                    call.respondText(it)
            }
    }
    route(ROUTE_BD) {
        post("/$PASSW") {
            call.respond(
                call.receiveText() == (System.getenv(
                    ADMIN_PASSW.uppercase().replace(" ", "_")
                ) ?: "adm")
            )
        }
        route("/{bd_name}") {
            post("/$JSON_PG_UPD/{pg_view?}/{where?}") {
                println("=====$JSON_PG_UPD==param======${call.parameters["pg_view"] ?: ""}===${call.parameters["where"] ?: ""}")
                val upd = call.receive<UpdateData>()
                call.respondText(
                        jsonPGupd(
                            setBD(), upd,
                            call.parameters["pg_view"] ?: ""
                        )
                )
            }
            get("/$JSON_PG/{pg_view}/{where?}") {
                (call.request.queryParameters[ROW_FIELDS_VALUE_PARAM]
                    ?.also{ println("GET /$JSON_PG =${call.parameters}") }
                    ?.let { Json.decodeFromString<Map<String, String>>(it) } ?: emptyMap())
                    .let {
                        call.respondText(
                            with(getView(it)) {
                                if (contains(ERROR_FIELD)
                                    && SQL_STATE_RETRY.split(",").any { contains(it) }
                                )
                                    getView(it) // try getPGConnection  after waiting
                                else this
                            })
                    }
            }
        }
    }
}

private fun RoutingContext.getView(fieldsValue: Map<String, String>) =
    jsonPGview(
        setBD(),
        call.parameters["pg_view"] ?: "",
        call.parameters["where"] ?: "",
        call.parameters["name"],
        fieldsValue    )

private fun RoutingContext.setBD() =
    call.parameters["bd_name"]
        .let { bd ->
            if (bd == QUERY_BD)
                queryBdName
            else bd
        } ?: ""

private fun runScript(src: String) =
//    https://github.com/Kotlin/kotlin-script-examples/blob/master/jvm/jsr223/jsr223-simple/src/test/kotlin/org/jetbrains/kotlin/script/examples/jvm/jsr223/simple/test/simpleJsr223Test.kt
//    https://www.tabnine.com/code/java/methods/javax.script.ScriptEngine/createBindings
    runCatching {
//        val engine = ScriptEngineManager().getEngineByExtension("kts")
//        engine.eval(src, engine.createBindings().apply {
//            rowFieldsValueMap.forEach { put(it.key, it.value) }
//        })
    }.onFailure { message = "ERROR in script -> ${it.message} " }
        .getOrNull() ?: message
