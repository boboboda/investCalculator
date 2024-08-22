package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.ui.theme.SellButtonColor
import kotlinx.coroutines.delay

@Composable
fun LoadingDialog(
    onDismissRequest: (Boolean) -> Unit,
    loadState: Boolean,
    guideText: String
) {

    val title = "안내"
    var guide = guideText ?: ""

    Dialog(
        onDismissRequest = {
            onDismissRequest.invoke(false)
        },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(top = 20.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if(!loadState) {

                CircularProgressIndicator(
                    modifier = Modifier.width(50.dp),
                    color = Color.Black,
                    trackColor = Color.LightGray,
                )

                Spacer(modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(0.9f))

                Text("Data Loading..",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 5.dp))



            } else {

                Text(text = "안내",
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 5.dp))

                Text(guide,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 5.dp))

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Buttons(
                        onClicked = {onDismissRequest(false)},

                        color = SellButtonColor,
                        fontColor = Color.White,
                        modifier = Modifier
                            .height(40.dp)
                            .width(80.dp)) {
                        Text(text = "확인", fontSize = 15.sp)
                    }
                }

            }



        }
    }
}