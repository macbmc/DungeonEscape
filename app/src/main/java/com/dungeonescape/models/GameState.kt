package com.dungeonescape.models

import com.dungeonescape.entities.Coin
import com.dungeonescape.entities.ExitPortal
import com.dungeonescape.entities.HealthPotion
import com.dungeonescape.entities.KeyItem
import com.dungeonescape.entities.MerchantAltar
import com.dungeonescape.entities.Particle
import com.dungeonescape.entities.Player
import com.dungeonescape.entities.Skeleton
import com.dungeonescape.entities.TrapSpike
import com.dungeonescape.entities.TreasureChest

data class GameState(
    val screenState: ScreenState = ScreenState.MAIN_MENU,
    val player: Player = Player(),
    val skeletons: List<Skeleton> = emptyList(),
    val coins: List<Coin> = emptyList(),
    val potions: List<HealthPotion> = emptyList(),
    val chests: List<TreasureChest> = emptyList(),
    val merchantAltar: MerchantAltar? = null,
    val traps: List<TrapSpike> = emptyList(),
    val key: KeyItem? = null,
    val portal: ExitPortal? = null,
    val tiles: Array<Array<Tile>> = emptyArray(),
    val particles: List<Particle> = emptyList(),
    val levelState: LevelState = LevelState(),
    val score: Int = 0,
    val cameraPosition: Vector2D = Vector2D.ZERO,
    val isPaused: Boolean = false,
    val theme: DungeonTheme = DungeonTheme.ANCIENT_RUINS,
    val isMerchantNear: Boolean = false,
    val isMerchantDialogOpen: Boolean = false,
    val healFeedbackText: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GameState

        if (screenState != other.screenState) return false
        if (player != other.player) return false
        if (skeletons != other.skeletons) return false
        if (coins != other.coins) return false
        if (potions != other.potions) return false
        if (chests != other.chests) return false
        if (merchantAltar != other.merchantAltar) return false
        if (traps != other.traps) return false
        if (key != other.key) return false
        if (portal != other.portal) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (particles != other.particles) return false
        if (levelState != other.levelState) return false
        if (score != other.score) return false
        if (cameraPosition != other.cameraPosition) return false
        if (isPaused != other.isPaused) return false
        if (theme != other.theme) return false
        if (isMerchantNear != other.isMerchantNear) return false
        if (isMerchantDialogOpen != other.isMerchantDialogOpen) return false
        if (healFeedbackText != other.healFeedbackText) return false

        return true
    }

    override fun hashCode(): Int {
        var result = screenState.hashCode()
        result = 31 * result + player.hashCode()
        result = 31 * result + skeletons.hashCode()
        result = 31 * result + coins.hashCode()
        result = 31 * result + potions.hashCode()
        result = 31 * result + chests.hashCode()
        result = 31 * result + (merchantAltar?.hashCode() ?: 0)
        result = 31 * result + traps.hashCode()
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (portal?.hashCode() ?: 0)
        result = 31 * result + tiles.contentDeepHashCode()
        result = 31 * result + particles.hashCode()
        result = 31 * result + levelState.hashCode()
        result = 31 * result + score
        result = 31 * result + cameraPosition.hashCode()
        result = 31 * result + isPaused.hashCode()
        result = 31 * result + theme.hashCode()
        result = 31 * result + isMerchantNear.hashCode()
        result = 31 * result + isMerchantDialogOpen.hashCode()
        result = 31 * result + (healFeedbackText?.hashCode() ?: 0)
        return result
    }
}
