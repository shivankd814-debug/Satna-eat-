package com.example.ui.screens.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.payment.RazorpayPaymentSheet
import com.example.payment.TaxInvoiceDialog
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CartCheckoutScreen(
    repository: SatnaEatsRepository,
    onBackClick: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val cartItems by repository.cartItems.collectAsState()
    val appliedCoupon by repository.appliedCoupon.collectAsState()
    val selectedAddress by repository.selectedAddress.collectAsState()
    val coupons by repository.coupons.collectAsState()
    val platformSettings by repository.platformSettings.collectAsState()

    var couponCodeInput by remember { mutableStateOf("") }
    var couponErrorMsg by remember { mutableStateOf<String?>(null) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.RAZORPAY) }

    var showAddressModal by remember { mutableStateOf(false) }
    var showRazorpaySheet by remember { mutableStateOf(false) }

    val itemTotal = cartItems.sumOf { it.totalPrice }
    val deliveryFee = if (itemTotal >= platformSettings.freeDeliveryAbove) 0.0 else platformSettings.baseDeliveryFee
    val packagingGst = 18.0

    val discount = appliedCoupon?.let { coupon ->
        val calc = (itemTotal * coupon.discountPercentage) / 100.0
        kotlin.math.min(calc, coupon.maxDiscountAmount)
    } ?: 0.0

    val grandTotal = (itemTotal + deliveryFee + packagingGst - discount).coerceAtLeast(0.0)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checkout & Cart", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Cart Items List
        items(cartItems) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VegNonVegBadge(isVeg = item.menuItem.isVeg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(item.menuItem.name, fontWeight = FontWeight.Bold)
                        }
                        item.selectedVariant?.let { v ->
                            Text("Variant: ${v.name}", fontSize = 11.sp, color = Color.Gray)
                        }
                        if (item.selectedToppings.isNotEmpty()) {
                            Text("Toppings: ${item.selectedToppings.joinToString { it.name }}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text("₹${item.totalPrice.toInt()}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Surface(
                        color = SatnaVegGreen,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { repository.updateCartQuantity(item.id, item.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                                Text("-", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Text("${item.quantity}", color = Color.White, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { repository.updateCartQuantity(item.id, item.quantity + 1) }, modifier = Modifier.size(28.dp)) {
                                Text("+", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Delivery Address Card
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = SatnaOrangePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delivery Address (${selectedAddress.label})", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = { showAddressModal = true }) {
                            Text("Change")
                        }
                    }
                    Text(selectedAddress.fullAddress, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (selectedAddress.landmark.isNotBlank()) {
                        Text("Landmark: ${selectedAddress.landmark}", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Coupon Code Manager
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Apply Promo Coupon 🏷️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = couponCodeInput,
                            onValueChange = { couponCodeInput = it.uppercase() },
                            placeholder = { Text("Enter SATNA50") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val match = coupons.find { it.code.equals(couponCodeInput.trim(), ignoreCase = true) }
                                if (match != null) {
                                    val ok = repository.applyCoupon(match)
                                    if (ok) couponErrorMsg = null else couponErrorMsg = "Min order value for ${match.code} is ₹${match.minOrderValue.toInt()}"
                                } else {
                                    couponErrorMsg = "Invalid Coupon Code"
                                }
                            }
                        ) {
                            Text("Apply")
                        }
                    }

                    appliedCoupon?.let { coupon ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = SatnaVegGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Coupon '${coupon.code}' Applied! Saved ₹${discount.toInt()}", color = SatnaVegGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                TextButton(onClick = { repository.removeCoupon() }) {
                                    Text("Remove", color = SatnaNonVegRed, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    couponErrorMsg?.let { err ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(err, color = SatnaNonVegRed, fontSize = 12.sp)
                    }
                }
            }
        }

        // Bill Summary
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Bill Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Item Total", fontSize = 13.sp)
                        Text("₹${itemTotal.toInt()}", fontSize = 13.sp)
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Delivery Partner Fee (Satna)", fontSize = 13.sp)
                        if (deliveryFee == 0.0) {
                            Text("FREE (Above ₹199)", color = SatnaVegGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        } else {
                            Text("₹${deliveryFee.toInt()}", fontSize = 13.sp)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Restaurant Packaging & Taxes", fontSize = 13.sp)
                        Text("₹${packagingGst.toInt()}", fontSize = 13.sp)
                    }
                    if (discount > 0) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Coupon Discount", fontSize = 13.sp, color = SatnaVegGreen)
                            Text("-₹${discount.toInt()}", fontSize = 13.sp, color = SatnaVegGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("To Pay", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Payment Gateway Selector
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Select Payment Gateway 💳", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    val options = listOf(
                        PaymentMethod.RAZORPAY to "Razorpay (Cards, NetBanking, All UPI)",
                        PaymentMethod.UPI_PHONEPE to "PhonePe / Google Pay Direct UPI",
                        PaymentMethod.CASH_ON_DELIVERY to "Cash on Delivery (COD)"
                    )

                    options.forEach { (method, title) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = method }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(title, fontSize = 13.sp, fontWeight = if (selectedPaymentMethod == method) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }
        }

        // Place Order Button
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
                        val order = repository.placeOrder(selectedPaymentMethod)
                        if (order != null) onOrderPlaced()
                    } else {
                        showRazorpaySheet = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
            ) {
                Text(
                    if (selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY) "PLACE COD ORDER (₹${grandTotal.toInt()}) 🚀"
                    else "PROCEED TO RAZORPAY PAY (₹${grandTotal.toInt()}) 🔒",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRazorpaySheet) {
        val currentUser by repository.currentUser.collectAsState()
        RazorpayPaymentSheet(
            amountInr = grandTotal,
            customerName = currentUser.name,
            customerPhone = currentUser.phone,
            onPaymentSuccess = { method, orderId, paymentId, sig ->
                showRazorpaySheet = false
                val order = repository.placeOrder(method, Triple(orderId, paymentId, sig))
                if (order != null) onOrderPlaced()
            },
            onDismiss = { showRazorpaySheet = false }
        )
    }
}

@Composable
fun LiveOrderTrackingScreen(
    repository: SatnaEatsRepository,
    onBackClick: () -> Unit
) {
    val activeOrder by repository.activeOrder.collectAsState()

    activeOrder?.let { order ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Live Order Tracking", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Order #${order.orderNumber} • ${order.createdAt}", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Live Map
            item {
                SatnaMapCanvas(
                    restaurantName = order.restaurantName,
                    customerAddress = order.deliveryAddress.label,
                    riderName = order.riderName ?: "Ramesh Sahu",
                    statusText = order.status.name.replace("_", " ")
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Delivery OTP Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SatnaOrangeContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Delivery Verification OTP", fontWeight = FontWeight.Bold, color = SatnaOrangeDark, fontSize = 13.sp)
                            Text("Share this 4-digit code with delivery partner upon arrival", fontSize = 11.sp, color = SatnaOrangeDark.copy(alpha = 0.9f))
                        }
                        Surface(color = SatnaOrangeDark, shape = RoundedCornerShape(8.dp)) {
                            Text(order.deliveryOtp, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Status Timeline
            item {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Order Status", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        val statuses = listOf(
                            OrderStatus.PLACED to "Order Placed & Confirmed",
                            OrderStatus.ACCEPTED to "Restaurant Accepted",
                            OrderStatus.COOKING to "Food Being Prepared",
                            OrderStatus.OUT_FOR_DELIVERY to "Out For Delivery with Rider",
                            OrderStatus.DELIVERED to "Delivered to Your Doorstep"
                        )

                        statuses.forEach { (st, label) ->
                            val isCompleted = order.status.ordinal >= st.ordinal
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                                Icon(
                                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isCompleted) SatnaVegGreen else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontSize = 13.sp, fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Rider Contact Info
            item {
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(36.dp), tint = SatnaTealSecondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(order.riderName ?: "Ramesh Sahu", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Delivery Partner • Satna", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = { /* Call Phone */ }) {
                            Icon(Icons.Default.Call, contentDescription = "Call Rider", tint = SatnaVegGreen)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No active order to track", fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomerProfileScreen(
    repository: SatnaEatsRepository,
    onLanguageToggle: () -> Unit
) {
    val currentUser by repository.currentUser.collectAsState()
    val orderHistory by repository.orderHistory.collectAsState()
    val favoriteIds by repository.favoriteRestaurantIds.collectAsState()
    val restaurants by repository.restaurants.collectAsState()

    var showAuthModal by remember { mutableStateOf(false) }
    var showAddAddressModal by remember { mutableStateOf(false) }
    var showReviewModalForOrder by remember { mutableStateOf<Order?>(null) }
    var showInvoiceForOrder by remember { mutableStateOf<Order?>(null) }
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    val favoriteRestaurants = restaurants.filter { favoriteIds.contains(it.id) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Profile Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SatnaTealContainer),
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
                                Text(currentUser.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = SatnaTealSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = SatnaVegGreen,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("VERIFIED 🔒", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                }
                            }
                            Text("Phone: ${currentUser.phone} • Satna, MP", fontSize = 13.sp, color = SatnaTealSecondary)
                            Text("Email: ${currentUser.email}", fontSize = 12.sp, color = SatnaTealSecondary.copy(alpha = 0.8f))
                        }

                        IconButton(onClick = { showAuthModal = true }) {
                            Icon(Icons.Default.VpnKey, contentDescription = "OTP / Google Login", tint = SatnaTealSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showAuthModal = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Account / +91 India OTP Login", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick App Settings & Preferences
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("App Preferences", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = SatnaOrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("App Language (${if (currentUser.language == "EN") "English" else "हिंदी"})", fontSize = 13.sp)
                        }
                        TextButton(onClick = onLanguageToggle) {
                            Text("Switch to ${if (currentUser.language == "EN") "हिंदी" else "English"}")
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = SatnaOrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FCM Delivery Order Push Alerts", fontSize = 13.sp)
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = SatnaOrangePrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Dark Theme Mode", fontSize = 13.sp)
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Saved Delivery Addresses
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Saved Delivery Addresses (${currentUser.addresses.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                TextButton(onClick = { showAddAddressModal = true }) {
                    Text("+ Add Address")
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        items(currentUser.addresses) { addr ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = SatnaOrangePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(addr.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(addr.fullAddress, fontSize = 11.sp, color = Color.Gray)
                        if (addr.landmark.isNotBlank()) {
                            Text("Landmark: ${addr.landmark}", fontSize = 10.sp, color = SatnaOrangeDark)
                        }
                    }
                    if (repository.selectedAddress.value.id == addr.id) {
                        Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                            Text("SELECTED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    } else {
                        TextButton(onClick = { repository.setSelectedAddress(addr) }) {
                            Text("Select", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Favorite / Wishlist Restaurants
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Wishlist & Favorite Restaurants (${favoriteRestaurants.size}) ❤️", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (favoriteRestaurants.isEmpty()) {
            item {
                Text("No favorite restaurants saved yet in Satna.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(favoriteRestaurants) { favRst ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(favRst.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${favRst.deliveryTimeMinutes} mins • ⭐ ${favRst.rating} • ${favRst.address}", fontSize = 11.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { repository.toggleFavoriteRestaurant(favRst.id) }) {
                            Icon(Icons.Default.Favorite, contentDescription = "Remove", tint = SatnaNonVegRed)
                        }
                    }
                }
            }
        }

        // Order History Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Order History & Invoices (${orderHistory.size}) 📜", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
        }

        if (orderHistory.isEmpty()) {
            item {
                Text("No past orders placed yet.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(orderHistory) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Text(order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${order.grandTotal.toInt()}", fontWeight = FontWeight.ExtraBold, color = SatnaOrangeDark)
                        }
                        Text("Order #${order.orderNumber} • ${order.createdAt}", fontSize = 11.sp, color = Color.Gray)
                        Text("Items: ${order.items.joinToString { "${it.quantity}x ${it.menuItem.name}" }}", fontSize = 12.sp, maxLines = 2)

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = SatnaTealSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${order.paymentMethod.name} • Razorpay Verified ✓", fontSize = 10.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold)
                        }

                        if (order.paymentStatus == PaymentStatus.REFUNDED) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(color = SatnaNonVegRed.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(
                                    "REFUNDED ₹${order.refundAmount.toInt()} (ID: ${order.refundId ?: "N/A"})",
                                    fontSize = 10.sp,
                                    color = SatnaNonVegRed,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (order.paymentStatus == PaymentStatus.REFUNDED) SatnaNonVegRed.copy(alpha = 0.15f) else SatnaVegGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    if (order.paymentStatus == PaymentStatus.REFUNDED) "CANCELLED & REFUNDED" else "Status: ${order.status.name}",
                                    fontSize = 10.sp,
                                    color = if (order.paymentStatus == PaymentStatus.REFUNDED) SatnaNonVegRed else SatnaVegGreen,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { showInvoiceForOrder = order },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tax Invoice", fontSize = 10.sp)
                                }

                                if (order.paymentStatus == PaymentStatus.SUCCESS && order.status != OrderStatus.DELIVERED) {
                                    Button(
                                        onClick = { repository.processRefund(order.id, "Customer requested cancellation") },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SatnaNonVegRed),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Refund 💸", fontSize = 10.sp, color = Color.White)
                                    }
                                } else {
                                    Button(
                                        onClick = { showReviewModalForOrder = order },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Rate ⭐", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal 1: +91 India Phone OTP & Google Login
    if (showAuthModal) {
        IndiaOtpAuthModal(
            onDismiss = { showAuthModal = false },
            onAuthSuccess = { phoneOrEmail ->
                showAuthModal = false
            }
        )
    }

    // Modal 2: Add New Address
    if (showAddAddressModal) {
        AddNewAddressModal(
            onDismiss = { showAddAddressModal = false },
            onAddressAdded = { newAddr ->
                repository.addAddress(newAddr)
                showAddAddressModal = false
            }
        )
    }

    // Modal 3: Rate & Review Order
    showReviewModalForOrder?.let { order ->
        OrderReviewModal(
            order = order,
            onDismiss = { showReviewModalForOrder = null },
            onSubmitReview = { rating, reviewText ->
                showReviewModalForOrder = null
            }
        )
    }

    // Modal 4: Tax Invoice Dialog
    showInvoiceForOrder?.let { order ->
        TaxInvoiceDialog(
            order = order,
            onDismiss = { showInvoiceForOrder = null }
        )
    }
}

@Composable
fun IndiaOtpAuthModal(
    onDismiss: () -> Unit,
    onAuthSuccess: (String) -> Unit
) {
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var otpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("485001") }
    var authError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Satna Eats Firebase Auth 🔑", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Login or Register using India (+91) OTP or Google Account:", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                if (!otpSent) {
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("India Phone Number (+91)") },
                        leadingIcon = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length >= 10) {
                                otpSent = true
                                authError = null
                            } else {
                                authError = "Please enter valid 10-digit mobile number"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                    ) {
                        Text("SEND 6-DIGIT OTP")
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("--- OR ---", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onAuthSuccess("shivank@satnaeats.com") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🌐 Sign in with Google Account")
                    }
                } else {
                    Text("OTP sent to +91 $phoneNumber. Check SMS.", fontSize = 12.sp, color = SatnaVegGreen)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("Enter 6-digit OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (otpCode.length == 6) {
                                onAuthSuccess("+91 $phoneNumber")
                            } else {
                                authError = "Invalid OTP code"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
                    ) {
                        Text("VERIFY OTP & LOGIN")
                    }
                }

                authError?.let { err ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(err, color = SatnaNonVegRed, fontSize = 11.sp)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddNewAddressModal(
    onDismiss: () -> Unit,
    onAddressAdded: (Address) -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var fullAddress by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("485001") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Delivery Address in Satna 📍") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Address Label (Home / Work / Other)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = fullAddress,
                    onValueChange = { fullAddress = it },
                    label = { Text("House/Flat No., Building, Street") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = landmark,
                    onValueChange = { landmark = it },
                    label = { Text("Landmark (e.g. Near Circuit House)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = pincode,
                    onValueChange = { pincode = it },
                    label = { Text("Pincode") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fullAddress.isNotBlank()) {
                        val newAddress = Address(
                            id = "addr_${System.currentTimeMillis()}",
                            label = label,
                            fullAddress = fullAddress,
                            landmark = landmark,
                            city = "Satna",
                            state = "Madhya Pradesh",
                            pincode = pincode
                        )
                        onAddressAdded(newAddress)
                    }
                }
            ) {
                Text("Save Address")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OrderReviewModal(
    order: Order,
    onDismiss: () -> Unit,
    onSubmitReview: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rate & Review ${order.restaurantName}") },
        text = {
            Column {
                Text("Order #${order.orderNumber}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Rating: $rating / 5 Stars ⭐", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star Stars",
                                tint = SatnaOrangePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Write your review for food quality & delivery...") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReview(rating, reviewText) },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaVegGreen)
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

