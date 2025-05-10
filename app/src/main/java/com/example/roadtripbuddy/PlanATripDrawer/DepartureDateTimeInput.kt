package com.example.roadtripbuddy.PlanATripDrawer

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.PlanATripViewModel
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.Date


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartureDateTimeInput(
    onValidTimeAndDate: (LocalDateTime) -> Unit, // when all inputs are valid this function occurs
    viewModel: PlanATripViewModel,
    onInvalidTimeAndDate: () -> Unit,
    isTyping: () -> Unit
) {
    var focus by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Sample data for dropdowns
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    val years = listOf(
        "2025", "2026", "2027", "2028"
    )
    val periods = listOf("AM", "PM")

    fun Date.toLocalDateTime(): LocalDateTime {
        return this.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    val dateAndTime by remember { mutableStateOf(viewModel.initialDeparture.value.toLocalDateTime()) }

    // States for dropdown expanded flags
    var monthExpanded by remember { mutableStateOf(false) }
    var periodExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    //Queries
    var monthQuery by remember { mutableStateOf(months[dateAndTime.monthValue - 1]) }
    var dayQuery by remember { mutableStateOf(dateAndTime.dayOfMonth.toString()) }
    var yearQuery by remember { mutableStateOf(dateAndTime.year.toString()) }
    var hourQuery by remember { mutableStateOf(
        // Convert the current hour to 12 hour format
        (if (dateAndTime.hour % 12 == 0) 12 else dateAndTime.hour % 12).toString()
    ) }
    var minuteQuery by remember { mutableStateOf(dateAndTime.minute.toString()) }
    var period by remember { mutableStateOf(
        // the default period accordingly
        if (dateAndTime.hour < 12) "AM" else "PM"
    ) }

    val validDateTime by remember {
        derivedStateOf {
            val hourInt = hourQuery.toIntOrNull()
            val dayInt = dayQuery.toIntOrNull()
            val yearInt = yearQuery.toIntOrNull()
            val minuteInt = minuteQuery.toIntOrNull()
            if (hourInt != null && hourInt in 1..12 &&
                dayInt != null && yearInt != null && minuteInt != null &&
                period.isNotEmpty()
            ) {
                val hour24 = convert12To24(hourInt, period)
                val monthInt = Month.valueOf(monthQuery.uppercase()).value
                tryConstructDateTime(
                    month = monthInt,
                    day = dayInt,
                    year = yearInt,
                    hour = hour24,
                    minute = minuteInt
                )
            } else {
                null
            }
        }
    }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        if (showError){
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
        // Row for the Date inputs Month, Day, Year
        Row(
            modifier = Modifier.width(300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Month Dropdown
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = monthQuery,
                    onValueChange = { },
                    readOnly = true,
                    isError = showError,
                    label = { Text("Month") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type= MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .requiredHeight(60.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused){
                                focus = false
                            } else if (focusState.isFocused){
                                focus = true
                            }
                        }
                )
                ExposedDropdownMenu(
                    expanded = monthExpanded,
                    onDismissRequest = { monthExpanded = false }
                ) {
                    months.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                monthQuery = selectionOption // change the text field
                                monthExpanded = false
                            }
                        )
                    }
                }
            }
            // Day Input
            OutlinedTextField(
                value = dayQuery,
                onValueChange = {
                    val digitsOnly = it.filter { char -> char.isDigit() }
                    if (digitsOnly.length <= 2) {
                        dayQuery = digitsOnly
                    }
                },
                label = { Text("Day") },
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(70.dp)
                    .requiredHeight(60.dp)
                    .onFocusChanged { focusState ->
                        if(focusState.isFocused){
                            isTyping()
                            focus = true
                        } else if (!focusState.isFocused){
                            focus = false
                        }
                    }
            )
            // Year Input
            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded},
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = yearQuery,
                    onValueChange = { },
                    readOnly = true,
                    isError = showError,
                    label = { Text("Year") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(type= MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .requiredHeight(60.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused){
                                focus = false
                            } else if (focusState.isFocused){
                                focus = true
                            }
                        }
                )
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded= false }
                ) {
                    years.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                yearQuery = selectionOption
                                yearExpanded = false
                            }
                        )
                    }
                }
            }
        }
        // Row for the Time inputs: Hour, Minute, AM/PM
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hour Input
            OutlinedTextField(
                value = hourQuery,
                onValueChange = {
                    val digitsOnly = it.filter { char -> char.isDigit() }
                    if (digitsOnly.length <= 2 && (digitsOnly.isEmpty() || digitsOnly.toInt() in 1..12)) {
                        hourQuery = digitsOnly
                    }
                },
                label = { Text("Hour") },
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(70.dp)
                    .requiredHeight(60.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            isTyping()
                            focus = true
                        } else {
                            focus = false
                        }
                    }
            )

// Minute Input
            OutlinedTextField(
                value = minuteQuery,
                onValueChange = {
                    val digitsOnly = it.filter { char -> char.isDigit() }
                    if (digitsOnly.length <= 2 && (digitsOnly.isEmpty() || digitsOnly.toInt() in 0..59)) {
                        minuteQuery = digitsOnly
                    }
                },
                label = { Text("Min") },
                singleLine = true,
                isError = showError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .width(70.dp)
                    .requiredHeight(60.dp)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            isTyping()
                            focus = true
                        } else {
                            focus = false
                        }
                    }
            )

            // AM/PM Dropdown
            ExposedDropdownMenuBox(
                expanded = periodExpanded,
                onExpandedChange = { periodExpanded = !periodExpanded },
                modifier = Modifier.width(100.dp)
            ) {
                OutlinedTextField(
                    value = period,
                    onValueChange = { },
                    readOnly = true,
                    isError = showError,
                    label = { Text("AM/PM") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = periodExpanded) },
                    modifier = Modifier
                        .width(100.dp)
                        .menuAnchor(type= MenuAnchorType.PrimaryNotEditable, enabled = true)
                        .height(60.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused){
                                focus = false
                            } else if (focusState.isFocused){
                                focus = true
                            }
                        }
                )
                ExposedDropdownMenu(
                    expanded = periodExpanded,
                    onDismissRequest = { periodExpanded = false },
                ) {
                    periods.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                period = selectionOption
                                periodExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(focus) {
        if (!focus) {
            if (validDateTime == null) {
                errorMessage = "Invalid date and time"
                showError = true
                onInvalidTimeAndDate()
                return@LaunchedEffect
            }

            validDateTime?.let { newDateTime ->
                if (newDateTime.isBefore(LocalDateTime.now())) {
                    errorMessage = "The date and time you entered is already in the past"
                    showError = true
                    onInvalidTimeAndDate()
                } else {
                    showError = false
                    errorMessage = ""
                    onValidTimeAndDate(newDateTime)
                }
            }
        }
    }

}
// Function used here to convert a 12 hour time clock to 24 hour time clock, this is because LocalTimeDate
// only accepts a 24 hour clock
fun convert12To24(hour12: Int, period: String): Int {
    return if (period.uppercase() == "PM") {
        if (hour12 < 12) hour12 + 12 else hour12
    } else { // AM
        if (hour12 == 12) 0 else hour12
    }
}
// A checker of sorts, intakes all parameters needed for LocalDateTime, if its invalid returns null
// this is used in validDateTime
fun tryConstructDateTime(
    month: Int, day: Int, year: Int, hour: Int, minute: Int
): LocalDateTime? {
    return try {
        LocalDateTime.of(year, month, day, hour, minute)
    } catch (e: DateTimeException) {
        null
    }
}
