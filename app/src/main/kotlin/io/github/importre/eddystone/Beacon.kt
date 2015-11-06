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

public class Beacon internal constructor(val deviceAddress: String, var rssi: Int) {
    // TODO: rename to make explicit the validation intent of this timestamp. We use it to
    // remember a recent frame to make sure that non-monotonic TLM values increase.
    internal var timestamp = System.currentTimeMillis()

    // Used to remove devices from the listview when they haven't been seen in a while.
    internal var lastSeenTimestamp = System.currentTimeMillis()

    internal var uidServiceData: ByteArray? = null
    internal var tlmServiceData: ByteArray? = null
    internal var urlServiceData: ByteArray? = null

    inner class UidStatus {
        internal var uidValue: String? = null
        internal var txPower: Int = 0

        internal var errTx: String? = null
        internal var errUid: String? = null
        internal var errRfu: String? = null

        val errors: String
            get() {
                val sb = StringBuilder()
                if (errTx != null) {
                    sb.append(BULLET).append(errTx).append("\n")
                }
                if (errUid != null) {
                    sb.append(BULLET).append(errUid).append("\n")
                }
                if (errRfu != null) {
                    sb.append(BULLET).append(errRfu).append("\n")
                }
                return sb.toString().trim { it <= ' ' }
            }
    }

    inner class TlmStatus {
        internal var version: String = ""
        internal var voltage: String = ""
        internal var temp: String = ""
        internal var advCnt: String = ""
        internal var secCnt: String = ""

        internal var errIdentialFrame: String? = null
        internal var errVersion: String? = null
        internal var errVoltage: String? = null
        internal var errTemp: String? = null
        internal var errPduCnt: String? = null
        internal var errSecCnt: String? = null
        internal var errRfu: String? = null

        val errors: String
            get() {
                val sb = StringBuilder()
                if (errIdentialFrame != null) {
                    sb.append(BULLET).append(errIdentialFrame).append("\n")
                }
                if (errVersion != null) {
                    sb.append(BULLET).append(errVersion).append("\n")
                }
                if (errVoltage != null) {
                    sb.append(BULLET).append(errVoltage).append("\n")
                }
                if (errTemp != null) {
                    sb.append(BULLET).append(errTemp).append("\n")
                }
                if (errPduCnt != null) {
                    sb.append(BULLET).append(errPduCnt).append("\n")
                }
                if (errSecCnt != null) {
                    sb.append(BULLET).append(errSecCnt).append("\n")
                }
                if (errRfu != null) {
                    sb.append(BULLET).append(errRfu).append("\n")
                }
                return sb.toString().trim { it <= ' ' }
            }

        override fun toString(): String {
            return errors
        }
    }

    inner class UrlStatus {
        internal var urlValue: String? = null
        internal var urlNotSet: String? = null
        internal var txPower: String? = null

        val errors: String
            get() {
                val sb = StringBuilder()
                if (txPower != null) {
                    sb.append(BULLET).append(txPower).append("\n")
                }
                if (urlNotSet != null) {
                    sb.append(BULLET).append(urlNotSet).append("\n")
                }
                return sb.toString().trim { it <= ' ' }
            }

        override fun toString(): String {
            val sb = StringBuilder()
            if (urlValue != null) {
                sb.append(urlValue).append("\n")
            }
            return sb.append(errors).toString().trim { it <= ' ' }
        }

        fun url(): String {
            return toString()
        }
    }

    inner class FrameStatus {
        var nullServiceData: String? = null
        var tooShortServiceData: String? = null
        var invalidFrameType: String? = null

        val errors: String
            get() {
                val sb = StringBuilder()
                if (nullServiceData != null) {
                    sb.append(BULLET).append(nullServiceData).append("\n")
                }
                if (tooShortServiceData != null) {
                    sb.append(BULLET).append(tooShortServiceData).append("\n")
                }
                if (invalidFrameType != null) {
                    sb.append(BULLET).append(invalidFrameType).append("\n")
                }
                return sb.toString().trim { it <= ' ' }
            }

        override fun toString(): String {
            return errors
        }
    }

    internal var hasUidFrame: Boolean = false
    var uidStatus = UidStatus()

    internal var hasTlmFrame: Boolean = false
    var tlmStatus = TlmStatus()

    internal var hasUrlFrame: Boolean = false
    var urlStatus = UrlStatus()

    var frameStatus = FrameStatus()

    companion object {
        private val BULLET = "● "
    }
}
