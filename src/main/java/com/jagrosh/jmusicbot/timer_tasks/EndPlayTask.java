package com.jagrosh.jmusicbot.timer_tasks;


import com.jagrosh.jmusicbot.Bot;

import java.util.TimerTask;

public class EndPlayTask extends TimerTask {
    private Bot bot;

    public EndPlayTask(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        System.out.println("[EndPlayTask] started");
    }
}