package com.jagrosh.jmusicbot.timer_tasks;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

public class StartPlayTask extends TimerTask {
    private Bot bot;
    private CommandClient client;
    private String playList;
    private String days;
    private String timezone;
    private String textChannel;
    private String voiceChannel;
    private Logger logger;

    public StartPlayTask(Bot bot, String timezone, String playList, String days, String textChannel, String voiceChannel) {
        logger = LoggerFactory.getLogger("StartPlayTask");
        this.bot = bot;
        for (Object listener : bot.getJDA().getRegisteredListeners()) {
            if (listener instanceof CommandClient) {
                this.client = (CommandClient) listener;
                break;
            }
        }
        this.playList = playList;
        this.days = days;
        this.timezone = timezone;
        this.textChannel = textChannel;
        this.voiceChannel = voiceChannel;
    }

    @Override
    public void run() {
        try {
            // check if today's the day
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(timezone));
            if (days.indexOf(String.valueOf(today.get(Calendar.DAY_OF_WEEK))) < 0) {
                logger.info("StartPlayTask cannot start with playlist " + playList + " because today is " + today);
                return;
            }
            logger.info("StartPlayTask started with playlist " + playList);

            for (Guild guild : bot.getJDA().getGuilds()) {
                Settings s = client.getSettingsFor(guild);
                List<TextChannel> tcs = bot.getJDA().getTextChannelsByName(textChannel, true);
                TextChannel tc = tcs.isEmpty() ? s.getTextChannel(guild) : tcs.get(0);
                logger.info("Wanted text channel " + textChannel + "; Use text channel " + tc.getName() + "(" + tc.getId() + ")");
                List<VoiceChannel> vcs = bot.getJDA().getVoiceChannelByName(voiceChannel, true);
                VoiceChannel vc = vcs.isEmpty() ? s.getVoiceChannel(guild) : vcs.get(0);
                logger.info("Wanted voice channel " + voiceChannel + "; Use voice channel " + vc.getName() + "(" + vc.getId() + ")");
                String pl = (playList == null ? s.getDefaultPlaylist() : playList);
                //tc.sendMessage((new MessageBuilder()).setContent(" Loading... `[" + pl + "]`").build())
                //        .queue(m -> bot.getPlayerManager().loadItemOrdered(guild, pl, new ResultHandler(m, client, guild, pl,false)));

                bot.getPlayerManager().setUpHandler(guild);
                try {
                    guild.getAudioManager().openAudioConnection(vc);
                } catch (PermissionException ex) {
                    tc.sendMessage(client.getError() + " I am unable to connect to **" + vc.getName() + "**!");
                    return;
                }
                PlaylistLoader.Playlist playlist = bot.getPlaylistLoader().getPlaylist(pl);
                if (playlist == null) {
                    tc.sendMessage((new MessageBuilder()).setContent("I could not find `" + pl + ".txt` in the Playlists folder.").build()).queue();
                    return;
                }
                tc.sendMessage(" Welcome! Please enjoy the music! Loading playlist **" + pl + "**... (" + playlist.getItems().size() + " items)").queue(m ->
                {
                    AudioHandler handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
                    playlist.loadTracks(bot.getPlayerManager(),
                            (at) -> {
                                handler.addTrack(new QueuedTrack(at, guild.getOwner().getUser()));
                            },
                            () -> {
                                StringBuilder builder = new StringBuilder(playlist.getTracks().isEmpty()
                                        ? client.getWarning() + " No tracks were loaded!"
                                        : client.getSuccess() + " Loaded **" + playlist.getTracks().size() + "** tracks!");
                                if (!playlist.getErrors().isEmpty())
                                    builder.append("\nThe following tracks failed to load:");
                                playlist.getErrors().forEach(err -> builder.append("\n`[").append(err.getIndex() + 1).append("]` **").append(err.getItem()).append("**: ").append(err.getReason()));
                                String str = builder.toString();
                                if (str.length() > 2000)
                                    str = str.substring(0, 1994) + " (...)";
                                m.editMessage(FormatUtil.filter(str)).queue();
                            });
                    handler.getQueue().shuffle(guild.getOwner().getUser().getIdLong());
                });
            }
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }
}
