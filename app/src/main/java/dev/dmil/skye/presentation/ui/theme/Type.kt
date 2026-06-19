package dev.dmil.skye.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import dev.dmil.skye.R

val pliantFamily = FontFamily(
    Font(R.font.pliant_regular, FontWeight.Normal),
    Font(R.font.pliant_light, FontWeight.Light)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = pliantFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)