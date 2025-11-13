ALTER TABLE chat_participants
  ADD CONSTRAINT uk_chat_participants_room_user UNIQUE (chat_room_id, user_id);