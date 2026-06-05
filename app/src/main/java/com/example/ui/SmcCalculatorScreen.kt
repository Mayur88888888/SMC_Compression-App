package com.example.ui

import kotlin.math.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

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
            ScrollableTabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                edgePadding = 8.dp
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
                            Text("Presets", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_presets")
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(16.dp))
                            Text("Flow Simulator", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_simulator")
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp))
                            Text("Diagnostics Suite", maxLines = 1)
                        }
                    },
                    modifier = Modifier.testTag("tab_diagnostics")
                )
                Tab(
                    selected = activeTab == 4,
                    onClick = { activeTab = 4 },
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
                        FlowSimulatorTabContent(
                            isTablet = isTablet
                        )
                    }
                    3 -> {
                        DiagnosticsSuiteTabContent(
                            viewModel = viewModel,
                            isTablet = isTablet
                        )
                    }
                    4 -> {
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

@Composable
fun FlowSimulatorTabContent(isTablet: Boolean) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedTemplateIndex by remember { mutableStateOf(0) } // 0: Bumper, 1: Bracket, 2: Dome
    var chargeX by remember { mutableStateOf(0.5f) } // 0f..1f
    var chargeY by remember { mutableStateOf(0.5f) } // 0f..1f
    var chargeScale by remember { mutableStateOf(0.42f) }
    var chargeAspectY by remember { mutableStateOf(1.0f) }
    var chargeRotation by remember { mutableStateOf(0f) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            selectedTemplateIndex = -1 // -1 is loaded user graphic
        }
    }

    if (isTablet) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left Controls Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                    FlowControlsSection(
                        selectedTemplateIndex = selectedTemplateIndex,
                        onSelectTemplate = { idx ->
                            selectedTemplateIndex = idx
                            selectedImageUri = null
                        },
                        onLauncherPickImage = { launcher.launch("image/*") },
                        selectedImageUri = selectedImageUri,
                        chargeScale = chargeScale,
                        onChargeScaleChange = { chargeScale = it },
                        chargeAspectY = chargeAspectY,
                        onChargeAspectYChange = { chargeAspectY = it },
                        chargeRotation = chargeRotation,
                        onChargeRotationChange = { chargeRotation = it },
                        chargeX = chargeX,
                        chargeY = chargeY,
                        onResetOffset = {
                            chargeX = 0.5f
                            chargeY = 0.5f
                        }
                    )
                }
            }

            // Right Visualizer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FlowVisualizerCanvas(
                        selectedTemplateIndex = selectedTemplateIndex,
                        imageUri = selectedImageUri,
                        chargeX = chargeX,
                        chargeY = chargeY,
                        chargeScale = chargeScale,
                        chargeAspectY = chargeAspectY,
                        chargeRotation = chargeRotation,
                        onPositionChange = { nx, ny ->
                            chargeX = nx
                            chargeY = ny
                        }
                    )
                }
            }
        }
    } else {
        // Compact Column Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141416)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FlowVisualizerCanvas(
                        selectedTemplateIndex = selectedTemplateIndex,
                        imageUri = selectedImageUri,
                        chargeX = chargeX,
                        chargeY = chargeY,
                        chargeScale = chargeScale,
                        chargeAspectY = chargeAspectY,
                        chargeRotation = chargeRotation,
                        onPositionChange = { nx, ny ->
                            chargeX = nx
                            chargeY = ny
                        }
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FlowControlsSection(
                        selectedTemplateIndex = selectedTemplateIndex,
                        onSelectTemplate = { idx ->
                            selectedTemplateIndex = idx
                            selectedImageUri = null
                        },
                        onLauncherPickImage = { launcher.launch("image/*") },
                        selectedImageUri = selectedImageUri,
                        chargeScale = chargeScale,
                        onChargeScaleChange = { chargeScale = it },
                        chargeAspectY = chargeAspectY,
                        onChargeAspectYChange = { chargeAspectY = it },
                        chargeRotation = chargeRotation,
                        onChargeRotationChange = { chargeRotation = it },
                        chargeX = chargeX,
                        chargeY = chargeY,
                        onResetOffset = {
                            chargeX = 0.5f
                            chargeY = 0.5f
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlowControlsSection(
    selectedTemplateIndex: Int,
    onSelectTemplate: (Int) -> Unit,
    onLauncherPickImage: () -> Unit,
    selectedImageUri: Uri?,
    chargeScale: Float,
    onChargeScaleChange: (Float) -> Unit,
    chargeAspectY: Float,
    onChargeAspectYChange: (Float) -> Unit,
    chargeRotation: Float,
    onChargeRotationChange: (Float) -> Unit,
    chargeX: Float,
    chargeY: Float,
    onResetOffset: () -> Unit
) {
    Text(
        text = "SMC Charge Flow & Weld-Line Simulator",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )

    Text(
        text = "Touch and reposition the raw material SMC charge sheet anywhere inside the cavity below to simulate fluid flows and predict weld lines.",
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    // Source selection text
    Text(
        text = "Molding Cavity Shape Source",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTemplateIndex == 0,
                onClick = { onSelectTemplate(0) },
                label = { Text("Car Bumper", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedTemplateIndex == 1,
                onClick = { onSelectTemplate(1) },
                label = { Text("Rib Bracket", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = selectedTemplateIndex == 2,
                onClick = { onSelectTemplate(2) },
                label = { Text("Aero Dome", fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
            )
        }
        
        Button(
            onClick = onLauncherPickImage,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedTemplateIndex == -1) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.secondaryContainer,
                contentColor = if (selectedTemplateIndex == -1) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (selectedImageUri != null) "Change Real Part Photo" else "Upload Real Part Photo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Text(
        text = "SMC Charge Dimensions & Plan",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold
    )

    // Size Slider
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Charge Coverage Size", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(chargeScale * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = chargeScale,
            onValueChange = onChargeScaleChange,
            valueRange = 0.15f..0.85f,
            modifier = Modifier.height(24.dp)
        )
    }

    // Aspect Ratio Slider
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Charge Aspect Ratio (Oblong)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(String.format(Locale.getDefault(), "%.2f", chargeAspectY), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = chargeAspectY,
            onValueChange = onChargeAspectYChange,
            valueRange = 0.5f..2.0f,
            modifier = Modifier.height(24.dp)
        )
    }

    // Angle Slider
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ply Fiber Orientation Angle", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${chargeRotation.roundToInt()}°", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = chargeRotation,
            onValueChange = onChargeRotationChange,
            valueRange = 0f..180f,
            modifier = Modifier.height(24.dp)
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format(Locale.getDefault(), "Charge Center: X:%.0f%% | Y:%.0f%%", chargeX * 100f, chargeY * 100f),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
        )
        TextButton(
            onClick = onResetOffset,
            modifier = Modifier.height(28.dp)
        ) {
            Text("Center Charge", fontSize = 11.sp)
        }
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

    Text(
        text = "Molding Flow Evaluation Metrics",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    val maxFlowLenPercent = sqrt(
        max(chargeX, 1f - chargeX).toDouble().pow(2.0) +
        max(chargeY, 1f - chargeY).toDouble().pow(2.0)
    ).toFloat()

    val nominalFlowDistanceMm = (maxFlowLenPercent * 380f).roundToInt()
    val isFarOffCenter = chargeX < 0.32f || chargeX > 0.68f || chargeY < 0.32f || chargeY > 0.68f
    val isExtremelyLongFlow = nominalFlowDistanceMm > 220
    val hasCoreInterference = selectedTemplateIndex == 1 && (
        sqrt((chargeX - 0.3f).pow(2f) + (chargeY - 0.35f).pow(2f)) < 0.15f ||
        sqrt((chargeX - 0.7f).pow(2f) + (chargeY - 0.35f).pow(2f)) < 0.15f ||
        sqrt((chargeX - 0.3f).pow(2f) + (chargeY - 0.65f).pow(2f)) < 0.15f ||
        sqrt((chargeX - 0.7f).pow(2f) + (chargeY - 0.65f).pow(2f)) < 0.15f
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Peak Flow Path Length", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            Text("$nominalFlowDistanceMm mm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isExtremelyLongFlow) Color(0xFFFF9800) else Color(0xFF4CAF50))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), RoundedCornerShape(4.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hydraulic Balance Offset", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
            val offsetVal = (sqrt((chargeX - 0.5f).pow(2f) + (chargeY - 0.5f).pow(2f)) * 100f).roundToInt()
            Text("$offsetVal%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (offsetVal > 25) Color(0xFFFF5252) else Color(0xFF4CAF50))
        }

        Surface(
            color = when {
                hasCoreInterference -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                isExtremelyLongFlow && isFarOffCenter -> Color(0xFFFFF3E0)
                isFarOffCenter -> Color(0xFFE8F5E9)
                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = when {
                        hasCoreInterference -> Icons.Default.Warning
                        isExtremelyLongFlow -> Icons.Default.Warning
                        else -> Icons.Default.CheckCircle
                    },
                    contentDescription = "Status Icon",
                    tint = when {
                        hasCoreInterference -> MaterialTheme.colorScheme.error
                        isExtremelyLongFlow -> Color(0xFFFF9800)
                        else -> Color(0xFF4CAF50)
                    },
                    modifier = Modifier.size(18.dp)
                )

                Column {
                    Text(
                        text = when {
                            hasCoreInterference -> "Core/Boss Interference Alert!"
                            isExtremelyLongFlow && isFarOffCenter -> "High Flow Path Distance Warning"
                            isFarOffCenter -> "Asymmetrical Flow Pattern"
                            else -> "Balanced SMC Flow Layout"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = when {
                            hasCoreInterference -> "The current SMC charge is overlapping too closely with structural tool inserts. Fiber degradation, rich resin concentration, or core shifting might occur."
                            isExtremelyLongFlow && isFarOffCenter -> "Peak flow path ($nominalFlowDistanceMm mm) exceeds standard 200mm recommendation. Material faces high flow resistance; raise mold temperatures to prevent pre-curing."
                            isFarOffCenter -> "Sub-optimal charge position causes lopsided mold fill. This might create unbalanced hydraulic moments on the press slider. Press level monitoring is recommended."
                            else -> "Optimal charge alignment center. Material flows evenly to the outer mold edges, achieving uniform pressure profile and minimizing knit-lines."
                        },
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun FlowVisualizerCanvas(
    selectedTemplateIndex: Int,
    imageUri: Uri?,
    chargeX: Float,
    chargeY: Float,
    chargeScale: Float,
    chargeAspectY: Float,
    chargeRotation: Float,
    onPositionChange: (Float, Float) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF141416))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onPositionChange(
                        (offset.x / size.width).coerceIn(0f, 1f),
                        (offset.y / size.height).coerceIn(0f, 1f)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Uploaded Real Part Frame",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width
            val cy = size.height

            if (cx <= 0 || cy <= 0) return@Canvas

            // Draw clean background technician grid
            val gridStep = 40f
            for (col in 0..(cx / gridStep).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(col * gridStep, 0f),
                    end = Offset(col * gridStep, cy),
                    strokeWidth = 1f
                )
            }
            for (row in 0..(cy / gridStep).toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.04f),
                    start = Offset(0f, row * gridStep),
                    end = Offset(cx, row * gridStep),
                    strokeWidth = 1f
                )
            }

            // Draw template outlines if no image uploaded
            if (imageUri == null) {
                when (selectedTemplateIndex) {
                    0 -> {
                        // Automotive Lip Bumper
                        val path = Path()
                        path.moveTo(cx * 0.08f, cy * 0.35f)
                        path.quadraticBezierTo(cx * 0.5f, cy * 0.12f, cx * 0.92f, cy * 0.35f)
                        path.lineTo(cx * 0.92f, cy * 0.72f)
                        path.quadraticBezierTo(cx * 0.5f, cy * 0.52f, cx * 0.08f, cy * 0.72f)
                        path.close()
                        drawPath(
                            path = path,
                            color = Color(0xFF00FFCC).copy(alpha = 0.08f),
                            style = androidx.compose.ui.graphics.drawscope.Fill
                        )
                        drawPath(
                            path = path,
                            color = Color(0xFF00FFCC),
                            style = Stroke(width = 3f)
                        )
                    }
                    1 -> {
                        // Ribbed bracket rectangle
                        val rx = cx * 0.12f
                        val ry = cy * 0.15f
                        val rw = cx * 0.76f
                        val rh = cy * 0.7f
                        drawRoundRect(
                            color = Color(0xFF00E5FF).copy(alpha = 0.08f),
                            topLeft = Offset(rx, ry),
                            size = Size(rw, rh),
                            cornerRadius = CornerRadius(16f, 16f)
                        )
                        drawRoundRect(
                            color = Color(0xFF00E5FF),
                            topLeft = Offset(rx, ry),
                            size = Size(rw, rh),
                            cornerRadius = CornerRadius(16f, 16f),
                            style = Stroke(width = 3f)
                        )
                        // Inner core boss columns (obstacles)
                        val centers = listOf(
                            Offset(cx * 0.3f, cy * 0.35f),
                            Offset(cx * 0.7f, cy * 0.35f),
                            Offset(cx * 0.3f, cy * 0.65f),
                            Offset(cx * 0.7f, cy * 0.65f)
                        )
                        centers.forEach { center ->
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.5f),
                                center = center,
                                radius = 22f
                            )
                            drawCircle(
                                color = Color(0xFF00E5FF).copy(alpha = 0.5f),
                                center = center,
                                radius = 22f,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                    2 -> {
                        // Concentric Aerospace Dome circular segment
                        val center = Offset(cx / 2f, cy / 2f)
                        val maxRadius = cx.coerceAtMost(cy) * 0.42f
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = 0.07f),
                            center = center,
                            radius = maxRadius
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            center = center,
                            radius = maxRadius,
                            style = Stroke(width = 3f)
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F).copy(alpha = 0.4f),
                            center = center,
                            radius = maxRadius * 0.35f,
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
                        )
                    }
                }
            } else {
                // If real image, draw a simple neat high-tech workspace border
                drawRoundRect(
                    color = Color(0xFF8E8E93).copy(alpha = 0.6f),
                    topLeft = Offset(4f, 4f),
                    size = Size(cx - 8f, cy - 8f),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                )
            }

            // Target positioning coordinate vectors
            val px = cx * chargeX
            val py = cy * chargeY

            // Draw faint alignment crosshairs to center of touch
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(px, 0f),
                end = Offset(px, cy),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f,5f))
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(0f, py),
                end = Offset(cx, py),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f,5f))
            )

            // Flow arrows radiating outwards from the core
            val baseScale = cx * 0.13f * chargeScale
            val baseW = cx * 0.35f * chargeScale
            val baseH = cx * 0.35f * chargeScale * chargeAspectY

            for (angleDeg in 0 until 360 step 30) {
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val cosA = cos(angleRad).toFloat()
                val sinA = sin(angleRad).toFloat()

                // Start point: right on the charge boundary
                val startDist = baseScale * max(abs(cosA), abs(sinA))
                val sx = px + cosA * startDist
                val sy = py + sinA * startDist

                // Destination point based on boundaries
                var ex = px + cosA * (baseScale * 2.8f)
                var ey = py + sinA * (baseScale * 2.8f)

                // Constrain targets within mold boundaries physically
                if (imageUri == null) {
                    when (selectedTemplateIndex) {
                        0 -> { // Bumper shape clamping
                            val boundHyp = min(cx * 0.44f, cy/2f)
                            val tDist = px - (cx / 2f)
                            if (abs(tDist) > cx * 0.4f) {
                                ex = ex.coerceIn(cx * 0.08f, cx * 0.92f)
                                ey = ey.coerceIn(cy * 0.35f, cy * 0.72f)
                            }
                        }
                        1 -> { // Rect Bracket bounds
                            val rx = cx * 0.12f
                            val ry = cy * 0.15f
                            val rw = cx * 0.76f
                            val rh = cy * 0.7f
                            ex = ex.coerceIn(rx, rx + rw)
                            ey = ey.coerceIn(ry, ry + rh)
                        }
                        2 -> { // Concentric Dome circular bounding
                            val center = Offset(cx / 2f, cy / 2f)
                            val maxRadius = cx.coerceAtMost(cy) * 0.42f
                            val dx = ex - center.x
                            val dy = ey - center.y
                            val dist = sqrt(dx*dx + dy*dy)
                            if (dist > maxRadius) {
                                ex = center.x + (dx / dist) * maxRadius
                                ey = center.y + (dy / dist) * maxRadius
                            }
                        }
                    }
                } else {
                    // Frame boundaries for Custom Photo
                    ex = ex.coerceIn(cx * 0.05f, cx * 0.95f)
                    ey = ey.coerceIn(cy * 0.05f, cy * 0.95f)
                }

                // If distance is non-zero, draw the vector
                val vecLen = sqrt((ex-sx).pow(2) + (ey-sy).pow(2))
                if (vecLen > 8f) {
                    // Draw flow trail line
                    drawLine(
                        color = Color(0xFF00FF66).copy(alpha = 0.55f),
                        start = Offset(sx, sy),
                        end = Offset(ex, ey),
                        strokeWidth = 3.5f
                    )

                    // Draw moving flow pulse dots
                    val dotPercent = (System.currentTimeMillis() % 1200) / 1200f
                    val dotX = sx + (ex - sx) * dotPercent
                    val dotY = sy + (ey - sy) * dotPercent
                    drawCircle(
                        color = Color(0xFF99FFB1),
                        center = Offset(dotX, dotY),
                        radius = 4.5f
                    )

                    // Draw flow arrowhead at end point
                    val arrowAngle = atan2((ey - sy).toDouble(), (ex - sx).toDouble()).toFloat()
                    val arrowLen = 13f
                    val aw1 = ex - arrowLen * cos(arrowAngle - Math.PI / 6).toFloat()
                    val ah1 = ey - arrowLen * sin(arrowAngle - Math.PI / 6).toFloat()
                    val aw2 = ex - arrowLen * cos(arrowAngle + Math.PI / 6).toFloat()
                    val ah2 = ey - arrowLen * sin(arrowAngle + Math.PI / 6).toFloat()

                    drawLine(color = Color(0xFF00FF66), start = Offset(ex, ey), end = Offset(aw1, ah1), strokeWidth = 3f)
                    drawLine(color = Color(0xFF00FF66), start = Offset(ex, ey), end = Offset(aw2, ah2), strokeWidth = 3f)
                }
            }

            // Draw Raw SMC Sheet Charge presentation with rotational angle transform
            withTransform({
                rotate(degrees = chargeRotation, pivot = Offset(px, py))
            }) {
                val tx = px - baseW / 2
                val ty = py - baseH / 2

                drawRoundRect(
                    color = Color(0xFFFF9800).copy(alpha = 0.42f),
                    topLeft = Offset(tx, ty),
                    size = Size(baseW, baseH),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                drawRoundRect(
                    color = Color(0xFFFFB74D),
                    topLeft = Offset(tx, ty),
                    size = Size(baseW, baseH),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(
                        width = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                )

                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(px - baseW * 0.35f, py),
                    end = Offset(px + baseW * 0.35f, py),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(px + baseW * 0.35f, py),
                    end = Offset(px + baseW * 0.22f, py - 6f),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = Color.White.copy(alpha = 0.6f),
                    start = Offset(px + baseW * 0.35f, py),
                    end = Offset(px + baseW * 0.22f, py + 6f),
                    strokeWidth = 2.5f
                )
            }

            drawCircle(
                color = Color.White,
                center = Offset(px, py),
                radius = 7f
            )
            drawCircle(
                color = Color(0xFFFF9800),
                center = Offset(px, py),
                radius = 12f,
                style = Stroke(width = 2.5f)
            )
        }

        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = "Touch/Tap anywhere to reposition the raw charge",
                fontSize = 10.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun DiagnosticsSuiteTabContent(viewModel: SmcCalculationViewModel, isTablet: Boolean) {
    var subTabState by remember { mutableStateOf(0) } // 0: Thermal, 1: Cost, 2: Press, 3: Troubleshooter

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High-fidelity local sub-selector chips with premium styling
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    SubTabItem("Mayur - Thermal Advisor", Icons.Default.Settings, 0),
                    SubTabItem("Mayur - Cost & Waste Calculator", Icons.Default.Done, 1),
                    SubTabItem("Mayur - Press Cap Matcher", Icons.Default.Build, 2),
                    SubTabItem("Mayur - Troubleshooter", Icons.Default.Warning, 3)
                ).forEach { item ->
                    FilterChip(
                        selected = subTabState == item.idx,
                        onClick = { subTabState = item.idx },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(item.icon, null, modifier = Modifier.size(14.dp))
                                Text(item.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        AnimatedContent(
            targetState = subTabState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { activeSubState ->
            when (activeSubState) {
                0 -> ThermalAdvisorView(viewModel = viewModel, isTablet = isTablet)
                1 -> CostEstimatorView(viewModel = viewModel, isTablet = isTablet)
                2 -> PressMatcherView(viewModel = viewModel, isTablet = isTablet)
                3 -> TroubleshooterView(isTablet = isTablet)
            }
        }
    }
}

private data class SubTabItem(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val idx: Int)

@Composable
fun ThermalAdvisorView(viewModel: SmcCalculationViewModel, isTablet: Boolean) {
    var heatingSource by remember { mutableStateOf("Thermal Oil") } // Thermal Oil, Electric Rods, Steam
    var topMoldTemp by remember { mutableStateOf(152f) } // °C
    var botMoldTemp by remember { mutableStateOf(147f) } // °C

    val output = viewModel.calculationResult
    val partThickness = viewModel.thicknessMm.toDoubleOrNull() ?: 4.0
    val chargeWeight = output?.chargeWeightG ?: 1250.0
    val cureTime = output?.estimatedCuringTimeSec ?: 150.0

    // Energy calculation: Q = m * cp * dT
    // cp of molding compound is ~1.25 J/gK
    // assume room temperature of 25°C
    val cp = 1.25
    val dT = max(0f, topMoldTemp - 25f)
    val thermalEnergyKJ = (chargeWeight * cp * dT) / 1000.0 // kJ
    val avgHeatPowerKW = if (cureTime > 0) thermalEnergyKJ / cureTime else 0.0

    val tempDiff = topMoldTemp - botMoldTemp
    val feedbackStatus = remember(tempDiff) {
        when {
            tempDiff in 3f..6f -> ThermalStatus(
                title = "Optimal Thermal Profile",
                desc = "Excellent! Top plate (cavity) is 3°C to 6°C hotter than bottom plate (core). This ensures the top mold surface cures with superior cosmetics first, and locks the finished part to the core side upon press opening for faultless automated ejection.",
                color = Color(0xFF4CAF50),
                icon = Icons.Default.CheckCircle
            )
            tempDiff == 0f -> ThermalStatus(
                title = "Neutral Thermal Profile",
                desc = "Top and bottom molds are identically heated. This might trigger unpredictable part release behavior, where the part sticks to the upper cavity upon opening. High cosmic vacuum bonding could scrap cosmetics.",
                color = Color(0xFFFF9800),
                icon = Icons.Default.Warning
            )
            tempDiff < 0f -> ThermalStatus(
                title = "Inverted Profile (High Core Sticking Risk)",
                desc = "Warning: Bottom mold is hotter than Top mold. The cooling sequence will contract onto the cavity surface first, forcing the part to adhere to the upper punch. Severe risk of automated gripper damage on the vacuum tool.",
                color = Color(0xFFFF5252),
                icon = Icons.Default.Warning
            )
            tempDiff > 12f -> ThermalStatus(
                title = "High Thermal Gradient (Extreme Warpage Prediction)",
                desc = "Critical failure warning. Temperature delta exceeds 12°C. The lopsided cure kinetic triggers differential molecular shrinkage between top and bottom fiber skins, yielding extreme physical part warping/bowing upon pressure release.",
                color = Color(0xFFFF5252),
                icon = Icons.Default.Warning
            )
            else -> ThermalStatus(
                title = "Moderate Gradient Warning",
                desc = "The temperature delta is wider than standard recommendations (1.5°C ~ 6.0°C). Minor post-mold warping (curving) is probable. Cooling fixture sizing might be necessary to restrain structural flatness.",
                color = Color(0xFFFFB74D),
                icon = Icons.Default.Warning
            )
        }
    }

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "Mayur - Thermal Differential & Heat Balance Advisor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Mayur - Analyze temperature mismatch between cavity and core sides to avoid cosmic warpage & predict energy ingestion.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Main Gradient Simulation Canvas Area
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Draw Grid lines
                        for (i in 0..(w / 50f).toInt()) {
                            drawLine(Color.White.copy(alpha = 0.03f), Offset(i * 50f, 0f), Offset(i * 50f, h), 1f)
                        }

                        // Top Cavity Tool Plate Block
                        // Color ranges from Yellowish (135C) to Dark Red (170C)
                        val topPercent = ((topMoldTemp - 135) / (175 - 135)).coerceIn(0f, 1f)
                        val topColor = Color(
                            red = 0.9f,
                            green = (0.6f - topPercent * 0.4f).coerceIn(0f, 1f),
                            blue = (0.2f - topPercent * 0.15f).coerceIn(0f, 1f)
                        )
                        drawRect(
                            color = topColor,
                            topLeft = Offset(w * 0.15f, h * 0.12f),
                            size = Size(w * 0.7f, h * 0.25f)
                        )
                        // Top Temperature text label overlay
                        // Canvas lacks direct text support without NativeCanvas. We draw visually rich helper lines
                        
                        // Bottom Core Tool Plate Block
                        val botPercent = ((botMoldTemp - 135) / (175 - 135)).coerceIn(0f, 1f)
                        val botColor = Color(
                            red = 0.9f,
                            green = (0.6f - botPercent * 0.4f).coerceIn(0f, 1f),
                            blue = (0.2f - botPercent * 0.15f).coerceIn(0f, 1f)
                        )
                        drawRect(
                            color = botColor,
                            topLeft = Offset(w * 0.15f, h * 0.63f),
                            size = Size(w * 0.7f, h * 0.25f)
                        )

                        // SMC Part inside Cavity
                        val smcColor = Color(0xFFE0E0E0)
                        val curedPercent = 0.5f
                        val blendColor = Color(0xFFFF9800).copy(alpha = 0.8f)
                        val r = smcColor.red + (blendColor.red - smcColor.red) * curedPercent
                        val g = smcColor.green + (blendColor.green - smcColor.green) * curedPercent
                        val b = smcColor.blue + (blendColor.blue - smcColor.blue) * curedPercent
                        val a = smcColor.alpha + (blendColor.alpha - smcColor.alpha) * curedPercent
                        drawRoundRect(
                            color = Color(r, g, b, a),
                            topLeft = Offset(w * 0.22f, h * 0.44f),
                            size = Size(width = w * 0.56f, height = h * 0.12f),
                            cornerRadius = CornerRadius(4f, 4f)
                        )

                        // Heat flow vectors/waves
                        val timeMod = (System.currentTimeMillis() % 1600) / 1600f
                        val waveY1 = h * 0.37f + (h * 0.08f * timeMod)
                        drawLine(
                            color = Color.White.copy(alpha = (1f - timeMod) * 0.5f),
                            start = Offset(w * 0.3f, waveY1),
                            end = Offset(w * 0.7f, waveY1),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                        )

                        val waveY2 = h * 0.63f - (h * 0.08f * timeMod)
                        drawLine(
                            color = Color.White.copy(alpha = (1f - timeMod) * 0.5f),
                            start = Offset(w * 0.3f, waveY2),
                            end = Offset(w * 0.7f, waveY2),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                        )
                    }

                    // On-Canvas UI labels and temperature tags
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color.White, RoundedCornerShape(100.dp)))
                        Text("Cavity (Top): ${topMoldTemp.roundToInt()}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(Color.White, RoundedCornerShape(100.dp)))
                        Text("Core (Bottom): ${botMoldTemp.roundToInt()}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 24.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "Delta T: %.1f°C", tempDiff),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tempDiff in 3f..6f) Color(0xFF4CAF50) else Color(0xFFFF5252)
                        )
                    }
                }
            }

            // Controllers Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Top Cavity Temp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${topMoldTemp.roundToInt()}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = topMoldTemp,
                        onValueChange = { topMoldTemp = it },
                        valueRange = 135f..170f,
                        modifier = Modifier.height(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Bottom Core Temp", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${botMoldTemp.roundToInt()}°C", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = botMoldTemp,
                        onValueChange = { botMoldTemp = it },
                        valueRange = 135f..170f,
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            // Quick Source Chip Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Heat Source Power Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Thermal Oil", "Electric Cartridge", "Steam Platen").forEach { source ->
                        FilterChip(
                            selected = heatingSource == source,
                            onClick = { heatingSource = source },
                            label = { Text(source, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Real-Time Diagnostic Advisory Panel
            Card(
                colors = CardDefaults.cardColors(containerColor = feedbackStatus.color.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, feedbackStatus.color.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = feedbackStatus.icon,
                        contentDescription = "Status Icon",
                        tint = feedbackStatus.color,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = feedbackStatus.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = feedbackStatus.desc,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Heating energy dynamic indicators
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Tooling Thermal Energy Metrics",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Transient Energy Ingest", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "%.1f kJ", thermalEnergyKJ), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Heat Transfer Velocity", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "%.2f kW", avgHeatPowerKW), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = "Estimated based on a typical SMC Heat Capacity of 1.25 J/gK inside a multi-zone temperature controlled mold. Heat loss at shearing edges is ignored.",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = 12.sp
                    )
                }
            }
        }
    }

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        content()
    }
}

