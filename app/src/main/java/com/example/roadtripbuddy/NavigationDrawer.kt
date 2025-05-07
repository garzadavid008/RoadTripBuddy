package com.example.roadtripbuddy

import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.math.log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    drawerState: DrawerState,
    userViewModel: UserViewModel,
    gesturesStatus: Boolean,
    authState: AuthState,
    navController: NavController,
    authViewModel: IAuthViewModel, // changed from AuthView model 4/19/25
    onItemClick: (MenuItems) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit // Add this parameter
) {


    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesStatus,
        scrimColor = Color.Black.copy(alpha = 0.5f),
        modifier = modifier,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                DrawerHeader(userViewModel,authViewModel)
                Spacer(modifier = Modifier.height(24.dp))
                DrawerBody(
                    items = listOf(
                        MenuItems(
                            id = "plan_a_trip",
                            title = "Plan A Trip",
                            contentDescription = "Go to Plan A Trip"
                        ),
                        MenuItems(
                            id = "suggest",
                            title = "Suggest",
                            contentDescription = "Go to Settings"
                        ),
                        MenuItems(
                            id = "settings",
                            title = "Settings",
                            contentDescription = "Go to Settings"
                        ),
                        MenuItems(
                            id = "about",
                            title = "About",
                            contentDescription = "Go to About page"
                        ), *if (authState == AuthState.Unauthenticated) { // login
                            arrayOf(
                                MenuItems(
                                    id = "login",
                                    title = "Login",
                                    contentDescription = "Login page"
                                )
                            )
                        } else if (authState == AuthState.Authenticated) { // signout
                            arrayOf(
                                MenuItems(
                                    id = "logout",
                                    title = "Logout",
                                    contentDescription = "Logout"
                                )
                            )
                        } else {
                            emptyArray()
                        }
                    ),
                    onItemClick = onItemClick
                )
            }
        },
        content = content // Pass the main content here
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerHeader(userViewModel: UserViewModel, authViewModel: IAuthViewModel) {

    // grabbing the users login instance
    var loginStatus = authViewModel.authState.value
     // grabbing the user
    var userName by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    val user = userViewModel.user
    Log.d("Auth", "$loginStatus")
    // boiler plate
    userName = "User name"
    userEmail = "test@gmail.com"
    if (user != null)
    {
        userName = user.name
        userEmail = user.email
    }
    if(loginStatus == AuthState.Authenticated)
    {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                //Profile Image + User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Spacer(modifier = Modifier.width(12.dp))

                    // User Info
                    Column {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = userEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

}

@Composable
fun DrawerBody(
    items: List<MenuItems>,
    modifier: Modifier = Modifier,
    itemTextStyle: TextStyle = TextStyle(fontSize = 18.sp),
    onItemClick: (MenuItems) -> Unit
) {
    LazyColumn(modifier) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onItemClick(item) }
                    .padding(16.dp)
            ) {
                Text(
                    text = item.title,
                    style = itemTextStyle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
