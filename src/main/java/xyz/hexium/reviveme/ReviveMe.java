package xyz.hexium.reviveme;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.hexium.reviveme.commands.CheckKnocked;
import xyz.hexium.reviveme.commands.Configs;
import xyz.hexium.reviveme.commands.Knock;
import xyz.hexium.reviveme.commands.Revive;
import xyz.hexium.reviveme.countdown.CountdownTimer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class ReviveMe extends JavaPlugin implements Listener {

    public ArrayList<CountdownTimer> timers;

    private static boolean banOnDeath;
    private static int reviveTime;
    private static long reviveTimeLimit;
    private static int reviveRadius;

    private final NamespacedKey KNOCKED_KEY = new NamespacedKey(this, "knocked");

    @Override
    public void onEnable() {
        getLogger().info("ReviveMe has been enabled! :)");

        //register events
        getServer().getPluginManager().registerEvents(this, this);



        //register commands
        this.getCommand("revive").setExecutor(new Revive());
        this.getCommand("knock").setExecutor(new Knock());
        this.getCommand("checkknocked").setExecutor(new CheckKnocked());
        this.getCommand("rmconfig").setExecutor(new Configs(this));

        timers = new ArrayList<>();



        File configFile = new File(getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        banOnDeath = config.getBoolean("banOnDeath");
        reviveTime = (config.getInt("reviveTime"));
        reviveRadius = config.getInt("reviveRadius");
        reviveTimeLimit = config.getLong("reviveTimeLimit");
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //reload configs
    public void reloadConfigs() {
        File configFile = new File(getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        banOnDeath = config.getBoolean("banOnDeath");
        reviveTime = (config.getInt("reviveTime"));
        reviveRadius = config.getInt("reviveRadius");
        reviveTimeLimit = config.getLong("reviveTimeLimit");
        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    //On player join listener
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getLogger().info(player.getName() + " has joined! -ReviveMe");

        //add "knocked" nbt boolean to players without one (default false)
        if (!hasKnockedTag(player)) {
            setKnockedTag(player, 0);
        }
    }

    //On player death listener
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        // if player is already knocked return
        if (hasKnockedTag(event.getEntity())) {
            return;
        }
        event.setCancelled(true);

        Location location = event.getPlayer().getLocation();

        Player player = event.getEntity();
//        getLogger().info(player.getName() + " has died! -ReviveMe");



        knockPlayer(player, location);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (hasKnockedTag(player)) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // Ensure the player only falls vertically (Y-axis)
            to.setX(from.getX());
            to.setZ(from.getZ());

            event.setTo(to);
        }
    }


    public boolean hasKnockedTag(Player player){
        PersistentDataContainer container = player.getPersistentDataContainer();

        try {
            return container.get(KNOCKED_KEY, PersistentDataType.BYTE) == 1;
        } catch (NullPointerException e) {
            // Handle the exception, e.g., log an error message
            getLogger().severe("Failed to get 'knocked' tag: " + e.getMessage());
            return false;
        }
    }

    public void setKnockedTag(Player player, int value) {
        try {
            player.getPersistentDataContainer().set(KNOCKED_KEY, PersistentDataType.BYTE, (byte) value);

//            getLogger().info("set tag to: " + player.getPersistentDataContainer().get(KNOCKED_KEY, PersistentDataType.BYTE));
        } catch (IllegalArgumentException e) {
            // Handle the exception, e.g., log an error message
            getLogger().severe("Failed to set 'knocked' tag: " + e.getMessage());
        }
    }



    public void knockPlayer(Player player, Location deathLocation) {
        // Set "knocked" NBT boolean to true
        setKnockedTag(player, 1);

        // Start the revival system
        RevivalSystem revivalSystem = new RevivalSystem(this);
        revivalSystem.startRevivalSystem(player, reviveRadius, reviveTime);
        player.setMetadata("RevivalSystem", new FixedMetadataValue(this, revivalSystem));

        int maxDistance = 10; // Adjust this distance if needed

        Location teleportLocation = null;
        boolean foundSolidBlock = false;

        for (int i = 1; i <= maxDistance; i++) {
            Location testLocation = deathLocation.clone().subtract(0, i, 0);
            Block blockBelow = testLocation.getBlock();

            if (blockBelow.getType() != Material.AIR) {
                // Found a solid block, adjust the Y coordinate
                teleportLocation = testLocation.clone().add(0.5, 1, 0.5);
                foundSolidBlock = true;
                break;
            }
        }

        // Teleport the player
        if (foundSolidBlock && teleportLocation != null) {
            player.teleport(teleportLocation);
        } else {
            // If no solid block is found, teleport the player to their original death location
            player.teleport(deathLocation);
        }

        // The rest of your code to freeze the player and start the timer
        player.setInvulnerable(true);
        player.setCollidable(false);
        player.setGlowing(true);
        player.setSilent(true);
        player.setGameMode(GameMode.ADVENTURE);

        CountdownTimer timer = new CountdownTimer(reviveTimeLimit * 1000, player);
        timers.add(timer);
        timer.start();
        player.sendTitle("Knocked!", "You have " + reviveTimeLimit + " seconds to be revived!", 10, 70, 20);
    }


    public void revivePlayer(Player player, boolean fullRevive){

        //unknock player
        setKnockedTag(player, 0);

        Iterator<CountdownTimer> iterator = timers.iterator();
        while (iterator.hasNext()) {
            CountdownTimer timer = iterator.next();
            if (timer.playerAttached == player) {
                timer.cancel();
                iterator.remove();
            }
        }

        List<MetadataValue> metadata = player.getMetadata("RevivalSystem");
        if (!metadata.isEmpty()) {
            RevivalSystem revivalSystem = (RevivalSystem) metadata.get(0).value();

            // Now that you have the revivalSystem, you can cancel it
            assert revivalSystem != null;
            revivalSystem.cancelRevivalSystem();
        }


        //set players knocked nbt to false
        setKnockedTag(player, 0);

        getLogger().info("Revived player: " + player.getName());





        //remove effects applied on death
        player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW);
        player.setInvulnerable(false);
        player.setCollidable(true);
        player.setGlowing(false);
        player.setSilent(false);
        player.setGravity(true);

        player.setGameMode(org.bukkit.GameMode.SURVIVAL);

        //set player hp to half a heart
        if(fullRevive){
            player.setHealth(20);
        } else {
            player.setHealth(1);
        }
    }

    public void fullKillPlayer(Player player){
        if (banOnDeath){
            Bukkit.getScheduler().runTask(ReviveMe.getInstance(), () -> {
                //ban player
                Bukkit.getServer().banIP(player.getAddress().getAddress().getHostAddress());
                Bukkit.getServer().getBanList(org.bukkit.BanList.Type.NAME).addBan(player.getName(), "You have been banned for dying.", null, null);

                //kick player
                player.kickPlayer("You have been banned for dying.");
            });
            return;
        }





        Bukkit.getScheduler().runTask(ReviveMe.getInstance(), () -> {
            player.setHealth(0);

            //send killed title
            player.sendTitle("Killed!", "You have been killed!", 10, 70, 20);

            revivePlayer(player, true);

            getLogger().info("Player fully killed! -ReviveMe");

        });
    }



    @EventHandler
    public void onPlayerLeave(org.bukkit.event.player.PlayerQuitEvent event) {
        Player player = event.getPlayer();

        //remove player from revival system
        List<MetadataValue> metadata = player.getMetadata("RevivalSystem");
        if (!metadata.isEmpty()) {
            RevivalSystem revivalSystem = (RevivalSystem) metadata.get(0).value();

            // Now that you have the revivalSystem, you can cancel it
            assert revivalSystem != null;
            revivalSystem.cancelRevivalSystem();
        }

        //remove player from timers
        Iterator<CountdownTimer> iterator = timers.iterator();
        while (iterator.hasNext()) {
            CountdownTimer timer = iterator.next();
            if (timer.playerAttached == player) {
                timer.cancel();
                iterator.remove();
            }
        }

        //remove player from knocked
        setKnockedTag(player, 0);
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ReviveMe getInstance() {
        return getPlugin(ReviveMe.class);
    }
}
