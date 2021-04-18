package ru.otus.vcs.objects;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static ru.otus.vcs.utils.Utils.decompress;

public class Main {

    public static void main(String[] args) throws IOException {
        final var file = Path.of("gitobject.example");
        final var content = new String(decompress(Files.readAllBytes(file)));
        Files.write(Path.of("final-project", "src", "test", "resources", "commit"), content.getBytes(StandardCharsets.UTF_8));
    }
}
