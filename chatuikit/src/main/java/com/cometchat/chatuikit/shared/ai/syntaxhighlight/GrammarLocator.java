package com.cometchat.chatuikit.shared.ai.syntaxhighlight;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_brainfuck;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_c;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_clike;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_clojure;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_cpp;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_csharp;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_css;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_css_extras;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_dart;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_git;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_go;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_groovy;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_java;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_javascript;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_json;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_kotlin;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_latex;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_makefile;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_markdown;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_markup;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_python;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_scala;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_sql;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_swift;
import com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages.Prism_yaml;

import java.util.HashSet;
import java.util.Set;

import io.noties.prism4j.Prism4j;

public class GrammarLocator implements io.noties.prism4j.GrammarLocator {

    @Nullable
    @Override
    public Prism4j.Grammar grammar(@NonNull Prism4j prism4j, @NonNull String language) {
        switch (language) {
            case "brainfuck": return Prism_brainfuck.create(prism4j);
            case "c": return Prism_c.create(prism4j);
            case "clike": return Prism_clike.create(prism4j);
            case "clojure": return Prism_clojure.create(prism4j);
            case "cpp": return Prism_cpp.create(prism4j);
            case "csharp": return Prism_csharp.create(prism4j);
            case "css": return Prism_css.create(prism4j);
            case "css_extras": return Prism_css_extras.create(prism4j);
            case "dart": return Prism_dart.create(prism4j);
            case "git": return Prism_git.create(prism4j);
            case "go": return Prism_go.create(prism4j);
            case "groovy": return Prism_groovy.create(prism4j);
            case "java": return Prism_java.create(prism4j);
            case "javascript": return Prism_javascript.create(prism4j);
            case "json": return Prism_json.create(prism4j);
            case "kotlin": return Prism_kotlin.create(prism4j);
            case "latex": return Prism_latex.create(prism4j);
            case "makefile": return Prism_makefile.create(prism4j);
            case "markdown": return Prism_markdown.create(prism4j);
            case "markup": return Prism_markup.create(prism4j);
            case "python": return Prism_python.create(prism4j);
            case "scala": return Prism_scala.create(prism4j);
            case "sql": return Prism_sql.create(prism4j);
            case "swift": return Prism_swift.create(prism4j);
            case "yaml": return Prism_yaml.create(prism4j);
            default: return null;
        }
    }

    @NonNull
    @Override
    public Set<String> languages() {
        Set<String> set = new HashSet<>();
        set.add("brainfuck");
        set.add("c");
        set.add("clike");
        set.add("clojure");
        set.add("cpp");
        set.add("csharp");
        set.add("css");
        set.add("css_extras");
        set.add("dart");
        set.add("git");
        set.add("go");
        set.add("groovy");
        set.add("java");
        set.add("javascript");
        set.add("json");
        set.add("kotlin");
        set.add("latex");
        set.add("makefile");
        set.add("markdown");
        set.add("markup");
        set.add("python");
        set.add("scala");
        set.add("sql");
        set.add("swift");
        set.add("yaml");
        return set;
    }
}
