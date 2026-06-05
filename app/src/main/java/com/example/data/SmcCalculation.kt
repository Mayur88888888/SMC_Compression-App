package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smc_calculations")
data class SmcCalculation(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    
    // Geometry & Dimensions
    val shapeType: String, // "Rectangular" or "Circular"
    val lengthMm: Double,
    val widthMm: Double,
    val diameterMm: Double,
    val thicknessMm: Double,
    
    // Material Properties
    val materialDensity: Double, // g/cm3
    val flashFactorPercent: Double, // %
    
    // Charge Parameters
    val chargeCoveragePercent: Double, // %
    val singleSheetThicknessMm: Double, // mm
    
    // Molding Parameters
    val moldingPressureMpa: Double, // MPa
    val curingFactorSecMm: Double, // sec/mm
    val baseCuringTimeSec: Double, // sec
    
    // Calculated Outputs
    val partAreaCm2: Double,
    val partVolumeCm3: Double,
    val partWeightG: Double,
    val chargeWeightG: Double,
    val chargeAreaCm2: Double,
    val requiredPlyCount: Int,
    val plyLengthMm: Double,
    val plyWidthMm: Double,
    val plyDiameterMm: Double,
    val clampingForceTons: Double,
    val estimatedCuringTimeSec: Double
)
