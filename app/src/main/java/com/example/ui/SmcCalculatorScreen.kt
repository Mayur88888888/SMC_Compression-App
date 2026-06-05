package com.example.ui

import kotlin.math.sqrt
import kotlin.math.roundToInt
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SmcCalculation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmcCalculatorScreen(
    viewModel: SmcCalculationViewModel,
    modifier: Modifier = Modifier
) {
    val calculationsList by viewModel.calculationsList.collectAsState()
    var activeTab by remember { mutableStateOf(0) } // 0: Calc, 1: Presets, 2: History

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "SMC Icon",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Mayur - SMC Compression Molding",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Material & Process Tooling Parameters",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.testTag("smc_calculator_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Selectors
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Build, null, modifier = Modifier.size(16.dp))
                            Text("Calculator", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_calculator")
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(16.dp))
                            Text("SMC Presets", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_presets")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.List, null, modifier = Modifier.size(16.dp))
                            Text("History (${calculationsList.size})", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }

            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { state ->
                when (state) {
                    0 -> {
                        CalculatorTabContent(
                            viewModel = viewModel,
                            isTablet = isTablet
                        )
                    }
                    1 -> {
                        PresetsTabContent(
                            viewModel = viewModel,
                            onApplyPreset = { activeTab = 0 }
                        )
                    }
                    2 -> {
                        HistoryTabContent(
                            calculationsList = calculationsList,
                            onLoad = { calc ->
                                viewModel.loadFromHistory(calc)
                                activeTab = 0
                            },
                            onDelete = { id -> viewModel.deleteById(id) },
                            onDeleteAll = { viewModel.deleteAll() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorTabContent(
    viewModel: SmcCalculationViewModel,
    isTablet: Boolean
) {
    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Inputs Part (Left)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ParameterInputsSection(viewModel)
                }
            }

            // Outputs Part (Right)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CalculationResultsSection(viewModel)
                }
            }
        }
    } else {
        // Compact Screen Column Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ParameterInputsSection(viewModel)
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            CalculationResultsSection(viewModel)
        }
    }
}

