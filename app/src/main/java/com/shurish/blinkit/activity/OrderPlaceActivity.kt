package com.shurish.blinkit.activity


import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.phonepe.intent.sdk.api.B2BPGRequest
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder
import com.phonepe.intent.sdk.api.PhonePe
import com.phonepe.intent.sdk.api.PhonePeInitException
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment
import com.shurish.blinkit.CartListener
import com.shurish.blinkit.Constants
import com.shurish.blinkit.R
import com.shurish.blinkit.Utils
import com.shurish.blinkit.adapters.AdapterCartProducts
import com.shurish.blinkit.databinding.ActivityOrderPlaceBinding
import com.shurish.blinkit.databinding.AddressLayoutBinding
import com.shurish.blinkit.models.Orders
import com.shurish.blinkit.viewModels.UserViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.nio.charset.Charset
import java.security.MessageDigest


class OrderPlaceActivity : AppCompatActivity() {
    private lateinit var binding :ActivityOrderPlaceBinding
    private val viewModel : UserViewModel by viewModels()
    private lateinit var adapterCartProducts: AdapterCartProducts
    private lateinit var b2BPGRequest : B2BPGRequest
    private var cartListener : CartListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOrderPlaceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor()
        backToUserMainActivity()
        getAllCartProducts()
        initializePhonePay()
        onPlaceOrderClicked()
    }

    private fun initializePhonePay() {
        val data = JSONObject()

        PhonePe.init(this,  PhonePeEnvironment.UAT, Constants.MERCHANT_ID, "")

        data.put("merchantId", Constants.MERCHANT_ID)
        data.put("merchantTransactionId",Constants.merchantTransactionId)
        data.put("amount",200)
        data.put("mobileNumber","6203224780")
        data.put("callbackUrl","https://webhook.site/callback-url")

        val paymentInstrument = JSONObject()
        paymentInstrument.put("type", "UPI_INTENT")
        paymentInstrument.put("targetApp", "com.phonepe.simulator")
        data.put("paymentInstrument", paymentInstrument)

        val deviceContext = JSONObject()
        deviceContext.put("deviceOS", "ANDROID")
        data.put("deviceContext", deviceContext)

        val payloadBase64 = android.util.Base64.encodeToString(
            data.toString().toByteArray(Charset.defaultCharset()), android.util.Base64.NO_WRAP
        )


        val checksum = sha256(payloadBase64 + Constants.apiEndPoint + Constants.SALY_KEY) + "###1";

         b2BPGRequest = B2BPGRequestBuilder()
            .setData(payloadBase64)
            .setChecksum(checksum)
            .setUrl(Constants.apiEndPoint)
            .build()


    }

    private fun sha256(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun onPlaceOrderClicked() {
        binding.btnNext.setOnClickListener {
            viewModel.getAddressStatus().observe(this){status->
                if (status){
                    getPaymentView()
                }
                else{

                    val addressLayoutBinding = AddressLayoutBinding.inflate(LayoutInflater.from(this))

                    val alertDialog = AlertDialog.Builder(this)
                        .setView(addressLayoutBinding.root)
                        .create()
                    alertDialog.show()

                    addressLayoutBinding.btnAdd.setOnClickListener {
                        saveAddress(alertDialog, addressLayoutBinding)
                    }

                }

            }
        }
    }

    val phonePayView = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode== RESULT_OK){
            checkStatus()
        }
    }

    private fun checkStatus() {
        val xVerify = sha256("/pg/v1/status/${Constants.MERCHANT_ID}/${Constants.merchantTransactionId}${Constants.SALY_KEY}") + "###1"
        val headers = mapOf(

            "Content-Type" to "application/json",
            "X-VERIFY" to xVerify,
            "X-MERCHANT-ID" to Constants.MERCHANT_ID

        )
        lifecycleScope.launch {
            viewModel.checkPayment(headers)
            viewModel.paymentStatus.collect{status ->
                if (status){
                    Log.d("PaymentStatus", "Payment Status: $status")
                    Utils.showToast(this@OrderPlaceActivity, "Payment Successful")
                    saveOrder()

                    viewModel.deleteCartProducts()
                    viewModel.savingCartItemCount(0)
                    cartListener?.hideCartLayout()

                    Utils.hideDialog()

                    startActivity(Intent(this@OrderPlaceActivity, UsersMainActivity::class.java))
                    finish()
                }else{
                    Log.d("PaymentStatus", "Payment Status: $status")
                    Utils.showToast(this@OrderPlaceActivity, "Unsuccessful Payment")
                    Utils.hideDialog()


                }

            }

        }

    }



    private fun saveOrder() {
        viewModel.getAll().observe(this){cartProductList->

            if (cartProductList.isNotEmpty()){

                viewModel.getUserAddress {address->
                    val order = Orders(

                        orderId= Utils.getRandomId(), orderList = cartProductList,
                        userAddress = address, orderStatus = 0, orderDate = Utils.getCurrentDate(),
                        orderingUserId = Utils.getCurrentUserId()


                    )

                    viewModel.saveOrderedProduct(order)
                    lifecycleScope.launch {
                        viewModel.sendNotification(cartProductList[0].adminUid!!, "Ordered", "Some product has been ordered")
                    }

                }

                for (products in cartProductList){
                    val count = products.productCount
                    val stock = products.productStock?.minus(count!!)
                    if (stock != null) {
                        viewModel.saveProductAfterOrder(stock, products)
                    }
                }

            }





        }
    }

    private fun getPaymentView() {

        try {

            PhonePe.getImplicitIntent(this, b2BPGRequest, "com.phonepe.simulator")
                .let {
                    phonePayView.launch(it)
                }



        } catch (e : PhonePeInitException){
            Utils.showToast(this, e.message.toString())
        }
    }

    private fun saveAddress(alertDialog: AlertDialog, addressLayoutBinding: AddressLayoutBinding) {
        Utils.showDialog(this, "Processing")



        val userPincode = addressLayoutBinding.etPinCode.text.toString()
        val userPhoneNumber = addressLayoutBinding.etPhoneNumber.text.toString()
        val userState = addressLayoutBinding.etState.text.toString()
        val userDistrict = addressLayoutBinding.etDistrict.text.toString()
        val userAddress = addressLayoutBinding.etDescriptiveAddress.text.toString()

        val address = "$userPincode,$userDistrict($userState), $userAddress, $userPhoneNumber"



        lifecycleScope.launch {
            val isSuccess = viewModel.saveUserAddress(address)

            if (isSuccess) {
                Utils.showToast(this@OrderPlaceActivity, "Saved..")
                alertDialog.dismiss()
                viewModel.saveAddressStatus()
                getPaymentView()
            } else {
                Utils.showToast(this@OrderPlaceActivity, "Failed to save address.")
                Utils.hideDialog()
                alertDialog.dismiss()

            }
        }


    }

    private fun backToUserMainActivity() {
        binding.tbOrderFragment.setNavigationOnClickListener {
            startActivity(Intent(this,UsersMainActivity::class.java))
            finish()
        }
    }

    private fun getAllCartProducts() {

        viewModel.getAll().observe(this){cartProductList->
            adapterCartProducts= AdapterCartProducts()
            binding.rvProductItems.adapter=adapterCartProducts
            adapterCartProducts.differ.submitList(cartProductList)

            var totalPrice=0
            for (products in cartProductList){
                val price = products.productPrice?.substring(1)?.toInt()
                val itemCount = products.productCount!!
                totalPrice +=(price?.times(itemCount)!!)
            }

            binding.tvSubTotal.text= totalPrice.toString()

            if (totalPrice>200){
                binding.tvDeliveryCharge.text="₹15"
                totalPrice+=15
            }

            binding.tvGrandTotal.text= totalPrice.toString()
        }
    }

    private fun setStatusBarColor(){

        window?.apply {
            val statusBarColors= ContextCompat.getColor(this@OrderPlaceActivity, R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT> Build.VERSION_CODES.M){
                decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}