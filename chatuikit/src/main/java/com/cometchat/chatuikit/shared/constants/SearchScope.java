package com.cometchat.chatuikit.shared.constants;

public enum SearchScope {
    CONVERSATIONS("conversations"),
    MESSAGES("messages");

    private final String value;

    SearchScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

