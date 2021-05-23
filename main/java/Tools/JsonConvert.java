package Tools;

import com.google.gson.Gson;

import java.lang.reflect.Type;

public class JsonConvert {

    public JsonConvert() {
    }

    public String convertToJson(Object o) {
        Gson g = new Gson();
        String json = g.toJson(o);

        return json;
    }

    public Object convertFromJson(String json, Type t) {
        Gson g = new Gson();
        Object o = g.fromJson(json, t);
        return o;
    }
}
