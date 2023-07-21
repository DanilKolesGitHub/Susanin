package com.example.navigation.pages

import com.example.navigation.navigation.DefaultNavigation
import com.example.navigation.screens.ScreenParams

internal class PagesNavigation(
    private val closeBehaviour: CloseBehaviour,
    private val backBehaviour: BackBehaviour,
): DefaultNavigation<PagesHostState>() {

    override fun open(
        screenParams: ScreenParams,
        onComplete: (newState: PagesHostState, oldState: PagesHostState) -> Unit
    ) = navigate (
        transformer = {
            if (it.pages.contains(screenParams))
                it.copy(selected = it.pages.indexOf(screenParams))
            else
                it.copy()
        },
        onComplete = onComplete
    )

    override fun close(
        screenParams: ScreenParams,
        onComplete: (newState: PagesHostState, oldState: PagesHostState) -> Unit
    ) = navigate (
        transformer = {
            if (it.pages.indexOf(screenParams) == it.selected) {
                closeBehaviour.close(it, screenParams)
            } else {
                it.copy()
            }
        },
        onComplete = onComplete
    )

    override fun back(state: PagesHostState): (() -> PagesHostState)? {
        return backBehaviour.back(state)
    }

    sealed interface CloseBehaviour {
        fun close(state: PagesHostState, screenParams: ScreenParams): PagesHostState

        object Circle : CloseBehaviour {
            override fun close(state: PagesHostState, screenParams: ScreenParams): PagesHostState {
                return if (state.selected == 0) {
                    state.copy(selected = state.pages.size - 1)
                } else {
                    state.copy(selected = state.selected - 1)
                }
            }
        }

        object ToFirst : CloseBehaviour {
            override fun close(state: PagesHostState, screenParams: ScreenParams): PagesHostState {
                return state.copy(selected = 0)
            }
        }

        object UntilFirst : CloseBehaviour {
            override fun close(state: PagesHostState, screenParams: ScreenParams): PagesHostState {
                return if (state.selected == 0) {
                    state.copy(selected = 0)
                } else {
                    state.copy(selected = state.selected - 1)
                }
            }
        }
    }

    sealed interface BackBehaviour {
        fun back(state: PagesHostState): (() -> PagesHostState)?

        object Never : BackBehaviour {
            override fun back(state: PagesHostState): (() -> PagesHostState)? = null
        }

        object Circle : BackBehaviour {
            override fun back(state: PagesHostState): (() -> PagesHostState)? {
                return {
                    CloseBehaviour.Circle.close(state, state.pages[state.selected])
                }
            }
        }

        object ToFirst : BackBehaviour {
            override fun back(state: PagesHostState): (() -> PagesHostState)? {
                if (state.selected == 0) return null
                return {
                    CloseBehaviour.ToFirst.close(state, state.pages[state.selected])
                }
            }
        }

        object UntilFirst : BackBehaviour {
            override fun back(state: PagesHostState): (() -> PagesHostState)? {
                if (state.selected == 0) return null
                return {
                    CloseBehaviour.UntilFirst.close(state, state.pages[state.selected])
                }
            }
        }
    }

}