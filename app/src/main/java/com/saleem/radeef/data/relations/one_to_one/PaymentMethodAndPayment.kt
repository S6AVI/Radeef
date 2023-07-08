package com.saleem.radeef.data.relations.one_to_one

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.relations.Payment
import com.saleem.radeef.data.relations.PaymentMethod

data class PaymentMethodAndPayment (

    @Embedded val paymentMethod: PaymentMethod,
    @Relation (
        parentColumn = "paymentMethodID",
        entityColumn = "paymentMethodID"
            )
    val payment: Payment
        )