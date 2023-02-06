package com.techwhizer.snsbiosystem.app;

public class UrlConfig {
   public static String getRootUrl(){return "http://localhost:8081";};

   public static  String getLoginUrl(){ return getRootUrl()+"/v1/auth/authenticate"; }
   public static  String getPasswordResetLinkUrl(){ return getRootUrl()+"/v1/auth/password-reset-link"; }
   public static  String getCheckUsernameUrl(){ return getRootUrl()+"/v1/auth/check-username"; }
   public static  String getResetPasswordUrl(){ return getRootUrl()+"/v1/auth/reset-password"; }
   public static  String getChangePasswordUrl(){ return getRootUrl()+"/v1/auth/change-password"; }
   public static  String getDashboardUrl(){ return getRootUrl()+"/v2/admin/dashboard"; }
   public static  String getAllUsersUrl(){ return getRootUrl()+"/v2/admin/users"; }
   public static  String getUserprofileUrl(){ return getRootUrl()+"/v1/user/"; }
   public static  String getUserDeleteUrl(){ return getRootUrl()+"/v1/user/"; }
   public static  String getProfileCreateUrl(){ return getRootUrl()+"/v2/admin/users"; }
   public static  String getPreviewProfileCsvUrl(){ return getRootUrl()+"/v2/admin/users/preview-csv"; }

   public static String getSterilizerPreviewUrl(){ return getRootUrl()+"/v2/admin/sterilizer/preview-csv"; }
   public static String getAddSterilizerUrl(){ return getRootUrl()+"/v1/sterilizer"; }
   public static String getDeleteSterilizerUrl(){ return getRootUrl()+"/v2/admin/sterilizer/"; }

   // sorting option

   public static String getUserSortingOptionUrl(){ return getRootUrl()+"/v2/admin/users/sorting-options"; }
   public static  String getRoleOptionUrl(){ return getRootUrl()+"/v2/admin/users/role-options"; }
   public static String getSterilizerSortingOptionUrl(){ return getRootUrl()+"/v2/admin/sterilizer/sorting-options"; }
   public static String getKitSortingOptionUrl(){ return getRootUrl()+"/v2/admin/kit/sorting-options"; }
   public static String getKitUsageSortingOptionUrl(){ return getRootUrl()+"/v2/admin/kit-usage/sorting-options"; }
   public static String getTestResultOptionUrl(){ return getRootUrl()+"/v1/kit/test-result-options"; }


   // kits

   public static String getDeleteKitUrl(){ return getRootUrl()+"/v2/admin/kit/"; }
   public static String getGetKitsUrl(){ return getRootUrl()+"/v1/kit"; }
   public static String getPreviewKitsUrl(){ return getRootUrl()+"/v2/admin/kit/preview-csv"; }

   // kits usages
   public static String getKitsUsagesUrl(){ return getRootUrl()+"/v1/kit/usage"; }
   public static String getKitsUsagesPreviewUrl(){ return getRootUrl()+"/v2/admin/kit/usage/preview-csv"; }

   // report
   public static String getKitReportUrl(){ return getRootUrl()+"/v1/kit/report"; }

   // notice
   public static String getKitNoticeUrl(){ return getRootUrl()+"/v2/admin/notice-board"; }

}
