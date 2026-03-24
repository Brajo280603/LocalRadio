package com.brajo.localradio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.brajo.localradio.ai.YamnetAnalyzer
import com.brajo.localradio.ui.MainScreen
import com.brajo.localradio.ui.theme.LocalRadioTheme


class MainActivity : ComponentActivity(){
    override fun onCreate(savedInstantState: Bundle?){
        super.onCreate(savedInstantState)

        val analyzer = YamnetAnalyzer(this)

        setContent {
            LocalRadioTheme {
                Surface(modifier = Modifier.fillMaxSize(),color = MaterialTheme.colorScheme.background) {
//                    MusicPlayerScreen()
                    MainScreen()
                }
            }
        }
    }
}