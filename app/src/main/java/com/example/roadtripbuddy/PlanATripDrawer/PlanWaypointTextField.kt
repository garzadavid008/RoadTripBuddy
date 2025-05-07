package com.example.roadtripbuddy.PlanATripDrawer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlanWaypointTextField(
    address: String,
    onAddressClick: () -> Unit,
    hour: String,
    onHourChange: (String) -> Unit,
    minute: String,
    onMinuteChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    ifFirstLocation: Boolean = false, // Boolean flag to not hour and min changes to the first location/index
    isTyping: () -> Unit
) {

    var localHour by rememberSaveable { mutableStateOf(hour)}
    var localMin  by rememberSaveable { mutableStateOf(minute) }

    var focus by remember { mutableStateOf(false) }


    Box(
        modifier = modifier
            .width(350.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))  // Pill shape
            .background(Color.White)
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Address field takes more space
            // Show the address or a placeholder if address is empty
            Box(
                modifier = Modifier
                    .width(200.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = address,
                    modifier = Modifier.clickable { onAddressClick() },
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                )
            }

            // Hour field
            BasicTextField(
                value = localHour,
                onValueChange = { newValue ->
                    if (!ifFirstLocation){
                        // Allow only digits
                        localHour = (newValue.filter { it.isDigit() })
                    }
                },
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .onFocusChanged { focusState ->
                        focus = focusState.isFocused
                        if(focusState.isFocused){
                            isTyping()
                        }else if (!focusState.isFocused){
                            Log.d("PlanAWaypoinyText", "hour changed")
                            onHourChange(localHour)
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (ifFirstLocation) {
                            Text(
                                text = "0",
                                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            // Minute field
            BasicTextField(
                value = localMin,
                onValueChange = { newValue ->
                    if (!ifFirstLocation){
                        // Allow only digits
                        localMin = (newValue.filter { it.isDigit() })
                    }
                },
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .onFocusChanged { focusState ->
                        if(focusState.isFocused) {
                            isTyping()
                        } else if (!focusState.isFocused){
                            Log.d("PlanAWaypointText", "minutes changed")
                            onMinuteChange(localMin)
                        }
                    },
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (ifFirstLocation) {
                            Text(
                                text = "0",
                                style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}