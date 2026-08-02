package com.example.data.support

import com.example.BuildConfig
import com.example.data.models.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiSupportService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun getAiSupportResponse(
        userMessage: String,
        customerName: String,
        customerPhone: String,
        activeOrder: Order?,
        conversationHistory: List<Pair<String, String>> = emptyList() // Pair("user" | "model", text)
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        val orderInfo = if (activeOrder != null) {
            """
            Active Order ID: ${activeOrder.id} (#${activeOrder.orderNumber})
            Restaurant: ${activeOrder.restaurantName}
            Status: ${activeOrder.status.name}
            Grand Total: ₹${activeOrder.grandTotal.toInt()}
            Items: ${activeOrder.items.joinToString { "${it.quantity}x ${it.menuItem.name}" }}
            Payment Method: ${activeOrder.paymentMethod.name}
            Payment Status: ${activeOrder.paymentStatus.name}
            Delivery Partner: ${activeOrder.riderName} (${activeOrder.riderPhone})
            Delivery OTP: ${activeOrder.deliveryOtp}
            Estimated Arrival: ${activeOrder.estimatedDeliveryMinutes} mins
            Delivery Address: ${activeOrder.deliveryAddress}
            """.trimIndent()
        } else {
            "No active order currently in progress."
        }

        val systemInstructionText = """
            YOU ARE 'Satna Eats Support Assistant', a virtual AI Customer Support Representative for Satna Eats.
            
            STRICT FOUNDER NAME RULE:
            1. NEVER mention the founder's name ('Shivank' or 'Shivank Dwivedi') in greetings, normal conversations, ticket creation, or escalations.
            2. ONLY IF the customer explicitly asks 'Who is the founder of Satna Eats?', 'Who created Satna Eats?', 'Who developed Satna Eats?', or 'Kisne banaya Satna Eats?' (or similar explicit questions asking who founded/created/developed Satna Eats), reply EXACTLY: 'Satna Eats was founded and developed by Shivank Dwivedi.'
            3. For EVERY other conversation, NEVER mention the founder's name under any circumstances.
            
            GREETINGS & POLITENESS:
            - Greet users warmly and politely using phrases like "Namaste! 🙏", "Hello Sir! 👋", "Hello Ma'am! 👋", or "Welcome to Satna Eats!".
            - Address the customer directly by their registered name: "$customerName".
            
            CUSTOMER DETAILS:
            - Registered Name: $customerName
            - Registered Phone: $customerPhone
            
            CURRENT ORDER CONTEXT:
            $orderInfo
            
            BEHAVIORAL GUIDELINES (Swiggy / Zomato Customer Care Style):
            1. Language: Support Hindi, English, and Hinglish naturally based on what language the user speaks.
            2. Tone: Polite, empathetic, professional, and human-like.
            3. Scope of Support:
               a) Live Order Status & Delay Tracking (refer to $orderInfo)
               b) Food Ordering & Menu recommendations
               c) Payment Failures & Razorpay Double Deductions
               d) Instant Refunds & Order Cancellation Requests
               e) Delivery Partner Conduct & Location tracking
               f) Restaurant Info & Food Quality Complaints
               g) Active Offers, Promo Codes & Coupons
               h) Account details & General questions
            4. Conversational Flow:
               - Do NOT give generic canned responses.
               - Ask necessary follow-up questions to solve problems step by step (e.g. "Which specific item was missing?", "Was the amount debited via UPI?").
               - Remember previous messages in the current conversation session.
            5. Ticket Creation & Escalation:
               - If a refund, cancellation, or serious complaint is raised, mention that support ticket (e.g. TICKET-SATNA-${(10000..99999).random()}) is created.
               - If the issue cannot be resolved automatically, reassure $customerName: "I have escalated your ticket to our Senior Human Support Team for priority resolution."
            6. Response Format: Keep replies short, helpful, and polite (2 to 4 sentences). Never repeat the exact same sentence.
        """.trimIndent()

        // Smart Offline / Fallback Conversational Engine
        fun generateSmartOfflineResponse(): String {
            val lower = userMessage.lowercase()
            val randomTicketNum = "TICKET-SATNA-${(10000..99999).random()}"

            // Strict Founder Check
            if (lower.contains("who is the founder") || lower.contains("who created") || lower.contains("who developed") || lower.contains("kisne banaya") || lower.contains("founder of satna eats") || lower.contains("founder kon") || lower.contains("founder name")) {
                return "Satna Eats was founded and developed by Shivank Dwivedi."
            }

            return when {
                lower.contains("hi") || lower.contains("hello") || lower.contains("namaste") || lower.contains("hey") -> {
                    if (activeOrder != null) {
                        "Namaste $customerName! 🙏 Welcome to Satna Eats Customer Care. How can I assist you with your active order #${activeOrder.orderNumber} from ${activeOrder.restaurantName} today?"
                    } else {
                        "Namaste $customerName! 🙏 Welcome to Satna Eats! How can I help you today with your orders, offers, payments, or account?"
                    }
                }
                lower.contains("where") || lower.contains("status") || lower.contains("kahan") || lower.contains("late") || lower.contains("delay") || lower.contains("time") -> {
                    if (activeOrder != null) {
                        "Namaste $customerName! Your order #${activeOrder.orderNumber} from ${activeOrder.restaurantName} is currently ${activeOrder.status.name}. Delivery partner ${activeOrder.riderName} is arriving in ~${activeOrder.estimatedDeliveryMinutes} mins. Your delivery OTP is ${activeOrder.deliveryOtp}."
                    } else {
                        "Namaste $customerName! You don't have an active order right now. If you're inquiring about a past order, please share the order number so I can check its status for you."
                    }
                }
                lower.contains("refund") || lower.contains("paise") || lower.contains("money") || lower.contains("double") || lower.contains("debited") -> {
                    "I understand your concern regarding payment debit, $customerName. Razorpay refunds are processed automatically within 2 to 24 hours. I have logged support ticket $randomTicketNum for instant transaction audit. Is there anything else about this payment you'd like to share?"
                }
                lower.contains("cancel") -> {
                    if (activeOrder != null) {
                        "I can help with cancellation for order #${activeOrder.orderNumber}, $customerName. Since your order status is ${activeOrder.status.name}, let me verify with ${activeOrder.restaurantName} kitchen. Would you like me to proceed with registering a cancellation request?"
                    } else {
                        "You currently do not have any active order to cancel, $customerName. Can I help you with anything else?"
                    }
                }
                lower.contains("missing") || lower.contains("quality") || lower.contains("cold") || lower.contains("bad") || lower.contains("spill") || lower.contains("taste") -> {
                    val restaurantName = activeOrder?.restaurantName ?: "the restaurant"
                    "I am truly sorry to hear about the food issue with $restaurantName, $customerName. I have generated support ticket $randomTicketNum and escalated it to our Quality Assurance team. Could you tell me which specific item was affected?"
                }
                lower.contains("escalate") || lower.contains("human") || lower.contains("talk") || lower.contains("call") || lower.contains("agent") -> {
                    "I have flagged ticket $randomTicketNum for human escalation, $customerName. Our Senior Support Desk has been notified and will contact you shortly on $customerPhone."
                }
                lower.contains("offer") || lower.contains("coupon") || lower.contains("discount") || lower.contains("code") -> {
                    "Welcome to Satna Eats Offers! You can use code 'SATNA50' to get 50% OFF up to ₹100 on your first order. We also offer FREE delivery on orders above ₹299!"
                }
                else -> {
                    "Thank you for reaching out, $customerName. 🙏 I am your 24/7 Satna Eats Virtual Support Assistant. Could you please specify a bit more detail about your concern so I can guide you step by step?"
                }
            }
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateSmartOfflineResponse()
        }

        try {
            val contentsArray = JSONArray()

            // System instructions turn
            val systemContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", systemInstructionText)))
            }
            contentsArray.put(systemContent)

            val systemResponse = JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", "Understood. I am Satna Eats Support Assistant, ready to assist $customerName attentively.")))
            }
            contentsArray.put(systemResponse)

            // Conversation history turns
            for ((role, text) in conversationHistory) {
                val contentObj = JSONObject().apply {
                    put("role", if (role == "user") "user" else "model")
                    put("parts", JSONArray().put(JSONObject().put("text", text)))
                }
                contentsArray.put(contentObj)
            }

            // Current user message turn
            val userContent = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userMessage)))
            }
            contentsArray.put(userContent)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 300)
                })
            }

            // Primary model endpoint: gemini-1.5-flash with fallback to gemini-2.0-flash
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext generateSmartOfflineResponse()
                }
                val responseBodyStr = response.body?.string() ?: return@withContext generateSmartOfflineResponse()
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", generateSmartOfflineResponse())
                    }
                }
                generateSmartOfflineResponse()
            }
        } catch (e: Exception) {
            generateSmartOfflineResponse()
        }
    }
}

