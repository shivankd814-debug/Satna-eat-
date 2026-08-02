package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.Address
import com.example.data.models.Restaurant
import com.example.data.models.UserRole
import com.example.data.repository.SatnaEatsRepository
import com.example.ui.components.TopBarLocationHeader
import com.example.ui.screens.admin.AdminDashboardScreen
import com.example.ui.screens.customer.*
import com.example.ui.screens.docs.DeveloperDocsScreen
import com.example.ui.screens.restaurant.RestaurantDashboardScreen
import com.example.ui.screens.rider.RiderDashboardScreen
import com.example.ui.theme.SatnaEatsTheme
import com.example.ui.theme.SatnaOrangePrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = SatnaEatsRepository.getInstance()

        setContent {
            SatnaEatsTheme {
                MainAppContainer(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(repository: SatnaEatsRepository) {
    val currentUser by repository.currentUser.collectAsState()
    val activePortal by repository.activePortal.collectAsState()
    val selectedAddress by repository.selectedAddress.collectAsState()
    val cartItems by repository.cartItems.collectAsState()
    val activeOrder by repository.activeOrder.collectAsState()

    var currentScreen by remember { mutableStateOf("HOME") }
    var selectedRestaurantDetail by remember { mutableStateOf<Restaurant?>(null) }

    var showPortalSwitcherModal by remember { mutableStateOf(false) }
    var showAddressSelectorModal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBarLocationHeader(
                currentAddress = selectedAddress.fullAddress,
                currentRole = activePortal,
                language = currentUser.language,
                onAddressClick = { showAddressSelectorModal = true },
                onRoleClick = { showPortalSwitcherModal = true },
                onLanguageToggle = {
                    repository.setLanguage(if (currentUser.language == "EN") "HI" else "EN")
                },
                onSearchClick = { currentScreen = "HOME" }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == "HOME",
                    onClick = { currentScreen = "HOME"; selectedRestaurantDetail = null },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Explore") },
                    label = { Text("Explore", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentScreen == "CART",
                    onClick = { currentScreen = "CART" },
                    icon = {
                        BadgedBox(badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text("${cartItems.sumOf { it.quantity }}") }
                            }
                        }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    },
                    label = { Text("Cart", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentScreen == "TRACKING",
                    onClick = { currentScreen = "TRACKING" },
                    icon = {
                        BadgedBox(badge = {
                            if (activeOrder != null) {
                                Badge(containerColor = SatnaOrangePrimary) { Text("LIVE") }
                            }
                        }) {
                            Icon(Icons.Default.DirectionsBike, contentDescription = "Track")
                        }
                    },
                    label = { Text("Track", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentScreen == "SUPPORT",
                    onClick = { currentScreen = "SUPPORT" },
                    icon = { Icon(Icons.Default.HeadsetMic, contentDescription = "Support") },
                    label = { Text("Support", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentScreen == "PROFILE",
                    onClick = { currentScreen = "PROFILE" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile", fontSize = 11.sp) }
                )

                NavigationBarItem(
                    selected = currentScreen == "ADMIN" || currentScreen == "DOCS",
                    onClick = { currentScreen = "ADMIN" },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                    label = { Text("Admin/Docs", fontSize = 11.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activePortal) {
                UserRole.CUSTOMER -> {
                    when (currentScreen) {
                        "HOME" -> {
                            if (selectedRestaurantDetail != null) {
                                RestaurantDetailScreen(
                                    restaurant = selectedRestaurantDetail!!,
                                    repository = repository,
                                    onBackClick = { selectedRestaurantDetail = null },
                                    onCartClick = { currentScreen = "CART" }
                                )
                            } else {
                                CustomerHomeScreen(
                                    repository = repository,
                                    onRestaurantClick = { selectedRestaurantDetail = it },
                                    onCartClick = { currentScreen = "CART" },
                                    onOrderTrackingClick = { currentScreen = "TRACKING" }
                                )
                            }
                        }
                        "CART" -> {
                            CartCheckoutScreen(
                                repository = repository,
                                onBackClick = { currentScreen = "HOME" },
                                onOrderPlaced = { currentScreen = "TRACKING" }
                            )
                        }
                        "TRACKING" -> {
                            LiveOrderTrackingScreen(
                                repository = repository,
                                onBackClick = { currentScreen = "HOME" }
                            )
                        }
                        "SUPPORT" -> {
                            CustomerSupportScreen(
                                repository = repository,
                                onNavigateBack = { currentScreen = "HOME" }
                            )
                        }
                        "PROFILE" -> {
                            CustomerProfileScreen(
                                repository = repository,
                                onLanguageToggle = {
                                    repository.setLanguage(if (currentUser.language == "EN") "HI" else "EN")
                                }
                            )
                        }
                        "ADMIN" -> {
                            AdminDashboardScreen(
                                repository = repository,
                                onOpenDocsClick = { currentScreen = "DOCS" }
                            )
                        }
                        "DOCS" -> {
                            DeveloperDocsScreen(
                                onBackClick = { currentScreen = "ADMIN" }
                            )
                        }
                    }
                }

                UserRole.RESTAURANT_OWNER -> {
                    RestaurantDashboardScreen(repository = repository)
                }

                UserRole.RIDER -> {
                    RiderDashboardScreen(repository = repository)
                }

                UserRole.ADMIN -> {
                    if (currentScreen == "DOCS") {
                        DeveloperDocsScreen(onBackClick = { currentScreen = "ADMIN" })
                    } else {
                        AdminDashboardScreen(
                            repository = repository,
                            onOpenDocsClick = { currentScreen = "DOCS" }
                        )
                    }
                }
            }
        }
    }

    // Portal Switcher Modal
    if (showPortalSwitcherModal) {
        AlertDialog(
            onDismissRequest = { showPortalSwitcherModal = false },
            title = { Text("Switch Satna Eats App Portal 🔄", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select your role/view within Satna Eats:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    val roles = listOf(
                        UserRole.CUSTOMER to "🍔 Customer App (Browse & Order)",
                        UserRole.RESTAURANT_OWNER to "🏪 Restaurant Owner (Satna Treat)",
                        UserRole.RIDER to "🛵 Delivery Partner (Rider App)",
                        UserRole.ADMIN to "👑 Founder & Admin (Shivank Dwivedi)"
                    )

                    roles.forEach { (role, label) ->
                        Surface(
                            color = if (activePortal == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    repository.setPortal(role)
                                    showPortalSwitcherModal = false
                                }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (activePortal == role) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPortalSwitcherModal = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Address Selector Modal
    if (showAddressSelectorModal) {
        AlertDialog(
            onDismissRequest = { showAddressSelectorModal = false },
            title = { Text("Select Delivery Address in Satna 📍") },
            text = {
                Column {
                    currentUser.addresses.forEach { addr ->
                        Surface(
                            color = if (selectedAddress.id == addr.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    repository.setSelectedAddress(addr)
                                    showAddressSelectorModal = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(addr.label, fontWeight = FontWeight.Bold)
                                Text(addr.fullAddress, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddressSelectorModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
