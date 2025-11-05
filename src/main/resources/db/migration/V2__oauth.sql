create table oauth_users (
    oauth_id bigint not null auto_increment primary key,
    provider varchar(255) not null,
    provider_id varchar(255) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime not null default current_timestamp on update current_timestamp,
    user_id bigint not null,
    constraint fk_oauth_user
        foreign key(user_id) references users(user_id)
        on delete cascade
);
