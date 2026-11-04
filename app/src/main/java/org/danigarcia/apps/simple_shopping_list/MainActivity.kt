package org.danigarcia.apps.simple_shopping_list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.danigarcia.apps.simple_shopping_list.ui.MainScreen
import org.danigarcia.apps.simple_shopping_list.ui.ShoppingListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShoppingListTheme {
                MainScreen()
            }
        }
    }
}
