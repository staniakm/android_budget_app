package com.example.internetapi.ui

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.internetapi.api.Resource
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.BudgetViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate


@AndroidEntryPoint
class ChartActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    OnChartValueSelectedListener {
    private val viewModel: BudgetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setTitle("PieChartActivity")

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ChartScreen(
                        viewModel = viewModel,
                        onValueSelected = { _, _ -> Unit },
                        onNothingSelected = { Unit }
                    )
                }
            }
        }
    }

    private fun processSuccess(it: Resource<Budget>) {
        it.data.let { res ->
            if (res != null) {
                res.let { data ->
                    // No-op in Compose version; handled in ChartScreen
                }
            } else {
                // No-op in Compose version; handled in ChartScreen
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        TODO("Not yet implemented")
    }


    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onValueSelected(e: Entry?, h: Highlight) {
        // handled inside Compose via chart listener
    }

    override fun onNothingSelected() {
        // handled inside Compose via chart listener
    }
}

private object ChartDefaults {
    const val ScreenPadding = 12
    const val CardPadding = 12
}

@Composable
private fun ChartScreen(
    viewModel: BudgetViewModel,
    onValueSelected: (label: String, value: Float) -> Unit,
    onNothingSelected: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch { scaffoldState.snackbarHostState.showSnackbar(message) }
    }

    var refreshKey by rememberSaveable { mutableStateOf(0) }
    val budgetsLiveData = remember(refreshKey) { viewModel.getBudgets() }
    val budgetsResource = observeResource(budgetsLiveData)

    LaunchedEffect(budgetsResource?.status) {
        if (budgetsResource?.status == Status.ERROR) showMessage("failed fetched data")
    }

    val labels = remember(MonthSelector.month) { monthSwitcherLabels(monthOffset = MonthSelector.month) }

    var infoText by rememberSaveable { mutableStateOf("") }
    var chartData by remember { mutableStateOf<PieData?>(null) }

    LaunchedEffect(budgetsResource?.data) {
        val budget = budgetsResource?.data ?: return@LaunchedEffect
        chartData = buildPieData(budget)
        infoText = ""
        onNothingSelected()
    }

    Scaffold(scaffoldState = scaffoldState) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = ChartDefaults.ScreenPadding.dp)
        ) {
            UnifiedMonthSwitcher(
                labels = labels,
                onPrevious = {
                    MonthSelector.previous()
                    refreshKey += 1
                },
                onCurrent = {
                    if (MonthSelector.month != 0) {
                        MonthSelector.current()
                        refreshKey += 1
                    }
                },
                onNext = {
                    if (MonthSelector.month < 0) {
                        MonthSelector.next()
                        refreshKey += 1
                    }
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        PieChart(context).apply {
                            setUsePercentValues(true)
                            description.isEnabled = false
                            setExtraOffsets(5F, 10F, 5F, 5F)
                            dragDecelerationFrictionCoef = 0.95f

                            isDrawHoleEnabled = true
                            setHoleColor(Color.WHITE)
                            setTransparentCircleColor(Color.WHITE)
                            setTransparentCircleAlpha(110)
                            holeRadius = 30f
                            transparentCircleRadius = 33f

                            rotationAngle = 0.0F
                            isRotationEnabled = true
                            isHighlightPerTapEnabled = true

                            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                                override fun onValueSelected(e: Entry?, h: Highlight?) {
                                    val pe = e as? PieEntry ?: return
                                    val label = pe.label
                                    val value = pe.y
                                    infoText = "$label  -  $value zl"
                                    onValueSelected(label, value)
                                }

                                override fun onNothingSelected() {
                                    infoText = ""
                                    onNothingSelected()
                                }
                            })

                            animateY(1400, Easing.EaseInOutQuad)
                            setEntryLabelColor(Color.WHITE)
                            setEntryLabelTypeface(Typeface.DEFAULT_BOLD)
                            setEntryLabelTextSize(12f)

                            legend.isEnabled = false
                        }
                    },
                    update = { chart ->
                        val data = chartData
                        if (data != null) {
                            chart.data = data
                            chart.highlightValues(null)
                            chart.invalidate()
                        }
                    }
                )

                if (budgetsResource?.status == Status.LOADING) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            Text(
                text = infoText,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

private fun buildPieData(budgetData: Budget): PieData {
    val other = budgetData.budgets
        .filter { it.spent > BigDecimal.ZERO && it.spent <= BigDecimal(40) }
        .sumOf { it.spent }
        .toFloat()

    val entries = budgetData.budgets
        .filter { it.spent > BigDecimal(40) }
        .map { PieEntry(it.spent.toFloat(), it.category) }
        .plus(PieEntry(other, "Reszta"))
        .sortedByDescending { it.value }

    val dataSet = PieDataSet(entries, "Categories").apply {
        setDrawIcons(false)
        sliceSpace = 3f
        iconsOffset = MPPointF(0F, 40F)
        selectionShift = 0f
        colors = listOf(
            Color.BLUE,
            Color.CYAN,
            Color.DKGRAY,
            Color.GRAY,
            Color.LTGRAY,
            Color.MAGENTA,
            Color.RED,
            Color.rgb(203, 63, 63),
            Color.rgb(138, 63, 203),
            Color.rgb(77, 63, 203),
            Color.rgb(54, 134, 188),
            Color.rgb(54, 188, 117),
            Color.rgb(85, 188, 54),
            Color.rgb(188, 170, 54)
        )
    }

    return PieData(dataSet).apply {
        setValueFormatter(PercentFormatter())
        setValueTextSize(11f)
        setValueTextColor(Color.WHITE)
        setValueTypeface(Typeface.DEFAULT_BOLD)
    }
}
