package com.sham.neopad.service

import com.sham.neopad.appLog
import com.sham.neopad.model.ControllerType
import com.sham.neopad.model.ButtonType
import com.sham.neopad.model.DpadDirection
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.max
import kotlin.math.min

class InputManager(val softTrigger: Boolean = false) {
    private val gamepads = ConcurrentHashMap<String, GamepadInput>()

    fun initializeGamepad(gid: Int, clientRefId: String, type: ControllerType) {
        gamepads[clientRefId] = GamepadInput(gid, clientRefId, type, softTrigger)
    }

    fun getGamepad(clientRefId: String): GamepadInput {
        return gamepads[clientRefId]
            ?: throw Exception("Couldn't Get Gamepad $clientRefId \nInput Manager Count = ${gamepads.size}\nGamepads = [${gamepads.keys}]")
    }

    fun getAllGamepads(): Map<String, GamepadInput> = gamepads.toMap()

    fun removeGamepad(clientRefId: String) {
        val gpad = gamepads.remove(clientRefId)
        gpad?.shutdown()
    }

    fun clearAll() {
        gamepads.values.forEach { it.shutdown() } // properly shut down threads
        gamepads.clear()
    }
}



class GamepadInput(
    val id:Int,
    val clientRefId: String,
    val type: ControllerType,
    val softTrigger: Boolean = false
) {
    @Volatile var stickLeftX: Short = 0
    @Volatile var stickLeftY: Short = 0
    @Volatile var stickRightX: Short = 0
    @Volatile var stickRightY: Short = 0

    @Volatile
    private var triggerLeft: Byte = 0
    @Volatile
    private var triggerRight: Byte = 0

    @Volatile
    private var buttonMask: Short = 0

    private val increaseStep = 600           // how fast it increases
    private val decreaseStep = 1800          // how fast it decreases

    private val maxValue = Short.MAX_VALUE.toInt()  // 32767
    private val tickDelay = 10L                     // ms between updates

    private val leftPressed = AtomicBoolean(false)
    private val rightPressed = AtomicBoolean(false)

    @Volatile private var running = true

    private var triggerThread: Thread? = null

    init {
        if (softTrigger) {
            triggerThread = Thread {
                while (running) {
                    triggerLeft = updateTrigger(triggerLeft, leftPressed.get())
                    triggerRight = updateTrigger(triggerRight, rightPressed.get())
                    Thread.sleep(tickDelay)
                }
            }.apply {
                isDaemon = true
                @Suppress("Deprecation")
                name = "SoftTriggerUpdater-${id}"
                start()
            }
        }
    }

    private fun updateTrigger(current: Byte, pressed: Boolean): Byte {
        val cur = current.toInt() and 0xFF  // treat unsigned 0–255
        val next = if (pressed)
            min(maxValue, cur + increaseStep)
        else
            max(0, cur - decreaseStep)
        return next.toByte()
    }


    fun setButton(button: ButtonType, pressed: Boolean) {
        if (button.bit < 0 || button.bit > 15)
        {
            setTrigger(button, pressed)
            return
        }
        buttonMask = if (pressed)
            (buttonMask.toInt() or (1 shl button.bit)).toShort()
        else
            (buttonMask.toInt() and (1 shl button.bit).inv()).toShort()

        appLog("$button Mask = ${buttonMask.toUInt().toString(2).padStart(16, '0')}")
    }

    fun setTrigger(button: ButtonType, pressed: Boolean) {
        if (softTrigger) {
            when (button) {
                ButtonType.L2, ButtonType.LT -> leftPressed.set(pressed)
                ButtonType.R2, ButtonType.RT -> rightPressed.set(pressed)
                else -> return
            }
        } else {
            val value: Byte = if (pressed) (-1).toByte() else 0
            when (button) {
                ButtonType.L2, ButtonType.LT -> triggerLeft = value
                ButtonType.R2, ButtonType.RT -> triggerRight = value
                else -> return
            }
        }
        appLog("$button Trigger values = $triggerLeft , $triggerRight")
    }

    fun setDpad(direction: DpadDirection, pressed: Boolean) {
        direction.bits.forEach { bit ->
            buttonMask = if (pressed)
                (buttonMask.toInt() or (1 shl bit)).toShort()
            else
                (buttonMask.toInt() and (1 shl bit).inv()).toShort()
        }
        appLog("$direction Mask = ${buttonMask.toUInt().toString(2).padStart(16, '0')}")
    }




    fun toByteArray(): ByteArray {
        val cRIBytes = clientRefId.toByteArray(Charsets.UTF_8)

        val buffer = ByteArray(1 + 1 + cRIBytes.size + 1 + 2 + 1 + 1 + 8)
        var offset = 0

        buffer[offset++] = id.toByte()
        buffer[offset++] = cRIBytes.size.toByte()
        System.arraycopy(cRIBytes, 0, buffer, offset, cRIBytes.size)
        offset += cRIBytes.size

        buffer[offset++] = when (type) {
            ControllerType.PS4 -> 0
            ControllerType.XBOX -> 1
        }.toByte()

        buffer[offset++] = (buttonMask.toInt() and 0xFF).toByte()
        buffer[offset++] = (buttonMask.toInt() shr 8).toByte()

        buffer[offset++] = triggerLeft
        buffer[offset++] = triggerRight

        fun putShort(value: Short) {
            buffer[offset++] = (value.toInt() and 0xFF).toByte()
            buffer[offset++] = (value.toInt() shr 8).toByte()
        }

        putShort(stickLeftX)
        putShort(stickLeftY)
        putShort(stickRightX)
        putShort(stickRightY)

        return buffer
    }

    fun shutdown() {
        running = false
        triggerThread?.interrupt()
    }
}
