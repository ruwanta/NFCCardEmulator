package com.okanatas.nfccardemulator.control;

import android.util.Log;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FileUploadHttpServer extends NanoHTTPD {


    public FileUploadHttpServer(int port) {
        super(port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.POST.equals(method) && "/upload".equals(uri)) {
            try {
                Map<String, String> files = new java.util.HashMap<>();
                session.parseBody(files);  // parses the body and populates `files`

                // Get uploaded file temp path (NanoHTTPD saves to a temp file)
                for (Map.Entry<String, String> entry : files.entrySet()) {
                    String formField = entry.getKey();
                    String tempFilePath = entry.getValue();

                    Log.d("UPLOAD", "Field: " + formField + " TempFile: " + tempFilePath);

                    // Save to your desired location on Android device
                    File tempFile = new File(tempFilePath);
                    File destFile = new File(getUploadDir(), "uploaded_file_" + System.currentTimeMillis());

                    if (tempFile.renameTo(destFile)) {
                        return newFixedLengthResponse("File uploaded successfully: " + destFile.getAbsolutePath());
                    } else {
                        return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "File save failed");
                    }
                }

            } catch (IOException | ResponseException e) {
                Log.e("UPLOAD", "Error", e);
                return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Upload failed: " + e.getMessage());
            }
        }

        return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Not Found");
    }

    private File getUploadDir() {
        // Make sure this is a writable directory (e.g., app's internal storage)
        File uploadDir = new File("/sdcard/UploadTest"); // Or context.getExternalFilesDir()
        if (!uploadDir.exists()) uploadDir.mkdirs();
        return uploadDir;
    }
}

