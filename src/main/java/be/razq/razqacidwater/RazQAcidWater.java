package be.razq.razqacidwater;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class RazQAcidWater extends JavaPlugin implements Listener {

    private int initialDamage = 1;
    private int maxDamage = 10;
    private int currentDamage = initialDamage;
    private boolean enableMessages = true;
    private double damageMultiplier = 1.0;

    @Override
    public void onEnable() {
        getLogger().info("RazQAcidWater has been enabled!");


        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();
        saveDefaultConfig();
        initialDamage = getConfig().getInt("initialDamage", 1);
        maxDamage = getConfig().getInt("maxDamage", 10);
        enableMessages = getConfig().getBoolean("enableMessages", true);
        damageMultiplier = getConfig().getDouble("damageMultiplier", 1);

        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().runTaskTimer(this, this::applyDamage, 20L, 20L);
    }

    @Override
    public void onDisable() {
        getLogger().info("RazQAcidWater has been disabled!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        applyBlindness(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    public void applyDamage() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getLocation().getBlock().isLiquid()) {
                player.damage(currentDamage * damageMultiplier);
                sendDamageMessages(player);
                if (currentDamage < maxDamage) {
                    currentDamage++;
                }
                applyBlindness(player);
            } else {
                currentDamage = initialDamage;
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            }
        }
    }

    private void applyBlindness(Player player) {
        if (player.getLocation().getBlock().isLiquid()) {
            Bukkit.getScheduler().runTask(this, () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1), true);
            });
        }
    }

    private void sendDamageMessages(Player player) {
        if (!enableMessages)
            return;

        double healthPercentage = player.getHealth() / player.getMaxHealth() * 100;
        int veryHighThreshold = getConfig().getInt("messageThresholds.damage4Threshold", 20);
        int highThreshold = getConfig().getInt("messageThresholds.damage3Threshold", 40);
        int mediumThreshold = getConfig().getInt("messageThresholds.damage2Threshold", 60);
        int lowThreshold = getConfig().getInt("messageThresholds.damage1Threshold", 80);

        if (healthPercentage > lowThreshold) {
            player.sendTitle("", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.damage1", "")), 5, 40, 5);
        } else if (healthPercentage > mediumThreshold) {
            player.sendTitle("", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.damage2", "")), 5, 40, 5);
        } else if (healthPercentage > highThreshold) {
            player.sendTitle("", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.damage3", "")), 5, 40, 5);
        } else if (healthPercentage > veryHighThreshold) {
            player.sendTitle("", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.damage4", "")), 5, 40, 5);
        } else {
            player.sendTitle("", ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.damage5", "")), 5, 40, 5);
        }
    }
}