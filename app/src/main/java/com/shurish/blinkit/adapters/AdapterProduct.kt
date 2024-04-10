package com.shurish.blinkit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import com.denzcoskun.imageslider.models.SlideModel
import com.shurish.blinkit.FilteringProducts

import com.shurish.blinkit.databinding.ItemViewProductBinding

import com.shurish.blinkit.models.Product
import com.shurish.blinkit.roomdb.CartProducts


class AdapterProduct(
    val onAddButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onIncreamentButtonClicked: (Product, ItemViewProductBinding) -> Unit,
    val onDecrementButtonClicked: (Product, ItemViewProductBinding) -> Unit,
   val cartProducts: List<CartProducts>,

    ) : RecyclerView.Adapter<AdapterProduct.ProductViewHolder>(), Filterable{
    class ProductViewHolder(val binding : ItemViewProductBinding) :ViewHolder(binding.root){

    }


    val diffUtil = object :DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return  oldItem.productRandomId == newItem.productRandomId
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return  oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return  ProductViewHolder(ItemViewProductBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
      return  differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {

        val product = differ.currentList[position]

        val cartProduct = cartProducts.find { it.productId == product.productRandomId }



        holder.binding.apply {

            val imageList = ArrayList<SlideModel>()

            val productImage = product.productImageUris

            for (i in 0 until productImage?.size!!){
                imageList.add(SlideModel(product.productImageUris!![i].toString()))
            }

            ivImageSlider.setImageList(imageList)
            tvProductTitle.text= product.productTitle
            val quantity = product.productQuantity.toString()+" "+product.productUnit
            tvProductQuantity.text= quantity
            tvProductPrice.text= "₹"+product.productPrice



            if (cartProduct != null && cartProduct.productCount!! > 0) {
                tvProductCount.text = cartProduct.productCount.toString()
                tvAdd.visibility = View.GONE
                llProductCount.visibility = View.VISIBLE
            } else {
                tvAdd.visibility = View.VISIBLE
                llProductCount.visibility = View.GONE
            }

//
//            if (product.itemCount!!>0){
//                tvProductCount.text=product.itemCount.toString()
//                tvAdd.visibility= View.GONE
//                llProductCount.visibility= View.VISIBLE
//            }


            tvAdd.setOnClickListener { onAddButtonClicked(product, this) }



            tvIncrementCount.setOnClickListener {
                onIncreamentButtonClicked(product,this)
            }
            tvDecrementCount.setOnClickListener {
                onDecrementButtonClicked(product,this)
            }

        }


    }




    private val filter : FilteringProducts?= null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if (filter==null) return  FilteringProducts(this, originalList)
        return  filter
    }




}