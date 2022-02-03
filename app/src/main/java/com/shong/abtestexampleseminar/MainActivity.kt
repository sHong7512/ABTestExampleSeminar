package com.shong.abtestexampleseminar

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName + "_sHong"
    // FA 변수 선언
    private val firebaseAnalytics: FirebaseAnalytics by lazy { FirebaseAnalytics.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.downloadCancelButton).setOnClickListener {
            Toast.makeText(this@MainActivity,"Download Cancel Button Click!", Toast.LENGTH_SHORT).show()
            firebaseAnalytics.logEvent("cancel_btn_click", null)    // 버튼 클릭 시 "cancel_btn_click" Event Log
        }

        val downloadButton = findViewById<Button>(R.id.downloadButton).apply {
            setOnClickListener {
                Toast.makeText(this@MainActivity,"Download Button Click!", Toast.LENGTH_SHORT).show()
                firebaseAnalytics.logEvent("btn_click", null)       // 버튼 클릭 시 "btn_click" Event Log
            }
        }

        // FRC 갱신 시간 설정 변수 선언
        // minimumFetchIntervalInSeconds은 fetch요청은 하지만 데이터를 받아오는 최소 시간 설정
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }
        // FRC 객체적용 및 사용
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(configSettings)

            // FRC Data 요청은 fetch와 activate로 나눠 볼 수 있음

            // task.isSuccessful은 단순히 요청(통신) 성공 여부를 나타냄. fetch에서 사용
            // false : 요청(통신) 실패  true : 요청(통신) 성공
            fetch().addOnCompleteListener { task ->
                Log.d(TAG, "Only Fetch() isSuccessful ${task.isSuccessful}")
            }

            // task.result는 결과값이 갱신되었는지 확인할 수 있음. activate에서 사용
            // false : ==old Data  true : ==new Data
            activate().addOnCompleteListener { task ->
                Log.d(TAG, "Only Activate() update -> ${task.result}")
            }

            // 패치 및 활성화를 한번에 하려면 fetchAndActivate() 사용
            fetchAndActivate().addOnCompleteListener(this@MainActivity) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Config params updated: ${task.result}")

                    // 데이터에서 key값이 download_button_color인 String을 가져옴
                    val colorStr = this.getString("download_button_color")

                    // 가져온 데이터를 버튼 적용 및 텍스트로 표현
                    downloadButton.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorStr))
                    findViewById<TextView>(R.id.colorTextView).text = colorStr
                    findViewById<TextView>(R.id.colorTextView).setTextColor(Color.parseColor(colorStr))

                    Log.d(TAG,"Fetch and activate succeeded")
                }
                else {
                    findViewById<TextView>(R.id.colorTextView).text = "Fetch failed"
                    Log.d(TAG,"Fetch failed")
                }
            }
        }
    }
}