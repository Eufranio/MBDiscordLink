# MBDiscordLink

MBDiscordLink is an addon for [MagiBridge](https://ore.spongepowered.org/Eufranio/MagiBridge) that adds an account linking system to it, as well as role syncing. I decided to not include it directly into MagiBridge because not everyone may use this feature, so it's a separate plugin. **The config file of MBDiscordLink is generated INSIDE the MagiBridge config folder.**

## Features
* Link Minecraft accounts with Discord accounts
* Adds users to a specific role once they link their account
* Run commands when a player links/unlinks his account
* Sync roles based on permissions for linked users
* All messages configurable

## Config
You can see an example of the current config [here](https://gist.github.com/Eufranio/c3f677794b297f6832c9b7a95a5f027b).

## Basic Setup
1. Customize your messages config node as you want
2. Set the default linked role on the plugin config. This role will be given to users once they link their accoint
3. Set the commands that should be run when players link/unlink their account
3. Optionally, set the roles to sync. The format is `"perm"="role-id"`. The permission can be anything you want, for example if you want to sync LuckPerms groups to roles, you can use the `group.<groupname>` permission, and the role you want associate to it. You can get the role ID by tagging the role on Discord with an `\` before it, like `\@Admins`. The ID is the numerical string. 
4. Ready to go! The plugin should be now working!

If you find any issues, report them to the [plugin's issue tracker](https://github.com/Eufranio/MBDiscordLink/issues). If you want, you can donate for me trough PayPal, my paypal email is **frani@magitechserver.com**.