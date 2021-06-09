package com.udacity.project4.locationreminders.reminderslist

import android.provider.CalendarContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource
    private var remindersList = mutableListOf<ReminderDTO>()

    @Before
    fun setupViewModel() {
        stopKoin()
        val reminder1 = ReminderDTO("Googleplex", "Check it out", "Googleplex", 37.4221, -122.0841)
        val reminder2 = ReminderDTO("Coffee", "Get a latte", "Starbucks", 37.4158, -122.0776)
        val reminder3 = ReminderDTO("Donuts", "Pick up a dozen", "Krispy Kreme", 37.4193, -122.0943)
        remindersList.addAll(arrayListOf(reminder1, reminder2, reminder3))
        dataSource = FakeDataSource(remindersList)
        remindersListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }

    @Test
    fun loadReminders_threeRemindersInList_remindersListIsCorrect() = runBlockingTest {
        // when loading the reminders
        remindersListViewModel.loadReminders()

        // then reminders from the list should equal reminders loaded
        val value = remindersListViewModel.remindersList.getOrAwaitValue()
        assertEquals(remindersList.size, value.size)
        assertEquals(remindersList[0].title, value[0].title)
        assertEquals(remindersList[0].description, value[0].description)
        assertEquals(remindersList[0].location, value[0].location)
        assertEquals(remindersList[0].latitude, value[0].latitude)
        assertEquals(remindersList[0].longitude, value[0].longitude)
        assertEquals(remindersList[0].id, value[0].id)
        assertEquals(remindersList[1].title, value[1].title)
        assertEquals(remindersList[1].description, value[1].description)
        assertEquals(remindersList[1].location, value[1].location)
        assertEquals(remindersList[1].latitude, value[1].latitude)
        assertEquals(remindersList[1].longitude, value[1].longitude)
        assertEquals(remindersList[1].id, value[1].id)
        assertEquals(remindersList[2].title, value[2].title)
        assertEquals(remindersList[2].description, value[2].description)
        assertEquals(remindersList[2].location, value[2].location)
        assertEquals(remindersList[2].latitude, value[2].latitude)
        assertEquals(remindersList[2].longitude, value[2].longitude)
        assertEquals(remindersList[2].id, value[2].id)
    }

    @Test
    fun loadReminders_emptyList_remindersListIsCorrect() = runBlockingTest {
        // Given empty data source
        dataSource.deleteAllReminders()
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        // when loading the reminders
        remindersListViewModel.loadReminders()
        // then reminders loaded should be empty
        assertEquals(0, remindersListViewModel.remindersList.value?.size)

    }

    @Test
    fun loadReminders_nullList_errorReturnedAndSnackbarShown() = runBlockingTest {
        // Given a null data source
        dataSource = FakeDataSource(null)
        remindersListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), dataSource)
        // when loading the reminders
        remindersListViewModel.loadReminders()
        // then error should be returned and snackbar shown with not found message
        val error = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assert(error.contains("Reminders not found"))
    }
}