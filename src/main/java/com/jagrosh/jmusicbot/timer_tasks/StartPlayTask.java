package com.jagrosh.jmusicbot.timer_tasks;


import com.jagrosh.jmusicbot.Bot;

import java.util.TimerTask;

public class StartPlayTask extends TimerTask {
    private Bot bot;

    public StartPlayTask(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        System.out.println("[StartPlayTask] started");
    }
}
