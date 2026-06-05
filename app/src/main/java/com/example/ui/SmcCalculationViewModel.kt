package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.SmcCalculation
import com.example.data.SmcCalculationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

class SmcCalculationViewModel(
    application: Application,
    private val repository: SmcCalculationRepository
) : AndroidViewModel(application) {

    // Inputs States
    var title by mutableStateOf("New Trial")
    var shapeType by mutableStateOf("Rectangular") // "Rectangular" or "Circular"
    var lengthMm by mutableStateOf("400")
    var widthMm by mutableStateOf("300")
    var diameterMm by mutableStateOf("300")
    var thicknessMm by mutableStateOf("4.0")
    
    var materialDensity by mutableStateOf("1.85")
    var flashFactorPercent by mutableStateOf("2.0")
    
    var chargeCoveragePercent by mutableStateOf("60")
    var singleSheetThicknessMm by mutableStateOf("3.0")
    
    var moldingPressureMpa by mutableStateOf("10.0")
    var curingFactorSecMm by mutableStateOf("40")
    var baseCuringTimeSec by mutableStateOf("30")

    // Database flow
    val calculationsList: StateFlow<List<SmcCalculation>> = repository.allCalculations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Calculation Result State
    val calculationResult: CalculationOutputs?
        get() = calculateOutputs()

    // Preset Selection List
    val presets = listOf(
        Preset("Standard SMC", "1.85", "60", "3.0", "10.0", "40", "30", "General purpose components, balanced flow properties"),
        Preset("Structural Carbon SMC", "1.45", "50", "2.5", "15.0", "50", "45", "High strength parts, short fibers, higher press force required"),
        Preset("Class A Surface SMC", "1.95", "70", "4.0", "8.0", "45", "30", "Automotive exterior Class-A finish, low shrinkage, low profile additives")
    )

    fun loadPreset(preset: Preset) {
        materialDensity = preset.density
        chargeCoveragePercent = preset.coverage
        singleSheetThicknessMm = preset.sheetThickness
        moldingPressureMpa = preset.pressure
        curingFactorSecMm = preset.curingFactor
        baseCuringTimeSec = preset.baseCuring
    }

    private fun calculateOutputs(): CalculationOutputs? {
        val thickness = thicknessMm.toDoubleOrNull() ?: return null
        val density = materialDensity.toDoubleOrNull() ?: return null
        val flashFactor = flashFactorPercent.toDoubleOrNull() ?: return null
        val coverage = chargeCoveragePercent.toDoubleOrNull() ?: return null
        val sheetThickness = singleSheetThicknessMm.toDoubleOrNull() ?: return null
        val pressure = moldingPressureMpa.toDoubleOrNull() ?: return null
        val cureFactor = curingFactorSecMm.toDoubleOrNull() ?: return null
        val baseCure = baseCuringTimeSec.toDoubleOrNull() ?: return null

        if (thickness <= 0.0 || density <= 0.0 || coverage <= 0.0 || sheetThickness <= 0.0 || pressure <= 0.0) {
            return null
        }

        val partAreaCm2: Double
        val plyLengthMm: Double
        val plyWidthMm: Double
        val plyDiameterMm: Double

        val len = lengthMm.toDoubleOrNull() ?: 0.0
        val wid = widthMm.toDoubleOrNull() ?: 0.0
        val dia = diameterMm.toDoubleOrNull() ?: 0.0

        if (shapeType == "Rectangular") {
            if (len <= 0.0 || wid <= 0.0) return null
            partAreaCm2 = (len / 10.0) * (wid / 10.0)
            
            // To maintain aspect ratio of flow, charge dimensions scale as sqrt(coverage)
            val scaleFactor = sqrt(coverage / 100.0)
            plyLengthMm = (len * scaleFactor).roundTo(1)
            plyWidthMm = (wid * scaleFactor).roundTo(1)
            plyDiameterMm = 0.0
        } else {
            if (dia <= 0.0) return null
            val radiusCm = (dia / 2.0) / 10.0
            partAreaCm2 = PI * radiusCm * radiusCm
            
            val scaleFactor = sqrt(coverage / 100.0)
            plyDiameterMm = (dia * scaleFactor).roundTo(1)
            plyLengthMm = 0.0
            plyWidthMm = 0.0
        }

        val partVolumeCm3 = partAreaCm2 * (thickness / 10.0)
        val partWeightG = partVolumeCm3 * density
        val chargeWeightG = partWeightG * (1.0 + (flashFactor / 100.0))
        val chargeAreaCm2 = partAreaCm2 * (coverage / 100.0)

        // Ply count calculation:
        // Weight of 1 ply with actual charge area = chargeAreaCm2 * (sheetThickness / 10) * density
        val singlePlyWeightG = chargeAreaCm2 * (sheetThickness / 10.0) * density
        val rawPlyCount = if (singlePlyWeightG > 0.0) chargeWeightG / singlePlyWeightG else 1.0
        val requiredPlyCount = ceil(rawPlyCount).toInt().coerceAtLeast(1)

        // Force calculations in Tons (Metric Ton force = kN / 9.80665)
        // Force (kN) = Area (cm2) * pressure (MPa) / 10.0 (since 1 MPa = 100 N/cm2, so Area * 100 N = force in N. kN = force / 10)
        // Force (Tons) = (Area * pressure) / 9.80665
        val clampingForceTons = (partAreaCm2 * pressure) / 9.80665

        // Curing time in seconds
        val estimatedCuringTimeSec = baseCure + (thickness * cureFactor)

        return CalculationOutputs(
            partAreaCm2 = partAreaCm2.roundTo(2),
            partVolumeCm3 = partVolumeCm3.roundTo(2),
            partWeightG = partWeightG.roundTo(1),
            chargeWeightG = chargeWeightG.roundTo(1),
            chargeAreaCm2 = chargeAreaCm2.roundTo(2),
            requiredPlyCount = requiredPlyCount,
            plyLengthMm = plyLengthMm,
            plyWidthMm = plyWidthMm,
            plyDiameterMm = plyDiameterMm,
            clampingForceTons = clampingForceTons.roundTo(1),
            estimatedCuringTimeSec = estimatedCuringTimeSec.roundTo(0)
        )
    }

    fun saveCalculation() {
        val result = calculationResult ?: return
        viewModelScope.launch {
            val record = SmcCalculation(
                title = if (title.trim().isEmpty()) "SMC Trial" else title.trim(),
                shapeType = shapeType,
                lengthMm = lengthMm.toDoubleOrNull() ?: 0.0,
                widthMm = widthMm.toDoubleOrNull() ?: 0.0,
                diameterMm = diameterMm.toDoubleOrNull() ?: 0.0,
                thicknessMm = thicknessMm.toDoubleOrNull() ?: 0.0,
                materialDensity = materialDensity.toDoubleOrNull() ?: 1.85,
                flashFactorPercent = flashFactorPercent.toDoubleOrNull() ?: 2.0,
                chargeCoveragePercent = chargeCoveragePercent.toDoubleOrNull() ?: 60.0,
                singleSheetThicknessMm = singleSheetThicknessMm.toDoubleOrNull() ?: 3.0,
                moldingPressureMpa = moldingPressureMpa.toDoubleOrNull() ?: 10.0,
                curingFactorSecMm = curingFactorSecMm.toDoubleOrNull() ?: 40.0,
                baseCuringTimeSec = baseCuringTimeSec.toDoubleOrNull() ?: 30.0,
                
                // outputs
                partAreaCm2 = result.partAreaCm2,
                partVolumeCm3 = result.partVolumeCm3,
                partWeightG = result.partWeightG,
                chargeWeightG = result.chargeWeightG,
                chargeAreaCm2 = result.chargeAreaCm2,
                requiredPlyCount = result.requiredPlyCount,
                plyLengthMm = result.plyLengthMm,
                plyWidthMm = result.plyWidthMm,
                plyDiameterMm = result.plyDiameterMm,
                clampingForceTons = result.clampingForceTons,
                estimatedCuringTimeSec = result.estimatedCuringTimeSec
            )
            repository.insert(record)
            // Reset title to prevent successive duplicate saves of same title
            title = "New Trial"
        }
    }

    fun deleteCalculation(smc: SmcCalculation) {
        viewModelScope.launch {
            repository.delete(smc)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun loadFromHistory(calc: SmcCalculation) {
        title = calc.title
        shapeType = calc.shapeType
        lengthMm = calc.lengthMm.toStringWithoutZero()
        widthMm = calc.widthMm.toStringWithoutZero()
        diameterMm = calc.diameterMm.toStringWithoutZero()
        thicknessMm = calc.thicknessMm.toStringWithoutZero()
        materialDensity = calc.materialDensity.toStringWithoutZero()
        flashFactorPercent = calc.flashFactorPercent.toStringWithoutZero()
        chargeCoveragePercent = calc.chargeCoveragePercent.toStringWithoutZero()
        singleSheetThicknessMm = calc.singleSheetThicknessMm.toStringWithoutZero()
        moldingPressureMpa = calc.moldingPressureMpa.toStringWithoutZero()
        curingFactorSecMm = calc.curingFactorSecMm.toStringWithoutZero()
        baseCuringTimeSec = calc.baseCuringTimeSec.toStringWithoutZero()
    }

    private fun Double.toStringWithoutZero(): String {
        return if (this == this.toLong().toDouble()) {
            this.toLong().toString()
        } else {
            this.toString()
        }
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}

data class Preset(
    val name: String,
    val density: String,
    val coverage: String,
    val sheetThickness: String,
    val pressure: String,
    val curingFactor: String,
    val baseCuring: String,
    val description: String
)

data class CalculationOutputs(
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

class SmcCalculationViewModelFactory(
    private val application: Application,
    private val repository: SmcCalculationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SmcCalculationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SmcCalculationViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
