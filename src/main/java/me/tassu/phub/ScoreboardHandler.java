package me.tassu.phub;

import lombok.val;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scoreboard.critieria.Criteria;
import org.spongepowered.api.scoreboard.displayslot.DisplaySlots;
import org.spongepowered.api.scoreboard.objective.Objective;
import org.spongepowered.api.scoreboard.objective.displaymode.ObjectiveDisplayModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ScoreboardHandler {

    @Listener
    public void onClientConnectionJoin(ClientConnectionEvent.Join event, @First Player player) {
        val scoreboard = player.getScoreboard();

        scoreboard.getObjectives().forEach(scoreboard::removeObjective);

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
        objective.getOrCreateScore(Text.of(Text.of(TextColors.DARK_GREEN, "www.tassu.me"))).setScore(-5);

        scoreboard.addObjective(objective);
        scoreboard.updateDisplaySlot(objective, DisplaySlots.SIDEBAR);

        player.setScoreboard(scoreboard);
    }
}
