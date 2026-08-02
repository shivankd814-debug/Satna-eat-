package com.example.ui.screens.customer

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.models.*
import com.example.data.repository.SatnaEatsRepository
import com.example.data.support.GeminiSupportService
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSupportScreen(
    repository: SatnaEatsRepository,
    initialOrderId: String? = null,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentUser by repository.currentUser.collectAsState()
    val activeOrder by repository.activeOrder.collectAsState()
    val orderHistory by repository.orderHistory.collectAsState()
    val supportTickets by repository.supportTickets.collectAsState()
    val faqList by repository.faqList.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var selectedOrderForHelp by remember { mutableStateOf<Order?>(activeOrder ?: orderHistory.firstOrNull()) }
    var showCreateTicketModal by remember { mutableStateOf(false) }

    // TTS engine setup
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialized
            }
        }
        tts.language = Locale("hi", "IN")
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("24/7 AI Support Desk", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = SatnaVegGreen,
                                shape = CircleShape,
                                modifier = Modifier.size(8.dp)
                            ) {}
                        }
                        Text(
                            "Satna Eats • 24/7 Virtual Support Assistant",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = SatnaTealSecondary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = SatnaTealSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hindi/Eng", fontSize = 10.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9FAFB))
        ) {
            // Main Navigation Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SatnaOrangePrimary
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("My Tickets (${supportTickets.count { it.status != TicketStatus.CLOSED }})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FAQs", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Tab Content Display
            when (selectedTabIndex) {
                0 -> AiChatTabContent(
                    repository = repository,
                    currentUser = currentUser,
                    selectedOrder = selectedOrderForHelp,
                    allOrders = listOfNotNull(activeOrder) + orderHistory,
                    onSelectOrder = { selectedOrderForHelp = it },
                    ttsEngine = ttsEngine,
                    onCreateTicketRequest = { showCreateTicketModal = true }
                )
                1 -> MyTicketsTabContent(
                    repository = repository,
                    supportTickets = supportTickets,
                    onCreateNewTicket = { showCreateTicketModal = true }
                )
                2 -> FaqTabContent(
                    faqList = faqList,
                    onContactSupport = { selectedTabIndex = 0 }
                )
            }
        }
    }

    // Modal: Create New Support Ticket
    if (showCreateTicketModal) {
        CreateSupportTicketModal(
            repository = repository,
            selectedOrder = selectedOrderForHelp,
            allOrders = listOfNotNull(activeOrder) + orderHistory,
            onDismiss = { showCreateTicketModal = false }
        )
    }
}

