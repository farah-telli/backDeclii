package tn.example.backdeclitech.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "✅ Backend accessible!";
    }

    @GetMapping("/check-folder")
    public String checkFolder() {
        File dir = new File("uploads/news");
        StringBuilder sb = new StringBuilder();

        sb.append("📂 Chemin: ").append(dir.getAbsolutePath()).append("\n");
        sb.append("📋 Existe: ").append(dir.exists()).append("\n");

        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                sb.append("📁 ").append(files.length).append(" fichiers:\n");
                for (File f : files) {
                    sb.append("  - ").append(f.getName())
                            .append(" (").append(f.length()).append(" bytes)\n");
                }
            }
        }

        return sb.toString();
    }
}