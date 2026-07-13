package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppNotification
import com.example.data.Booking
import com.example.data.SupportTicket
import com.example.data.UserProfile
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixNowApp(viewModel: FixNowViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    val allNotifications by viewModel.allNotifications.collectAsStateWithLifecycle()
    
    val unreadNotificationsCount = allNotifications.count { !it.isRead }

    Scaffold(
        bottomBar = {
            if (currentScreen != AppScreen.Auth) {
                BottomNavigationBar(
                    currentScreen = currentScreen,
                    currentUserRole = currentUserProfile?.role ?: "CUSTOMER",
                    unreadCount = unreadNotificationsCount,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onSwitchRole = { role ->
                        val userId = when (role) {
                            "CUSTOMER" -> "customer"
                            "PROFESSIONAL" -> "professional"
                            "ADMIN" -> "admin"
                            else -> "customer"
                        }
                        viewModel.loginAs(userId)
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val screen = currentScreen) {
                is AppScreen.Auth -> AuthScreen(viewModel)
                is AppScreen.CustomerHome -> CustomerHomeScreen(viewModel)
                is AppScreen.CustomerNewBooking -> CustomerNewBookingScreen(viewModel, screen.category)
                is AppScreen.CustomerBookingDetail -> CustomerBookingDetailScreen(viewModel, screen.bookingId)
                is AppScreen.CustomerChat -> CustomerChatScreen(viewModel, screen.bookingId)
                is AppScreen.CustomerHistory -> CustomerHistoryScreen(viewModel)
                is AppScreen.CustomerNotifications -> CustomerNotificationsScreen(viewModel)
                is AppScreen.CustomerSupportChat -> CustomerSupportChatScreen(viewModel)
                is AppScreen.CustomerProfile -> CustomerProfileScreen(viewModel)
                
                is AppScreen.ProfessionalDashboard -> ProfessionalDashboardScreen(viewModel)
                is AppScreen.ProfessionalJobDetail -> ProfessionalJobDetailScreen(viewModel, screen.bookingId)
                is AppScreen.ProfessionalWallet -> ProfessionalWalletScreen(viewModel)
                is AppScreen.ProfessionalProfile -> ProfessionalProfileScreen(viewModel)
                
                is AppScreen.AdminDashboard -> AdminDashboardScreen(viewModel)
                is AppScreen.AdminManageUsers -> AdminManageUsersScreen(viewModel)
                is AppScreen.AdminSupportTickets -> AdminSupportTicketsScreen(viewModel)
            }
        }
    }
}

// --- COMMON COMPONENT: BOTTOM BAR & ROLE SWITCHER ---
@Composable
fun BottomNavigationBar(
    currentScreen: AppScreen,
    currentUserRole: String,
    unreadCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onSwitchRole: (String) -> Unit
) {
    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            // Workspace Role Switcher Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueLight)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Role",
                        tint = BluePrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Workspace: $currentUserRole",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BluePrimary
                    )
                }
                
                Row {
                    Text(
                        text = "Switch to: ",
                        fontSize = 11.sp,
                        color = LightTextSecondary,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    if (currentUserRole != "CUSTOMER") {
                        Text(
                            text = "Customer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onSwitchRole("CUSTOMER") }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (currentUserRole != "PROFESSIONAL") {
                        Text(
                            text = "Professional",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentSuccess,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onSwitchRole("PROFESSIONAL") }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (currentUserRole != "ADMIN") {
                        Text(
                            text = "Admin",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentOrange,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onSwitchRole("ADMIN") }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Actual Tabs based on role
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                when (currentUserRole) {
                    "CUSTOMER" -> {
                        val isHome = currentScreen is AppScreen.CustomerHome
                        val isHistory = currentScreen is AppScreen.CustomerHistory
                        val isNotifications = currentScreen is AppScreen.CustomerNotifications
                        val isBot = currentScreen is AppScreen.CustomerSupportChat
                        
                        TabItem(Icons.Default.Home, "Explore", isHome) { onNavigate(AppScreen.CustomerHome) }
                        TabItem(Icons.Default.History, "Bookings", isHistory) { onNavigate(AppScreen.CustomerHistory) }
                        TabItem(
                            icon = Icons.Default.Notifications,
                            label = "Alerts",
                            isSelected = isNotifications,
                            badgeCount = unreadCount
                        ) { onNavigate(AppScreen.CustomerNotifications) }
                        TabItem(Icons.Default.Chat, "AI Helper", isBot) { onNavigate(AppScreen.CustomerSupportChat) }
                        TabItem(Icons.Default.Person, "Profile", currentScreen is AppScreen.CustomerProfile) { onNavigate(AppScreen.CustomerProfile) }
                    }
                    "PROFESSIONAL" -> {
                        val isDash = currentScreen is AppScreen.ProfessionalDashboard
                        val isWallet = currentScreen is AppScreen.ProfessionalWallet
                        val isProfile = currentScreen is AppScreen.ProfessionalProfile
                        
                        TabItem(Icons.Default.Home, "Jobs", isDash) { onNavigate(AppScreen.ProfessionalDashboard) }
                        TabItem(Icons.Default.Wallet, "Wallet", isWallet) { onNavigate(AppScreen.ProfessionalWallet) }
                        TabItem(Icons.Default.Person, "Profile", isProfile) { onNavigate(AppScreen.ProfessionalProfile) }
                    }
                    "ADMIN" -> {
                        val isDash = currentScreen is AppScreen.AdminDashboard
                        val isTickets = currentScreen is AppScreen.AdminSupportTickets
                        
                        TabItem(Icons.Default.Home, "Overview", isDash) { onNavigate(AppScreen.AdminDashboard) }
                        TabItem(Icons.Default.Chat, "Tickets", isTickets) { onNavigate(AppScreen.AdminSupportTickets) }
                    }
                }
            }
        }
    }
}

@Composable
fun TabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) BluePrimary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-4).dp)
                            .background(Color.Red, CircleShape)
                            .size(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = badgeCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = if (isSelected) BluePrimary else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// --- SCREEN 1: AUTHENTICATION (PHONE & GOOGLE SIGN IN) ---
@Composable
fun AuthScreen(viewModel: FixNowViewModel) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var showOtpField by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BluePrimary, DarkBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stylish Icon badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(BlueLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Logo",
                        tint = BluePrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "FixNow",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = BluePrimary
                )
                Text(
                    text = "Uber for Home Services",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (!showOtpField) {
                    Text(
                        text = "Login or Create Profile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 XXXXX XXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Icon(Icons.Default.Call, contentDescription = "phone") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text("Vedant Singh") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (errorMsg.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorMsg, color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (phoneNumber.length < 10) {
                                errorMsg = "Please enter a valid 10-digit phone number"
                            } else {
                                errorMsg = ""
                                showOtpField = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Get OTP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Text(
                        text = "Enter Verification Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "OTP sent to $phoneNumber",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = { Text("6-Digit OTP") },
                        placeholder = { Text("123456") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.loginAs("customer") // Log in default customer
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify & Proceed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showOtpField = false }) {
                        Text("Back to Edit", color = BluePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Google sign in simulation
                Button(
                    onClick = {
                        viewModel.loginAs("customer")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("G  ", fontWeight = FontWeight.Black, fontSize = 18.sp, color = BluePrimary)
                        Text("Sign In with Google", color = Color.Black, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// --- SCREEN 2: CUSTOMER EXPLORE HOME SCREEN ---
@Composable
fun CustomerHomeScreen(viewModel: FixNowViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val aiCategorySuggestion by viewModel.aiCategoryRecommendation.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    val categories = listOf(
        Pair("Electrician", Icons.Default.Build),
        Pair("Plumber", Icons.Default.WaterDamage),
        Pair("Carpenter", Icons.Default.Handyman),
        Pair("AC Technician", Icons.Default.AcUnit),
        Pair("Painter", Icons.Default.FormatPaint),
        Pair("Appliance Repair", Icons.Default.Tv),
        Pair("Cleaner", Icons.Default.CleaningServices),
        Pair("Pest Control", Icons.Default.BugReport)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Hero Header & SOS
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello Vedant,",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "What help do you need today?",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                // SOS Trigger button
                var showSosAlert by remember { mutableStateOf(false) }
                Button(
                    onClick = { showSosAlert = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                if (showSosAlert) {
                    AlertDialog(
                        onDismissRequest = { showSosAlert = false },
                        confirmButton = {
                            Button(
                                onClick = { showSosAlert = false },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Call Support Now") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showSosAlert = false }) { Text("Cancel") }
                        },
                        title = { Text("Emergency SOS Alert") },
                        text = { Text("Press Call to immediately dispatch high-priority 30-minute priority emergency backup and open local customer support hotline.") }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Search bar with AI assistance
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it 
                            if (it.isBlank()) viewModel.clearCategoryRecommendation()
                        },
                        placeholder = { Text("Search or type your home issue...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "search") },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.clearCategoryRecommendation()
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            viewModel.recommendServiceCategory(searchQuery)
                            keyboardController?.hide()
                        }),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.recommendServiceCategory(searchQuery)
                                keyboardController?.hide()
                            },
                            enabled = searchQuery.isNotBlank() && !isAiLoading
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "AI", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Recommend Category with AI")
                        }

                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        }
                    }

                    aiCategorySuggestion?.let { category ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlueLight),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable {
                                    viewModel.navigateTo(AppScreen.CustomerNewBooking(category))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Verified", tint = BluePrimary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("AI Suggestion: $category", fontWeight = FontWeight.Bold, color = BluePrimary)
                                    Text("Tap to book this service category now.", fontSize = 11.sp, color = BluePrimary)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Emergency Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFECEF)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.navigateTo(AppScreen.CustomerNewBooking("Electrician"))
                    }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFFF3B30), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Emergency", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Emergency 30-Min Booking",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF3B30)
                        )
                        Text(
                            text = "Immediate dispatch of top professionals for power cuts, major pipe leaks, lockout issues.",
                            fontSize = 11.sp,
                            color = Color.Black
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Categories Header
        item {
            Text(
                text = "Services Categories",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Grid of Categories (8 Categories, 2 per row)
        val chunked = categories.chunked(2)
        items(chunked) { rowCategories ->
            Row(modifier = Modifier.fillMaxWidth()) {
                for (cat in rowCategories) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp)
                            .clickable {
                                viewModel.navigateTo(AppScreen.CustomerNewBooking(cat.first))
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(BlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = cat.second,
                                    contentDescription = cat.first,
                                    tint = BluePrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = cat.first,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Popular Offers & Discounts
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Exclusive Offers",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Get 50% Off First Service!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Use code FIXNOW50 at payment checkout. Max value ₹150.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.navigateTo(AppScreen.CustomerNewBooking("Cleaner"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Book", color = BluePrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Membership plans banner
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                border = BorderStroke(1.dp, Color(0xFFD8B4FE)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE9D5FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Premium", tint = Color(0xFF7E22CE))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Join Platinum Club", fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE))
                        Text("Get free inspections, zero service fees & 30-min guaranteed priorities.", fontSize = 11.sp, color = Color.Black)
                    }
                    Text(
                        "Join",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF7E22CE),
                        modifier = Modifier.clickable { }
                    )
                }
            }
        }
    }
}

// --- SCREEN 3: BOOKING FLOW & AI COST ESTIMATION SCREEN ---
@Composable
fun CustomerNewBookingScreen(viewModel: FixNowViewModel, category: String) {
    var description by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Today, 13 July") }
    var selectedTimeSlot by remember { mutableStateOf("10:00 AM - 11:00 AM") }
    
    // AI Estimation outputs
    val aiCostEst by viewModel.aiCostEstimation.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val availableDates = listOf("Today, 13 July", "Tomorrow, 14 July", "Wednesday, 15 July")
    val availableSlots = listOf("09:00 AM - 10:00 AM", "10:00 AM - 11:00 AM", "02:00 PM - 03:00 PM", "05:00 PM - 06:00 PM")

    // Simulated gallery options for diagnostics
    var showGallerySelection by remember { mutableStateOf(false) }
    var selectedPhotoName by remember { mutableStateOf("") }

    val mockPhotoOptions = listOf(
        Pair("Burnt Socket / Wiring Spark", "electric_fault.png"),
        Pair("Water Pipe Leakage", "pipe_leak.png"),
        Pair("Clogged Kitchen Drain", "clogged_drain.png"),
        Pair("AC Gas Service / Filter Dust", "ac_dust.png"),
        Pair("Broken Door Lock / Wood Split", "door_broken.png")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Book $category", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Problem Description input
        item {
            Text("Describe the Issue", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("What needs repairing? (e.g. 'Kitchen sink is leaking water onto the floor')") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Photo uploads & AI estimation trigger
        item {
            Text("Add Photo for AI Cost Estimate", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showGallerySelection = true }
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "upload", tint = BluePrimary)
                        Text(
                            if (selectedPhotoName.isEmpty()) "Attach Photo" else selectedPhotoName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.estimateRepairCost(description, selectedPhotoName)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                    shape = RoundedCornerShape(12.dp),
                    enabled = description.isNotBlank() && !isAiLoading
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Settings, contentDescription = "AI", tint = BluePrimary)
                        Text("Calculate AI Price", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            if (isAiLoading) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI analyzing issue details and setting diagnostics...", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Display AI Estimate Result
            aiCostEst?.let { estimation ->
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "AI", tint = AccentSuccess)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Estimation", fontWeight = FontWeight.Bold, color = AccentSuccess)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(estimation.first, fontSize = 12.sp, color = Color.Black)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Estimated Cost Range:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(estimation.second, fontWeight = FontWeight.Black, color = AccentSuccess, fontSize = 14.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Date selection
        item {
            Text("Select Date", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableDates) { date ->
                    val isSelected = selectedDate == date
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BluePrimary else Color.White
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedDate = date }
                            .border(1.dp, if (isSelected) BluePrimary else Color.LightGray, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = date,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Slot selection
        item {
            Text("Select Time Slot", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(availableSlots) { slot ->
                    val isSelected = selectedTimeSlot == slot
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) BluePrimary else Color.White
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTimeSlot = slot }
                            .border(1.dp, if (isSelected) BluePrimary else Color.LightGray, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = slot,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Address description
        item {
            Text("Service Address", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                placeholder = { Text("E.g. A-402 Shanti Towers, Bandra West, Mumbai") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Home") }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pricing Info & Book Action
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BlueLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Base Rate Diagnostic Fare", fontSize = 11.sp, color = BluePrimary)
                        Text(viewModel.serviceBasePricing[category] ?: "₹199 Booking fee", fontWeight = FontWeight.Bold, color = BluePrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalEstimate = aiCostEst?.second ?: "₹199 - ₹499"
                    viewModel.createBooking(
                        category = category,
                        description = description.ifBlank { "Standard repair request" },
                        date = selectedDate,
                        timeSlot = selectedTimeSlot,
                        address = address.ifBlank { "Home Address, Mumbai" },
                        priceRange = finalEstimate,
                        photoLabel = selectedPhotoName
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirm & Search Professional", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    // Gallery Picker Simulation Dialog
    if (showGallerySelection) {
        Dialog(onDismissRequest = { showGallerySelection = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Select Diagnostic Photo", fontWeight = FontWeight.Bold, fontSize = 16.dp.value.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    mockPhotoOptions.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPhotoName = p.first
                                    showGallerySelection = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Photo", tint = BluePrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(p.first, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 4: BOOKING DETAIL & LIVE TECH MAP TRACKING SCREEN ---
@Composable
fun CustomerBookingDetailScreen(viewModel: FixNowViewModel, bookingId: Long) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val booking = bookings.find { it.id == bookingId } ?: return

    val techCoords by viewModel.techCoordinates.collectAsStateWithLifecycle()
    val techEta by viewModel.techEtaMinutes.collectAsStateWithLifecycle()

    var showPaymentDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.CustomerHome) }) {
                    Icon(Icons.Default.Home, contentDescription = "Home")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Booking Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Primary Status Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(booking.serviceCategory, fontWeight = FontWeight.Black, fontSize = 20.sp, color = BluePrimary)
                            Text("ID: #FXN-$bookingId", fontSize = 11.sp, color = Color.Gray)
                        }

                        val (statusText, statusColor) = when (booking.status) {
                            "PENDING" -> Pair("Searching Expert...", AccentOrange)
                            "ASSIGNED" -> Pair("On The Way", BluePrimary)
                            "STARTED" -> Pair("In Progress", AccentSuccess)
                            "COMPLETED" -> Pair("Job Done", AccentSuccess)
                            else -> Pair("Cancelled", Color.Red)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                statusText,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        Icon(Icons.Default.Build, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("${booking.date} • ${booking.timeSlot}", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Icon(Icons.Default.Home, contentDescription = "Address", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(booking.address, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live map tracking Canvas (for ASSIGNED and STARTED states)
        if (booking.status == "ASSIGNED" || booking.status == "STARTED") {
            item {
                Text("Live Technician Tracking", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Custom Canvas Map Simulation
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            // Draw a light grid represent streets
                            val paintLines = Color.LightGray.copy(alpha = 0.3f)
                            for (x in 0..w.toInt() step 60) {
                                drawLine(paintLines, Offset(x.toFloat(), 0f), Offset(x.toFloat(), h))
                            }
                            for (y in 0..h.toInt() step 60) {
                                drawLine(paintLines, Offset(0f, y.toFloat()), Offset(w, y.toFloat()))
                            }

                            // Draw Route path dashed line
                            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            drawLine(
                                color = BluePrimary,
                                start = Offset(w * 0.2f, h * 0.7f), // Tech start
                                end = Offset(w * 0.7f, h * 0.3f), // Customer destination
                                strokeWidth = 4f,
                                pathEffect = pathEffect
                            )

                            // Customer node (Destination)
                            drawCircle(Color.White, radius = 16f, center = Offset(w * 0.7f, h * 0.3f))
                            drawCircle(BluePrimary, radius = 12f, center = Offset(w * 0.7f, h * 0.3f))

                            // Tech node (Active position)
                            // Linear scale interpolation based on simulated techCoords
                            val scaleLat = (techCoords.first - 19.0650f) / (19.0760f - 19.0650f)
                            val scaleLng = (techCoords.second - 72.8650f) / (72.8777f - 72.8650f)

                            val xTech = w * 0.2f + (w * 0.5f * scaleLng)
                            val yTech = h * 0.7f - (h * 0.4f * scaleLat)

                            // Draw pulsating beacon ring
                            drawCircle(BluePrimary.copy(alpha = 0.3f), radius = 24f, center = Offset(xTech, yTech))
                            drawCircle(AccentSuccess, radius = 10f, center = Offset(xTech, yTech))
                        }

                        // ETA overlay
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                "ETA: $techEta mins • Rajesh is approaching",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Professional Assigned Profile Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("RK", color = BluePrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(booking.professionalName, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "star", tint = AccentOrange, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("4.9 Verified Expert", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        // Chat and call quick actions
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.CustomerChat(bookingId)) }) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = BluePrimary)
                        }
                        
                        var showCallSimulation by remember { mutableStateOf(false) }
                        IconButton(onClick = { showCallSimulation = true }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = AccentSuccess)
                        }

                        if (showCallSimulation) {
                            AlertDialog(
                                onDismissRequest = { showCallSimulation = false },
                                confirmButton = {
                                    Button(onClick = { showCallSimulation = false }) { Text("End Call") }
                                },
                                title = { Text("FixNow Simulated Call") },
                                text = { Text("Connecting you securely to your assigned professional, Rajesh Kumar via custom mask call service...") }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Invoice and Payment (if Completed & PENDING payment)
        if (booking.status == "COMPLETED") {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BlueLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "done", tint = BluePrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Service Invoice", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BluePrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Diagnostic base fee", fontSize = 13.sp)
                            Text("₹199.00", fontSize = 13.sp)
                        }
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Labour & repair charge", fontSize = 13.sp)
                            Text("₹451.00", fontSize = 13.sp)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Total Outstanding Balance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("₹${booking.finalPrice}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = BluePrimary)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (booking.paymentStatus == "PENDING") {
                            Button(
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                            ) {
                                Text("Pay Now (UPI, Cards, Wallet)", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = AccentSuccess.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "paid", tint = AccentSuccess)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Paid via ${booking.paymentMethod}", fontWeight = FontWeight.Bold, color = AccentSuccess)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // If not rated yet, show feedback trigger
                            if (booking.ratingStars == 0) {
                                Button(
                                    onClick = { showRatingDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                                ) {
                                    Text("Submit Rating & Reviews", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Your Review", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Row {
                                            repeat(booking.ratingStars) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        if (booking.ratingComment.isNotBlank()) {
                                            Text(booking.ratingComment, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Checkout Payment Method Sheet Dialog
    if (showPaymentDialog) {
        Dialog(onDismissRequest = { showPaymentDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Select Payment Option", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val methods = listOf("UPI / GooglePay", "Credit/Debit Card", "Wallet Balance", "Cash On Service")
                    methods.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val walletMethod = if (m.startsWith("Wallet")) "Wallet" else m
                                    viewModel.payForBooking(bookingId, walletMethod)
                                    showPaymentDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Wallet, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(m, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Rating & Feedback Submission Dialog
    if (showRatingDialog) {
        var starsSelected by remember { mutableStateOf(5) }
        var reviewText by remember { mutableStateOf("") }
        
        Dialog(onDismissRequest = { showRatingDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rate Rajesh Kumar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (i <= starsSelected) AccentOrange else Color.LightGray,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable { starsSelected = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("Write your feedback here (optional)...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.submitReview(bookingId, starsSelected, reviewText)
                            showRatingDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Feedback")
                    }
                }
            }
        }
    }
}

// --- SCREEN 5: IN-APP CHAT WITH TECHNICIAN ---
@Composable
fun CustomerChatScreen(viewModel: FixNowViewModel, bookingId: Long) {
    val messagesFlow = viewModel.repository.getChatMessages(bookingId).collectAsStateWithLifecycle(initialValue = emptyList())
    val messages = messagesFlow.value
    var inputMsg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.navigateBack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chat with Rajesh Kumar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message board list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = false,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderRole == "CUSTOMER"
                val isSys = msg.senderRole == "SYSTEM"

                if (isSys) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = msg.messageText,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    val bubbleColor = if (isMe) BluePrimary else BlueLight
                    val textColor = if (isMe) Color.White else Color.Black

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 12.dp,
                                            topEnd = 12.dp,
                                            bottomStart = if (isMe) 12.dp else 0.dp,
                                            bottomEnd = if (isMe) 0.dp else 12.dp
                                        )
                                    )
                                    .background(bubbleColor)
                                    .padding(12.dp)
                            ) {
                                Text(msg.messageText, color = textColor, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputMsg,
                onValueChange = { inputMsg = it },
                placeholder = { Text("Message...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputMsg.isNotBlank()) {
                        viewModel.sendChatMessage(bookingId, "CUSTOMER", inputMsg)
                        inputMsg = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(BluePrimary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// --- SCREEN 6: BOOKING HISTORY / PAST BOOKINGS ---
@Composable
fun CustomerHistoryScreen(viewModel: FixNowViewModel) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val customerBookings = bookings.filter { it.customerId == "customer" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Booking History", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (customerBookings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.History, contentDescription = "Empty", tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No past bookings found", fontWeight = FontWeight.Bold)
                    Text("Book services from the explore home page.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        items(customerBookings) { booking ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.navigateTo(AppScreen.CustomerBookingDetail(booking.id))
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(booking.serviceCategory, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(booking.date, fontSize = 11.sp, color = Color.Gray)
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (booking.status == "COMPLETED") AccentSuccess.copy(alpha = 0.1f) else Color.LightGray
                            )
                        ) {
                            Text(
                                booking.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (booking.status == "COMPLETED") AccentSuccess else Color.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Issue: ${booking.problemDescription}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total: ₹${booking.finalPrice.coerceAtLeast(199.0)}", fontWeight = FontWeight.Black)

                        Button(
                            onClick = {
                                viewModel.navigateTo(AppScreen.CustomerNewBooking(booking.serviceCategory))
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Rebook", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 7: NOTIFICATIONS CENTER ---
@Composable
fun CustomerNotificationsScreen(viewModel: FixNowViewModel) {
    val notifications by viewModel.allNotifications.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Alerts Center", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (notifications.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = "empty", tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("Zero active alerts", fontWeight = FontWeight.Bold)
                    Text("Updates about your requests will show up here.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        items(notifications) { notif ->
            val icon = when (notif.type) {
                "BOOKING" -> Icons.Default.Build
                "PAYMENT" -> Icons.Default.Wallet
                else -> Icons.Default.Star
            }

            val iconColor = when (notif.type) {
                "BOOKING" -> BluePrimary
                "PAYMENT" -> AccentSuccess
                else -> AccentOrange
            }

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (notif.isRead) Color.White else BlueLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        viewModel.markNotificationAsRead(notif.id)
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(iconColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(notif.message, fontSize = 12.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

// --- SCREEN 8: AI SMART CHATBOT CUSTOMER SUPPORT ---
@Composable
fun CustomerSupportChatScreen(viewModel: FixNowViewModel) {
    val chatMessages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()
    var inputField by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = "AI Support", tint = BluePrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("FixNow AI Copilot Support", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text("AI support agent available 24/7", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { chat ->
                val isAi = chat.second
                val align = if (isAi) Alignment.CenterStart else Alignment.CenterEnd
                val color = if (isAi) BlueLight else BluePrimary
                val txtColor = if (isAi) Color.Black else Color.White

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = align) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(color)
                            .padding(12.dp)
                    ) {
                        Text(chat.first, color = txtColor, fontSize = 13.sp)
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI writing response...", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputField,
                onValueChange = { inputField = it },
                placeholder = { Text("Ask anything about bookings, payments...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (inputField.isNotBlank()) {
                        viewModel.sendUserMessageToSupport(inputField)
                        inputField = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(BluePrimary, CircleShape)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

// --- SCREEN 9: CUSTOMER PROFILE & SETTINGS ---
@Composable
fun CustomerProfileScreen(viewModel: FixNowViewModel) {
    val profile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(BluePrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                profile?.name?.take(2)?.uppercase() ?: "VS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(profile?.name ?: "Vedant Singh", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(profile?.phoneNumber ?: "+91 98765 43210", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = AccentSuccess.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "Platinum Club Member",
                color = AccentSuccess,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wallet Balance
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wallet Balance", fontSize = 12.sp, color = Color.Gray)
                Text("₹${profile?.walletBalance ?: 0.0}", fontWeight = FontWeight.Black, fontSize = 24.sp, color = BluePrimary)
                
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { /* Simulated deposit */ },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueLight),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Add Money", color = BluePrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Referral Code card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Referral Code", fontSize = 11.sp, color = Color.Gray)
                    Text(profile?.referralCode ?: "FIXNOW50", fontWeight = FontWeight.Bold)
                }
                Text("Share & Earn ₹100", color = BluePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.signOut() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out", color = Color.White)
        }
    }
}

// ==========================================
// --- WORKSPACE 2: SERVICE PROFESSIONAL ---
// ==========================================
@Composable
fun ProfessionalDashboardScreen(viewModel: FixNowViewModel) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val profProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    // Professional active jobs
    val currentJobs = bookings.filter { it.status != "COMPLETED" && it.status != "CANCELLED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        item {
            Text(
                "Welcome back, ${profProfile?.name ?: "Rajesh Kumar"}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Verified Service Expert • Mumbai", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stats Row Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Today's Earnings", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${profProfile?.walletBalance ?: 3450.0}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AccentSuccess)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Jobs Rating", fontSize = 11.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = "star", tint = AccentOrange, modifier = Modifier.size(16.dp))
                            Text(" 4.9", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Acceptance", fontSize = 11.sp, color = Color.Gray)
                        Text("98%", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BluePrimary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Skill Badges list
        item {
            Text("Your Registered Skills", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Electrician", "Appliance Repair", "AC Installation").forEach { skill ->
                    Box(
                        modifier = Modifier
                            .background(BlueLight, RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(skill, color = BluePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Booking list header
        item {
            Text(
                "Your Active Assignments (${currentJobs.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        if (currentJobs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "No Jobs", tint = AccentSuccess, modifier = Modifier.size(48.dp))
                        Text("No pending jobs assigned!", fontWeight = FontWeight.Bold)
                        Text("Stay available to receive customer requests.", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        items(currentJobs) { job ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.navigateTo(AppScreen.ProfessionalJobDetail(job.id))
                    }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(job.serviceCategory, fontWeight = FontWeight.Bold)
                            Text(job.timeSlot, fontSize = 11.sp, color = Color.Gray)
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = BlueLight)
                        ) {
                            Text(
                                job.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Customer: ${job.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Issue: ${job.problemDescription}", fontSize = 12.sp, color = Color.DarkGray)
                    Text("Address: ${job.address}", fontSize = 11.sp, color = Color.Gray)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Expected: ${job.estimatedPriceRange}", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                        Button(
                            onClick = {
                                viewModel.navigateTo(AppScreen.ProfessionalJobDetail(job.id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                        ) {
                            Text("Manage Job", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 11: PROFESSIONAL JOB MANAGEMENT WORKSPACE DETAIL ---
@Composable
fun ProfessionalJobDetailScreen(viewModel: FixNowViewModel, bookingId: Long) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()
    val job = bookings.find { it.id == bookingId } ?: return

    var mockPhotoCompleted by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.navigateTo(AppScreen.ProfessionalDashboard) }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Manage Assignment", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Job Details
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(job.serviceCategory, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BluePrimary)
                    Text("Customer: ${job.customerName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Description: ${job.problemDescription}", fontSize = 13.sp)
                    Text("Address: ${job.address}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (job.photoBase64.isNotBlank()) {
                        Card(colors = CardDefaults.cardColors(containerColor = BlueLight)) {
                            Text(
                                "Customer Visual Attachment: ${job.photoBase64}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BluePrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Navigation simulation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // stylized navigation grid
                        val w = size.width
                        val h = size.height
                        drawLine(Color.DarkGray, Offset(w * 0.1f, 0f), Offset(w * 0.1f, h))
                        drawLine(Color.DarkGray, Offset(w * 0.8f, 0f), Offset(w * 0.8f, h))
                        drawLine(Color.DarkGray, Offset(0f, h * 0.5f), Offset(w, h * 0.5f))
                        
                        // draw target home
                        drawCircle(Color.White, radius = 10f, center = Offset(w * 0.8f, h * 0.5f))
                        drawCircle(AccentSuccess, radius = 6f, center = Offset(w * 0.8f, h * 0.5f))
                    }
                    Text("In-App Navigation to Customer active Map", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Actions
        item {
            Text("Job Progression", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))

            when (job.status) {
                "ASSIGNED" -> {
                    Button(
                        onClick = { viewModel.updateJobStatus(bookingId, "STARTED") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("Mark Started (Arrived at customer)", color = Color.White)
                    }
                }
                "STARTED" -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { mockPhotoCompleted = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "camera", tint = AccentSuccess)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (mockPhotoCompleted) "✓ Completion Photo Attached" else "Attach Completion Proof Photo",
                                color = AccentSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.updateJobStatus(bookingId, "COMPLETED", "completed_repair_proof.png") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                    ) {
                        Text("Mark Job Completed", color = Color.White)
                    }
                }
                "COMPLETED" -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AccentSuccess.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = AccentSuccess)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Job Completed Successfully!", fontWeight = FontWeight.Bold, color = AccentSuccess)
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 12: PROFESSIONAL WALLET, ANALYTICS & WITHDRAW ---
@Composable
fun ProfessionalWalletScreen(viewModel: FixNowViewModel) {
    val profProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()
    var isWithdrawing by remember { mutableStateOf(false) }
    var showWithdrawSuccessAlert by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Wallet & Earnings", fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        // Balance Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentSuccess),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Withdrawable Balance", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text("₹${profProfile?.walletBalance ?: 3450.0}", fontWeight = FontWeight.Black, fontSize = 32.sp, color = Color.White)
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isWithdrawing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isWithdrawing = true
                                    delay(2000) // Simulated transaction time
                                    viewModel.withdrawProfessionalFunds {
                                        isWithdrawing = false
                                        showWithdrawSuccessAlert = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Withdraw to Bank Account", color = AccentSuccess, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bank Account info
        item {
            Text("Settlement Account details", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Home, contentDescription = "Bank", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(profProfile?.bankAccount?.substringBefore(" - ") ?: "HDFC0001234", fontWeight = FontWeight.Bold)
                        Text(profProfile?.bankAccount?.substringAfter(" - ") ?: "A/C 50100223344", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Analytics chart simulation
        item {
            Text("Weekly Earnings Analytics", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        val w = size.width
                        val h = size.height

                        // simple analytical line chart
                        val points = listOf(h * 0.8f, h * 0.6f, h * 0.9f, h * 0.4f, h * 0.3f, h * 0.1f)
                        val stepX = w / (points.size - 1)

                        // draw line path
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = AccentSuccess,
                                start = Offset(i * stepX, points[i]),
                                end = Offset((i + 1) * stepX, points[i + 1]),
                                strokeWidth = 5f
                            )
                        }
                    }
                    Text("Total jobs completed: 18", modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp), fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }

    if (showWithdrawSuccessAlert) {
        AlertDialog(
            onDismissRequest = { showWithdrawSuccessAlert = false },
            confirmButton = {
                Button(onClick = { showWithdrawSuccessAlert = false }) { Text("OK") }
            },
            title = { Text("Settlement Initiated") },
            text = { Text("We have safely transferred the settlement balance directly to your registered HDFC bank account. Settlement will complete in 2 hours.") }
        )
    }
}

// --- SCREEN 13: PROFESSIONAL PROFILE DETAILS ---
@Composable
fun ProfessionalProfileScreen(viewModel: FixNowViewModel) {
    val profProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    var nameInput by remember { mutableStateOf(profProfile?.name ?: "Rajesh Kumar") }
    var skillsInput by remember { mutableStateOf(profProfile?.skills ?: "Electrician, Appliance Repair") }
    var bankInput by remember { mutableStateOf(profProfile?.bankAccount ?: "HDFC0001234 - A/C 50100223344") }
    var aadhaarInput by remember { mutableStateOf(profProfile?.aadhaarId ?: "1234-5678-9012") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Expert Professional Profile", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        item {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = skillsInput,
                onValueChange = { skillsInput = it },
                label = { Text("Skills List (Comma separated)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = bankInput,
                onValueChange = { bankInput = it },
                label = { Text("Bank account IFSC and Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = aadhaarInput,
                onValueChange = { aadhaarInput = it },
                label = { Text("Aadhaar Government ID") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    viewModel.saveProfessionalProfile(nameInput, skillsInput, bankInput, aadhaarInput)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("Save & Verify Account Info", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.signOut() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Sign Out", color = Color.White)
            }
        }
    }
}

// ==========================================
// --- WORKSPACE 3: SYSTEM ADMIN PANEL ---
// ==========================================
@Composable
fun AdminDashboardScreen(viewModel: FixNowViewModel) {
    val bookings by viewModel.allBookings.collectAsStateWithLifecycle()

    val totalRevenue = bookings.filter { it.status == "COMPLETED" }.sumOf { it.finalPrice }
    val totalBookings = bookings.size
    val activeBookings = bookings.count { it.status != "COMPLETED" && it.status != "CANCELLED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("FixNow Admin Control Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Platform health & analytics controller", fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Analytical summary cards
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = BlueLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Gross Revenue", fontSize = 11.sp, color = BluePrimary)
                        Text("₹$totalRevenue", fontWeight = FontWeight.Black, fontSize = 18.sp, color = BluePrimary)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Daily Bookings", fontSize = 11.sp, color = AccentSuccess)
                        Text("$totalBookings", fontWeight = FontWeight.Black, fontSize = 18.sp, color = AccentSuccess)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Active Technicians", fontSize = 11.sp, color = Color(0xFFB45309))
                        Text("1", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFFB45309))
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Live Bookings", fontSize = 11.sp, color = Color.DarkGray)
                        Text("$activeBookings", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.DarkGray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Live booking list monitoring
        item {
            Text("Platform Booking Logs", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(bottom = 12.dp))
        }

        if (bookings.isEmpty()) {
            item {
                Text("Zero booking requests registered on platform", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        }

        items(bookings) { booking ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(booking.serviceCategory, fontWeight = FontWeight.Bold)
                        Text("Cust: ${booking.customerName} • Tech: ${booking.professionalName}", fontSize = 11.sp, color = Color.Gray)
                        Text("Address: ${booking.address}", fontSize = 11.sp, color = Color.LightGray)
                    }
                    Card {
                        Text(
                            booking.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- SCREEN 14: ADMIN SUPPORT TICKETS AND REVIEWS ---
@Composable
fun AdminSupportTicketsScreen(viewModel: FixNowViewModel) {
    val tickets by viewModel.allSupportTickets.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Incoming Customer Support Tickets", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        }

        if (tickets.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "zero", tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No outstanding support tickets", fontWeight = FontWeight.Bold)
                }
            }
        }

        items(tickets) { ticket ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Card {
                            Text(ticket.status, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(ticket.message, fontSize = 12.sp, color = Color.DarkGray)

                    if (ticket.status == "OPEN") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.resolveSupportTicket(ticket)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentSuccess)
                        ) {
                            Text("Mark Resolved", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminManageUsersScreen(viewModel: FixNowViewModel) {
    // Left simple for structure
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("User Management Screen")
    }
}
