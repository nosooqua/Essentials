package net.essentialsx.discordlink;

import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.metrics.MetricsWrapper;
import net.essentialsx.api.v2.services.discord.EssentialsDiscordAPI;
import net.essentialsx.api.v2.services.discord.InteractionException;
import net.essentialsx.discord.EssentialsDiscord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.earth2me.essentials.I18n.tl;

public class EssentialsDiscordLink extends JavaPlugin {
    private final static Logger logger = Logger.getLogger("EssentialsDiscordLink");
    private transient IEssentials ess;
    private transient EssentialsDiscord essDiscord;
    private transient MetricsWrapper metrics = null;

    private EssentialsDiscordAPI api;
    private DiscordLinkSettings settings;
    private AccountStorage accounts;

    @Override
    public void onEnable() {
        ess = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
        essDiscord = (EssentialsDiscord) getServer().getPluginManager().getPlugin("EssentialsDiscord");
        if (ess == null || !ess.isEnabled() || essDiscord == null || !essDiscord.isEnabled()) {
            setEnabled(false);
            return;
        }
        if (!getDescription().getVersion().equals(ess.getDescription().getVersion())) {
            getLogger().log(Level.WARNING, tl("versionMismatchAll"));
        }

        api = getServer().getServicesManager().load(EssentialsDiscordAPI.class);

        settings = new DiscordLinkSettings();
        ess.addReloadListener(settings);
        try {
            accounts = new AccountStorage(this);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to create link accounts file", e);
            setEnabled(false);
            return;
        }

        if (!(api.getInteractionController().getCommand("link") instanceof LinkInteractionCommand)) {
            try {
                api.getInteractionController().registerCommand(new AccountInteractionCommand(accounts));
                api.getInteractionController().registerCommand(new LinkInteractionCommand(accounts));
                api.getInteractionController().registerCommand(new UnlinkInteractionCommand(accounts));
            } catch (InteractionException e) {
                e.printStackTrace();
                setEnabled(false);
                return;
            }
        }

        if (metrics == null) {
            //TODO
        }
    }

    @Override
    public void onDisable() {
        if (accounts != null) {
            accounts.shutdown();
        }
    }

    public IEssentials getEss() {
        return ess;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        metrics.markCommand(command.getName(), true);
        return ess.onCommandEssentials(sender, command, label, args, EssentialsDiscordLink.class.getClassLoader(), "net.essentialsx.discordlink.Command", "essentials.", accounts);
    }
}