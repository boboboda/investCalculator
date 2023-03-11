package com.bobodroid.myapplication.models.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class SharedViewModel: ViewModel() {

    var changeMoney = MutableStateFlow(1) 
    
}