@Composable
fun AiChatTabContent(
    repository: SatnaEatsRepository,
    currentUser: User,
    selectedOrder: Order?,
    allOrders: List<Order>,
    onSelectOrder: (Order?) -> Unit,
    ttsEngine: TextToSpeech?,
    onCreateTicketRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val chatMessages by repository.liveAiChatMessages.collectAsStateWithLifecycle()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isVoiceRecording by remember { mutableStateOf(false) }
    var showOrderPickerDropdown by remember { mutableStateOf(false) }

    fun sendMessage(textToSend: String, isVoice: Boolean = false) {
        if (textToSend.isBlank()) return

        val userMsg = SupportMessage(
            id = "msg_${System.currentTimeMillis()}",
            ticketId = "live_ai",
            senderRole = "CUSTOMER",
            senderName = currentUser.name,
            message = textToSend,
            timestamp = "Just now",
            isVoiceMessage = isVoice
        )

        repository.addLiveAiChatMessage(userMsg)
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            if (chatMessages.isNotEmpty()) {
                listState.animateScrollToItem(chatMessages.size - 1)
            }

            val history = chatMessages.map {
                Pair(if (it.senderRole == "CUSTOMER") "user" else "model", it.message)
            }

            val aiReplyText = GeminiSupportService.getAiSupportResponse(
                userMessage = textToSend,
                customerName = currentUser.name,
                customerPhone = currentUser.phone,
                activeOrder = selectedOrder,
                conversationHistory = history
            )

            isThinking = false
            val aiMsg = SupportMessage(
                id = "ai_${System.currentTimeMillis()}",
                ticketId = "live_ai",
                senderRole = "AI_AGENT",
                senderName = "Satna Support Assistant",
                message = aiReplyText,
                timestamp = "Just now"
            )

            repository.addLiveAiChatMessage(aiMsg)
            if (chatMessages.isNotEmpty()) {
                listState.animateScrollToItem(chatMessages.size - 1)
            }

            // Speak AI response if voice mode was used or if audio speech requested
            ttsEngine?.speak(aiReplyText, TextToSpeech.QUEUE_FLUSH, null, "ai_speak_${System.currentTimeMillis()}")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Linked Order Selector Card
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showOrderPickerDropdown = true }
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = SatnaOrangePrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            if (selectedOrder != null) "Linked Order: #${selectedOrder.orderNumber} (${selectedOrder.restaurantName})" else "Link an order for quick support",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (selectedOrder != null) "Status: ${selectedOrder.status.name} • ₹${selectedOrder.grandTotal.toInt()}" else "Tap to attach active/past order",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showOrderPickerDropdown,
                    onDismissRequest = { showOrderPickerDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None (General Enquiry)", fontSize = 12.sp) },
                        onClick = {
                            onSelectOrder(null)
                            showOrderPickerDropdown = false
                        }
                    )
                    allOrders.forEach { ord ->
                        DropdownMenuItem(
                            text = { Text("Order #${ord.orderNumber} - ${ord.restaurantName} (₹${ord.grandTotal.toInt()})", fontSize = 12.sp) },
                            onClick = {
                                onSelectOrder(ord)
                                showOrderPickerDropdown = false
                            }
                        )
                    }
                }

                OutlinedButton(
                    onClick = onCreateTicketRequest,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Ticket", fontSize = 10.sp)
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                SuggestionChip(
                    onClick = { sendMessage("Where is my active order?") },
                    label = { Text("📍 Order Live Status", fontSize = 11.sp) }
                )
            }
            item {
                SuggestionChip(
                    onClick = { sendMessage("Razorpay payment refund status") },
                    label = { Text("💸 Payment Refund", fontSize = 11.sp) }
                )
            }
            item {
                SuggestionChip(
                    onClick = { sendMessage("I want to cancel my active order") },
                    label = { Text("❌ Cancel Order", fontSize = 11.sp) }
                )
            }
            item {
                SuggestionChip(
                    onClick = { sendMessage("Food quality or missing item complaint") },
                    label = { Text("🍱 Food Quality Issue", fontSize = 11.sp) }
                )
            }
            item {
                SuggestionChip(
                    onClick = {
                        sendMessage("Please escalate my complaint directly to Founder Shivank Dwivedi")
                        val newTkt = repository.createSupportTicket(
                            category = TicketCategory.ORDER_ISSUE,
                            subject = "Escalated to Founder Shivank",
                            description = "Customer requested human founder intervention via AI Chat.",
                            orderId = selectedOrder?.id,
                            priority = TicketPriority.HIGH,
                            assignedToRole = "FOUNDER",
                            assignedToName = "Shivank Dwivedi (Founder)"
                        )
                    },
                    label = { Text("👑 Talk to Founder", fontSize = 11.sp) }
                )
            }
        }

        Divider(color = Color.LightGray.copy(alpha = 0.3f))

        // Chat Conversation Transcript
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { msg ->
                ChatMessageBubble(msg = msg, ttsEngine = ttsEngine)
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = SatnaOrangePrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Satna AI Executive is typing...", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Bottom Message Input Bar with Voice Toggle
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isVoiceRecording = !isVoiceRecording
                        if (isVoiceRecording) {
                            val voiceSampleQueries = listOf(
                                "Satna AI, mera order kitni der me aayega?",
                                "UPI payment double kat gaya hai refund karo",
                                "Food quantity bahut kam thi quality bad hai"
                            )
                            val randomQuery = voiceSampleQueries.random()
                            inputText = randomQuery
                        }
                    }
                ) {
                    Icon(
                        if (isVoiceRecording) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isVoiceRecording) SatnaNonVegRed else SatnaTealSecondary
                    )
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text(if (isVoiceRecording) "Voice recording active... (Tap mic to send)" else "Type or speak in Hindi/English...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF3F4F6),
                        unfocusedContainerColor = Color(0xFFF3F4F6),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(20.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                )

                Spacer(modifier = Modifier.width(6.dp))

                FloatingActionButton(
                    onClick = { sendMessage(inputText, isVoice = isVoiceRecording) },
                    modifier = Modifier.size(44.dp),
                    containerColor = SatnaOrangePrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(msg: SupportMessage, ttsEngine: TextToSpeech?) {
    val isCustomer = msg.senderRole == "CUSTOMER"

    val avatarBg = when (msg.senderRole) {
        "CUSTOMER" -> Color(0xFF1E88E5)
        "AI_AGENT" -> SatnaOrangePrimary
        "FOUNDER_ADMIN" -> Color(0xFFD4AF37) // Gold
        "RESTAURANT" -> Color(0xFFE65100)
        "RIDER" -> SatnaVegGreen
        else -> SatnaTealSecondary
    }

    val bubbleBg = when (msg.senderRole) {
        "CUSTOMER" -> SatnaOrangePrimary.copy(alpha = 0.12f)
        "AI_AGENT" -> Color(0xFFE0F2F1) // Soft Teal
        "FOUNDER_ADMIN" -> Color(0xFFFFF8E1) // Soft Gold
        else -> Color.White
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isCustomer) {
            Surface(
                color = avatarBg,
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        when (msg.senderRole) {
                            "AI_AGENT" -> "🤖"
                            "FOUNDER_ADMIN" -> "👑"
                            "RESTAURANT" -> "🍳"
                            "RIDER" -> "🛵"
                            else -> "🎧"
                        },
                        fontSize = 13.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isCustomer) Alignment.End else Alignment.Start) {
            Surface(
                color = bubbleBg,
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isCustomer) 14.dp else 2.dp,
                    bottomEnd = if (isCustomer) 2.dp else 14.dp
                ),
                border = if (!isCustomer && msg.senderRole == "FOUNDER_ADMIN") BorderStroke(1.dp, Color(0xFFD4AF37)) else null,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            msg.senderName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (msg.senderRole == "FOUNDER_ADMIN") Color(0xFFB78103) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(msg.timestamp, fontSize = 9.sp, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(msg.message, fontSize = 12.sp, lineHeight = 16.sp)

                    if (msg.isVoiceMessage) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = SatnaTealSecondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Voice Note", fontSize = 10.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Audio Speech Listen Button for AI replies
            if (msg.senderRole == "AI_AGENT" && ttsEngine != null) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable {
                            ttsEngine.speak(msg.message, TextToSpeech.QUEUE_FLUSH, null, "ai_speak")
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Listen", tint = SatnaTealSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Listen (सुनें)", fontSize = 10.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (isCustomer) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                color = avatarBg,
                shape = CircleShape,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("👤", fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun MyTicketsTabContent(
    repository: SatnaEatsRepository,
    supportTickets: List<SupportTicket>,
    onCreateNewTicket: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedTicketForDetails by remember { mutableStateOf<SupportTicket?>(null) }

    val filteredTickets = remember(supportTickets, selectedFilter) {
        when (selectedFilter) {
            "OPEN" -> supportTickets.filter { it.status == TicketStatus.OPEN || it.status == TicketStatus.IN_PROGRESS }
            "ESCALATED" -> supportTickets.filter { it.status == TicketStatus.ESCALATED }
            "RESOLVED" -> supportTickets.filter { it.status == TicketStatus.RESOLVED || it.status == TicketStatus.CLOSED }
            else -> supportTickets
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Header Row & New Ticket FAB
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Support Tickets (${supportTickets.size})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Button(
                onClick = onCreateNewTicket,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create Ticket", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Filter Chips
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("ALL", "OPEN", "ESCALATED", "RESOLVED").forEach { status ->
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text(status, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredTickets.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.ConfirmationNumber, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No tickets found in '$selectedFilter'", fontWeight = FontWeight.Bold)
                    Text("Need help? Create a new support ticket or chat with Satna AI.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredTickets) { ticket ->
                    SupportTicketCard(
                        ticket = ticket,
                        onViewDetails = { selectedTicketForDetails = ticket }
                    )
                }
            }
        }
    }

    selectedTicketForDetails?.let { tkt ->
        TicketTranscriptDialog(
            repository = repository,
            ticket = tkt,
            onDismiss = { selectedTicketForDetails = null }
        )
    }
}

@Composable
fun SupportTicketCard(
    ticket: SupportTicket,
    onViewDetails: () -> Unit
) {
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
            .clickable { onViewDetails() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.ticketNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        ticket.status.name,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(ticket.subject, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(ticket.description, fontSize = 11.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(12.dp), tint = SatnaTealSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assigned: ${ticket.assignedToName}", fontSize = 10.sp, color = SatnaTealSecondary, fontWeight = FontWeight.Bold)
                }
                Text("Created: ${ticket.createdAt}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TicketTranscriptDialog(
    repository: SatnaEatsRepository,
    ticket: SupportTicket,
    onDismiss: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }
    var ratingSelected by remember { mutableStateOf(ticket.satisfactionRating ?: 5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Transcript: ${ticket.ticketNumber}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Category: ${ticket.category.name} • Assigned: ${ticket.assignedToName}", fontSize = 11.sp, color = Color.Gray)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LazyColumn(
                    modifier = Modifier
                        .height(260.dp)
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ticket.messages) { msg ->
                        ChatMessageBubble(msg = msg, ttsEngine = null)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (ticket.status != TicketStatus.CLOSED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Reply to support...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    repository.addMessageToSupportTicket(
                                        ticketId = ticket.id,
                                        senderRole = "CUSTOMER",
                                        senderName = ticket.customerName,
                                        messageText = replyText
                                    )
                                    replyText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = SatnaOrangePrimary)
                        }
                    }
                }

                if (ticket.status == TicketStatus.RESOLVED) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rate Customer Support Resolution:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    ratingSelected = star
                                    repository.rateSupportTicket(ticket.id, star)
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    if (star <= ratingSelected) Icons.Default.Star else Icons.Outlined.Star,
                                    contentDescription = null,
                                    tint = if (star <= ratingSelected) Color(0xFFFFB300) else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close Window")
            }
        }
    )
}

@Composable
fun FaqTabContent(
    faqList: List<FaqItem>,
    onContactSupport: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedFaqId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Orders", "Payments & Refunds", "Delivery", "Quality & Hygiene", "Account & Offers")

    val filteredFaqs = remember(faqList, searchQuery, selectedCategory) {
        faqList.filter { item ->
            val matchesCat = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() || item.question.contains(searchQuery, ignoreCase = true) || item.answer.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search FAQ questions...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(filteredFaqs) { faq ->
                val isExpanded = expandedFaqId == faq.id
                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaqId = if (isExpanded) null else faq.id }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faq.question, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Icon(
                                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null
                            )
                        }

                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(faq.answer, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onContactSupport,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SatnaTealSecondary)
        ) {
            Icon(Icons.Default.HeadsetMic, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Didn't find your answer? Chat with 24/7 AI Support")
        }
    }
}

@Composable
fun CreateSupportTicketModal(
    repository: SatnaEatsRepository,
    selectedOrder: Order?,
    allOrders: List<Order>,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(TicketCategory.ORDER_ISSUE) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var chosenOrder by remember { mutableStateOf(selectedOrder) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Support Ticket", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(TicketCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name.replace("_", " "), fontSize = 10.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject / Issue Title", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Missing chutney, Delayed delivery", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Detailed Explanation", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && description.isNotBlank()) {
                        repository.createSupportTicket(
                            category = selectedCategory,
                            subject = subject,
                            description = description,
                            orderId = chosenOrder?.id
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SatnaOrangePrimary)
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
