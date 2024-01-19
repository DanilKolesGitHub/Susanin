package com.example.susanin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import com.arkivanov.essenty.lifecycle.subscribe
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TestScreen(context: ScreenContext, type: TestScreenParams): ViewScreen<TestScreenParams>(context, type) {

    var ltext: TextView? = null
    var vltext: TextView? = null
    var controls: View? = null
    var overlay: Boolean = true
    var animate: Boolean = true
    var scope: CoroutineScope? = null

    fun nextParams() = TestScreenParams(
        id = params.id + 1,
        overlay = overlay,
        animate = animate,
    )

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.test_screen, parent, false)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        ltext = view.findViewById<TextView>(R.id.ltext)
        vltext = view.findViewById<TextView>(R.id.vltext)
        controls = view.findViewById<View>(R.id.controls)
        view.findViewById<TextView>(R.id.idtext).text = params.id.toString()

        val overlayGroup = view.findViewById<RadioGroup>(R.id.overlayGroup)
        overlayGroup.setOnCheckedChangeListener { group, checkedId ->
            overlay = checkedId == R.id.overlay_true
            Log.d("TESSTDEB", "overlay = $overlay")
        }

        val animGroup = view.findViewById<RadioGroup>(R.id.animateGroup)
        animGroup.setOnCheckedChangeListener { group, checkedId ->
            animate = checkedId == R.id.animate_true
            Log.d("TESSTDEB", "animate = $animate")
        }

        view.findViewById<Button>(R.id.next_button).setOnClickListener {
            transaction { open(nextParams()) }
        }

        controls?.visibility = View.GONE
        viewLifecycle.subscribe(
            onResume = { controls?.visibility = View.VISIBLE },
            onPause = { controls?.visibility = View.GONE },
            onDestroy = {
                controls = null
            }
        )
        scope = CoroutineScope(Dispatchers.Main)
        scope?.launch {
            ll.state.collectLatest { ltext?.text = it }
        }
        scope?.launch {
            vll.state.collectLatest { vltext?.text = it }
        }
    }

    override fun onDestroyView() {
        scope?.cancel()
        ltext?.text = "onDestroy"
        vltext?.text = "onDestroy"
        scope = null
        ltext = null
        vltext = null
        controls = null
    }
}