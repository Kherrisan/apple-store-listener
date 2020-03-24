package cn.kherrisan.dragonfly.telegram

import org.apache.commons.codec.binary.Hex
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

fun md5(text: String, prefix: Int = 6): String {
    return fullMD5(text).substring(0, prefix)
}

val MD5_DG = MessageDigest.getInstance("MD5")

fun fullMD5(text: String): String {
    return Hex.encodeHexString(MD5_DG.digest(text.toByteArray()))
}

fun writeFile(name: String, text: String) {
    FileUtils.write(File("${name}.html"), text, StandardCharsets.UTF_8)
}