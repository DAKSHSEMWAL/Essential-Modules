package com.mediclinic.onetoonechat.Utils

import android.app.Application
import android.content.Context

class UserLastSeenTime : Application() {
    fun getTimeAgo(time: Long, applicationContext: Context?): String {
        var time = time
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000
        }


        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {

            return "Active just now"
            //return null;
        }

        // TODO: localize
        val diff = now - time
        return if (diff < MINUTE_MILLIS) {
            "Active few seconds ago"

        } else if (diff < 2 * MINUTE_MILLIS) {
            "Active a minute ago"

        } else if (diff < 50 * MINUTE_MILLIS) {
            "Active " + diff / MINUTE_MILLIS + " minutes ago"

        } else if (diff < 90 * MINUTE_MILLIS) {
            "Active an hour ago"

        } else if (diff < 24 * HOUR_MILLIS) {
            "Active " + diff / HOUR_MILLIS + " hours ago"

        } else if (diff < 48 * HOUR_MILLIS) {
            "Active on yesterday"

        } else {
            "Active " + diff / DAY_MILLIS + " days ago"
        }
    }

    companion object {

        private val SECOND_MILLIS = 1000
        private val MINUTE_MILLIS = 60 * SECOND_MILLIS
        private val HOUR_MILLIS = 60 * MINUTE_MILLIS
        private val DAY_MILLIS = 24 * HOUR_MILLIS

    }

}
