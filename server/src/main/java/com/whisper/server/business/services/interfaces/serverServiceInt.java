package com.whisper.server.business.services.interfaces;

import com.whisper.server.persistence.entities.User;

import java.util.List;

public interface serverServiceInt {
    public void startServer();
    public void stopServer();
    public void Announcement(String s);
    public List<User> viewClients();
}