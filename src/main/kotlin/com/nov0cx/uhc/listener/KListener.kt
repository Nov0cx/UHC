package com.nov0cx.uhc.listener

import com.nov0cx.uhc.UHC
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

// credits: https://github.com/bluefireoly/KSpigot/blob/master/src/main/kotlin/net/axay/kspigot/event/KSpigotListeners.kt


/**
 * Shortcut for registering this listener on the given plugin.
 */
fun Listener.register() = Bukkit.getPluginManager().registerEvents(this, UHC.instance)

/**
 * Shortcut for unregistering all events in this listener.
 */
fun Listener.unregister() = HandlerList.unregisterAll(this)

/**
 * Registers the event with a custom event [executor].
 *
 * @param T the type of event
 * @param priority the priority when multiple listeners handle this event
 * @param ignoreCancelled if manual cancellation should be ignored
 * @param executor handles incoming events
 */
inline fun <reified T : Event> Listener.register(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    noinline executor: (Listener, Event) -> Unit
) {
    Bukkit.getPluginManager().registerEvent(T::class.java, this, priority, executor, UHC.instance, ignoreCancelled)
}

/**
 * This class represents a [Listener] with
 * only one event to listen to.
 */
interface SingleListener<T : Event> : Listener {
    fun onEvent(event: T)
}

/**
 * Registers the [SingleListener] with its
 * event listener.
 *
 * @param priority the priority when multiple listeners handle this event
 * @param ignoreCancelled if manual cancellation should be ignored
 */
inline fun <reified T : Event> SingleListener<T>.register(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false
) {
    register<T>(priority, ignoreCancelled) { _, event ->
        (event as? T)?.let { this.onEvent(it) }
    }
}

/**
 * @param T the type of event to listen to
 * @param priority the priority when multiple listeners handle this event
 * @param ignoreCancelled if manual cancellation should be ignored
 * @param onEvent the event callback
 */
inline fun <reified T : Event> listen(
    priority: EventPriority = EventPriority.NORMAL,
    ignoreCancelled: Boolean = false,
    crossinline onEvent: (event: T) -> Unit
): SingleListener<T> {
    val listener = object : SingleListener<T> {
        override fun onEvent(event: T) = onEvent.invoke(event)
    }
    listener.register(priority, ignoreCancelled)
    return listener
}
