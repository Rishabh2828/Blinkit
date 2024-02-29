package com.shurish.blinkit.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.shurish.blinkit.R
import com.shurish.blinkit.Utils
import com.shurish.blinkit.activity.AuthMainActivity
import com.shurish.blinkit.databinding.AddressBookLayoutBinding
import com.shurish.blinkit.databinding.FragmentProfileBinding
import com.shurish.blinkit.viewModels.UserViewModel

class ProfileFragment : Fragment() {

    private lateinit var binding : FragmentProfileBinding
    private val viewModel : UserViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)

        onBackButtonClicked()
        onOrdersLayoutClicked()
        onAddressBookClicked()
        onLogoutClicked()

        return binding.root



    }

    private fun onLogoutClicked() {
        binding.llLogout.setOnClickListener {


            val builder = AlertDialog.Builder(requireContext())
            val alertDialog = builder.create()
            builder.setTitle("Log Out")
                .setMessage("Your message goes here")
                .setPositiveButton("Yes"){_,_->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(), AuthMainActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_->
                    alertDialog.dismiss()

                }
                .show()
                .setCancelable(false)


        }
    }

    private fun onAddressBookClicked() {
        binding.llAddress.setOnClickListener {

            val addressBookLayoutBinding = AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress {address->
                addressBookLayoutBinding.etAddress.setText(address.toString())

            }

            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialog.show()



            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled = true
            }

            addressBookLayoutBinding.btnSave.setOnClickListener {
                   viewModel.saveAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext(), "Address Updated")
            }
        }
    }

    private fun onOrdersLayoutClicked() {
        binding.llOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)

        }
    }

    private fun onBackButtonClicked() {
        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }
}
