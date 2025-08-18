-- Drop the database if it exists (for clean reset during development)
DROP DATABASE IF EXISTS bpark;

-- Create the database using utf8mb4 (full Unicode support)
CREATE DATABASE bpark DEFAULT CHARACTER SET utf8mb4;

-- Use the newly created database for all subsequent operations
USE bpark;

/********************* 2. Core reference tables **********************/
SET SQL_SAFE_UPDATES = 0;

-- TABLE: user  (stores login credentials + session flag)
CREATE TABLE bpark.`user` (
    username     VARCHAR(50)  PRIMARY KEY,
    password     VARCHAR(255) NOT NULL,
    role         ENUM('Subscriber','Manager','Attendant') NOT NULL,
    is_logged_in TINYINT(1)   NOT NULL DEFAULT 0   -- 0 = offline, 1 = online
);

-- Faster lookup when someone tries to log in
CREATE INDEX idx_users_logged_in ON bpark.`user` (is_logged_in);

-- Seed: Users with roles (no personal details here)
INSERT INTO bpark.`user` (username, password, role) VALUES
('sadran1','hash1','Attendant'),
('shiraB','hash2','Subscriber'),
('manager1','hash3','Manager'),
('danaL','hash4','Subscriber'),
('roniC','hash5','Subscriber'),
('amitD','hash6','Subscriber'),
('noaB','hash7','Subscriber'),
('liorK','hash8','Subscriber'),
('tamarS','hash9','Subscriber'),
('galP','hash10','Subscriber'),
('yaelG','hash11','Subscriber'),
('aviM','hash12','Subscriber'),
('omerS','hash13','Subscriber'),
('linaG','hash14','Subscriber'),
('erezZ','hash15','Subscriber'),
('kerenM','hash16','Subscriber'),
('idanH','hash17','Subscriber'),
('mayaT','hash18','Subscriber'),
('yossiR','hash19','Subscriber'),
('danaP','hash20','Subscriber'),
('nirL','hash21','Subscriber'),
('rachelB','hash22','Subscriber'),
('libiC','hash23','Subscriber'),
('sharonR','hash24','Subscriber'),
('maayanA','hash25','Subscriber'),
('ofraH','hash26','Subscriber'),
('haimE','hash27','Subscriber'),
('ninetT','hash28','Subscriber'),
('roniD','hash29','Subscriber'),
('idanA','hash30','Subscriber'),
('yardenaA','hash31','Subscriber'),
('shalomH','hash32','Subscriber'),
('matiC','hash33','Subscriber'),
('galG','hash34','Subscriber'),
('OmerD','hash35','Subscriber'),
('yehodaL','hash36','Subscriber'),
('omerA','hash37','Subscriber'),
('noaKi','hash38','Subscriber'),
('erezT1','hash39','Subscriber'),
('leoM','hash40','Subscriber'),
('guriA','hash41','Subscriber'),
('millerA','hash42','Subscriber'),
('tomHo1','hash43','Subscriber'),
('lawrence','hash44','Subscriber'),
('barRR','hash45','Subscriber'),
('artziS','hash46','Subscriber'),
('ronaldo7','hash47','Subscriber'),
('zuckerberg11','hash48','Subscriber'),
('Steve09','hash49','Subscriber'),
('SelaR','hash50','Subscriber'),
('Aloni12','hash51','Subscriber'),
('aviv_alush','hash52','Subscriber'),
('nivSu','hash53','Subscriber'),
('alonaT','hash54','Subscriber'),
('shiriMai','hash55','Subscriber'),
('benAri','hash56','Subscriber'),
('DiCaprio','hash57','Subscriber'),
('pittB','hash58','Subscriber'),
('Jolie','hash59','Subscriber'),
('AssiAzar','hash60','Subscriber'),
('HadarTZ','hash61','Subscriber'),
('Suchard','hash62','Subscriber'),
('einstein','hash63','Subscriber'),
('Alberstein','hash64','Subscriber'),
('GaonY','hash65','Subscriber'),
('Argov','hash66','Subscriber'),
('gidiG','hash67','Subscriber'),
('Aloni66','hash68','Subscriber'),
('yaffa10','hash69','Subscriber'),
('Almagor','hash70','Subscriber'),
('rivkale','hash71','Subscriber'),
('Tzafir','hash72','Subscriber'),
('sandraS','hash73','Subscriber'),
('ZakAnna','hash74','Subscriber'),
('yasminM','hash75','Subscriber'),
('benzR','hash76','Subscriber'),
('osher21','hash77','Subscriber'),
('LeviI','hash78','Subscriber'),
('HadadS','hash79','Subscriber'),
('YonitLevi','hash80','Subscriber'),
('Plotnik','hash81','Subscriber'),
('NsKadri','hash82','Subscriber'),
('DannyR','hash83','Subscriber'),
('edenh','hash84','Subscriber'),
('ILider','hash85','Subscriber'),
('peerT','hash86','Subscriber'),
('adirG','hash87','Subscriber'),
('almaZ','hash88','Subscriber'),
('AvTal','hash89','Subscriber'),
('Barabi','hash90','Subscriber'),
('dennisL','hash91','Subscriber'),
('liranD','hash92','Subscriber'),
('EviatarB','hash93','Subscriber'),
('ornaB','hash94','Subscriber'),
('galiA','hash95','Subscriber'),
('Ribo18','hash96','Subscriber'),
('Loai1','hash97','Subscriber'),
('nathanG','hash98','Subscriber'),
('oriBA','hash99','Subscriber'),
('RotemC','hash100','Subscriber'),
('ShabatS','hash101','Subscriber'),
('uriB','hash102','Subscriber'),
('aflalo12','hash103','Subscriber'),
('yuvalDa1','hash104','Subscriber'),
('taylorS','hash105','Subscriber'),
('danielP','hash106','Subscriber'),
('Linoyyy','hash107','Subscriber'),
('koshmaro','hash108','Subscriber'),
('AdvaD','hash109','Subscriber'),
('ran108','hash110','Subscriber'),
('ElianY','hash111','Subscriber'),
('Hitman','hash112','Subscriber');


-- TABLE: subscriber
CREATE TABLE bpark.subscriber (
    subscriberCode INT PRIMARY KEY,
    userId         CHAR(9) UNIQUE NOT NULL,
    firstName      VARCHAR(50) NOT NULL,
    lastName       VARCHAR(50) NOT NULL,
    phoneNumber    VARCHAR(20) UNIQUE,
    email          VARCHAR(100) UNIQUE,
    username       VARCHAR(50) NOT NULL,
    tagId         VARCHAR(100) UNIQUE, -- added for RFID tag identification
    CONSTRAINT fk_sub_user FOREIGN KEY (username)
      REFERENCES bpark.`user`(username)
);

