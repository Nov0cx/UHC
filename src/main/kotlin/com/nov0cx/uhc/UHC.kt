package com.nov0cx.uhc

import org.bukkit.plugin.java.JavaPlugin

class UHC : JavaPlugin() {

    companion object STATIC {
        lateinit var instance: UHC
    }

    override fun onEnable() {
        instance = this
    }

    override fun onDisable() {

    }
}