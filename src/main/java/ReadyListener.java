import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;

public class ReadyListener implements EventListener {
    private static final String token = "NzUwMjk0MTYxMDE3NjY3NjI1.X04b4w.S-qbhEZBnS6a83yewyIEh65s3sg";
    public static void main(String[] args) throws LoginException {
        JDABuilder builder = JDABuilder.createDefault(token);
//        JDABuilder builder = JDABuilder.createLight(token);
        builder.setActivity(Activity.playing("IntelliJ IDEA"));
        builder.addEventListeners(new ReadyListener());
        builder.addEventListeners(new MessageListener());
        JDA jda = builder.build();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if(event instanceof ReadyEvent){
            System.out.println("API is Ready");
        }else if(event instanceof ShutdownEvent){
            System.out.println("Shutting Down...");
        }
    }
}
