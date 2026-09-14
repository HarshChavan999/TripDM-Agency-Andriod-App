package com.tripdm.agency.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Luggage
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripdm.agency.ui.theme.DeepNavy
import com.tripdm.agency.ui.theme.InterFontFamily
import com.tripdm.agency.ui.theme.PrimaryOrange
import com.tripdm.agency.ui.theme.TextSecondary

data class AgencyNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenterAction: Boolean = false
)

@Composable
fun AgencyBottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    unreadLeadsCount: Int = 0
) {
    // 5-item symmetric bar with Chat placed right in the center (index 2)
    val items = listOf(
        AgencyNavItem("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        AgencyNavItem("Listings", Icons.Filled.Luggage, Icons.Outlined.Luggage),
        AgencyNavItem("Chat", Icons.Filled.Chat, Icons.Outlined.ChatBubbleOutline, isCenterAction = true),
        AgencyNavItem("Credits", Icons.Filled.MonetizationOn, Icons.Outlined.MonetizationOn),
        AgencyNavItem("Profile", Icons.Filled.Business, Icons.Outlined.Business)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTab == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    if (item.isCenterAction) {
                        // Highlighted Center Chat Icon
                        BadgedBox(
                            badge = {
                                if (unreadLeadsCount > 0) {
                                    Badge(
                                        containerColor = PrimaryOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadLeadsCount")
                                    }
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        if (isSelected) PrimaryOrange else PrimaryOrange.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color.White else PrimaryOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected || item.isCenterAction) FontWeight.Bold else FontWeight.Medium,
                        fontFamily = InterFontFamily
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryOrange,
                    selectedTextColor = PrimaryOrange,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

