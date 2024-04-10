package com.shurish.blinkit.roomdb

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CartProductsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartProduct(products: CartProducts)

    @Update
    fun updateCartProduct(products: CartProducts)

    @Query("DELETE FROM CartProducts WHERE productId = :productId")
    fun deleteCartProduct(productId : String)

    @Query("SELECT * FROM CartProducts WHERE productCount > 0")
    fun getAllCartProducts(): LiveData<List<CartProducts>>


    @Query("DELETE FROM CartProducts")
   suspend fun deleteCartProducts()


}