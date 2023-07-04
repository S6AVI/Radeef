package com.saleem.radeef

import java.lang.Exception

sealed class MyResource<out R> {
    data class Success<out R>(val result: R): MyResource<R>()
    data class Failure<out R>(val exception: Exception): MyResource<Nothing>()
    object Loading: MyResource<Nothing>()
}
