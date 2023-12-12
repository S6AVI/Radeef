package com.saleem.radeef.util

sealed class HomeEvent {
    data class UpdateResult(val state: UiState<Boolean>) : HomeEvent()
    data class CallPassenger(val phoneNumber: String) : HomeEvent()
}