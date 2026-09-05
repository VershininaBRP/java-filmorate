package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FriendHelper {
    private final JdbcTemplate jdbcTemplate;

    public FriendHelper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriendRequest(int fromUserId, int toUserId) {
        String checkSql = "SELECT COUNT(*) FROM friend_requests WHERE from_user_id = ? AND to_user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, fromUserId, toUserId);
        if (count != null && count > 0) {
            jdbcTemplate.update("UPDATE friend_requests SET status = 'PENDING', created_at = CURRENT_TIMESTAMP WHERE from_user_id = ? AND to_user_id = ?",
                    fromUserId, toUserId);
        } else {
            jdbcTemplate.update("INSERT INTO friend_requests (from_user_id, to_user_id, status) VALUES (?, ?, 'PENDING')",
                    fromUserId, toUserId);
        }
    }

    public void acceptFriendRequest(int fromUserId, int toUserId) {
        jdbcTemplate.update("UPDATE friend_requests SET status = 'ACCEPTED' WHERE from_user_id = ? AND to_user_id = ?",
                fromUserId, toUserId);
        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'ACCEPTED')",
                fromUserId, toUserId);
    }

    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId);
        jdbcTemplate.update("DELETE FROM friend_requests WHERE (from_user_id = ? AND to_user_id = ?) OR (from_user_id = ? AND to_user_id = ?)",
                userId, friendId, friendId, userId);
    }

    public List<Integer> getFriendIds(int userId) {
        return jdbcTemplate.queryForList(
                "SELECT friend_id FROM friendships WHERE user_id = ? AND status = 'ACCEPTED'",
                Integer.class, userId);
    }

    public List<Integer> getPendingRequests(int userId) {
        return jdbcTemplate.queryForList(
                "SELECT from_user_id FROM friend_requests WHERE to_user_id = ? AND status = 'PENDING'",
                Integer.class, userId);
    }

    public boolean hasPendingRequest(int fromUserId, int toUserId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friend_requests WHERE from_user_id = ? AND to_user_id = ? AND status = 'PENDING'",
                Integer.class, fromUserId, toUserId);
        return count != null && count > 0;
    }

    public boolean areFriends(int userId, int friendId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM friendships WHERE user_id = ? AND friend_id = ? AND status = 'ACCEPTED'",
                Integer.class, userId, friendId);
        return count != null && count > 0;
    }
}