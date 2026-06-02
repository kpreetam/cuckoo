package com.example.cuckooclock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cuckooclock.R
import com.example.cuckooclock.views.ArtisanCuckooClockView

class ArtisanClockFragment : Fragment() {
    private var clockView: ArtisanCuckooClockView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_artisan_clock, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clockView = view.findViewById(R.id.artisanClockView)
    }

    fun tick() { if (!isAdded) return; clockView?.tick() }
    fun triggerAnimation(count: Int) {
    if (!isAdded) return

    // Each cuckoo cycle lasts ~900ms (door → out → bob → in)
    val cycleDuration = 900L

    for (i in 0 until count) {
        clockView?.postDelayed({
            clockView?.animateSingleCuckoo(i, count)
        }, i * cycleDuration)
    }
}


    override fun onDestroyView() { super.onDestroyView(); clockView = null }

    
}
