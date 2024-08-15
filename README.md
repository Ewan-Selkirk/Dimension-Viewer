# Dimension Viewer
A highly customizable server-side Minecraft Forge mod for viewing what dimension players are currently in.
> Now supporting Fabric, Forge & NeoForge!

## Configuration
By modifying the config file, you can change the colour* of the text on a per-dimension basis, allow showing the dimension in chat messages and change the overall format of the dimension with the ability to use Minecraft text formatting such as underlining, bold and italic fonts.

The config file is located in `[Server Folder]/config/dimensionviewer-common.toml` for (Neo)Forge or `[Server Folder]/config/dimensionviewer.json` for Fabric. Any changes made will be applied as soon as the config file is saved.
See below for an example config:


<details>
<summary>Example Configurations</summary>
<details>
<summary>(Neo)Forge Example</summary>

The (Neo)Forge configuration file contains comments for every setting and the valid options available for the setting.

```toml
#Customization Settings
[customization]
#The color to use for the dimension font if perDimColorPath is false.
#(In the event of a modded dimension being entered, this color will be used as a fallback)
fontColor = "DARK_AQUA"
#Global toggle for dimension aliases. Requires aliases to be set below.
enableAliases = true
#Format that will be used to display the dimension in the tab list with the use of tokens:
#    %d - Dimension Name
#    %i - Italic font
#    %b - Bold font
#    %u - Underline font
#    %o - Obfuscated font
#    %s - Strikethrough font
listFormat = "%i<%d>"
#Whether the dimension should be placed before or after the player name
#Allowed Values: PREPEND, APPEND
dimensionPosition = "APPEND"
#Should each dimension have its own color?
perDimColor = true

#Per-Dimension Customization
[customization.dimension]
    #Color to use for the Overworld
    #Allowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK
    overworldColor = "DARK_GREEN"
    #Color to use for the Nether
    #Allowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK
    netherColor = "DARK_RED"
    #Color to use for the End
    #Allowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK
    endColor = "FUTURE_PURPLE"

#Chat-related Customization
[customization.chat]
    #Should a users' current dimension be added to chat messages?
    dimInChatName = true
    #Add a hover effect in chat that will display which mod added the dimension
    #Requires 'dimInChatName' to be set to true
    chatDimHover = true

#Modded Dimension Customization
[customization.modded]
    #A list of aliases to use instead of the original dimension ID.
    #Uses the format 'modid:dim_id New Name'.
    #For example, to replace 'Overworld' with 'Grasslands' you would use 'minecraft:overworld Grasslands'
    dimensionAliases = ["minecraft:the_end ULTIMA THULE"]
    #A list of modded dimension resource IDs and a color in the format of "modid:dim_id color"
    #For example, Twilight Forest in Gold would be "twilightforest:twilight_forest GOLD"
    #Will throw an exception if the color is not valid
    #Allowed Values: DARK_RED, RED, GOLD, YELLOW, DARK_GREEN, GREEN, AQUA, DARK_AQUA, DARK_BLUE, BLUE, LIGHT_PURPLE, DARK_PURPLE, WHITE, GRAY, DARK_GRAY, BLACK
    moddedDimensionIds = ["twilightforest:twilight_forest GOLD"]

#Extra Customization
[customization.extra]
    #Custom colors can be defined here.
    #Uses the format 'COLOR_NAME #HEX' or 'COLOR_NAME r000 g000 b000'
    #If a custom color of the same name already exists the server will reject the newest one.
    #The name must be uppercase and can only contain letters and underscores.
    custom_colors = ["HOT_PINK #C62F75", "FUTURE_PURPLE r152 g154 b255"]
```

</details>
<details>
<summary>Fabric Configuration</summary>

The Fabric configuration is stored and laid out differently to the (Neo)Forge configuration file due to having to write the saving and loading functions manually. Everything is the same in terms of settings, but there are no comments as JSON doesn't support comments.
This is something that I would like to fix in the future but for now it works.

```json
{
  "LIST_FORMAT": "%i<%d>",
  "DIM_POSITION": "APPEND",
  "DEFAULT_COLOR": "HOT_PINK",
  "OVERWORLD_COLOR": "DARK_GREEN",
  "NETHER_COLOR": "FUTURE_PURPLE",
  "END_COLOR": "HOT_PINK",
  "PER_DIM_COLOR": true,
  "DIM_IN_CHAT_NAME": true,
  "CHAT_DIM_HOVER": true,
  "ENABLE_ALIASES": true,
  "MODDED_DIMS": ["twilightforest:twilight_forest GOLD"],
  "DIM_ALIASES": ["minecraft:overworld %bThe Grasslands", "minecraft:the_nether %u%d", "minecraft:the_end %oEnd"],
  "CUSTOM_COLORS": ["HOT_PINK #C62F75", "FUTURE_PURPLE r152 g154 b255"]
}
```

### Settings and What They Do
___
- `LIST_FORMAT`
  - The format that will be used to display the dimension in the player list or chat name. It supports a few tokens to tweak the layout.
    - `%d` - Dimension (required) 
    - `%i` - Italic
    - `%b` - Bold
    - `%u` - Underlined
    - `%s` - Strikethrough
    - `%o` - Obfuscate text
    - `%%` - Literal %
  - Any character following a `%` will be interpreted as a token and removed, so use `%%` if you want a percentage sign in the name
- `DIM_POSITION`
  - Sets the position the dimension is shown in relation to the players name.
    - `PREPEND` - Before the player name
    - `APPEND` - After the player name
