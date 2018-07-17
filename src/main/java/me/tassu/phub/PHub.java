package me.tassu.phub;

import com.flowpowered.math.vector.Vector3d;
import com.google.inject.Inject;
import lombok.val;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
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
import org.spongepowered.api.event.cause.entity.damage.source.DamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.event.item.inventory.ChangeInventoryEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.Hotbar;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.concurrent.TimeUnit;


@Plugin(id = "phub", name = "PHub", description = "the hub is awesome", version = "1.0.0")
public class PHub {

    @Inject private Game game;
    @Inject private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Preparing to take over the world!");

        game.getEventManager().registerListeners(this, new ServerSelectorHandler());
        game.getEventManager().registerListeners(this, new ScoreboardHandler());
    }

    @Listener
    public void onGameStopping(GameStoppingEvent event) {
        logger.info("kthxbye.");
    }

    @Listener(order = Order.LATE)
    public void onJoin(ClientConnectionEvent.Join event, @First Player player) {
        val world = game.getServer().getWorld(game.getServer().getDefaultWorld()
                .orElseThrow(IllegalStateException::new).getUniqueId()).orElseThrow(IllegalArgumentException::new);

        player.offer(Keys.GAME_MODE, GameModes.ADVENTURE);
        player.offer(Keys.IS_FLYING, false);
        player.offer(Keys.CAN_FLY, false);

        player.getInventory().clear();
        player.setLocation(world.getSpawnLocation().add(0.5, 0.5, 0.5));

        val effects = player.getOrCreate(PotionEffectData.class).orElseThrow(IllegalArgumentException::new);

        effects.removeAll(it -> true);

        effects.addElement(PotionEffect.builder()
                .potionType(PotionEffectTypes.INVISIBILITY)
                .particles(false)
                .ambience(true)
                .duration(Integer.MAX_VALUE)
                .build());

        player.offer(effects);

        val slots = player.getInventory().query(QueryOperationTypes.INVENTORY_TYPE.of(Hotbar.class)).slots().iterator();

        slots.next().set(generateCompass());
        slots.next().set(generateChest());

        game.getScheduler().createTaskBuilder()
                .name("Firework Delay")
                .delay(2, TimeUnit.SECONDS)
                .execute(() -> world.spawnParticles(createFireworks(), player.getLocation().getPosition()))
                .submit(this);
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
        }
    }

    @Listener
    public void onChangeGameMode(ChangeGameModeEvent event, @First Player player) {
        val effects = player.getOrCreate(PotionEffectData.class).orElseThrow(IllegalArgumentException::new);

        effects.removeAll(it -> true);

        if (event.getGameMode() != GameModes.CREATIVE) {
            effects.addElement(PotionEffect.builder()
                    .potionType(PotionEffectTypes.INVISIBILITY)
                    .particles(false)
                    .ambience(true)
                    .duration(Integer.MAX_VALUE)
                    .build());
        }

        player.offer(effects);
    }

    @Listener
    public void onChangeInventory(ChangeInventoryEvent event, @First Player player) {
        if (player.gameMode().get() == GameModes.CREATIVE) return;
        if (event instanceof ChangeInventoryEvent.Held) return;
        event.setCancelled(true);
    }
}
