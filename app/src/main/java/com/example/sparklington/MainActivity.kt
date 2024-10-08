package com.example.sparklington

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sparklington.ServerCommunication.makeLoginRequest
import com.example.sparklington.UserDataHolder.accessToken
import com.example.sparklington.ui.theme.SparklingtonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SparklingtonTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainContent()
                }
            }
        }

    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainContent() {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }

    val tabs = listOf("잔디밭", "염소목장", "기부하기", "마이페이지")

    Scaffold(
        bottomBar = {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            if (isRunning) {
                                showWarningDialog = true
                            } else {
                                selectedTabIndex = index
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTabIndex) {
                0 -> TabContent1(
                    isRunningState = { isRunning = it },
                    modifier = Modifier.padding(paddingValues)
                )

                1 -> GoatTabContent(onDonateClicked = { selectedTabIndex = 2 })
                2 -> TabContent3()
                3 -> TabContent4()
            }
        }
        if (showWarningDialog) {
            AlertDialog(
                onDismissRequest = { showWarningDialog = false },
                title = { Text(text = "경고") },
                text = { Text(text = "타이머가 실행 중일 때는 다른 탭으로 이동할 수 없습니다.") },
                confirmButton = {
                    TextButton(onClick = { showWarningDialog = false }) {
                        Text("확인")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContentPreview() {
    MaterialTheme {
        MainContent()
    }
}