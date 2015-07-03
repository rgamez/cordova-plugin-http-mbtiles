package uk.ac.edina.mobile.cordova.plugins.httpmbtiles;

import fi.iki.elonen.NanoHTTPD;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;


public class HTTPMBTilesServer extends CordovaPlugin {

    private static final String TAG = "HTTPMBTiles";
    private HTTPServer httpServer;
    private final Pattern tilePattern = Pattern.compile("/(.*?)/([0-9]+)/([0-9]+)/([0-9]+)\\.");
    private HashMap<String, MBTiles> layers;


    public HTTPMBTilesServer() {
        super();

        layers = new HashMap<String, MBTiles>();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("startServer")) {
            this.startServer(callbackContext);
            return true;
        } else if (action.equals("addTiles")) {
            String layerName = args.getString(0);
            String fileName = args.getString(1);
            this.addTiles(layerName, fileName, callbackContext);
            return true;
        }

        return false;
    }


    public void startServer(CallbackContext callbackContext) {
        int listeningPort;

        httpServer = new HTTPServer(0);

        try {
            httpServer.start();
            listeningPort = httpServer.getListeningPort();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("port", Integer.toString(listeningPort));
            callbackContext.success(jsonObject);
        }catch(Exception ex){
            Log.e(TAG, ex.toString());
            callbackContext.error(ex.toString());
        }

    }

    public void addTiles(String layerName, String fileName, CallbackContext callbackContext) {
        try {
            JSONObject jsonObject = new JSONObject();
            MBTiles mbTiles = new MBTiles(fileName);
            double[] bounds = mbTiles.getBounds();

            layers.put(layerName, mbTiles);
            jsonObject.put("error", 0);

            if (bounds != null) {
                JSONArray boundsJSON = new JSONArray();
                for (int i = 0; i < bounds.length; i++) {
                    boundsJSON.put(bounds[i]);
                }
                jsonObject.put("bounds", boundsJSON);
            }

            callbackContext.success(jsonObject);
        } catch(Exception ex) {
            Log.e(TAG, ex.toString());
            callbackContext.error(ex.toString());
        }
    }

    class HTTPServer extends NanoHTTPD {

        public HTTPServer(int port) {
            super(port);
        }

        @Override public Response serve(IHTTPSession session) {
            NanoHTTPD.Response response;
            Method method = session.getMethod();
            String uri = session.getUri();
            Log.d(TAG, method + " '" + uri + "' ");

            Matcher matcher = tilePattern.matcher(uri);
            if(!matcher.find()) {
                response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not found");
            } else {
                String layerName = matcher.group(1);
                int z = Integer.parseInt(matcher.group(2));
                int x = Integer.parseInt(matcher.group(3));
                int y = Integer.parseInt(matcher.group(4));

                MBTiles mbTiles = layers.get(layerName);

                if (mbTiles != null) {
                    try {
                        byte[] tile = mbTiles.getTile(z, x, y);
                        if (tile != null) {
                            response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, "image/png", new ByteArrayInputStream(tile));
                        } else {
                            response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Tile not found");
                        }
                    } catch(Exception ex) {
                        Log.e(TAG, ex.toString());
                        response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.INTERNAL_ERROR, NanoHTTPD.MIME_PLAINTEXT, ex.toString());
                    }
                } else {
                    response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Layer not found");
                }
            }

            return response;
        }
    }

}
