package com.example.cuckooclock.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cuckooclock.R
import com.example.cuckooclock.views.CuckooAnimationView

class CuckooAnimationFragment : Fragment() {

    private lateinit var animView: CuckooAnimationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_cuckoo_animation, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animView = view.findViewById(R.id.cuckooAnimView)
    }

    fun triggerAnimation(count: Int) {
        if (!isAdded) return
        animView.animateCuckoo(count)
    }
}
