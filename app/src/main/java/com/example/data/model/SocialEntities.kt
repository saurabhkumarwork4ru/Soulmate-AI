package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "social_friends")
data class SocialFriend(
    @PrimaryKey val id: String, // Unique user ID: e.g., SMA784521
    val username: String,
    val displayName: String,
    val bio: String = "",
    val interests: String = "", // e.g. "Coding, Fitness, Gaming, Music"
    val city: String = "City Sandbox",
    val distanceKm: Float = 1.2f,
    val isOnline: Boolean = false,
    val lastSeen: String = "Offline",
    val discoverability: String = "Everyone", // Everyone, Friends Only, Hidden
    val friendshipStatus: String = "None", // None, Sent, Received, Accepted
    val isBlocked: Boolean = false,
    val isMuted: Boolean = false,
    val age: Int = 22,
    val email: String = "",
    val phoneNumber: String = "",
    val socialProfiles: String = "",
    val is18PlusVerified: Boolean = false
)

@Entity(tableName = "social_messages")
data class SocialMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val friendId: String,
    val senderId: String, // "me" or friend's unique ID
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mediaType: String = "text", // text, image, voice_note, document, sticker
    val mediaUrl: String = "", // preset local simulation URL or state
    val delivered: Boolean = true,
    val seen: Boolean = false,
    val replyToId: Long = -1,
    val reactions: String = "", // e.g. "❤️"
    val isEdited: Boolean = false
)

@Entity(tableName = "community_groups")
data class CommunityGroup(
    @PrimaryKey val id: String, // e.g. group_coding, group_fitness
    val name: String,
    val description: String,
    val category: String, // Public, Interest, Study, Career, Gaming, Local City, Adult 18+
    val memberCount: Int = 12,
    val isJoined: Boolean = false,
    val pfpEmoji: String = "💬",
    val isAdultOnly: Boolean = false
)

@Entity(tableName = "group_messages")
data class GroupMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: String,
    val authorId: String,
    val authorName: String,
    val authorEmoji: String = "👤",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val isLikedByMe: Boolean = false
)

@Entity(tableName = "call_log_items")
data class CallLogItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String,
    val peerName: String,
    val callType: String = "Voice", // Voice, Video
    val status: String = "Incoming", // Incoming, Outgoing, Missed
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)
