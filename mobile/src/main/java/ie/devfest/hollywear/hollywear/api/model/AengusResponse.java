package ie.devfest.hollywear.hollywear.api.model;

import com.google.gson.JsonElement;

/**
 * Created by maui on 31.10.2014.
 */
public class AengusResponse {

    private final String category;
    private final JsonElement result;

    public AengusResponse(String category, JsonElement result) {
        this.category = category;
        this.result = result;
    }

    public String getCategory() {
        return category;
    }

    public JsonElement getResult() {
        return result;
    }
}
