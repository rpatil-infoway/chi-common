package ca.infoway.confluence.plugins.common.spaceInfo;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.atlassian.confluence.security.SpacePermission;

public class CustomSpacePermissions {
	
    public static final Collection<String> CONFLUENCE_ADMINISTRATOR_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
        
//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        CONFLUENCE_ADMINISTRATOR_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    
    public static final Collection<String> INFOWAY_DOC_ADMINS_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
        
        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);

//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
//        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
//        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        INFOWAY_DOC_ADMINS_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    
    public static final Collection<String> DOC_ADMINS_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
        
        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);

//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
//        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
//        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        DOC_ADMINS_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    
    public static final Collection<String> AUTHORS_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
        
        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);

//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
//        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
//        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        AUTHORS_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    public static final Collection<String> REVIEWERS_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
//        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
//        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
//        
        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);

//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
//        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
//        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
//        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
//        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        REVIEWERS_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    
    public static final Collection<String> INFOWAY_USERS_PERMISSION_TYPES;
    static
    {
        List<String> permissionTypes = new ArrayList<String>();
        
        permissionTypes.add(SpacePermission.VIEWSPACE_PERMISSION);
        
//        permissionTypes.add(SpacePermission.CREATEEDIT_PAGE_PERMISSION); 
//        permissionTypes.add(SpacePermission.REMOVE_PAGE_PERMISSION);
//        
//        permissionTypes.add(SpacePermission.COMMENT_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_COMMENT_PERMISSION);

//        permissionTypes.add(SpacePermission.EDITBLOG_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_BLOG_PERMISSION);
        
//        permissionTypes.add(SpacePermission.CREATE_ATTACHMENT_PERMISSION);
//        permissionTypes.add(SpacePermission.REMOVE_ATTACHMENT_PERMISSION);
        
//        permissionTypes.add(SpacePermission.SET_PAGE_PERMISSIONS_PERMISSION);
       
//        permissionTypes.add(SpacePermission.REMOVE_MAIL_PERMISSION);
        
        permissionTypes.add(SpacePermission.EXPORT_SPACE_PERMISSION);
//        permissionTypes.add(SpacePermission.ADMINISTER_SPACE_PERMISSION);

        INFOWAY_USERS_PERMISSION_TYPES = unmodifiableList(permissionTypes);
    }
    
   
    
}
