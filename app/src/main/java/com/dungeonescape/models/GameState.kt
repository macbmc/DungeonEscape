package com.dungeonescape.models

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.Particle
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton

data class GameState(
    val screenState: ScreenState = ScreenState.MAIN_MENU,
    val player: Player = Player(),
    val skeletons: List<Skeleton> = emptyList(),
    val coins: List<Coin> = emptyList(),
    val key: KeyItem? = null,
    val portal: ExitPortal? = null,
    val tiles: Array<Array<Tile>> = emptyArray(),
    val particles: List<Particle> = emptyList(),
    val levelState: LevelState = LevelState(),
    val score: Int = 0,
    val cameraPosition: Vector2D = Vector2D.ZERO,
    val isPaused: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (screenState != other.screenState) return false
        if (player != other.player) return false
        if (skeletons != other.skeletons) return false
        if (coins != other.coins) return false
        if (key != other.key) return false
        if (portal != other.portal) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (particles != other.particles) return false
        if (levelState != other.levelState) return false
        if (score != other.score) return false
        if (cameraPosition != other.cameraPosition) return false
        if (isPaused != other.isPaused) return false

        return true
    }

    override fun hashCode(): Int {
        var result = screenState.hashCode()
        result = 31 * result + player.hashCode()
        result = 31 * result + skeletons.hashCode()
        result = 31 * result + coins.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (portal?.hashCode() ?: 0)
        result = 31 * result + tiles.contentDeepHashCode()
        result = 31 * result + particles.hashCode()
        result = 31 * result + levelState.hashCode()
        result = 31 * result + score
        result = 31 * result + cameraPosition.hashCode()
        result = 31 * result + isPaused.hashCode()
        return result
    }
}
