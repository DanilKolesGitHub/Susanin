package com.example.navigation.screens

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.example.core.dagger.LifecycleLogger
import com.example.navigation.context.ScreenContext

abstract class ViewScreen<P: ScreenParams>(context: ScreenContext, params: P):
    Screen<P>(context, params) {
    protected val viewll = LifecycleLogger("view ${params}", Log::d)
    private var _viewLifecycle: Lifecycle? = null
    protected val viewLifecycle: Lifecycle get() { return _viewLifecycle ?: throw IllegalLifecycleException(null) }

    override fun createView(parent: ViewGroup, viewLifecycle: Lifecycle): View {
        _viewLifecycle = viewLifecycle
        _viewLifecycle!!.subscribe(viewll)
        val view = onCreateView(LayoutInflater.from(parent.context), parent)
        _viewLifecycle!!.doOnCreate { onViewCreated(view) }
        _viewLifecycle!!.doOnDestroy {
            onDestroyView()
            _viewLifecycle = null
        }
        return view
    }

    abstract fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View

    open fun onViewCreated(view: View) = Unit

    open fun onDestroyView() = Unit

}