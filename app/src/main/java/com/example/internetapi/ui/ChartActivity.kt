package com.example.internetapi.ui

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.WindowManager
import com.github.mikephil.charting.charts.PieChart;
import android.widget.SeekBar
import androidx.activity.viewModels
import com.example.internetapi.api.Resource
import com.example.internetapi.config.DateFormatter
import com.example.internetapi.databinding.ActivityChartBinding
import com.example.internetapi.global.MonthSelector
import com.example.internetapi.models.Budget
import com.example.internetapi.models.Status
import com.example.internetapi.ui.viewModel.BudgetViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.time.LocalDate


@AndroidEntryPoint
class ChartActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener,
    OnChartValueSelectedListener {
    private val viewModel: BudgetViewModel by viewModels()
    private lateinit var binding: ActivityChartBinding

    private lateinit var chart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        binding = ActivityChartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.monthManipulator.previous.setOnClickListener {
            MonthSelector.previous()
            loadData()
        }

        binding.monthManipulator.next.setOnClickListener {
            if (MonthSelector.month < 0) {
                MonthSelector.next()
                loadData()
            }
        }

        binding.monthManipulator.date.setOnClickListener {
            if (MonthSelector.month != 0) {
                MonthSelector.current()
                loadData()
            }
        }



        setTitle("PieChartActivity");

        chart = binding.chart1
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5F, 10F, 5F, 5F);

        chart.setDragDecelerationFrictionCoef(0.95f);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(30f);
        chart.setTransparentCircleRadius(33f);

        chart.setRotationAngle(0.0F);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // chart.setUnit(" €");
        // chart.setDrawUnitsInChart(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        chart.animateY(1400, Easing.EaseInOutQuad);
        // chart.spin(2000, 0, 360);

        val l = chart.getLegend();
        l.isEnabled = false

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTypeface(Typeface.DEFAULT_BOLD);
        chart.setEntryLabelTextSize(12f);

        loadData()
    }

    fun loadData() {
        val date = LocalDate.now().withDayOfMonth(1)
        binding.monthManipulator.date.text = date.plusMonths(MonthSelector.month.toLong())
            .format(DateFormatter.yyyymm)
        binding.monthManipulator.previous.text = date.plusMonths(MonthSelector.month.toLong() - 1)
            .format(DateFormatter.yyyymm)
        binding.monthManipulator.next.text = date.plusMonths(MonthSelector.month.toLong() + 1)
            .format(DateFormatter.yyyymm)
        viewModel.getBudgets().observe(this, {
            when (it.status) {
                Status.SUCCESS -> processSuccess(it)
                Status.ERROR -> Snackbar.make(
                    binding.root,
                    "failed fetched data",
                    Snackbar.LENGTH_LONG
                )
                    .show()
                Status.LOADING -> Log.println(Log.DEBUG, "InvoiceDetails", "Loading.....")
            }
        })
    }

    private fun processSuccess(it: Resource<Budget>) {
        it.data.let { res ->
            if (res != null) {
                res.let { data ->
                    setData(data)
                }
            } else {
                Snackbar.make(binding.root, "Status = false", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun setData(budgetData: Budget) {

        val other =
            budgetData.budgets.filter { it.spent > BigDecimal.ZERO && it.spent <= BigDecimal(40) }
                .sumOf { it.spent }.toFloat()

        val dataSet = budgetData.budgets.filter { it.spent > BigDecimal(40) }
            .map {
                PieEntry(it.spent.toFloat(), it.category)
            }.plus(PieEntry(other, "Reszta"))
            .sortedByDescending { it.value }
            .let {
                PieDataSet(it, "Categories")
                    .apply {
                        setDrawIcons(false);
                        setSliceSpace(3f);
                        setIconsOffset(MPPointF(0F, 40F));
                        setSelectionShift(5f);
                    }
            }

        // add a lot of colors
        val colors = listOf<Int>(Color.BLUE,Color.CYAN, Color.DKGRAY, Color.GRAY, Color.LTGRAY, Color.MAGENTA, Color.RED,
        Color.rgb(203,63,63),Color.rgb(138,63,203), Color.rgb(77,63,203),
        Color.rgb(54,134,188), Color.rgb(54,188,117), Color.rgb(85,188,54), Color.rgb(188,170,54))

        dataSet.setColors(colors);
        dataSet.setSelectionShift(0f);

        val data: PieData = PieData(dataSet);
        data.setValueFormatter(PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTypeface(Typeface.DEFAULT_BOLD);

        chart.setData(data);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        TODO("Not yet implemented")
    }


    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onValueSelected(e: Entry?, h: Highlight) {
        if (e == null)
            return;

        binding.info.text = (e as PieEntry).label + "  -  ${e.y} zł"
    }

    override fun onNothingSelected() {
        binding.info.text = ""
    }
}