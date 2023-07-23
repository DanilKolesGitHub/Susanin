package com.example.navigation.pages

import android.os.Parcelable
import com.example.navigation.navigation.DefaultNavigation

internal class PagesNavigation<P: Parcelable>(
    private val closeBehaviour: CloseBehaviour,
    private val backBehaviour: BackBehaviour,
): DefaultNavigation<P, PagesHostState<P>>() {

    override fun open(
        params: P,
        onComplete: (newState: PagesHostState<P>, oldState: PagesHostState<P>) -> Unit
    ) = navigate (
        transformer = {
            if (it.pages.contains(params))
                it.copy(selected = it.pages.indexOf(params))
            else
                it.copy()
        },
        onComplete = onComplete
    )

    override fun close(
        params: P,
        onComplete: (newState: PagesHostState<P>, oldState: PagesHostState<P>) -> Unit
    ) = navigate (
        transformer = {
            if (it.pages.indexOf(params) == it.selected) {
                closeBehaviour.close(it)
            } else {
                it.copy()
            }
        },
        onComplete = onComplete
    )

    override fun back(state: PagesHostState<P>): (() -> PagesHostState<P>)? {
        return backBehaviour.back(state)
    }

    sealed interface CloseBehaviour {
        fun <P: Parcelable> close(state: PagesHostState<P>): PagesHostState<P>

        object Circle : CloseBehaviour {

            override fun <P : Parcelable> close(state: PagesHostState<P>): PagesHostState<P> {
                return if (state.selected == 0) {
                    state.copy(selected = state.pages.size - 1)
                } else {
                    state.copy(selected = state.selected - 1)
                }
            }
        }

        object ToFirst : CloseBehaviour {

            override fun <P : Parcelable> close(state: PagesHostState<P>): PagesHostState<P> {
                return state.copy(selected = 0)
            }
        }

        object UntilFirst : CloseBehaviour {

            override fun <P : Parcelable> close(state: PagesHostState<P>): PagesHostState<P> {
                return if (state.selected == 0) {
                    state.copy(selected = 0)
                } else {
                    state.copy(selected = state.selected - 1)
                }
            }
        }
    }

    sealed interface BackBehaviour {
        fun  <P : Parcelable> back(state: PagesHostState<P>): (() -> PagesHostState<P>)?

        object Circle : BackBehaviour {

            override fun <P : Parcelable> back(state: PagesHostState<P>): (() -> PagesHostState<P>)? {
                return {
                    CloseBehaviour.Circle.close(state)
                }
            }
        }

        object ToFirst : BackBehaviour {

            override fun <P : Parcelable> back(state: PagesHostState<P>): (() -> PagesHostState<P>)? {
                if (state.selected == 0) return null
                return {
                    CloseBehaviour.ToFirst.close(state)
                }
            }
        }

        object UntilFirst : BackBehaviour {

            override fun <P : Parcelable> back(state: PagesHostState<P>): (() -> PagesHostState<P>)? {
                if (state.selected == 0) return null
                return {
                    CloseBehaviour.UntilFirst.close(state)
                }
            }
        }
    }

}