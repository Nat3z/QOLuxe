package com.nat3z.qoluxe.utils

import com.nat3z.qoluxe.QOLuxe
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object WebUtils {

    fun setUnsafeMode() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<X509Certificate>? {
                return null
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}
        })

        // Install the all-trusting trust manager
        try {
            val sc = SSLContext.getInstance("TLS")
            sc.init(null, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {
        }

    }

    fun fetch(urlstring: String, fetchRunnable: (FetchResponse) -> Unit) {
        try {
            val url = URL(urlstring)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "${QOLuxe.MOD_ID}-Mod/NatiaDev/" + QOLuxe.VERSION)

            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = StringBuilder()

                var line: String? = null;
                while ({ line = reader.readLine(); line }() != null) {
                    response.append(line)
                }
                reader.close()

                fetchRunnable(FetchResponse(response.toString()))
            }
        } catch (ex: IOException) {
            QOLuxe.LOGGER.error("Unable to GET data from $urlstring")
            ex.printStackTrace()
        }

    }

}
