package com.example.ui.screens.rider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.data.repository.SatnaEatsRepository
import com.example.ui.components.SatnaMapCanvas
import com.example.ui.theme.*

@Composable
fun RiderDashboardScreen(
    repository: SatnaEatsRepository
) {
    val riders by repository.riders.collectAsState()
    val activeOrder by repository.activeOrder.collectAsState()
    val orderHistory by repository.orderHistory.collectAsState()

    val rider = riders.firstOrNull() ?: Rider(
        id = "rdr_1",
        name = "Ramesh Sahu",
        phone = "+91 9425123456"
    )

    var isOnline by remember { mutableStateOf(rider.isOnline) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Live Navigation & Trips, 1: Earnings & Wallet, 2: KYC & Vehicle, 3: Ratings & Alerts

    var showOtpModal by remember { mutableStateOf(false) }
    var showEditProfileModal by remember { mutableStateOf(false) }
    var showRejectReasonModal by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("Vehicle fuel/charging low") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Rider Header Bar
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
                            Text(rider.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SatnaTealSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                                Text("VERIFIED RIDER ✓", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text("${rider.vehicleModel} (${rider.bikeNumber}) • Satna, MP", fontSize = 12.sp, color = SatnaTealSecondary)
                        Text("DL: ${rider.licenseNumber} • Rating: ⭐ ${rider.rating}", fontSize = 11.sp, color = SatnaTealSecondary.copy(alpha = 0.85f))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (isOnline) "DUTY ON" else "OFFLINE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isOnline) SatnaVegGreen else SatnaNonVegRed
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isOnline,
                                onCheckedChange = {
                                    isOnline = it
                                    repository.updateRider(rider.copy(isOnline = it))
                                }
                            )
                        }

                        TextButton(
                            onClick = { showOtpModal = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("OTP Verification", fontSize = 11.sp, color = SatnaOrangePrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Navigation Tabs
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Trips & Map", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Text("Earnings", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                Text("KYC & Bike", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                Text("Ratings & Alerts", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            0 -> LiveTripsAndNavigationTab(
                repository = repository,
                rider = rider,
                activeOrder = activeOrder,
                isOnline = isOnline,
                onRejectClick = { showRejectReasonModal = true }
            )
            1 -> EarningsAndPayoutsTab(rider = rider, orders = orderHistory)
            2 -> KycAndVehicleTab(rider = rider, onEditClick = { showEditProfileModal = true })
            3 -> RatingsAndAlertsTab(rider = rider)
        }
    }

    // Modal 1: OTP Login / Phone Verification
    if (showOtpModal) {
        RiderOtpVerificationModal(
            phone = rider.phone,
            onDismiss = { showOtpModal = false }
        )
    }

    // Modal 2: Edit Rider Profile & Documents
    if (showEditProfileModal) {
        RiderProfileModal(
            existingRider = rider,
            onDismiss = { showEditProfileModal = false },
            onSave = { updated ->
                repository.updateRider(updated)
                showEditProfileModal = false
            }
        )
    }

    // Modal 3: Reject Order Reason
    if (showRejectReasonModal && activeOrder != null) {
        AlertDialog(
            onDismissRequest = { showRejectReasonModal = false },
            title = { Text("Reject Delivery Order Request?") },
            text = {
                Column {
                    Text("Select a reason for declining this order in Satna:")
                    Spacer(modifier = Modifier.height(8.dp))
                    val reasons = listOf("Vehicle fuel/charging low", "Customer location outside my area", "Heavy Traffic at Semariya Chowk", "Personal break time")
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { rejectReason = r }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = (rejectReason == r), onClick = { rejectReason = r })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(r, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeOrder?.let { repository.updateOrderStatus(it.id, OrderStatus.CANCELLED) }
                        showRejectReasonModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaNonVegRed)
                ) {
                    Text("Decline Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectReasonModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun LiveTripsAndNavigationTab(
    repository: SatnaEatsRepository,
    rider: Rider,
    activeOrder: Order?,
    isOnline: Boolean,
    onRejectClick: () -> Unit
) {
    var deliveryOtpInput by remember { mutableStateOf("") }
    var simNavStep by remember { mutableStateOf("Head straight on Circuit House Road towards Semariya Chowk (1.2 km)") }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // GPS & Duty Status Indicator
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isOnline) SatnaVegGreen.copy(alpha = 0.12f) else SatnaOrangeContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = if (isOnline) SatnaVegGreen else SatnaOrangeDark,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            if (isOnline) "Live GPS Active (Satna, MP)" else "Rider Offline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Text("Accuracy: ±3m • Lat: ${rider.currentLat}, Lng: ${rider.currentLng}", fontSize = 10.sp, color = Color.Gray)
                    }
                }

                Surface(
                    color = if (isOnline) SatnaVegGreen else Color.Gray,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        if (isOnline) "SEARCHING TRIPS" else "OFF DUTY",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeOrder == null) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Moped,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = SatnaOrangePrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No Active Trip Right Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Stay in high-demand areas like Civil Lines or Bus Stand Road to get instant order requests in Satna.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Trip #${activeOrder.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = SatnaOrangeContainer, shape = RoundedCornerShape(4.dp)) {
                                Text(activeOrder.status.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }

                        Text("Payout: ₹55.00", fontWeight = FontWeight.ExtraBold, color = SatnaVegGreen, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pickup & Drop Locations
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = SatnaTealSecondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PICKUP: ${activeOrder.restaurantName}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Home, contentDescription = null, tint = SatnaVegGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("DROP: ${activeOrder.customerName} - ${activeOrder.deliveryAddress.fullAddress}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Simulated Google Maps Route Navigation Canvas
                    SatnaMapCanvas(
                        restaurantName = activeOrder.restaurantName,
                        customerAddress = activeOrder.deliveryAddress.label,
                        riderName = rider.name,
                        statusText = activeOrder.status.name
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Navigation Banner
                    Surface(color = SatnaTealSecondary, shape = RoundedCornerShape(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Google Maps Live Turn-by-Turn", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(simNavStep, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeOrder.status == OrderStatus.PLACED || activeOrder.status == OrderStatus.COOKING) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onRejectClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SatnaNonVegRed)
                            ) {
                                Text("Reject Trip ❌", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    repository.updateOrderStatus(activeOrder.id, OrderStatus.READY_FOR_PICKUP)
                                    simNavStep = "Picked up order! Arrive at customer location in 8 mins (2.1 km)"
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                            ) {
                                Text("Accept & Pick Up 📦", fontSize = 12.sp)
                            }
                        }
                    } else if (activeOrder.status == OrderStatus.READY_FOR_PICKUP) {
                        Button(
                            onClick = {
                                repository.updateOrderStatus(activeOrder.id, OrderStatus.OUT_FOR_DELIVERY)
                                simNavStep = "Out for delivery! On way to ${activeOrder.customerName}"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                        ) {
                            Text("Start Journey to Customer 🚀", fontSize = 13.sp)
                        }
                    } else if (activeOrder.status == OrderStatus.OUT_FOR_DELIVERY) {
                        Column {
                            Text("Enter 4-Digit Customer OTP to complete delivery:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = deliveryOtpInput,
                                    onValueChange = { deliveryOtpInput = it },
                                    placeholder = { Text("Customer OTP e.g. 4850") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = {
                                        if (deliveryOtpInput == activeOrder.deliveryOtp || deliveryOtpInput == "4850" || deliveryOtpInput.length == 4) {
                                            repository.updateOrderStatus(activeOrder.id, OrderStatus.DELIVERED)
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                                ) {
                                    Text("Verify & Delivered ✓")
                                }
                            }
                        }
                    } else {
                        Surface(color = SatnaVegGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                            Text("TRIP DELIVERED SUCCESSFULLY! ₹55 ADDED TO WALLET 🎉", color = SatnaVegGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EarningsAndPayoutsTab(rider: Rider, orders: List<Order>) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SatnaVegGreen.copy(alpha = 0.12f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rider Earnings Summary 💸", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Today", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${rider.dailyEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SatnaVegGreen)
                    }
                    Column {
                        Text("This Week", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${rider.weeklyEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("This Month", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${rider.monthlyEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Withdrawable Wallet Balance", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${rider.walletBalance.toInt()}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Button(
                        onClick = { /* Instant Bank Payout */ },
                        colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary)
                    ) {
                        Text("Instant Payout to Bank")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Satna Incentive Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Base Trip Payouts:", fontSize = 12.sp)
                    Text("₹${(rider.dailyEarnings * 0.75).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Peak Hour Surge Bonus:", fontSize = 12.sp)
                    Text("₹${(rider.dailyEarnings * 0.15).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaOrangePrimary)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Customer Tips Received:", fontSize = 12.sp)
                    Text("₹${(rider.dailyEarnings * 0.10).toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaVegGreen)
                }
            }
        }
    }
}

@Composable
fun KycAndVehicleTab(rider: Rider, onEditClick: () -> Unit) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Driving Licence & Verification 🆔", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text("Licence No: ${rider.licenseNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("DL Document: Verified ✓", fontSize = 12.sp, color = SatnaVegGreen)
                Text("Registered Mobile: ${rider.phone} (OTP Verified)", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Bike / Vehicle Details 🛵", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Vehicle Model: ${rider.vehicleModel}", fontSize = 12.sp)
                Text("Registration Number: ${rider.bikeNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("RC Certificate: ${rider.vehicleRcNumber} (Approved ✓)", fontSize = 12.sp, color = SatnaVegGreen)
                Text("Insurance & Pollution: Valid until 2027", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Bank Account Details for Payouts 🏦", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Account Holder: ${rider.bankHolderName}", fontSize = 12.sp)
                Text("Account Number: ${rider.bankAccount}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("IFSC Code: ${rider.bankIfsc}", fontSize = 12.sp)
                Text("Bank Name: ${rider.bankName}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun RatingsAndAlertsTab(rider: Rider) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SatnaOrangeContainer),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Rider Performance & Rating ⭐", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${rider.rating}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = SatnaOrangeDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Out of 5.0 Rating", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Based on ${rider.totalDeliveries} completed orders in Satna", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = SatnaVegGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
                        Text("On-Time 98%", fontSize = 11.sp, color = SatnaVegGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                    Surface(color = SatnaTealContainer, shape = RoundedCornerShape(16.dp)) {
                        Text("Safe Driving 100%", fontSize = 11.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Rider Alerts & Notifications 🔔", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(6.dp))

        val alerts = listOf(
            "🎉 Weekend Satna Surge: Earn extra ₹15 per delivery during 7 PM - 10 PM!",
            "✅ Daily Payout of ₹450 processed to your SBI Satna account.",
            "⭐ Customer left a 5-Star review: 'Very polite rider and warm food!'",
            "📋 Vehicle RC document re-verified successfully by Satna Eats Admin."
        )

        alerts.forEach { alert ->
            Card(shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(alert, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun RiderOtpVerificationModal(
    phone: String,
    onDismiss: () -> Unit
) {
    var otpInput by remember { mutableStateOf("") }
    var isVerified by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rider OTP Login Verification 📲") },
        text = {
            Column {
                Text("OTP sent to registered partner mobile number $phone in Satna.")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = otpInput,
                    onValueChange = { if (it.length <= 6) otpInput = it },
                    label = { Text("Enter 6-Digit OTP (Try 123456)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isVerified) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✓ OTP VERIFIED! LOGIN SUCCESSFUL", color = SatnaVegGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isVerified = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Text("Verify OTP")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun RiderProfileModal(
    existingRider: Rider,
    onDismiss: () -> Unit,
    onSave: (Rider) -> Unit
) {
    var name by remember { mutableStateOf(existingRider.name) }
    var phone by remember { mutableStateOf(existingRider.phone) }
    var bikeNumber by remember { mutableStateOf(existingRider.bikeNumber) }
    var vehicleModel by remember { mutableStateOf(existingRider.vehicleModel) }
    var licenseNumber by remember { mutableStateOf(existingRider.licenseNumber) }
    var bankAccount by remember { mutableStateOf(existingRider.bankAccount) }
    var bankIfsc by remember { mutableStateOf(existingRider.bankIfsc) }
    var bankName by remember { mutableStateOf(existingRider.bankName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rider Profile & Documents") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Rider Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bikeNumber, onValueChange = { bikeNumber = it }, label = { Text("Bike Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = vehicleModel, onValueChange = { vehicleModel = it }, label = { Text("Vehicle Model") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = licenseNumber, onValueChange = { licenseNumber = it }, label = { Text("Driving Licence No.") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankAccount, onValueChange = { bankAccount = it }, label = { Text("Bank Account No.") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankIfsc, onValueChange = { bankIfsc = it }, label = { Text("IFSC Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = bankName, onValueChange = { bankName = it }, label = { Text("Bank Name") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        existingRider.copy(
                            name = name,
                            phone = phone,
                            bikeNumber = bikeNumber,
                            vehicleModel = vehicleModel,
                            licenseNumber = licenseNumber,
                            bankAccount = bankAccount,
                            bankIfsc = bankIfsc,
                            bankName = bankName
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
