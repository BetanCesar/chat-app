package mx.itesm.edu.earthone.chatapp.pojo;

/**
 * Created by Cesar on 27/02/18.
 */

public class ChatPojo {
    private String name, imageURL, message;

    public ChatPojo() {
    }

    public ChatPojo(String name, String imageURL, String message) {
        this.name = name;
        this.imageURL = imageURL;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
