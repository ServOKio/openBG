version = "1.0.0" // Plugin version. Increment this to trigger the updater
description = "This plugin allows you to change your banner without nitro. Your banner will be seen by users who have also installed this plugin" // Plugin description that will be shown to user

aliucord {
    // Changelog of your plugin
    changelog.set("""
        Just a first unload
    """.trimIndent())
    // Image or Gif that will be shown at the top of your changelog page
    // changelogMedia.set("https://cool.png")

    // Add additional authors to this plugin
    // author("Name", 0)
    // author("Name", 0)

    // Excludes this plugin from the updater, meaning it won't show up for users.
    // Set this if the plugin is unfinished
    excludeFromUpdaterJson.set(true)
}
