package me.tassu.phub;

import lombok.val;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.text.Text;

public class JumpPadHandler {

    @Listener
    public void onMoveEntity(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        val player = (Player) event.getTargetEntity();
        val block = event.getToTransform().getExtent().getBlock(event.getToTransform().getPosition().sub(0, 1, 0).toInt());

        if (block.getType() == BlockTypes.SLIME) {
            player.setVelocity(player.getVelocity().add(0, 0.05, 0).mul(5));
        }

    }
}
