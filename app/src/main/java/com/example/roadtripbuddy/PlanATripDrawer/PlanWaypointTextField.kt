package com.example.roadtripbuddy.PlanATripDrawer

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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
    onFocus: (FocusState) -> Unit,
    modifier: Modifier = Modifier,
    ifFirstLocation: Boolean = false, // Boolean flag to not hour and min changes to the first location/index
) {
    Box(
        modifier = modifier
            .width(350.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))  // Pill shape
            .background(Color.White)
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 12.dp)
    ) {
        // Row to arrange the three fields side by side
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Address field takes more space
            // Show the address or a placeholder if address is empty.
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

            //Spacer(modifier = Modifier.weight(1f))
            // Hour field
            BasicTextField(
                value = hour,
                onValueChange = { newValue ->
                    if (!ifFirstLocation){
                        // Allow only digits
                        onHourChange(newValue.filter { it.isDigit() })
                    }
                },
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
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
                value = minute,
                onValueChange = { newValue ->
                    if (!ifFirstLocation){
                        // Allow only digits
                        onMinuteChange(newValue.filter { it.isDigit() })
                    }
                },
                textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp),
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