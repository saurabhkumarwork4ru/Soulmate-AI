package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SocialFriendDao {
    @Query("SELECT * FROM social_friends ORDER BY displayName ASC")
    fun getAllFriends(): Flow<List<SocialFriend>>

    @Query("SELECT * FROM social_friends WHERE friendshipStatus = 'Accepted' ORDER BY displayName ASC")
    fun getAcceptedFriends(): Flow<List<SocialFriend>>

    @Query("SELECT * FROM social_friends WHERE friendshipStatus = 'Received' ORDER BY displayName ASC")
    fun getIncomingFriendRequests(): Flow<List<SocialFriend>>

    @Query("SELECT * FROM social_friends WHERE friendshipStatus = 'Sent' ORDER BY displayName ASC")
    fun getOutgoingFriendRequests(): Flow<List<SocialFriend>>

    @Query("SELECT * FROM social_friends WHERE id = :friendId")
    suspend fun getFriendById(friendId: String): SocialFriend?

    @Query("SELECT * FROM social_friends WHERE id = :friendId")
    fun getFriendFlowById(friendId: String): Flow<SocialFriend?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<SocialFriend>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: SocialFriend)

    @Update
    suspend fun updateFriend(friend: SocialFriend)

    @Query("UPDATE social_friends SET friendshipStatus = :status WHERE id = :friendId")
    suspend fun updateFriendshipStatus(friendId: String, status: String)

    @Query("UPDATE social_friends SET isBlocked = :blocked WHERE id = :friendId")
    suspend fun updateBlockStatus(friendId: String, blocked: Boolean)

    @Query("UPDATE social_friends SET isMuted = :muted WHERE id = :friendId")
    suspend fun updateMuteStatus(friendId: String, muted: Boolean)

    @Query("DELETE FROM social_friends WHERE id = :friendId")
    suspend fun deleteFriend(friendId: String)
}

@Dao
interface SocialMessageDao {
    @Query("SELECT * FROM social_messages WHERE friendId = :friendId ORDER BY timestamp ASC")
    fun getMessagesForFriend(friendId: String): Flow<List<SocialMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SocialMessage): Long

    @Query("UPDATE social_messages SET seen = 1 WHERE friendId = :friendId AND senderId != 'me'")
    suspend fun markMessagesAsSeen(friendId: String)

    @Query("UPDATE social_messages SET reactions = :reaction WHERE id = :messageId")
    suspend fun addReaction(messageId: Long, reaction: String)

    @Query("UPDATE social_messages SET text = :newText, isEdited = 1 WHERE id = :messageId")
    suspend fun editMessage(messageId: Long, newText: String)

    @Query("DELETE FROM social_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)
}

@Dao
interface CommunityGroupDao {
    @Query("SELECT * FROM community_groups ORDER BY memberCount DESC")
    fun getAllGroups(): Flow<List<CommunityGroup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<CommunityGroup>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: CommunityGroup)

    @Query("UPDATE community_groups SET isJoined = :joined, memberCount = memberCount + (case when :joined = 1 then 1 else -1 end) WHERE id = :groupId")
    suspend fun updateJoinStatus(groupId: String, joined: Boolean)
}

@Dao
interface GroupMessageDao {
    @Query("SELECT * FROM group_messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: String): Flow<List<GroupMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMessage(message: GroupMessage)

    @Query("UPDATE group_messages SET likes = likes + 1, isLikedByMe = 1 WHERE id = :messageId")
    suspend fun likeGroupMessage(messageId: Long)
}

@Dao
interface CallLogItemDao {
    @Query("SELECT * FROM call_log_items ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogItem)

    @Query("DELETE FROM call_log_items")
    suspend fun clearCallLogs()
}
