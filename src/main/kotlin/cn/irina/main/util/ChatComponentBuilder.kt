package cn.irina.main.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*

import java.lang.reflect.Field
import java.util.regex.Pattern

class ChatComponentBuilder(text: String) : ComponentBuilder("") {
    companion object {
        private var partsField: Field? = null
        private var currField: Field? = null

        init {
            try {
                currField = ComponentBuilder::class.java.getDeclaredField("current")
                partsField = ComponentBuilder::class.java.getDeclaredField("parts")
                currField?.isAccessible = true
                partsField?.isAccessible = true
            } catch (e: NoSuchFieldException) {
                e.printStackTrace()
            }
        }
    }

    init {
        parse(text)
    }

    private val current: TextComponent?
        get() = try {
            currField?.get(this) as? TextComponent
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            null
        }

    private val parts: List<Any>?
        get() = try {
            partsField?.get(this) as? List<Any>
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            null
        }

    fun setCurrentHoverEvent(hoverEvent: HoverEvent): ChatComponentBuilder {
        current?.hoverEvent = hoverEvent
        return this
    }

    fun setCurrentClickEvent(clickEvent: ClickEvent): ChatComponentBuilder {
        current?.clickEvent = clickEvent
        return this
    }

    fun attachToEachPart(hoverEvent: HoverEvent): ChatComponentBuilder {
        parts?.forEach { part ->
            val component = part as? TextComponent
            component?.let {
                if (it.hoverEvent == null) {
                    it.hoverEvent = hoverEvent
                }
            }
        }
        current?.hoverEvent = hoverEvent
        return this
    }

    fun attachToEachPart(clickEvent: ClickEvent): ChatComponentBuilder {
        parts?.forEach { part ->
            val component = part as? TextComponent
            component?.let {
                if (it.clickEvent == null) {
                    it.clickEvent = clickEvent
                }
            }
        }
        current?.clickEvent = clickEvent
        return this
    }

    fun parse(text: String): ChatComponentBuilder {
        val regex = "[&ยง]{1}([a-fA-Fl-oL-O0-9-r]){1}"
        var modifiedText = text.replace(regex, "ยง$1")

        if (!Pattern.compile(regex).matcher(modifiedText).find()) {
            if (parts?.isEmpty() == true && current != null && current!!.text.isEmpty()) {
                current?.setText(modifiedText)
            } else {
                append(modifiedText)
            }
        } else {
            val words = modifiedText.split(regex.toRegex())
            var index = words[0].length

            for (word in words) {
                try {
                    if (index != words[0].length) {
                        if (parts?.isEmpty() == true && current != null && current!!.text.isEmpty()) {
                            current?.setText(word)
                        } else {
                            append(word)
                        }

                        val color = ChatColor.getByChar(modifiedText[index - 1])
                        when (color) {
                            ChatColor.BOLD -> bold(true)
                            ChatColor.STRIKETHROUGH -> strikethrough(true)
                            ChatColor.MAGIC -> obfuscated(true)
                            ChatColor.UNDERLINE -> underlined(true)
                            ChatColor.RESET -> {
                                bold(false)
                                strikethrough(false)
                                obfuscated(false)
                                underlined(false)
                            }
                            else -> this.color(color)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                index += word.length + 2
            }
        }
        return this
    }

    fun append(components: Array<BaseComponent>): ChatComponentBuilder {
        components.forEach { append(it as TextComponent) }
        return this
    }

    fun append(textComponent: TextComponent?): ChatComponentBuilder {
        textComponent?.let {
            val text = it.text
            val color = it.color
            val bold = it.isBold
            val underline = it.isUnderlined
            val italic = it.isItalic
            val strike = it.isStrikethrough
            val hoverEvent = it.hoverEvent
            val clickEvent = it.clickEvent

            append(text)
            color(color)
            underlined(underline)
            italic(italic)
            strikethrough(strike)
            event(hoverEvent)
            event(clickEvent)

            it.extra?.forEach { bc ->
                if (bc is TextComponent) append(bc)
            }
        }
        return this
    }

    override fun create(): Array<BaseComponent> {
        val components = mutableListOf<TextComponent>().apply {
            parts?.filterIsInstance<TextComponent>()?.let { addAll(it) }
        }

        current?.let { components.add(it) }

        val first = components.getOrNull(0)
        if (first?.text.isNullOrEmpty()) {
            components.removeAt(0)
        }

        val last = components.getOrNull(components.size - 1)
        if (last?.text.isNullOrEmpty()) {
            components.removeAt(components.size - 1)
        }

        return components.toTypedArray()
    }

}
