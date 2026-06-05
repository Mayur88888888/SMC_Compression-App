package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SmcCalculationDao {
    @Query("SELECT * FROM smc_calculations ORDER BY timestamp DESC")
    fun getAllCalculations(): Flow<List<SmcCalculation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(smcCalculation: SmcCalculation): Long

    @Delete
    suspend fun deleteCalculation(smcCalculation: SmcCalculation)

    @Query("DELETE FROM smc_calculations WHERE id = :id")
    suspend fun deleteCalculationById(id: Long)

    @Query("DELETE FROM smc_calculations")
    suspend fun deleteAllCalculations()
}
