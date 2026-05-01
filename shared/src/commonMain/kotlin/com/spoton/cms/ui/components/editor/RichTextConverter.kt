package com.spoton.cms.ui.components.editor

import com.mohamedrejeb.richeditor.model.RichTextState

/**
 * Interface for converting between internal RichTextState and external formats.
 * This allows us to support HTML now and Gutenberg blocks later.
 */
interface RichTextConverter {
    fun toOutput(state: RichTextState): String
    fun fromInput(input: String, state: RichTextState)
}

/**
 * Default converter for standard HTML content (WordPress descriptions/articles).
 */
class HtmlRichTextConverter : RichTextConverter {
    override fun toOutput(state: RichTextState): String {
        return state.toHtml()
    }

    override fun fromInput(input: String, state: RichTextState) {
        state.setHtml(input)
    }
}
