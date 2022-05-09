-- DROP SCHEMA anime;
CREATE SCHEMA anime AUTHORIZATION root;


-- anime.app_user definition
-- Drop table
-- DROP TABLE anime.app_user;
CREATE TABLE anime.app_user (
	id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	"name" varchar(255) NOT NULL,
	username varchar(100) NOT NULL,
	"password" varchar(150) NOT NULL,
	authorities varchar(150) NOT NULL,
	CONSTRAINT user_pk PRIMARY KEY (id)
);


-- anime.anime definition
-- Drop table
-- DROP TABLE anime.anime;
CREATE TABLE anime.anime (
	id int4 NOT NULL GENERATED ALWAYS AS IDENTITY,
	"name" varchar NOT NULL,
	CONSTRAINT anime_pk PRIMARY KEY (id)
);
CREATE UNIQUE INDEX anime_id_idx ON anime.anime USING btree (id);


--Insert Users
INSERT INTO anime.app_user ("name",username,"password",authorities) VALUES
	 ('Marcos','malves','{bcrypt}$2a$10$/POEJu6OVR2yXRIY99dL1e4h4lISn1ibG1prZGqr/ZnYX8Y45OYG6','ROLE_ADMIN,ROLE_USER'),
	 ('User','user','{bcrypt}$2a$10$/POEJu6OVR2yXRIY99dL1e4h4lISn1ibG1prZGqr/ZnYX8Y45OYG6','ROLE_USER');