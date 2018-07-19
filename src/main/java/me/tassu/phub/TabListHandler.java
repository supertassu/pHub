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
import me.tassu.phub.util.BungeeTracker;
import me.tassu.phub.util.QueueData;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.spongepowered.api.text.Text.NEW_LINE;
import static org.spongepowered.api.text.Text.of;
import static org.spongepowered.api.text.format.TextColors.*;
import static org.spongepowered.api.text.format.TextStyles.BOLD;
import static org.spongepowered.api.text.format.TextStyles.ITALIC;

public class TabListHandler {

    private Game game = Sponge.getGame();
    private PHub instance;

    public TabListHandler(PHub instance) {
        this.instance = instance;

        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> game.getServer().getOnlinePlayers().forEach(this::update))
                .interval(10, TimeUnit.SECONDS)
                .name("Hub Tab List updater")
                .submit(instance);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event, @First Player player) {
        update(player);
    }

    private void update(Player player) {
        TabList tab = player.getTabList();

        val header = of(
                NEW_LINE,
                GREEN, BOLD, "***", RESET, TextStyles.RESET, DARK_GREEN, " TASSUCRAFT ",GREEN, BOLD, "***", NEW_LINE,
                RESET, TextStyles.RESET, NEW_LINE,
                GREEN, "YOU ARE PLAYING ON ", DARK_GREEN, "LOBBY-1", NEW_LINE,
                GREEN, " WITH ", DARK_GREEN, getPlayersInThisServer(), GREEN, " OTHER PLAYERS", NEW_LINE,
                GREEN, "THERE ARE ", BOLD, DARK_GREEN, BungeeTracker.getInstance().getCount(), TextStyles.RESET, RESET,
                GREEN, " PLAYERS GLOBALLY ONLINE",
                NEW_LINE
        );

        Text footer = of(GRAY, ITALIC, "(not queued)");

        val queue = Optional
                .ofNullable(instance.getQueueDataMap().get(player.getUniqueId()))
                .orElse(new QueueData());

        if (!queue.getQueue().isEmpty()) {
            footer = of(
                    NEW_LINE,
                    GREEN, "Queued for ", DARK_GREEN, queue.getQueue(), GREEN, ".", NEW_LINE,
                    GREEN, "Current position is ", DARK_GREEN, queue.getPosition(),
                    GREEN, " out of ", DARK_GREEN, queue.getLength(), GREEN, ".", NEW_LINE
            );
        }

        tab.setHeaderAndFooter(
                header,
                footer
        );
    }

    private Integer getPlayersInThisServer() {
        return game.getServer().getOnlinePlayers().size() - 1;
    }
}
