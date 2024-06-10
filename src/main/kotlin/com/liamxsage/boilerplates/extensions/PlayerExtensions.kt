package com.liamxsage.boilerplates.extensions

import com.liamxsage.boilerplates.BLOCK_PREFIX
import com.liamxsage.boilerplates.PREFIX
import dev.fruxz.stacked.text
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


fun Player.sendMessagePrefixed(message: String) {
    this.sendMessage(text(PREFIX + message))
}

fun CommandSender.sendMessagePrefixed(message: String) = sendMessage(text(PREFIX + message))

fun broadcastPrefixed(message: String) = Bukkit.broadcast(text(PREFIX + message))

fun CommandSender.sendMessageBlock(vararg lines: String) {
    sendEmtpyLine()
    sendMessage(text(BLOCK_PREFIX))
    sendEmtpyLine()
    lines.forEach { sendMessage(text(it)) }
    sendEmtpyLine()
}

fun CommandSender.sendEmtpyLine() = sendMessage(text(" "))

fun Player.soundExecution() {
    playSound(location, Sound.ENTITY_ITEM_PICKUP, .75F, 2F)
    playSound(location, Sound.ITEM_ARMOR_EQUIP_LEATHER, .25F, 2F)
    playSound(location, Sound.ITEM_ARMOR_EQUIP_CHAIN, .1F, 2F)
}


fun Player.sendDeniedSound() = playSound(location, "minecraft:block.note_block.bass", 1f, 1f)

fun Player.sendSuccessSound() = playSound(location, "minecraft:block.note_block.pling", 1f, 1f)

fun Player.sendTeleportSound() = playSound(location, "minecraft:block.note_block.harp", 1f, 1f)

fun Player.sendOpenSound() = playSound(location, "minecraft:block.note_block.chime", 1f, 1f)