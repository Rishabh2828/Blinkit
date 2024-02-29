package com.shurish.blinkit.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shurish.blinkit.CartListener
import com.shurish.blinkit.adapters.AdapterCartProducts
import com.shurish.blinkit.databinding.ActivityUsersMainBinding
import com.shurish.blinkit.databinding.BsCartProductsBinding
import com.shurish.blinkit.roomdb.CartProducts
import com.shurish.blinkit.viewModels.UserViewModel

class UsersMainActivity : AppCompatActivity(), CartListener {
    private lateinit var binding : ActivityUsersMainBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var cartProductList : List<CartProducts>
    private lateinit var adapterCartProducts: AdapterCartProducts

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getAllCartProducts()
        getTotalItemCount()
        onCartClicked()
        onNextButtonClicked()
    }

    private fun onNextButtonClicked() {
        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, OrderPlaceActivity::class.java))
        }
    }

    private fun getAllCartProducts() {
        viewModel.getAll().observe(this){
                cartProductList=it
        }
    }

    private fun onCartClicked() {
        binding.llItemCart.setOnClickListener {
           val bsCartProductsBinding= BsCartProductsBinding.inflate(LayoutInflater.from(this))

            val bs = BottomSheetDialog(this)
            bs.setContentView(bsCartProductsBinding.root)

            bsCartProductsBinding.tvNumberOfProductCount.text=binding.tvNumberOfProductCount.text
            adapterCartProducts= AdapterCartProducts()
            bsCartProductsBinding.rvProductsItems.adapter= adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            bsCartProductsBinding.btnNext.setOnClickListener {
                startActivity(Intent(this, OrderPlaceActivity::class.java))
            }

            bs.show()
        }
    }


    private fun getTotalItemCount() {
        viewModel.fetchTotalCartItemCount().observe(this){

           if (it>0){
               binding.llCart.visibility= View.VISIBLE
               binding.tvNumberOfProductCount.text=it.toString()

           }else{
               binding.llCart.visibility= View.GONE

           }

        }
    }

    override fun showCartLayout(itemCount : Int) {
        val previousCount = binding.tvNumberOfProductCount.text.toString().toInt()
        val updateCount = previousCount + itemCount

        if (updateCount>0){
            binding.llCart.visibility= View.VISIBLE
            binding.tvNumberOfProductCount.text= updateCount.toString()
        }
        else{
            binding.llCart.visibility= View.GONE
            binding.tvNumberOfProductCount.text= "0"
        }
    }

    override fun savingCartItemCount(itemCount: Int) {
         viewModel.fetchTotalCartItemCount().observe(this){

            viewModel.savingCartItemCount(it+itemCount)

        }

    }

    override fun hideCartLayout() {
        binding.llCart.visibility= View.GONE
        binding.tvNumberOfProductCount.text= "0"

    }

}