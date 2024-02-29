package com.shurish.blinkit.auth

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.shurish.blinkit.R
import com.shurish.blinkit.Utils
import com.shurish.blinkit.databinding.FragmentSignInBinding


class SignInFragment : Fragment() {

    lateinit var binding: FragmentSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater)

        setStatusBarColor()

        getUserNumber()
        onContinueButtonClick()
        return binding.root
    }

    private fun onContinueButtonClick() {
        binding.btnContinue.setOnClickListener {
            val number = binding.etUserNumber.text.toString()

            if (isValidPhoneNumber(number)) {
                val bundle = Bundle()
                bundle.putString("number", number)
                findNavController().navigate(R.id.action_signInFragment_to_OTPFragment, bundle)
            } else {
                Utils.showToast(requireContext(), "Please enter valid 10-digit phone number")
            }
        }
    }

    private fun isValidPhoneNumber(number: String): Boolean {
        // Check if the number starts with "+91" and is followed by 10 digits, or if it is exactly 10 digits
        val regex = Regex("""^(?:\+91\d{10}|\d{10})$""")
        return regex.matches(number)
    }


    private fun getUserNumber() {

        binding.etUserNumber.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(number: CharSequence?, start: Int, before: Int, count: Int) {
                val regex = Regex("""^(?:\+91\d{10}|\d{10})$""")

                if (number?.let { regex.matches(number) } == true){
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green))
                }
                else{
                    binding.btnContinue.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grayishblue))

                }


            }

            override fun afterTextChanged(s: Editable?) {
            }

        })
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


}