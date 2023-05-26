package com.tudorvalentine.augmentedimages.app;

public class AppConfig {
    private static boolean local = true;
    private static String localhost = "192.168.8.128:8080";
    public static final String URL_REGISTER = local ? "http://"+ localhost +"/app/register/" : "https://augmented-images.herokuapp.com/app/register" ;
    public static final String URL_AUTH = local ? "http://"+ localhost +"/app/login/" : "https://augmented-images.herokuapp.com/app/login";
    public static final String URL_IMAGES = local ? "http://"+ localhost +"/app/images/" : "https://augmented-images.herokuapp.com/app/images";
    public static final String URL_PREV = local ? "http://"+ localhost +"/app/prev/" : "https://augmented-images.herokuapp.com/app/images";
    public static final String URL_PDF = local ? "http://"+ localhost +"/app/pdf/" : "https://augmented-images.herokuapp.com/app/pdf/";
    public static final String URL_SYNC = local ? "http://"+ localhost +"/app/sync/" : "https://augmented-images.herokuapp.com/app/sync/";
    public static final String URL_UPLOAD_DOC = local ? "http://"+ localhost +"/app/upload-document/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_UPLOAD_IMAGES = local ? "http://"+ localhost +"/app/upload-image/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_TESTIMG = local ? "http://"+ localhost +"/app/testimg/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_CREATE_ASSOC = local ? "http://"+ localhost +"/app/create-assoc/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_UPLOAD_DOC_PREV = local ? "http://"+ localhost +"/app/upload-document-prev/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_GET_IMGDB = local ? "http://"+ localhost +"/app/imgdb/" : "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_DELETE_ASSOC = local ? "http://"+ localhost +"/app/delete/": "https://augmented-images.herokuapp.com/app/upload/";
    public static final String URL_RESTORE_ASSOC = local ? "http://"+ localhost +"/app/restore/": "https://augmented-images.herokuapp.com/app/upload/";

}
