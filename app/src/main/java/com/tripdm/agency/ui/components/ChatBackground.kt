package com.tripdm.agency.ui.components

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tripdm.agency.R

@Composable
fun Modifier.chatTravelBackground(): Modifier {
    val bgColor = Color(0xFFFAF6F0)
    val painter = painterResource(id = R.drawable.chat_travel_bg)

    return this
        .background(bgColor)
        .drawBehind {
            val tileSize = 360.dp.toPx()
            val columns = (size.width / tileSize).toInt() + 1
            val rows = (size.height / tileSize).toInt() + 1
            for (col in 0 until columns) {
                for (row in 0 until rows) {
                    translate(left = col * tileSize, top = row * tileSize) {
                        with(painter) {
                            draw(Size(tileSize, tileSize))
                        }
                    }
                }
            }
        }
}
