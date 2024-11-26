package com.bobodroid.myapplication.components.Dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.components.CardIconButton
import com.bobodroid.myapplication.models.viewmodels.AllViewModel

@Composable
fun NoticeDialog(
    content: String,
    onDismissRequest: () -> Unit,
    dateDelaySelected: () -> Unit,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End
            ) {
                CardIconButton(
                    imageVector = Icons.Filled.Close,
                    onClicked = { onDismissRequest.invoke() },
                    modifier = Modifier,
                    buttonColor = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 500.dp)
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = content,
                    lineHeight = 25.sp,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .height(IntrinsicSize.Max)
                        .padding(end = 10.dp)
                        .padding(top = 5.dp)
                        .padding(horizontal = 15.dp)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.End
            ) {
                BasicCheckBox {
                    onDismissRequest()
                    dateDelaySelected.invoke()
                }
            }
        }
    }
}


@Composable
fun BasicCheckBox(clicked: () -> Unit) {
    Row(
        modifier = Modifier
            .wrapContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "다시 보지 않기", fontSize = 15.sp)


        Spacer(modifier = Modifier.width(5.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_unchecked),
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clickable {
                    clicked.invoke()
                })


    }

}