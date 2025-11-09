package com.sham.neopad.layout

import com.sham.neopad.model.*

val xboxDefaultLayout = ControllerLayoutData(
    layoutId = "6609265f-9fa2-4673-ad9a-5dd8894343f2",
    layoutName = "XBOX Default",
    controllerType = ControllerType.XBOX,
    components = listOf(
        GamepadComponent(
            type = ComponentType.Dpad,
            x = 0.16278443f,
            y = 0.5312147f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.3f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.LeftStick,
            x = 0.37112468f,
            y = 0.7944741f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.4110518f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.RightStick,
            x = 0.6446306f,
            y = 0.80808496f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.38383004f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.GamepadButtons,
            x = 0.8535315f,
            y = 0.5376275f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.33101055f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.Menu),
            x = 0.40678427f,
            y = 0.2558856f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.Back),
            x = 0.5938656f,
            y = 0.2590974f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.XBOX),
            x = 0.500824f,
            y = 0.25878274f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.LT),
            x = 0.111503094f,
            y = 0.08093172f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.10598542f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.RT),
            x = 0.9041923f,
            y = 0.081468835f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.109830394f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.LB),
            x = 0.10982463f,
            y = 0.24914461f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.1027969f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.RB),
            x = 0.9025f,
            y = 0.23870715f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.10598542f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type=ComponentType.ControllerButton(ButtonType.RS),
            x=0.563171f,
            y=0.57795554f,
            color=-65536,
            dimension= ComponentDimension.SameSize(size=0.16f),
            squareMode=false
        ),
        GamepadComponent(
            type=ComponentType.ControllerButton(ButtonType.LS),
            x=0.47112393f,
            y=0.5803683f,
            color=-65536,
            dimension=ComponentDimension.SameSize(size=0.16f),
            squareMode=false
        )
    ),
    isCustom = false,
    specialButtons = SpecialButtons(
        left = ButtonType.None,
        right = ButtonType.None,
        up = ButtonType.None,
        down = ButtonType.None,
        volumeUp = ButtonType.None,
        volumeDown = ButtonType.None
    ),
    filename = "DEFAULT_LAYOUT"
)

val ps4DefaultLayout = ControllerLayoutData(
    layoutId = "522ebb97-ecdd-4c20-a788-7a5f1bb8c80d",
    layoutName = "PS4 Default",
    controllerType = ControllerType.PS4,
    components = listOf(
        GamepadComponent(
            type = ComponentType.LeftStick,
            x = 0.37501848f,
            y = 0.73777485f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.5086865f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.RightStick,
            x = 0.59907454f,
            y = 0.7302966f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.53587574f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.LS),
            x = 0.43791753f,
            y = 0.4638889f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.RS),
            x = 0.5298547f,
            y = 0.47056985f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.Dpad,
            x = 0.13133855f,
            y = 0.58100617f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.4237453f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.PS),
            x = 0.49574953f,
            y = 0.28000867f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.Options),
            x = 0.38951945f,
            y = 0.28040814f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.Share),
            x = 0.607519f,
            y = 0.2771711f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.16f),
            squareMode = false
        ),
        GamepadComponent(
            type = ComponentType.GamepadButtons,
            x = 0.84731555f,
            y = 0.59754574f,
            color = -65536,
            dimension = ComponentDimension.SameSize(0.4871469f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.L2),
            x = 0.09332168f,
            y = 0.08f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.119209036f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.L1),
            x = 0.09287204f,
            y = 0.2321253f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.11814971f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.R2),
            x = 0.8807092f,
            y = 0.08f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.104577325f, 0.16f),
            squareMode = true
        ),
        GamepadComponent(
            type = ComponentType.ControllerButton(ButtonType.R1),
            x = 0.8827356f,
            y = 0.22838417f,
            color = -65536,
            dimension = ComponentDimension.DoubleSize(0.11073446f, 0.16f),
            squareMode = true
        )
    ),
    isCustom = false,
    specialButtons = SpecialButtons(
        left = ButtonType.None,
        right = ButtonType.None,
        up = ButtonType.None,
        down = ButtonType.None,
        volumeUp = ButtonType.None,
        volumeDown = ButtonType.None
    ),
    filename = "DEFAULT_LAYOUT"
)