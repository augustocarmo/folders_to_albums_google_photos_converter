package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

import java.util.regex.Pattern

class EmailValidator {

    fun isValid(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    companion object {
        private val EMAIL_PATTERN = Pattern.compile("^(.+)@(.+)$")
    }
}