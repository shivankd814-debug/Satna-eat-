package com.example.ui.screens.customer

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.models.*
import com.example.data.repository.SatnaEatsRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun CustomerHomeScreen(
    repository: SatnaEatsRepository,
    onRestaurantClick: (Restaurant) -> Unit,
    onCartClick: () -> Unit,
    onOrderTrackingClick: () -> Unit
) {
    val restaurants by repository.restaurants.collectAsState()
    val menuItems by repository.menuItems.collectAsState()
    val cartItems by repository.cartItems.collectAsState()
    val activeOrder by repository.activeOrder.collectAsState()
    val favorites by repository.favoriteRestaurantIds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var pureVegFilter by remember { mutableStateOf(false) }

    val categories = listOf("All", "Biryani", "Thali", "Pizza", "Samosa/Chaat", "North Indian", "South Indian", "Sweets")

    val filteredRestaurants = restaurants.filter { r ->
        val matchesCategory = if (selectedCategory == "All") true else r.categories.contains(selectedCategory)
        val matchesVeg = if (pureVegFilter) r.isPureVeg else true
        val matchesSearch = if (searchQuery.isBlank()) true else {
            r.name.contains(searchQuery, ignoreCase = true) ||
                    r.tagline.contains(searchQuery, ignoreCase = true) ||
                    r.categories.any { it.contains(searchQuery, ignoreCase = true) }
        }
        matchesCategory && matchesVeg && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartItems.isNotEmpty() || activeOrder != null) 80.dp else 16.dp)
        ) {
            // Live Order Banner if active
            activeOrder?.let { order ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOrderTrackingClick() }
                            .padding(12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsBike,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Active Order #${order.orderNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "Status: ${order.status.name.replace("_", " ")} • Tap to Track Live 📍",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }
            }

            // Real GPS Location Header
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                val selectedAddress by repository.selectedAddress.collectAsState()

                Card(
                    colors = CardDefaults.cardColors(containerColor = SatnaOrangeContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MyLocation, contentDescription = null, tint = SatnaOrangeDark)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("DELIVERING TO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SatnaOrangeDark)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(color = SatnaVegGreen, shape = RoundedCornerShape(4.dp)) {
                                        Text("GPS LIVE", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                    }
                                }
                                Text(selectedAddress.fullAddress, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaOrangeDark, maxLines = 1)
                                Text("Lat: ${selectedAddress.latitude}, Lng: ${selectedAddress.longitude}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = {
                                val (lat, lng) = com.example.maps.LocationAndMapsManager.getRealGpsLocation(context)
                                repository.updateCustomerCoordinates(lat, lng, "Circuit House Road, Satna (Real GPS)")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangeDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use Real GPS", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.height(160.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_satna),
                            contentDescription = "Satna Eats Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = SatnaOrangePrimary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FOUNDER: SHIVANK DWIVEDI",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Satna's #1 Food Delivery",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hot, fresh meals delivered in 20 mins across Satna 🚀",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Satna Treat, Biryani, Samosa...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Coupon Offer Strip
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Surface(
                            color = SatnaTealContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🏷️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Use Code: SATNA50", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaTealSecondary)
                                    Text("50% OFF up to ₹100 on orders > ₹149", fontSize = 10.sp, color = SatnaTealSecondary)
                                }
                            }
                        }
                    }
                    item {
                        Surface(
                            color = SatnaOrangeContainer,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Code: WELCOME100", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = SatnaOrangeDark)
                                    Text("Flat ₹120 OFF for Satna foodies", fontSize = 10.sp, color = SatnaOrangeDark)
                                }
                            }
                        }
                    }
                }
            }

            // Categories Filter Bar
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (selectedCategory == cat) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            selected = pureVegFilter,
                            onClick = { pureVegFilter = !pureVegFilter },
                            label = { Text("Pure Veg 🥦") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SatnaVegGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Restaurants Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Restaurants in Satna (${filteredRestaurants.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Live GPS Radius 8 km",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Restaurant Items
            items(filteredRestaurants) { restaurant ->
                RestaurantCard(
                    restaurant = restaurant,
                    isFavorite = favorites.contains(restaurant.id),
                    onFavoriteToggle = { repository.toggleFavoriteRestaurant(restaurant.id) },
                    onClick = { onRestaurantClick(restaurant) }
                )
            }
        }

        // Floating Cart Bar
        if (cartItems.isNotEmpty()) {
            val totalItems = cartItems.sumOf { it.quantity }
            val totalPrice = cartItems.sumOf { it.totalPrice }

            Surface(
                color = SatnaVegGreen,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onCartClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "$totalItems ITEM${if (totalItems > 1) "S" else ""} • ₹${totalPrice.toInt()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Satna Eats Instant Checkout",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "View Cart",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_satna),
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                // Pure Veg Badge
                if (restaurant.isPureVeg) {
                    Surface(
                        color = SatnaVegGreen,
                        shape = RoundedCornerShape(bottomEnd = 8.dp),
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "PURE VEG 🥦",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Favorite Icon
                IconButton(
                    onClick = onFavoriteToggle,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) SatnaNonVegRed else Color.White
                    )
                }

                // Delivery Time Pill
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${restaurant.deliveryTimeMinutes} mins • ${restaurant.distanceKm} km",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = restaurant.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    RatingChip(rating = restaurant.rating, totalRatings = restaurant.totalRatings)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = restaurant.tagline,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📍 ${restaurant.address}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    repository: SatnaEatsRepository,
    onBackClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val allMenuItems by repository.menuItems.collectAsState()
    val cartItems by repository.cartItems.collectAsState()

    val restaurantMenu = allMenuItems.filter { it.restaurantId == restaurant.id }
    var searchMenuQuery by remember { mutableStateOf("") }
    var isVegOnly by remember { mutableStateOf(false) }

    var selectedItemForCustomization by remember { mutableStateOf<MenuItem?>(null) }

    val filteredMenu = restaurantMenu.filter { item ->
        val matchesVeg = if (isVegOnly) item.isVeg else true
        val matchesSearch = if (searchMenuQuery.isBlank()) true else item.name.contains(searchMenuQuery, ignoreCase = true)
        matchesVeg && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (cartItems.isNotEmpty()) 80.dp else 16.dp)
        ) {
            item {
                Box(modifier = Modifier.height(180.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_satna),
                        contentDescription = restaurant.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f))
                    )
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(restaurant.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(restaurant.tagline, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RatingChip(rating = restaurant.rating)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${restaurant.deliveryTimeMinutes} mins • ${restaurant.distanceKm} km", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("FSSAI Lic. #${restaurant.fssaiLicense}", fontSize = 10.sp, color = Color.Gray)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Veg Only 🥦", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = isVegOnly,
                                onCheckedChange = { isVegOnly = it }
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchMenuQuery,
                    onValueChange = { searchMenuQuery = it },
                    placeholder = { Text("Search in ${restaurant.name}...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            items(filteredMenu) { item ->
                val cartQuantity = cartItems.filter { it.menuItem.id == item.id }.sumOf { it.quantity }
                MenuItemRow(
                    item = item,
                    cartQuantity = cartQuantity,
                    onAddClick = {
                        if (item.variants.isNotEmpty() || item.extraToppings.isNotEmpty()) {
                            selectedItemForCustomization = item
                        } else {
                            repository.addToCart(item)
                        }
                    },
                    onIncrease = { repository.addToCart(item) },
                    onDecrease = {
                        val cartItem = cartItems.find { it.menuItem.id == item.id }
                        cartItem?.let { repository.updateCartQuantity(it.id, it.quantity - 1) }
                    }
                )
            }
        }

        // Customization Dialog
        selectedItemForCustomization?.let { item ->
            FoodCustomizationDialog(
                menuItem = item,
                onDismiss = { selectedItemForCustomization = null },
                onAddToCart = { variant, toppings ->
                    repository.addToCart(item, variant, toppings)
                    selectedItemForCustomization = null
                }
            )
        }

        // Bottom Cart Bar
        if (cartItems.isNotEmpty()) {
            val totalItems = cartItems.sumOf { it.quantity }
            val totalPrice = cartItems.sumOf { it.totalPrice }

            Surface(
                color = SatnaVegGreen,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { onCartClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("$totalItems ITEMS • ₹${totalPrice.toInt()}", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("View Cart →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItem,
    cartQuantity: Int,
    onAddClick: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    VegNonVegBadge(isVeg = item.isVeg)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${item.price.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    item.originalPrice?.let { orig ->
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("₹${orig.toInt()}", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 1.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (cartQuantity == 0) {
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
                ) {
                    Text("ADD +", fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    color = SatnaVegGreen,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = onDecrease, modifier = Modifier.size(28.dp)) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text("$cartQuantity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        IconButton(onClick = onIncrease, modifier = Modifier.size(28.dp)) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodCustomizationDialog(
    menuItem: MenuItem,
    onDismiss: () -> Unit,
    onAddToCart: (FoodOption?, List<FoodOption>) -> Unit
) {
    var selectedVariant by remember { mutableStateOf(menuItem.variants.firstOrNull()) }
    val selectedToppings = remember { mutableStateListOf<FoodOption>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize ${menuItem.name}") },
        text = {
            Column {
                if (menuItem.variants.isNotEmpty()) {
                    Text("Select Size / Variant:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    menuItem.variants.forEach { variant ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedVariant = variant }
                        ) {
                            RadioButton(
                                selected = selectedVariant?.id == variant.id,
                                onClick = { selectedVariant = variant }
                            )
                            Text("${variant.name} (+₹${variant.price.toInt()})", fontSize = 13.sp)
                        }
                    }
                }

                if (menuItem.extraToppings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Extra Toppings / Add-ons:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    menuItem.extraToppings.forEach { topping ->
                        val isChecked = selectedToppings.contains(topping)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) selectedToppings.remove(topping) else selectedToppings.add(topping)
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = {
                                    if (it) selectedToppings.add(topping) else selectedToppings.remove(topping)
                                }
                            )
                            Text("${topping.name} (+₹${topping.price.toInt()})", fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAddToCart(selectedVariant, selectedToppings.toList()) }) {
                Text("Add to Cart")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
