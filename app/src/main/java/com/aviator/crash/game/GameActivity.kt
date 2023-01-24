package com.aviator.crash.game

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.aviator.crash.game.databinding.FragmentGameBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.IOException
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

class GameActivity : Fragment() {

    private lateinit var binding: FragmentGameBinding
    private lateinit var list: MutableList<Entry>
    private lateinit var dataSet: LineDataSet
    private lateinit var data: LineData
    private lateinit var preferences: SharedPreferences
    private var paused = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGameBinding.inflate(inflater, container, false)
        try {
            // get input stream
            val ims = requireContext().assets.open("av.png")
            // load image as Drawable
            val d = Drawable.createFromStream(ims, null)
            // set image to ImageView
            binding.imageView6.setImageDrawable(d)
            ims.close()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        preferences =
            requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
        binding.line.setDrawGridBackground(false)
        list = ArrayList()
        dataSet = LineDataSet(list, "")
        binding.line.xAxis.setDrawGridLines(false)
        binding.line.axisLeft.setDrawGridLines(false)
        binding.line.axisRight.setDrawGridLines(false)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        dataSet.setDrawValues(false)
        dataSet.setDrawCircles(false)
        dataSet.isHighlightEnabled = true
        dataSet.setDrawVerticalHighlightIndicator(false)
        dataSet.setDrawHorizontalHighlightIndicator(false)
        dataSet.lineWidth = 3f
        dataSet.cubicIntensity = 0.1f
        dataSet.color = R.color.chart
        dataSet.fillAlpha = 255
        dataSet.setDrawFilled(true)
        dataSet.fillColor = R.color.chart
        dataSet.setCircleColor(R.color.chart)
        dataSet.setGradientColor(R.color.chart, R.color.chart)
        dataSet.highLightColor = resources.getColor(R.color.empty, requireContext().theme)
        dataSet.axisDependency = YAxis.AxisDependency.LEFT
        dataSet.isHighlightEnabled = true
        dataSet.valueTextColor = Color.GREEN
        dataSet.setDrawFilled(true)
        binding.line.setDrawGridBackground(false)
        binding.line.description.isEnabled = false
        data = LineData(dataSet)
        binding.line.data = data
        binding.line.xAxis.isEnabled = false
        binding.line.axisRight.isEnabled = false
        binding.line.axisLeft.isEnabled = false
        val data1: MutableList<Entry> = ArrayList()
        data1.add(Entry(0F, 10000F * 10000F))
        for (i in 1..9999) data1.add(Entry(i.toFloat(), 0F))
        val dataSet1 = LineDataSet(data1, "1")
        dataSet1.setDrawFilled(false)
        dataSet1.color = R.color.chart
        dataSet1.fillColor = R.color.chart
        dataSet1.setCircleColor(R.color.chart)
        dataSet1.lineWidth = 0.2f
        dataSet1.fillAlpha = 255
        dataSet1.isHighlightEnabled = false
        binding.line.data.addDataSet(dataSet1)
        binding.line.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry, h: Highlight) {
                val params = ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.leftMargin = h.xPx.toInt() - binding.imageView3.width / 4
                params.bottomMargin =
                    binding.layout.height - h.yPx.toInt() - binding.constraintLayout.height
                params.bottomMargin -= if (e.x < 2300f) binding.imageView3.height / 3 else binding.imageView3.height / 5
                params.leftToLeft = R.id.layout
                params.bottomToBottom = R.id.layout
                binding.layout.updateViewLayout(binding.imageView3, params)




            }



