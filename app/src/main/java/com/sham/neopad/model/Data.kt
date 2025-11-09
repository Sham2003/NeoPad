package com.sham.neopad.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName
import java.net.InetAddress


@Parcelize
data class LayoutCreatorDTO(
    val layoutName: String,
    val layoutType: ControllerType,
    val isDefault: Boolean,
    val selectedLayout : String,
    val isEditMode: Boolean
): Parcelable


@Parcelize
data class LayoutViewerDTO(
    val isDefault: Boolean,
    val layoutId: String,
    val filename : String
): Parcelable

@Parcelize
data class ControllerDTO(
    val layoutId: String,
    val clientRefId : String,
): Parcelable


// Data class for tilt state
data class TiltState(
    val p: Float = 0f, // -1 (left) to 1 (right)
    val r: Float = 0f,
    val a: Float = 0f
)

data class TiltIndicatorConfig(
    val showLeft: Boolean = false,
    val showRight: Boolean = false,
    val showUp: Boolean = false,
    val showDown: Boolean = false,
)


data class SettingStore(
    val buttonThreshold: Float = 0.4f,
    val glowWidth: Float = 8f,
    val animationDuration: Int = 150,
    val hapticFeedback: Boolean = true,
    val steeringRange : Float = 0.5f,
    val softTrigger : Boolean = false,
    val neutralAzimuth : Float = 0.0f,
    val neutralPitch : Float = 0.0f,
    val neutralRoll : Float = 0.0f
)

data class VirtualController (
    @SerializedName("Id")
    val id: Int = 0,

    @SerializedName("Type")
    val type: ControllerType = ControllerType.PS4,

    @SerializedName("Name")
    val name: String = "dummy",

    @SerializedName("ClientRefId")
    val clientRefId: String = "",

    @SerializedName("LayoutId")
    val layoutId: String = ""
)

data class ConnectionInfo(
    val sessionId: String = "",
    val url:String = "",
    val name: String = "",
    val username: String = ""
)

data class DiscoveredConnection(
    val sessionId: String,
    val address: InetAddress,
    val port: Int,
    val name: String,
    val expiresAt: Long
)
