package com.jagrosh.jmusicbot.timer_tasks;


import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

public class EndPlayTask extends TimerTask {
    private Bot bot;
    private CommandClient client;
    private String days;
    private String timezone;
    private String textChannel;
    private Logger logger;

    public EndPlayTask(Bot bot, String timezone, String days, String textChannel) {
        logger = LoggerFactory.getLogger("EndPlayTask");
        this.bot = bot;
        for (Object listener : bot.getJDA().getRegisteredListeners()) {
            if (listener instanceof CommandClient) {
                this.client = (CommandClient)listener;
                break;
            }
        }
        this.days = days;
        this.timezone = timezone;
        this.textChannel = textChannel;
    }

    @Override
    public void run() {
        try {
            // check if today's the day
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(timezone));
            if (days.indexOf(String.valueOf(today.get(Calendar.DAY_OF_WEEK))) < 0) {
                logger.info("EndPlayTask cannot end playlist because today is " + today);
                return;
            }
            logger.info("EndPlayTask started");

            for (Guild guild : bot.getJDA().getGuilds()) {
                Settings s = client.getSettingsFor(guild);
                List<TextChannel> tcs = bot.getJDA().getTextChannelsByName(textChannel, true);
                TextChannel tc = tcs.isEmpty() ? s.getTextChannel(guild) : tcs.get(0);
                logger.info("Wanted text channel " + textChannel + "; Use text channel " + tc.getName() + "(" + tc.getId() + ")");
                AudioHandler handler = (AudioHandler) guild.getAudioManager().getSendingHandler();
                if (handler != null) {
                    handler.stopAndClear();
                    guild.getAudioManager().closeAudioConnection();
                    tc.sendMessage((new MessageBuilder()).setContent(client.getSuccess() + " Time to stop now. Please come back next time!").build()).queue();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}