package com.mason.milkteastatistics

import android.app.Application

/**
 * 应用入口类。
 *
 * 注册 [androidx.startup.AppInitializer]，由 Jetpack App Startup 库
 * 在 ContentProvider 阶段自动调度 [com.mason.milkteastatistics.startup.DatabaseInitializer]，
 * 在 MainActivity 创建之前完成数据库初始化，消除冷启动数据库构建耗时。
 */
class MilkTeaApplication : Application()
