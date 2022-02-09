
    drop table if exists bwnote_subs cascade;

    drop sequence if exists hibernate_sequence;
create sequence hibernate_sequence start 1 increment 1;

    create table bwnote_subs (
        bwnote_id int8 not null,
        bwnote_seq int4 not null,
        bwnote_subid varchar(250) not null,
        bwnote_trans char(1) not null,
        bwnote_conname varchar(100),
        bwnote_phref varchar(500) not null,
        bwnote_lrefresh varchar(20),
        bwnote_lstatus varchar(100),
        bwnote_errorct int4,
        bwnote_missing char(1) not null,
        bwnote_uri varchar(500),
        bwnote_props text,
        primary key (bwnote_id)
    );
create index bwnoteidx_subid on bwnote_subs (bwnote_subid);
create index bwnoteidx_phref on bwnote_subs (bwnote_phref);

    alter table bwnote_subs 
        add constraint UK_dumuiwx6roty7oeuuj8y501hj unique (bwnote_subid);
