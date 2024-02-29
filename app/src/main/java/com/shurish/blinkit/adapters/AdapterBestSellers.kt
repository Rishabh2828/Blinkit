package com.shurish.blinkit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.shurish.blinkit.databinding.ItemViewBestsellerBinding
import com.shurish.blinkit.models.BestSeller

class AdapterBestSellers(val onSeeAllButtonCLicked: (BestSeller) -> Unit) : RecyclerView.Adapter<AdapterBestSellers.BestSellerViewHolder>() {
    class BestSellerViewHolder(val binding : ItemViewBestsellerBinding) : ViewHolder(binding.root)

    val diffUtil = object : DiffUtil.ItemCallback<BestSeller>(){
        override fun areItemsTheSame(oldItem: BestSeller, newItem: BestSeller): Boolean {
            return  oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BestSeller, newItem: BestSeller): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this,diffUtil)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BestSellerViewHolder {
        return BestSellerViewHolder(ItemViewBestsellerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return  differ.currentList.size
    }

    override fun onBindViewHolder(holder: BestSellerViewHolder, position: Int) {

        val productType =differ.currentList[position]
        holder.binding.apply {
            tvProductType.text = productType.productType
            tvTotalProducts.text= productType.products?.size.toString() + " products"

            val listOcIv = listOf(ivProduct1, ivProduct2, ivProduct3)

            val minimumsize = minOf(listOcIv.size, productType.products?.size!!)

            for (i in 0 until  minimumsize){
                listOcIv[i].visibility = View.VISIBLE
                Glide.with(holder.itemView).load(productType.products[i].productImageUris?.get(0)).into(listOcIv[i])
            }

            if (productType.products.size!!>3){
                tvProductCount.visibility = View.VISIBLE
                tvProductCount.text = "+" + (productType.products?.size!!-3).toString()
            }


        }

        holder.itemView.setOnClickListener { onSeeAllButtonCLicked(productType) }
    }


}