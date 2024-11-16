package com.primogemstudio.engine.ext

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class WrappedProcess(val proc: Process) {
    init {
        GlobalScope.launch {
            val r = proc.inputReader(Charsets.UTF_8)
            while (proc.isAlive) {
                r.readLine()?.let {
                    println(it)
                }
            }
        }

        GlobalScope.launch {
            val r = proc.errorReader(Charsets.UTF_8)
            while (proc.isAlive) {
                r.readLine()?.let {
                    println(it)
                }
            }
        }
    }

    fun waitForProcess(): Int {
        while (proc.isAlive) {
            proc.isAlive
        }

        return proc.exitValue()
    }
}