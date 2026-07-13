package com.example.data

import kotlinx.coroutines.flow.Flow

class FixNowRepository(
    private val userProfileDao: UserProfileDao,
    private val bookingDao: BookingDao,
    private val chatMessageDao: ChatMessageDao,
    private val appNotificationDao: AppNotificationDao,
    private val supportTicketDao: SupportTicketDao
) {
    // User Profiles
    suspend fun getUserProfile(id: String): UserProfile? = userProfileDao.getUserProfile(id)
    fun getUserProfileFlow(id: String): Flow<UserProfile?> = userProfileDao.getUserProfileFlow(id)
    suspend fun insertOrUpdateProfile(profile: UserProfile) = userProfileDao.insertOrUpdateProfile(profile)

    // Bookings
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookingsFlow()
    fun getCustomerBookings(customerId: String): Flow<List<Booking>> = bookingDao.getCustomerBookingsFlow(customerId)
    fun getProfessionalBookings(professionalId: String): Flow<List<Booking>> = bookingDao.getProfessionalBookingsFlow(professionalId)
    suspend fun getBookingById(id: Long): Booking? = bookingDao.getBookingById(id)
    fun getBookingByIdFlow(id: Long): Flow<Booking?> = bookingDao.getBookingByIdFlow(id)
    suspend fun insertBooking(booking: Booking): Long = bookingDao.insertBooking(booking)
    suspend fun updateBooking(booking: Booking) = bookingDao.updateBooking(booking)
    suspend fun deleteBookingById(id: Long) = bookingDao.deleteBookingById(id)

    // Chats
    fun getChatMessages(bookingId: Long): Flow<List<ChatMessage>> = chatMessageDao.getMessagesForBookingFlow(bookingId)
    suspend fun insertChatMessage(message: ChatMessage) = chatMessageDao.insertMessage(message)

    // Notifications
    val allNotifications: Flow<List<AppNotification>> = appNotificationDao.getAllNotificationsFlow()
    suspend fun insertNotification(notification: AppNotification) = appNotificationDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = appNotificationDao.markAsRead(id)

    // Support Tickets
    val allSupportTickets: Flow<List<SupportTicket>> = supportTicketDao.getAllTicketsFlow()
    suspend fun insertSupportTicket(ticket: SupportTicket) = supportTicketDao.insertTicket(ticket)
}
