package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.SatnaEatsRepository
import com.example.ui.theme.*

@Composable
fun AdminDashboardScreen(
    repository: SatnaEatsRepository,
    onOpenDocsClick: () -> Unit
) {
    val platformSettings by repository.platformSettings.collectAsState()
    val restaurants by repository.restaurants.collectAsState()
    val riders by repository.riders.collectAsState()
    val customers by repository.customers.collectAsState()
    val orderHistory by repository.orderHistory.collectAsState()
    val coupons by repository.coupons.collectAsState()
    val banners by repository.banners.collectAsState()
    val categories by repository.categories.collectAsState()
    val cities by repository.cities.collectAsState()
    val notifications by repository.notifications.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Founder Header Badge
        Card(
            colors = CardDefaults.cardColors(containerColor = SatnaOrangePrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    "FOUNDER SUPER ADMIN • FULL CONTROL",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            platformSettings.founderName,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "${platformSettings.platformName} Hub • ${platformSettings.launchCity}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = if (platformSettings.isEmergencyOffline) SatnaNonVegRed else SatnaVegGreen,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (platformSettings.isEmergencyOffline) "SYSTEM OFFLINE" else "PLATFORM LIVE",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Total Revenue: ₹${platformSettings.totalRevenue.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs for Founder
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Overview & Revenue", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Restaurants (${restaurants.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("Riders (${riders.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Customers (${customers.size})", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 4, onClick = { selectedTab = 4 }) {
                Text("Orders & Payments", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 5, onClick = { selectedTab = 5 }) {
                Text("Commission & Pricing", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 6, onClick = { selectedTab = 6 }) {
                Text("Coupons", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 7, onClick = { selectedTab = 7 }) {
                Text("Banners & Categories", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 8, onClick = { selectedTab = 8 }) {
                Text("Cities Expansion", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 9, onClick = { selectedTab = 9 }) {
                Text("Push Alerts", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 10, onClick = { selectedTab = 10 }) {
                Text("Support & Complaints 🎧", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 11, onClick = { selectedTab = 11; onOpenDocsClick() }) {
                Text("Dev Docs 📄", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> RevenueOverviewTab(platformSettings, repository, restaurants.size, riders.size, customers.size, orderHistory.size)
            1 -> ManageRestaurantsTab(repository, restaurants)
            2 -> ManageRidersTab(repository, riders)
            3 -> ManageCustomersTab(repository, customers)
            4 -> ManageOrdersAndPaymentsTab(repository, orderHistory)
            5 -> EditCommissionAndPricingTab(repository, platformSettings)
            6 -> ManageCouponsTab(repository, coupons)
            7 -> ManageBannersAndCategoriesTab(repository, banners, categories)
            8 -> ManageCitiesTab(repository, cities)
            9 -> BroadcastPushNotificationsTab(repository, notifications)
            10 -> FounderSupportManagementTab(repository)
        }
    }
}

@Composable
fun RevenueOverviewTab(
    settings: PlatformSettings,
    repository: SatnaEatsRepository,
    totalRestaurants: Int,
    totalRiders: Int,
    totalCustomers: Int,
    totalOrders: Int
) {
    val totalRevenue = settings.totalRevenue
    val commissionRevenue = totalRevenue * (settings.commissionPercent / 100.0)
    val riderPayouts = totalRevenue * 0.20
    val netPlatformProfit = commissionRevenue - (riderPayouts * 0.1)

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text("Revenue & Platform Metrics 📊", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Card(colors = CardDefaults.cardColors(containerColor = SatnaTealContainer), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Gross Order GMV", fontSize = 11.sp, color = SatnaTealSecondary)
                    Text("₹${totalRevenue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SatnaTealSecondary)
                    Text("Total 1,842 orders in Satna", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = SatnaOrangeContainer), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Satna Eats Commission", fontSize = 11.sp, color = SatnaOrangeDark)
                    Text("₹${commissionRevenue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SatnaOrangeDark)
                    Text("@ ${settings.commissionPercent}% platform cut", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Card(colors = CardDefaults.cardColors(containerColor = SatnaVegGreen.copy(alpha = 0.12f)), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Net Founder Profit", fontSize = 11.sp, color = SatnaVegGreen)
                    Text("₹${netPlatformProfit.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = SatnaVegGreen)
                    Text("After partner & rider settlements", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Rider Payout Total", fontSize = 11.sp, color = Color.Gray)
                    Text("₹${riderPayouts.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Delivery charges disbursed", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Founder Control Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("• Active Satna Restaurants: $totalRestaurants", fontSize = 12.sp)
                Text("• Verified Delivery Partners: $totalRiders", fontSize = 12.sp)
                Text("• Registered Customers: $totalCustomers", fontSize = 12.sp)
                Text("• Delivery Charge Base Rate: ₹${settings.baseDeliveryFee.toInt()}", fontSize = 12.sp)
                Text("• Free Delivery Order Limit: ₹${settings.freeDeliveryAbove.toInt()}", fontSize = 12.sp)
                Text("• Emergency Offline Mode: ${if (settings.isEmergencyOffline) "ACTIVE 🛑" else "NORMAL ✅"}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        repository.updatePlatformSettings(settings.copy(isEmergencyOffline = !settings.isEmergencyOffline))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (settings.isEmergencyOffline) SatnaVegGreen else SatnaNonVegRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (settings.isEmergencyOffline) "Resume Platform Services ▶" else "Emergency Halt Platform 🛑")
                }
            }
        }
    }
}

@Composable
fun ManageRestaurantsTab(repository: SatnaEatsRepository, restaurants: List<Restaurant>) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = restaurants.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true)
    }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search restaurants in Satna...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { rest ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(rest.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (rest.isPureVeg) {
                                        Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                                            Text("PURE VEG", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                                Text("FSSAI: ${rest.fssaiLicense} • GST: ${rest.gstNumber}", fontSize = 11.sp, color = Color.Gray)
                                Text("Owner Contact: ${rest.ownerPhone}", fontSize = 11.sp, color = Color.Gray)
                                Text("Address: ${rest.address}", fontSize = 11.sp, color = Color.Gray)
                                Text("Bank: ${rest.bankHolderName} (${rest.bankAccount})", fontSize = 11.sp, color = SatnaTealSecondary)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = if (rest.isApprovedByAdmin) SatnaVegGreen else SatnaNonVegRed,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        if (rest.isApprovedByAdmin) "APPROVED ✓" else "PENDING ⏳",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = { repository.approveRestaurant(rest.id, !rest.isApprovedByAdmin) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (rest.isApprovedByAdmin) SatnaNonVegRed else SatnaVegGreen
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(if (rest.isApprovedByAdmin) "Reject / Suspend" else "Approve Restaurant", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageRidersTab(repository: SatnaEatsRepository, riders: List<Rider>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(riders) { rider ->
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rider.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Phone: ${rider.phone} • Bike: ${rider.bikeNumber}", fontSize = 11.sp)
                            Text("Model: ${rider.vehicleModel} • RC: ${rider.vehicleRcNumber}", fontSize = 11.sp, color = Color.Gray)
                            Text("Licence: ${rider.licenseNumber}", fontSize = 11.sp, color = Color.Gray)
                            Text("Rating: ⭐ ${rider.rating} • Deliveries: ${rider.totalDeliveries}", fontSize = 11.sp, color = SatnaOrangeDark)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (rider.isApproved) SatnaVegGreen else SatnaNonVegRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (rider.isApproved) "APPROVED ✓" else "PENDING ⏳",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { repository.approveRider(rider.id, !rider.isApproved) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (rider.isApproved) SatnaNonVegRed else SatnaVegGreen
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(if (rider.isApproved) "Block / Suspend" else "Approve Partner", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageCustomersTab(repository: SatnaEatsRepository, customers: List<CustomerModel>) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.phone.contains(searchQuery)
    }

    Column {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search customer name or phone...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { customer ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Phone: ${customer.phone} • Email: ${customer.email}", fontSize = 11.sp, color = Color.Gray)
                            Text("Orders Placed: ${customer.totalOrders} • Total Spent: ₹${customer.totalSpent.toInt()}", fontSize = 11.sp, color = SatnaTealSecondary)
                            Text("Joined: ${customer.registeredDate}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                color = if (customer.isBlocked) SatnaNonVegRed else SatnaVegGreen,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    if (customer.isBlocked) "BLOCKED 🛑" else "ACTIVE ✅",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = { repository.toggleCustomerBlock(customer.id) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (customer.isBlocked) SatnaVegGreen else SatnaNonVegRed
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(if (customer.isBlocked) "Unblock" else "Block User", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrdersAndPaymentsTab(repository: SatnaEatsRepository, orders: List<Order>) {
    var selectedOrderForInvoice by remember { mutableStateOf<Order?>(null) }

    if (orders.isEmpty()) {
        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("No Platform Orders Found", fontWeight = FontWeight.Bold)
                Text("Place sample orders from customer view to manage platform orders here.", fontSize = 12.sp, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(orders) { order ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = Color(0xFF0C2340), shape = RoundedCornerShape(4.dp)) {
                                    Text("RAZORPAY ✓", color = Color(0xFF00C853), fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("₹${order.grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, color = SatnaVegGreen, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 11.sp)
                        Text("Restaurant: ${order.restaurantName}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Payment Method: ${order.paymentMethod.name} (${order.paymentStatus.name})", fontSize = 11.sp, color = SatnaOrangeDark, fontWeight = FontWeight.Bold)
                        Text("Razorpay Order ID: ${order.razorpayOrderId ?: "N/A"}", fontSize = 10.sp, color = Color.Gray)
                        Text("Razorpay Payment ID: ${order.razorpayPaymentId ?: "N/A"}", fontSize = 10.sp, color = Color.Gray)

                        if (order.paymentStatus == PaymentStatus.REFUNDED) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = SatnaNonVegRed.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text("REFUNDED: ₹${order.refundAmount.toInt()} (Refund ID: ${order.refundId ?: "N/A"})", fontSize = 10.sp, color = SatnaNonVegRed, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = SatnaTealContainer, shape = RoundedCornerShape(6.dp)) {
                                Text("Status: ${order.status.name}", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { selectedOrderForInvoice = order },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Tax Invoice 📄", fontSize = 10.sp)
                                }

                                if (order.paymentStatus != PaymentStatus.REFUNDED) {
                                    Button(
                                        onClick = { repository.processRefund(order.id, "Admin triggered full refund") },
                                        colors = ButtonDefaults.buttonColors(containerColor = SatnaNonVegRed),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Issue Refund 💸", fontSize = 10.sp)
                                    }
                                }

                                Button(
                                    onClick = { repository.updateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Delivered ✓", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedOrderForInvoice?.let { ord ->
        com.example.payment.TaxInvoiceDialog(
            order = ord,
            onDismiss = { selectedOrderForInvoice = null }
        )
    }
}

@Composable
fun EditCommissionAndPricingTab(repository: SatnaEatsRepository, settings: PlatformSettings) {
    var baseFeeText by remember { mutableStateOf(settings.baseDeliveryFee.toString()) }
    var freeAboveText by remember { mutableStateOf(settings.freeDeliveryAbove.toString()) }
    var commissionText by remember { mutableStateOf(settings.commissionPercent.toString()) }
    var gstText by remember { mutableStateOf(settings.gstPercent.toString()) }
    var surgeText by remember { mutableStateOf(settings.surgeCharge.toString()) }
    var minOrderText by remember { mutableStateOf(settings.minOrderValue.toString()) }
    var founderNameText by remember { mutableStateOf(settings.founderName) }
    var supportPhoneText by remember { mutableStateOf(settings.supportPhone) }
    var supportEmailText by remember { mutableStateOf(settings.supportEmail) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Edit Platform Commission & Charges ⚙️", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = founderNameText,
                    onValueChange = { founderNameText = it },
                    label = { Text("Founder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = commissionText,
                        onValueChange = { commissionText = it },
                        label = { Text("Commission Rate (%)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = baseFeeText,
                        onValueChange = { baseFeeText = it },
                        label = { Text("Base Delivery Fee (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = freeAboveText,
                        onValueChange = { freeAboveText = it },
                        label = { Text("Free Delivery Threshold (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = surgeText,
                        onValueChange = { surgeText = it },
                        label = { Text("Peak Surge Fee (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gstText,
                        onValueChange = { gstText = it },
                        label = { Text("GST Rate (%)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = minOrderText,
                        onValueChange = { minOrderText = it },
                        label = { Text("Min Order Value (₹)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = supportPhoneText,
                    onValueChange = { supportPhoneText = it },
                    label = { Text("Support Helpline Phone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = supportEmailText,
                    onValueChange = { supportEmailText = it },
                    label = { Text("Support Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        repository.updatePlatformSettings(
                            settings.copy(
                                founderName = founderNameText,
                                commissionPercent = commissionText.toDoubleOrNull() ?: 15.0,
                                baseDeliveryFee = baseFeeText.toDoubleOrNull() ?: 25.0,
                                freeDeliveryAbove = freeAboveText.toDoubleOrNull() ?: 199.0,
                                surgeCharge = surgeText.toDoubleOrNull() ?: 10.0,
                                gstPercent = gstText.toDoubleOrNull() ?: 5.0,
                                minOrderValue = minOrderText.toDoubleOrNull() ?: 99.0,
                                supportPhone = supportPhoneText,
                                supportEmail = supportEmailText
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Founder Platform Settings ✅")
                }
            }
        }
    }
}

@Composable
fun ManageCouponsTab(repository: SatnaEatsRepository, coupons: List<Coupon>) {
    var codeInput by remember { mutableStateOf("") }
    var discInput by remember { mutableStateOf("") }
    var maxDiscInput by remember { mutableStateOf("100") }
    var minOrderInput by remember { mutableStateOf("149") }
    var descInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Create New Satna Offer Coupon 🏷️", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it },
                        label = { Text("Code (e.g. SATNA100)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = discInput,
                        onValueChange = { discInput = it },
                        label = { Text("Disc %") },
                        modifier = Modifier.weight(0.6f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = maxDiscInput,
                        onValueChange = { maxDiscInput = it },
                        label = { Text("Max Cap (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = minOrderInput,
                        onValueChange = { minOrderInput = it },
                        label = { Text("Min Order (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = descInput,
                    onValueChange = { descInput = it },
                    label = { Text("Description text") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (codeInput.isNotBlank()) {
                            repository.addCoupon(
                                Coupon(
                                    code = codeInput.uppercase(),
                                    discountPercentage = discInput.toIntOrNull() ?: 20,
                                    maxDiscountAmount = maxDiscInput.toDoubleOrNull() ?: 100.0,
                                    minOrderValue = minOrderInput.toDoubleOrNull() ?: 149.0,
                                    description = descInput.ifBlank { "${discInput}% OFF Satna Offer" }
                                )
                            )
                            codeInput = ""
                            discInput = ""
                            descInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Publish Coupon Code")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Active Coupons in Satna (${coupons.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        coupons.forEach { coupon ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("CODE: ${coupon.code}", fontWeight = FontWeight.Bold, color = SatnaOrangeDark, fontSize = 14.sp)
                        Text(coupon.description, fontSize = 12.sp)
                        Text("${coupon.discountPercentage}% OFF • Max ₹${coupon.maxDiscountAmount.toInt()} • Min order ₹${coupon.minOrderValue.toInt()}", fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(onClick = { repository.deleteCoupon(coupon.code) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SatnaNonVegRed)
                    }
                }
            }
        }
    }
}

@Composable
fun ManageBannersAndCategoriesTab(
    repository: SatnaEatsRepository,
    banners: List<Banner>,
    categories: List<String>
) {
    var newBannerTitle by remember { mutableStateOf("") }
    var newBannerUrl by remember { mutableStateOf("") }
    var newBannerTag by remember { mutableStateOf("PROMO") }

    var newCategoryName by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // Banner Management Section
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Manage App Homepage Banners 🖼️", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = newBannerTitle,
                    onValueChange = { newBannerTitle = it },
                    label = { Text("Banner Title / Headline") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBannerUrl,
                    onValueChange = { newBannerUrl = it },
                    label = { Text("Image URL (Unsplash or direct URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = newBannerTag,
                    onValueChange = { newBannerTag = it },
                    label = { Text("Promo Badge Tag (e.g. 50% OFF)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (newBannerTitle.isNotBlank()) {
                            repository.addBanner(
                                Banner(
                                    id = "b_${System.currentTimeMillis()}",
                                    title = newBannerTitle,
                                    imageUrl = newBannerUrl.ifBlank { "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600" },
                                    promoTag = newBannerTag,
                                    isActive = true
                                )
                            )
                            newBannerTitle = ""
                            newBannerUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Promo Banner")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        banners.forEach { banner ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SatnaOrangeContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = SatnaOrangeDark)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(banner.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Badge: ${banner.promoTag}", fontSize = 11.sp, color = SatnaOrangeDark)
                    }
                    IconButton(onClick = { repository.deleteBanner(banner.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Banner", tint = SatnaNonVegRed)
                    }
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // Food Categories Section
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Manage Food Categories 🍱", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New Category Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newCategoryName.isNotBlank()) {
                                repository.addCategory(newCategoryName.trim())
                                newCategoryName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary)
                    ) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Active Food Categories (${categories.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))

                categories.chunked(2).forEach { pair ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        pair.forEach { cat ->
                            Surface(
                                color = SatnaOrangeContainer,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f).padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SatnaOrangeDark)
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(14.dp).clickable { repository.deleteCategory(cat) },
                                        tint = SatnaNonVegRed
                                    )
                                }
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageCitiesTab(repository: SatnaEatsRepository, cities: List<CityModel>) {
    var newCityName by remember { mutableStateOf("") }
    var newPincode by remember { mutableStateOf("") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Satna Eats City Expansion Hub 🌆", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newCityName,
                        onValueChange = { newCityName = it },
                        label = { Text("City Name (e.g. Rewa)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newPincode,
                        onValueChange = { newPincode = it },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (newCityName.isNotBlank()) {
                            repository.addCity(
                                CityModel(
                                    id = "city_${System.currentTimeMillis()}",
                                    name = newCityName,
                                    state = "Madhya Pradesh",
                                    pincode = newPincode.ifBlank { "485001" },
                                    isActive = false,
                                    totalRestaurants = 0
                                )
                            )
                            newCityName = ""
                            newPincode = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add City to Pipeline")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Target Operating Cities", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        cities.forEach { city ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${city.name}, MP (${city.pincode})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(if (city.isActive) "Active Launch City • ${city.totalRestaurants} Restaurants" else "Upcoming Expansion Area", fontSize = 11.sp, color = Color.Gray)
                    }

                    Switch(
                        checked = city.isActive,
                        onCheckedChange = { repository.toggleCityActive(city.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun BroadcastPushNotificationsTab(repository: SatnaEatsRepository, notifications: List<PushNotification>) {
    var titleInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var targetInput by remember { mutableStateOf("CUSTOMERS") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Broadcast Push Notification 📢", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Push Notification Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    label = { Text("Message Body") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("Target Audience:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CUSTOMERS", "RIDERS", "RESTAURANTS", "ALL").forEach { t ->
                        FilterChip(
                            selected = (targetInput == t),
                            onClick = { targetInput = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (titleInput.isNotBlank() && messageInput.isNotBlank()) {
                            repository.sendNotification(
                                PushNotification(
                                    id = "n_${System.currentTimeMillis()}",
                                    title = titleInput,
                                    message = messageInput,
                                    targetAudience = targetInput,
                                    sentTime = "Just now",
                                    status = "SENT ✓"
                                )
                            )
                            titleInput = ""
                            messageInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Send FCM Broadcast Alert 🚀")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Recent Broadcast History", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        notifications.forEach { notif ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Surface(color = SatnaOrangeContainer, shape = RoundedCornerShape(4.dp)) {
                            Text(notif.targetAudience, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SatnaOrangeDark, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                    Text(notif.message, fontSize = 12.sp, color = Color.Gray)
                    Text("Sent: ${notif.sentTime} • Status: ${notif.status}", fontSize = 10.sp, color = SatnaVegGreen)
                }
            }
        }
    }
}

@Composable
fun FounderSupportManagementTab(repository: SatnaEatsRepository) {
    val supportTickets by repository.supportTickets.collectAsState()
    val analytics = repository.getSupportAnalytics()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedTicketForDetail by remember { mutableStateOf<SupportTicket?>(null) }

    val filteredTickets = remember(supportTickets, selectedFilter) {
        when (selectedFilter) {
            "ESCALATED" -> supportTickets.filter { it.status == TicketStatus.ESCALATED }
            "OPEN" -> supportTickets.filter { it.status == TicketStatus.OPEN || it.status == TicketStatus.IN_PROGRESS }
            "RESOLVED" -> supportTickets.filter { it.status == TicketStatus.RESOLVED || it.status == TicketStatus.CLOSED }
            else -> supportTickets
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Founder CSAT & Support Help Desk", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text("Monitor 24/7 AI agent, reply to escalated complaints, and reassign tickets.", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(10.dp))

        // Analytics Row Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SatnaOrangeContainer),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Complaints", fontSize = 10.sp, color = SatnaOrangeDark)
                    Text("${analytics.totalTickets}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SatnaOrangeDark)
                }
            }
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SatnaNonVegRed.copy(alpha = 0.15f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Escalated", fontSize = 10.sp, color = SatnaNonVegRed)
                    Text("${analytics.escalatedTickets}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SatnaNonVegRed)
                }
            }
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = SatnaVegGreen.copy(alpha = 0.15f)),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CSAT Score", fontSize = 10.sp, color = SatnaVegGreen)
                    Text("${analytics.satisfactionScore} ★", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SatnaVegGreen)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Status Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "ESCALATED", "OPEN", "RESOLVED").forEach { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text(status, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTickets.isEmpty()) {
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text("No tickets found in category '$selectedFilter'", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            filteredTickets.forEach { ticket ->
                val statusColor = when (ticket.status) {
                    TicketStatus.OPEN -> Color(0xFF1E88E5)
                    TicketStatus.IN_PROGRESS -> SatnaOrangeDark
                    TicketStatus.ESCALATED -> SatnaNonVegRed
                    TicketStatus.RESOLVED -> SatnaVegGreen
                    TicketStatus.CLOSED -> Color.Gray
                }

                Card(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedTicketForDetail = ticket }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ticket.ticketNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                    Text(ticket.status.name, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("₹${ticket.satisfactionRating?.let { "$it ★" } ?: "Not rated"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB300))
                        }

                        Text("Customer: ${ticket.customerName} (${ticket.customerPhone})", fontSize = 11.sp)
                        Text("Subject: ${ticket.subject}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Category: ${ticket.category.name} • Assigned: ${ticket.assignedToName}", fontSize = 10.sp, color = SatnaTealSecondary)

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Messages: ${ticket.messages.size} turns", fontSize = 10.sp, color = Color.Gray)
                            Button(
                                onClick = { selectedTicketForDetail = ticket },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Founder Reply & Manage ✍️", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedTicketForDetail?.let { ticket ->
        FounderTicketDetailDialog(
            repository = repository,
            ticket = ticket,
            onDismiss = { selectedTicketForDetail = null }
        )
    }
}

@Composable
fun FounderTicketDetailDialog(
    repository: SatnaEatsRepository,
    ticket: SupportTicket,
    onDismiss: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(ticket.assignedToRole) }

    val roleOptions = listOf(
        Pair("FOUNDER", "Founder Shivank Dwivedi"),
        Pair("SUPPORT_STAFF", "Satna Support Executive"),
        Pair("RESTAURANT", "Restaurant Owner"),
        Pair("RIDER", "Delivery Partner")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage: ${ticket.ticketNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Customer: ${ticket.customerName} • ${ticket.customerPhone}", fontSize = 11.sp, color = Color.Gray)
                Text("Subject: ${ticket.subject}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(6.dp))
                LazyColumn(
                    modifier = Modifier
                        .height(200.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ticket.messages) { msg ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (msg.senderRole == "FOUNDER_ADMIN") Color(0xFFFFF8E1) else Color.White, RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Text("${msg.senderName} (${msg.senderRole}) • ${msg.timestamp}", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text(msg.message, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Founder Official Reply", fontSize = 11.sp) },
                    placeholder = { Text("Type official response to customer...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text("Reassign Ticket To:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(roleOptions) { option ->
                        val role = option.first
                        val name = option.second
                        FilterChip(
                            selected = selectedRole == role,
                            onClick = {
                                selectedRole = role
                                repository.updateTicketStatus(ticket.id, ticket.status, assignedToRole = role, assignedToName = name)
                            },
                            label = { Text(name, fontSize = 9.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            repository.addMessageToSupportTicket(
                                ticketId = ticket.id,
                                senderRole = "FOUNDER_ADMIN",
                                senderName = "Shivank Dwivedi (Founder)",
                                messageText = replyText
                            )
                        }
                        repository.updateTicketStatus(ticket.id, TicketStatus.RESOLVED)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                ) {
                    Text("Post & Mark Resolved ✓", fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
