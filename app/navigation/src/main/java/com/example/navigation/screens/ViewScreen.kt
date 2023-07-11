package com.example.navigation.screens

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.view.ViewRender

abstract class ViewScreen<P: ScreenParams>(context: ScreenContext, params: P):
    Screen<P>(context, params) {

    private var _viewLifecycle: Lifecycle? = null
    protected val viewLifecycle: Lifecycle get() { return _viewLifecycle ?: throw IllegalLifecycleException(null) }

    final override fun createView(parent: ViewGroup, viewLifecycle: Lifecycle): View {
        _viewLifecycle = viewLifecycle
        val view = onCreateView(LayoutInflater.from(parent.context), parent)
        viewLifecycle.doOnCreate { onViewCreated(view) }
        viewLifecycle.doOnDestroy {
            onDestroyView()
            _viewLifecycle = null
        }
        return view
    }

    abstract fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View

    open fun onViewCreated(view: View) = Unit

    open fun onDestroyView() = Unit

}