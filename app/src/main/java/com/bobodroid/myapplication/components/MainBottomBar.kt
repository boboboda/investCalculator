// =====================================================================
// MainBottomBar.kt — 전체 교체
// 변경 내용:
//  - isTravelModeEnabled 파라미터 추가
//  - "기록" 탭: 여행모드면 라벨을 "여행"으로 표시 (라우트/아이콘은 그대로, 라벨 텍스트만 교체)
//  - "알람" 탭: 여행모드면 터치 막힘(enabled = false) + 회색 음영 처리
// =====================================================================

package com.bobodroid.myapplication.components

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import com.bobodroid.myapplication.MainActivity.Companion.TAG
import com.bobodroid.myapplication.models.datamodels.service.BackupApi.BackupNoticeStatus
import com.bobodroid.myapplication.models.viewmodels.MainViewModel
import com.bobodroid.myapplication.routes.MainRoute
import com.bobodroid.myapplication.routes.RouteAction
import com.bobodroid.myapplication.ui.theme.TopButtonColor
import com.bobodroid.myapplication.ui.theme.TopButtonInColor
import kotlinx.coroutines.launch



@Composable
fun MainBottomBar(
    mainRouteAction: RouteAction<MainRoute>,
    mainRouteBackStack: NavBackStackEntry?,
    mainViewModel: MainViewModel,
    isTravelModeEnabled: Boolean = false   // ✅ 여행모드 추가
) {

    val mainBottomSelectedValue = remember { mutableStateOf(1) }

    // 백업이 되고 있지 않으면 마이페이지 탭에 빨간 점 표시
    val backupNoticeStatus = mainViewModel.backupNoticeStatus.collectAsState().value

    BottomNavigation(
        modifier = Modifier.fillMaxWidth()
    ) {
        MainRoute.Main.let {
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                label = {
                    // ✅ 여행모드면 "여행"으로 라벨만 교체 (라우트/아이콘은 그대로)
                    Text(text = if (isTravelModeEnabled) "여행" else it.title!!)
                },
                icon = {
                    it.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = it.title)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route) == it.routeName,
                onClick = {
                    mainRouteAction.navTo(it)
                    mainBottomSelectedValue.value = it.selectValue!!
                },
            )
        }

        MainRoute.Alert.let {
            // ✅ 여행모드면 터치 막힘 + 회색 음영 처리 (환율 알림은 투자 전용 기능)
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                enabled = !isTravelModeEnabled,
                label = { Text(text = it.title!!) },
                icon = {
                    it.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = it.title)
                    }
                },
                selectedContentColor = if (isTravelModeEnabled) Color(0xFFBDBDBD) else Color.Black,
                unselectedContentColor = Color(0xFFBDBDBD),
                selected = (mainRouteBackStack?.destination?.route) == it.routeName,
                onClick = {
                    if (!isTravelModeEnabled) {
                        mainRouteAction.navTo(it)
                        mainBottomSelectedValue.value = it.selectValue!!
                    }
                }

            )
        }

        MainRoute.AnalysisScreen.let { parentRoute ->
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                label = { Text(text = parentRoute.title!!) },
                icon = {
                    parentRoute.iconResId?.let { iconId ->
                        Icon(painter = painterResource(iconId), contentDescription = parentRoute.title)
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route == parentRoute.routeName ||
                        mainRouteBackStack?.destination?.route in parentRoute.subRoutes),
                onClick = {
                    mainRouteAction.navTo(parentRoute)
                    mainBottomSelectedValue.value = parentRoute.selectValue!!

//                    returnContent()

                }
            )
        }


        MainRoute.MyPage.let { parentRoute ->
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                label = { Text(text = parentRoute.title!!) },
                icon = {
                    Box {
                        parentRoute.iconResId?.let { iconId ->
                            Icon(painter = painterResource(iconId), contentDescription = parentRoute.title)
                        }
                        if (backupNoticeStatus != BackupNoticeStatus.NONE) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                        }
                    }
                },
                selectedContentColor = Color.Black,
                unselectedContentColor = Color.LightGray,
                selected = (mainRouteBackStack?.destination?.route == parentRoute.routeName ||
                        mainRouteBackStack?.destination?.route in parentRoute.subRoutes),
                onClick = {
                    mainRouteAction.navTo(parentRoute)
                    mainBottomSelectedValue.value = parentRoute.selectValue!!
                }
            )
        }

    }
}
