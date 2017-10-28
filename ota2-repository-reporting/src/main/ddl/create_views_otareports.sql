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

CREATE OR REPLACE VIEW included_namespaces AS
  SELECT DISTINCT l.base_namespace
  FROM library l
  WHERE l.base_namespace LIKE "http://www.travelport.com/%"
     OR l.base_namespace LIKE "http://project.travelport.com/%";

CREATE OR REPLACE VIEW libraries_by_namespace AS
  SELECT l.base_namespace, l.library_name, COUNT( lv.id ) version_count
  FROM library l, library_version lv
  WHERE l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
    AND lv.library_id = l.id
  GROUP BY l.base_namespace, l.library_name
  ORDER BY l.base_namespace, l.library_name;

CREATE OR REPLACE VIEW libraries_by_month AS
  SELECT COUNT(*) library_count, STR_TO_DATE( CONCAT( DATE_FORMAT( l.create_date, "%Y-%m"), "-01" ), "%Y-%m-%d" ) create_month
  FROM library l
  WHERE l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
  GROUP BY YEAR( l.create_date ), MONTH( l.create_date )
  ORDER BY YEAR( l.create_date ), MONTH( l.create_date );

CREATE OR REPLACE VIEW library_versions_by_month AS
  SELECT COUNT(*) library_version_count, STR_TO_DATE( CONCAT( DATE_FORMAT( lv.create_date, "%Y-%m"), "-01" ), "%Y-%m-%d" ) create_month
  FROM library l, library_version lv
  WHERE lv.library_id = l.id
    AND l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
  GROUP BY YEAR( lv.create_date ), MONTH( lv.create_date )
  ORDER BY YEAR( lv.create_date ), MONTH( lv.create_date );

CREATE OR REPLACE VIEW commits_by_month AS
  SELECT COUNT(lc.id) commit_count, STR_TO_DATE( CONCAT( DATE_FORMAT( lc.commit_date, "%Y-%m"), "-01" ), "%Y-%m-%d" ) commit_month
  FROM library l, library_version lv, library_version_commit lc
  WHERE l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
    AND lv.library_id = l.id
    AND lc.library_version_id = lv.id
  GROUP BY YEAR( lc.commit_date ), MONTH( lc.commit_date )
  ORDER BY YEAR( lc.commit_date ), MONTH( lc.commit_date );
  
CREATE OR REPLACE VIEW entities_by_library AS
  SELECT l.base_namespace, l.library_name, COUNT( e.id ) total_count,
    SUM(CASE WHEN (e.entity_type = 'TLResource') THEN 1 ELSE 0 END) AS resource_count,
    SUM(CASE WHEN (e.entity_type = 'TLService') THEN 1 ELSE 0 END) AS service_count,
    SUM(CASE WHEN (e.entity_type = 'TLBusinessObject') THEN 1 ELSE 0 END) AS business_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLChoiceObject') THEN 1 ELSE 0 END) AS choice_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLCoreObject') THEN 1 ELSE 0 END) AS core_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLContextualFacet') THEN 1 ELSE 0 END) AS contextual_facet_count,
    SUM(CASE WHEN (e.entity_type = 'TLExtensionPointFacet') THEN 1 ELSE 0 END) AS epf_count,
    SUM(CASE WHEN (e.entity_type = 'TLValueWithAttributes') THEN 1 ELSE 0 END) AS vwa_count,
    SUM(CASE WHEN (e.entity_type = 'TLOpenEnumeration') THEN 1 ELSE 0 END) AS open_enum_count,
    SUM(CASE WHEN (e.entity_type = 'TLClosedEnumeration') THEN 1 ELSE 0 END) AS closed_enum_count,
    SUM(CASE WHEN (e.entity_type = 'TLSimple') THEN 1 ELSE 0 END) AS simple_count
  FROM library l, entity e
  WHERE l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
    AND e.library_id = l.id
    AND e.delete_date IS NULL
  GROUP BY l.base_namespace, l.library_name
  ORDER BY l.base_namespace, l.library_name;

CREATE OR REPLACE VIEW entities_by_month AS
  SELECT COUNT( e.id ) total_count,
    SUM(CASE WHEN (e.entity_type = 'TLResource') THEN 1 ELSE 0 END) AS resource_count,
    SUM(CASE WHEN (e.entity_type = 'TLService') THEN 1 ELSE 0 END) AS service_count,
    SUM(CASE WHEN (e.entity_type = 'TLBusinessObject') THEN 1 ELSE 0 END) AS business_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLChoiceObject') THEN 1 ELSE 0 END) AS choice_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLCoreObject') THEN 1 ELSE 0 END) AS core_object_count,
    SUM(CASE WHEN (e.entity_type = 'TLContextualFacet') THEN 1 ELSE 0 END) AS contextual_facet_count,
    SUM(CASE WHEN (e.entity_type = 'TLExtensionPointFacet') THEN 1 ELSE 0 END) AS epf_count,
    SUM(CASE WHEN (e.entity_type = 'TLValueWithAttributes') THEN 1 ELSE 0 END) AS vwa_count,
    SUM(CASE WHEN (e.entity_type = 'TLOpenEnumeration') THEN 1 ELSE 0 END) AS open_enum_count,
    SUM(CASE WHEN (e.entity_type = 'TLClosedEnumeration') THEN 1 ELSE 0 END) AS closed_enum_count,
    SUM(CASE WHEN (e.entity_type = 'TLSimple') THEN 1 ELSE 0 END) AS simple_count,
    STR_TO_DATE( CONCAT( DATE_FORMAT( e.create_date, "%Y-%m"), "-01" ), "%Y-%m-%d" ) create_month
  FROM library l, entity e
  WHERE l.base_namespace IN (SELECT base_namespace FROM included_namespaces)
    AND e.library_id = l.id
    AND e.delete_date IS NULL
  GROUP BY YEAR( e.create_date ), MONTH( e.create_date )
  ORDER BY YEAR( e.create_date ), MONTH( e.create_date );
