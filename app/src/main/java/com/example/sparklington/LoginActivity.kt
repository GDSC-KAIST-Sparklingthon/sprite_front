package com.example.sparklington


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.sparklington.ServerCommunication.Constants
import com.example.sparklington.ServerCommunication.Constants.TAG
import com.example.sparklington.ServerCommunication.makeLoginRequest
import com.example.sparklington.ui.theme.KakaoYellow
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.user.UserApiClient

class LoginActivity : ComponentActivity() {
    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(Constants.TAG, "로그인 실패 $error")
        } else if (token != null) {
            UserDataHolder.accessToken = token.accessToken
            Log.d(Constants.TAG, "로그인 성공 ${token.accessToken}")
            nextMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "keyhash : ${Utility.getKeyHash(this)}")
        KakaoSdk.init(this, Constants.APP_KEY)

        // Check if user is already logged in
        if (AuthApiClient.instance.hasToken()) {
            val token = AuthApiClient.instance.tokenManagerProvider.manager.getToken()
            UserDataHolder.accessToken = token?.accessToken
            Log.d("KAKAO TOKEN", token.toString())
            UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
                if (error == null) {
                    nextMainActivity()
                }
            }
        }
        setContent {
            MaterialTheme {
                LoginScreen(
                    onLoginClick = { kakaoTalkLogin() }
                )
            }
        }
    }
    private fun kakaoTalkLogin(){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "talk 로그인 실패 $error")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        Log.e(Constants.TAG, "talk client 로그인 실패 $error")
                        return@loginWithKakaoTalk
                    } else {
                        Log.e(Constants.TAG, "talk 로그인 실패, client 아님 $error")
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                    }
                } else if (token != null) {
                    UserDataHolder.accessToken = token.accessToken
                    Log.d(Constants.TAG, "talk 로그인 성공 ${token}")
                    nextMainActivity()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this){ token, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "account 로그인 실패 $error")
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        Log.e(Constants.TAG, "account client 로그인 실패 $error")
                        return@loginWithKakaoAccount
                    } else {
                        Log.e(Constants.TAG, "account 로그인 실패, client 아님 $error")
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = mCallback)
                    }
                } else if (token != null) {
                    UserDataHolder.accessToken = token.accessToken
                    Log.d(Constants.TAG, "account 로그인 성공 ${token}")
                    nextMainActivity()
                }
            }
        }
    }
    private fun fetchKakaoTalkProfile() {
        Log.d("FETCH PROFILE", "START FETCHING")
        TalkApiClient.instance.profile { profile, error ->
            if (error != null) {
                Log.e(Constants.TAG, "카카오톡 프로필 가져오기 실패", error)
            } else if (profile != null) {
                UserDataHolder.nickname = profile.nickname
                Toast.makeText(this, "${profile.nickname}님 로그인 성공!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun nextMainActivity() {
        fetchKakaoTalkProfile()
        if (UserDataHolder.accessToken != null){
            Log.d("nextMainActivity", "Hello")
            makeLoginRequest(UserDataHolder.accessToken!!)
        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.balibali),
                contentDescription = "logo",
                modifier = Modifier
                    .size(200.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.kakao_login), // Replace with your image resource
                contentDescription = "Kakao Login",
                modifier = Modifier
                    .size(width = 250.dp, height = 70.dp) // Set the size of the image
                    .clickable(onClick = {
                        // Handle the click event here
                        onLoginClick()
                    })
            )
        }
    }
}