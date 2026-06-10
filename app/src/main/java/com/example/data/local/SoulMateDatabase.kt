package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

@Dao
interface CompanionMemoryDao {
    @Query("SELECT * FROM companion_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<CompanionMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: CompanionMemory)

    @Query("DELETE FROM companion_memories WHERE id = :id")
    suspend fun deleteMemory(id: Long)

    @Query("DELETE FROM companion_memories")
    suspend fun clearAllMemories()
}

@Dao
interface MoodEntryDao {
    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoodEntries(): Flow<List<MoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun clearAllMoods()
}

@Dao
interface ProductivityItemDao {
    @Query("SELECT * FROM productivity_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<ProductivityItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ProductivityItem)

    @Query("DELETE FROM productivity_items WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("DELETE FROM productivity_items")
    suspend fun clearAllItems()
}

@Dao
interface CommunityPostDao {
    @Query("SELECT * FROM community_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<CommunityPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: CommunityPost)

    @Query("UPDATE community_posts SET likes = likes + 1 WHERE id = :id")
    suspend fun likePost(id: Long)
}

@Dao
interface SmartReminderDao {
    @Query("SELECT * FROM smart_reminders ORDER BY timestamp DESC")
    fun getAllReminders(): Flow<List<SmartReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: SmartReminder)

    @Query("DELETE FROM smart_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("UPDATE smart_reminders SET isTriggered = :triggered WHERE id = :id")
    suspend fun setTriggered(id: Long, triggered: Boolean)

    @Query("DELETE FROM smart_reminders")
    suspend fun clearAllReminders()
}

@Dao
interface RelationshipMilestoneDao {
    @Query("SELECT * FROM relationship_milestones ORDER BY timestamp DESC")
    fun getAllMilestones(): Flow<List<RelationshipMilestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: RelationshipMilestone)

    @Query("DELETE FROM relationship_milestones WHERE id = :id")
    suspend fun deleteMilestone(id: Long)

    @Query("DELETE FROM relationship_milestones")
    suspend fun clearAllMilestones()
}

@Database(
    entities = [
        UserProfile::class,
        ChatMessage::class,
        CompanionMemory::class,
        MoodEntry::class,
        ProductivityItem::class,
        CommunityPost::class,
        SmartReminder::class,
        RelationshipMilestone::class,
        SocialFriend::class,
        SocialMessage::class,
        CommunityGroup::class,
        GroupMessage::class,
        CallLogItem::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SoulMateDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun companionMemoryDao(): CompanionMemoryDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun productivityItemDao(): ProductivityItemDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun smartReminderDao(): SmartReminderDao
    abstract fun relationshipMilestoneDao(): RelationshipMilestoneDao
    abstract fun socialFriendDao(): SocialFriendDao
    abstract fun socialMessageDao(): SocialMessageDao
    abstract fun communityGroupDao(): CommunityGroupDao
    abstract fun groupMessageDao(): GroupMessageDao
    abstract fun callLogItemDao(): CallLogItemDao
}
