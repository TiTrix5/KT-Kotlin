package ru.example

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Serializable
data class Task(
    val id: Int,
    val title: String,
    val description: String = "",
    val completed: Boolean = false
)

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String = "",
    val completed: Boolean = false
)

@Serializable
data class ApiError(val error: String)

private val tasks = ConcurrentHashMap<Int, Task>()
private val nextId = AtomicInteger(1)

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            ignoreUnknownKeys = false
            isLenient = false
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // Invalid request bodies are handled here as a client error;
            // unexpected exceptions are returned as a generic server error.
            if (cause is IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ApiError(cause.message ?: "Некорректный запрос"))
            } else {
                environment.log.error("Unhandled server error", cause)
                call.respond(HttpStatusCode.InternalServerError, ApiError("Внутренняя ошибка сервера"))
            }
        }
    }

    routing {
        get("/") {
            call.respond(
                mapOf(
                    "name" to "Ktor To-Do API",
                    "status" to "ok",
                    "endpoints" to listOf(
                        "GET /tasks",
                        "GET /tasks/{id}",
                        "POST /tasks",
                        "DELETE /tasks/{id}"
                    )
                )
            )
        }

        route("/tasks") {
            get {
                val completedParam = call.request.queryParameters["completed"]
                val search = call.request.queryParameters["search"]?.trim()?.lowercase()

                if (completedParam != null && completedParam != "true" && completedParam != "false") {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ApiError("Параметр 'completed' должен быть true или false")
                    )
                    return@get
                }

                val result = tasks.values
                    .asSequence()
                    .filter { completedParam == null || it.completed == (completedParam == "true") }
                    .filter { search.isNullOrBlank() ||
                        it.title.lowercase().contains(search) ||
                        it.description.lowercase().contains(search)
                    }
                    .sortedBy { it.id }
                    .toList()

                call.respond(HttpStatusCode.OK, result)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null || id <= 0) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("ID должен быть положительным целым числом"))
                    return@get
                }

                val task = tasks[id]
                if (task == null) {
                    call.respond(HttpStatusCode.NotFound, ApiError("Задача с ID $id не найдена"))
                } else {
                    call.respond(HttpStatusCode.OK, task)
                }
            }

            post {
                val request = try {
                    call.receive<CreateTaskRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Некорректное JSON-тело запроса"))
                    return@post
                }

                val title = request.title.trim()
                if (title.isEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("Поле 'title' обязательно и не должно быть пустым"))
                    return@post
                }

                val id = nextId.getAndIncrement()
                val task = Task(
                    id = id,
                    title = title,
                    description = request.description.trim(),
                    completed = request.completed
                )
                tasks[id] = task
                call.respond(HttpStatusCode.Created, task)
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null || id <= 0) {
                    call.respond(HttpStatusCode.BadRequest, ApiError("ID должен быть положительным целым числом"))
                    return@delete
                }

                val removed = tasks.remove(id)
                if (removed == null) {
                    call.respond(HttpStatusCode.NotFound, ApiError("Задача с ID $id не найдена"))
                } else {
                    call.respond(HttpStatusCode.OK, removed)
                }
            }
        }
    }
}
