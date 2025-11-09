package dev.thevindu.readless;

import lombok.Data;

@Data
public class ReadlessRequest {
    private String content;
    private String operation;
}
