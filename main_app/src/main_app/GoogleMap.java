/*
 * license: standard library, none
 * this file is used to create a html 
 * file and display a google map for user
 */
package main_app;

import java.io.PrintWriter;
import java.io.*;
import java.awt.*;

//create a google map
public class GoogleMap {
    private float BASET = 20;
    private float BASEW = 5;
    private float BASES = 1000;
    

    
    /*begin to run the browser
    parameter none
    return none*/
    public void runMap() throws Exception {
        createHtml();
        File htmlFile = new File("engg2800map.html");
        Desktop.getDesktop().browse(htmlFile.toURI());
        //Application.launch();
    }
    
    //create a html of google map
    public void createHtml()throws Exception {
        int start, end;
        if (IO.lat.size() > 10) {
            start = IO.lat.size() - 10;
            end = IO.lat.size();
            
        } else {
            start = 0;
            end = IO.lat.size();
        }
            
        PrintWriter f = new PrintWriter("engg2800map.html", "UTF-8");
        f.println("<!DOCTYPE html>\n" +
"<html>\n" +
"  <head>\n" +
"    <meta charset=\"utf-8\">\n" +
"    <title>Heatmaps</title>\n" +
"    <style>\n" +
"      html, body {\n" +
"        height: 100%;\n" +
"        margin: 0;\n" +
"        padding: 0;\n" +
"      }\n" +
"      #map {\n" +
"        height: 100%;\n" +
"      }\n" +
"\n" +
"\n" +
"      #floating-panel {\n" +
"        background-color: #fff;\n" +
"        border: 1px solid #999;\n" +
"        left: 25%;\n" +
"        padding: 6px;\n" +
"        position: absolute;\n" +
"        top: 11px;\n" +
"        z-index: 5;\n" +
"      }\n" +
"    </style>\n" +
"  </head>\n" +
"\n" +
"  <body>\n" +
"      <div id=\"floating-panel\">\n" +
"      <button onclick=\"Tmap()\">Temperature</button>\n" +
"      <button onclick=\"Wmap()\">wind speed</button>\n" +
"      <button onclick=\"Smap()\">sunshine</button>\n" +
"    </div>\n" +
"    <div id=\"map\"></div>\n" +
"    <script>\n" +
"\n" +
"var map, heatmap;\n" +
"var myLatLng = [");
        for (int i = start; i < end ; i++) {
            if (i == IO.lat.size() - 1) {
                f.printf("['Time: %s; T: %f; W: %f; S: %f', %f, %f]\n", IO.mapTimeIO.get(i) ,IO.mapT.get(i), IO.mapW.get(i), 
                        IO.mapSIO.get(i),((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)),  
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)));
            } else {
                f.printf("['Time: %s; T: %f; W: %f; S: %f', %f, %f],\n", IO.mapTimeIO.get(i) ,IO.mapT.get(i), IO.mapW.get(i), 
                        IO.mapSIO.get(i),((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)));
            }
        }
        
        f.println("]; \n" +
"function initMap() {\n" +
"  map = new google.maps.Map(document.getElementById('map'), {\n" +
"    zoom: 13,center: new google.maps.LatLng(myLatLng[0][1], myLatLng[0][2]),\n" +
"    mapTypeId: google.maps.MapTypeId.SATELLITE\n" +
"  });\n" +
"\n" +
"  heatmap = new google.maps.visualization.HeatmapLayer({\n" +
"    data: getPointsT(),\n" +
"    map: map\n" +
"  });\n" +
"  heatmap.set('radius', 25);\n" +
"    var infowindow = new google.maps.InfoWindow();\n" +
"  var i;\n" +
"  for (i = 0; i < myLatLng.length; i++) {\n" +
"      marker = new google.maps.Marker({\n" +
"        position: new google.maps.LatLng(myLatLng[i][1], myLatLng[i][2]),\n" +
"        map: map\n" +
"        \n" +
"      });\n" +
"      \n" +
"    google.maps.event.addListener(marker, 'click', (function(marker, i) {\n" +
"        return function() {\n" +
"          infowindow.setContent(myLatLng[i][0]);\n" +
"          infowindow.open(map, marker);\n" +
"        };\n" +
"      })(marker, i));\n" +
"     \n" +
"      \n" +
"  }\n" +
"}\n" +
"\n" +
"function Tmap() {\n" +
"    heatmap.setMap(null);\n" +
"    heatmap = new google.maps.visualization.HeatmapLayer({\n" +
"    data: getPointsT(),\n" +
"    map: map\n" +
"  }); \n" +
"  heatmap.set('radius', 25);\n" +
"}\n" +
"\n" +
"function Wmap() {   \n" +
"    heatmap.setMap(null);\n" +
"    heatmap = new google.maps.visualization.HeatmapLayer({\n" +
"    data: getPointsW(),\n" +
"    map: map\n" +
"  }); \n" +
"  heatmap.set('radius', 25);\n" +
"}\n" +
"\n" +
"function Smap() {   \n" +
"    heatmap.setMap(null);\n" +
"    heatmap = new google.maps.visualization.HeatmapLayer({\n" +
"    data: getPointsS(),\n" +
"    map: map\n" +
"  }); \n" +
"  heatmap.set('radius', 25);"
                + "}");
    f.flush();
    
    f.println("function getPointsT() {\n" +
"  return [");
    for (int i = start; i < end; i++) {
        if (i == IO.lat.size() - 1) {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f}\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), Math.abs(IO.mapT.get(i) / BASET));
        } else {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f},\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), Math.abs(IO.mapT.get(i) / BASET));
        }
    }
    
        
    f.println("];\n" +
"}");    
    
    f.println("function getPointsW() {\n" +
"  return [");
    for (int i = start; i < end; i++) {
        if (i == IO.lat.size() - 1) {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f}\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), IO.mapW.get(i) / BASEW);
        } else {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f},\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), IO.mapW.get(i) / BASEW);
        }
    }
    
        
    f.println("];\n" +
"}");
    
    f.println("function getPointsS() {\n" +
"  return [");
    for (int i = start; i < end; i++) {
        if (i == IO.lat.size() - 1) {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f}\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), IO.mapSIO.get(i) / BASES);
        } else {
            f.printf("{location: new google.maps.LatLng(%f, %f), weight: %f},\n", ((int)(IO.lat.get(i) / 100) +  (IO.lat.get(i) % 100 / 60.0)), 
                        ((int)(IO.lon.get(i) / 100) +  (IO.lon.get(i) % 100 / 60.0)), IO.mapSIO.get(i) / BASES);
        }
    }
    
        
    f.println("];\n" +
"}"
            + "</script>\n" +
"    <script async defer\n" +
"        src=\"https://maps.googleapis.com/maps/api/js?key=AIzaSyDyrXBYup5k3STj8LnB5OtPLfwBpDMvbiU&signed_in=true&libraries=visualization&callback=initMap\">\n" +
"    </script>\n" +
"  </body>\n" +
"</html>");
        
        f.flush();
        f.close();
    }
}