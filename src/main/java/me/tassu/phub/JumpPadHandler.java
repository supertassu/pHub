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

import lombok.val;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;

public class JumpPadHandler {

    @Listener
    public void onMoveEntity(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        val player = (Player) event.getTargetEntity();
        val block = event.getToTransform().getExtent().getBlock(event.getToTransform().getPosition().sub(0, 1, 0).toInt());

        if (block.getType() == BlockTypes.SLIME) {
            player.setVelocity(player.getVelocity().add(0, 0.05, 0).mul(3));
        }

    }
}
