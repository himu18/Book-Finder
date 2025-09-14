package com.bookfinder.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bookfinder.app.data.local.AppDatabase
import com.bookfinder.app.data.local.entity.BookEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class RoomTest {
    private lateinit var db: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun upsert_and_query_saved_books() = runBlocking {
        val entity = BookEntity(
            id = "/works/OL1W",
            title = "T",
            author = "A",
            cover_url = null,
            publish_year = 2020,
            description = "D",
            is_saved = true,
        )
        db.bookDao().upsert(entity)
        val list = db.bookDao().observeAll().first()
        assertThat(list).hasSize(1)
        assertThat(list[0].title).isEqualTo("T")
    }
}


