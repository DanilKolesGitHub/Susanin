package com.example.navigation.view

import android.view.View
import android.view.ViewGroup
import com.arkivanov.essenty.lifecycle.Lifecycle

interface ViewRender {
    /**
     * Создает дочернюю View для HostView.
     * При создании view не прикрепляйте ее к родителю.
     * inflater.inflate(_, _, attachToRoot = false)
     *
     * @param parent HostView родитель-контейнер, в котором будет размещена созданная View.
     * @param viewLifecycle жизненный цикл для создаваемой view. Передается в состоянии INITIALIZED. Привязана к жц HostView.
     */
    fun createView(parent: ViewGroup, viewLifecycle: Lifecycle): View
}