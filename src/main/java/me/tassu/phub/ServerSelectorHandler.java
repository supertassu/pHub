package me.tassu.phub;

import com.github.xemiru.sponge.boxboy.Boxboy;
import com.github.xemiru.sponge.boxboy.Menu;
import com.github.xemiru.sponge.boxboy.button.ActionButton;
import com.github.xemiru.sponge.boxboy.button.Button;
import com.github.xemiru.sponge.boxboy.button.DummyButton;
import com.github.xemiru.sponge.boxboy.util.MenuPattern;
import com.google.common.collect.Lists;
import lombok.val;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.EnchantmentData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.action.InteractEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.enchantment.Enchantment;
import org.spongepowered.api.item.enchantment.EnchantmentTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class ServerSelectorHandler {

    private Menu menu = getMenu();

    private Button getWipButtonFor(ItemStack itemStack) {
        return ActionButton.of(
                itemStack,
                ctx -> ctx.getClicker().sendMessage(Text.of(TextColors.RED, TextStyles.ITALIC, "Not yet.... "))
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
                .setButton('B', getWipButtonFor(getSurvivalItem()))
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
}