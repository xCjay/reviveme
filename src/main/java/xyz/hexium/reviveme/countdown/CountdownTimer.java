package xyz.hexium.reviveme.countdown;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import xyz.hexium.reviveme.RevivalSystem;
import xyz.hexium.reviveme.ReviveMe;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CountdownTimer {

    private final long millisInFuture;
    private final Timer timer;


    public Player playerAttached;

    public CountdownTimer(long millisInFuture, Player player) {
        this.millisInFuture = millisInFuture;

        this.timer = new Timer();
        this.playerAttached = player;
    }

    public void start() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ReviveMe.getInstance().getLogger().info("Timer finished for" + playerAttached.getName());
                List<MetadataValue> metadata = playerAttached.getMetadata("RevivalSystem");
                if (!metadata.isEmpty()) {
                    RevivalSystem revivalSystem = (RevivalSystem) metadata.get(0).value();

                    // Now that you have the revivalSystem, you can cancel it
                    assert revivalSystem != null;
                    revivalSystem.cancelRevivalSystem();
                }

                ReviveMe.getInstance().fullKillPlayer(playerAttached);
            }
        }, millisInFuture);
    }

    public void cancel() {
        timer.cancel();
    }
}
