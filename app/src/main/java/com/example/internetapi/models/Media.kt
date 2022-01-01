package com.example.internetapi.models

import java.math.BigDecimal

data class MediaType(val id: Int, val name: String)
data class MediaTypeRequest(val mediaName: String)
data class MediaUsage(val id: Int, val year: Int, val month: Int, val meterRead: BigDecimal)
data class MediaRegisterRequest(val mediaType: Int, val meterRead: BigDecimal, val year: Int, val month: Int)