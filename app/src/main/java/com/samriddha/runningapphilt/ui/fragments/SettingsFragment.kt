package com.samriddha.runningapphilt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.samriddha.runningapphilt.R
import com.samriddha.runningapphilt.other.Constants.KEY_USERNAME
import com.samriddha.runningapphilt.other.Constants.KEY_USER_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment :Fragment(R.layout.fragment_settings) {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDataFromSharedPref()

        btnApplyChanges.setOnClickListener {

            val success = changeUsernameOrWeight()
            if (success){
                Snackbar.make(view,"Changes Applied!",Snackbar.LENGTH_LONG).show()

            }else{
                Snackbar.make(view,"Please enter all fields.",Snackbar.LENGTH_LONG).show()

            }

        }

    }

    private fun changeUsernameOrWeight():Boolean{

        val name = etName.text.toString()
        val wight = etWeight.text.toString()

        if(name.isEmpty()||wight.isEmpty()){
            return false
        }

        sharedPreferences.edit()
            .putString(KEY_USERNAME,name)
            .putFloat(KEY_USER_WEIGHT,wight.toFloat())
            .apply()

        val toolbarText = "Lets run $name"
        requireActivity().tvToolbarTitle.text = toolbarText

        return true
    }

    private fun loadDataFromSharedPref(){

        val name = sharedPreferences.getString(KEY_USERNAME,"")
        val weight = sharedPreferences.getFloat(KEY_USER_WEIGHT,70f)

        etName.setText(name)
        etWeight.setText(weight.toString())
    }

}