package ui.theme

import androidx.compose.ui.graphics.Color

val DarkGreen = Color(16, 139, 102)
val Gray = Color.DarkGray
val LightGray = Color(100, 100, 100)
val DarkGray = Color(32, 32, 32)
val PreviewImageAreaHoverColor = Color(45, 45, 45)
val ToastBackground = Color(23, 23, 23)
val MiniatureColor = Color(50, 50, 50)
val MiniatureHoverColor = Color(55, 55, 55)
val Foreground = Color(210, 210, 210)
val TranslucentBlack = Color(0, 0, 0, 60)
val TranslucentWhite = Color(255, 255, 255, 20)
val Transparent = Color.Transparent
val LightBlue = Color(0xFFD7E8DE)
val RedOrange = Color(0xffffab91)
val RedPink = Color(0xfff48fb1)
val BabyBlue = Color(0xff81deea)
val Violet = Color(0xffcf94da)
val LightGreen = Color(0xffe7ed9b)

val colorMap: HashMap<Int, java.awt.Color> = generateAWTColorMap()

fun generateAWTColorMap(): java.util.HashMap<Int, java.awt.Color> {
    return HashMap<Int, java.awt.Color>().apply {
        this[0] = java.awt.Color.BLUE
        this[1] = java.awt.Color.RED
        this[2] = java.awt.Color.GREEN
        this[3] = java.awt.Color.YELLOW
        this[4] = java.awt.Color.CYAN
    }
}
