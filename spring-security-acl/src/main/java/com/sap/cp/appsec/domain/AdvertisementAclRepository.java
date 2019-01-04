package com.sap.cp.appsec.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdvertisementAclRepository extends PagingAndSortingRepository<Advertisement, Long> {

    String FIND_ADS_FOR_SID_SUBQUERY =
            "FROM ACL_OBJECT_IDENTITY obj " +
                    "INNER JOIN ACL_ENTRY entry   ON entry.acl_object_identity = obj.id " +
                    "INNER JOIN ACL_SID sid       ON entry.sid = sid.id " +
                    "INNER JOIN ADVERTISEMENT ads ON CAST(obj.object_id_identity as bigint) = ads.id " +
                "WHERE sid.sid IN :sid " +
                    "AND (entry.mask = :mask) " +
                    "AND entry.granting = true " +
                    "AND obj.object_id_class = (SELECT id FROM ACL_CLASS WHERE acl_class.class = 'com.sap.cp.appsec.domain.Advertisement')";

    String GET_ALL_ACCESSIBLE_OBJECTS_RECURSIVE_CTE =
            "WITH RECURSIVE accessibleObjects (id, object_id_class, object_id_identity, parent_object) AS " +
            "( SELECT    id, " +
                        "object_id_class, " +
                        "object_id_identity, " +
                        "parent_object " +
            "  FROM     ACL_OBJECT_IDENTITY " +
            "  WHERE    id IN (SELECT DISTINCT acl_object_identity " +
                                "FROM ACL_ENTRY entry " +
                                "INNER JOIN ACL_SID sid              ON entry.sid = sid.id " +
                                "INNER JOIN ACL_OBJECT_IDENTITY obj  ON entry.acl_object_identity = obj.id " +
                                "WHERE " +
                                        "entry.granting = true " + // AND ENTRIES_INHERITING = true to exclude leafs
                                        "AND entry.mask = :mask " +
                                        "AND sid.sid IN :sid " +
                                ") " +
            "  UNION ALL " +
                    "SELECT  parent.id, " +
                            "parent.object_id_class, " +
                            "parent.object_id_identity, " +
                            "parent.parent_object " +
                    "FROM    ACL_OBJECT_IDENTITY parent, accessibleObjects " +
                    "WHERE	 parent.parent_object = accessibleObjects.id " +
            ") ";

    String FIND_ADS_FOR_SID_IN_HIERARCHY_SUBQUERY =
	        "FROM accessibleObjects " +
            "INNER JOIN ADVERTISEMENT ads ON CAST(object_id_identity AS bigint) = ads.id " +
                "WHERE ads.is_published = TRUE " +
                    "AND object_id_class = ( " +
                        "SELECT id FROM ACL_CLASS WHERE acl_class.class = 'com.sap.cp.appsec.domain.Advertisement' ) " +
                "ORDER BY ads.id DESC ";


    String SELECT_ADS_FOR_SID_QUERY = "SELECT DISTINCT ads.*"
            + FIND_ADS_FOR_SID_SUBQUERY;

    String COUNT_ADS_FOR_SID_QUERY = "SELECT COUNT( DISTINCT ads.id) " + FIND_ADS_FOR_SID_SUBQUERY;

    String SELECT_ADS_FOR_SID_IN_HIERARCHY_QUERY = GET_ALL_ACCESSIBLE_OBJECTS_RECURSIVE_CTE +
            "SELECT DISTINCT ads.* " + FIND_ADS_FOR_SID_IN_HIERARCHY_SUBQUERY;

    String COUNT_ADS_FOR_SID_IN_HIERARCHY_QUERY = GET_ALL_ACCESSIBLE_OBJECTS_RECURSIVE_CTE +
            "SELECT COUNT(DISTINCT ads.id) " + FIND_ADS_FOR_SID_IN_HIERARCHY_SUBQUERY;


    @Query(value = SELECT_ADS_FOR_SID_QUERY, countQuery = COUNT_ADS_FOR_SID_QUERY, nativeQuery = true)
    Page<Advertisement> findAllByPermission(@Param("mask") int permissionCode, @Param("sid") String[] sid, Pageable pageable);

    @Query(value = SELECT_ADS_FOR_SID_IN_HIERARCHY_QUERY, countQuery = COUNT_ADS_FOR_SID_IN_HIERARCHY_QUERY, nativeQuery = true)
    Page<Advertisement> findAllPublishedByHierarchicalPermission(
            @Param("mask") int permissionCode, @Param("sid") String[] sid, Pageable pageable);

    List<Advertisement> findByTitle(String title);
}
