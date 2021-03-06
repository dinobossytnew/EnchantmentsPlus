package de.geolykt.enchantments_plus.enchantments;

import static de.geolykt.enchantments_plus.enums.Tool.BOW;
import static org.bukkit.inventory.EquipmentSlot.HAND;
import static org.bukkit.inventory.EquipmentSlot.OFF_HAND;

import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import de.geolykt.enchantments_plus.CustomEnchantment;
import de.geolykt.enchantments_plus.Storage;
import de.geolykt.enchantments_plus.arrows.EnchantedArrow;
import de.geolykt.enchantments_plus.arrows.enchanted.MultiArrow;
import de.geolykt.enchantments_plus.enums.BaseEnchantments;
import de.geolykt.enchantments_plus.enums.Hand;
import de.geolykt.enchantments_plus.enums.Tool;
import de.geolykt.enchantments_plus.util.Utilities;

public class Spread extends CustomEnchantment {

    public static final int ID = 57;

    @Override
    public Builder<Spread> defaults() {
        return new Builder<>(Spread::new, ID)
            .maxLevel(5)
            .loreName("Spread")
            .probability(0)
            .enchantable(new Tool[]{BOW})
            .conflicting(Burst.class)
            .description("Fires an array of arrows simultaneously")
            .cooldown(0)
            .power(1.0)
            .handUse(Hand.RIGHT)
            .base(BaseEnchantments.SPREAD);
    }

    @Override
    public boolean onProjectileLaunch(ProjectileLaunchEvent evt, int level, boolean usedHand) {
        Arrow originalArrow = (Arrow) evt.getEntity();
        Player player = (Player) originalArrow.getShooter();
        ItemStack hand = Utilities.usedStack(player, usedHand);
        MultiArrow ar = new MultiArrow(originalArrow);
        EnchantedArrow.putArrow(originalArrow, ar, player);

        Bukkit.getPluginManager().callEvent(
            Storage.COMPATIBILITY_ADAPTER.ConstructEntityShootBowEvent(player, hand, null, originalArrow,
                    usedHand ? HAND : OFF_HAND, (float) originalArrow.getVelocity().length(), false));

        Utilities.damageTool(player, (int) Math.round(level / 2.0 + 1), usedHand);

        for (int i = 0; i < (int) Math.round(power * level * 4); i++) {
            Vector v = originalArrow.getVelocity();
            v.setX(v.getX() + Math.max(Math.min(Storage.rnd.nextGaussian() / 8, 0.75), -0.75));
            v.setZ(v.getZ() + Math.max(Math.min(Storage.rnd.nextGaussian() / 8, 0.75), -0.75));
            Arrow arrow = player.getWorld().spawnArrow(
                player.getEyeLocation().add(player.getLocation().getDirection().multiply(1.0)), v, 1, 0);
            arrow.setShooter(player);
            arrow.setVelocity(v.normalize().multiply(originalArrow.getVelocity().length()));
            arrow.setFireTicks(originalArrow.getFireTicks());
            arrow.setKnockbackStrength(originalArrow.getKnockbackStrength());
            EntityShootBowEvent event = Storage.COMPATIBILITY_ADAPTER.ConstructEntityShootBowEvent(player, hand,
                    null, arrow, usedHand ? HAND : OFF_HAND, (float) arrow.getVelocity().length(), false);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                arrow.remove();
                return false;
            }
            arrow.setMetadata("ze.arrow", new FixedMetadataValue(Storage.enchantments_plus, null));
            arrow.setCritical(originalArrow.isCritical());
            EnchantedArrow.putArrow(originalArrow, new MultiArrow(originalArrow), player);
        }
        return true;
    }
}
