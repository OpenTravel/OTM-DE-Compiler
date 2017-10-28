#
# Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

CREATE TABLE library (
	id BIGINT NOT NULL AUTO_INCREMENT,
	library_name VARCHAR(50) NOT NULL,
	base_namespace VARCHAR(200) NOT NULL,
	create_date DATETIME NOT NULL,
	PRIMARY KEY (id)
);

CREATE TABLE library_version (
	id BIGINT NOT NULL AUTO_INCREMENT,
	library_id BIGINT NOT NULL,
	version_identifier VARCHAR(50) NOT NULL,
	major_version INT NOT NULL,
	minor_version INT NOT NULL,
	patch_version INT NOT NULL,
	create_date DATETIME NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (library_id) REFERENCES library(id)
);

CREATE TABLE library_version_commit (
	id BIGINT NOT NULL AUTO_INCREMENT,
	library_version_id BIGINT NOT NULL,
	commit_number INT NOT NULL,
	commit_date DATETIME NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (library_version_id) REFERENCES library_version(id)
);

CREATE TABLE entity (
	id BIGINT NOT NULL AUTO_INCREMENT,
	library_id BIGINT NOT NULL,
	entity_name VARCHAR(200) NOT NULL,
	entity_type VARCHAR(50) NOT NULL,
	create_date DATETIME NOT NULL,
	delete_date DATETIME,
	PRIMARY KEY (id),
	FOREIGN KEY (library_id) REFERENCES library(id)
);

CREATE TABLE entity_version (
	id BIGINT NOT NULL AUTO_INCREMENT,
	entity_id BIGINT NOT NULL,
	library_version_id BIGINT NOT NULL,
	create_date DATETIME NOT NULL,
	delete_date DATETIME,
	PRIMARY KEY (id),
	FOREIGN KEY (entity_id) REFERENCES entity(id),
	FOREIGN KEY (library_version_id) REFERENCES library_version(id)
);

CREATE TABLE hibernate_sequence (
	next_val BIGINT(20)
);
INSERT INTO hibernate_sequence VALUES ( 1000 );

SOURCE create_views_otareports.sql