-- Seed: Subscribers (linked to users by username)
INSERT INTO bpark.subscriber (subscriberCode, userId, firstName, lastName, phoneNumber, email, username, tagId) VALUES
(1001,'012345678','Shira','Bar','0556677889','shira@example.com','shiraB','TAG_001'),
(1002,'123456789','Dana','Levi','0501234567','dana@example.com','danaL','TAG_002'),
(1003,'234567891','Roni','Cohen','0529876543','roni@example.com','roniC','TAG_003'),
(1004,'345678912','Amit','Darai','0545511122','amitderei123@gmail.com','amitD','TAG_004'),
(1005,'456789123','Noa','Ben Ami','0534567890','noa@example.com','noaB','TAG_005'),
(1006,'567891234','Lior','Katz','0507654321','lior@example.com','liorK','TAG_006'),
(1007,'678912345','Tamar','Shani','0582233344','tamar@example.com','tamarS','TAG_007'),
(1008,'789123456','Gal','Peretz','0523332111','gal@example.com','galP','TAG_008'),
(1009,'891234567','Yael','Gold','0509998887','yael@example.com','yaelG','TAG_009'),
(1010,'912345678','Avi','Mizrahi','0541122344','avi@example.com','aviM','TAG_010'),
(1011,'211122233','Omer','Shalom','0528000001','omer@example.com','omerS','TAG_011'),
(1012,'222233344','Lina','Green','0528000002','lina@example.com','linaG','TAG_012'),
(1013,'233344455','Erez','Zamir','0528000003','erez@example.com','erezZ','TAG_013'),
(1014,'244455566','Keren','Mor','0528000004','keren@example.com','kerenM','TAG_014'),
(1015,'255566677','Idan','Halevi','0528000005','idan@example.com','idanH','TAG_015'),
(1016,'266677788','Maya','Tal','0528000006','maya@example.com','mayaT','TAG_016'),
(1017,'277788899','Yossi','Revivo','0528000007','yossi@example.com','yossiR','TAG_017'),
(1018,'288899900','Dana','Peri','0528000008','dana1@example.com','danaP','TAG_018'),
(1019,'299900011','Nir','Levi','0528000009','nir@example.com','nirL','TAG_019'),
(1020,'300011122','Rachel','Bar','0528000010','rachel@example.com','rachelB','TAG_020'),
(1021,'301122333','Libi','Cohen','0501234100','libi@example.com','libiC','TAG_121'),
(1022,'301122334','Sharon','Rozen','0501234101','sharon@example.com','sharonR','TAG_122'),
(1023,'301122335','Maayan','Adam','0501234102','maayan@example.com','maayanA','TAG_123'),
(1024,'301122336','Ofra','Haza','0501234103','ofra@example.com','ofraH','TAG_124'),
(1025,'301122337','Haim','Etgar','0501234104','haim@example.com','haimE','TAG_125'),
(1026,'301122338','Ninet','Tayeb','0501234105','ninet@example.com','ninetT','TAG_126'),
(1027,'301122339','Roni','Dalomi','0501234106','ronid@example.com','roniD','TAG_127'),
(1028,'301122340','Idan','Amedi','0501234107','idanamedi@example.com','idanA','TAG_128'),
(1029,'301122341','Yardena','Arazi','0501234108','yardena@example.com','yardenaA','TAG_129'),
(1030,'301122342','Shalom','Hanoch','0501234109','shalom@example.com','shalomH','TAG_130'),
(1031,'301122343','Mati','Caspi','0501234110','mati@example.com','matiC','TAG_131'),
(1032,'301122344','Gal','Gadot','0501234111','galgadot@example.com','galG','TAG_132'),
(1033,'301122345','Omer','Dror','0501234112','omerdror12@example.com','OmerD','TAG_133'),
(1034,'301122346','Yehoda','Levi','0501234113','yehoda10@example.com','yehodaL','TAG_134'),
(1035,'301122347','Omer','Adam','0501234114','omer35@example.com','omerA','TAG_135'),
(1036,'301122348','Noa','Kirel','0501234115','noa35@example.com','noaKi','TAG_136'),
(1037,'301122349','Erez','Tal','0501234116','erez_tal@example.com','erezT1','TAG_137'),
(1038,'301122350','Leo','Messi','0501234117','leo10@example.com','leoM','TAG_138'),
(1039,'301122351','Guri','Alfi','0501234118','guri9030@example.com','guriA','TAG_139'),
(1040,'301122352','Adir','Miller','0501234119','miller1999@example.com','millerA','TAG_140'),
(1041,'301122353','Tom','Holland','0501234120','tom12@example.com','tomHo1','TAG_141'),
(1042,'301122354','Jennifer','Lawrence','0501234121','lawrence@example.com','lawrence','TAG_142'),
(1043,'301122355','Bar','Refaeli','0501234122','barrr@example.com','barRR','TAG_143'),
(1044,'301122356','Shlomo','Artzi','0501234123','artzi56@example.com','artziS','TAG_144'),
(1045,'301122357','Cristiano','Ronaldo','0501234124','ronaldo7@example.com','ronaldo7','TAG_145'),
(1046,'301122358','Mark','Zuckerberg','0501234125','zuckerberg@example.com','zuckerberg11','TAG_146'),
(1047,'301122359','Steve','Jobs','0501234126','steve12@example.com','Steve09','TAG_147'),
(1048,'301122360','Rotem','Sela','0501234127','sela90@example.com','SelaR','TAG_148'),
(1049,'301122361','Michael','Aloni','0501234128','aloni@example.com','Aloni12','TAG_149'),
(1050,'301122362','Aviv','Alush','0501234129','aviv.alush@example.com','aviv_alush','TAG_150'),
(1051,'301122363','Niv','Sultan','0501234130','nivss@example.com','nivSu','TAG_151'),
(1052,'301122364','Alona','Tal','0501234131','alonat@example.com','alonaT','TAG_152'),
(1053,'301122365','Shiri','Maimon','0501234132','shirimai@example.com','shiriMai','TAG_153'),
(1054,'301122366','Hanan','Ben Ari','0501234133','benari@example.com','benAri','TAG_154'),
(1055,'301122367','Leonardo','DiCaprio','0501234134','dicaprio@example.com','DiCaprio','TAG_155'),
(1056,'301122368','Brad','Pitt','0501234135','pitt@example.com','pittB','TAG_156'),
(1057,'301122369','Angelina','Jolie','0501234136','Jolie@example.com','Jolie','TAG_157'),
(1058,'301122370','Assi','Azar','0501234137','user58@example.com','AssiAzar','TAG_158'),
(1059,'301122371','Tzvika','Hadar','0501234138','tzhadar@example.com','HadarTZ','TAG_159'),
(1060,'301122372','Lior','Suchard','0501234139','suchard@example.com','Suchard','TAG_160'),
(1061,'301122373','Arik','Einstein','0501234140','einstein@example.com','einstein','TAG_161'),
(1062,'301122374','Chava','Alberstein','0501234141','alberstein@example.com','Alberstein','TAG_162'),
(1063,'301122375','Yehoram','Gaon','0501234142','gaon3010@example.com','GaonY','TAG_163'),
(1064,'301122376','Zohar','Argov','0501234143','argov@example.com','Argov','TAG_164'),
(1065,'301122377','Gidi','Gov','0501234144','gidi.gov@example.com','gidiG','TAG_165'),
(1066,'301122378','Miri','Aloni','0501234145','Miri@example.com','Aloni66','TAG_166'),
(1067,'301122379','Yaffa','Yarkoni','0501234146','yaffa10@example.com','yaffa10','TAG_167'),
(1068,'301122380','Gila','Almagor','0501234147','almagor@example.com','Almagor','TAG_168'),
(1069,'301122381','Rivka','Michaeli','0501234148','rivale@example.com','rivkale','TAG_169'),
(1070,'301122382','Tuvia','Tzafir','0501234149','tzafir@example.com','Tzafir','TAG_170'),
(1071,'301122383','Sandra','Sade','0501234150','sandara@example.com','sandraS','TAG_171'),
(1072,'301122384','Anna','Zak','0525381648','annazak@example.com','ZakAnna','TAG_172'),
(1073,'301122385','Yasmin','Moallem','0501234152','yasss@example.com','yasminM','TAG_173'),
(1074,'301122386','Ben','Zur','0501234153','benz@example.com','benzR','TAG_174'),
(1075,'301122387','Osher','Cohen','0501234154','oshercohen78@example.com','osher21','TAG_175'),
(1076,'301122388','Itay','Levi','0501234155','itay@example.com','LeviI','TAG_176'),
(1077,'301122389','Sarit','Hadad','0501234156','hadad@example.com','HadadS','TAG_177'),
(1078,'301122390','Yonit','Levi','0501234157','yonit123@example.com','YonitLevi','TAG_178'),
(1079,'301122391','Ravid','Plotnik','0501234158','plotnik@example.com','Plotnik','TAG_179'),
(1080,'301122392','Nasrin','Kadri','0501234159','kadrinasrin@example.com','NsKadri','TAG_180'),
(1081,'301122393','Danny','Robas','0501234160','robas@example.com','DannyR','TAG_181'),
(1082,'301122394','Eden','Hason','0501234161','edenh@example.com','edenh','TAG_182'),
(1083,'301122395','Ivri','Lider','0501234162','ilider@example.com','ILider','TAG_183'),
(1084,'301122396','Peer','Tasi','0501234163','peert@example.com','peerT','TAG_184'),
(1085,'301122397','Adir','Getz','0501234164','adirG@example.com','adirG','TAG_185'),
(1086,'301122398','Alma','Zohar','0501234165','alma.zohar@example.com','almaZ','TAG_186'),
(1087,'301122399','Avraham','Tal','0501234166','avital123@example.com','AvTal','TAG_187'),
(1088,'301122400','Benaia','Barabi','0501234167','barabi88@example.com','Barabi','TAG_188'),
(1089,'301122401','Dennis','Lloyd','0501234168','dennis@example.com','dennisL','TAG_189'),
(1090,'301122402','Liran','Danino','0501234169','liran12356@example.com','liranD','TAG_190'),
(1091,'301122403','Eviatar','Banai','0501234170','eviatar12@example.com','EviatarB','TAG_191'),
(1092,'301122404','Orna','Banai','0501234171','ornab@example.com','ornaB','TAG_192'),
(1093,'301122405','Gali','Atari','0501234172','galia@example.com','galiA','TAG_193'),
(1094,'301122406','Ishay','Ribo','0501234173','ribo18@example.com','Ribo18','TAG_194'),
(1095,'301122407','Loai','Ali','0501234174','loai12@example.com','Loai1','TAG_195'),
(1096,'301122408','Nathan','Goshen','0501234175','nathang@example.com','nathanG','TAG_196'),
(1097,'301122409','Ori','Ben Ari','0501234176','orib@example.com','oriBA','TAG_197'),
(1098,'301122410','Rotem','Cohen','0501234177','rotemcohen12@example.com','RotemC','TAG_198'),
(1099,'301122411','Shlomi','Shabat','0501234178','shlomi123@example.com','ShabatS','TAG_199'),
(1100,'301122412','Uri','Banai','0501234179','urib12@example.com','uriB','TAG_200'),
(1101,'301122413','Kobi','Aflalo','0501234180','aflalo@example.com','aflalo12','TAG_201'),
(1102,'301122414','Yuval','Dayan','0501234181','yuvi12@example.com','yuvalDa1','TAG_202'),
(1103,'301122415','Taylor','Swift','0501234182','titis@example.com','taylorS','TAG_203'),
(1104,'301122416','Daniel','Peretz','0501234183','daniper@example.com','danielP','TAG_204'),
(1105,'301122417','Linoy','Ashram','0501234184','lini@example.com','Linoyyy','TAG_205'),
(1106,'301122418','Dani','Koshmaro','0501234185','koshmaro@example.com','koshmaro','TAG_206'),
(1107,'301122419','Adva','Dadon','0501234186','advad@example.com','AdvaD','TAG_207'),
(1108,'301122420','Ran','Danker','0501234187','ran108@example.com','ran108','TAG_208'),
(1109,'301122421','Yona','Elian','0501234188','elian@example.com','ElianY','TAG_209'),
(1110,'301122422','Uzi','Hitman','0501234189','hitman@example.com','Hitman','TAG_210');

