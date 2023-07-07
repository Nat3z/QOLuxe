package com.nat3z.qoluxe.utils

import com.nat3z.qoluxe.QOLuxe
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.entity.mime.FileBody
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.HttpEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import java.io.BufferedReader
import java.io.File
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

    fun fetch(urlstring: String, requestMethod: String, fetchRunnable: (FetchResponse) -> Unit, failedRunnable: ((FetchResponse) -> Unit)? = null) {
        try {
            val url = URL(urlstring)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = requestMethod
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
            else {
                if (failedRunnable != null) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()

                    var line: String? = null;
                    while ({ line = reader.readLine(); line }() != null) {
                        response.append(line)
                    }
                    reader.close()

                    failedRunnable(FetchResponse(response.toString()))
                }
            }
        } catch (ex: IOException) {
            QOLuxe.LOGGER.error("Unable to GET data from $urlstring")
            if (failedRunnable != null) {
                failedRunnable(FetchResponse(""))
            }
            ex.printStackTrace()
        }

    }

    fun uploadFile(urlstring: String, file: File, storedSignature: String, fetchRunnable: (FetchResponse) -> Unit, failedRunnable: ((FetchResponse) -> Unit)? = null) {
        try {
            // using apache http client, append file to multipart entity and post to url
            val httpClient: CloseableHttpClient = HttpClients.createDefault()
            val httpPost: HttpPost = HttpPost(urlstring)
            val builder: MultipartEntityBuilder = MultipartEntityBuilder.create()
            builder.addPart("file", FileBody(file, ContentType.DEFAULT_BINARY))
            val entity: HttpEntity = builder.build()
            httpPost.setEntity(entity)
            httpPost.setHeader("Authorization", storedSignature)
            val response: CloseableHttpResponse = httpClient.execute(httpPost)
            val responseString = EntityUtils.toString(response.entity)
            httpClient.close()
            response.close()

            if (response.code == 200) {
                fetchRunnable(FetchResponse(responseString))
            }
            else {
                if (failedRunnable != null) {
                    failedRunnable(FetchResponse(responseString))
                }
            }
        } catch (ex: IOException) {
            QOLuxe.LOGGER.error("Unable to POST data to $urlstring")
            ex.printStackTrace()
        }

    }

    fun fetch(urlstring: String, fetchRunnable: (FetchResponse) -> Unit) {
        fetch(urlstring, "GET", fetchRunnable)
    }

}
