package de.geolykt.enchantments_plus.enchantments;

import static de.geolykt.enchantments_plus.enums.Tool.BOW;
import static de.geolykt.enchantments_plus.enums.Tool.SWORD;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;

import de.geolykt.enchantments_plus.CustomEnchantment;
import de.geolykt.enchantments_plus.arrows.EnchantedArrow;
import de.geolykt.enchantments_plus.arrows.enchanted.SiphonArrow;
import de.geolykt.enchantments_plus.enums.BaseEnchantments;
import de.geolykt.enchantments_plus.enums.Hand;
import de.geolykt.enchantments_plus.enums.Tool;

public class Siphon extends CustomEnchantment {

    public static final int ID = 53;
    public static double ratio = 0.5;
    public static boolean calcAmour = true;

    @Override
    public Builder<Siphon> defaults() {
        return new Builder<>(Siphon::new, ID)
                .maxLevel(4)
                .loreName("Siphon")
                .probability(0)
                .enchantable(new Tool[]{BOW, SWORD})
                .conflicting()
                .description("Drains the health of the mob that you attack, giving it to you")
                .cooldown(0)
                .power(1.0)
                .handUse(Hand.BOTH)
                .base(BaseEnchantments.SIPHON);
    }

    @Override
    public boolean onEntityHit(EntityDamageByEntityEvent evt, int level, boolean usedHand) {
        if (evt.getEntity() instanceof LivingEntity
                && ADAPTER.attackEntity((LivingEntity) evt.getEntity(), (Player) evt.getDamager(), 0)) {
            Player player = (Player) evt.getDamager();
            int difference = 0;
            if (calcAmour) {
                difference = (int) Math.round(.17 * level * power * evt.getFinalDamage());
            } else {
                difference = (int) Math.round(.17 * level * power * evt.getDamage());
            }
            while (difference > 0) {
                if (player.getHealth()+1 <= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    player.setHealth(player.getHealth() + ratio);
                } else {
                    return true;
                }
                difference--;
            }
        }
        return true;
    }

    @Override
    public boolean onEntityShootBow(EntityShootBowEvent evt, int level, boolean usedHand) {
        SiphonArrow arrow = new SiphonArrow((Arrow) evt.getProjectile(), level, power);
        EnchantedArrow.putArrow((Arrow) evt.getProjectile(), arrow, (Player) evt.getEntity());
        return true;
    }
}