/********************* 3. Parking structure **********************/

-- TABLE: vehicle
CREATE TABLE bpark.vehicle (
    vehicleId      VARCHAR(10) PRIMARY KEY,
    subscriberCode INT UNIQUE,
    CONSTRAINT fk_vehicle_sub FOREIGN KEY (subscriberCode)
      REFERENCES bpark.subscriber(subscriberCode)
      ON DELETE SET NULL
      ON UPDATE CASCADE
);

-- Seed: Vehicles
INSERT INTO bpark.vehicle (vehicleId,subscriberCode) VALUES
('111222333',1001),
('222333444',1002),
('333444555',1003),
('444555666',1004),
('555666777',1005),
('666777888',1006),
('777888999',1007),
('888999000',1008),
('999000111',1009),
('000111222',1010),
('123450001',1011),
('123450002',1012),
('123450003',1013),
('123450004',1014),
('123450005',1015),
('123450006',1016),
('123450007',1017),
('123450008',1018),
('123450009',1019),
('123450010',1020),
('123450011',1021),
('123450012',1022),
('123450013',1023),
('123450014',1024),
('123450015',1025),
('123450016',1026),
('123450017',1027),
('123450018',1028),
('123450019',1029),
('123450020',1030),
('123450021',1031),
('123450022',1032),
('123450023',1033),
('123450024',1034),
('123450025',1035),
('123450026',1036),
('123450027',1037),
('123450028',1038),
('123450029',1039),
('123450030',1040),
('123450031',1041),
('123450032',1042),
('123450033',1043),
('123450034',1044),
('123450035',1045),
('123450036',1046),
('123450037',1047),
('123450038',1048),
('123450039',1049),
('123450040',1050),
('123450041',1051),
('123450042',1052),
('123450043',1053),
('123450044',1054),
('123450045',1055),
('123450046',1056),
('123450047',1057),
('123450048',1058),
('123450049',1059),
('123450050',1060),
('123450051',1061),
('123450052',1062),
('123450053',1063),
('123450054',1064),
('123450055',1065),
('123450056',1066),
('123450057',1067),
('123450058',1068),
('123450059',1069),
('123450060',1070),
('123450061',1071),
('123450062',1072),
('123450063',1073),
('123450064',1074),
('123450065',1075),
('123450066',1076),
('123450067',1077),
('123450068',1078),
('123450069',1079),
('123450070',1080),
('123450071',1081),
('123450072',1082),
('123450073',1083),
('123450074',1084),
('123450075',1085),
('123450076',1086),
('123450077',1087),
('123450078',1088),
('123450079',1089),
('123450080',1090),
('123450081',1091),
('123450082',1092),
('123450083',1093),
('123450084',1094),
('123450085',1095),
('123450086',1096),
('123450087',1097),
('123450088',1098),
('123450089',1099),
('123450090',1100),
('123450091',1101),
('123450092',1102),
('123450093',1103),
('123450094',1104),
('123450095',1105),
('123450096',1106),
('123450097',1107),
('123450098',1108),
('123450099',1109),
('123450100',1110);

-- TABLE: parkingLot
CREATE TABLE bpark.parkingLot (
    NameParkingLot VARCHAR(10) PRIMARY KEY,
    totalSpots     INT,
    occupiedSpots  INT
);
INSERT INTO bpark.parkingLot VALUES ('Braude',20, 0);

-- TABLE: extensionCapacity (used to manage how many parking sessions can still be extended)
CREATE TABLE bpark.extensionCapacity (
    lotName VARCHAR(50) PRIMARY KEY,
    remainingExtensions INT
);

-- Seed: initial capacity entry (Braude has 0 extensions allowed initially)
INSERT INTO bpark.extensionCapacity (lotName, remainingExtensions)
VALUES ('Braude', 0);

-- TABLE: parkingSpaces
CREATE TABLE bpark.parkingSpaces (
    parking_space INT PRIMARY KEY,
    is_occupied   BOOLEAN DEFAULT FALSE
);
INSERT INTO bpark.parkingSpaces (parking_space,is_occupied)
SELECT seq, FALSE
FROM (SELECT ROW_NUMBER() OVER () AS seq FROM information_schema.columns LIMIT 20) AS generator;

/********************* 4. Business operations **********************/

-- TABLE: order
CREATE TABLE bpark.`order` (
    order_number INT AUTO_INCREMENT PRIMARY KEY,
    parking_space INT,
    order_date DATE,
    arrival_time TIME,
    confirmation_code VARCHAR(6) NOT NULL,
    subscriberCode INT,
    date_of_placing_an_order DATE,
    `status` ENUM('ACTIVE', 'CANCELLED', 'INACTIVE', 'FULFILLED') NOT NULL,
    CONSTRAINT fk_order_subscriber FOREIGN KEY (subscriberCode)
      REFERENCES bpark.subscriber(subscriberCode),
    CONSTRAINT fk_order_space FOREIGN KEY (parking_space)
      REFERENCES bpark.parkingSpaces(parking_space)
);

