package ru.example

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {
    @Test
    fun rootEndpointReturnsOk() = testApplication {
        application { module() }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("Ktor To-Do API"))
    }

    @Test
    fun createAndReadTask() = testApplication {
        application { module() }

        val create = client.post("/tasks") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Изучить Ktor","description":"Сделать учебный проект"}""")
        }
        assertEquals(HttpStatusCode.Created, create.status)
        assertTrue(create.bodyAsText().contains("Изучить Ktor"))

        val list = client.get("/tasks")
        assertEquals(HttpStatusCode.OK, list.status)
        assertTrue(list.bodyAsText().contains("Изучить Ktor"))
    }

    @Test
    fun invalidCompletedQueryReturnsBadRequest() = testApplication {
        application { module() }
        val response = client.get("/tasks?completed=yes")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
