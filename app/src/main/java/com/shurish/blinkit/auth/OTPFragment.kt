package com.shurish.blinkit.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.shurish.blinkit.R
import com.shurish.blinkit.Utils
import com.shurish.blinkit.activity.UsersMainActivity
import com.shurish.blinkit.databinding.FragmentOTPBinding
import com.shurish.blinkit.models.Users
import com.shurish.blinkit.viewModels.AuthViewModel
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {
    private  val viewModel : AuthViewModel by viewModels()
    lateinit var binding : FragmentOTPBinding
    lateinit var userNumber : String
    lateinit var sendUserNumber : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOTPBinding.inflate(inflater)




        checkUserNumber()
        getUserNumber()
        sendOTP()
        customizingEnteringOtp()
        onLoginButtonClicked()
        onBackButtonClicked()


        return binding.root
    }

    private fun onLoginButtonClicked() {
        binding.btnLogin.setOnClickListener {
            val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
            val otp = editTexts.joinToString(""){it.text.toString()}

            if (otp.length<editTexts.size){
                Utils.showToast(requireContext(), "Please enter right otp")
            }
            else{
                editTexts.forEach{it.text?.clear(); it.clearFocus()}
                Utils.showDialog(requireContext(), "Signing you..")
                verifyOtp(otp)
            }
        }
    }

    private fun verifyOtp(otp: String) {

        val user  = Users(uid = null, userPhoneNumber = sendUserNumber, userAddress = " ")

        viewModel.signInWithPhoneAuthCredential(otp, sendUserNumber, user)

        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect(){
                if (it==true){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Logged in...")
                    viewModel._isSignedInSuccessfully.value= null

                    startActivity(Intent(requireActivity(), UsersMainActivity::class.java))
                    requireActivity().finish()
                }else if (it==false){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Wrong otp")
                    viewModel._isSignedInSuccessfully.value= null

                }
            }
        }
    }

    private fun checkUserNumber() {
        val regex = Regex("""^\+91\d{10}$""")
        val bundle = arguments
        sendUserNumber = bundle?.getString("number").toString()

        if (regex.matches(sendUserNumber)) {
            sendUserNumber = sendUserNumber.substring(3)
        } else {
            sendUserNumber = sendUserNumber
        }    }

    private fun sendOTP() {
        Utils.showDialog(requireContext(), "Sending OTP...")

        viewModel.apply {





            viewModel.sendOTP(sendUserNumber, requireActivity())

            lifecycleScope.launch {
                otpSent.collect{
                    if (it){
                        Utils.hideDialog()
                        Utils.showToast(requireContext(),"Otp Sent Successfully")
                    }
                }
            }
        }

    }

    private fun onBackButtonClicked() {
        binding.tbOtpFragment.setNavigationOnClickListener {

            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizingEnteringOtp() {

        val editTexts = arrayOf(binding.etOtp1,binding.etOtp2,binding.etOtp3,binding.etOtp4,binding.etOtp5,binding.etOtp6)
        for (i in editTexts.indices){

            editTexts[i].addTextChangedListener(object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                  if (s?.length==1){
                      if (i<editTexts.size-1)
                          editTexts[i+1].requestFocus()
                  }else if (s?.length==0){
                      if(i>0){
                          editTexts[i-1].requestFocus()
                      }
                  }
                }

            })
        }
    }

    private fun getUserNumber() {

        val regex = Regex("""^\+91\d{10}$""")
        val bundle = arguments
        userNumber = bundle?.getString("number").toString()

        if (regex.matches(userNumber)){
            binding.countryCode.visibility = View.GONE
            binding.tvUserNumber.text = userNumber

        }else{
            binding.countryCode.visibility = View.VISIBLE
            binding.tvUserNumber.text = userNumber


        }
    }


}