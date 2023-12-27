package com.bobodroid.myapplication.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bobodroid.myapplication.R
import com.bobodroid.myapplication.ui.theme.TopBarColor
import kotlinx.coroutines.launch

@Composable
fun MainTopBar(menuBarClinked: () -> Unit) {


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(TopBarColor)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        Image(
            modifier = Modifier
                .clip(CircleShape)
                .padding(5.dp),
            painter = painterResource(id = R.drawable.ic_icon),
            contentDescription = "")

        Spacer(modifier = Modifier.width(10.dp))
        AutoSizeText(
            value = "달러 기록",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            minFontSize = 10.sp,
            color = Color.Black)


        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            imageVector = Icons.Outlined.Settings,
            onClicked = {
                menuBarClinked.invoke()

            }, modifier = Modifier.padding(end = 10.dp))
    }
}