-- Seed: Orders
INSERT INTO bpark.`order` (parking_space,order_date,arrival_time,confirmation_code,subscriberCode,date_of_placing_an_order, `status`) VALUES
/* ---------- February 2025 ---------- */
(1,'2025-02-01','08:15','198751',1001,'2025-01-31', 'FULFILLED'),
(1,'2025-02-01','08:00','768564',1001,'2025-01-29', 'CANCELLED'),
(2,'2025-02-01','10:15','246531',1020,'2025-01-30', 'FULFILLED'),
(3,'2025-02-01','08:15','975342',1035,'2025-01-31', 'INACTIVE'),
(1,'2025-02-02','14:30','531086',1072,'2025-01-31', 'FULFILLED'),
(1,'2025-02-03','08:15','284710',1093,'2025-01-31', 'FULFILLED'),
(1,'2025-02-04','07:30','845628',1003,'2025-01-28', 'CANCELLED'),
(2,'2025-02-05','11:45','096385',1002,'2025-02-03', 'FULFILLED'),
(3,'2025-02-05','08:15','567321',1090,'2025-01-31', 'CANCELLED'),
(1,'2025-02-06','14:30','975631',1067,'2025-02-04', 'CANCELLED'),
(1,'2025-02-09','08:15','875600',1007,'2025-02-02', 'FULFILLED'),
(2,'2025-02-09','08:00','112543',1004,'2025-02-06', 'FULFILLED'),
(3,'2025-02-09','10:15','090056',1088,'2025-02-06', 'FULFILLED'),
(4,'2025-02-09','08:15','232176',1009,'2025-02-06', 'FULFILLED'),
(5,'2025-02-09','10:30','169347',1056,'2025-02-07', 'FULFILLED'),
(1,'2025-02-10','06:15','453876',1093,'2025-02-05', 'FULFILLED'),
(1,'2025-02-11','07:30','764280',1011,'2025-02-06', 'CANCELLED'),
(1,'2025-02-12','12:45','126550',1001,'2025-02-06', 'INACTIVE'),
(1,'2025-02-14','08:45','567321',1060,'2025-02-10', 'FULFILLED'),
(1,'2025-02-15','16:30','513245',1061,'2025-02-04', 'FULFILLED'),
(1,'2025-02-20','15:30','764280',1012,'2025-02-06', 'CANCELLED'),
(1,'2025-02-22','19:45','126550',1025,'2025-02-06', 'INACTIVE'),
(1,'2025-02-24','08:15','567321',1026,'2025-02-10', 'FULFILLED'),
(1,'2025-02-25','16:30','513245',1037,'2025-02-04', 'FULFILLED'),

/* ---------- March 2025 ---------- */
(1,'2025-03-01','09:45','437264',1009,'2025-02-28', 'INACTIVE'),
(2,'2025-03-01','08:00','427398',1044,'2025-02-28', 'CANCELLED'),
(2,'2025-03-01','11:45','456399',1020,'2025-02-28', 'FULFILLED'),
(1,'2025-03-03','09:15','535445',1099,'2025-03-01', 'INACTIVE'),
(1,'2025-03-04','15:15','987423',1093,'2025-03-01', 'FULFILLED'),
(1,'2025-03-05','08:15','645665',1002,'2025-03-01', 'INACTIVE'),
(1,'2025-03-06','07:30','845628',1003,'2025-03-02', 'CANCELLED'),
(1,'2025-03-07','12:15','008641',1081,'2025-03-03', 'FULFILLED'),
(1,'2025-03-08','08:15','467281',1090,'2025-03-05', 'CANCELLED'),
(1,'2025-03-10','14:30','975631',1068,'2025-03-08', 'CANCELLED'),
(1,'2025-03-12','09:15','875600',1007,'2025-03-10', 'INACTIVE'),
(2,'2025-03-12','09:00','112543',1004,'2025-03-10', 'FULFILLED'),
(3,'2025-03-12','11:15','534531',1098,'2025-03-10', 'FULFILLED'),
(4,'2025-03-12','09:15','647621',1103,'2025-03-11', 'FULFILLED'),
(5,'2025-03-12','10:30','324321',1054,'2025-03-11', 'CANCELLED'),
(1,'2025-03-14','11:15','543534',1093,'2025-03-08', 'FULFILLED'),
(2,'2025-03-14','07:30','423663',1087,'2025-03-09', 'CANCELLED'),
(1,'2025-03-15','12:45','126550',1005,'2025-03-06', 'FULFILLED'),
(1,'2025-03-15','08:15','567321',1068,'2025-03-10', 'FULFILLED'),
(2,'2025-03-15','16:30','978563',1076,'2025-03-14', 'FULFILLED'),
(1,'2025-03-20','15:30','565645',1047,'2025-03-14', 'CANCELLED'),
(1,'2025-03-23','18:15','543544',1021,'2025-03-22', 'FULFILLED'),
(1,'2025-03-24','08:45','432563',1097,'2025-03-22', 'FULFILLED'),
(1,'2025-03-30','15:00','543554',1017,'2025-03-25', 'INACTIVE'),

/* ---------- April 2025 ---------- */
(1,'2025-04-01','15:00','365231',1024,'2025-03-30','INACTIVE'),
(1,'2025-04-02','11:00','504839',1086,'2025-04-01','FULFILLED'),
(1,'2025-04-07','07:45','509949',1031,'2025-04-06', 'FULFILLED'),
(1,'2025-04-07','12:45','110140',1089,'2025-04-05', 'FULFILLED'),
(1,'2025-04-09','11:45','212347',1020,'2025-04-07','INACTIVE'),
(1,'2025-04-11','07:30','772452',1044,'2025-04-06','INACTIVE'),
(1,'2025-04-13','16:15','725819',1015,'2025-04-08','FULFILLED'),
(2,'2025-04-13','12:15','224121',1051,'2025-04-11','CANCELLED'),
(1,'2025-04-14','07:30','418674',1095,'2025-04-09','INACTIVE'),
(1,'2025-04-14','18:45','275760',1039,'2025-04-10','INACTIVE'),
(2,'2025-04-18','12:00','496826',1068,'2025-04-15','CANCELLED'),
(2,'2025-04-18','12:45','652394',1081,'2025-04-16','INACTIVE'),
(1,'2025-04-18','10:15','483981',1036,'2025-04-13','INACTIVE'),
(1,'2025-04-19','17:45','793922',1029,'2025-04-18','CANCELLED'),
(1,'2025-04-20','10:45','363115',1059,'2025-04-16','INACTIVE'),
(1,'2025-04-21','17:45','276363',1050,'2025-04-20','FULFILLED'),
(1,'2025-04-22','18:00','709969',1041,'2025-04-20','FULFILLED'),
(1,'2025-04-25','10:15','236726',1006,'2025-04-23','FULFILLED'),
(2,'2025-04-25','18:15','884489',1026,'2025-04-23','FULFILLED'),
(1,'2025-04-25','18:15','729453',1035,'2025-04-22','CANCELLED'),
(1,'2025-04-26','12:00','743837',1007,'2025-04-21','INACTIVE'),
(1,'2025-04-26','18:15','749778',1008,'2025-04-24','CANCELLED'),

