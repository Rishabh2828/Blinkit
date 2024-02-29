package com.shurish.blinkit

import androidx.lifecycle.MutableLiveData

interface CartListener {

    fun showCartLayout(itemCount : Int)
    fun savingCartItemCount(itemCount : Int)
    fun hideCartLayout()


}