package com.whisper.server.model.repo;

import com.whisper.server.model.*;
import com.whisper.server.model.enums.Mode;
import com.whisper.server.model.enums.Status;

import java.sql.SQLException;
import java.util.List;

public interface RepositoryInterface {
    List<User> getUsers() throws SQLException;

    User getUserById(int userId) throws SQLException;

    User getUserByPhone(String phoneNumber) throws SQLException;

    List<User> getUsersContainPhone(String subPhoneNumber) throws SQLException;

    Mode getUserMode(int userId) throws SQLException;

    Status getUserStatus(int userId) throws SQLException;

    List<Contact> getContactsForUser(int userId) throws SQLException;

    List<RoomChat> getRoomChatsForUser(int userId) throws SQLException;

    List<Notification> getNotificationForUser(int userId) throws SQLException;

    List<Message> getMessagesForUser(int userId) throws SQLException;

    List<Message> getMessagesFromUser(int userId) throws SQLException;

    List<Message> getMessagesForRoomChat(int roomChatId) throws SQLException;

    List<PendingRequest> getUserPendingRequests(int toUserId) throws SQLException;

    List<PendingRequest> getUserSendingRequest(int fromUserId) throws SQLException;

    List<RoomMember> getRoomChatMembers(int roomChatId) throws SQLException;

    boolean createUser(User user) throws SQLException;
    boolean updateUser(User newUser);
    boolean deleteUserById(int userId);
    boolean addContact(int userId, int contactId);

    void closeConnection() throws SQLException;
}
