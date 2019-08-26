package me.antonio.noack.productivity.tasks

import java.lang.RuntimeException
import java.net.URL
import java.net.URLEncoder

object WebAPI {

    private fun encode(value: String): String {
        return URLEncoder.encode(value)
    }

    fun getText(site: String): String = getText(URL("https://api.phychi.com/$site"))

    fun getText(url: URL): String {
        val con = url.openConnection()
        val input = con.getInputStream()
        val bytes = input.readBytes()
        input.close()
        // println(bytes.joinToString(" "))
        val text = String(bytes)
        if(text.startsWith("#")){
            throw RuntimeException(text)
        } else {
            return text
        }
    }

}