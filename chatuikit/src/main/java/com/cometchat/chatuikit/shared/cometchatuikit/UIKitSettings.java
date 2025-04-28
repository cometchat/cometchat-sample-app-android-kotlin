package com.cometchat.chatuikit.shared.cometchatuikit;

import com.cometchat.chatuikit.ai.AIExtensionDataSource;
import com.cometchat.chatuikit.shared.framework.ExtensionsDataSource;
import com.cometchat.chatuikit.shared.interfaces.DateTimeFormatterCallback;

import java.util.List;

/**
 * The settings class for configuring the UIKit functionality.
 */
public class UIKitSettings {
    private static final String TAG = UIKitSettings.class.getSimpleName();
    private final String appId;
    private final String region;
    private final String subscriptionType;
    private final boolean autoEstablishSocketConnection;
    private final String authKey;
    private final List<String> roles;
    private final String overrideAdminHost;
    private final String overrideClientHost;
    private final List<AIExtensionDataSource> aiFeatures;
    private final List<ExtensionsDataSource> extensions;
    private final DateTimeFormatterCallback dateTimeFormatterCallback;

    /**
     * Constructs a new instance of `UIKitSettings` using the builder pattern.
     *
     * @param builder The builder object used to construct the settings.
     */
    private UIKitSettings(UIKitSettingsBuilder builder) {
        this.appId = builder.appId;
        this.region = builder.region;
        this.subscriptionType = builder.subscriptionType;
        this.autoEstablishSocketConnection = builder.autoEstablishSocketConnection;
        this.authKey = builder.authKey;
        this.roles = builder.roles;
        this.overrideAdminHost = builder.overrideAdminHost;
        this.overrideClientHost = builder.overrideClientHost;
        this.aiFeatures = builder.aiFeatures;
        this.extensions = builder.extensions;
        this.dateTimeFormatterCallback = builder.dateTimeFormatterCallback;
    }

    /**
     * Returns the app ID.
     *
     * @return The app ID.
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Returns the region where the app is hosted.
     *
     * @return The region where the app is hosted.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Returns the list of roles associated with the app.
     *
     * @return The list of roles associated with the app.
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Returns the subscription type for presence updates.
     *
     * @return The subscription type for presence updates.
     */
    public String getSubscriptionType() {
        return subscriptionType;
    }

    /**
     * Returns the authentication key for the app.
     *
     * @return The authentication key for the app.
     */
    public String getAuthKey() {
        return authKey;
    }

    public String getOverrideAdminHost() {
        return overrideAdminHost;
    }

    public String getOverrideClientHost() {
        return overrideClientHost;
    }

    /**
     * Returns whether the socket connection is automatically established.
     *
     * @return `true` if the socket connection is automatically established, `false`
     * otherwise.
     */
    public boolean isAutoEstablishSocketConnection() {
        return autoEstablishSocketConnection;
    }

    public List<AIExtensionDataSource> getAIFeatures() {
        return aiFeatures;
    }

    public List<ExtensionsDataSource> getExtensions() {
        return extensions;
    }

    public DateTimeFormatterCallback getDateTimeFormatterCallback() {
        return dateTimeFormatterCallback;
    }

    /**
     * Builder class for constructing `UIKitSettings` instances.
     */
    public static class UIKitSettingsBuilder {
        private String appId;
        private String region;
        private String subscriptionType = "NONE";
        private List<String> roles;
        private Boolean autoEstablishSocketConnection = true;
        private String authKey;
        private String overrideAdminHost;
        private String overrideClientHost;
        private List<AIExtensionDataSource> aiFeatures;
        private List<ExtensionsDataSource> extensions;
        private DateTimeFormatterCallback dateTimeFormatterCallback;

        /**
         * Constructs a new instance of `UIKitSettingsBuilder`.
         */
        public UIKitSettingsBuilder() {
        }

        /**
         * Builds a new instance of `UIKitSettings` using the provided configuration.
         *
         * @return A new instance of `UIKitSettings`.
         */
        public UIKitSettings build() {
            return new UIKitSettings(this);
        }

        /**
         * Sets the app ID.
         *
         * @param appId The app ID.
         * @return The builder object.
         */
        public UIKitSettingsBuilder setAppId(String appId) {
            this.appId = appId;
            return this;
        }

        /**
         * Sets the region where the app is hosted.
         *
         * @param region The region where the app is hosted.
         * @return The builder object.
         */
        public UIKitSettingsBuilder setRegion(String region) {
            this.region = region;
            return this;
        }

        /**
         * Sets the subscription type to subscribe to presence updates for all users.
         *
         * @return The builder object.
         */
        public UIKitSettingsBuilder subscribePresenceForAllUsers() {
            this.subscriptionType = "ALL_USERS";
            return this;
        }

        /**
         * Sets the subscription type to subscribe to presence updates for the specified
         * roles.
         *
         * @param roles The list of roles to subscribe to.
         * @return The builder object.
         */
        public UIKitSettingsBuilder subscribePresenceForRoles(List<String> roles) {
            this.subscriptionType = "ROLES";
            this.roles = roles;
            return this;
        }

        /**
         * Sets the subscription type to subscribe to presence updates for friends.
         *
         * @return The builder object.
         */
        public UIKitSettingsBuilder subscribePresenceForFriends() {
            this.subscriptionType = "FRIENDS";
            return this;
        }

        /**
         * Sets the list of roles associated with the app.
         *
         * @param roles The list of roles associated with the app.
         * @return The builder object.
         */
        public UIKitSettingsBuilder setRoles(List<String> roles) {
            this.roles = roles;
            return this;
        }

        /**
         * Sets whether to automatically establish a socket connection.
         *
         * @param autoEstablishSocketConnection Flag indicating whether to automatically establish a socket
         *                                      connection.
         * @return The builder object.
         */
        public UIKitSettingsBuilder setAutoEstablishSocketConnection(Boolean autoEstablishSocketConnection) {
            this.autoEstablishSocketConnection = autoEstablishSocketConnection;
            return this;
        }

        /**
         * Sets the authentication key for the app.
         *
         * @param authKey The authentication key for the app.
         * @return The builder object.
         */
        public UIKitSettingsBuilder setAuthKey(String authKey) {
            this.authKey = authKey;
            return this;
        }

        public UIKitSettingsBuilder overrideAdminHost(String adminHost) {
            this.overrideAdminHost = adminHost;
            return this;
        }

        public UIKitSettingsBuilder overrideClientHost(String clientHost) {
            this.overrideClientHost = clientHost;
            return this;
        }

        public UIKitSettingsBuilder setAIFeatures(List<AIExtensionDataSource> aiFeatures) {
            this.aiFeatures = aiFeatures;
            return this;
        }

        public UIKitSettingsBuilder setExtensions(List<ExtensionsDataSource> extensions) {
            this.extensions = extensions;
            return this;
        }

        public UIKitSettingsBuilder setDateTimeFormatterCallback(DateTimeFormatterCallback dateTimeFormatterCallback) {
            this.dateTimeFormatterCallback = dateTimeFormatterCallback;
            return this;
        }
    }
}
