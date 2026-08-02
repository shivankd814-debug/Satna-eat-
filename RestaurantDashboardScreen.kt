package com.example.ui.screens.restaurant

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.SatnaEatsRepository
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.*

@Composable
fun RestaurantDashboardScreen(
    repository: SatnaEatsRepository
) {
    val restaurants by repository.restaurants.collectAsState()
    val allMenuItems by repository.menuItems.collectAsState()
    val orderHistory by repository.orderHistory.collectAsState()

    var selectedRestaurantId by remember { mutableStateOf(restaurants.firstOrNull()?.id ?: "") }
    var showRegisterModal by remember { mutableStateOf(false) }

    val currentRestaurant = restaurants.find { it.id == selectedRestaurantId } ?: restaurants.firstOrNull()

    var isRestaurantOnline by remember { mutableStateOf(currentRestaurant?.isOpen ?: true) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Live Orders, 1: Food Menu, 2: Analytics & Revenue, 3: Profile & KYC Docs

    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<MenuItem?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    if (currentRestaurant == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No restaurant created yet.", fontSize = 16.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { showRegisterModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                ) {
                    Text("+ Register New Restaurant in Satna")
                }
            }
        }

        if (showRegisterModal) {
            RegisterRestaurantModal(
                onDismiss = { showRegisterModal = false },
                onRegister = { newRst ->
                    repository.addRestaurant(newRst)
                    selectedRestaurantId = newRst.id
                    showRegisterModal = false
                }
            )
        }
        return
    }

    val myMenuItems = allMenuItems.filter { it.restaurantId == currentRestaurant.id }
    val myOrders = orderHistory.filter { it.restaurantId == currentRestaurant.id }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header & Restaurant Switcher
        Card(
            colors = CardDefaults.cardColors(containerColor = SatnaTealContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                currentRestaurant.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SatnaTealSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (currentRestaurant.isApprovedByAdmin) {
                                Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                                    Text("APPROVED ✓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                        }
                        Text("${currentRestaurant.address} • FSSAI: ${currentRestaurant.fssaiLicense}", fontSize = 11.sp, color = SatnaTealSecondary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isRestaurantOnline) "ONLINE" else "OFFLINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isRestaurantOnline) SatnaVegGreen else SatnaNonVegRed
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = isRestaurantOnline,
                            onCheckedChange = {
                                isRestaurantOnline = it
                                repository.updateRestaurant(currentRestaurant.copy(isOpen = it))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showRegisterModal = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Register Another Outlet", fontSize = 11.sp)
                    }

                    TextButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Outlet Profile & Docs", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs (5 Tabs)
        ScrollableTabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Live Orders (${myOrders.size})", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Food Menu (${myMenuItems.size})", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Analytics & Revenue", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("KYC & Bank", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                Text("Complaints 🎧", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> LiveOrdersTab(repository, myOrders)
            1 -> MenuManagementTab(
                repository = repository,
                items = myMenuItems,
                onAddNew = { showAddItemDialog = true },
                onEdit = { itemToEdit = it }
            )
            2 -> AnalyticsAndRevenueTab(restaurant = currentRestaurant, orders = myOrders)
            3 -> KycAndBankDetailsTab(restaurant = currentRestaurant, repository = repository, onEditClick = { showEditProfileDialog = true })
            4 -> RestaurantComplaintsTab(repository = repository, restaurantId = currentRestaurant.id, restaurantName = currentRestaurant.name)
        }
    }

    // Modal 1: Add New Food Item
    if (showAddItemDialog) {
        FoodItemFormModal(
            restaurantId = currentRestaurant.id,
            existingItem = null,
            onDismiss = { showAddItemDialog = false },
            onSave = { newItem ->
                repository.addMenuItem(newItem)
                showAddItemDialog = false
            }
        )
    }

    // Modal 2: Edit Existing Food Item
    itemToEdit?.let { existing ->
        FoodItemFormModal(
            restaurantId = currentRestaurant.id,
            existingItem = existing,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                repository.updateMenuItem(updated)
                itemToEdit = null
            }
        )
    }

    // Modal 3: Register New Restaurant Account
    if (showRegisterModal) {
        RegisterRestaurantModal(
            onDismiss = { showRegisterModal = false },
            onRegister = { newRst ->
                repository.addRestaurant(newRst)
                selectedRestaurantId = newRst.id
                showRegisterModal = false
            }
        )
    }

    // Modal 4: Edit Restaurant Profile & Documents
    if (showEditProfileDialog) {
        RegisterRestaurantModal(
            existingRestaurant = currentRestaurant,
            onDismiss = { showEditProfileDialog = false },
            onRegister = { updatedRst ->
                repository.updateRestaurant(updatedRst)
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
fun LiveOrdersTab(repository: SatnaEatsRepository, orders: List<Order>) {
    var rejectReasonOrder by remember { mutableStateOf<Order?>(null) }

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active orders for your outlet yet in Satna.", color = Color.Gray, fontSize = 13.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(orders) { order ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("₹${order.grandTotal.toInt()}", fontWeight = FontWeight.Bold, color = SatnaOrangePrimary, fontSize = 15.sp)
                        }
                        Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp, color = Color.Gray)
                        Text("Delivery: ${order.deliveryAddress.fullAddress}", fontSize = 11.sp, color = Color.Gray)
                        Text("Payment: ${order.paymentMethod.name} (${order.paymentStatus.name})", fontSize = 11.sp, color = SatnaTealSecondary)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Ordered Items:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        order.items.forEach { item ->
                            Text("• ${item.quantity}x ${item.menuItem.name} - ₹${item.totalPrice.toInt()}", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(color = SatnaOrangeContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(order.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (order.status == OrderStatus.PLACED) {
                                    OutlinedButton(
                                        onClick = { rejectReasonOrder = order },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SatnaNonVegRed)
                                    ) {
                                        Text("Reject ❌", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { repository.updateOrderStatus(order.id, OrderStatus.COOKING) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                                    ) {
                                        Text("Accept & Cook 🍳", fontSize = 11.sp)
                                    }
                                } else if (order.status == OrderStatus.COOKING) {
                                    Button(
                                        onClick = { repository.updateOrderStatus(order.id, OrderStatus.READY_FOR_PICKUP) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                                    ) {
                                        Text("Mark Ready for Rider 📦", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    rejectReasonOrder?.let { order ->
        AlertDialog(
            onDismissRequest = { rejectReasonOrder = null },
            title = { Text("Reject Order #${order.orderNumber}") },
            text = { Text("Are you sure you want to reject this order? Customer will receive instant refund.") },
            confirmButton = {
                Button(
                    onClick = {
                        repository.updateOrderStatus(order.id, OrderStatus.CANCELLED)
                        rejectReasonOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaNonVegRed)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectReasonOrder = null }) {
                    Text("Back")
                }
            }
        )
    }
}

@Composable
fun MenuManagementTab(
    repository: SatnaEatsRepository,
    items: List<MenuItem>,
    onAddNew: () -> Unit,
    onEdit: (MenuItem) -> Unit
) {
    Column {
        Button(
            onClick = onAddNew,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("ADD NEW FOOD DISH / ITEM")
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = SatnaOrangeContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = SatnaOrangePrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                VegNonVegBadge(isVeg = item.isVeg)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Text("${item.category} • Price: ₹${item.price.toInt()}", fontSize = 12.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onEdit(item) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Item", tint = SatnaTealSecondary)
                            }

                            Switch(
                                checked = item.isAvailable,
                                onCheckedChange = { updated ->
                                    repository.updateMenuItem(item.copy(isAvailable = updated))
                                }
                            )

                            IconButton(onClick = { repository.deleteMenuItem(item.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SatnaNonVegRed)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticsAndRevenueTab(restaurant: Restaurant, orders: List<Order>) {
    val totalSales = orders.sumOf { it.itemTotal }
    val commission = totalSales * 0.15
    val netEarnings = totalSales - commission

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SatnaVegGreen.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Revenue Analytics & Bank Payouts 💰", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Gross Orders Sales", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${totalSales.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("Satna Eats Fee (15%)", fontSize = 12.sp, color = Color.Gray)
                        Text("-₹${commission.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SatnaNonVegRed)
                    }
                    Column {
                        Text("Net Earnings", fontSize = 12.sp, color = Color.Gray)
                        Text("₹${netEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SatnaVegGreen)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text("Bank Payout Target: ${restaurant.bankName} (${restaurant.bankAccount})", fontSize = 11.sp, color = SatnaTealSecondary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Outlet Key Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Orders Delivered:", fontSize = 12.sp)
                    Text("${orders.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Average Order Value (AOV):", fontSize = 12.sp)
                    val aov = if (orders.isNotEmpty()) totalSales / orders.size else 0.0
                    Text("₹${aov.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Customer Rating:", fontSize = 12.sp)
                    Text("⭐ ${restaurant.rating} / 5 (${restaurant.totalRatings} ratings)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaOrangePrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Instant Bank Payout */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary)
        ) {
            Text("INSTANT PAYOUT TO BANK ACCOUNT (NEFT/UPI) 💸", fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
fun KycAndBankDetailsTab(restaurant: Restaurant, repository: SatnaEatsRepository, onEditClick: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Restaurant Verification & KYC Documents 📋", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                DocStatusRow(title = "GSTIN Number", code = restaurant.gstNumber, docUrl = restaurant.gstDocUrl)
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                DocStatusRow(title = "FSSAI License", code = restaurant.fssaiLicense, docUrl = restaurant.fssaiDocUrl)
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                DocStatusRow(title = "PAN Card Number", code = restaurant.panNumber, docUrl = restaurant.panDocUrl)
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                DocStatusRow(title = "Aadhaar Card Number", code = restaurant.aadhaarNumber, docUrl = restaurant.aadhaarDocUrl)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Bank Account Details 🏦", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Account Holder: ${restaurant.bankHolderName}", fontSize = 12.sp)
                Text("Account Number: ${restaurant.bankAccount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("IFSC Code: ${restaurant.bankIfsc}", fontSize = 12.sp)
                Text("Bank Name: ${restaurant.bankName}", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Satna GPS Location Coordinates 📍", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Latitude: ${restaurant.latitude}, Longitude: ${restaurant.longitude}", fontSize = 12.sp, color = SatnaTealSecondary)
                Text("Address: ${restaurant.address}, Satna, MP", fontSize = 12.sp)

                Spacer(modifier = Modifier.height(10.dp))

                com.example.maps.RestaurantPinLocationPicker(
                    currentLat = restaurant.latitude,
                    currentLng = restaurant.longitude,
                    onLocationSelected = { lat, lng, addr ->
                        repository.updateRestaurantCoordinates(restaurant.id, lat, lng, addr)
                    }
                )
            }
        }
    }
}

@Composable
fun DocStatusRow(title: String, code: String, docUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(code, fontSize = 12.sp, color = SatnaTealSecondary)
        }

        if (docUrl.isNotBlank()) {
            Surface(color = SatnaVegGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                Text("VERIFIED ✓", color = SatnaVegGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        } else {
            Surface(color = SatnaOrangeContainer, shape = RoundedCornerShape(4.dp)) {
                Text("PENDING UPLOAD", color = SatnaOrangeDark, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
fun FoodItemFormModal(
    restaurantId: String,
    existingItem: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (MenuItem) -> Unit
) {
    var name by remember { mutableStateOf(existingItem?.name ?: "") }
    var priceText by remember { mutableStateOf(existingItem?.price?.toInt()?.toString() ?: "180") }
    var originalPriceText by remember { mutableStateOf(existingItem?.originalPrice?.toInt()?.toString() ?: "220") }
    var category by remember { mutableStateOf(existingItem?.category ?: "North Indian") }
    var description by remember { mutableStateOf(existingItem?.description ?: "") }
    var isVeg by remember { mutableStateOf(existingItem?.isVeg ?: true) }
    var imageUrl by remember { mutableStateOf(existingItem?.imageUrl ?: "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingItem == null) "Add Food Dish" else "Edit Food Dish") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Dish Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (₹)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = originalPriceText, onValueChange = { originalPriceText = it }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Food Photo Image URL") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Is Pure Veg Dish?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(checked = isVeg, onCheckedChange = { isVeg = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 100.0
                    val origPrice = originalPriceText.toDoubleOrNull()
                    if (name.isNotBlank()) {
                        onSave(
                            MenuItem(
                                id = existingItem?.id ?: "m_${System.currentTimeMillis()}",
                                restaurantId = restaurantId,
                                name = name,
                                description = description,
                                price = price,
                                originalPrice = origPrice,
                                category = category,
                                isVeg = isVeg,
                                imageUrl = imageUrl,
                                isAvailable = existingItem?.isAvailable ?: true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Text(if (existingItem == null) "Save Dish" else "Update Dish")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RegisterRestaurantModal(
    existingRestaurant: Restaurant? = null,
    onDismiss: () -> Unit,
    onRegister: (Restaurant) -> Unit
) {
    var name by remember { mutableStateOf(existingRestaurant?.name ?: "") }
    var tagline by remember { mutableStateOf(existingRestaurant?.tagline ?: "Authentic Taste of Satna") }
    var address by remember { mutableStateOf(existingRestaurant?.address ?: "Circuit House Road, Satna") }
    var gstNumber by remember { mutableStateOf(existingRestaurant?.gstNumber ?: "23AAAAA0000A1Z5") }
    var gstDocUrl by remember { mutableStateOf(existingRestaurant?.gstDocUrl ?: "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=400") }
    var fssaiLicense by remember { mutableStateOf(existingRestaurant?.fssaiLicense ?: "11824001000543") }
    var fssaiDocUrl by remember { mutableStateOf(existingRestaurant?.fssaiDocUrl ?: "https://images.unsplash.com/photo-1607619056574-7b8d3ee536b2?w=400") }
    var panNumber by remember { mutableStateOf(existingRestaurant?.panNumber ?: "ABCDE1234F") }
    var panDocUrl by remember { mutableStateOf(existingRestaurant?.panDocUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400") }
    var aadhaarNumber by remember { mutableStateOf(existingRestaurant?.aadhaarNumber ?: "9876-5432-1098") }
    var aadhaarDocUrl by remember { mutableStateOf(existingRestaurant?.aadhaarDocUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400") }
    var bankAccount by remember { mutableStateOf(existingRestaurant?.bankAccount ?: "123456789012") }
    var bankIfsc by remember { mutableStateOf(existingRestaurant?.bankIfsc ?: "SBIN0001234") }
    var bankName by remember { mutableStateOf(existingRestaurant?.bankName ?: "State Bank of India Satna") }
    var bankHolderName by remember { mutableStateOf(existingRestaurant?.bankHolderName ?: "Shivank Dwivedi") }
    var coverUrl by remember { mutableStateOf(existingRestaurant?.coverUrl ?: "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800") }
    var logoUrl by remember { mutableStateOf(existingRestaurant?.logoUrl ?: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400") }
    var isPureVeg by remember { mutableStateOf(existingRestaurant?.isPureVeg ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existingRestaurant == null) "Register Outlet in Satna 🏪" else "Update Outlet Profile & KYC") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Restaurant Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = tagline, onValueChange = { tagline = it }, label = { Text("Tagline / Speciality") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Full Satna Address & Landmark") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))
                Text("KYC Documents & Licenses 📄", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(value = gstNumber, onValueChange = { gstNumber = it }, label = { Text("GSTIN Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = fssaiLicense, onValueChange = { fssaiLicense = it }, label = { Text("FSSAI License No.") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = panNumber, onValueChange = { panNumber = it }, label = { Text("PAN Card No.") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = aadhaarNumber, onValueChange = { aadhaarNumber = it }, label = { Text("Aadhaar Card No.") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))
                Text("Bank Details for Payouts 🏦", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(value = bankHolderName, onValueChange = { bankHolderName = it }, label = { Text("Account Holder Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankAccount, onValueChange = { bankAccount = it }, label = { Text("Bank Account No.") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankIfsc, onValueChange = { bankIfsc = it }, label = { Text("IFSC Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank & Branch Name") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(10.dp))
                Text("Images & Pure Veg Status 🖼️", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                OutlinedTextField(value = coverUrl, onValueChange = { coverUrl = it }, label = { Text("Cover Image Banner URL") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = logoUrl, onValueChange = { logoUrl = it }, label = { Text("Logo Image URL") }, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("100% Pure Veg Outlet?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Switch(checked = isPureVeg, onCheckedChange = { isPureVeg = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onRegister(
                            Restaurant(
                                id = existingRestaurant?.id ?: "rst_${System.currentTimeMillis()}",
                                name = name,
                                tagline = tagline,
                                address = address,
                                gstNumber = gstNumber,
                                gstDocUrl = gstDocUrl,
                                fssaiLicense = fssaiLicense,
                                fssaiDocUrl = fssaiDocUrl,
                                panNumber = panNumber,
                                panDocUrl = panDocUrl,
                                aadhaarNumber = aadhaarNumber,
                                aadhaarDocUrl = aadhaarDocUrl,
                                bankAccount = bankAccount,
                                bankIfsc = bankIfsc,
                                bankName = bankName,
                                bankHolderName = bankHolderName,
                                coverUrl = coverUrl,
                                logoUrl = logoUrl,
                                isPureVeg = isPureVeg,
                                isApprovedByAdmin = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Text(if (existingRestaurant == null) "Submit Outlet Registration" else "Save Changes")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RestaurantComplaintsTab(
    repository: SatnaEatsRepository,
    restaurantId: String,
    restaurantName: String
) {
    val supportTickets by repository.supportTickets.collectAsState()
    val restaurantTickets = supportTickets.filter { it.restaurantId == restaurantId || it.assignedToRole == "RESTAURANT" }

    var selectedTicketForReply by remember { mutableStateOf<SupportTicket?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Customer Complaints for $restaurantName", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Direct complaints regarding food quality, missing items, or preparation delays.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(10.dp))

        if (restaurantTickets.isEmpty()) {
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SatnaVegGreen, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("No active customer complaints!", fontWeight = FontWeight.Bold)
                    Text("Great job maintaining food quality for $restaurantName.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        } else {
            restaurantTickets.forEach { tkt ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tkt.ticketNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Surface(color = SatnaOrangeContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(tkt.status.name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SatnaOrangeDark, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Customer: ${tkt.customerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Subject: ${tkt.subject}", fontSize = 12.sp)
                        Text("Description: ${tkt.description}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { selectedTicketForReply = tkt },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                        ) {
                            Text("Reply to Customer ✍️", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    selectedTicketForReply?.let { ticket ->
        var replyMessage by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedTicketForReply = null },
            title = { Text("Reply to Ticket ${ticket.ticketNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Customer: ${ticket.customerName}", fontSize = 11.sp, color = Color.Gray)
                    Text("Issue: ${ticket.description}", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = replyMessage,
                        onValueChange = { replyMessage = it },
                        label = { Text("Restaurant Official Reply") },
                        placeholder = { Text("e.g. Apologies! We will send complimentary item in next order.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (replyMessage.isNotBlank()) {
                            repository.addMessageToSupportTicket(
                                ticketId = ticket.id,
                                senderRole = "RESTAURANT",
                                senderName = "$restaurantName Manager",
                                messageText = replyMessage
                            )
                            repository.updateTicketStatus(ticket.id, TicketStatus.RESOLVED)
                            selectedTicketForReply = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                ) {
                    Text("Send Reply & Resolve")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTicketForReply = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

