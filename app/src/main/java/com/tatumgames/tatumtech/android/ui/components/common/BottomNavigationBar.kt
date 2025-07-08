package com.tatumgames.tatumtech.android.ui.components.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.navigation.routes.NavRoutes
import com.tatumgames.tatumtech.android.ui.components.screens.models.BottomNavigationItem

@Composable
fun BottomNavigationBar(
    navController: NavController
) {
    // Safe cast to NavHostController to avoid runtime crash
    val navHostController = navController as NavHostController
    val navBackStackEntry = navHostController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    val navItems = listOf(
        BottomNavigationItem(
            Icons.Default.Home,
            stringResource(R.string.home),
            NavRoutes.MAIN_SCREEN
        ),
        BottomNavigationItem(
            Icons.Default.Face,
            stringResource(R.string.learn),
            NavRoutes.LEARN_SCREEN
        ),
        BottomNavigationItem(
            Icons.Default.DateRange,
            stringResource(R.string.timeline),
            NavRoutes.MY_TIMELINE_SCREEN
        ),
        BottomNavigationItem(
            Icons.Default.Star,
            stringResource(R.string.stats),
            NavRoutes.STATS_SCREEN
        ),
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.MAIN_SCREEN) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                },
                alwaysShowLabel = true
            )
        }
    }
}
