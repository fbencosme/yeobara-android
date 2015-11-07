// Copyright 2015 Google Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.github.importre.eddystone

import android.util.Log
import android.util.SparseArray
import android.webkit.URLUtil
import java.lang.StringBuilder
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID


/**
 * Helpers for Eddystone-URL frame validation. Copied from
 * https://github.com/google/uribeacon/android-uribeacon/uribeacon-library
 */
internal object UrlUtils {
    private val TAG = UrlUtils::class.java.simpleName

    private val URI_SCHEMES = object : SparseArray<String>() {
        init {
            put(0.toByte().toInt(), "http://www.")
            put(1.toByte().toInt(), "https://www.")
            put(2.toByte().toInt(), "http://")
            put(3.toByte().toInt(), "https://")
            put(4.toByte().toInt(), "urn:uuid:")
        }
    }

    private val URL_CODES = object : SparseArray<String>() {
        init {
            put(0.toByte().toInt(), ".com/")
            put(1.toByte().toInt(), ".org/")
            put(2.toByte().toInt(), ".edu/")
            put(3.toByte().toInt(), ".net/")
            put(4.toByte().toInt(), ".info/")
            put(5.toByte().toInt(), ".biz/")
            put(6.toByte().toInt(), ".gov/")
            put(7.toByte().toInt(), ".com")
            put(8.toByte().toInt(), ".org")
            put(9.toByte().toInt(), ".edu")
            put(10.toByte().toInt(), ".net")
            put(11.toByte().toInt(), ".info")
            put(12.toByte().toInt(), ".biz")
            put(13.toByte().toInt(), ".gov")
        }
    }

    fun decodeUrl(serviceData: ByteArray): String? {
        val url = StringBuilder()
        var offset = 2
        val b = serviceData[offset++]
        val scheme = URI_SCHEMES.get(b.toInt())
        if (scheme != null) {
            url.append(scheme)
            if (URLUtil.isNetworkUrl(scheme)) {
                return decodeUrl(serviceData, offset, url)
            } else if ("urn:uuid:" == scheme) {
                return decodeUrnUuid(serviceData, offset, url)
            }
        }
        return url.toString()
    }

    fun decodeUrl(serviceData: ByteArray, offset: Int, urlBuilder: StringBuilder): String {
        var o = offset
        while (o < serviceData.size) {
            val b = serviceData[o++]
            val code = URL_CODES.get(b.toInt())
            if (code != null) {
                urlBuilder.append(code)
            } else {
                urlBuilder.append(b.toChar())
            }
        }
        return urlBuilder.toString()
    }

    fun decodeUrnUuid(serviceData: ByteArray, offset: Int, urnBuilder: StringBuilder): String? {
        val bb = ByteBuffer.wrap(serviceData)
        // UUIDs are ordered as byte array, which means most significant first
        bb.order(ByteOrder.BIG_ENDIAN)
        val mostSignificantBytes: Long
        val leastSignificantBytes: Long
        try {
            bb.position(offset)
            mostSignificantBytes = bb.long
            leastSignificantBytes = bb.long
        } catch (e: BufferUnderflowException) {
            Log.w(TAG, "decodeUrnUuid BufferUnderflowException!")
            return null
        }

        val uuid = UUID(mostSignificantBytes, leastSignificantBytes)
        urnBuilder.append(uuid.toString())
        return urnBuilder.toString()
    }

}