- `DEFAULT_COLOR`
  - Sets the default font colour to use if `PER_DIM_COLOR` is false or is a custom dimension that is not defined in `MODDED_DIMS`
    - Accepts any vanilla text colour: `DARK_RED`, `RED`, `GOLD`, `YELLOW`, `DARK_GREEN`, `GREEN`, `AQUA`, `DARK_AQUA`, `DARK_BLUE`, `BLUE`, `LIGHT_PURPLE`, `DARK_PURPLE`, `WHITE`, `GRAY`, `DARK_GRAY`, `BLACK`
    - Also accepts the name of any custom colours defined in `CUSTOM_COLORS`
    - The names must be UPPERCASE
- `OVERWORLD_COLOR`, `NETHER_COLOR` & `END_COLOR`
  - Used to change the colors of vanilla dimensions when `PER_DIM_COLOR` is set to `true`
  - Accepts the same inputs as `DEFAULT_COLOR`
- `PER_DIM_COLOR`
  - Used to enable individual dimension colouring. Disabling this will display all dimensions the same colour as defined in `DEFAULT_COLOR`
- `DIM_IN_CHAT_NAME`
  - Used to toggle showing the dimension in a players name when sending chat messages
  - When turned off, the players chat name will be unaffected and only the tab list name will show the players current dimension
- `ENABLE_ALIASES`
  - Used to toggle whether custom dimension names should be used.
  - When enabled, any dimensions that match items in `DIM_ALIASES` will have their display names swapped for the aliased name.
- `MODDED_DIMS`
  - Used to change the colors of custom modded dimensions
  - Uses the format `modid:dim_id color`
  - For example, Twilight Forest in Gold would be `twilightforest:twilight_forest GOLD`
- `DIM_ALIASES`
  - Used to show a custom name for a given dimension
  - Uses the format `modid:dim_id New Name` (Spaces allowed)
  - For example, to replace `Overworld` with `The Grasslands` you would use `minecraft:overworld The Grasslands`
  - Has no effect if `ENABLE_ALIASES` is false
  - [VERSION 2.0.0+] Allows the use of tokens in the alias
    - Make only the overworld text bold with `minecraft:overworld %b%d`
- `CUSTOM_COLORS`
  - Used to define custom font colors in either HEX format or RGB format.
  - Uses the format `COLOR_NAME #HEX` for hexadecimal colours or `COLOR_NAME r000 g000 b000` for RGB colors.
    - The name of the color must be UPPERCASE 
    - Hexadecimal numbers must be 6 characters long. Truncated hex values and hex w/ alpha values are not supported at this moment.
  - For example, a custom pink hex color would be `HOT_PINK #C62F75`
  - Another example, a custom purple RGB color would be `FUTURE_PURPLE r152 g154 b255`

</details>
</details>

## Compatibility
Dimension Viewer should be compatible with all dimensions. There may be some cases where the mod name shown on hover is not formatted correctly due to the use of the modid to get which mod a dimension is from.

Mods that make changes to the tab list name format (or even the regular display name) will likely cause issues with this mod but at the moment I cannot think of any to test with. If you find any issues, please [leave an issue](https://github.com/Ewan-Selkirk/Dimension-Viewer/issues/new)! Thank you!

[//]: # (If there are any issues encountered &#40;no dimension being shown for a player&#41; either have someone change dimension once or use the command `/refreshPlayerList` to try and manually update the player list.)

<img src="img/DimensionViewerCompatibility.png" width="100%">

## Features
Modded dimensions can now be coloured independently.
<img src="img/ModdedDimensionColor.png">

Dimension names can now be aliased to something else, allowing you to modify the name displayed per dimension.
<img src="img/DimensionAliasing.png">

<a href="https://www.curseforge.com/minecraft/mc-mods/dimension-viewer">![CurseForge Link](https://cf.way2muchnoise.eu/title/857799(989AFF).svg?badge_style=for_the_badge)</a>
![CurseForge Versions](https://cf.way2muchnoise.eu/versions/857799_all(989AFF).svg?badge_style=for_the_badge)

<a href="https://modrinth.com/mod/dimension-viewer">![Modrinth Game Versions](https://img.shields.io/modrinth/game-versions/3aeFezQk?label=Modrinth&logo=Modrinth&style=for-the-badge)</a>

## Changelog
____
<details>
    <summary>View Changelog</summary>
    <details open>
        <summary>Version 2.0.0</summary>
        <ul>
            <li>Now supports Fabric!</li>
            <li>Now supports custom RGB and Hex font colours!</li>
            <li>Simplified the format configuration</li>
        </ul>
    </details>
    <details>
        <summary>Version 1.4.0</summary>
        <ul>
            <li>Added support for dimension aliases! (Custom dimension names)</li>
        </ul>
    </details>
    <details>
        <summary>Version 1.3.1</summary>
        <ul>
            <li>Fixed a regex bug causing modids with underscores to not be valid.</li>
        </ul>
    </details>
    <details>
        <summary>Version 1.3.0</summary>
        <ul>
            <li>Added support for colouring individual modded dimensions!</li>
            <li>Changed command 'refreshPlayerList' to 'refreshplayerlist' to fit better with vanilla command styling.</li>
        </ul>
    </details>
    <details>
        <summary>Version 1.2.0</summary>
        <ul>
            <li>Initial release!</li>
            (I can't remember why I started at 1.2.0...)
        </ul>
    </details>
</details>