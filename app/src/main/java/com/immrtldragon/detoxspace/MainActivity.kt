package com.immrtldragon.detoxspace

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.immrtldragon.detoxspace.ui.DetoxSpaceApp
import com.immrtldragon.detoxspace.ui.theme.DetoxSpaceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DetoxSpaceTheme {
                DetoxSpaceApp()
            }
        }
    }
}

