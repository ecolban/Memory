package org.jointheleague.memory.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt
import java.lang.System.currentTimeMillis as now

@ExperimentalCoroutinesApi
fun CoroutineScope.primeChannel(): ReceiveChannel<Int> = produce {
    val primesSoFar = mutableListOf(2)
    send(2)
    var p = 3
    while (true) {
        val s = sqrt(p.toDouble()).toInt()
        val d = primesSoFar.first { it > s || p % it == 0 }
        if (d > s) {
            primesSoFar.add(p)
            send(p)
        }
        p += 2
    }
}

@ExperimentalCoroutinesApi
private fun primesUsingChannel() {
    runBlocking {
        val primes = primeChannel()
        repeat(999_999) {
            primes.receive()
        }
        repeat(100) {
            print("${primes.receive()}, ")
        }
        primes.cancel()
    }
}


private fun primesUsingList() {
    val primesSoFar = mutableListOf(2)
    val k = 999_999 + 100
    var p = 3
    var n = 1
    while (n <= k) {
        val s = sqrt(p.toDouble()).toInt()
        val d = primesSoFar.first { it > s || p % it == 0 }
        if (d > s) {
            primesSoFar.add(p)
            n++
        }
        p += 2
    }
    println(primesSoFar
            .subList(k - 100, k)
            .joinToString(separator = ", "))
}

@ExperimentalCoroutinesApi
fun main() {
    val start1 = now()
    primesUsingList()
    println("Time: ${now() - start1}")
    val start2 = now()
    primesUsingChannel()
    println("Time: ${now() - start2}")
}