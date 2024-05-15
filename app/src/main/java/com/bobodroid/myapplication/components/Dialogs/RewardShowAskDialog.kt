package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.AutoSizeText
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.components.CardIconButton
import com.bobodroid.myapplication.ui.theme.SellButtonColor

@Composable
fun RewardShowAskDialog(
    onDismissRequest: (Boolean) -> Unit,
    onClicked: () -> Unit
) {

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(20.dp))
                .background(Color.White)
                .wrapContentHeight()
                .fillMaxWidth(1f)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 500.dp)
                    .wrapContentHeight()
                    .padding(bottom = 30.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {

                AutoSizeText(
                    value = "저의 앱은 무료로 제공되고 있습니다 그리하여 \n" +
                            "광고 시청은 개발자에게 큰 힘이 됩니다.\n " +
                            "광고를 시청하시겠습니까?",
                    fontSize = 16.sp,
                    minFontSize = 15.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Start,
                    lineHeight = 25,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .padding(top = 30.dp)
                        .padding(horizontal = 15.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier =
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Buttons(label = "예",
                        onClicked = onClicked,

                        color = SellButtonColor,
                        fontColor = Color.White,
                        modifier = Modifier
                            .height(40.dp)
                            .width(80.dp),
                        fontSize = 15)
                    Spacer(modifier = Modifier.width(25.dp))

                    Buttons(label = "아니요",
                        onClicked = {onDismissRequest(false)},
                        color = SellButtonColor,
                        fontColor = Color.White,
                        modifier = Modifier
                            .height(40.dp)
                            .wrapContentSize()
                        , fontSize = 15)
                }
            }
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .heightIn(min = 40.dp)
//                    .wrapContentHeight(),
//                horizontalArrangement = Arrangement.End
//            ) {
//                BasicCheckBox {
//                    onDismissRequest(false)
//                    dateDelaySelected.invoke()
//                }
//            }
        }
    }
}