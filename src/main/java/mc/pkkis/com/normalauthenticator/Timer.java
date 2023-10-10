package mc.pkkis.com.normalauthenticator;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class Timer{
    public static int count = 0;
    public static int count1 = 0;

    public static void countdown(){
        count = NormalAuthenticator.keytime;
        if(NormalAuthenticator.typeo) {

            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    if(count==0){
                        NormalAuthenticator.keystart();
                        countdown();
                        this.cancel();
                    }
                    NormalAuthenticator.time(count);
                    count--;
                }
            };
            task.runTaskTimer(NormalAuthenticator.getPlugin(), 0L, 20L);
        }
    }
    public static void countdown1(){
        count1 = NormalAuthenticator.arntime;
        if(NormalAuthenticator.arn) {
            BukkitRunnable task = new BukkitRunnable() {
                public void run() {
                    if(count1==0){
                        NormalAuthenticator.noti();
                        countdown1();
                        this.cancel();
                    }

                    count1--;
                }
            };
            task.runTaskTimer(NormalAuthenticator.getPlugin(), 0L, 20L);
        }
    }
}