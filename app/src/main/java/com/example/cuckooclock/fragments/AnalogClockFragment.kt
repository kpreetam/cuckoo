package com.example.cuckooclock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cuckooclock.R
import com.example.cuckooclock.views.AnalogClockView

class AnalogClockFragment : Fragment() {

    private lateinit var clockView: AnalogClockView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_analog_clock, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clockView = view.findViewById(R.id.analogClockView)
        tick()
    }

    fun tick() {
        if (!isAdded) return
        clockView.invalidate()
    }
}
