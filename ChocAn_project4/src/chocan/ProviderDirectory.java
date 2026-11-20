package chocan;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Lists service codes, names, and fees alphabetically. Author: Faizan Khan. */
public class ProviderDirectory {
    private final ServiceManager services;
    public ProviderDirectory(ServiceManager services) { this.services = services; }
    public String directoryText() {
        StringBuilder text = new StringBuilder("CHOCAN PROVIDER DIRECTORY\nDemonstration service catalog\n\n");
        text.append(String.format("%-8s %-30s %10s%n", "Code", "Service", "Fee"));
        for (Service s : services.getServices())
            text.append(String.format("%-8s %-30s %10s%n", s.code(), s.name(), "$" + s.fee()));
        return text.toString();
    }
    public void displayDirectory() { System.out.println(directoryText()); }
    public Path generateDirectoryFile() {
        Path file = Path.of("reports", "provider-directory.txt");
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, directoryText(), StandardCharsets.UTF_8);
        } catch (IOException e) { throw new UncheckedIOException("Could not write directory", e); }
        System.out.println("Directory saved: " + file.toAbsolutePath());
        return file;
    }
}
