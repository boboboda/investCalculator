package com.bobodroid.myapplication.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.Dialogs.LoadingDialog
import com.bobodroid.myapplication.models.viewmodels.AllViewModel
import com.bobodroid.myapplication.models.viewmodels.AnalysisViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AnalysisScreen() {

    var loadDialog by remember { mutableStateOf(false) }
    var loadState by remember { mutableStateOf(false) }
    var loadResultText by remember { mutableStateOf("") }

    val analysisViewModel: AnalysisViewModel = hiltViewModel()




}