/* ---------- May 2025 ---------- */
(1, '2025-05-02', '08:15', '636403', 1096, '2025-05-01', 'FULFILLED'),
(2, '2025-05-02', '08:45', '472484', 1084, '2025-05-01', 'CANCELLED'),
(1, '2025-05-06', '07:30', '556174', 1067, '2025-05-02', 'INACTIVE'),
(2, '2025-05-07', '14:45', '095438', 1053, '2025-05-05', 'INACTIVE'),
(1, '2025-05-07', '17:45', '311923', 1097, '2025-05-03', 'INACTIVE'),
(2, '2025-05-09', '15:15', '238542', 1049, '2025-05-07', 'FULFILLED'),
(1, '2025-05-09', '16:15', '080778', 1040, '2025-05-06', 'FULFILLED'),
(1, '2025-05-11', '10:15', '793083', 1017, '2025-05-08', 'INACTIVE'),
(1, '2025-05-12', '14:30', '337274', 1016, '2025-05-08', 'CANCELLED'),
(1, '2025-05-18', '10:45', '466320', 1012, '2025-05-16', 'INACTIVE'),
(2, '2025-05-18', '08:45', '353842', 1052, '2025-05-16', 'INACTIVE'),
(2, '2025-05-19', '18:00', '086357', 1002, '2025-05-17', 'CANCELLED'),
(1, '2025-05-19', '11:15', '762370', 1019, '2025-05-15', 'FULFILLED'),
(1, '2025-05-20', '13:00', '253344', 1028, '2025-05-15', 'INACTIVE'),
(2, '2025-05-20', '15:30', '433051', 1088, '2025-05-18', 'FULFILLED'),
(3, '2025-05-20', '16:00', '044269', 1022, '2025-05-18', 'FULFILLED'),
(1, '2025-05-22', '18:00', '550772', 1005, '2025-05-18', 'FULFILLED'),
(1, '2025-05-22', '08:30', '009665', 1043, '2025-05-17', 'INACTIVE'),
(2, '2025-05-22', '15:45', '572639', 1075, '2025-05-20', 'FULFILLED'),
(3, '2025-05-22', '17:45', '039959', 1055, '2025-05-21', 'FULFILLED'),
(1, '2025-05-26', '07:15', '442568', 1087, '2025-05-24', 'FULFILLED'),
(2, '2025-05-27', '15:45', '711970', 1058, '2025-05-26', 'FULFILLED'),
(1, '2025-05-27', '12:30', '944033', 1037, '2025-05-22', 'FULFILLED'),


/* ---------- June 2025 ---------- */
(1, '2025-06-02', '18:00', '164687', 1001, '2025-05-31', 'FULFILLED'),
(1, '2025-06-08', '07:45', '328373', 1090, '2025-06-03', 'CANCELLED'),
(1, '2025-06-09', '08:15', '776360', 1010, '2025-06-07', 'FULFILLED'),
(1, '2025-06-12', '09:15', '638752', 1056, '2025-06-08', 'FULFILLED'),
(2, '2025-06-12', '12:45', '265810', 1094, '2025-06-09', 'INACTIVE'),
(2, '2025-06-14', '12:15', '925167', 1065, '2025-06-11', 'CANCELLED'),
(1, '2025-06-14', '15:15', '189697', 1046, '2025-06-10', 'INACTIVE'),
(1, '2025-06-15', '10:00', '512274', 1042, '2025-06-13', 'FULFILLED'),
(1, '2025-06-17', '15:00', '310581', 1064, '2025-06-15', 'FULFILLED'),
(1, '2025-06-18', '10:15', '694190', 1048, '2025-06-16', 'INACTIVE'),
(1, '2025-06-19', '13:30', '203503', 1073, '2025-06-17', 'FULFILLED'),
(1, '2025-06-20', '14:45', '364368', 1057, '2025-06-17', 'INACTIVE'),
(1, '2025-06-21', '17:00', '481051', 1061, '2025-06-19', 'CANCELLED'),
(1, '2025-06-22', '09:30', '952730', 1078, '2025-06-20', 'FULFILLED'),
(1, '2025-06-23', '08:15', '053692', 1058, '2025-06-21', 'INACTIVE'),
(1, '2025-06-24', '10:30', '862384', 1076, '2025-06-22', 'INACTIVE'),
(1, '2025-06-26', '09:15', '074438', 1075, '2025-06-25', 'FULFILLED'),
(1, '2025-06-27', '08:30', '350723', 1093, '2025-06-25', 'CANCELLED'),
(1, '2025-06-28', '14:30', '138907', 1080, '2025-06-27', 'FULFILLED'),

/* ---------- July 2025 ---------- */
(1, '2025-07-01', '08:45', '347854', 1047, '2025-06-30', 'FULFILLED'),
(1, '2025-07-02', '10:30', '579184', 1063, '2025-07-01', 'FULFILLED'),
(1, '2025-07-02', '17:15', '945380', 1051, '2025-07-01', 'INACTIVE'),
(1, '2025-07-03', '13:30', '162905', 1067, '2025-07-02', 'FULFILLED'),
(1, '2025-07-04', '08:15', '928350', 1053, '2025-07-02', 'FULFILLED'),
(1, '2025-07-05', '11:30', '591422', 1082, '2025-07-03', 'CANCELLED'),
(1, '2025-07-06', '09:15', '583192', 1045, '2025-07-04', 'FULFILLED'),
(1, '2025-07-08', '08:15', '012415', 1043, '2025-07-06', 'INACTIVE'),
(1, '2025-07-09', '15:30', '479316', 1083, '2025-07-07', 'FULFILLED'),
(1, '2025-07-10', '10:30', '238174', 1091, '2025-07-08', 'ACTIVE'),
(1, '2025-07-11', '09:00', '679521', 1060, '2025-07-09', 'CANCELLED'),
(1, '2025-07-12', '17:15', '204791', 1074, '2025-07-10', 'ACTIVE'),
(1, '2025-07-13', '10:30', '578402', 1054, '2025-07-11', 'ACTIVE'),
(1, '2025-07-14', '09:15', '613579', 1092, '2025-07-12', 'ACTIVE'),
(1, '2025-07-15', '15:15', '278163', 1079, '2025-07-13', 'ACTIVE'),
(1, '2025-07-16', '08:45', '768531', 1077, '2025-07-14', 'ACTIVE'),
(2, '2025-07-16', '08:45', '567345', 1098, '2025-07-14', 'ACTIVE'),
(3, '2025-07-16', '08:45', '768531', 1097, '2025-07-14', 'ACTIVE'),
(4, '2025-07-16', '08:45', '534534', 1045, '2025-07-14', 'ACTIVE'),
(6, '2025-07-16', '08:45', '748319', 1066, '2025-07-14', 'ACTIVE'),
(7, '2025-07-16', '08:45', '678438', 1092, '2025-07-14', 'ACTIVE'),
(8, '2025-07-16', '08:45', '432423', 1034, '2025-07-14', 'ACTIVE'),
(9, '2025-07-16', '08:45', '123121', 1081, '2025-07-14', 'ACTIVE'),
(10, '2025-07-16', '08:45', '687777', 1110, '2025-07-14', 'ACTIVE'),
(11, '2025-07-16', '08:45', '788889', 1109, '2025-07-14', 'ACTIVE'),
(12, '2025-07-16', '08:45', '090888', 1072, '2025-07-14', 'ACTIVE'),
(5, '2025-07-16', '08:45', '543543', 1091, '2025-07-14', 'ACTIVE');



-- TABLE: parkingEvent
CREATE TABLE bpark.parkingEvent (
    eventId INT AUTO_INCREMENT PRIMARY KEY,
    subscriberCode INT,
    parking_space INT NOT NULL,
    entryDate DATE NOT NULL,
    entryHour TIME NOT NULL,
    exitDate DATE DEFAULT NULL,
    exitHour TIME DEFAULT NULL,
    wasExtended BOOLEAN DEFAULT FALSE,
    vehicleId VARCHAR(10),
    NameParkingLot VARCHAR(10),
    parkingCode INT NOT NULL,
    sendMsgForLating BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_event_subscriber FOREIGN KEY (subscriberCode)
      REFERENCES bpark.subscriber(subscriberCode),
    CONSTRAINT fk_event_vehicle FOREIGN KEY (vehicleId)
      REFERENCES bpark.vehicle(vehicleId),
    CONSTRAINT fk_event_lot FOREIGN KEY (NameParkingLot)
      REFERENCES bpark.parkingLot(NameParkingLot),
    CONSTRAINT fk_event_space FOREIGN KEY (parking_space)
      REFERENCES bpark.parkingSpaces(parking_space)
);
CREATE INDEX idx_event_subscriber ON bpark.parkingEvent(subscriberCode);

