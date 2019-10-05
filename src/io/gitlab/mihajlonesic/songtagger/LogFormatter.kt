package io.gitlab.mihajlonesic.songtagger

import java.text.SimpleDateFormat
import java.util.Date
import java.util.logging.Formatter
import java.util.logging.LogRecord

class LogFormatter: Formatter() {

    private val ansiReset = "\u001B[0m"
    private val ansiBlack = "\u001B[30m"
    private val ansiRed = "\u001B[31m"
    private val ansiGreen = "\u001B[32m"
    private val ansiYellow = "\u001B[33m"
    private val ansiBlue = "\u001B[34m"
    private val ansiPurple = "\u001B[35m"
    private val ansiCyan = "\u001B[36m"
    private val ansiWhite = "\u001B[37m"

    override fun format(record: LogRecord): String {
        val builder = StringBuilder()

        builder.append(ansiYellow)
        builder.append("[")
        builder.append(calculateDate(record.millis))
        builder.append("]")

        builder.append(ansiCyan)
        builder.append(" [")
        builder.append(record.sourceClassName)
        builder.append("#")
        builder.append(record.sourceMethodName)
        builder.append("]")

        when {
            record.level.name == "INFO" -> builder.append(ansiGreen)
            record.level.name == "WARNING" -> builder.append(ansiRed)
            else -> builder.append(ansiPurple)
        }

        builder.append(" [")
        builder.append(record.level.name)
        builder.append("]")

        builder.append(ansiWhite)
        builder.append(" - ")
        builder.append(record.message)

        val params = record.parameters

        if (params != null) {
            builder.append("\t")
            for (i in params.indices) {
                builder.append(params[i])
                if (i < params.size - 1)
                    builder.append(", ")
            }
        }

        builder.append(ansiReset)
        builder.append("\n")

        return builder.toString()
    }

    private fun calculateDate(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
        val resultDate = Date(milliseconds)
        return dateFormat.format(resultDate)
    }
}