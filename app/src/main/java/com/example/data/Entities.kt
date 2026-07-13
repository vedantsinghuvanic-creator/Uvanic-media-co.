package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: String, // "customer", "professional", "admin"
    val name: String,
    val role: String, // "CUSTOMER", "PROFESSIONAL", "ADMIN"
    val phoneNumber: String,
    val email: String,
    val profilePicUrl: String = "",
    val isVerified: Boolean = false,
    val bankAccount: String = "",
    val aadhaarId: String = "",
    val userRating: Float = 4.8f,
    val acceptanceRate: Float = 95.0f,
    val walletBalance: Double = 0.0,
    val membership: String = "Regular", // Regular, Gold, Platinum
    val referralCode: String = "FIXNOW50",
    val serviceCity: String = "Mumbai",
    val skills: String = "Electrician, Plumber" // comma separated
)

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: String,
    val customerName: String,
    val professionalId: String,
    val professionalName: String,
    val serviceCategory: String, // "Electrician", "Plumber", etc.
    val problemDescription: String,
    val status: String, // "PENDING", "ASSIGNED", "STARTED", "COMPLETED", "CANCELLED"
    val date: String,
    val timeSlot: String,
    val address: String,
    val photoBase64: String = "",
    val estimatedPriceRange: String = "",
    val finalPrice: Double = 0.0,
    val paymentMethod: String = "UPI", // "UPI", "Card", "Wallet", "Cash"
    val paymentStatus: String = "PENDING", // "PENDING", "PAID"
    val ratingStars: Int = 0,
    val ratingComment: String = "",
    val serviceWarrantyDays: Int = 30,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val senderRole: String, // "CUSTOMER", "PROFESSIONAL", "SYSTEM"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "BOOKING", "PAYMENT", "PROMO"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "support_tickets")
data class SupportTicket(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val message: String,
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val timestamp: Long = System.currentTimeMillis()
)
