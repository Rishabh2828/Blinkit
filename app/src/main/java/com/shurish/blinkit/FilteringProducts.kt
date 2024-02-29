package com.shurish.blinkit


import com.shurish.blinkit.adapters.AdapterProduct
import com.shurish.blinkit.models.Product
import java.util.*
import java.util.logging.Filter
import kotlin.collections.ArrayList

class FilteringProducts(
    val adapter : AdapterProduct,
    val filter : ArrayList<Product>
): android.widget.Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
              val result = FilterResults()


        if (!constraint.isNullOrEmpty()){

            val filteredList = ArrayList<Product>()

            val query = constraint.toString().trim().uppercase(Locale.getDefault()).split(" ")

            for (products in filter){
                if (query.any{
                        products.productTitle?.uppercase(Locale.getDefault())?.contains(it) ==true||
                        products.productCategory?.uppercase(Locale.getDefault())?.contains(it) ==true||
                        products.productPrice?.toString()?.uppercase(Locale.getDefault())?.contains(it) ==true||
                        products.productType?.uppercase(Locale.getDefault())?.contains(it) ==true

                    })
                    filteredList.add(products)
            }

            result.values=filteredList
            result.count=filteredList.size



        }

        else{
            result.values=filter
            result.count=filter.size
        }

        return result
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapter.differ.submitList(results?.values as ArrayList<Product>)

    }
}