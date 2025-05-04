package pack.smartwaste.RequestsEntities;

import jakarta.validation.constraints.NotNull;

public class TextRequest {
    @NotNull(message = "Text cannot be null")
    private String text;

    // Constructors
    public TextRequest() {}
    public TextRequest(String image) {
        this.text = image;
    }

    // Getter and Setter
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