-- Seed: Parking events
INSERT INTO bpark.parkingEvent
(subscriberCode,parking_space,entryDate,entryHour,exitDate,exitHour,wasExtended,vehicleId,NameParkingLot,parkingCode) VALUES
/* ---------- February 2025 ---------- */
(1001,1,'2025-02-01','08:15','2025-02-01','13:00',TRUE,'111222333','Braude',784197), -- order
(1020,2,'2025-02-01','10:20','2025-02-01','12:49',FALSE,'123450010','Braude',347610), -- order
(1009,1,'2025-02-01','14:25','2025-02-01','16:50',TRUE,'999000111','Braude',847291),
(1096,1,'2025-02-01','16:38','2025-02-01','17:50',FALSE,'123450086','Braude',561943),
(1072,1,'2025-02-02','14:31','2025-02-02','20:15',FALSE,'123450062','Braude',100655), -- order+late
(1093,1,'2025-02-03','08:15','2025-02-03','16:50',TRUE,'123450083','Braude',946719), -- order+late
(1005,20,'2025-02-03','11:00','2025-02-03','16:47',FALSE,'555666777','Braude',493773), -- late
(1001,11,'2025-02-05','08:00','2025-02-05','11:00',FALSE,'111222333','Braude',800109),
(1034,15,'2025-02-05','09:30','2025-02-05','12:15',TRUE,'123450024','Braude',965198),
(1098,10,'2025-02-05','08:45','2025-02-05','10:09',FALSE,'123450088','Braude',501973),
(1002,2,'2025-02-05','11:50','2025-02-05','13:15',FALSE,'222333444','Braude',563791), -- order
(1002,12,'2025-02-06','09:15','2025-02-06','13:45',FALSE,'222333444','Braude',800110), -- late (>4)
(1003,13,'2025-02-07','10:30','2025-02-07','18:20',TRUE ,'333444555','Braude',800111), -- 7h50 extended (ok)
(1004,14,'2025-02-08','12:40','2025-02-08','21:10',TRUE ,'444555666','Braude',800112), -- 8h30 late even with ext
(1076,15,'2025-02-08','12:15','2025-02-08','16:53',FALSE,'123450066','Braude',496921), -- late
(1005,15,'2025-02-09','07:45','2025-02-09','10:00',FALSE,'555666777','Braude',800113),
(1003,10,'2025-02-09','14:00','2025-02-09','18:10',TRUE,'333444555','Braude',332965),
(1004,2,'2025-02-09','08:00','2025-02-09','14:05',FALSE,'444555666','Braude',800113), -- order+late
(1007,1,'2025-02-09','08:19','2025-02-09','10:00',FALSE,'777888999','Braude',810163), -- order
(1009,4,'2025-02-09','08:15','2025-02-09','18:00',TRUE,'999000111','Braude',567917), -- order
(1088,3,'2025-02-09','10:15','2025-02-09','15:00',FALSE,'123450078','Braude',104699), -- order+late
(1056,5,'2025-02-09','10:30','2025-02-09','14:00',TRUE,'123450046','Braude',367482), -- order
(1093,1,'2025-02-10','06:20','2025-02-09','14:00',TRUE,'123450083','Braude',448711), -- order
(1098,16,'2025-02-10','10:00','2025-02-10','15:50',TRUE,'123450088','Braude',774806),
(1006,16,'2025-02-10','11:20','2025-02-10','15:50',TRUE ,'666777888','Braude',800114),
(1007,17,'2025-02-11','09:25','2025-02-11','12:35',FALSE,'777888999','Braude',800115),
(1046,18,'2025-02-11','16:15','2025-02-11','21:58',TRUE,'123450036','Braude',432919),
(1085,19,'2025-02-11','13:45','2025-02-11','18:13',FALSE,'123450075','Braude',980451), -- late
(1008,17,'2025-02-12','14:10','2025-02-12','19:30',TRUE ,'888999000','Braude',800116),
(1009,19,'2025-02-13','08:05','2025-02-13','11:25',FALSE,'999000111','Braude',800117),
(1010,20,'2025-02-14','12:40','2025-02-14','16:15',TRUE ,'000111222','Braude',121437),
(1060,1,'2025-02-14','08:45','2025-02-14','10:15',FALSE,'123450050','Braude',800118), -- order
(1011,20,'2025-02-15','09:10','2025-02-15','12:40',FALSE,'123450001','Braude',800119),
(1061,1,'2025-02-15','16:32','2025-02-15','19:40',FALSE,'123450051','Braude',119965), -- order
(1012,20,'2025-02-16','10:45','2025-02-16','14:55',FALSE,'123450002','Braude',800120), -- late
(1023,15,'2025-02-21','07:00','2025-02-21','12:53',FALSE,'123450013','Braude',765502), -- late
(1108,16,'2025-02-23','09:30','2025-02-23','12:00',FALSE,'123450098','Braude',446913), 
(1026,1,'2025-02-24','08:21','2025-02-24','18:40',FALSE,'123450016','Braude',469102), -- order+late
(1102,6,'2025-02-24','09:15','2025-02-24','13:32',TRUE,'123450092','Braude',928768),
(1037,1,'2025-02-25','16:33','2025-02-25','18:43',FALSE,'123450027','Braude',469102), -- order

/* ---------- March 2025 ---------- */
(1020,1,'2025-03-01','11:46','2025-03-01','12:50',TRUE,'123450010','Braude',783491), -- order
(1094,2,'2025-03-04','08:00','2025-03-04','09:39',FALSE,'123450084','Braude',879418),
(1093,1,'2025-03-04','15:17','2025-03-04','20:59',TRUE,'123450083','Braude',760043), -- order
(1013,6,'2025-03-05','08:20','2025-03-05','11:50',FALSE,'123450003','Braude',800121),
(1014,3,'2025-03-06','09:40','2025-03-06','18:00',TRUE ,'123450004','Braude',800122),
(1081,1,'2025-03-07','12:16','2025-03-07','16:05',FALSE,'123450071','Braude',752291), -- order
(1015,5,'2025-03-07','07:50','2025-03-07','10:05',FALSE,'123450005','Braude',800123),
(1016,9,'2025-03-08','11:15','2025-03-08','15:45',TRUE ,'123450006','Braude',800124),
(1017,8,'2025-03-09','09:35','2025-03-09','13:55',FALSE,'123450007','Braude',800125),
(1030,2,'2025-03-09','14:30','2025-03-09','20:19',TRUE,'123450020','Braude',602726),
(1018,7,'2025-03-10','14:20','2025-03-10','18:50',FALSE,'123450008','Braude',800126),
(1019,6,'2025-03-11','08:00','2025-03-11','12:20',FALSE,'123450009','Braude',800127), -- late
(1057,9,'2025-03-11','09:15','2025-03-11','14:37',FALSE,'123450047','Braude',895137), -- late
(1020,11,'2025-03-12','12:30','2025-03-12','16:00',TRUE ,'123450010','Braude',800128), 
(1098,3,'2025-03-12','11:24','2025-03-12','16:19',TRUE ,'123450088','Braude',469911), -- order
(1004,2,'2025-03-12','09:07','2025-03-12','13:00',FALSE ,'444555666','Braude',398887), -- order+late
(1001,11,'2025-03-13','08:10','2025-03-13','11:40',FALSE,'111222333','Braude',800129),
(1002,12,'2025-03-14','09:30','2025-03-14','13:10',TRUE ,'222333444','Braude',800130),
(1093,1,'2025-03-14','11:15','2025-03-14','13:10',FALSE ,'123450083','Braude',237291), -- order
(1003,13,'2025-03-15','10:55','2025-03-15','14:15',FALSE,'333444555','Braude',800131),
(1005,2,'2025-03-15','12:54','2025-03-15','15:15',FALSE,'555666777','Braude',473921), -- order
(1068,1,'2025-03-15','08:15','2025-03-15','15:15',TRUE,'123450058','Braude',847900), -- order
(1076,1,'2025-03-15','16:31','2025-03-15','20:15',FALSE,'123450066','Braude',321176), -- order
(1004,14,'2025-03-16','13:15','2025-03-16','17:35',FALSE,'444555666','Braude',800132),
(1097,15,'2025-03-16','06:00','2025-03-16','11:59',TRUE,'123450087','Braude',827303),
(1098,19,'2025-03-17','10:30','2025-03-17','12:46',FALSE,'123450088','Braude',907193),
(1059,15,'2025-03-17','06:15','2025-03-17','11:56',TRUE,'123450049','Braude',961013),
(1035,16,'2025-03-19','07:15','2025-03-19','09:38',FALSE,'123450025','Braude',926012),
(1073,17,'2025-03-19','08:00','2025-03-19','10:33',FALSE,'123450063','Braude',598429),
(1004,1,'2025-03-23','18:15','2025-03-23','19:39',FALSE,'444555666','Braude',573110), -- order
(1097,1,'2025-03-24','08:47','2025-03-24','17:35',TRUE,'123450087','Braude',468291), -- order+late
(1109,18,'2025-03-28','09:30','2025-03-28','13:16',FALSE,'123450099','Braude',387449),

