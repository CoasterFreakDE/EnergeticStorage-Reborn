package com.liamxsage.energeticstorage.extensions

import com.liamxsage.energeticstorage.BLOCK_PREFIX
import com.liamxsage.energeticstorage.PLAYER_DEBUG_MODE_NAMESPACE
import com.liamxsage.energeticstorage.PREFIX
import dev.fruxz.stacked.text
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType


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

fun Player.sendInfoSound() = playSound(location, "minecraft:block.note_block.bit", 1f, 1f)

fun Player.sendRemoveDiskSound() = playSound(location, Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1f, 1f)

fun Player.sendInsertDiskSound() = playSound(location, Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 1f, 1f)

var Player.isESDebugModeEnabled: Boolean
    get() = persistentDataContainer.has(PLAYER_DEBUG_MODE_NAMESPACE, PersistentDataType.BOOLEAN) &&
            persistentDataContainer[PLAYER_DEBUG_MODE_NAMESPACE, PersistentDataType.BOOLEAN] == true
    set(value) {
        persistentDataContainer[PLAYER_DEBUG_MODE_NAMESPACE, PersistentDataType.BOOLEAN] = value
    }