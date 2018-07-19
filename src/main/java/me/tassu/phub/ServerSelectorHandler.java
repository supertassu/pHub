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
import com.github.xemiru.sponge.boxboy.Boxboy;
import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.ActionButton;
import com.github.xemiru.sponge.boxboy.button.Button;
import com.github.xemiru.sponge.boxboy.button.DummyButton;
import com.github.xemiru.sponge.boxboy.util.MenuPattern;
import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

@RequiredArgsConstructor
public class ServerSelectorHandler {

    private Menu menu = getMenu();

    private final PHub instance;

    private Game game = Sponge.getGame();

    private void queue(Player player, String server) {
        instance.getBungeeLib()
                .getChan()
                .sendTo(player, buf -> buf.writeUTF("JoinQueue")
                        .writeUTF(player.getUniqueId().toString())
                        .writeUTF(server));

        val world = game.getServer().getWorld(game.getServer().getDefaultWorld()
                .orElseThrow(IllegalStateException::new).getUniqueId()).orElseThrow(IllegalArgumentException::new);

        player.setLocation(new Location<>(world, -425, 40, 567));
        player.setRotation(new Vector3d(0, 0, -90));
    }

    private Button getWipButtonFor(ItemStack itemStack) {
        return ActionButton.of(
                itemStack,
                ctx -> ctx.getClicker().sendMessage(Text.of(TextColors.RED, TextStyles.ITALIC, "Not yet.... "))
        );
    }

    private Button getButtonForQueue(ItemStack itemStack, String queue) {
        return ActionButton.of(
                itemStack,
                ctx -> queue(ctx.getClicker(), queue)
        );
    }

    private ItemStack getSurvivalItem() {
        val item = ItemStack.builder()
                .itemType(ItemTypes.GRASS)
                .quantity(1)
                .build();

        item.offer(Keys.DISPLAY_NAME, Text.of(
                TextStyles.BOLD, TextColors.GREEN, "SURVIVAL"
        ));

        item.offer(Keys.ITEM_LORE, Lists.newArrayList(
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.RESET, "The classic game,"),
                Text.of(TextStyles.RESET, TextColors.RESET, "but with a twist."),
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.RESET, "We've completely"),
                Text.of(TextStyles.RESET, TextColors.RESET, "reworked some of"),
                Text.of(TextStyles.RESET, TextColors.RESET, "the game's most"),
                Text.of(TextStyles.RESET, TextColors.RESET, "important mechanics"),
                Text.of(TextStyles.RESET, TextColors.RESET, "to create a truly"),
                Text.of(TextStyles.RESET, TextColors.RESET, "unique experience."),
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.RESET, "Trust us, it's"),
                Text.of(TextStyles.RESET, TextColors.RESET, "going to be fun."),
                Text.EMPTY,
                Text.of(TextStyles.RESET, TextColors.DARK_GREEN, "Coming soon.")
        ));

        val enchantmentData = item
                .getOrCreate(EnchantmentData.class).orElseThrow(RuntimeException::new);

        enchantmentData.addElement(Enchantment.builder()
                .type(EnchantmentTypes.UNBREAKING)
                .level(1)
                .build());

        item.offer(enchantmentData);

        item.offer(Keys.HIDE_ENCHANTMENTS, true);

        return item;
    }

    private Menu getMenu() {
        val menu = Boxboy.get().createMenu(5, Text.of(
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::",
                TextStyles.RESET, TextColors.AQUA, " Server Selector ",
                TextColors.DARK_GRAY, TextStyles.OBFUSCATED, ":::"
        ));

        new MenuPattern()
                .setButton('A', DummyButton.of(ItemStack.of(ItemTypes.STAINED_GLASS_PANE, 1)))
                .setButton('B', getButtonForQueue(getSurvivalItem(), "survival"))
                .setPattern(
                        "AAAAAAAAA",
                        "A       A",
                        "A   B   A",
                        "A       A",
                        "AAAAAAAAA")
                .apply(menu);

        return menu;
    }

    @Listener
    public void onInteract(InteractEvent event, @First Player player) {
        val optionalHand = player.getItemInHand(HandTypes.MAIN_HAND);
        if (!optionalHand.isPresent()) return;

        val hand = optionalHand.get();
        if (hand.getType() != ItemTypes.COMPASS) return;

        event.setCancelled(true);
        menu.open(player);
    }

    @Listener
    public void onMoveEntity(MoveEntityEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) return;

        val player = (Player) event.getTargetEntity();
        val block = event.getToTransform().getLocation().getBlock();

        if (block.getType() == BlockTypes.PORTAL) {
            queue(player, "survival");
        }

    }

}
