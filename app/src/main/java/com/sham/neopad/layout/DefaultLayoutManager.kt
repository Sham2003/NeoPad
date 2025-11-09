package com.sham.neopad.layout

import com.sham.neopad.model.ControllerLayoutData
import com.sham.neopad.model.ControllerType

object DefaultLayoutManager {
    private val l = listOf(
        ps4DefaultLayout,
        xboxDefaultLayout,
    )

    fun getAll(): List<ControllerLayoutData> {
        return l
    }

    fun getLayout(id : String)  = l.first { it.layoutId == id }

    fun containsLayout(lid: String): Boolean {
        return l.indexOfFirst { it.layoutId == lid } != -1
    }

    fun getDummyLayout() : List<ControllerLayoutData> {
        return listOf(
            ControllerLayoutData("xbox_1", "Classic XBOX", ControllerType.XBOX),
            ControllerLayoutData("xbox_race", "Racing", ControllerType.XBOX),
            ControllerLayoutData("ps4_fight", "Racing PS4", ControllerType.PS4),
        )
    }
}