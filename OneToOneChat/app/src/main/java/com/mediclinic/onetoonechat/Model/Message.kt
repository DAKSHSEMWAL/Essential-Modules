package com.mediclinic.onetoonechat.Model

class Message {
    // getter & setter
    lateinit var message: String
    var type: String? = null
    var time: Long = 0
    var isSeen: Boolean = false
    var from: String? = null

    // default constructor
    constructor() {}

    // constructor
    constructor(message: String, type: String, time: Long, seen: Boolean, from: String) {
        this.message = message
        this.type = type
        this.time = time
        this.isSeen = seen
        this.from = from
    }

}
