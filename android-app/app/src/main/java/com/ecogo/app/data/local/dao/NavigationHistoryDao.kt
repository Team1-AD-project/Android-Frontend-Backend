package com.ecogo.app.data.local.dao

import androidx.room.*
import com.ecogo.app.data.local.entity.NavigationHistory
import com.ecogo.app.data.local.entity.NavigationHistorySummary
import kotlinx.coroutines.flow.Flow

/**
 * 导航历史记录数据访问对象
 * 提供数据库CRUD操作
 */
@Dao
interface NavigationHistoryDao {

    /**
     * 插入一条导航历史记录
     * @return 插入记录的ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: NavigationHistory): Long

    /**
     * 插入多条导航历史记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<NavigationHistory>)

    /**
     * 更新导航历史记录
     */
    @Update
    suspend fun update(history: NavigationHistory)

    /**
     * 删除导航历史记录
     */
    @Delete
    suspend fun delete(history: NavigationHistory)

    /**
     * 根据ID删除记录
     */
    @Query("DELETE FROM navigation_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 清空所有历史记录
     */
    @Query("DELETE FROM navigation_history")
    suspend fun deleteAll()

    /**
     * 根据ID查询单条记录
     */
    @Query("SELECT * FROM navigation_history WHERE id = :id")
    suspend fun getById(id: Long): NavigationHistory?

    /**
     * 获取所有导航历史记录（按时间倒序）
     */
    @Query("SELECT * FROM navigation_history ORDER BY startTime DESC")
    suspend fun getAll(): List<NavigationHistory>

    /**
     * 获取所有导航历史记录（Flow，用于实时更新）
     */
    @Query("SELECT * FROM navigation_history ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<NavigationHistory>>

    /**
     * 获取简化版历史记录列表（用于列表显示）
     */
    @Query("""
        SELECT id, startTime, originName, destinationName, totalDistance,
               transportMode, carbonSaved, durationSeconds
        FROM navigation_history
        ORDER BY startTime DESC
    """)
    suspend fun getAllSummaries(): List<NavigationHistorySummary>

    /**
     * 根据时间范围查询
     */
    @Query("""
        SELECT * FROM navigation_history
        WHERE startTime >= :startTime AND startTime <= :endTime
        ORDER BY startTime DESC
    """)
    suspend fun getByTimeRange(startTime: Long, endTime: Long): List<NavigationHistory>

    /**
     * 根据交通方式查询
     */
    @Query("""
        SELECT * FROM navigation_history
        WHERE transportMode = :mode
        ORDER BY startTime DESC
    """)
    suspend fun getByTransportMode(mode: String): List<NavigationHistory>

    /**
     * 查询绿色出行记录
     */
    @Query("""
        SELECT * FROM navigation_history
        WHERE isGreenTrip = 1
        ORDER BY startTime DESC
    """)
    suspend fun getGreenTrips(): List<NavigationHistory>

    /**
     * 获取最近N条记录
     */
    @Query("""
        SELECT * FROM navigation_history
        ORDER BY startTime DESC
        LIMIT :limit
    """)
    suspend fun getRecent(limit: Int): List<NavigationHistory>

    /**
     * 统计总记录数
     */
    @Query("SELECT COUNT(*) FROM navigation_history")
    suspend fun getCount(): Int

    /**
     * 统计总减碳量
     */
    @Query("SELECT SUM(carbonSaved) FROM navigation_history WHERE isGreenTrip = 1")
    suspend fun getTotalCarbonSaved(): Double?

    /**
     * 统计总行驶距离
     */
    @Query("SELECT SUM(totalDistance) FROM navigation_history")
    suspend fun getTotalDistance(): Double?

    /**
     * 统计绿色出行次数
     */
    @Query("SELECT COUNT(*) FROM navigation_history WHERE isGreenTrip = 1")
    suspend fun getGreenTripCount(): Int

    /**
     * 根据用户ID查询
     */
    @Query("""
        SELECT * FROM navigation_history
        WHERE userId = :userId
        ORDER BY startTime DESC
    """)
    suspend fun getByUserId(userId: String): List<NavigationHistory>

    /**
     * 搜索记录（根据起点或终点名称）
     */
    @Query("""
        SELECT * FROM navigation_history
        WHERE originName LIKE '%' || :keyword || '%'
           OR destinationName LIKE '%' || :keyword || '%'
        ORDER BY startTime DESC
    """)
    suspend fun search(keyword: String): List<NavigationHistory>
}
