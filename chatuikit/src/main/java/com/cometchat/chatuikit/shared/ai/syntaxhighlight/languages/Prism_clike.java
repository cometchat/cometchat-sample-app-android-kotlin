package com.cometchat.chatuikit.shared.ai.syntaxhighlight.languages;

import static java.util.regex.Pattern.compile;
import static io.noties.prism4j.Prism4j.grammar;
import static io.noties.prism4j.Prism4j.pattern;
import static io.noties.prism4j.Prism4j.token;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import io.noties.prism4j.Prism4j;

@SuppressWarnings("unused")
public abstract class Prism_clike {

  @NotNull
  public static Prism4j.Grammar create(@NotNull Prism4j prism4j) {
    return grammar(
      "clike",
      token(
        "comment",
        pattern(compile("(^|[^\\\\])\\/\\*[\\s\\S]*?(?:\\*\\/|$)"), true),
        pattern(compile("(^|[^\\\\:])\\/\\/.*"), true, true)
      ),
      token(
        "string",
        pattern(compile("([\"'])(?:\\\\(?:\\r\\n|[\\s\\S])|(?!\\1)[^\\\\\\r\\n])*\\1"), false, true)
      ),
      token(
        "class-name",
        pattern(
          compile("((?:\\b(?:class|interface|extends|implements|trait|instanceof|new)\\s+)|(?:catch\\s+\\())[\\w.\\\\]+"),
          true,
          false,
          null,
          grammar("inside", token("punctuation", pattern(compile("[.\\\\]"))))
        )
      ),
      token(
        "keyword",
        pattern(compile("\\b(?:if|else|while|do|for|return|in|instanceof|function|new|try|throw|catch|finally|null|break|continue)\\b"))
      ),
      token("boolean", pattern(compile("\\b(?:true|false)\\b"))),
      token("function", pattern(compile("[a-z0-9_]+(?=\\()", Pattern.CASE_INSENSITIVE))),
      token(
        "number",
        pattern(compile("\\b0x[\\da-f]+\\b|(?:\\b\\d+\\.?\\d*|\\B\\.\\d+)(?:e[+-]?\\d+)?", Pattern.CASE_INSENSITIVE))
      ),
      token("operator", pattern(compile("--?|\\+\\+?|!=?=?|<=?|>=?|==?=?|&&?|\\|\\|?|\\?|\\*|\\/|~|\\^|%"))),
      token("punctuation", pattern(compile("[{}\\[\\];(),.:]")))
    );
  }

  private Prism_clike() {
  }
}