private data class ThermalStatus(
    val title: String,
    val desc: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun CostEstimatorView(viewModel: SmcCalculationViewModel, isTablet: Boolean) {
    var compoundCostPerKg by remember { mutableStateOf(3.50f) } // $
    var annualVolume by remember { mutableStateOf(20000f) } // parts/year

    val output = viewModel.calculationResult
    val partWeightKg = (output?.partWeightG ?: 1200.0) / 1000.0
    val chargeWeightKg = (output?.chargeWeightG ?: 1224.0) / 1000.0
    val garbageFlashKg = (chargeWeightKg - partWeightKg).coerceAtLeast(0.0)

    val singlePartMaterialCost = chargeWeightKg * compoundCostPerKg
    val singlePartFlashWasteCost = garbageFlashKg * compoundCostPerKg
    val totalAnnualMaterialInvestment = singlePartMaterialCost * annualVolume
    val totalAnnualMoneyLostToFlash = singlePartFlashWasteCost * annualVolume

    // ROI Scenario: Reducing flash loss down to 1.0% by optimal template cutting vs current flash scale
    // Standard waste reduction scenario calculations
    val optimizedFlashPercent = 0.01 // 1%
    val optimizedChargeWeightKg = partWeightKg * (1.0 + optimizedFlashPercent)
    val optimizedSinglePartCost = optimizedChargeWeightKg * compoundCostPerKg
    val prospectiveSavingsPerPart = (singlePartMaterialCost - optimizedSinglePartCost).coerceAtLeast(0.0)
    val annualSavingsTarget = prospectiveSavingsPerPart * annualVolume

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Mayur - SMC Cost & Material Waste Estimator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Analyze cumulative financial losses directly stemming from resin flashing and check prospective nesting returns.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Two-column responsive metric panel
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Unit Cost Structure (Per Molding Strobe)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Part Mass", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "%.3f kg", partWeightKg), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Column {
                            Text("Charge Mass (w/ Flash)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "%.3f kg", chargeWeightKg), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Squeezed Out Flash", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "%.3f kg", garbageFlashKg), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Raw Compound Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "$%.2f", singlePartMaterialCost), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Trim Waste Waste Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(String.format(Locale.getDefault(), "$%.2f", singlePartFlashWasteCost), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF5252))
                        }
                    }
                }
            }

            // Cost Sliders controllers
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Compound Cost per KG ($)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.getDefault(), "$%.2f /kg", compoundCostPerKg), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = compoundCostPerKg,
                        onValueChange = { compoundCostPerKg = it },
                        valueRange = 1.50f..8.50f,
                        modifier = Modifier.height(26.dp)
                    )
                }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Annual Production Run Target", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(String.format(Locale.getDefault(), "%s parts/yr", annualVolume.roundToInt().toString()), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = annualVolume,
                        onValueChange = { annualVolume = it },
                        valueRange = 1000f..100000f,
                        steps = 99,
                        modifier = Modifier.height(26.dp)
                    )
                }
            }

            // Material utilized vs scrap representation graph
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Expenditure Composition Breakdown", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val utilizedPercent = (partWeightKg / chargeWeightKg).toFloat()

                            // Render utilized color (Green/Blue theme)
                            drawRect(
                                color = Color(0xFF1E88E5),
                                topLeft = Offset(0f, 0f),
                                size = Size(w * utilizedPercent, h)
                            )
                            // Render wasted color (Crimson)
                            drawRect(
                                color = Color(0xFFFF5252),
                                topLeft = Offset(w * utilizedPercent, 0f),
                                size = Size(w * (1f - utilizedPercent), h)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFF1E88E5), RoundedCornerShape(2.dp)))
                            Text(String.format(Locale.getDefault(), "Finished Part Value: $%.1fK (%.1f%%)", (totalAnnualMaterialInvestment - totalAnnualMoneyLostToFlash)/1000f, (partWeightKg/chargeWeightKg)*100f), fontSize = 10.sp, color = Color.LightGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5252), RoundedCornerShape(2.dp)))
                            Text(String.format(Locale.getDefault(), "Wasted Flash Loss: $%.1fK (%.1f%%)", totalAnnualMoneyLostToFlash/1000f, (garbageFlashKg/chargeWeightKg)*100f), fontSize = 10.sp, color = Color.LightGray)
                        }
                    }
                }
            }

            // ROI Optimization Action Card (Highly Polished Green Box)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                border = BorderStroke(1.dp, Color(0xFF81C784))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "ROI Success Icon",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Precision Molding Opportunity Detected",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(
                                Locale.getDefault(),
                                "By implementing precise nested water-jet or ultrasonic ply template cutters, you can reduce the flash trim allowance to exactly 1.0%%. For this trial, that single yield enhancement secures $%.2f in savings per stroke, yielding a stunning target savings of $%,.2f per year.",
                                prospectiveSavingsPerPart,
                                annualSavingsTarget
                            ),
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        content()
    }
}

