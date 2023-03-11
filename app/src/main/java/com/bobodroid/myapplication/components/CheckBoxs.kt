package com.bobodroid.myapplication.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.MainActivity
import com.bobodroid.myapplication.R

@Composable
fun InverstCheckBox(title: String,
                    id: Int,
                    selectedCheckId: Int,
                    selectCheckBoxAction: (Int) -> Unit) {


    var currentCheckBoxId : Int = id

    var changeTogglePainter = if (selectedCheckId == currentCheckBoxId) R.drawable.ic_checked else R.drawable.ic_unchecked


    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Spacer(modifier = Modifier.width(12.dp))

        Text(text = "$title", fontSize = 18.sp, modifier = Modifier.padding(2.dp))

        Spacer(modifier = Modifier.width(1.dp))

        Image(
            painter = painterResource(id = changeTogglePainter) ,
            contentDescription = null,
            modifier = Modifier
                .size(45.dp)
                .clickable {
                    selectCheckBoxAction(currentCheckBoxId)
                    Log.d(MainActivity.TAG, "체크박스가 클릭되었습니다.")
                })



    }
}