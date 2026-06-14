package com.lalkalol.mujahid.util

import dev.arbjerg.lavalink.client.Link
import dev.arbjerg.lavalink.client.player.LavalinkLoadResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.requests.RestAction
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> RestAction<T>.await(): T = suspendCancellableCoroutine { cont ->
    queue(
        { result -> cont.resume(result) },
        { error -> cont.resumeWithException(error) },
    )
}

suspend fun Link.loadItemSuspend(identifier: String): LavalinkLoadResult =
    loadItem(identifier).awaitSingle()
