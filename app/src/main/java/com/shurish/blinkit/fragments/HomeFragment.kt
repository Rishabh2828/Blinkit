package com.shurish.blinkit.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.shurish.blinkit.CartListener
import com.shurish.blinkit.Constants
import com.shurish.blinkit.R
import com.shurish.blinkit.Utils
import com.shurish.blinkit.adapters.AdapterBestSellers
import com.shurish.blinkit.adapters.AdapterCategory
import com.shurish.blinkit.adapters.AdapterProduct
import com.shurish.blinkit.databinding.BsSeeAllBinding
import com.shurish.blinkit.databinding.FragmentHomeBinding
import com.shurish.blinkit.databinding.ItemViewProductBinding
import com.shurish.blinkit.models.BestSeller
import com.shurish.blinkit.models.Category
import com.shurish.blinkit.models.Product
import com.shurish.blinkit.roomdb.CartProducts
import com.shurish.blinkit.viewModels.UserViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {


    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterBestSellers: AdapterBestSellers
    private lateinit var adapterProduct: AdapterProduct
    private var cartListener : CartListener? = null



    private  lateinit var  binding : FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)



        // Set up observer for cart products list LiveData
        viewModel.getAll().observe(viewLifecycleOwner) { cartProductsList ->
            adapterProduct = AdapterProduct(
                ::onAddButtonClicked,
                ::onIncreamentButtonClicked,
                ::onDecrementButtonClicked,
                cartProductsList ?: emptyList()
            )
        }


        setStatusBarColor()
        setAllCategories()
        navigatingToSearchFragment()
        onProfileClicked()
        fetchBestSellers()



        return binding.root
    }







    private fun fetchBestSellers() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {

            viewModel.fetchProductType().collect{

                adapterBestSellers = AdapterBestSellers(::onSeeAllButtonCLicked)
                binding.rvBestSellers.adapter =adapterBestSellers
                adapterBestSellers.differ.submitList(it)
                binding.shimmerViewContainer.visibility = View.GONE

            }
        }
    }

    private fun onProfileClicked() {
        binding.iVProfile.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun navigatingToSearchFragment() {
        binding.searchCv.setOnClickListener {

            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Category>()

        for (i in 0 until  Constants.allProductCategoryIcon.size){
            categoryList.add(Category(Constants.allProductCategory[i], Constants.allProductCategoryIcon[i]))
        }

        binding.rvCategories.adapter = AdapterCategory(categoryList, ::onCategoryIconClicked)
    }

    fun onCategoryIconClicked(category: Category){
        val  bundle = Bundle()
        bundle.putString("category", category.title)
       findNavController().navigate(R.id.action_homeFragment_to_categoryFragment, bundle)
    }






    fun onSeeAllButtonCLicked(productType : BestSeller){

        val bsSeeAllBinding = BsSeeAllBinding.inflate(LayoutInflater.from(requireContext()))
        val bs = BottomSheetDialog(requireContext())
        bs.setContentView(bsSeeAllBinding.root)

        // Set the adapter for the RecyclerView inside the bottom sheet
        bsSeeAllBinding.rvProducts.adapter = adapterProduct

        // Submit the list of products to the adapter
        adapterProduct.differ.submitList(productType.products)

        bs.show()


//        adapterProduct = AdapterProduct(
//            ::onAddButtonClicked,
//            ::onIncreamentButtonClicked,
//            ::onDecrementButtonClicked,
//            cartProductsList ?: emptyList(),
//
//        )
//        bsSeeAllBinding.rvProducts.adapter=adapterProduct
//        adapterProduct.differ.submitList(productType.products)
//           bs.show()
    }

    private fun onAddButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        productBinding.tvAdd.visibility= View.GONE
        productBinding.llProductCount.visibility= View.VISIBLE

        var itemCount = productBinding.tvProductCount.text.toString().toInt()
        itemCount++
        productBinding.tvProductCount.text = itemCount.toString()

        cartListener?.showCartLayout(1)

        product.itemCount = itemCount

        lifecycleScope.launch {
            cartListener?.savingCartItemCount(1)
            saveProductInRoomDb(product)
           // viewModel.updateItemCount(product,itemCount)

        }

    }



    private fun onIncreamentButtonClicked(product: Product, productBinding: ItemViewProductBinding){
        var itemCountInc = productBinding.tvProductCount.text.toString().toInt()
        itemCountInc++

        if (product.productStock!!+1>itemCountInc){
            productBinding.tvProductCount.text = itemCountInc.toString()

            cartListener?.showCartLayout(1)

            product.itemCount = itemCountInc

            lifecycleScope.launch {
                cartListener?.savingCartItemCount(1)
                saveProductInRoomDb(product)
             //   viewModel.updateItemCount(product,itemCountInc)


            }
        }else{
            Utils.showToast(requireContext(), "Can't add more item of this")
        }



    }

    fun onDecrementButtonClicked(product: Product, productBinding: ItemViewProductBinding){

        var itemCountDec = productBinding.tvProductCount.text.toString().toInt()
        itemCountDec--

        product.itemCount = itemCountDec

        lifecycleScope.launch {
            cartListener?.savingCartItemCount(-1)
            saveProductInRoomDb(product)
           // viewModel.updateItemCount(product,itemCountDec)


        }

        if (itemCountDec>0){
            productBinding.tvProductCount.text = itemCountDec.toString()

        }else{
            lifecycleScope.launch { viewModel.deleteCartProduct(product.productRandomId!!) }
            productBinding.tvAdd.visibility= View.VISIBLE
            productBinding.llProductCount.visibility= View.GONE
            productBinding.tvProductCount.text = "0"

        }

        cartListener?.showCartLayout(-1)





    }

    private fun saveProductInRoomDb(product: Product) {

        val cartProduct = CartProducts(
            productId = product.productRandomId!!,
            productTitle = product.productTitle,
            productQuantity = product.productQuantity.toString() + product.productUnit.toString(),
            productPrice = "₹" + "${product.productPrice}",
            productCount = product.itemCount,
            productStock = product.productStock,
            productImage = product.productImageUris?.get(0)!!,
            productCategory = product.productCategory,
            adminUid = product.adminUid,
            productType = product.productType



        )

        lifecycleScope.launch {
            viewModel.insertCartProduct(cartProduct)
        }

    }

    private fun setStatusBarColor(){

        activity?.window?.apply {
            val statusBarColors= ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT> Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is CartListener){
            cartListener = context
        }else{

            throw ClassCastException("Please implement cart listener")
        }
    }


    }
