package com.example.internetapi.models

data class MediaType(val id: Int, val name: String)
data class MediaTypeRequest(val mediaName: String)
data class MediaUsage(val id: Int, val year: Int, val month: Int, val meterRead: Double)
data class MediaRegisterRequest(val mediaType: Int, val meterRead: Double, val year: Int, val month: Int)