@Composable
fun ParameterInputsSection(viewModel: SmcCalculationViewModel) {
    Text(
        text = "Molding Configuration Details",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    // Title Input
    OutlinedTextField(
        value = viewModel.title,
        onValueChange = { viewModel.title = it },
        label = { Text("Trial Title") },
        leadingIcon = { Icon(Icons.Default.Create, null, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("input_title")
    )

    // Shape toggles
    Column {
        Text("Part Shape Geometry", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.shapeType = "Rectangular" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.shapeType == "Rectangular") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (viewModel.shapeType == "Rectangular") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("shape_rectangular")
            ) {
                Text("Rectangular (Plate)")
            }

            Button(
                onClick = { viewModel.shapeType = "Circular" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.shapeType == "Circular") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (viewModel.shapeType == "Circular") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("shape_circular")
            ) {
                Text("Circular (Disc)")
            }
        }
    }

    // Geometry Dimensions
    if (viewModel.shapeType == "Rectangular") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = viewModel.lengthMm,
                onValueChange = { viewModel.lengthMm = it },
                label = { Text("Length (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_length")
            )
            OutlinedTextField(
                value = viewModel.widthMm,
                onValueChange = { viewModel.widthMm = it },
                label = { Text("Width (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("input_width")
            )
        }
    } else {
        OutlinedTextField(
            value = viewModel.diameterMm,
            onValueChange = { viewModel.diameterMm = it },
            label = { Text("Diameter (mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_diameter")
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.thicknessMm,
            onValueChange = { viewModel.thicknessMm = it },
            label = { Text("Thickness (mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("input_thickness")
        )

        OutlinedTextField(
            value = viewModel.materialDensity,
            onValueChange = { viewModel.materialDensity = it },
            label = { Text("SMC Density (g/cm³)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("input_density")
        )
    }

    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Text(
        text = "SMC Charge & Flow Parameters By Mayur S",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    // Coverage Slider & Sheet parameters
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SMC Charge Coverage %", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("${viewModel.chargeCoveragePercent}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = (viewModel.chargeCoveragePercent.toIntOrNull() ?: 60).toFloat(),
            onValueChange = { viewModel.chargeCoveragePercent = it.roundToInt().toString() },
            valueRange = 20f..100f,
            steps = 79,
            modifier = Modifier.testTag("slider_coverage")
        )
        Text(
            text = "Typically 40%-80%. Low coverage increases flow distance; high coverage risks trapped air.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            lineHeight = 15.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.singleSheetThicknessMm,
            onValueChange = { viewModel.singleSheetThicknessMm = it },
            label = { Text("Sheet Thickness (mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("input_sheet_thickness")
        )

        OutlinedTextField(
            value = viewModel.flashFactorPercent,
            onValueChange = { viewModel.flashFactorPercent = it },
            label = { Text("Flash Loss factor %") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("input_flash")
        )
    }

    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Text(
        text = "Press Force & Cure Settings By Mayur S",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = viewModel.moldingPressureMpa,
            onValueChange = { viewModel.moldingPressureMpa = it },
            label = { Text("Molding Pressure (MPa)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1.1f)
                .testTag("input_pressure")
        )

        OutlinedTextField(
            value = viewModel.curingFactorSecMm,
            onValueChange = { viewModel.curingFactorSecMm = it },
            label = { Text("Cure rate (s/mm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .weight(0.9f)
                .testTag("input_cure_factor")
        )
    }

    OutlinedTextField(
        value = viewModel.baseCuringTimeSec,
        onValueChange = { viewModel.baseCuringTimeSec = it },
        label = { Text("Base Minimum Curing Time (sec)") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("input_base_curing")
    )
}

@Composable
fun CalculationResultsSection(viewModel: SmcCalculationViewModel) {
    val results = viewModel.calculationResult

    if (results == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Please complete parameters",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Please enter valid numeric parameters to instantly view calculation results.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        Text(
            text = "Calculation Output Summary By Mayur S",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        // Force and curing highlight cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Force card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PRESS FORCE", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${results.clampingForceTons} Tons",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val pressureBar = (viewModel.moldingPressureMpa.toDoubleOrNull() ?: 10.0) * 10
                    Text("$pressureBar bar ($StatusPressureInfo)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f))
                }
            }

            // Cure time card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("EST. CURE TIME", fontSize = 11.sp, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.height(4.dp))
                    val minutes = (results.estimatedCuringTimeSec / 60).toInt()
                    val seconds = (results.estimatedCuringTimeSec % 60).toInt()
                    Text(
                        if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Total process hold timer", fontSize = 9.sp, color = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f))
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Mold and Charge Dimension Blueprint Canvas
        Text("Visual Charge Blueprint", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1E1E)) // Industrial blueprint dark background
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "MOLD CAVITY vs CENTERED CHARGE (${viewModel.chargeCoveragePercent}% COVERAGE)",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    BlueprintCanvas(viewModel = viewModel, outputs = results)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (viewModel.shapeType == "Rectangular") {
                            "Cavity: ${viewModel.lengthMm}x${viewModel.widthMm}mm  |  Charge: ${results.plyLengthMm}x${results.plyWidthMm}mm"
                        } else {
                            "Cavity Ø: ${viewModel.diameterMm}mm  |  Charge Ø: ${results.plyDiameterMm}mm"
                        },
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Layers Required: ${results.requiredPlyCount} Plies",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Specifications List
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutputRow("Part Surface Area", "${results.partAreaCm2} cm²")
                OutputRow("Part Total Volume", "${results.partVolumeCm3} cm³")
                OutputRow("Finished Part Weight", "${results.partWeightG} g")
                OutputRow("SMC Charge Volume Area", "${results.chargeAreaCm2} cm²")
                OutputRow("Target Charge Input Weight", "${results.chargeWeightG} g", highlight = true)
                OutputRow("Stack thickness (loaded)", "${(results.requiredPlyCount * (viewModel.singleSheetThicknessMm.toDoubleOrNull() ?: 3.0))} mm")
                OutputRow("Plies of sheet requested", "${results.requiredPlyCount} layers")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Save Button
        Button(
            onClick = { viewModel.saveCalculation() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("button_save_trial"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Icon(Icons.Default.AddCircle, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save parameter parameters to trial logs", fontWeight = FontWeight.Bold)
        }
    }
}

val StatusPressureInfo: String = "Surgically calculated"

@Composable
fun OutputRow(label: String, value: String, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BlueprintCanvas(viewModel: SmcCalculationViewModel, outputs: CalculationOutputs) {
    val isRect = viewModel.shapeType == "Rectangular"
    val coverage = (viewModel.chargeCoveragePercent.toDoubleOrNull() ?: 60.0).coerceIn(20.0, 100.0)
    val scaleFactor = sqrt(coverage / 100.0)

    val gridColor = Color(0xFF333333)
    val moldOutlineColor = Color(0xFFDCDCDC)
    val chargeFillColor = Color(0xFFFF9800) // Beautiful bright resin orange representation

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        val width = size.width
        val height = size.height

        // 1. Draw Grid lines (Blueprint background style)
        val gridSize = 25f
        for (i in 0..(width / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(i * gridSize, 0f),
                end = Offset(i * gridSize, height),
                strokeWidth = 1f
            )
        }
        for (j in 0..(height / gridSize).toInt()) {
            drawLine(
                color = gridColor,
                start = Offset(0f, j * gridSize),
                end = Offset(width, j * gridSize),
                strokeWidth = 1f
            )
        }

        // 2. Draw outer mold boundary (Projected area)
        val outerMaxW = width * 0.85f
        val outerMaxH = height * 0.85f
        val moldW: Float
        val moldH: Float

        if (isRect) {
            val ratio = (viewModel.widthMm.toDoubleOrNull() ?: 300.0) / (viewModel.lengthMm.toDoubleOrNull() ?: 400.0)
            if (ratio <= 1.0) {
                moldW = outerMaxW
                moldH = (outerMaxW * ratio).toFloat().coerceAtMost(outerMaxH)
            } else {
                moldH = outerMaxH
                moldW = (outerMaxH / ratio).toFloat().coerceAtMost(outerMaxW)
            }

            val moldX = (width - moldW) / 2
            val moldY = (height - moldH) / 2

            // Drawn mold boundary
            drawRoundRect(
                color = moldOutlineColor,
                topLeft = Offset(moldX, moldY),
                size = Size(moldW, moldH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                style = Stroke(width = 3f)
            )

            // 3. Draw charge fill (Centered, scaling size as sqrt of coverage percentage)
            val chargeW = moldW * scaleFactor.toFloat()
            val chargeH = moldH * scaleFactor.toFloat()
            val chargeX = (width - chargeW) / 2
            val chargeY = (height - chargeH) / 2

            // Draw orange transparent charge
            drawRoundRect(
                color = chargeFillColor.copy(alpha = 0.35f),
                topLeft = Offset(chargeX, chargeY),
                size = Size(chargeW, chargeH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Dashed outline of charge
            drawRoundRect(
                color = chargeFillColor,
                topLeft = Offset(chargeX, chargeY),
                size = Size(chargeW, chargeH),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Flow arrows indicating direction of molding expansion
            val arrowLength = 20f
            // Flow from edge of charge to mold boundary
            // North
            drawLine(chargeFillColor, Offset(width / 2, chargeY), Offset(width / 2, moldY), strokeWidth = 3f)
            // South
            drawLine(chargeFillColor, Offset(width / 2, chargeY + chargeH), Offset(width / 2, moldY + moldH), strokeWidth = 3f)
            // West
            drawLine(chargeFillColor, Offset(chargeX, height / 2), Offset(moldX, height / 2), strokeWidth = 3f)
            // East
            drawLine(chargeFillColor, Offset(chargeX + chargeW, height / 2), Offset(moldX + moldW, height / 2), strokeWidth = 3f)

        } else {
            // Circular layout
            val diameter = outerMaxW.coerceAtMost(outerMaxH)
            val radius = diameter / 2f
            val centerX = width / 2
            val centerY = height / 2

            // Draw outer cylindrical mold boundary
            drawCircle(
                color = moldOutlineColor,
                center = Offset(centerX, centerY),
                radius = radius,
                style = Stroke(width = 3f)
            )

            // Draw proportional circular charge
            val chargeRadius = radius * scaleFactor.toFloat()
            drawCircle(
                color = chargeFillColor.copy(alpha = 0.35f),
                center = Offset(centerX, centerY),
                radius = chargeRadius
            )
            drawCircle(
                color = chargeFillColor,
                center = Offset(centerX, centerY),
                radius = chargeRadius,
                style = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )
            )

            // Radial flow lines
            val directions = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
            for (angle in directions) {
                val rad = Math.toRadians(angle.toDouble())
                val cos = Math.cos(rad).toFloat()
                val sin = Math.sin(rad).toFloat()
                drawLine(
                    color = chargeFillColor,
                    start = Offset(centerX + chargeRadius * cos, centerY + chargeRadius * sin),
                    end = Offset(centerX + radius * cos, centerY + radius * sin),
                    strokeWidth = 3f
                )
            }
        }
    }
}

@Composable
fun PresetsTabContent(
    viewModel: SmcCalculationViewModel,
    onApplyPreset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Industry Compound Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        viewModel.presets.forEach { preset ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preset_card_${preset.name.lowercase().replace(" ", "_")}")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = preset.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = preset.description,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PresetStat("Density", "${preset.density} g/cc")
                        PresetStat("Coverage", "${preset.coverage}%")
                        PresetStat("Press Pres.", "${preset.pressure} MPa")
                        PresetStat("Cure Coeff.", "${preset.curingFactor}s/mm")
                    }

                    Button(
                        onClick = {
                            viewModel.loadPreset(preset)
                            onApplyPreset()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .align(Alignment.End)
                            .testTag("apply_preset_${preset.name.lowercase().replace(" ", "_")}")
                    ) {
                        Text("Apply Preset Configuration", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Engineering Guidelines Reference",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• SMC Density: Typically 1.7 to 2.0 g/cm³ for glass-fiber filled polyester. Carbon SMC is much lighter (~1.4 to 1.5 g/cm³).\n" +
                            "• Charge Coverage: 40% to 80% of mold planar area. Lower coverage forces severe fiber alignment. High coverage blocks escaping volatile steam.\n" +
                            "• Clamping Pressure: Heavy ribbed components need 10 to 15 MPa (100-150 bar) to yield flawless deep cavities. Thin flat sheets require only 5-8 MPa.\n" +
                            "• Curing Mechanics: Molding thermal range runs 135-160°C. Standard rule of thumb is 30-50 seconds/mm of segment depth.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PresetStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HistoryTabContent(
    calculationsList: List<SmcCalculation>,
    onLoad: (SmcCalculation) -> Unit,
    onDelete: (Long) -> Unit,
    onDeleteAll: () -> Unit
) {
    if (calculationsList.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.List,
                    contentDescription = "No calculations found",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No saved trials in the history.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conduct a parameters calculation and press 'Save parameter parameter to trial logs' to record configurations.",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Trial Entries (${calculationsList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                TextButton(
                    onClick = onDeleteAll,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("button_delete_all_history")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear All", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear All")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = calculationsList,
                    key = { it.id }
                ) { calc ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_item_${calc.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mayur - SMC Compression Molding",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleLarge
                                        )                                                                      
                                                                        
                                    Text(
                                        text = calc.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    val dateStr = SimpleDateFormat("MMM dd, yyyy - HH:mm", Locale.getDefault()).format(Date(calc.timestamp))
                                    Text(
                                        text = "Saved on: $dateStr",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }

                                IconButton(
                                    onClick = { onDelete(calc.id) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("delete_item_${calc.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete entry",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // Grid specs
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Shape: ${calc.shapeType} (${if (calc.shapeType == "Rectangular") "${calc.lengthMm.toInt()}x${calc.widthMm.toInt()}" else "Ø ${calc.diameterMm.toInt()}"}x${calc.thicknessMm}mm)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Ply Count: ${calc.requiredPlyCount} plies",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "Weight: Part ${calc.partWeightG}g | Charge: ${calc.chargeWeightG}g",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Force: ${calc.clampingForceTons} Tons",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    val estMin = (calc.estimatedCuringTimeSec / 60).toInt()
                                    val estSec = (calc.estimatedCuringTimeSec % 60).toInt()
                                    Text(
                                        text = "Pressure: ${calc.moldingPressureMpa} MPa (${(calc.moldingPressureMpa*10).toInt()} bar)",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Cure: ${if (estMin > 0) "${estMin}m ${estSec}s" else "${estSec}s"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { onLoad(calc) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .testTag("load_item_${calc.id}")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Load Config", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Restore Configuration Workspace", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
