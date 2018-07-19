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

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import lombok.Getter;
import lombok.val;
import me.tassu.phub.util.BungeeTracker;
import me.tassu.phub.util.QueueData;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.potion.PotionEffect;
import org.spongepowered.api.effect.potion.PotionEffectTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.action.FishingEvent;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.util.Tristate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Plugin(id = "phub", name = "PHub", description = "the hub is awesome", version = "1.0.0")
public class PHub {

    @Getter private flavor.pie.bungeelib.BungeeLib bungeeLib;

    @Inject private Game game;
    @Inject private Logger logger;

    @Inject private PluginContainer container;

    @Getter private Map<UUID, QueueData> queueDataMap = Maps.newHashMap();

    @Listener
    public void onServerStart(GameStartingServerEvent event) {
        logger.info("Preparing to take over the world!");

        bungeeLib = new flavor.pie.bungeelib.BungeeLib(container);

        bungeeLib.getChan().addListener(Platform.Type.SERVER, (data, connection, side) -> {
            if (data.available() < "QueueUpdate".getBytes().length) return;
            val subChannel = data.readUTF();
            if (!subChannel.equalsIgnoreCase("QueueUpdate")) return;

            try {
                //noinspection InfiniteLoopStatement - will throw an exception
                while (true) {
                    val uuid = UUID.fromString(data.readUTF());
                    queueDataMap.put(uuid, new QueueData(data.readUTF(), data.readUTF(), data.readUTF()));
                }
            } catch (IndexOutOfBoundsException ignored) {}
        });

        new BungeeTracker(this);

        game.getEventManager().registerListeners(this, new ServerSelectorHandler(this));
        game.getEventManager().registerListeners(this, new TabListHandler(this));
        game.getEventManager().registerListeners(this, new JumpPadHandler());
        game.getEventManager().registerListeners(this, new ScoreboardHandler(this));
        game.getEventManager().registerListeners(this, new CheckpointHandler());
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        logger.info("kthxbye.");
    }

    private void giveItems(Player player) {
        player.getInventory().clear();

        val slots = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).slots().iterator();

        slots.next().set(generateCompass());
        slots.next().set(generateChest());
    }

    private void giveEffects(Player player) {
        val effects = player.getOrCreate(PotionEffectData.class).orElseThrow(IllegalArgumentException::new);

        effects.removeAll(it -> true);

        effects.addElement(PotionEffect.builder()
                .potionType(PotionEffectTypes.INVISIBILITY)
                .particles(false)
                .ambience(true)
                .duration(Integer.MAX_VALUE)
                .build());

        player.offer(effects);
    }

    @Listener(order = Order.LATE)
    public void onJoin(ClientConnectionEvent.Join event, @First Player player) {
        val world = game.getServer().getWorld(game.getServer().getDefaultWorld()
                .orElseThrow(IllegalStateException::new).getUniqueId()).orElseThrow(IllegalArgumentException::new);

        player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
        player.offer(Keys.IS_FLYING, false);
        player.offer(Keys.CAN_FLY, false);

        player.setLocation(world.getSpawnLocation().add(0.5, 0.5, 0.5));
        player.setRotation(new Vector3d(0, 0, 90));

        this.giveEffects(player);
        this.giveItems(player);

        game.getScheduler().createTaskBuilder()
                .name("Firework Delay")
                .delay(2, TimeUnit.SECONDS)
                .execute(() -> world.spawnParticles(createFireworks(), player.getLocation().getPosition()))
                .submit(this);

        player.sendTitle(Title.builder()
                .subtitle(Text.of(TextColors.GRAY, TextStyles.ITALIC, "Welcome back, adventurer..."))
                .fadeIn(1)
                .stay(100)
                .fadeOut(100)
                .build());
    }

    private ParticleEffect createFireworks() {
        return ParticleEffect.builder()
                .type(ParticleTypes.FIREWORKS_SPARK)
                .quantity(150)
                .offset(new Vector3d(1, 1, 1))
                .build();
    }

    @Listener
    public void onDropItem(DropItemEvent event, @First Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) return;

        event.setCancelled(true);
    }

    private ItemStack generateCompass() {
        val item = ItemStack.builder()
                .itemType(ItemTypes.COMPASS)
                .quantity(1)
                .build();

        item.offer(Keys.DISPLAY_NAME, Text.of(
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::",
                TextStyles.RESET, TextColors.AQUA, " Quick Connect ",
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::"
                ));

        return item;
    }

    private ItemStack generateChest() {
        val item = ItemStack.builder()
                .itemType(ItemTypes.CHEST)
                .quantity(1)
                .build();

        item.offer(Keys.DISPLAY_NAME, Text.of(
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::",
                TextStyles.RESET, TextColors.GREEN, " Cosmetics ",
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::"
        ));

        return item;
    }

    @Listener
    public void onDamageEntity(DamageEntityEvent event, @First DamageSource damageSource) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        val player = (Player) event.getTargetEntity();

        event.setCancelled(true);

        if (damageSource == DamageSources.VOID) {
            val world = game.getServer().getWorld(game.getServer().getDefaultWorld()
                    .orElseThrow(IllegalStateException::new).getUniqueId()).orElseThrow(IllegalArgumentException::new);

            player.setLocation(world.getSpawnLocation().add(0.5, 0.5, 0.5));
            player.setRotation(new Vector3d(0, 0, 90));
        }
    }

    @Listener
    public void onChangeGameMode(ChangeGameModeEvent.TargetPlayer event) {
        val player = event.getTargetEntity();
        val effects = player.getOrCreate(PotionEffectData.class).orElseThrow(IllegalArgumentException::new);
        effects.removeAll(it -> true);
        player.offer(effects);

        if (event.getGameMode() == GameModes.ADVENTURE) {
            giveEffects(player);
            giveItems(player);
        }
    }

    @Listener
    public void onChangeInventory(ChangeInventoryEvent event, @First Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) return;
        if (event instanceof ChangeInventoryEvent.Held) return;
        event.setCancelled(true);
    }

    public PluginContainer getContainer() {
        return container;
    }
}
