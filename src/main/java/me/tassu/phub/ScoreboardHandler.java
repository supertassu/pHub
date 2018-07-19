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

import com.google.common.collect.Lists;
import flavor.pie.bungeelib.BungeeLib;
import lombok.val;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.living.humanoid.ChangeLevelEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ScoreboardHandler {

    private BungeeLib bungeeLib;

    private static final Text ONLINE_PLAYERS_TEXT = Text.of(
            TextColors.GRAY, TextStyles.ITALIC, "Players "
    );

    private static final Text NETWORK_LEVEL_TEXT = Text.of(
            TextColors.GRAY, TextStyles.ITALIC, "Level "
    );

    public ScoreboardHandler(PHub instance) {
        bungeeLib = new BungeeLib(instance.getContainer());

        Sponge.getScheduler().createTaskBuilder()
                .execute(() -> {
                    if (Sponge.getServer().getOnlinePlayers().size() < 1) return;
                    bungeeLib.getGlobalPlayerCount().thenAccept(it -> Sponge.getServer().getOnlinePlayers().forEach(player -> {
                        player.getScoreboard().getTeam("int_level")
                                .ifPresent(team -> team.setSuffix(Text.of(TextColors.RESET,
                                        player.get(Keys.EXPERIENCE_LEVEL).orElse(0))));
                        player.getScoreboard().getTeam("int_online")
                                .ifPresent(team -> team.setSuffix(Text.of(TextColors.RESET, it)));
                    }));
                })
                .interval(10, TimeUnit.SECONDS)
                .name("Hub Scoreboard updater")
                .submit(instance);
    }

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event, @First Player player) {
        val objective = Objective.builder()
                .objectiveDisplayMode(ObjectiveDisplayModes.INTEGER)
                .criterion(Criteria.DUMMY)
                .displayName(Text.of(TextColors.GREEN, "*"))
                .name("sidebar")
                .build();

        objective.getOrCreateScore(Text.of(TextColors.DARK_BLUE)).setScore(-1);
        objective.getOrCreateScore(Text.of(TextColors.GRAY, "Hello, ")).setScore(-2);
        objective.getOrCreateScore(Text.of(player.getName())).setScore(-3);
        objective.getOrCreateScore(Text.of(TextColors.DARK_GREEN)).setScore(-4);
        objective.getOrCreateScore(ONLINE_PLAYERS_TEXT).setScore(-5);
        objective.getOrCreateScore(NETWORK_LEVEL_TEXT).setScore(-6);
        objective.getOrCreateScore(Text.of(TextColors.DARK_RED)).setScore(-7);
        objective.getOrCreateScore(Text.of(Text.of(TextColors.DARK_GREEN, "www.tassu.me"))).setScore(-8);

        val scoreboard = Scoreboard
                .builder()
                .objectives(Collections.singletonList(objective))
                .teams(Lists.newArrayList(
                        Team
                                .builder()
                                .name("int_online")
                                .suffix(Text.of(TextColors.DARK_GRAY, "Unknown"))
                                .members(Collections.singleton(ONLINE_PLAYERS_TEXT))
                                .build(),
                        Team
                                .builder()
                                .name("int_level")
                                .suffix(Text.of(TextColors.RESET, player.get(Keys.EXPERIENCE_LEVEL).orElse(0)))
                                .members(Collections.singleton(NETWORK_LEVEL_TEXT))
                                .build()
                ))
                .build();

        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

        player.setScoreboard(scoreboard);
    }

    @Listener
    // NOT IMPLEMENTED YET
    public void onChangeLevel(ChangeLevelEvent.TargetPlayer event) {
        event.getTargetEntity().getScoreboard().getTeam("int_level")
                .ifPresent(team -> team.setSuffix(Text.of(TextColors.RESET, event.getLevel())));
    }
}
