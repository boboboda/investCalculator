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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bobodroid.myapplication.components.Buttons
import com.bobodroid.myapplication.ui.theme.SellButtonColor

@Composable
fun ChoiceDialog(
    onDismissRequest: (Boolean) -> Unit,
    firstItem: String,
    secondItem: String,
    firstItemClicked:() -> Unit,
    secondItemClicked:() -> Unit,
) {

    Dialog(
        onDismissRequest = { onDismissRequest(false) },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(vertical = 25.dp)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Buttons(
                label = firstItem,
                onClicked = firstItemClicked,
                color =  SellButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .width(200.dp)
                    .padding(horizontal = 20.dp),
                fontSize = 15
            )

            Spacer(modifier = Modifier.height(15.dp))

            Buttons(
                label = secondItem,
                onClicked = secondItemClicked,
                color =  SellButtonColor,
                fontColor = Color.Black,
                modifier = Modifier
                    .width(200.dp)
                    .padding(horizontal = 20.dp),
                fontSize = 15
            )
        }
    }
}