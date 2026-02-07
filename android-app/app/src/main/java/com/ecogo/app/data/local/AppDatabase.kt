package com.ecogo.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ecogo.app.data.local.dao.NavigationHistoryDao
import com.ecogo.app.data.local.entity.NavigationHistory

/**
 * 应用数据库
 * 单例模式，提供全局唯一的数据库实例
 */
@Database(
    entities = [NavigationHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * 获取导航历史DAO
     */
    abstract fun navigationHistoryDao(): NavigationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * 获取数据库实例（单例）
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ecogo_database"
                )
                    .fallbackToDestructiveMigration() // 开发阶段可以使用，生产环境需要提供迁移策略
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 用于测试：清除数据库实例
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