            override fun onNothingSelected() {}
        })
        binding.line.legend.isEnabled = false
        binding.line.setPinchZoom(false)
        binding.line.setTouchEnabled(false)
        binding.line.setScaleEnabled(false)
        binding.line.isDragXEnabled = false
        binding.line.isDoubleTapToZoomEnabled = false
        binding.line.isDragEnabled = false
        binding.line.setOnTouchListener { _, _ ->
            true
        }
        binding.line.setDrawBorders(false)
        binding.line.xAxis.setDrawAxisLine(false)
        binding.line.axisLeft.setDrawAxisLine(false)
        binding.button2.setOnClickListener { start() }
        binding.balance.text = preferences.getString("balance", "1000")
        binding.inc.setOnClickListener {
            var bet = binding.bet.text.toString().toInt()
            bet += 1
            binding.bet.text = min(
                bet,
                binding.balance.text.toString().toInt()
            ).toString()
        }
        binding.dec.setOnClickListener {
            var bet = binding.bet.text.toString().toInt()
            bet -= 1
            binding.bet.text = max(bet, 10).toString()
        }

        binding.button3.setOnClickListener {
           paused = true
        }
        return binding.root
    }

    private var disposable: Disposable? = null



    private fun start() {
        paused = false;
        val bet = binding.bet.text.toString().toInt()
        var balance = binding.balance.text.toString().toInt()
        if (bet > balance) return
        balance -= bet
        preferences
            .edit()
            .putString("balance", balance.toString())
            .apply()
        binding.balance.text = balance.toString()
        list.clear()
        binding.button2.isEnabled = false
        binding.inc.isEnabled = false
        binding.dec.isEnabled = false
        val params = ConstraintLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 60
        params.leftToLeft = R.id.layout
        params.bottomToBottom = R.id.layout
        binding.layout.removeView(binding.imageView3)
        binding.layout.addView(binding.imageView3, params)
        binding.count.visibility = View.VISIBLE
        binding.imageView3.visibility = View.VISIBLE
        val observable: Observable<Float> =
            Observable.create { emitter ->
                var i = 0
                while (!paused && i < 6000) {
                    emitter.onNext(i.toFloat())
                    try {
                        Thread.sleep(4)
                    } catch (e: Exception) {
                        break
                    }
                    i += 3
                }
                emitter.onComplete()
            }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    if (activity == null) return@doOnComplete
                    if (!requireView().isAttachedToWindow) return@doOnComplete
                    preferences
                        .edit()
                        .putString(
                            "balance", (
                                    preferences.getString("balance", "1000")!!
                                        .toInt() + (binding.count.text.toString()
                                        .replace(",", ".")
                                        .toFloat() * bet).toInt()).toString()
                        )
                        .apply()
                    requireActivity().runOnUiThread {
                        binding.balance.text = preferences.getString(
                            "balance",
                            "1000"
                        )
                    }
                    val completable: Completable = Completable.create { emitter ->
                        clear()
                        requireActivity().runOnUiThread {
                            binding.inc.isEnabled = true
                            binding.dec.isEnabled = true
                            binding.button2.isEnabled = true
                        }
                        emitter.onComplete()
                    }.delaySubscription(if(paused) 0 else 6000, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.from(Looper.getMainLooper()))
                        .observeOn(AndroidSchedulers.from(Looper.getMainLooper()))
                    completable.subscribe()
                    disposable!!.dispose()
                }
        disposable = observable.subscribe { aFloat ->
            //Log.d("TAG","SIZE: "+list.size());
            list.add(
                Entry(
                    aFloat,
                    (aFloat + 1100) * (aFloat + 1100)
                )
            )
            dataSet.notifyDataSetChanged()
            data.notifyDataChanged()
            binding.line.notifyDataSetChanged()
            binding.line.invalidate()
            val highlight = binding.line.getHighlightByTouchPoint(
                aFloat / if (aFloat < 2300f) 12 else 10,
                300F
            )
            if (highlight != null) binding.line.highlightValue(highlight, true)
            if (aFloat.toInt() % 100 == 0) {
                var tmp: Float = binding.count.text.toString().replace(",", ".").toFloat()
                tmp += random.nextInt(30) / 100f
                binding.count.text = String.format("%.2f", tmp)
            }
        }

    }

    private val random = Random()
    private fun clear() {
        if (activity == null) return
        requireActivity().runOnUiThread { binding.count.text = "1.00" }
        binding.count.visibility = View.INVISIBLE
        binding.imageView3.visibility = View.INVISIBLE
        list.clear()
        dataSet.notifyDataSetChanged()
        data.notifyDataChanged()
        binding.line.notifyDataSetChanged()
        binding.line.invalidate()
    }


}