package com.rubens.controletarefas.ui.theme

import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val White = Color(0xFFFFFFFF)
val DarkGray = Color(0xFF1A1A1A)
val MediumGray = Color(0xFF666666)
val LightGray = Color(0xFFF5F5F5)
val BorderGray = Color(0xFFE0E0E0)
val AccentGray = Color(0xFF333333)
val SoftWhite = Color(0xFFFAFAFA)

data class TagColors(val container: Color, val content: Color)

fun getTagColors(tag: String): TagColors {
    if (tag.isBlank()) return TagColors(Color(0xFFF5F5F5), Color(0xFF333333))
    
    return when (tag.lowercase().trim()) {
        "trabalho" -> TagColors(Color(0xFFE3F2FD), Color(0xFF0D47A1))
        "estudo" -> TagColors(Color(0xFFE8F5E9), Color(0xFF1B5E20))
        "games" -> TagColors(Color(0xFFF3E5F5), Color(0xFF4A148C))
        "lazer" -> TagColors(Color(0xFFFFFDE7), Color(0xFFF57F17))
        "almoco", "almoço" -> TagColors(Color(0xFFFFEBEE), Color(0xFFB71C1C))
        "descanso" -> TagColors(Color(0xFFE0F2F1), Color(0xFF004D40))
        "redes sociais" -> TagColors(Color(0xFFFCE4EC), Color(0xFF880E4F))
        "exercicio", "exercício" -> TagColors(Color(0xFFE8EAF6), Color(0xFF3F51B5))
        "projetos" -> TagColors(Color(0xFFE0F7FA), Color(0xFF00838F))
        "leitura" -> TagColors(Color(0xFFF1F8E9), Color(0xFF558B2F))
        "reuniao", "reunião" -> TagColors(Color(0xFFFFE0B2), Color(0xFFE65100))
        else -> {
            val hash = tag.hashCode().let { if (it < 0) -it else it }
            val presets = listOf(
                TagColors(Color(0xFFE3F2FD), Color(0xFF0D47A1)),
                TagColors(Color(0xFFE8F5E9), Color(0xFF1B5E20)),
                TagColors(Color(0xFFF3E5F5), Color(0xFF4A148C)),
                TagColors(Color(0xFFFFF3E0), Color(0xFFE65100)),
                TagColors(Color(0xFFFFEBEE), Color(0xFFB71C1C)),
                TagColors(Color(0xFFE0F2F1), Color(0xFF004D40)),
                TagColors(Color(0xFFFCE4EC), Color(0xFF880E4F)),
                TagColors(Color(0xFFEFEBE9), Color(0xFF4E342E)),
                TagColors(Color(0xFFECEFF1), Color(0xFF37474F))
            )
            presets[hash % presets.size]
        }
    }
}

