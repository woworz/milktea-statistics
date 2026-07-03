package com.mason.milkteastatistics.startup

import android.content.Context
import androidx.startup.Initializer
import com.mason.milkteastatistics.data.MilkTeaDatabase

/**
 * App Startup 初始化器：在应用启动阶段预初始化 Room 数据库。
 *
 * 通过将数据库构建从 ViewModel 构造时（首次进入首页）提前到
 * Application 启动阶段，避免冷启动时首页卡顿。
 */
class DatabaseInitializer : Initializer<MilkTeaDatabase> {

    override fun create(context: Context): MilkTeaDatabase {
        return MilkTeaDatabase.getDatabase(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // 无依赖，最早执行
        return emptyList()
    }
}
