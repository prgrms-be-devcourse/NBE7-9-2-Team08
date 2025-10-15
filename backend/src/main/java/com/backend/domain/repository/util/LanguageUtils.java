package com.backend.domain.repository.util;

import com.backend.domain.repository.entity.Language;

public class LanguageUtils {
    public static Language fromGitHubName(String name) {
        if(name == null || name.isBlank()) return Language.OTHER;

        String normalized = name.trim().toLowerCase()
                .replace("+", "P")
                .replace("#", "sharp")
                .replace("-", "")
                .replace(" ", "");

        return switch (normalized) {
            case "java" -> Language.JAVA;
            case "python" -> Language.PYTHON;
            case "c" -> Language.C;
            case "cpp" -> Language.CPP;
            case "csharp" -> Language.CSHARP;
            case "go" -> Language.GO;
            case "rust" -> Language.RUST;
            case "kotlin" -> Language.KOTLIN;
            case "swift" -> Language.SWIFT;
            case "ruby" -> Language.RUBY;
            case "php" -> Language.PHP;
            case "objectivc", "objectivec" -> Language.OBJECTIVEC;
            case "javascript" -> Language.JAVASCRIPT;
            case "typescript" -> Language.TYPESCRIPT;
            case "html" -> Language.HTML;
            case "css" -> Language.CSS;
            case "scss", "sass" -> Language.SCSS;
            case "vue" -> Language.VUE;
            case "r" -> Language.R;
            case "sql" -> Language.SQL;
            case "shell", "bash", "sh" -> Language.SHELL;
            case "yaml", "yml" -> Language.YAML;
            case "json" -> Language.JSON;
            case "perl" -> Language.PERL;
            case "dart" -> Language.DART;
            case "haskell" -> Language.HASKELL;

            default -> Language.OTHER;
        };
    }
}
