package pack.smartwaste.RequestsEntities;

import jakarta.validation.constraints.NotNull;

public class ImageRequest {
    @NotNull(message = "Image URL cannot be null")
    private String image;

    // Constructors
    public ImageRequest() {}
    public ImageRequest(String image) {
        this.image = image;
    }

    // Getter and Setter
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

