package com.bobodroid.myapplication

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bobodroid.myapplication.models.viewmodels.WebViewModel
import com.bobodroid.myapplication.models.viewmodels.YenViewModel
import com.bobodroid.myapplication.screens.WebView
import com.google.android.material.bottomappbar.BottomAppBar

class WebActivity : AppCompatActivity() {

    private val webViewModel: WebViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val url = intent.getStringExtra("url") ?: ""

            WebScreen(webViewModel, url)

        }
    }
}

@Composable
fun WebScreen(webViewModel: WebViewModel, url: String) {

    Scaffold(
        bottomBar = {

            BottomAppBar( modifier = Modifier.height(50.dp),
                actions = {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { webViewModel.undo() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                            tint = Color.DarkGray
                        )
                    }
                    IconButton(onClick = { webViewModel.redo() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "forward",
                            tint = Color.DarkGray
                        )
                    }
                })
        }
    )  {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = it.calculateTopPadding(),
                    bottom = it.calculateBottomPadding()
                ),
            verticalArrangement = Arrangement.Center
        ) {
            WebView(webViewModel, url)
        }


    }
}