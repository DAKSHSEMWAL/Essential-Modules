package com.example.chatapp.data


import android.content.Context
import android.util.Log
import com.example.chatapp.model.ChatMessage
import com.example.chatapp.model.Friend
import com.example.chatapp.util.Constants
import com.example.chatapp.util.Constants.NODE_ID
import com.example.chatapp.util.Constants.NODE_IS_READ
import com.example.chatapp.util.Constants.NODE_NAME
import com.example.chatapp.util.Constants.NODE_PHOTO
import com.example.chatapp.util.Constants.NODE_RECEIVER_ID
import com.example.chatapp.util.Constants.NODE_RECEIVER_NAME
import com.example.chatapp.util.Constants.NODE_RECEIVER_PHOTO
import com.example.chatapp.util.Constants.NODE_SENDER_ID
import com.example.chatapp.util.Constants.NODE_SENDER_NAME
import com.example.chatapp.util.Constants.NODE_SENDER_PHOTO
import com.example.chatapp.util.Constants.NODE_TEXT
import com.example.chatapp.util.Constants.NODE_TIMESTAMP
import com.google.firebase.database.DataSnapshot
import java.util.*

/**
 * Created by Bibaswann on 23-06-2017.
 */

class ParseFirebaseData(context: Context?) {

    private val set: SettingsAPI = SettingsAPI(context)

    fun getAllUser(dataSnapshot: DataSnapshot): ArrayList<Friend> {
        val frnds = ArrayList<Friend>()
        var name: String? = null
        var id: String? = null
        var photo: String? = null
        for (data in dataSnapshot.children) {
            name = data.child(NODE_NAME).value!!.toString()
            id = data.child(NODE_ID).value!!.toString()
            photo = data.child(NODE_PHOTO).value!!.toString()

            if (set.readSetting(Constants.PREF_MY_ID) != id)
                frnds.add(Friend(id, name, photo))
            Log.i("Pref Id", Constants.PREF_MY_ID)
        }
        return frnds
    }

    fun getUserData(dataSnapshot: DataSnapshot) {

    }

    fun getMessagesForSingleUser(dataSnapshot: DataSnapshot): List<ChatMessage> {
        val chats = ArrayList<ChatMessage>()
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        for (data in dataSnapshot.children) {
            text = data.child(NODE_TEXT).value!!.toString()
            msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
            senderId = data.child(NODE_SENDER_ID).value!!.toString()
            senderName = data.child(NODE_SENDER_NAME).value!!.toString()
            senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
            receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
            receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
            receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
            //Node isRead is added later, may be null
            read = data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

            chats.add(ChatMessage(text, msgTime, receiverId, receiverName, receiverPhoto, senderId, senderName, senderPhoto, read))
        }
        return chats
    }

    fun getAllLastMessages(dataSnapshot: DataSnapshot): ArrayList<ChatMessage> {
        // TODO: 11/09/18 Return only last messages of every conversation current user is involved in
        val lastChats = ArrayList<ChatMessage>()
        var tempMsgList: ArrayList<ChatMessage>
        var lastTimeStamp: Long
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        for (wholeChatData in dataSnapshot.children) {

            tempMsgList = ArrayList()
            lastTimeStamp = 0

            for (data in wholeChatData.children) {
                msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
                if (java.lang.Long.parseLong(msgTime) > lastTimeStamp)
                    lastTimeStamp = java.lang.Long.parseLong(msgTime)
                text = data.child(NODE_TEXT).value!!.toString()
                senderId = data.child(NODE_SENDER_ID).value!!.toString()
                senderName = data.child(NODE_SENDER_NAME).value!!.toString()
                senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
                receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
                receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
                receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
                //Node isRead is added later, may be null
                read = data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

                tempMsgList.add(ChatMessage(text, msgTime, receiverId, receiverName, receiverPhoto, senderId, senderName, senderPhoto, read))
            }

            for (oneTemp in tempMsgList) {
                if (set.readSetting(Constants.PREF_MY_ID) == oneTemp.receiver.id || set.readSetting("myid") == oneTemp.sender.id) {
                    if (oneTemp.timestamp == lastTimeStamp.toString()) {
                        lastChats.add(oneTemp)
                    }
                }
            }
        }
        return lastChats
    }

    fun getAllUnreadReceivedMessages(dataSnapshot: DataSnapshot): ArrayList<ChatMessage> {
        val lastChats = ArrayList<ChatMessage>()
        var tempMsgList: ArrayList<ChatMessage>
        var lastTimeStamp: Long
        var text: String? = null
        var msgTime: String? = null
        var senderId: String? = null
        var senderName: String? = null
        var senderPhoto: String? = null
        var receiverId: String? = null
        var receiverName: String? = null
        var receiverPhoto: String? = null
        var read: Boolean? = java.lang.Boolean.TRUE
        for (wholeChatData in dataSnapshot.children) {

            tempMsgList = ArrayList()
            lastTimeStamp = 0

            for (data in wholeChatData.children) {
                msgTime = data.child(NODE_TIMESTAMP).value!!.toString()
                if (java.lang.Long.parseLong(msgTime) > lastTimeStamp)
                    lastTimeStamp = java.lang.Long.parseLong(msgTime)
                text = data.child(NODE_TEXT).value!!.toString()
                senderId = data.child(NODE_SENDER_ID).value!!.toString()
                senderName = data.child(NODE_SENDER_NAME).value!!.toString()
                senderPhoto = data.child(NODE_SENDER_PHOTO).value!!.toString()
                receiverId = data.child(NODE_RECEIVER_ID).value!!.toString()
                receiverName = data.child(NODE_RECEIVER_NAME).value!!.toString()
                receiverPhoto = data.child(NODE_RECEIVER_PHOTO).value!!.toString()
                //Node isRead is added later, may be null
                read = data.child(NODE_IS_READ).value == null || java.lang.Boolean.parseBoolean(data.child(NODE_IS_READ).value!!.toString())

                tempMsgList.add(ChatMessage(text, msgTime, receiverId, receiverName, receiverPhoto, senderId, senderName, senderPhoto, read))
            }

            for (oneTemp in tempMsgList) {
                if (set.readSetting(Constants.PREF_MY_ID) == oneTemp.receiver.id) {
                    if (oneTemp.timestamp == lastTimeStamp.toString() && (!oneTemp.isRead!!)!!) {
                        lastChats.add(oneTemp)
                    }
                }
            }
        }
        return lastChats
    }

    private fun encodeText(msg: String): String {
        return msg.replace(",", "#comma#").replace("{", "#braceopen#").replace("}", "#braceclose#").replace("=", "#equals#")
    }

    private fun decodeText(msg: String): String {
        return msg.replace("#comma#", ",").replace("#braceopen#", "{").replace("#braceclose#", "}").replace("#equals#", "=")
    }

}
