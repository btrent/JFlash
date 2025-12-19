package com.jflash.domain.model

data class FSRSParameters(
    val w: kotlin.collections.List<Double> = listOf(
        0.4072, 1.1829, 3.1262, 15.4722,
        7.2102, 0.5316, 1.0651, 0.0234,
        1.616, 0.1544, 1.0824, 1.9813,
        0.0953, 0.2975, 2.2042, 0.2407,
        2.9466, 0.5034, 0.6567
    ),
    val requestRetention: Double = 0.9,
    val maximumInterval: Int = 36500,
    val lapseBonus: Double = 1.0,
    val repBonus: Double = 1.0
)