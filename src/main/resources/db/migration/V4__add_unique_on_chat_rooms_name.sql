ALTER TABLE chat_rooms
  ADD CONSTRAINT uk_chat_rooms_name UNIQUE (name);