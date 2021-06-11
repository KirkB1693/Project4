package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
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
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(), dataSource)
    }


    @Test
    fun clearData_dataSourceIsEmpty() {
        // when data is cleared
        saveReminderViewModel.onClear()

        // then the values should all be null
        assertEquals(null, saveReminderViewModel.reminderTitle.value)
        assertEquals(null, saveReminderViewModel.reminderDescription.value)
        assertEquals(null, saveReminderViewModel.reminderSelectedLocationStr.value)
        assertEquals(null, saveReminderViewModel.latitude.value)
        assertEquals(null, saveReminderViewModel.longitude.value)
        assertEquals(null, saveReminderViewModel.selectedPOI.value)
    }

    @Test
    fun validateAndSaveReminder_validReminder_reminderAddedToDataSource() = runBlockingTest {
        // when valid reminder is added
        val reminderToAdd = ReminderDataItem(
            "Golfing",
            "Hit a dozen practice balls on the driving range",
            "Shoreline Golf Links",
            37.4306,
            -122.0855
        )

        saveReminderViewModel.validateAndSaveReminder(reminderToAdd)

        // then reminder should now be part of data and the saved toast should show
        val result = saveReminderViewModel.dataSource.getReminder(reminderToAdd.id)
        val data: ReminderDTO? = when (result) {
            is Success<*> -> {
                result.data as ReminderDTO
            }
            else -> null
        }
        val toast = saveReminderViewModel.showToast.getOrAwaitValue()
        assertNotEquals(null, data)
        assertEquals(reminderToAdd.title, data?.title)
        assertEquals(reminderToAdd.description, data?.description)
        assertEquals(reminderToAdd.location, data?.location)
        assertEquals(reminderToAdd.latitude, data?.latitude)
        assertEquals(reminderToAdd.longitude, data?.longitude)
        assertEquals(reminderToAdd.id, data?.id)
        assert(toast.contains("Reminder Saved"))

    }

    @Test
    fun validateAndSaveReminder_invalidReminderNoTitle_reminderNotAddedToDataSourceAndSnackBarShows() = runBlockingTest {
        // when invalid reminder is added
        val reminderToAdd = ReminderDataItem(
            "",
            "Hit a dozen practice balls on the driving range",
            "Shoreline Golf Links",
            37.4306,
            -122.0855
        )

        saveReminderViewModel.validateAndSaveReminder(reminderToAdd)

        // then reminder should not be part of data and the snackbar should show to prompt user for valid input
        val result = saveReminderViewModel.dataSource.getReminder(reminderToAdd.id)
        val error: Boolean = when (result) {
            is Error -> {
                true
            }
            else -> false
        }
        val snackbarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertEquals(true, error)
        assertEquals(R.string.err_enter_title, snackbarInt)
    }

    @Test
    fun validateAndSaveReminder_invalidReminderNoLocation_reminderNotAddedToDataSourceAndSnackBarShows() = runBlockingTest {
        // when invalid reminder is added
        val reminderToAdd = ReminderDataItem(
            "Shoreline Golf Links",
            "Hit a dozen practice balls on the driving range",
            "",
            0.0,
            0.0
        )

        saveReminderViewModel.validateAndSaveReminder(reminderToAdd)

        // then reminder should not be part of data and the snackbar should show to prompt user for valid input
        val result = saveReminderViewModel.dataSource.getReminder(reminderToAdd.id)
        val error: Boolean = when (result) {
            is Error -> {
                true
            }
            else -> false
        }
        val snackbarInt = saveReminderViewModel.showSnackBarInt.getOrAwaitValue()
        assertEquals(true, error)
        assertEquals(R.string.err_select_location, snackbarInt)
    }

    @Test
    fun setPOI_validPOI_valuesAreSet() {
        // when a valid POI is passed in
        val poi = PointOfInterest(LatLng(37.4306, -122.0855), "", "Shoreline Golf Links")
        // then the latitude, longitude and name values should be set in the viewmodel
        saveReminderViewModel.setPoi(poi)
        assertEquals(poi.name, saveReminderViewModel.reminderSelectedLocationStr.value)
        assertEquals(poi.latLng.latitude, saveReminderViewModel.latitude.value)
        assertEquals(poi.latLng.longitude, saveReminderViewModel.longitude.value)
    }

    @Test
    fun setPOI_invalidPOI_valuesAreNotSet() {
        // when an invalid POI is passed in
        val poi = null
        // then the latitude, longitude and name values should not be set in the viewmodel
        saveReminderViewModel.setPoi(poi)
        assertEquals(null, saveReminderViewModel.reminderSelectedLocationStr.value)
        assertEquals(null, saveReminderViewModel.latitude.value)
        assertEquals(null, saveReminderViewModel.longitude.value)
    }

    @Test
    fun saveReminder_checkLoading() = runBlockingTest {
        val reminderToAdd = ReminderDataItem(
            "Golfing",
            "Hit a dozen practice balls on the driving range",
            "Shoreline Golf Links",
            37.4306,
            -122.0855
        )
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()
        // when loading the reminders
        saveReminderViewModel.saveReminder(reminderToAdd)
        // Then assert that the progress indicator is shown.
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(true)
        )
        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()
        // Then assert that the progress indicator is hidden.
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            Matchers.`is`(false)
        )
    }
}