/* ---------- April 2025 (12) ---------- */
(1076,15,'2025-04-02','10:45','2025-04-02','15:02',TRUE,'123450066','Braude',905520),
(1086,1,'2025-04-02','11:01','2025-04-02','13:15',FALSE,'123450076','Braude',985444), -- order
(1076,4,'2025-04-03','11:45','2025-04-03','16:13',TRUE,'123450066','Braude',751109),
(1005,15,'2025-04-05','07:55','2025-04-05','10:15',FALSE,'555666777','Braude',800133),
(1006,16,'2025-04-06','11:25','2025-04-06','15:55',TRUE ,'666777888','Braude',800134),
(1007,17,'2025-04-07','09:45','2025-04-07','13:05',FALSE,'777888999','Braude',800135),
(1031,1,'2025-04-07','07:46','2025-04-07','17:05',TRUE,'123450021','Braude',436371), -- order + late
(1089,2,'2025-04-07','12:46','2025-04-07','13:05',FALSE,'123450079','Braude',461133),
(1008,18,'2025-04-08','14:30','2025-04-08','18:50',FALSE,'888999000','Braude',800136),
(1009,19,'2025-04-09','08:15','2025-04-09','11:35',FALSE,'999000111','Braude',800137),
(1005,5,'2025-04-09','15:45','2025-04-09','19:08',TRUE,'555666777','Braude',446917),
(1010,4,'2025-04-10','12:50','2025-04-10','16:15',TRUE ,'000111222','Braude',800138),
(1011,1,'2025-04-11','08:05','2025-04-11','11:35',FALSE,'123450001','Braude',800139),
(1086,19,'2025-04-11','07:45','2025-04-11','13:26',TRUE,'123450076','Braude',760240),
(1013,2,'2025-04-11','08:00','2025-04-11','09:33',TRUE,'123450003','Braude',302696),
(1012,4,'2025-04-12','09:25','2025-04-12','18:10',TRUE ,'123450002','Braude',800140), -- 8h45 late
(1013,3,'2025-04-13','10:40','2025-04-13','14:00',FALSE,'123450003','Braude',800141),
(1015,1,'2025-04-13','16:21','2025-04-13','19:00',TRUE,'123450005','Braude',632783), -- order + late
(1014,4,'2025-04-14','13:00','2025-04-14','17:20',FALSE,'123450004','Braude',800142),
(1048,2,'2025-04-14','08:00','2025-04-14','09:48',FALSE,'123450038','Braude',925897),
(1015,6,'2025-04-15','07:40','2025-04-15','10:00',FALSE,'123450005','Braude',800143),
(1016,8,'2025-04-16','11:10','2025-04-16','15:40',TRUE ,'123450006','Braude',800144),
(1094,17,'2025-04-17','15:30','2025-04-17','18:11',FALSE,'123450084','Braude',816383),
(1034,2,'2025-04-17','13:45','2025-04-17','17:50',FALSE,'123450024','Braude',382609), -- late
(1050,1,'2025-04-21','17:40','2025-04-21','19:40',TRUE ,'123450040','Braude',282911), -- order
(1041,1,'2025-04-22','18:10','2025-04-22','20:15',FALSE ,'123450031','Braude',343318), -- order
(1015,19,'2025-04-23','06:30','2025-04-23','10:04',FALSE,'123450005','Braude',773160),
(1006,1,'2025-04-25','10:15','2025-04-25','16:15',FALSE ,'666777888','Braude',234231), -- order + late
(1026,2,'2025-04-25','18:17','2025-04-26','00:15',TRUE ,'123450016','Braude',436728), -- order
(1027,20,'2025-04-25','08:30','2025-04-25','12:20',FALSE,'123450017','Braude',891160),

/* ---------- May 2025 (13) ---------- */
(1012,6,'2025-05-01','10:00','2025-05-01','15:07',FALSE,'123450002','Braude',987303), -- late
(1017,7,'2025-05-05','09:30','2025-05-05','12:50',FALSE,'123450007','Braude',800145),
(1018,8,'2025-05-05','14:15','2025-05-05','18:35',FALSE,'123450008','Braude',800146),
(1019,9,'2025-05-06','08:05','2025-05-06','11:25',FALSE,'123450009','Braude',800147),
(1020,5,'2025-05-06','12:40','2025-05-06','16:05',TRUE ,'123450010','Braude',800148),
(1001,11,'2025-05-07','07:50','2025-05-07','10:50',FALSE,'111222333','Braude',800149),
(1002,12,'2025-05-07','11:35','2025-05-07','15:05',TRUE ,'222333444','Braude',800150),
(1083,14,'2025-05-07','09:15','2025-05-07','10:20',FALSE,'123450073','Braude',922455),
(1107,15,'2025-05-07','11:30','2025-05-07','12:31',FALSE,'123450097','Braude',918065),
(1003,13,'2025-05-08','09:05','2025-05-08','13:25',FALSE,'333444555','Braude',800151),
(1065,16,'2025-05-08','15:15','2025-05-08','20:49',TRUE,'123450055','Braude',591500),
(1004,14,'2025-05-08','13:40','2025-05-08','18:00',FALSE,'444555666','Braude',800152),
(1005,8,'2025-05-09','08:45','2025-05-09','11:15',FALSE,'555666777','Braude',800153),
(1006,12,'2025-05-09','12:10','2025-05-09','16:40',TRUE ,'666777888','Braude',800154),
(1049,2,'2025-05-09','15:20','2025-05-09','16:40',FALSE ,'123450039','Braude',543544), -- order
(1040,1,'2025-05-09','16:25','2025-05-09','19:45',FALSE ,'123450030','Braude',432432), -- order
(1014,3,'2025-05-09','10:45','2025-05-09','12:53',FALSE,'123450004','Braude',418878),
(1007,7,'2025-05-10','09:20','2025-05-10','12:50',FALSE,'777888999','Braude',800155),
(1008,8,'2025-05-10','14:05','2025-05-10','19:35',FALSE,'888999000','Braude',800156),
(1009,9,'2025-05-11','08:00','2025-05-11','12:30',TRUE ,'999000111','Braude',800157), -- 4.5h ext (ok)
(1106,13,'2025-05-13','14:15','2025-05-13','17:39',FALSE,'123450096','Braude',860381),
(1043,3,'2025-05-17','15:45','2025-05-17','19:38',TRUE,'123450033','Braude',979175),
(1026,8,'2025-05-17','16:15','2025-05-17','20:14',FALSE,'123450016','Braude',779868),
(1088,2,'2025-05-20','15:35','2025-05-20','17:35',TRUE,'123450078','Braude',544100), -- order
(1022,3,'2025-05-20','16:05','2025-05-20','20:35',FALSE,'123450012','Braude',009421), -- order + late
(1005,1,'2025-05-22','18:00','2025-05-22','18:35',FALSE,'555666777','Braude',985625), -- order
(1075,2,'2025-05-22','15:51','2025-05-22','17:35',FALSE,'123450065','Braude',232322), -- order
(1055,3,'2025-05-22','17:51','2025-05-22','18:35',TRUE,'123450045','Braude',543421), -- order
(1031,4,'2025-05-24','15:30','2025-05-24','20:16',TRUE,'123450021','Braude',831925),
(1087,1,'2025-05-26','07:16','2025-05-26','12:35',FALSE,'123450077','Braude',432422), -- order + late
(1058,2,'2025-05-27','15:48','2025-05-27','18:35',FALSE,'123450045','Braude',543443), -- order
(1037,1,'2025-05-27','12:34','2025-05-27','16:35',FALSE,'123450045','Braude',543443), -- order + late
(1078,8,'2025-05-27','16:30','2025-05-27','20:48',FALSE,'123450068','Braude',945037), -- late

