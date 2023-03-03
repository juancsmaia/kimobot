CREATE TABLE `roulette` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `active` varchar(255) NOT NULL DEFAULT 0,
  `guild_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `anime` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(5000) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `roulette_index` int NOT NULL,
  `episodes` int NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `watched` int NOT NULL,
  `roulette_id` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY (`roulette_id`),
  CONSTRAINT FOREIGN KEY (`roulette_id`) REFERENCES `roulette` (`id`)
);

CREATE TABLE `user_token` (
    `id` int NOT NULL AUTO_INCREMENT,
    `id_user` varchar(255) UNIQUE NOT NULL,
    `token` longtext,
    PRIMARY KEY (`id`)
);