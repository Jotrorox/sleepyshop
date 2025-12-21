package com.jotrorox.shop.listener;

import com.jotrorox.shop.manager.ShopManager;
import com.jotrorox.shop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignListener implements Listener {
    private final ShopManager manager;
    private static final Component PREFIX = Component.text("[SleepyShop] ", NamedTextColor.BLUE);

    public SignListener(ShopManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String line1 = PlainTextComponentSerializer.plainText().serialize(event.line(0));
        if (line1.equalsIgnoreCase("[Shop]") || line1.equalsIgnoreCase("[SleepyShop]")) {
            Block signBlock = event.getBlock();
            Block chestBlock = findAttachedChest(signBlock);

            if (chestBlock == null) {
                event.getPlayer().sendMessage(PREFIX.append(Component.text("No chest found nearby!", NamedTextColor.RED)));
                return;
            }

            Shop shop = new Shop(signBlock.getLocation(), chestBlock.getLocation(), event.getPlayer().getUniqueId());
            manager.saveShop(shop);

            event.line(0, Component.text("[SleepyShop]", NamedTextColor.BLUE));
            event.getPlayer().sendMessage(PREFIX.append(Component.text("SleepyShop created!", NamedTextColor.GREEN)));
        }
    }

    private Block findAttachedChest(Block signBlock) {
        // If it's a wall sign or hanging sign, check what it's attached to
        if (signBlock.getBlockData() instanceof Directional directional) {
            Block attachedTo = signBlock.getRelative(directional.getFacing().getOppositeFace());
            if (isChest(attachedTo)) return attachedTo;
        }

        // Check all faces around the sign
        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};
        for (BlockFace face : faces) {
            Block relative = signBlock.getRelative(face);
            if (isChest(relative)) return relative;
        }
        return null;
    }

    private boolean isChest(Block block) {
        return block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST || block.getType() == Material.BARREL;
    }
}
