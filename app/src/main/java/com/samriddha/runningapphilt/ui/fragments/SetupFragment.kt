package com.samriddha.runningapphilt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.samriddha.runningapphilt.other.Constants.KEY_USERNAME
import com.samriddha.runningapphilt.other.Constants.KEY_USER_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment :Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @set:Inject
    var isFirstTimeOpened = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isFirstTimeOpened)
        {
            /*we don't want to save SetupFragment to back stack.If this fragment is put inside back stack then
            * when user press back button inside run fragment he will navigate to SetupFragment again which we don't want.*/
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setupFragment2,true)
                .build()

            findNavController().navigate(R.id.action_setupFragment2_to_runFragment,
            savedInstanceState,
            navOptions)
        }


        tvContinue.setOnClickListener {

            val success = writeUserDataToSharedPref()

            if (success)
                findNavController().navigate(R.id.action_setupFragment2_to_runFragment)
            else{
                Snackbar.make(requireView(),"Please Enter All the Fields To Continue.",Snackbar.LENGTH_LONG).show()
            }

        }
    }

    private fun writeUserDataToSharedPref():Boolean{

        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if (name.isEmpty()||weight.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_USERNAME,name)
            .putFloat(KEY_USER_WEIGHT,weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE,false)
            .apply()

        val toolBarText = "Lets Run,$name"
        requireActivity().tvToolbarTitle.text =  toolBarText

        return true
    }

}