/* ---------- June 2025 ---------- */
(1010,6,'2025-06-01','08:20','2025-06-01','11:40',FALSE,'000111222','Braude',800158),
(1069,4,'2025-06-02','15:15','2025-06-02','21:10',TRUE,'123450059','Braude',533137),
(1016,3,'2025-06-02','09:30','2025-06-02','14:45',FALSE,'123450006','Braude',944878), -- late
(1001,1,'2025-06-02','18:14','2025-06-02','20:40',FALSE,'111222333','Braude',457391), -- order
(1011,6,'2025-06-02','10:35','2025-06-02','15:05',TRUE ,'123450001','Braude',800159),
(1012,16,'2025-06-03','09:55','2025-06-03','13:15',FALSE,'123450002','Braude',800160),
(1013,12,'2025-06-04','13:10','2025-06-04','17:40',FALSE,'123450003','Braude',800161),
(1046,11,'2025-06-05','13:30','2025-06-05','17:43',TRUE,'123450036','Braude',654472),
(1014,20,'2025-06-05','07:35','2025-06-05','10:05',FALSE,'123450004','Braude',800162),
(1025,9,'2025-06-06','14:30','2025-06-06','18:58',TRUE,'123450015','Braude',724058),
(1020,20,'2025-06-06','14:10','2025-06-06','18:40',FALSE,'123450010','Braude',800168),
(1001,1,'2025-06-07','08:25','2025-06-07','12:55',TRUE ,'111222333','Braude',800169),
(1002,2,'2025-06-08','11:50','2025-06-08','16:20',TRUE ,'222333444','Braude',800170),
(1010,1,'2025-06-09','08:16','2025-06-09','14:20',FALSE ,'000111222','Braude',367181), -- order + late
(1056,1,'2025-06-12','09:20','2025-06-12','13:20',TRUE ,'123450046','Braude',432422), -- order
(1012,7,'2025-06-12','12:30','2025-06-12','17:46',TRUE,'123450002','Braude',696279),
(1074,8,'2025-06-14','12:45','2025-06-14','17:07',TRUE,'123450064','Braude',839254),
(1042,1,'2025-06-15','10:08','2025-06-15','13:30',FALSE ,'123450032','Braude',432243), -- order
(1057,1,'2025-06-16','07:15','2025-06-16','10:03',FALSE,'123450047','Braude',559860),
(1064,1,'2025-06-17','15:03','2025-06-17','20:20',TRUE ,'123450054','Braude',936731), -- order
(1091,2,'2025-06-17','11:15','2025-06-17','12:47',FALSE,'123450081','Braude',620062),
(1073,1,'2025-06-19','13:30','2025-06-19','17:21',FALSE ,'123450063','Braude',432431), -- order+ late
(1054,9,'2025-06-21','09:00','2025-06-21','14:18',TRUE,'123450044','Braude',650476),
(1078,1,'2025-06-22','09:36','2025-06-22','20:20',TRUE ,'123450068','Braude',342777), -- order + late
(1075,1,'2025-06-26','09:21','2025-06-26','12:25',FALSE ,'123450065','Braude',236732), -- order 
(1066,3,'2025-06-27','13:45','2025-06-27','14:57',TRUE,'123450056','Braude',769960),
(1080,1,'2025-06-28','14:30','2025-06-28','15:20',FALSE ,'123450070','Braude',537812), -- order

/* ---------- July 2025 ---------- */
(1047,1,'2025-07-01','08:45','2025-07-01','15:21',TRUE ,'123450037','Braude',342342), -- order
(1042,3,'2025-07-01','09:30','2025-07-01','12:54',FALSE,'123450032','Braude',324810),
(1063,1,'2025-07-01','10:39','2025-07-01','14:21',FALSE ,'123450053','Braude',728931), -- order
(1027,2,'2025-07-02','06:15','2025-07-02','08:15',TRUE,'123450017','Braude',644856),
(1067,1,'2025-07-03','13:31','2025-07-03','17:05',TRUE ,'123450057','Braude',243987), -- order
(1076,10,'2025-07-03','12:30','2025-07-03','14:55',TRUE,'123450066','Braude',555107),
(1053,1,'2025-07-04','08:29','2025-07-04','19:00',TRUE ,'123450043','Braude',434533), -- order+ late
(1101,3,'2025-07-04','06:15','2025-07-04','07:16',FALSE,'123450091','Braude',918160),
(1043,15,'2025-07-04','16:00','2025-07-04','19:20',FALSE,'123450033','Braude',854499),
(1095,5,'2025-07-05','13:00','2025-07-05','17:43',FALSE,'123450085','Braude',946060),
(1057,20,'2025-07-06','09:15','2025-07-06','10:26',FALSE,'123450047','Braude',549254),
(1045,1,'2025-07-06','09:15','2025-07-06','13:05',FALSE ,'123450035','Braude',009642), -- order
(1067,1,'2025-07-09','15:32','2025-07-09','17:05',FALSE ,'123450057','Braude',232265), -- order
(1099,3,'2025-07-10','07:30','2025-07-10','11:21',FALSE,'123450089','Braude',822273),
(1050, 1,  '2025-07-10', '09:00:00', NULL, NULL, FALSE, '123450001', 'Braude', 900020);

UPDATE bpark.parkingEvent
SET sendMsgForLating = TRUE
WHERE exitDate IS NOT NULL AND exitHour IS NOT NULL
  AND (
    (wasExtended = FALSE AND TIMESTAMPDIFF(HOUR, TIMESTAMP(entryDate, entryHour), TIMESTAMP(exitDate, exitHour)) > 4)
    OR
    (wasExtended = TRUE  AND TIMESTAMPDIFF(HOUR, TIMESTAMP(entryDate, entryHour), TIMESTAMP(exitDate, exitHour)) > 8)
  );

-- Sync: mark spaces as occupied for active events
UPDATE bpark.parkingSpaces
SET is_occupied = TRUE
WHERE parking_space IN (
    SELECT parking_space FROM bpark.parkingEvent WHERE exitDate IS NULL
);

-- Sync: update occupied spots count in lot table
UPDATE bpark.parkingLot
SET occupiedSpots = (
    SELECT COUNT(*) FROM bpark.parkingEvent WHERE NameParkingLot = 'Braude' AND exitDate IS NULL
)
WHERE NameParkingLot = 'Braude';

-- Ensures that the 'eventId' field in the 'parkingevent' table will automatically increment for each new row.
ALTER TABLE bpark.parkingEvent MODIFY eventId INT AUTO_INCREMENT;


CREATE TABLE bpark.parkingReport(
dateOfParkingReport Date PRIMARY KEY,
totalEntries INT,
totalExtends INT,
totalLates INT,
lessThanFourHours INT,
betweenFourToEight INT,
`moreThanEight` INT
);

CREATE TABLE bpark.subscriberStatusReport (
    reportMonth    DATE NOT NULL,          -- always ‘yyyy-MM-01’
    subscriberCode INT  NOT NULL,
    totalEntries   INT  NOT NULL,
    totalExtends   INT  NOT NULL,
    totalLates     INT  NOT NULL,
    totalHours     DOUBLE NOT NULL,
    PRIMARY KEY (reportMonth, subscriberCode),
    FOREIGN KEY (subscriberCode)
        REFERENCES bpark.subscriber(subscriberCode)
);



