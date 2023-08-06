package tool;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.awt.geom.Point2D;
import java.io.IOException;

public class Point2DAdapter extends TypeAdapter<Point2D> {
    @Override
    public void write(JsonWriter jsonWriter, Point2D point2D) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name("longitude").value(point2D.getX());
        jsonWriter.name("latitude").value(point2D.getY());
        jsonWriter.endObject();
    }

    @Override
    public Point2D read(JsonReader jsonReader) throws IOException {
        jsonReader.beginObject();
        double x = 0.0;
        double y = 0.0;
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            if (name.equals("longitude")) {
                x = jsonReader.nextDouble();
            } else if (name.equals("latitude")) {
                y = jsonReader.nextDouble();
            }
        }
        jsonReader.endObject();
        return new Point2D.Double(x, y);
    }
}
