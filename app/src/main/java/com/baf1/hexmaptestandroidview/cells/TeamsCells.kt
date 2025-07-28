package com.baf1.hexmaptestandroidview.cells

import com.baf1.hexmaptestandroidview.CellDTO
import com.baf1.hexmaptestandroidview.Teams
import java.util.concurrent.ConcurrentHashMap

class TeamsCells {
    private val teamsCells = ConcurrentHashMap<Teams, HashSet<String>>()
    var dominantTeam = Teams.NONE
        private set

    fun updateTeamsCells(cell: CellDTO) {
        val teamId = Teams.getById(cell.teamId)

        teamsCells.values.forEach { it.remove(cell.id) } // Удаление из старой команды
        teamsCells.getOrPut(teamId) { hashSetOf() }.add(cell.id) // Добавление в новую

        dominantTeam = teamsCells.maxByOrNull { it.value.size }?.key ?: Teams.NONE
    }
}