@Composable
fun PressMatcherView(viewModel: SmcCalculationViewModel, isTablet: Boolean) {
    var availablePressCapacityTons by remember { mutableStateOf(800f) }

    val output = viewModel.calculationResult
    val requiredForceTons = output?.clampingForceTons ?: 450.0

    val ratio = (requiredForceTons / availablePressCapacityTons)
    val ratioPercent = (ratio * 100f).roundToInt()

    val matchEvaluation = remember(ratio, requiredForceTons, availablePressCapacityTons) {
        when {
            requiredForceTons > availablePressCapacityTons -> PressEvaluationStatus(
                title = "CRITICAL INSUFFICIENT TONNAGE",
                color = Color(0xFFFF5252),
                desc = "Warning: The mold required force of ${requiredForceTons.roundToInt()} Tons exceeds available press capacity of ${availablePressCapacityTons.roundToInt()} Tons. Attempting to cycle will trigger safety emergency limits, separate the parting lines, produce excessive flashing, or cause internal resin starvation."
            )
            ratio >= 0.85f -> PressEvaluationStatus(
                title = "BORDERLINE SYSTEM CAPABILITY",
                color = Color(0xFFFF9800),
                desc = "Operation possible but highly unsafe. Compacting is occurring at ${ratioPercent}% capacity. Press tie bars, guidance rails, and hydraulic platen cylinders will endure elevated flexural strains, causing accelerated wear on seals."
            )
            ratio in 0.55f..0.85f -> PressEvaluationStatus(
                title = "OPTIMAL EFFICIENCY ZONE",
                color = Color(0xFF4CAF50),
                desc = "Excellent match! Running at ${ratioPercent}% press rating ensures standard hydraulic compression speeds, maintains perfect mold parallel positioning control, and prolongs machine life."
            )
            else -> PressEvaluationStatus(
                title = "SUB-OPTIMAL LOW UTILIZATION",
                color = Color(0xFF1E88E5),
                desc = "Oversized machine choice. Press capacity is highly underutilized (only ${ratioPercent}%). Squeezing a small projection tool inside a gigantic hydraulic press boosts electric utilities waste, yields sluggish closure, and strains individual mold shear edges."
            )
        }
    }

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Mayur - Press Capacity Matcher & Efficiency Judge",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Confirm tool compatibility. Verify that required mold projection area clamping force perfectly matches hydraulic press specifications.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Dual indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MOLD FORCE DEMAND", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${requiredForceTons.roundToInt()} T", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TARGET PRESS ENGINE", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${availablePressCapacityTons.roundToInt()} T", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Slider of press options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Select Shop Press Tonnage Limit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${availablePressCapacityTons.roundToInt()} Metric Tons", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = availablePressCapacityTons,
                    onValueChange = { availablePressCapacityTons = it },
                    valueRange = 100f..2500f,
                    modifier = Modifier.height(26.dp)
                )

                // Quick presets buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(300f, 500f, 1000f, 1500f, 2000f).forEach { tonRating ->
                        AssistChip(
                            onClick = { availablePressCapacityTons = tonRating },
                            label = { Text("${tonRating.roundToInt()}T", fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Gauge utilization graphics
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tonnage Utilization Gauge", fontSize = 11.sp, color = Color.White)
                        Text("$ratioPercent%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = matchEvaluation.color)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height

                            val cap = ratio.coerceIn(0.0, 1.0).toFloat()
                            drawRect(
                                color = matchEvaluation.color,
                                topLeft = Offset(0f, 0f),
                                size = Size(width = w * cap, height = h)
                            )

                            // 85% limit tick line
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(w * 0.85f, 0f),
                                end = Offset(w * 0.85f, h),
                                strokeWidth = 2.5f
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0%", fontSize = 9.sp, color = Color.Gray)
                        Text("Optimal (55-85%)", fontSize = 9.sp, color = Color.Gray, modifier = Modifier.padding(start = 30.dp))
                        Text("Safety Limit (85%)", fontSize = 9.sp, color = Color.Gray)
                    }
                }
            }

            // Evaluation status advisory card
            Card(
                colors = CardDefaults.cardColors(containerColor = matchEvaluation.color.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, matchEvaluation.color.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (ratioPercent > 100) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = "Evaluation Status",
                        tint = matchEvaluation.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = matchEvaluation.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = matchEvaluation.desc,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        content()
    }
}

private data class PressEvaluationStatus(
    val title: String,
    val color: Color,
    val desc: String
)

@Composable
fun TroubleshooterView(isTablet: Boolean) {
    var selectedDefectIndex by remember { mutableStateOf(0) }

    val defects = listOf(
        DefectItem(
            name = "Surface Blisters (Bubbles)",
            tagline = "Gas/moisture bubbles bulging on skin",
            severity = "Moderate",
            cause = "Incomplete degassing. Internal moisture, volatile monomer solvents in unsaturated polyester resin matrix, or premature chemical closure sealing hot gases inside core.",
            symptoms = "Circular hollow bumps appearing on exterior skins within 20 minutes of demolding.",
            action = "Relieve mold compression; open venting slits. Increase early close dwell time. Lower temperature of surface to retard skin curing before air venting executes.",
            redesign = "Extend mold shear edge venting margins. Lower shelf-age of compound to enforce batch dryness. Set lower initiator concentration."
        ),
        DefectItem(
            name = "Short Mold (Non-fill cavity)",
            tagline = "Unfilled edges, thin margins, skeletal ribs",
            severity = "Critical",
            cause = "Resin gelation/pre-cure prevents flow completing. Flow resistance exceeds hydraulic compression, or charge weight has sub-nominal scale.",
            symptoms = "Ragged, brittle, unfilled borders, empty structural bosses, fibers visible at fringes.",
            action = "Increase raw SMC charge layer weight by adding a small balancing helper layer. Maximize hydraulic slide approach speed. Decrease platen heat to slow down premature curing.",
            redesign = "Relocate local charge placement templates closer to deep pockets or corners. Add helper channels or taper high vertical walls."
        ),
        DefectItem(
            name = "Weld Lines (Knit paths)",
            tagline = "Visible seams where separate stream-lines meet",
            severity = "Moderate",
            cause = "Laps or knit marks. Separate fluid stream-lines converge with too low local cross-linking pressure, stalling fiber integration and forming micro-cracks.",
            symptoms = "Linear surface lines, highly prone to direct structural snapping or physical failure during testing.",
            action = "Reposition raw charges as a cohesive single sheet rather than multiple puzzle segments. Elevate mold cylinder pressure to foster flow convergence.",
            redesign = "Optimize charge overlap structure. Re-design part flow path obstacles to minimize stream splitting."
        ),
        DefectItem(
            name = "Fiber Segregation (Resin Rich)",
            tagline = "Glass fibers stall, resin flows separately",
            severity = "High",
            cause = "Excessive flow paths. Material travels over extreme distances, forcing long glass fibers to stall against micro-structural corners or narrow mold clearances.",
            symptoms = "Translucent structural borders lacking glass fiber reinforcements, prone to severe thermal stress cracking.",
            action = "Increase charge coverage area (e.g., from 60% up to 75%) to cut flow travel distances. Use low-shrinkage low-profile resin matrix blends.",
            redesign = "Smooth interior corners with generous fillets. Avoid sudden narrow-gap sections inside vertical rib segments."
        ),
        DefectItem(
            name = "Part Warpage (Twisting)",
            tagline = "Uncontrolled bending or curl on ejecting",
            severity = "High",
            cause = "Uneven molecular cure shrinkage rates between raw top and bottom faces, or high thermal shock differential across tooling components.",
            symptoms = "Part twisting upon cooling. Structural dimensions fall out of standard assembly fit tolerances.",
            action = "Check thermal zones on manifolds. Maintain top mold 3°C to 5°C hotter than bottom center. Use a cooling fixture table immediately after demolding.",
            redesign = "Ensure uniform part wall thickness. Eliminate severe asymmetry across rib layouts or side bosses."
        )
    )

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text(
                    text = "Mayur - Molding Defects Expert Troubleshooter",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Interactive physical faults manual. Tap a defect to extract real-world root causes and dynamic shop-floor corrective actions.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Defects Horizontal scroll grid selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                defects.forEachIndexed { index, defect ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedDefectIndex == index) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(
                            width = 1.2.dp,
                            color = if (selectedDefectIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .width(180.dp)
                            .clickable { selectedDefectIndex = index }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (defect.severity) {
                                        "Critical" -> "CRITICAL"
                                        "High" -> "HIGH"
                                        else -> "MODERATE"
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when (defect.severity) {
                                        "Critical" -> Color(0xFFFF5252)
                                        "High" -> Color(0xFFFF9800)
                                        else -> Color(0xFF1E88E5)
                                    },
                                    modifier = Modifier
                                        .background(
                                            color = when (defect.severity) {
                                                "Critical" -> Color(0xFFFF5252).copy(alpha = 0.08f)
                                                "High" -> Color(0xFFFF9800).copy(alpha = 0.08f)
                                                else -> Color(0xFF1E88E5).copy(alpha = 0.08f)
                                            },
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )

                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = defect.name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )

                            Text(
                                text = defect.tagline,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 2,
                                lineHeight = 12.sp,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Expanded detail view
            val activeDefect = defects[selectedDefectIndex]
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeDefect.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Severity: ${activeDefect.severity}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (activeDefect.severity) {
                                "Critical" -> Color(0xFFFF5252)
                                "High" -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Physical symptoms
                    Column {
                        Text("Visible Symptoms", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Text(activeDefect.symptoms, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                    }

                    // Physical Root cause
                    Column {
                        Text("Physical Root Cause", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                        Text(activeDefect.cause, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 15.sp)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                    // Live shop floor corrective actions
                    Surface(
                        color = Color(0xFFFFECEB),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Shop Floor Operator Response (Immediate)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFAD140F)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                activeDefect.action,
                                fontSize = 11.sp,
                                color = Color(0xFF5C0000),
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Engineering redesign modifications
                    Surface(
                        color = Color(0xFFE8F0FE),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Tooling & Material Redesign (Long-Term)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1A73E8)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                activeDefect.redesign,
                                fontSize = 11.sp,
                                color = Color(0xFF0D3C7A),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (isTablet) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    } else {
        content()
    }
}

private data class DefectItem(
    val name: String,
    val tagline: String,
    val severity: String,
    val cause: String,
    val symptoms: String,
    val action: String,
    val redesign: String
)
