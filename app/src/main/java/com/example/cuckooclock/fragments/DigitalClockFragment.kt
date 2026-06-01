package com.example.cuckooclock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.cuckooclock.R
import java.text.SimpleDateFormat
import java.util.*

class DigitalClockFragment : Fragment() {

    private lateinit var tvTime: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvSeconds: TextView
    private lateinit var tvAmPm: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_digital_clock, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvTime = view.findViewById(R.id.tvTime)
        tvDate = view.findViewById(R.id.tvDate)
        tvSeconds = view.findViewById(R.id.tvSeconds)
        tvAmPm = view.findViewById(R.id.tvAmPm)
        tick()
    }

    fun tick() {
        if (!isAdded) return
        val now = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("hh:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val secFmt = SimpleDateFormat("ss", Locale.getDefault())
        val amPmFmt = SimpleDateFormat("a", Locale.getDefault())
        tvTime.text = timeFmt.format(now.time)
        tvDate.text = dateFmt.format(now.time)
        tvSeconds.text = secFmt.format(now.time)
        tvAmPm.text = amPmFmt.format(now.time)
    }
}
