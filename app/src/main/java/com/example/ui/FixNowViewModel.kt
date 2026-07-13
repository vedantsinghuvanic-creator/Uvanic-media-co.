package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

sealed class AppScreen {
    object Auth : AppScreen()
    object CustomerHome : AppScreen()
    data class CustomerNewBooking(val category: String) : AppScreen()
    data class CustomerBookingDetail(val bookingId: Long) : AppScreen()
    data class CustomerChat(val bookingId: Long) : AppScreen()
    object CustomerHistory : AppScreen()
    object CustomerNotifications : AppScreen()
    object CustomerSupportChat : AppScreen()
    object CustomerProfile : AppScreen()
    
    object ProfessionalDashboard : AppScreen()
    data class ProfessionalJobDetail(val bookingId: Long) : AppScreen()
    object ProfessionalWallet : AppScreen()
    object ProfessionalProfile : AppScreen()
    
    object AdminDashboard : AppScreen()
    object AdminManageUsers : AppScreen()
    object AdminSupportTickets : AppScreen()
}

class FixNowViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = FixNowRepository(
        database.userProfileDao(),
        database.bookingDao(),
        database.chatMessageDao(),
        database.appNotificationDao(),
        database.supportTicketDao()
    )

    // Current screen
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Auth)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Navigation Backstack
    private val screenBackstack = mutableListOf<AppScreen>()

    // Current User ID
    private val _currentUserId = MutableStateFlow<String>("customer")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    // Current active profile
    val currentUserProfile: StateFlow<UserProfile?> = _currentUserId.flatMapLatest { id ->
        repository.getUserProfileFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // All bookings list
    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All notifications list
    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All support tickets
    val allSupportTickets: StateFlow<List<SupportTicket>> = repository.allSupportTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulated tech coordinates for map tracking
    private val _techCoordinates = MutableStateFlow<Pair<Float, Float>>(Pair(19.0760f, 72.8777f)) // Mumbai baseline
    val techCoordinates: StateFlow<Pair<Float, Float>> = _techCoordinates.asStateFlow()

    // Simulated tech ETA
    private val _techEtaMinutes = MutableStateFlow<Int>(15)
    val techEtaMinutes: StateFlow<Int> = _techEtaMinutes.asStateFlow()

    // AI chat list (for Support Bot)
    private val _aiChatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(Pair("Hello! I am your FixNow AI Support Assistant. How can I help you today?", true))
    )
    val aiChatMessages: StateFlow<List<Pair<String, Boolean>>> = _aiChatMessages.asStateFlow()

    // AI recommendations and estimations states
    private val _aiCategoryRecommendation = MutableStateFlow<String?>(null)
    val aiCategoryRecommendation: StateFlow<String?> = _aiCategoryRecommendation.asStateFlow()

    private val _aiCostEstimation = MutableStateFlow<Pair<String, String>?>(null) // Explanation, Range
    val aiCostEstimation: StateFlow<Pair<String, String>?> = _aiCostEstimation.asStateFlow()

    private val _isAiLoading = MutableStateFlow<Boolean>(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Active city list
    val cities = listOf("Mumbai", "Delhi", "Bengaluru", "Hyderabad", "Pune", "Chennai")

    // Service pricing lists
    val serviceBasePricing = mapOf(
        "Electrician" to "₹199 Base Fare (includes 30 mins diagnostics)",
        "Plumber" to "₹249 Base Fare (includes 30 mins diagnostics)",
        "Carpenter" to "₹299 Base Fare (includes 30 mins diagnostics)",
        "AC Technician" to "₹399 Base Fare (includes deep service check)",
        "Painter" to "₹499 Base Fare (includes full room consultation)",
        "Appliance Repair" to "₹299 Base Fare (includes complete diagnostic)",
        "Cleaner" to "₹349 Base Fare (includes standard single-room service)",
        "Pest Control" to "₹599 Base Fare (includes initial treatment)"
    )

    init {
        // Seed default user profiles and mock data on launch if empty
        viewModelScope.launch {
            if (repository.getUserProfile("customer") == null) {
                // Customer Profile
                repository.insertOrUpdateProfile(
                    UserProfile(
                        id = "customer",
                        name = "Vedant Singh",
                        role = "CUSTOMER",
                        phoneNumber = "+91 98765 43210",
                        email = "itzvedantsingh@gmail.com",
                        membership = "Platinum",
                        walletBalance = 1500.0,
                        referralCode = "FIXNOW50",
                        serviceCity = "Mumbai"
                    )
                )
                // Service Professional Profile
                repository.insertOrUpdateProfile(
                    UserProfile(
                        id = "professional",
                        name = "Rajesh Kumar",
                        role = "PROFESSIONAL",
                        phoneNumber = "+91 99887 76655",
                        email = "rajesh.services@fixnow.com",
                        isVerified = true,
                        aadhaarId = "1234-5678-9012",
                        bankAccount = "HDFC0001234 - A/C 50100223344",
                        userRating = 4.9f,
                        acceptanceRate = 98.0f,
                        walletBalance = 3450.0,
                        skills = "Electrician, Appliance Repair",
                        serviceCity = "Mumbai"
                    )
                )
                // Admin Profile
                repository.insertOrUpdateProfile(
                    UserProfile(
                        id = "admin",
                        name = "System Admin",
                        role = "ADMIN",
                        phoneNumber = "+91 90000 11111",
                        email = "admin@fixnow.com",
                        serviceCity = "Global"
                    )
                )

                // Add seed notification
                repository.insertNotification(
                    AppNotification(
                        title = "Welcome to FixNow!",
                        message = "Your on-demand service app is ready. Use promo code **FIXNOW50** for ₹50 off your first booking!",
                        type = "PROMO"
                    )
                )
            }
        }
    }

    // Navigation methods
    fun navigateTo(screen: AppScreen) {
        screenBackstack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (screenBackstack.isNotEmpty()) {
            _currentScreen.value = screenBackstack.removeAt(screenBackstack.size - 1)
        } else {
            // fallback if stack is empty
            when (_currentUserId.value) {
                "customer" -> _currentScreen.value = AppScreen.CustomerHome
                "professional" -> _currentScreen.value = AppScreen.ProfessionalDashboard
                "admin" -> _currentScreen.value = AppScreen.AdminDashboard
                else -> _currentScreen.value = AppScreen.Auth
            }
        }
    }

    // Role Switching or Login
    fun loginAs(userId: String) {
        _currentUserId.value = userId
        screenBackstack.clear()
        when (userId) {
            "customer" -> _currentScreen.value = AppScreen.CustomerHome
            "professional" -> _currentScreen.value = AppScreen.ProfessionalDashboard
            "admin" -> _currentScreen.value = AppScreen.AdminDashboard
        }
    }

    // Sign out
    fun signOut() {
        _currentScreen.value = AppScreen.Auth
        screenBackstack.clear()
    }

    // AI Support Chat Bot Interaction
    fun sendUserMessageToSupport(message: String) {
        if (message.isBlank()) return
        val currentList = _aiChatMessages.value.toMutableList()
        currentList.add(Pair(message, false))
        _aiChatMessages.value = currentList

        _isAiLoading.value = true
        viewModelScope.launch {
            val systemPrompt = """
                You are FixNow Assistant, an elite, professional customer support chatbot for FixNow, an on-demand home services platform like Uber. 
                Be friendly, highly concise, and guide the user on bookings, technician assignment, cancellations, and premium memberships. 
                Refer to standard services: Electrician, Plumber, Carpenter, AC Technician, Painter, Cleaner, Pest Control.
                Keep answers strictly under 3 sentences. Do not show raw JSON or code.
            """.trimIndent()
            
            val aiResponse = GeminiService.getCompletion(message, systemPrompt)
            _isAiLoading.value = false
            
            val newList = _aiChatMessages.value.toMutableList()
            newList.add(Pair(aiResponse, true))
            _aiChatMessages.value = newList
        }
    }

    // AI service category recommendation based on text
    fun recommendServiceCategory(complaint: String) {
        if (complaint.isBlank()) {
            _aiCategoryRecommendation.value = null
            return
        }
        _isAiLoading.value = true
        viewModelScope.launch {
            val prompt = """
                Based on this user's description of their household issue, recommend the single best service category from these options: 
                Electrician, Plumber, Carpenter, AC Technician, Painter, Appliance Repair, Cleaner, Pest Control.
                
                Issue: "$complaint"
                
                Return ONLY the exact category name. Nothing else, no explanation.
            """.trimIndent()
            
            val result = GeminiService.getCompletion(prompt).trim()
            _isAiLoading.value = false
            // Verify it's a valid category, otherwise default
            val validCategories = listOf("Electrician", "Plumber", "Carpenter", "AC Technician", "Painter", "Appliance Repair", "Cleaner", "Pest Control")
            val matching = validCategories.firstOrNull { it.equals(result, ignoreCase = true) }
            _aiCategoryRecommendation.value = matching ?: "Electrician"
        }
    }

    fun clearCategoryRecommendation() {
        _aiCategoryRecommendation.value = null
    }

    // AI repair cost estimation based on description (and optional simulated image)
    fun estimateRepairCost(description: String, imageLabel: String = "") {
        _isAiLoading.value = true
        viewModelScope.launch {
            val imgContext = if (imageLabel.isNotEmpty()) "and the uploaded photo showing '$imageLabel'" else "no photo uploaded"
            val prompt = """
                You are the FixNow Smart Estimator AI. Estimate the repair cost for a technician visit in Mumbai, India.
                Description: "$description"
                Visual: $imgContext
                
                Analyze the likely problem and give a reasonable cost range in Indian Rupees (₹) and a brief 1-sentence diagnostic explanation of what is wrong.
                
                Return a JSON object with exactly two keys:
                "range": e.g. "₹600 - ₹900"
                "explanation": "Brief diagnostic explanation..."
                
                Do not wrap in markdown or write anything else than the JSON object.
            """.trimIndent()

            val rawResult = GeminiService.getCompletion(prompt).trim()
            _isAiLoading.value = false
            try {
                // Try parsing JSON, otherwise fallback
                val jsonStr = rawResult.substringAfter("{").substringBeforeLast("}")
                val cleanJson = "{$jsonStr}"
                val json = JSONObject(cleanJson)
                val range = json.getString("range")
                val explanation = json.getString("explanation")
                _aiCostEstimation.value = Pair(explanation, range)
            } catch (e: Exception) {
                // If parsing fails, extract or set robust default
                Log.w("ViewModel", "Failed to parse JSON from AI: $rawResult")
                _aiCostEstimation.value = Pair(
                    "Standard inspection and correction of $description.",
                    "₹499 - ₹899"
                )
            }
        }
    }

    fun clearCostEstimation() {
        _aiCostEstimation.value = null
    }

    // Booking actions
    fun createBooking(
        category: String,
        description: String,
        date: String,
        timeSlot: String,
        address: String,
        priceRange: String,
        photoLabel: String = ""
    ) {
        viewModelScope.launch {
            val booking = Booking(
                customerId = "customer",
                customerName = "Vedant Singh",
                professionalId = "professional", // Pre-assign the professional for this demo
                professionalName = "Rajesh Kumar",
                serviceCategory = category,
                problemDescription = description,
                status = "PENDING",
                date = date,
                timeSlot = timeSlot,
                address = address,
                estimatedPriceRange = priceRange,
                photoBase64 = photoLabel
            )
            
            val bookingId = repository.insertBooking(booking)
            
            // Add system confirmation notification
            repository.insertNotification(
                AppNotification(
                    title = "Booking Confirmed!",
                    message = "Your request for $category service on $date has been received. A technician will be assigned shortly.",
                    type = "BOOKING"
                )
            )

            // Direct tracking immediately to show the live flow
            navigateTo(AppScreen.CustomerBookingDetail(bookingId))

            // Simulate technician assignment progress
            simulateTechnicianJourney(bookingId)
        }
    }

    private fun simulateTechnicianJourney(bookingId: Long) {
        viewModelScope.launch {
            // Step 1: Wait 5 seconds, then mark as ASSIGNED
            delay(5000)
            val bookingAssigned = repository.getBookingById(bookingId)
            if (bookingAssigned != null && bookingAssigned.status == "PENDING") {
                repository.updateBooking(bookingAssigned.copy(status = "ASSIGNED"))
                repository.insertNotification(
                    AppNotification(
                        title = "Technician Assigned",
                        message = "Our certified expert Rajesh Kumar is assigned to your ${bookingAssigned.serviceCategory} service.",
                        type = "BOOKING"
                    )
                )
                repository.insertChatMessage(
                    ChatMessage(
                        bookingId = bookingId,
                        senderRole = "SYSTEM",
                        messageText = "Rajesh Kumar has been assigned to your request."
                    )
                )
                repository.insertChatMessage(
                    ChatMessage(
                        bookingId = bookingId,
                        senderRole = "PROFESSIONAL",
                        messageText = "Hello! I am on my way to your address. Feel free to share any specifics."
                    )
                )

                // Simulate Technician Movement on Map
                // Customer coordinates (19.0760, 72.8777)
                // Tech starts at (19.0650, 72.8650)
                var currentLat = 19.0650f
                var currentLng = 72.8650f
                val destLat = 19.0760f
                val destLng = 72.8777f

                _techCoordinates.value = Pair(currentLat, currentLng)
                _techEtaMinutes.value = 15

                for (step in 1..5) {
                    delay(3000) // update every 3 seconds
                    currentLat += (destLat - currentLat) / (6 - step)
                    currentLng += (destLng - currentLng) / (6 - step)
                    _techCoordinates.value = Pair(currentLat, currentLng)
                    _techEtaMinutes.value = (15 - step * 3).coerceAtLeast(1)
                }

                // Step 2: Tech Arrives
                delay(2000)
                val bookingArrived = repository.getBookingById(bookingId)
                if (bookingArrived != null && bookingArrived.status == "ASSIGNED") {
                    repository.updateBooking(bookingArrived.copy(status = "STARTED"))
                    repository.insertNotification(
                        AppNotification(
                            title = "Technician Arrived",
                            message = "Rajesh Kumar has arrived at your location and started the service.",
                            type = "BOOKING"
                        )
                    )
                    repository.insertChatMessage(
                        ChatMessage(
                            bookingId = bookingId,
                            senderRole = "SYSTEM",
                            messageText = "Service started by Rajesh Kumar."
                        )
                    )
                }
            }
        }
    }

    // Professional updates job status
    fun updateJobStatus(bookingId: Long, newStatus: String, completionPhoto: String = "") {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId)
            if (booking != null) {
                var finalPrice = booking.finalPrice
                var payStatus = booking.paymentStatus
                if (newStatus == "COMPLETED") {
                    // Extract numerical values from range or default
                    finalPrice = 650.0 // Completed job standard pricing
                    payStatus = "PENDING"
                    
                    // Add earnings to professional wallet
                    val prof = repository.getUserProfile("professional")
                    if (prof != null) {
                        repository.insertOrUpdateProfile(
                            prof.copy(walletBalance = prof.walletBalance + finalPrice)
                        )
                    }

                    repository.insertNotification(
                        AppNotification(
                            title = "Job Completed!",
                            message = "Your service for ${booking.serviceCategory} is completed. Please complete payment of ₹$finalPrice.",
                            type = "PAYMENT"
                        )
                    )
                }

                repository.updateBooking(
                    booking.copy(
                        status = newStatus,
                        finalPrice = finalPrice,
                        paymentStatus = payStatus,
                        photoBase64 = if (completionPhoto.isNotEmpty()) completionPhoto else booking.photoBase64
                    )
                )

                val statusMessage = when (newStatus) {
                    "STARTED" -> "Technician started work."
                    "COMPLETED" -> "Service completed! Invoice generated for ₹$finalPrice."
                    "CANCELLED" -> "Service was cancelled."
                    else -> "Status updated to $newStatus"
                }

                repository.insertChatMessage(
                    ChatMessage(
                        bookingId = bookingId,
                        senderRole = "SYSTEM",
                        messageText = statusMessage
                    )
                )
            }
        }
    }

    // Customer processes checkout payment
    fun payForBooking(bookingId: Long, method: String) {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId)
            if (booking != null) {
                repository.updateBooking(
                    booking.copy(
                        paymentStatus = "PAID",
                        paymentMethod = method
                    )
                )

                // Subtract from customer wallet if wallet selected
                if (method == "Wallet") {
                    val customer = repository.getUserProfile("customer")
                    if (customer != null) {
                        repository.insertOrUpdateProfile(
                            customer.copy(walletBalance = (customer.walletBalance - booking.finalPrice).coerceAtLeast(0.0))
                        )
                    }
                }

                repository.insertNotification(
                    AppNotification(
                        title = "Payment Successful!",
                        message = "Payment of ₹${booking.finalPrice} via $method was successful. Invoice has been sent.",
                        type = "PAYMENT"
                    )
                )

                repository.insertChatMessage(
                    ChatMessage(
                        bookingId = bookingId,
                        senderRole = "SYSTEM",
                        messageText = "Payment successful of ₹${booking.finalPrice} via $method."
                    )
                )
            }
        }
    }

    // Submit review and rating
    fun submitReview(bookingId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            val booking = repository.getBookingById(bookingId)
            if (booking != null) {
                repository.updateBooking(
                    booking.copy(
                        ratingStars = rating,
                        ratingComment = comment
                    )
                )
                
                // Update average professional rating
                val prof = repository.getUserProfile(booking.professionalId)
                if (prof != null) {
                    // Quick weighted update
                    val newRating = ((prof.userRating * 9) + rating) / 10f
                    repository.insertOrUpdateProfile(
                        prof.copy(userRating = newRating)
                    )
                }
            }
        }
    }

    // In-app chat messaging
    fun sendChatMessage(bookingId: Long, senderRole: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertChatMessage(
                ChatMessage(
                    bookingId = bookingId,
                    senderRole = senderRole,
                    messageText = text
                )
            )
        }
    }

    // Professional profile updates
    fun saveProfessionalProfile(name: String, skills: String, bank: String, aadhaar: String) {
        viewModelScope.launch {
            val prof = repository.getUserProfile("professional")
            if (prof != null) {
                repository.insertOrUpdateProfile(
                    prof.copy(
                        name = name,
                        skills = skills,
                        bankAccount = bank,
                        aadhaarId = aadhaar,
                        isVerified = aadhaar.isNotBlank() && bank.isNotBlank()
                    )
                )
            }
        }
    }

    // Admin updates services pricing
    fun updateServicePricing(category: String, info: String) {
        // Simple in-memory change or notification
        viewModelScope.launch {
            repository.insertNotification(
                AppNotification(
                    title = "Service Updated",
                    message = "Pricing updated for $category: $info",
                    type = "PROMO"
                )
            )
        }
    }

    // Support ticket submit
    fun submitSupportTicket(subject: String, message: String) {
        viewModelScope.launch {
            repository.insertSupportTicket(
                SupportTicket(
                    subject = subject,
                    message = message
                )
            )
            repository.insertNotification(
                AppNotification(
                    title = "Support Ticket Opened",
                    message = "Our support team is reviewing your ticket regarding '$subject'.",
                    type = "PROMO"
                )
            )
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun resolveSupportTicket(ticket: SupportTicket) {
        viewModelScope.launch {
            repository.insertSupportTicket(ticket.copy(status = "RESOLVED"))
        }
    }

    fun withdrawProfessionalFunds(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val prof = repository.getUserProfile("professional")
            if (prof != null) {
                repository.insertOrUpdateProfile(prof.copy(walletBalance = 0.0))
            }
            onSuccess()
        }
    }
}
