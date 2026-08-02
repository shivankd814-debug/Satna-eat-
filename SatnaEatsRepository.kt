package com.example.data.repository

import com.example.data.backend.FirebaseBackendManager
import com.example.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SatnaEatsRepository private constructor() {

    // Current logged in user
    private val _currentUser = MutableStateFlow(
        User(
            id = "usr_001",
            name = "Shivank Dwivedi",
            phone = "+91 9876543210",
            email = "shivank@satnaeats.com",
            role = UserRole.CUSTOMER,
            addresses = listOf(
                Address(
                    id = "addr_1",
                    label = "Home",
                    fullAddress = "House No. 42, Circuit House Road, Satna",
                    landmark = "Near Civil Lines Police Station",
                    city = "Satna",
                    state = "Madhya Pradesh",
                    pincode = "485001",
                    latitude = 24.5828,
                    longitude = 80.8310,
                    isDefault = true
                ),
                Address(
                    id = "addr_2",
                    label = "Work",
                    fullAddress = "Satna Eats Tech Hub, Semariya Chowk, Satna",
                    landmark = "Above HDFC Bank, 2nd Floor",
                    city = "Satna",
                    state = "Madhya Pradesh",
                    pincode = "485001",
                    latitude = 24.5862,
                    longitude = 80.8345,
                    isDefault = false
                )
            ),
            language = "EN"
        )
    )
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    // Selected portal tab: CUSTOMER, RESTAURANT_OWNER, RIDER, ADMIN
    private val _activePortal = MutableStateFlow(UserRole.CUSTOMER)
    val activePortal: StateFlow<UserRole> = _activePortal.asStateFlow()

    // Active delivery address
    private val _selectedAddress = MutableStateFlow(
        _currentUser.value.addresses.firstOrNull() ?: Address(
            id = "addr_def",
            label = "Satna Location",
            fullAddress = "Circuit House Road, Satna",
            landmark = "Civil Lines",
            city = "Satna"
        )
    )
    val selectedAddress: StateFlow<Address> = _selectedAddress.asStateFlow()

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Applied Coupon
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    // Active order for live tracking
    private val _activeOrder = MutableStateFlow<Order?>(null)
    val activeOrder: StateFlow<Order?> = _activeOrder.asStateFlow()

    // Order History
    private val _orderHistory = MutableStateFlow<List<Order>>(emptyList())
    val orderHistory: StateFlow<List<Order>> = _orderHistory.asStateFlow()

    // Customer Support Tickets
    private val _supportTickets = MutableStateFlow<List<SupportTicket>>(getInitialSatnaSupportTickets())
    val supportTickets: StateFlow<List<SupportTicket>> = _supportTickets.asStateFlow()

    // FAQ System
    private val _faqList = MutableStateFlow<List<FaqItem>>(getInitialSatnaFaqs())
    val faqList: StateFlow<List<FaqItem>> = _faqList.asStateFlow()

    // Restaurants
    private val _restaurants = MutableStateFlow<List<Restaurant>>(getInitialSatnaRestaurants())
    val restaurants: StateFlow<List<Restaurant>> = _restaurants.asStateFlow()

    // Menu Items
    private val _menuItems = MutableStateFlow<List<MenuItem>>(getInitialSatnaMenuItems())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    // Favorite restaurant IDs
    private val _favoriteRestaurantIds = MutableStateFlow<Set<String>>(setOf("rst_1", "rst_3"))
    val favoriteRestaurantIds: StateFlow<Set<String>> = _favoriteRestaurantIds.asStateFlow()

    // Coupons list
    private val _coupons = MutableStateFlow<List<Coupon>>(
        listOf(
            Coupon("SATNA50", 50, 100.0, 149.0, "50% OFF up to ₹100 on all Satna restaurants"),
            Coupon("WELCOME100", 60, 120.0, 199.0, "Flat ₹120 OFF for new Satna Eats members"),
            Coupon("SHIVANK_SPECIAL", 40, 80.0, 99.0, "Special founder offer: 40% OFF on thalis"),
            Coupon("FREEDEL", 100, 30.0, 199.0, "Free delivery on orders above ₹199")
        )
    )
    val coupons: StateFlow<List<Coupon>> = _coupons.asStateFlow()

    // Delivery Riders
    private val _riders = MutableStateFlow<List<Rider>>(
        listOf(
            Rider("rdr_1", "Ramesh Sahu", "+91 9425123456", "MP-19-AB-4850", "DL19-2021008451"),
            Rider("rdr_2", "Vikram Singh", "+91 9826198765", "MP-19-CD-9988", "DL19-2022091823"),
            Rider("rdr_3", "Anil Tripathi", "+91 7000188223", "MP-19-EF-1234", "DL19-2020011223")
        )
    )
    val riders: StateFlow<List<Rider>> = _riders.asStateFlow()

    // Platform Settings
    private val _platformSettings = MutableStateFlow(PlatformSettings())
    val platformSettings: StateFlow<PlatformSettings> = _platformSettings.asStateFlow()

    // Customers List
    private val _customers = MutableStateFlow<List<CustomerModel>>(
        listOf(
            CustomerModel("cust_1", "Shivank Dwivedi (Founder)", "+91 9876543210", "shivank@satnaeats.com", 45, 12850.0, false, "01 Jan 2026"),
            CustomerModel("cust_2", "Aman Sharma", "+91 9425112233", "aman.satna@gmail.com", 18, 3420.0, false, "10 Jan 2026"),
            CustomerModel("cust_3", "Priya Verma", "+91 9826144556", "priya.verma@yahoo.com", 12, 2150.0, false, "15 Jan 2026"),
            CustomerModel("cust_4", "Rahul Gautam", "+91 7000177889", "rahul.satna@outlook.com", 8, 1420.0, false, "20 Jan 2026"),
            CustomerModel("cust_5", "Sanjay Gupta", "+91 9981122334", "sanjay.gupta@gmail.com", 3, 580.0, true, "25 Jan 2026")
        )
    )
    val customers: StateFlow<List<CustomerModel>> = _customers.asStateFlow()

    // Banners List
    private val _banners = MutableStateFlow<List<Banner>>(
        listOf(
            Banner("b_1", "Flat 50% OFF on Satna Special Thalis", "https://images.unsplash.com/photo-1546833999-b9f581a1996d?w=600", "50% OFF", true),
            Banner("b_2", "Craving Biryani? Get Free Delivery", "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=600", "FREE DEL", true),
            Banner("b_3", "Hot Samosa & Gulab Jamun at Maitri Sweets", "https://images.unsplash.com/photo-1601050690597-df0568f70950?w=600", "SNACKS", true)
        )
    )
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    // Food Categories
    private val _categories = MutableStateFlow<List<String>>(
        listOf("North Indian", "Thali", "Biryani", "Pizza", "South Indian", "Bakery", "Pure Veg", "Chaat & Snacks", "Sweets", "Chinese", "Beverages")
    )
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Cities Expansion List
    private val _cities = MutableStateFlow<List<CityModel>>(
        listOf(
            CityModel("city_1", "Satna", "Madhya Pradesh", "485001", true, 6),
            CityModel("city_2", "Rewa", "Madhya Pradesh", "486001", false, 0),
            CityModel("city_3", "Maihar", "Madhya Pradesh", "485771", false, 0),
            CityModel("city_4", "Katni", "Madhya Pradesh", "483501", false, 0),
            CityModel("city_5", "Panna", "Madhya Pradesh", "488001", false, 0)
        )
    )
    val cities: StateFlow<List<CityModel>> = _cities.asStateFlow()

    // Push Notifications Log
    private val _notifications = MutableStateFlow<List<PushNotification>>(
        listOf(
            PushNotification("n_1", "🎉 Weekend Dhamaka in Satna!", "Get Flat ₹100 OFF on all orders above ₹249. Use code SATNA100.", "CUSTOMERS", "Today 10:00 AM"),
            PushNotification("n_2", "⚡ Peak Surge Extra Incentive", "Riders earn +₹20 extra per delivery during dinner rush 7 PM - 10 PM.", "RIDERS", "Yesterday 6:30 PM"),
            PushNotification("n_3", "📋 FSSAI License Renewal Alert", "All partner restaurants must keep valid FSSAI certificate updated.", "RESTAURANTS", "28 Jan 2026")
        )
    )
    val notifications: StateFlow<List<PushNotification>> = _notifications.asStateFlow()


    // Methods
    fun setPortal(role: UserRole) {
        _activePortal.value = role
    }

    fun setLanguage(lang: String) {
        _currentUser.update { it.copy(language = lang) }
    }

    fun setSelectedAddress(address: Address) {
        _selectedAddress.value = address
    }

    fun addAddress(address: Address) {
        _currentUser.update { user ->
            val updated = user.addresses + address
            user.copy(addresses = updated)
        }
        _selectedAddress.value = address
    }

    fun toggleFavoriteRestaurant(restaurantId: String) {
        _favoriteRestaurantIds.update { set ->
            if (set.contains(restaurantId)) set - restaurantId else set + restaurantId
        }
    }

    fun addToCart(item: MenuItem, variant: FoodOption? = null, toppings: List<FoodOption> = emptyList()) {
        _cartItems.update { currentList ->
            val existingIndex = currentList.indexOfFirst {
                it.menuItem.id == item.id &&
                        it.selectedVariant?.id == variant?.id &&
                        it.selectedToppings == toppings
            }
            if (existingIndex != -1) {
                currentList.mapIndexed { index, cartItem ->
                    if (index == existingIndex) cartItem.copy(quantity = cartItem.quantity + 1)
                    else cartItem
                }
            } else {
                currentList + CartItem(
                    id = "cart_${System.currentTimeMillis()}",
                    menuItem = item,
                    selectedVariant = variant,
                    selectedToppings = toppings,
                    quantity = 1
                )
            }
        }
    }

    fun updateCartQuantity(cartItemId: String, newQty: Int) {
        if (newQty <= 0) {
            _cartItems.update { list -> list.filterNot { it.id == cartItemId } }
        } else {
            _cartItems.update { list ->
                list.map { if (it.id == cartItemId) it.copy(quantity = newQty) else it }
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
    }

    fun applyCoupon(coupon: Coupon): Boolean {
        val itemTotal = _cartItems.value.sumOf { it.totalPrice }
        if (itemTotal >= coupon.minOrderValue) {
            _appliedCoupon.value = coupon
            return true
        }
        return false
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    fun placeOrder(paymentMethod: PaymentMethod, razorpayDetails: Triple<String?, String?, String?> = Triple(null, null, null)): Order? {
        val items = _cartItems.value
        if (items.isEmpty()) return null

        val itemTotal = items.sumOf { it.totalPrice }
        val firstRestaurant = _restaurants.value.find { it.id == items.first().menuItem.restaurantId }
            ?: _restaurants.value.first()

        val deliveryFee = if (itemTotal >= _platformSettings.value.freeDeliveryAbove) 0.0 else _platformSettings.value.baseDeliveryFee
        val packagingAndGst = 18.0

        val discount = _appliedCoupon.value?.let { coupon ->
            val calc = (itemTotal * coupon.discountPercentage) / 100.0
            kotlin.math.min(calc, coupon.maxDiscountAmount)
        } ?: 0.0

        val grandTotal = (itemTotal + deliveryFee + packagingAndGst - discount).coerceAtLeast(0.0)

        val razorpayOrderId = razorpayDetails.first ?: "order_Satna_${System.currentTimeMillis()}"
        val razorpayPaymentId = razorpayDetails.second ?: "pay_Satna_${System.currentTimeMillis() % 1000000}"
        val razorpaySig = razorpayDetails.third ?: "sig_${System.currentTimeMillis()}"

        val newOrder = Order(
            id = "ord_${System.currentTimeMillis()}",
            orderNumber = "SE-${(1000..9999).random()}",
            customerId = _currentUser.value.id,
            customerName = _currentUser.value.name,
            customerPhone = _currentUser.value.phone,
            restaurantId = firstRestaurant.id,
            restaurantName = firstRestaurant.name,
            deliveryAddress = _selectedAddress.value,
            items = items,
            itemTotal = itemTotal,
            deliveryFee = deliveryFee,
            packagingAndGst = packagingAndGst,
            discountAmount = discount,
            couponCode = _appliedCoupon.value?.code ?: "",
            grandTotal = grandTotal,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.PENDING else PaymentStatus.SUCCESS,
            status = OrderStatus.PLACED,
            riderId = _riders.value.first().id,
            riderName = _riders.value.first().name,
            riderPhone = _riders.value.first().phone,
            deliveryOtp = (1000..9999).random().toString(),
            razorpayOrderId = razorpayOrderId,
            razorpayPaymentId = razorpayPaymentId,
            razorpaySignature = razorpaySig,
            isSignatureVerified = true
        )

        _activeOrder.value = newOrder
        _orderHistory.update { listOf(newOrder) + it }
        clearCart()
        return newOrder
    }

    fun processRefund(orderId: String, reason: String = "Customer request / Order cancelled"): Boolean {
        val targetOrder = _orderHistory.value.find { it.id == orderId } ?: return false
        val refundId = "rfnd_Satna_${System.currentTimeMillis()}"
        val timestamp = "Today, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

        val updatedOrder = targetOrder.copy(
            paymentStatus = PaymentStatus.REFUNDED,
            status = OrderStatus.CANCELLED,
            refundId = refundId,
            refundAmount = targetOrder.grandTotal,
            refundReason = reason,
            refundTimestamp = timestamp
        )

        if (_activeOrder.value?.id == orderId) {
            _activeOrder.value = updatedOrder
        }

        _orderHistory.update { list ->
            list.map { if (it.id == orderId) updatedOrder else it }
        }

        // Notify customer
        sendNotification(
            PushNotification(
                id = "notif_rfnd_${System.currentTimeMillis()}",
                title = "Refund Processed: ₹${targetOrder.grandTotal.toInt()}",
                message = "Refund ID $refundId of ₹${targetOrder.grandTotal.toInt()} for Order #${targetOrder.orderNumber} credited via Razorpay.",
                targetAudience = "CUSTOMERS",
                sentTime = timestamp
            )
        )
        return true
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        if (_activeOrder.value?.id == orderId) {
            _activeOrder.update { it?.copy(status = newStatus) }
        }
        _orderHistory.update { list ->
            list.map { if (it.id == orderId) it.copy(status = newStatus) else it }
        }
    }

    fun updateRiderLocation(orderId: String, lat: Double, lng: Double) {
        if (_activeOrder.value?.id == orderId) {
            _activeOrder.update { it?.copy(riderLat = lat, riderLng = lng) }
        }
    }

    fun addMenuItem(item: MenuItem) {
        _menuItems.update { listOf(item) + it }
    }

    // --- Customer Support Functions ---

    fun createSupportTicket(
        category: TicketCategory,
        subject: String,
        description: String,
        orderId: String? = null,
        restaurantId: String? = null,
        riderId: String? = null,
        priority: TicketPriority = TicketPriority.MEDIUM,
        assignedToRole: String = "AI_AGENT",
        assignedToName: String = "Satna Eats Support Assistant"
    ): SupportTicket {
        val user = _currentUser.value
        val ticketId = "tkt_${System.currentTimeMillis()}"
        val tktNum = "TICKET-SATNA-${(10000..99999).random()}"
        val timestamp = "Today, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

        val initialMessage = SupportMessage(
            id = "msg_${System.currentTimeMillis()}",
            ticketId = ticketId,
            senderRole = "CUSTOMER",
            senderName = user.name,
            message = description,
            timestamp = timestamp
        )

        val newTicket = SupportTicket(
            id = ticketId,
            ticketNumber = tktNum,
            customerId = user.id,
            customerName = user.name,
            customerPhone = user.phone,
            orderId = orderId,
            restaurantId = restaurantId ?: _activeOrder.value?.restaurantId,
            riderId = riderId ?: _activeOrder.value?.riderId,
            category = category,
            subject = subject,
            description = description,
            status = TicketStatus.OPEN,
            priority = priority,
            assignedToRole = assignedToRole,
            assignedToName = assignedToName,
            createdAt = timestamp,
            updatedAt = timestamp,
            messages = listOf(initialMessage)
        )

        _supportTickets.update { listOf(newTicket) + it }

        // Trigger Push Notification
        sendNotification(
            PushNotification(
                id = "notif_tkt_${System.currentTimeMillis()}",
                title = "Support Ticket Created: $tktNum",
                message = "Your support request '$subject' has been registered. AI Executive and Support Staff are reviewing.",
                targetAudience = "CUSTOMERS",
                sentTime = timestamp
            )
        )

        return newTicket
    }

    fun addMessageToSupportTicket(
        ticketId: String,
        senderRole: String,
        senderName: String,
        messageText: String,
        isVoice: Boolean = false,
        audioLengthSec: Int = 0
    ) {
        val timestamp = "Today, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
        val newMessage = SupportMessage(
            id = "msg_${System.currentTimeMillis()}",
            ticketId = ticketId,
            senderRole = senderRole,
            senderName = senderName,
            message = messageText,
            timestamp = timestamp,
            isVoiceMessage = isVoice,
            audioLengthSec = audioLengthSec
        )

        _supportTickets.update { tickets ->
            tickets.map { t ->
                if (t.id == ticketId) {
                    val updatedStatus = if (senderRole == "CUSTOMER" && t.status == TicketStatus.RESOLVED) TicketStatus.OPEN else t.status
                    t.copy(
                        messages = t.messages + newMessage,
                        status = updatedStatus,
                        updatedAt = timestamp
                    )
                } else t
            }
        }

        // Notify customer if reply is from Founder/Restaurant/Rider/Support Staff
        if (senderRole != "CUSTOMER") {
            val targetTicket = _supportTickets.value.find { it.id == ticketId }
            sendNotification(
                PushNotification(
                    id = "notif_rep_${System.currentTimeMillis()}",
                    title = "Support Reply from $senderName",
                    message = "Re: ${targetTicket?.ticketNumber ?: "Ticket"} - \"$messageText\"",
                    targetAudience = "CUSTOMERS",
                    sentTime = timestamp
                )
            )
        }
    }

    fun updateTicketStatus(
        ticketId: String,
        newStatus: TicketStatus,
        assignedToRole: String? = null,
        assignedToName: String? = null
    ) {
        val timestamp = "Today, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

        _supportTickets.update { list ->
            list.map { t ->
                if (t.id == ticketId) {
                    t.copy(
                        status = newStatus,
                        assignedToRole = assignedToRole ?: t.assignedToRole,
                        assignedToName = assignedToName ?: t.assignedToName,
                        updatedAt = timestamp
                    )
                } else t
            }
        }

        val targetTicket = _supportTickets.value.find { it.id == ticketId }
        sendNotification(
            PushNotification(
                id = "notif_st_${System.currentTimeMillis()}",
                title = "Ticket Status Updated: ${targetTicket?.ticketNumber}",
                message = "Your ticket status is now ${newStatus.name}. Assigned to ${targetTicket?.assignedToName}.",
                targetAudience = "CUSTOMERS",
                sentTime = timestamp
            )
        )
    }

    fun rateSupportTicket(ticketId: String, rating: Int, feedback: String? = null) {
        _supportTickets.update { list ->
            list.map { t ->
                if (t.id == ticketId) {
                    t.copy(satisfactionRating = rating, feedbackComment = feedback)
                } else t
            }
        }
    }

    fun getSupportAnalytics(): SupportAnalytics {
        val tickets = _supportTickets.value
        val total = tickets.size
        val resolved = tickets.count { it.status == TicketStatus.RESOLVED || it.status == TicketStatus.CLOSED }
        val pending = tickets.count { it.status == TicketStatus.OPEN || it.status == TicketStatus.IN_PROGRESS }
        val escalated = tickets.count { it.status == TicketStatus.ESCALATED }

        val ratedTickets = tickets.mapNotNull { it.satisfactionRating }
        val avgScore = if (ratedTickets.isNotEmpty()) ratedTickets.average() else 4.8

        return SupportAnalytics(
            totalTickets = total,
            resolvedTickets = resolved,
            pendingTickets = pending,
            escalatedTickets = escalated,
            avgResponseTimeMinutes = 1.8,
            satisfactionScore = (avgScore * 10).toInt() / 10.0
        )
    }

    // Live AI Support Chat
    private val _liveAiChatMessages = MutableStateFlow<List<SupportMessage>>(
        listOf(
            SupportMessage(
                id = "init_msg",
                ticketId = "live_ai",
                senderRole = "AI_AGENT",
                senderName = "Satna Support Assistant",
                message = "Namaste! I am Satna Support Assistant, your 24/7 Virtual Customer Care Assistant. How can I help you with your order, payment, or delivery today?",
                timestamp = "Just now"
            )
        )
    )
    val liveAiChatMessages: StateFlow<List<SupportMessage>> = _liveAiChatMessages.asStateFlow()

    fun addLiveAiChatMessage(msg: SupportMessage) {
        _liveAiChatMessages.update { it + msg }
        FirebaseBackendManager().saveSupportChatMessageToFirestore(msg)
    }

    fun updateMenuItem(item: MenuItem) {
        _menuItems.update { list -> list.map { if (it.id == item.id) item else it } }
    }

    fun deleteMenuItem(itemId: String) {
        _menuItems.update { list -> list.filterNot { it.id == itemId } }
    }

    fun addRestaurant(restaurant: Restaurant) {
        _restaurants.update { listOf(restaurant) + it }
    }

    fun updateRestaurant(restaurant: Restaurant) {
        _restaurants.update { list ->
            list.map { if (it.id == restaurant.id) restaurant else it }
        }
    }

    fun approveRestaurant(restaurantId: String, approved: Boolean) {
        _restaurants.update { list ->
            list.map { if (it.id == restaurantId) it.copy(isApprovedByAdmin = approved) else it }
        }
    }

    fun updateRider(rider: Rider) {
        _riders.update { list ->
            list.map { if (it.id == rider.id) rider else it }
        }
    }

    fun approveRider(riderId: String, approved: Boolean) {
        _riders.update { list ->
            list.map { if (it.id == riderId) it.copy(isApproved = approved) else it }
        }
    }

    fun toggleCustomerBlock(customerId: String) {
        _customers.update { list ->
            list.map { if (it.id == customerId) it.copy(isBlocked = !it.isBlocked) else it }
        }
    }

    fun deleteCoupon(code: String) {
        _coupons.update { list -> list.filterNot { it.code.equals(code, ignoreCase = true) } }
    }

    fun addBanner(banner: Banner) {
        _banners.update { listOf(banner) + it }
    }

    fun deleteBanner(bannerId: String) {
        _banners.update { list -> list.filterNot { it.id == bannerId } }
    }

    fun addCategory(category: String) {
        if (!_categories.value.contains(category)) {
            _categories.update { listOf(category) + it }
        }
    }

    fun deleteCategory(category: String) {
        _categories.update { list -> list.filterNot { it.equals(category, ignoreCase = true) } }
    }

    fun addCity(city: CityModel) {
        _cities.update { listOf(city) + it }
    }

    fun toggleCityActive(cityId: String) {
        _cities.update { list ->
            list.map { if (it.id == cityId) it.copy(isActive = !it.isActive) else it }
        }
    }

    fun sendNotification(notification: PushNotification) {
        _notifications.update { listOf(notification) + it }
    }

    fun updateRestaurantCoordinates(restaurantId: String, lat: Double, lng: Double, address: String) {
        _restaurants.update { list ->
            list.map { r ->
                if (r.id == restaurantId) {
                    val currentAddr = _selectedAddress.value
                    val dist = com.example.maps.LocationAndMapsManager.calculateDistanceKm(lat, lng, currentAddr.latitude, currentAddr.longitude)
                    val eta = com.example.maps.LocationAndMapsManager.calculateDeliveryTimeMinutes(dist)
                    r.copy(latitude = lat, longitude = lng, address = address, distanceKm = dist, deliveryTimeMinutes = eta)
                } else r
            }
        }
    }

    fun updateCustomerCoordinates(lat: Double, lng: Double, fullAddress: String) {
        val updatedAddr = Address(
            id = "addr_gps_${System.currentTimeMillis()}",
            label = "Real GPS Location",
            fullAddress = fullAddress,
            city = "Satna",
            state = "Madhya Pradesh",
            pincode = "485001",
            latitude = lat,
            longitude = lng,
            isDefault = true
        )
        _selectedAddress.value = updatedAddr

        // Recalculate distance and ETA for all restaurants based on new GPS
        _restaurants.update { list ->
            list.map { r ->
                val dist = com.example.maps.LocationAndMapsManager.calculateDistanceKm(r.latitude, r.longitude, lat, lng)
                val eta = com.example.maps.LocationAndMapsManager.calculateDeliveryTimeMinutes(dist)
                r.copy(distanceKm = dist, deliveryTimeMinutes = eta)
            }
        }
    }

    fun addCoupon(coupon: Coupon) {
        _coupons.update { listOf(coupon) + it }
    }

    fun updatePlatformSettings(settings: PlatformSettings) {
        _platformSettings.value = settings
    }


    companion object {
        @Volatile
        private var instance: SatnaEatsRepository? = null

        fun getInstance(): SatnaEatsRepository {
            return instance ?: synchronized(this) {
                instance ?: SatnaEatsRepository().also { instance = it }
            }
        }
    }
}

private fun getInitialSatnaRestaurants(): List<Restaurant> {
    return listOf(
        Restaurant(
            id = "rst_1",
            name = "Satna Treat Restaurant",
            tagline = "Famous for Special North Indian, Mughlai & Thalis in Satna",
            address = "Circuit House Road, Near Civil Lines, Satna",
            rating = 4.8,
            totalRatings = 420,
            deliveryTimeMinutes = 22,
            distanceKm = 1.8,
            isPureVeg = false,
            isFeatured = true,
            categories = listOf("North Indian", "Biryani", "Chinese", "Thali")
        ),
        Restaurant(
            id = "rst_2",
            name = "Kwality Restaurant & Bakers",
            tagline = "Delicious Pizzas, Burgers, Fresh Bakery & South Indian",
            address = "Bus Stand Road, Satna",
            rating = 4.6,
            totalRatings = 310,
            deliveryTimeMinutes = 25,
            distanceKm = 2.4,
            isPureVeg = false,
            isFeatured = true,
            categories = listOf("Bakery", "Pizza", "South Indian", "Fast Food")
        ),
        Restaurant(
            id = "rst_3",
            name = "Shree Maya Thali & Bhojanalaya",
            tagline = "100% Pure Veg Rajasthani & MP Unlimited Thali",
            address = "Semariya Chowk, Satna",
            rating = 4.9,
            totalRatings = 580,
            deliveryTimeMinutes = 20,
            distanceKm = 1.2,
            isPureVeg = true,
            isFeatured = true,
            categories = listOf("Thali", "Pure Veg", "North Indian", "Sweets")
        ),
        Restaurant(
            id = "rst_4",
            name = "Royal Biryani & Kabab House",
            tagline = "Authentic Hyderabadi Dum Biryani & Tandoori Kababs",
            address = "Panna Naka, Satna",
            rating = 4.7,
            totalRatings = 290,
            deliveryTimeMinutes = 28,
            distanceKm = 3.1,
            isPureVeg = false,
            isFeatured = false,
            categories = listOf("Biryani", "Kabab", "Mughlai")
        ),
        Restaurant(
            id = "rst_5",
            name = "Venkatesh Food Plaza",
            tagline = "Crispy South Indian Dosa, Idli & Indo-Chinese Delight",
            address = "Rewa Road, Satna",
            rating = 4.5,
            totalRatings = 210,
            deliveryTimeMinutes = 24,
            distanceKm = 2.9,
            isPureVeg = true,
            isFeatured = false,
            categories = listOf("South Indian", "Chinese", "Beverages")
        ),
        Restaurant(
            id = "rst_6",
            name = "Maitri Sweets & Namkeen",
            tagline = "Fresh Satna Samosa, Kachori, Gulab Jamun & Jalebi",
            address = "Jagat Dev Talao Road, Satna",
            rating = 4.9,
            totalRatings = 640,
            deliveryTimeMinutes = 18,
            distanceKm = 0.9,
            isPureVeg = true,
            isFeatured = true,
            categories = listOf("Chaat & Snacks", "Sweets", "Pure Veg")
        )
    )
}

private fun getInitialSatnaMenuItems(): List<MenuItem> {
    return listOf(
        // Satna Treat
        MenuItem(
            id = "m_101",
            restaurantId = "rst_1",
            name = "Paneer Butter Masala",
            description = "Soft cottage cheese cooked in rich tomato cashew butter gravy with aromatic spices",
            price = 220.0,
            originalPrice = 250.0,
            category = "North Indian",
            isVeg = true,
            rating = 4.8,
            variants = listOf(
                FoodOption("v_1", "Half Plate", 140.0),
                FoodOption("v_2", "Full Plate", 220.0)
            ),
            extraToppings = listOf(
                FoodOption("t_1", "Extra Butter", 20.0),
                FoodOption("t_2", "Extra Cheese", 30.0)
            )
        ),
        MenuItem(
            id = "m_102",
            restaurantId = "rst_1",
            name = "Special Satna Thali",
            description = "Paneer Sabzi, Seasonal Veg, Dal Tadka, Jeera Rice, 4 Butter Roti, Gulab Jamun, Salad & Papad",
            price = 199.0,
            originalPrice = 230.0,
            category = "Thali",
            isVeg = true,
            rating = 4.9
        ),
        MenuItem(
            id = "m_103",
            restaurantId = "rst_1",
            name = "Butter Naan",
            description = "Tandoori naan brushed with rich Amul butter",
            price = 40.0,
            category = "North Indian",
            isVeg = true
        ),
        MenuItem(
            id = "m_104",
            restaurantId = "rst_1",
            name = "Chicken Tikka Masala",
            description = "Charcoal roasted chicken pieces simmered in spicy onion tomato gravy",
            price = 290.0,
            originalPrice = 330.0,
            category = "North Indian",
            isVeg = false,
            rating = 4.7
        ),

        // Kwality Restaurant
        MenuItem(
            id = "m_201",
            restaurantId = "rst_2",
            name = "Kwality Special Loaded Pizza",
            description = "Crispy crust topped with mozzarella, paneer, capsicum, corn, olives and red paprika",
            price = 280.0,
            originalPrice = 320.0,
            category = "Pizza",
            isVeg = true,
            rating = 4.6,
            variants = listOf(
                FoodOption("v_21", "Medium (8 inch)", 280.0),
                FoodOption("v_22", "Large (12 inch)", 440.0)
            )
        ),
        MenuItem(
            id = "m_202",
            restaurantId = "rst_2",
            name = "Cold Coffee with Chocolate Ice Cream",
            description = "Thick blended iced coffee topped with chocolate scoop and choco chips",
            price = 110.0,
            category = "Beverages",
            isVeg = true,
            rating = 4.8
        ),

        // Shree Maya Thali
        MenuItem(
            id = "m_301",
            restaurantId = "rst_3",
            name = "Maharaja Rajasthani & MP Thali",
            description = "Dal Baati Churma, Shahi Paneer, Gatte Ki Sabzi, Malpua, Chass, Rice, Roti, Salad",
            price = 210.0,
            originalPrice = 250.0,
            category = "Thali",
            isVeg = true,
            rating = 4.9
        ),
        MenuItem(
            id = "m_302",
            restaurantId = "rst_3",
            name = "Dal Baati Churma Special",
            description = "4 Crispy ghee wheat baatis served with spicy Panchmel Dal and sweet Churma",
            price = 160.0,
            category = "Pure Veg",
            isVeg = true,
            rating = 4.9
        ),

        // Royal Biryani
        MenuItem(
            id = "m_401",
            restaurantId = "rst_4",
            name = "Hyderabadi Chicken Dum Biryani",
            description = "Long grain basmati rice layered with marinated chicken, saffron and whole spices, served with Mirchi Ka Salan and Raita",
            price = 240.0,
            originalPrice = 280.0,
            category = "Biryani",
            isVeg = false,
            rating = 4.8,
            variants = listOf(
                FoodOption("v_41", "Half Handi", 150.0),
                FoodOption("v_42", "Full Handi", 240.0)
            )
        ),

        // Venkatesh Food Plaza
        MenuItem(
            id = "m_501",
            restaurantId = "rst_5",
            name = "Butter Cheese Masala Dosa",
            description = "Golden crispy rice crepe filled with spiced potato masala and topped with grated cheese and butter, served with coconut chutney & sambhar",
            price = 130.0,
            category = "South Indian",
            isVeg = true,
            rating = 4.6
        ),

        // Maitri Sweets
        MenuItem(
            id = "m_601",
            restaurantId = "rst_6",
            name = "Satna Special Garam Samosa (2 Pcs)",
            description = "Crispy golden fried samosa filled with tangy potato green pea masala, served with meethi and hari chutney",
            price = 30.0,
            category = "Chaat & Snacks",
            isVeg = true,
            rating = 4.9
        ),
        MenuItem(
            id = "m_602",
            restaurantId = "rst_6",
            name = "Hot Desi Ghee Gulab Jamun (2 Pcs)",
            description = "Melt in mouth soft milk dumplings soaked in cardamom sugar syrup",
            price = 50.0,
            category = "Sweets",
            isVeg = true,
            rating = 4.9
        )
    )
}

fun getInitialSatnaSupportTickets(): List<SupportTicket> {
    return listOf(
        SupportTicket(
            id = "tkt_1001",
            ticketNumber = "TICKET-SATNA-48501",
            customerId = "user_cust_1",
            customerName = "Shivank Customer",
            customerPhone = "+91 9425123456",
            orderId = "ord_prev_101",
            restaurantId = "rst_1",
            riderId = "rd_101",
            category = TicketCategory.FOOD_QUALITY,
            subject = "Extra chutney was missing in Paneer Tikka order",
            description = "I requested extra green chutney in special instructions, but it was not packed.",
            status = TicketStatus.RESOLVED,
            priority = TicketPriority.MEDIUM,
            assignedToRole = "RESTAURANT",
            assignedToName = "Kwality Restaurant Satna Manager",
            createdAt = "Yesterday, 08:15 PM",
            updatedAt = "Yesterday, 08:30 PM",
            messages = listOf(
                SupportMessage("m_1", "tkt_1001", "CUSTOMER", "Shivank Customer", "I requested extra green chutney in special instructions, but it was missing.", "Yesterday, 08:15 PM"),
                SupportMessage("m_2", "tkt_1001", "AI_AGENT", "Satna Eats Support Assistant", "Apologies for the inconvenience! I have alerted Kwality Restaurant Satna.", "Yesterday, 08:16 PM"),
                SupportMessage("m_3", "tkt_1001", "RESTAURANT", "Kwality Restaurant Manager", "We sincerely apologize Shivank ji! We have added a free dessert coupon for your next order.", "Yesterday, 08:25 PM")
            ),
            satisfactionRating = 5,
            feedbackComment = "Very fast resolution by support assistant and restaurant!"
        ),
        SupportTicket(
            id = "tkt_1002",
            ticketNumber = "TICKET-SATNA-84920",
            customerId = "user_cust_1",
            customerName = "Shivank Customer",
            customerPhone = "+91 9425123456",
            orderId = null,
            restaurantId = "rst_2",
            riderId = null,
            category = TicketCategory.PAYMENT_REFUND,
            subject = "Razorpay payment debited twice during UPI transaction",
            description = "Amount ₹240 was debited twice via PhonePe UPI on order attempt.",
            status = TicketStatus.ESCALATED,
            priority = TicketPriority.HIGH,
            assignedToRole = "FOUNDER",
            assignedToName = "Shivank Dwivedi (Founder)",
            createdAt = "Today, 11:20 AM",
            updatedAt = "Today, 11:45 AM",
            messages = listOf(
                SupportMessage("m_4", "tkt_1002", "CUSTOMER", "Shivank Customer", "Amount ₹240 was debited twice via PhonePe UPI on order attempt.", "Today, 11:20 AM"),
                SupportMessage("m_5", "tkt_1002", "AI_AGENT", "Satna Eats Support Assistant", "I am checking Razorpay HMAC payment logs. Duplicate transaction detected. Escalating to Founder Shivank Dwivedi.", "Today, 11:22 AM"),
                SupportMessage("m_6", "tkt_1002", "FOUNDER_ADMIN", "Shivank Dwivedi (Founder)", "Hello Shivank, Razorpay Refund RFND-98234 has been processed. Credit expected in 2 hours.", "Today, 11:45 AM")
            )
        )
    )
}

fun getInitialSatnaFaqs(): List<FaqItem> {
    return listOf(
        FaqItem("f_1", "How do I track my active Satna Eats order?", "Go to the Active Delivery tab in the app to view live GPS tracking of your delivery partner, kitchen status, and estimated arrival time.", "Orders", true),
        FaqItem("f_2", "How do refunds work with Razorpay?", "All online payments (UPI, Cards, Net Banking, Wallets) refunded via Razorpay are processed instantly. Funds reflect in your bank account or wallet within 2 to 24 hours.", "Payments & Refunds", true),
        FaqItem("f_3", "What is the Cash on Delivery (COD) policy in Satna?", "Cash on Delivery is available for all orders across Satna city. Please keep exact change ready for our delivery partner upon order arrival.", "Payments & Refunds", false),
        FaqItem("f_4", "Can I cancel my order after placing it?", "Orders can be cancelled free of charge if the restaurant has not started preparing the food. Tap 'Cancel Order' in the Active Order screen.", "Orders", true),
        FaqItem("f_5", "What if items are missing or food quality is poor?", "Tap 'Report Issue' or chat with our 24/7 Satna AI Support. We generate a priority support ticket escalated directly to the restaurant and Founder Shivank Dwivedi.", "Quality & Hygiene", true),
        FaqItem("f_6", "How do delivery charges work in Satna?", "Delivery is FREE on all orders above ₹199 across Satna! For orders under ₹199, a nominal base fee of ₹25 applies.", "Delivery", false),
        FaqItem("f_7", "How can I contact human customer support?", "Our 24/7 AI agent handles instant queries. You can also request 'Escalate to Founder' anytime in the chat to talk to human executive or call +91 9425123456.", "Account & Offers", true)
    )
}
