package com.sfvtech.payperview.utils;

import android.webkit.MimeTypeMap;

public class DownloadUtils {

    /**
     * Used to get MimeType from url.
     *
     * @param url Url.
     * @return Mime Type for the given url.
     */
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }
}
