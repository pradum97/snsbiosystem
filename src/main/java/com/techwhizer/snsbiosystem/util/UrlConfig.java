package com.techwhizer.snsbiosystem.util;

public class UrlConfig {
   public static String getRootUrl(){return "http://localhost:8080";};

   public static  String getLoginUrl(){ return getRootUrl()+"/v1/auth/authenticate"; }
   public static  String getPasswordResetLinkUrl(){ return getRootUrl()+"/v1/auth/password-reset-link"; }
   public static  String getCheckUsernameUrl(){ return getRootUrl()+"/v1/auth/check-username"; }
   public static  String getResetPasswordUrl(){ return getRootUrl()+"/v1/auth/reset-password"; }
   public static  String getChangePasswordUrl(){ return getRootUrl()+"/v1/auth/change-password"; }
   public static  String getDashboardUrl(){ return getRootUrl()+"/v2/admin/dashboard"; }
   public static  String getRoleOptionUrl(){ return getRootUrl()+"/v2/admin/users/role-options"; }
   public static  String getAllUsersUrl(){ return getRootUrl()+"/v2/admin/users"; }
   public static  String getUserprofileUrl(){ return getRootUrl()+"/v1/user/"; }
   public static  String getUserDeleteUrl(){ return getRootUrl()+"/v1/user/"; }
   public static  String getProfileCreateUrl(){ return getRootUrl()+"/v2/admin/users"; }
   public static  String getPreviewProfileCsvUrl(){ return getRootUrl()+"/v2/admin/users/preview-csv"; }

   public static String getSterilizerPreviewUrl(){ return getRootUrl()+"/v2/admin/sterilizer/preview-csv"; }
}
