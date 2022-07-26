<div style="text-align: center">

# Chatty
[![Java CI with Gradle](https://github.com/MineInAbyss/Chatty/actions/workflows/gradle-ci.yml/badge.svg)](https://github.com/MineInAbyss/Chatty/actions/workflows/gradle-ci.yml)
[![Maven](https://img.shields.io/maven-metadata/v?metadataUrl=https://repo.mineinabyss.com/releases/com/mineinabyss/chatty/maven-metadata.xml)](https://repo.mineinabyss.com/#/releases/com/mineinabyss/chatty)
[![Wiki](https://img.shields.io/badge/-Project%20Wiki-blueviolet?logo=Wikipedia&labelColor=gray)](https://github.com/MineInAbyss/Chatty/wiki)
[![Contribute](https://shields.io/badge/Contribute-e57be5?logo=github%20sponsors&style=flat&logoColor=white)](https://github.com/MineInAbyss/MineInAbyss/wiki/Setup-and-Contribution-Guide)
</div>

## Overview
Chatty is a powerful chat-plugin specifically aimed at Paper-servers and built around MiniMessage.    
In addition it has support for Velocity and Bungee(soonTM). Much like VentureChat it does this by using the proxy as a bridge.  
This means the formatting is still done on the servers, and placeholders etc will still work.  

It is built on top of [Geary](https://github.com/MineInAbyss/Geary), our own Entity Component System (ECS).  
Right now it is primarily used to store channel-info and other preferences, but is useful for extending out to other plugins.  
Currently it is used in [MineInAbyss](https://github.com/MineInAbyss/MineInAbyss) for privated guild-chats for our Guild System.

__Small featurelist:__
- Global / Radius / Permission based channels
- A fourth Private channel intended for party/guild chat mainly for other plugins to use
- Nickname support via displayname (Make sure to disable this in your Essentials config if you use that)
- Nickname supports all MiniMessage tags aswell as spaces (Can be disabled just like any other tag)
- Pinging other players, which plays a configurable sound
- Player -> Player private messages

It also includes options for MiniMessage support in books and on signs.  

It also includes an "emotefixer" to translate emojis from Minecraft Chat to Discord.  
This feature essentially relies on DiscordSRV and is built around BondrewdLikesHisEmotes


## Requirements
- [Idofront Platform](https://github.com/MineInAbyss/Idofront)
- [Geary](https://github.com/MineInAbyss/Geary)  
#### Note: Only required on the servers, not the proxy

## Recommended addons
- [BondrewdLikesHisEmotes](https://github.com/MineInAbyss/BondrewdLikesHisEmotes) - Emojis/GIFs & everything else with unicodes  
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) - For common placeholders (Recommend devbuild [5.0.1](https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/target/ProtocolLib.jar))
- [DiscordSRV](https://github.com/DiscordSRV/DiscordSRV) - Send messages from Minecraft Chat to a Discord Server
