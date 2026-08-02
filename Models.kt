package com.example.data.models

enum class UserRole {
    CUSTOMER, RESTAURANT_OWNER, RIDER, ADMIN
}

data class Address(
    val id: String,
    val label: String, // Home, Work, Circuit House, etc.
    val fullAddress: String,
    val landmark: String = "",
    val city: String = "Satna",
    val state: String = "Madhya Pradesh",
    val pincode: String = "485001",
    val latitude: Double = 24.5828,
    val longitude: Double = 80.8310,
    val isDefault: Boolean = false
)

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val role: UserRole = UserRole.CUSTOMER,
    val addresses: List<Address> = emptyList(),
    val language: String = "EN" // EN, HI
)

data class FoodOption(
    val id: String,
    val name: String,
    val price: Double
)

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val originalPrice: Double? = null,
    val category: String,
    val isVeg: Boolean,
    val imageUrl: String = "",
    val isAvailable: Boolean = true,
    val rating: Double = 4.5,
    val variants: List<FoodOption> = emptyList(),
    val extraToppings: List<FoodOption> = emptyList()
)

data class Restaurant(
    val id: String,
    val name: String,
    val tagline: String,
    val logoUrl: String = "",
    val coverUrl: String = "",
    val address: String,
    val city: String = "Satna",
    val rating: Double = 4.5,
    val totalRatings: Int = 120,
    val deliveryTimeMinutes: Int = 25,
    val distanceKm: Double = 2.4,
    val isPureVeg: Boolean = false,
    val isFeatured: Boolean = false,
    val categories: List<String> = listOf("North Indian", "Biryani", "Thali"),
    val fssaiLicense: String = "11824001000543",
    val fssaiDocUrl: String = "https://images.unsplash.com/photo-1607619056574-7b8d3ee536b2?w=400",
    val gstNumber: String = "23AAAAA0000A1Z5",
    val gstDocUrl: String = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=400",
    val panNumber: String = "ABCDE1234F",
    val panDocUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
    val aadhaarNumber: String = "9876-5432-1098",
    val aadhaarDocUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
    val bankAccount: String = "123456789012",
    val bankIfsc: String = "SBIN0001234",
    val bankName: String = "State Bank of India - Satna Main Branch",
    val bankHolderName: String = "Shivank Dwivedi",
    val latitude: Double = 24.5828,
    val longitude: Double = 80.8310,
    val ownerPhone: String = "+91 9876543210",
    val isOpen: Boolean = true,
    val isApprovedByAdmin: Boolean = true
)

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val selectedVariant: FoodOption? = null,
    val selectedToppings: List<FoodOption> = emptyList(),
    val quantity: Int = 1,
    val instructions: String = ""
) {
    val unitPrice: Double
        get() = (selectedVariant?.price ?: menuItem.price) + selectedToppings.sumOf { it.price }

    val totalPrice: Double
        get() = unitPrice * quantity
}

enum class OrderStatus {
    PLACED,
    ACCEPTED,
    COOKING,
    READY_FOR_PICKUP,
    RIDER_ASSIGNED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}

enum class PaymentMethod {
    RAZORPAY, UPI_PHONEPE, UPI_GPAY, CREDIT_DEBIT_CARD, NET_BANKING, WALLET, CASH_ON_DELIVERY
}

enum class PaymentStatus {
    PENDING, SUCCESS, REFUND_PROCESSING, REFUNDED, FAILED
}

data class Order(
    val id: String,
    val orderNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val restaurantId: String,
    val restaurantName: String,
    val deliveryAddress: Address,
    val items: List<CartItem>,
    val itemTotal: Double,
    val deliveryFee: Double,
    val packagingAndGst: Double,
    val discountAmount: Double,
    val couponCode: String = "",
    val grandTotal: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus = PaymentStatus.SUCCESS,
    val status: OrderStatus = OrderStatus.PLACED,
    val riderId: String? = null,
    val riderName: String? = null,
    val riderPhone: String? = null,
    val riderLat: Double = 24.5828,
    val riderLng: Double = 80.8310,
    val estimatedDeliveryMinutes: Int = 25,
    val createdAt: String = "Just now",
    val deliveryOtp: String = "4850",
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val razorpaySignature: String? = null,
    val isSignatureVerified: Boolean = true,
    val invoiceNumber: String = "INV-SATNA-${System.currentTimeMillis() % 100000}",
    val refundId: String? = null,
    val refundAmount: Double = 0.0,
    val refundReason: String? = null,
    val refundTimestamp: String? = null
)

data class Coupon(
    val code: String,
    val discountPercentage: Int,
    val maxDiscountAmount: Double,
    val minOrderValue: Double,
    val description: String,
    val isSatnaExclusive: Boolean = true
)

data class Review(
    val id: String,
    val userName: String,
    val rating: Double,
    val comment: String,
    val date: String,
    val restaurantId: String
)

