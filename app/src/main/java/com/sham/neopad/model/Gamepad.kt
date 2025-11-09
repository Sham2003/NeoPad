package com.sham.neopad.model

import com.sham.neopad.R
import java.io.InputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.UUID


enum class DpadDirection(val bits: List<Int>) {
    NORTH(listOf(12)),
    SOUTH(listOf(13)),
    WEST(listOf(14)),
    EAST(listOf(15)),

    NORTH_EAST(listOf(12, 15)),
    NORTH_WEST(listOf(12, 14)),
    SOUTH_EAST(listOf(13, 15)),
    SOUTH_WEST(listOf(13, 14));
}


enum class ButtonType(val label: String, val isDrawable: Boolean = false, val drawable: Int = 0, val bit: Int = -1) {
    None("None"),

    // XBOX Buttons (Bits 0-7 overlap with PS4)
    A("A", bit = 0),
    B("B", bit = 1),
    X("X", bit = 2),
    Y("Y", bit = 3),

    LB("LB" , bit = 4),
    RB("RB" , bit = 5),
    LT("LT", bit = -1), // Set to -1 (Analog Input)
    RT("RT", bit = -1), // Set to -1 (Analog Input)

    Menu("Menu", true, R.drawable.ic_menu, bit = 8),
    Back("Back", true, R.drawable.ic_back, bit = 9),
    XBOX("XBOX", true, R.drawable.logo_xbox, bit = 10),

    Cross("Cross", true, R.drawable.ps4_cross, bit = 0),
    Circle("Circle", true, R.drawable.ps4_circle, bit = 1),
    Square("Square", true, R.drawable.ps4_square, bit = 2),
    Triangle("Triangle", true, R.drawable.ps4_triangle, bit = 3),

    L1("L1", bit = 4),
    R1("R1", bit = 5),
    L2("L2", bit = -1),
    R2("R2", bit = -1),

    Options("Options", true, R.drawable.ic_menu, bit = 8),
    Share("Share", true, R.drawable.ic_back, bit = 9),
    PS("PS", true, R.drawable.logo_ps4, bit = 10),
    TouchPad("TouchPad",true,R.drawable.ic_touchpad,bit = 11),

    LS("LS", bit = 6),
    RS("RS", bit = 7);

    companion object {
        val PS4_BUTTONS = listOf(PS, Cross, Circle, Square, Triangle, L1, R1, L2, R2, Options, Share, LS, RS,TouchPad)
        val XBOX_BUTTONS: List<ButtonType> = listOf(A, B, X, Y, LB, RB, LT, RT, Menu, Back, XBOX, LS, RS)
    }
}

sealed class ComponentType {
    open val isSquareModeCompatible = false
    open val canBe2D = false
    object Dpad : ComponentType() {
        override fun toString(): String {
            return "Dpad"
        }
    }
    object GamepadButtons: ComponentType() {
        override val isSquareModeCompatible = true
        override fun toString(): String {
            return "GamepadButtons"
        }
    }
    object LeftStick : ComponentType() {
        override fun toString(): String {
            return "LeftStick"
        }
    }

    object RightStick : ComponentType() {
        override fun toString(): String {
            return "RightStick"
        }
    }
    data class ControllerButton(val button: ButtonType) : ComponentType() {
        override val isSquareModeCompatible = true
        override val canBe2D = true
        override fun toString(): String {
            return "Controller[btn=$button]"
        }
    }
}

sealed class ComponentDimension {
    data class SameSize(val size: Float) : ComponentDimension() {
        override fun toString(): String {
            return "SameDimension[size=$size]"
        }
    }
    data class DoubleSize(val width: Float, val height: Float ): ComponentDimension() {
        override fun toString(): String {
            return "DoubleDimension[w=$width,h=$height]"
        }
    }

}




