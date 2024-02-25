package jatx.video.manager

fun String.cp1251toUTF8(): String {
    val w1251 = charset("Windows-1251")
    val utf8 = charset("UTF-8")
    return this.toByteArray(utf8).toString(w1251)
}

fun String.utf8toCP1251(): String {
    val w1251 = charset("Windows-1251")
    val utf8 = charset("UTF-8")
    return this.toByteArray(w1251).toString(utf8)
}