data class Rider(
    val id: String,
    val name: String,
    val phone: String,
    val bikeNumber: String = "MP-19-ZA-4850",
    val vehicleModel: String = "Hero Splendor Pro",
    val vehicleRcNumber: String = "MP-19-RC-9876",
    val vehicleRcDocUrl: String = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=400",
    val licenseNumber: String = "MP19 20220014850",
    val drivingLicenseDocUrl: String = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
    val bankAccount: String = "987654321012",
    val bankIfsc: String = "SBIN0001234",
    val bankName: String = "State Bank of India Satna",
    val bankHolderName: String = "Satna Rider",
    val isOnline: Boolean = true,
    val isApproved: Boolean = true,
    val rating: Double = 4.8,
    val currentLat: Double = 24.5850,
    val currentLng: Double = 80.8350,
    val totalDeliveries: Int = 340,
    val walletBalance: Double = 1450.0,
    val dailyEarnings: Double = 450.0,
    val weeklyEarnings: Double = 2850.0,
    val monthlyEarnings: Double = 11400.0
)

data class PlatformSettings(
    val founderName: String = "Shivank Dwivedi",
    val platformName: String = "Satna Eats",
    val launchCity: String = "Satna, Madhya Pradesh",
    val baseDeliveryFee: Double = 25.0,
    val freeDeliveryAbove: Double = 199.0,
    val commissionPercent: Double = 15.0,
    val activeCoupons: Int = 4,
    val totalOrdersCount: Int = 1842,
    val totalRevenue: Double = 328450.0,
    val gstPercent: Double = 5.0,
    val surgeCharge: Double = 10.0,
    val minOrderValue: Double = 99.0,
    val supportPhone: String = "+91 9425123456",
    val supportEmail: String = "support@satnaeats.in",
    val isEmergencyOffline: Boolean = false
)

data class Banner(
    val id: String,
    val title: String,
    val imageUrl: String,
    val promoTag: String = "50% OFF",
    val isActive: Boolean = true
)

data class CityModel(
    val id: String,
    val name: String,
    val state: String = "Madhya Pradesh",
    val pincode: String,
    val isActive: Boolean = true,
    val totalRestaurants: Int = 0
)

data class CustomerModel(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val totalOrders: Int = 12,
    val totalSpent: Double = 2450.0,
    val isBlocked: Boolean = false,
    val registeredDate: String = "12 Jan 2026"
)

data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val targetAudience: String, // ALL, CUSTOMERS, RIDERS, RESTAURANTS
    val sentTime: String = "Just now",
    val status: String = "SENT"
)

enum class TicketCategory {
    ORDER_ISSUE, PAYMENT_REFUND, FOOD_QUALITY, DELIVERY_PARTNER, RESTAURANT_ISSUE, CANCEL_REQUEST, GENERAL_ENQUIRY
}

enum class TicketStatus {
    OPEN, IN_PROGRESS, ESCALATED, RESOLVED, CLOSED
}

enum class TicketPriority {
    LOW, MEDIUM, HIGH, URGENT
}

data class SupportMessage(
    val id: String = "msg_${System.currentTimeMillis()}",
    val ticketId: String,
    val senderRole: String, // CUSTOMER, AI_AGENT, FOUNDER_ADMIN, RESTAURANT, RIDER, SUPPORT_STAFF
    val senderName: String,
    val message: String,
    val timestamp: String = "Just now",
    val isVoiceMessage: Boolean = false,
    val audioLengthSec: Int = 0
)

data class SupportTicket(
    val id: String = "tkt_${System.currentTimeMillis()}",
    val ticketNumber: String = "TICKET-SATNA-${(10000..99999).random()}",
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val orderId: String? = null,
    val restaurantId: String? = null,
    val riderId: String? = null,
    val category: TicketCategory = TicketCategory.GENERAL_ENQUIRY,
    val subject: String,
    val description: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val priority: TicketPriority = TicketPriority.MEDIUM,
    val assignedToRole: String = "AI_AGENT", // AI_AGENT, FOUNDER, RESTAURANT, RIDER, SUPPORT_STAFF
    val assignedToName: String = "Satna AI Executive",
    val createdAt: String = "Today, 02:30 PM",
    val updatedAt: String = "Today, 02:30 PM",
    val messages: List<SupportMessage> = emptyList(),
    val satisfactionRating: Int? = null, // 1 to 5 stars
    val feedbackComment: String? = null
)

data class FaqItem(
    val id: String,
    val question: String,
    val answer: String,
    val category: String, // Orders, Payments & Refunds, Delivery, Quality & Hygiene, Account & Offers
    val isPopular: Boolean = false
)

data class SupportAnalytics(
    val totalTickets: Int = 42,
    val resolvedTickets: Int = 38,
    val pendingTickets: Int = 3,
    val escalatedTickets: Int = 1,
    val avgResponseTimeMinutes: Double = 1.8,
    val satisfactionScore: Double = 4.8
)