data class GamepadComponent(
    val type: ComponentType,
    val x: Float,
    val y: Float,
    val color: Int,
    val dimension: ComponentDimension,
    val squareMode: Boolean = false
) {

    val creatorId = UUID.randomUUID()
    companion object {
        fun fromStream(dis: DataInputStream) : GamepadComponent {
            // 1. Read ComponentType
            val typeOrdinal = dis.readByte().toInt()
            val type: ComponentType = when (typeOrdinal) {
                0 -> ComponentType.Dpad
                1 -> ComponentType.GamepadButtons
                2 -> ComponentType.LeftStick
                3 -> ComponentType.RightStick
                4 -> { // ControllerButton
                    val buttonOrdinal = dis.readInt()
                    // Assuming ButtonType.entries is available (or using values() for older Kotlin)
                    val buttonType = ButtonType.entries.getOrNull(buttonOrdinal)
                        ?: throw IOException("Unknown ButtonType ordinal: $buttonOrdinal")
                    ComponentType.ControllerButton(buttonType)
                }
                else -> throw IOException("Unknown ComponentType ordinal: $typeOrdinal")
            }

            // 2. Read GamepadComponent base properties
            val x = dis.readFloat()
            val y = dis.readFloat()
            val color = dis.readInt()

            // 3. Read ComponentDimension indicator and data
            val dimensionOrdinal = dis.readByte().toInt()
            val dimension: ComponentDimension = when (dimensionOrdinal) {
                0 -> { // SameSize(size: Float)
                    val size = dis.readFloat()
                    ComponentDimension.SameSize(size)
                }
                1 -> { // DoubleSize(width: Float, height: Float)
                    val width = dis.readFloat()
                    val height = dis.readFloat()
                    ComponentDimension.DoubleSize(width, height)
                }
                else -> throw IOException("Unknown ComponentDimension ordinal: $dimensionOrdinal")
            }

            // 4. Read squareMode
            val squareMode = dis.readBoolean()

            return GamepadComponent(type, x, y, color, dimension, squareMode)
        }

        // Helper function to write a single GamepadComponent
        fun toStream(component: GamepadComponent, dos: DataOutputStream) {
            // 1. Write ComponentType
            when (component.type) {
                ComponentType.Dpad -> dos.writeByte(0)
                ComponentType.GamepadButtons -> dos.writeByte(1)
                ComponentType.LeftStick -> dos.writeByte(2)
                ComponentType.RightStick -> dos.writeByte(3)
                is ComponentType.ControllerButton -> {
                    dos.writeByte(4)
                    dos.writeInt(component.type.button.ordinal) // Write the ButtonType ordinal
                }
            }

            // 2. Write GamepadComponent base properties
            dos.writeFloat(component.x)
            dos.writeFloat(component.y)
            dos.writeInt(component.color)

            // 3. Write ComponentDimension indicator and data
            when (val dimension = component.dimension) {
                is ComponentDimension.SameSize -> {
                    dos.writeByte(0) // Indicator for SameSize
                    dos.writeFloat(dimension.size)
                }
                is ComponentDimension.DoubleSize -> {
                    dos.writeByte(1) // Indicator for DoubleSize
                    dos.writeFloat(dimension.width)
                    dos.writeFloat(dimension.height)
                }
            }

            // 4. Write squareMode
            dos.writeBoolean(component.squareMode)
        }
    }
}



data class SpecialButtons(
    val left: ButtonType = ButtonType.None,
    val right: ButtonType = ButtonType.None,
    val up : ButtonType = ButtonType.None,
    val down: ButtonType = ButtonType.None,
    val volumeUp: ButtonType = ButtonType.None,
    val volumeDown: ButtonType = ButtonType.None
) {
    val isAllNone = left == ButtonType.None && right == ButtonType.None
            && up == ButtonType.None && down == ButtonType.None
            && volumeUp == ButtonType.None && volumeDown == ButtonType.None
}


data class ControllerLayoutData(
    val layoutId: String,
    val layoutName: String,
    val controllerType: ControllerType = ControllerType.PS4,
    val components: List<GamepadComponent> = emptyList(),
    val isCustom: Boolean = false,
    val specialButtons: SpecialButtons = SpecialButtons(),
    val filename: String = "DEFAULT_LAYOUT"
) {

    fun isRotationLayout() = !specialButtons.isAllNone

    companion object {
        private const val DATA_MAGIC = 2003
        fun fromStream(stream: InputStream) : ControllerLayoutData {
            val dis = DataInputStream(stream)
            val magic = dis.readInt()
            if (magic != DATA_MAGIC) {
                throw IOException("Unsupported file magic code: $magic")
            }

            val layoutId = dis.readUTF()
            val layoutName = dis.readUTF()
            val controllerType = ControllerType.entries[dis.readInt()]
            val isCustom = dis.readBoolean()

            val leftTilt = ButtonType.entries[dis.readInt()]
            val rightTilt = ButtonType.entries[dis.readInt()]
            val upTilt = ButtonType.entries[dis.readInt()]
            val downTilt = ButtonType.entries[dis.readInt()]
            val volumeUp = ButtonType.entries[dis.readInt()]
            val volumeDown = ButtonType.entries[dis.readInt()]

            val specialButtons = SpecialButtons(leftTilt,rightTilt,upTilt,downTilt, volumeUp, volumeDown)

            val componentCount = dis.readInt()
            val components = mutableListOf<GamepadComponent>()
            repeat(componentCount) {
                components.add(GamepadComponent.fromStream(dis))
            }

            // Read filename (Optional, can be skipped if not needed for file content)
            val filename = dis.readUTF()

            return ControllerLayoutData(
                layoutId = layoutId,
                layoutName = layoutName,
                controllerType = controllerType,
                components = components,
                isCustom = isCustom,
                specialButtons = specialButtons,
                filename = filename
            )
        }

        fun toStream(data: ControllerLayoutData, stream: OutputStream) {
            DataOutputStream(stream).use { dos ->
                // Write header
                dos.writeInt(DATA_MAGIC)

                // Write basic properties
                dos.writeUTF(data.layoutId) // writeUTF is preferred for Strings in DataOutputStream
                dos.writeUTF(data.layoutName)
                dos.writeInt(data.controllerType.ordinal)
                dos.writeBoolean(data.isCustom)

                // Write SpecialButtons
                dos.writeInt(data.specialButtons.left.ordinal)
                dos.writeInt(data.specialButtons.right.ordinal)
                dos.writeInt(data.specialButtons.up.ordinal)
                dos.writeInt(data.specialButtons.down.ordinal)
                dos.writeInt(data.specialButtons.volumeUp.ordinal)
                dos.writeInt(data.specialButtons.volumeDown.ordinal)

                dos.writeInt(data.components.size)
                data.components.forEach { component ->
                    GamepadComponent.toStream(component, dos)
                }
                dos.writeUTF(data.filename)
                dos.flush() // Ensure all data is written to the underlying stream
            }
        }

    }
}
