package com.ft.aem.poet.frost;

/**
 * Represents Frost response
 */
public class FrostResponse {

    private String content;
    private int responseCode;
    private String responseCodeText;

    public FrostResponse(String content, int responseCode, String responseCodeText) {
        this.content = content;
        this.responseCode = responseCode;
        this.responseCodeText = responseCodeText;
    }

    /**
     * the frost response content, typically the work id as json
     *
     * @return frost content
     */
    public String getContent() {
        return content;
    }

    /**
     * response text from calling frost
     *
     * @return response text
     */
    public String getResponseCodeText() {
        return content;
    }

    /**
     * response code from calling frost
     *
     * @return response code
     */
    public int getResponseCode() {
        return responseCode;
    }
}
