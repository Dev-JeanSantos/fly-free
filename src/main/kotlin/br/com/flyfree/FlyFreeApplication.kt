package br.com.flyfree

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class FlyFreeApplication

fun main(args: Array<String>) {
    runApplication<FlyFreeApplication>(*args)
}
