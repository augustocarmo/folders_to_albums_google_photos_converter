package br.com.afadc.folderstoalbumsconvertergooglephotos.utils

object Log {

    private enum class Severity (val acronym: String) {
        INFORMATION ("I"),
        WARNING ("W"),
        ERROR("E")
    }

    fun i(tag: String, msg: String) {
        log(
            Severity.INFORMATION,
            tag,
            msg
        )
    }

    fun w(tag: String, msg: String) {
        log(
            Severity.WARNING,
            tag,
            msg
        )
    }

    fun e(tag: String, msg: String) {
        log(
            Severity.ERROR,
            tag,
            msg
        )
    }

    private fun log(severity: Severity, tag: String, msg: String) {
        println("Logger/${severity.acronym} $tag: $msg")
    }
}