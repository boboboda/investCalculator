package com.bobodroid.myapplication.screens

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.shadowCustom
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.bobodroid.myapplication.WebActivity
import com.bobodroid.myapplication.routes.MyPageRoute
import com.bobodroid.myapplication.routes.RouteAction

@Composable
fun CustomerScreen(
    routeAction: RouteAction<MyPageRoute>
) {

    val context = LocalContext.current

    val webIntent = Intent(context, WebActivity::class.java)
    webIntent.putExtra("url", "https://cobusil.vercel.app")

    val webPostIntent = Intent(context, WebActivity::class.java)
    webPostIntent.putExtra("url", "https://cobusil.vercel.app/release/postBoard/dollarRecord")



    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TextButton(onClick = {
            routeAction.goBack()
        }) {
            Text(text = "뒤로", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Buttons(
                onClicked = {
                    ContextCompat.startActivity(context, webIntent, null)
                },
                color = Color.White,
                fontColor = Color.Black,
                modifier = androidx.compose.ui.Modifier
                    .height(55.dp)
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 5.dp,
                        offsetY = 5.dp,
                        blurRadius = 10.dp
                    )
            ) {
                Text(
                    text = "공식사이트",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = androidx.compose.ui.Modifier.padding(0.dp))
            }



        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp)
        ) {
            Buttons(
                onClicked = {
                    ContextCompat.startActivity(context, webPostIntent, null)
                },
                color = Color.White,
                fontColor = Color.Black,
                modifier = androidx.compose.ui.Modifier
                    .height(55.dp)
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .shadowCustom(
                        color = Color.LightGray,
                        offsetX = 5.dp,
                        offsetY = 5.dp,
                        blurRadius = 10.dp
                    )
            ) {
                Text(
                    text = "문의게시판",
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = androidx.compose.ui.Modifier.padding(0.dp))
            }



        }
    }
}