CREATE TABLE `roulette` (
  `id` int NOT NULL AUTO_INCREMENT,
  `closed_at` datetime DEFAULT NULL,
  `created_at` datetime DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `status` varchar(255) NOT NULL,
  `guild_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `anime` (
  `id` int NOT NULL AUTO_INCREMENT,
  `description` varchar(5000) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `roulette_index` int NOT NULL,
  `total_episodes` int NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `watched_episodes` int NOT NULL,
  `fk_roulette` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcgbmsbk5ivcd3pft0fttfulk4` (`fk_roulette`),
  CONSTRAINT `FKcgbmsbk5ivcd3pft0fttfulk4` FOREIGN KEY (`fk_roulette`) REFERENCES `roulette` (`id`)
);

CREATE TABLE `user_token` (
    `id` int NOT NULL AUTO_INCREMENT,
    `id_user` varchar(255) UNIQUE NOT NULL,
    `token` longtext,
    PRIMARY KEY (`id`)
);