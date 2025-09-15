package com.bookfinder.app

import com.bookfinder.app.data.remote.BookApiInterface
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BookApiInterfaceTest {
    private lateinit var server: MockWebServer
    private lateinit var api: BookApiInterface

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BookApiInterface::class.java)
    }

    @After
    fun tearDown() { server.shutdown() }

    @Test
    fun search_parses_response() = runBlocking {
        val body = """
            {"docs":[{"title":"The Great Gatsby","author_name":["F. Scott Fitzgerald"],"cover_i":8739161,"key":"/works/OL468516W","first_publish_year":1925,"isbn":["9780743273565"]}],"numFound":1000}
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))
        val res = api.searchBooks("gatsby", 20, 1)
        assertThat(res.docs.first().title).isEqualTo("The Great Gatsby")
        assertThat(res.numFound).isEqualTo(1000)
    }
}

