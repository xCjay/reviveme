package xyz.hexium.reviveme;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RevivalSystem {
    private ReviveMe plugin;
    private Map<Player, Integer> revivalTimers; // Track time players have been in the radius
    private BukkitTask revivalTask;

    private boolean playerInRing;

    public RevivalSystem(ReviveMe plugin) {
        this.plugin = plugin;
        this.revivalTimers = new HashMap();
    }

    public void startRevivalSystem(Player knockedPlayer, int radius, int revivalTimeInSeconds) {
        plugin.getLogger().info("Starting revival system for " + knockedPlayer.getName());

        revivalTask = new BukkitRunnable() {
            @Override
            public void run() {
                boolean playerInRing = false; // Initialize the flag

                List<Player> nearbyPlayers = knockedPlayer.getNearbyEntities(radius, radius, radius).stream()
                        .filter(entity -> entity instanceof Player)
                        .map(entity -> (Player) entity)
                        .filter(player -> !player.equals(knockedPlayer)) // Exclude the knockedPlayer
                        .collect(Collectors.toList());

                for (Player nearbyPlayer : nearbyPlayers) {
                    if (!revivalTimers.containsKey(nearbyPlayer)) {
                        revivalTimers.put(nearbyPlayer, 0);
                    } else {
                        int timeElapsed = revivalTimers.get(nearbyPlayer);
                        timeElapsed++;
                        plugin.getLogger().info(nearbyPlayer.getName() + " has been in " + knockedPlayer.getName() + "'s revival ring for " + timeElapsed + " seconds.   " + (revivalTimeInSeconds-timeElapsed) + " more seconds required.");

                        // If a player has been in the radius for the required time, trigger revival logic
                        if (timeElapsed >= revivalTimeInSeconds) {
                            // Perform the revival logic here
                            // For example: revive the knockedPlayer
                            revivePlayer(knockedPlayer);

                            // Remove the player from the timers map
                            revivalTimers.remove(nearbyPlayer);
                        } else {
                            revivalTimers.put(nearbyPlayer, timeElapsed);
                        }
                    }

                    // Check if the nearbyPlayer is within the revival ring
                    if (nearbyPlayer.getLocation().distance(knockedPlayer.getLocation()) <= radius) {
                        playerInRing = true;
                    }
                }

                // After processing all nearby players, you can use the 'playerInRing' flag to show particles
                showRevivalParticleRing(knockedPlayer, radius, playerInRing);
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run the task every 20 ticks (1 second)
    }




    private void showRevivalParticleRing(Player player, int radius, boolean isInRevivalRing) {
        World world = player.getWorld();
        Location playerLocation = player.getLocation();

        for (double t = 0; t < 2 * Math.PI; t += Math.PI / 20) {
            double x = radius * Math.cos(t);
            double z = radius * Math.sin(t);

            Location particleLocation = playerLocation.clone().add(x, 0, z);

            if (isInRevivalRing) {
                // If the player is in the revival ring, use green particles
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(0, 255, 0), 1);
                world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 1, dustOptions);
            } else {
                // Use a different color (e.g., red) for players outside the ring
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1);
                world.spawnParticle(Particle.REDSTONE, particleLocation, 1, 0, 0, 0, 1, dustOptions);
            }
        }
    }

    // Add a method to cancel the revival system
    public void cancelRevivalSystem() {
        if (revivalTask != null) {
            revivalTask.cancel();
        }
    }

    // Implement your revivePlayer method as needed
    private void revivePlayer(Player player) {
        plugin.revivePlayer(player, false);
    }
}
