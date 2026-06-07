package com.mason.milkteastatistics.service

import com.mason.milkteastatistics.data.CommonBrand
import com.mason.milkteastatistics.data.MilkTeaRepository
import kotlinx.coroutines.flow.Flow

/**
 * 品牌服务：负责品牌列表查询和常用品牌管理。
 *
 * 所有对外暴露的查询均返回 [Flow]，由调用方通过
 * [kotlinx.coroutines.flow.stateIn] 转换为 [kotlinx.coroutines.flow.StateFlow]。
 */
class BrandService(private val repository: MilkTeaRepository) {

    fun getAllBrands(): Flow<List<String>> = repository.getAllBrands()

    fun getCommonBrands(): Flow<List<CommonBrand>> = repository.getCommonBrands()

    suspend fun addCommonBrand(name: String) {
        repository.addCommonBrand(name)
    }

    suspend fun removeCommonBrand(id: Long) {
        repository.removeCommonBrand(id)
    }
}
