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

package me.tassu.phub.util;

import lombok.Getter;
import me.tassu.phub.PHub;
import org.spongepowered.api.Sponge;

import java.util.concurrent.TimeUnit;

public class BungeeTracker {

    @Getter private static BungeeTracker instance;
    @Getter private int count = 0;

    public BungeeTracker(PHub plugin) {
        instance = this;

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            if (Sponge.getServer().getOnlinePlayers().size() < 1) return;
            plugin.getBungeeLib().getGlobalPlayerCount().thenAccept(count -> this.count = count);
        })
                .interval(10, TimeUnit.SECONDS)
                .name("Hub Scoreboard updater")
                .submit(plugin);
    }
}
