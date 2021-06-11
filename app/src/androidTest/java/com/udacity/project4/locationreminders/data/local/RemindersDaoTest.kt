package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841)
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id from the database.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - The loaded data contains the expected values.
        assertThat<ReminderDTO>(loaded as ReminderDTO, Matchers.notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun updateReminderAndGetById() = runBlockingTest {
        // 1. Insert a reminder into the DAO.
        val reminder = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841)
        database.reminderDao().saveReminder(reminder)
        // 2. Update the reminder by creating a new reminder with the same ID but different attributes.
        val updateReminder = ReminderDTO("Google", "World Headquarters", "Google", 37.42, -122.08, reminder.id)
        database.reminderDao().saveReminder(updateReminder)
        // 3. Check that when you get the task by its ID, it has the updated values.
        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, Matchers.notNullValue())
        assertThat(loaded.id, `is`(updateReminder.id))
        assertThat(loaded.title, `is`(updateReminder.title))
        assertThat(loaded.description, `is`(updateReminder.description))
        assertThat(loaded.location, `is`(updateReminder.location))
        assertThat(loaded.latitude, `is`(updateReminder.latitude))
        assertThat(loaded.longitude, `is`(updateReminder.longitude))
    }

    @Test
    fun insertRemindersAndGetReminders() = runBlockingTest {
        // GIVEN - Insert 2 reminders.
        val reminder1 = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841)
        database.reminderDao().saveReminder(reminder1)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get all the reminders from the database.
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values.
        assertThat<List<ReminderDTO>>(loaded, Matchers.notNullValue())
        loaded.forEach { 
            when (it.id) {
                reminder1.id -> {
                    assertThat(it.id, `is`(reminder1.id))
                    assertThat(it.title, `is`(reminder1.title))
                    assertThat(it.description, `is`(reminder1.description))
                    assertThat(it.location, `is`(reminder1.location))
                    assertThat(it.latitude, `is`(reminder1.latitude))
                    assertThat(it.longitude, `is`(reminder1.longitude))
                }
                reminder2.id -> {
                    assertThat(it.id, `is`(reminder2.id))
                    assertThat(it.title, `is`(reminder2.title))
                    assertThat(it.description, `is`(reminder2.description))
                    assertThat(it.location, `is`(reminder2.location))
                    assertThat(it.latitude, `is`(reminder2.latitude))
                    assertThat(it.longitude, `is`(reminder2.longitude))
                }
                else -> {
                    assertEquals(null, it)  //This should never be reached, but results in a failed test if it does
                }
            }
        }
        
    }

    @Test
    fun insertRemindersAndDeleteAllReminders() = runBlockingTest {
        // GIVEN - Insert 2 reminders.
        val reminder1 = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841)
        database.reminderDao().saveReminder(reminder1)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Delete all the reminders from the database.  Then try to get reminders.
        database.reminderDao().deleteAllReminders()
        val loaded = database.reminderDao().getReminders()

        // THEN - The loaded data contains the expected values (an empty list).
        assertThat<List<ReminderDTO>>(loaded, Matchers.notNullValue())
        assertEquals(0, loaded.size)

    }

    @Test
    fun tryToFindReminderNotInDB() = runBlockingTest {
        // GIVEN - Insert a reminder.
        val reminder = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841, "0")
        database.reminderDao().saveReminder(reminder)

        // WHEN - Then try to get a different reminder.
        val result = database.reminderDao().getReminderById("1")

        // THEN - The result contains the expected value (null).
        assertThat<ReminderDTO>(result, Matchers.nullValue())

    }


}