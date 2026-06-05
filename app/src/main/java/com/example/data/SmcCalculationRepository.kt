package com.example.data

import kotlinx.coroutines.flow.Flow

class SmcCalculationRepository(private val smcCalculationDao: SmcCalculationDao) {
    val allCalculations: Flow<List<SmcCalculation>> = smcCalculationDao.getAllCalculations()

    suspend fun insert(smcCalculation: SmcCalculation): Long {
        return smcCalculationDao.insertCalculation(smcCalculation)
    }

    suspend fun delete(smcCalculation: SmcCalculation) {
        smcCalculationDao.deleteCalculation(smcCalculation)
    }

    suspend fun deleteById(id: Long) {
        smcCalculationDao.deleteCalculationById(id)
    }

    suspend fun deleteAll() {
        smcCalculationDao.deleteAllCalculations()
    }
}
