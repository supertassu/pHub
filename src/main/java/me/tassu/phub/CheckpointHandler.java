/*
 * The MIT License
 *
 * Copyright © 2018 Tassu <hello@tassu.me>
 * Copyright © 2018 Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package me.tassu.phub;

import com.google.common.collect.Maps;
import lombok.val;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.UUID;

public class CheckpointHandler {

    private Map<UUID, Location<World>> checkpoints = Maps.newHashMap();

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        checkpoints.remove(event.getCause().first(Player.class).orElseThrow(IllegalStateException::new).getUniqueId());
    }

    @Listener(order = Order.LATE)
    @IsCancelled(Tristate.UNDEFINED)
    public void onDamageEntity(DamageEntityEvent event, @First DamageSource damageSource) {
        if (!(event.getTargetEntity() instanceof Player)) return;
        val player = (Player) event.getTargetEntity();

        if (damageSource == DamageSources.VOID && checkpoints.containsKey(player.getUniqueId())) {
            player.sendMessage(Text.of(TextColors.GREEN, "(Parkour) ", TextColors.DARK_GREEN, "Checkpoint loaded."));
            player.setLocation(checkpoints.get(player.getUniqueId()));
        }

    }

    @Listener
    public void onMoveEntity(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        val player = (Player) event.getTargetEntity();
        val block = event.getToTransform().getLocation().getBlock();

        if (block.getType() == BlockTypes.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            checkpoints.put(player.getUniqueId(), player.getLocation());
            player.sendMessage(ChatTypes.ACTION_BAR, Text.of(TextColors.GREEN, "(Parkour) ", TextColors.DARK_GREEN, "Checkpoint saved."));
        }

    }
}
