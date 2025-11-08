create table chat_rooms(
    chat_room_id bigint not null auto_increment primary key,
    name varchar(50) not null,
    is_group_chat boolean not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp
);

create table chat_participants (
    chat_participant_id bigint not null auto_increment primary key,
    user_id bigint not null,
    chat_room_id bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    constraint fk_participant_user
        foreign key(user_id) references users(user_id)
        on delete cascade,
    constraint fk_participant_chatRoom
        foreign key(chat_room_id) references chat_rooms(chat_room_id)
        on delete cascade
);

create table chat_messages(
    chat_message_id bigint not null auto_increment primary key,
    contents text not null,
    chat_room_id bigint not null,
    user_id bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    constraint fk_message_user
            foreign key(user_id) references users(user_id)
            on delete cascade,
    constraint fk_message_chatRoom
            foreign key(chat_room_id) references chat_rooms(chat_room_id)
            on delete cascade
);

create table read_status (
    read_status_id bigint not null auto_increment primary key,
    is_read boolean not null,
    chat_room_id bigint not null,
    chat_message_id bigint not null,
    user_id bigint not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,

    constraint fk_read_user
                foreign key(user_id) references users(user_id)
                on delete cascade,
    constraint fk_read_chatRoom
                foreign key(chat_room_id) references chat_rooms(chat_room_id)
                on delete cascade,
    constraint fk_read_message
                foreign key(chat_message_id) references chat_messages(chat_message_id)
                on delete cascade
);
