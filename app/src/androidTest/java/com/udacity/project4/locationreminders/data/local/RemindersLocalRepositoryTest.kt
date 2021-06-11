package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersLocalRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(
                database.reminderDao(),
                Dispatchers.Main
            )
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun saveReminders_getReminders() = runBlocking {
        // GIVEN - Two new reminders are saved in the database.
        val reminder1 = ReminderDTO("Google HQ", "Check it out", "Googleplex", 37.4221, -122.0841)
        remindersLocalRepository.saveReminder(reminder1)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        remindersLocalRepository.saveReminder(reminder2)

        // WHEN  - All reminders retrieved.
        val result = remindersLocalRepository.getReminders()

        // THEN - Both reminders are returned in the list.
        assertThat(result.succeeded, Matchers.`is`(true))
        result as Result.Success
        result.data.forEach {
            when (it.id) {
                reminder1.id -> {
                    assertThat(it.title, Matchers.`is`("Google HQ"))
                    assertThat(it.description, Matchers.`is`("Check it out"))
                    assertThat(it.location, Matchers.`is`("Googleplex"))
                    assertThat(it.latitude, Matchers.`is`(37.4221))
                    assertThat(it.longitude, Matchers.`is`(-122.0841))
                    assertThat(it.id, Matchers.`is`(reminder1.id))
                }
                reminder2.id -> {
                    assertThat(it.title, Matchers.`is`("Coffee"))
                    assertThat(it.description, Matchers.`is`("Get a latte"))
                    assertThat(it.location, Matchers.`is`("Starbucks"))
                    assertThat(it.latitude, Matchers.`is`(37.4158))
                    assertThat(it.longitude, Matchers.`is`(-122.0776))
                    assertThat(it.id, Matchers.`is`(reminder2.id))
                }
                else -> {
                    assertEquals(null, it.id)  // this should never be reached, so fails if reached
                }
            }
        }

    }

    @Test
    fun saveReminder_retrievesReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("Google HQ", "Check it out", "Googleplex", 37.4221, -122.0841)
        remindersLocalRepository.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = remindersLocalRepository.getReminder(reminder.id)

        // THEN - Same reminder is returned.
        assertThat(result.succeeded, Matchers.`is`(true))
        result as Result.Success
        assertThat(result.data.title, Matchers.`is`("Google HQ"))
        assertThat(result.data.description, Matchers.`is`("Check it out"))
        assertThat(result.data.location, Matchers.`is`("Googleplex"))
        assertThat(result.data.latitude, Matchers.`is`(37.4221))
        assertThat(result.data.longitude, Matchers.`is`(-122.0841))
        assertThat(result.data.id, Matchers.`is`(reminder.id))
    }

    @Test
    fun saveReminders_deleteAllReminders_getReminders() = runBlocking {
        // GIVEN - Two new reminders are saved in the database.
        val reminder1 = ReminderDTO("Google HQ", "Check it out", "Googleplex", 37.4221, -122.0841)
        remindersLocalRepository.saveReminder(reminder1)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        remindersLocalRepository.saveReminder(reminder2)

        // WHEN  - All reminders deleted and then retrieved.
        remindersLocalRepository.deleteAllReminders()
        val result = remindersLocalRepository.getReminders()

        // THEN - An empty reminders list is returned.
        assertThat(result.succeeded, Matchers.`is`(true))
        result as Result.Success
        assertEquals(0, result.data.size)  // list of reminders is empty
    }


    @Test
    fun saveReminder_retrieveAReminderNotInDB_resultsInError() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDTO("Google HQ", "Check it out", "Googleplex", 37.4221, -122.0841, "0")
        remindersLocalRepository.saveReminder(reminder)

        // WHEN  - Reminder retrieved by ID.
        val result = remindersLocalRepository.getReminder("1")

        // THEN - An error message is returned.
        assertThat(result.succeeded, Matchers.`is`(false))
        result as Result.Error
        assertThat(result.message, Matchers.`is`("Reminder not found!"))

    }

}